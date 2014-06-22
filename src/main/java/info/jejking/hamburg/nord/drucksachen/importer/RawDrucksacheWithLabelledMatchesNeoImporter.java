/* 
 *  Hamburg-Nord Geocoder, by John King.
 *  Copyright (C) 2014,  John King
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package info.jejking.hamburg.nord.drucksachen.importer;

import static info.jejking.hamburg.nord.geocoder.DrucksacheNames.DATE;
import static info.jejking.hamburg.nord.geocoder.DrucksacheNames.DRUCKSACHE;
import static info.jejking.hamburg.nord.geocoder.DrucksacheNames.DRUCKSACHE_ID;
import static info.jejking.hamburg.nord.geocoder.DrucksacheNames.HEADER;
import static info.jejking.hamburg.nord.geocoder.DrucksacheNames.IN_BODY;
import static info.jejking.hamburg.nord.geocoder.DrucksacheNames.IN_HEADER;
import static info.jejking.hamburg.nord.geocoder.DrucksacheNames.ORIGINAL_URL;
import static info.jejking.hamburg.nord.geocoder.DrucksacheNames.REF_LOCATION;
import info.jejking.hamburg.nord.drucksachen.allris.DrucksachenPropertyKeys;
import info.jejking.hamburg.nord.drucksachen.allris.RawDrucksache;
import info.jejking.hamburg.nord.drucksachen.matcher.Matches;
import info.jejking.hamburg.nord.drucksachen.matcher.RawDrucksacheWithLabelledMatches;
import info.jejking.hamburg.nord.geocoder.AbstractNeoImporter;
import info.jejking.hamburg.nord.geocoder.GazetteerNames;
import info.jejking.hamburg.nord.geocoder.GazetteerRelationshipTypes;

import java.util.Map;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;

import rx.functions.Action1;

/**
 * Class to import a {@link RawDrucksacheWithLabelledMatches} into Neo4j. The assumption is that
 * the gazetteer has already been prepared in the graph database, otherwise not much will happen. 
 * 
 * @author jejking
 *
 */
public final class RawDrucksacheWithLabelledMatchesNeoImporter extends AbstractNeoImporter<RawDrucksacheWithLabelledMatches> implements Action1<RawDrucksacheWithLabelledMatches>  {

    private final GraphDatabaseService graph;
    private final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("dd.MM.yyyy");

    RawDrucksacheWithLabelledMatchesNeoImporter(GraphDatabaseService graph) {
        this.graph = graph;
    }

    @Override
    public void call(RawDrucksacheWithLabelledMatches rawDrucksacheWithLabelledMatches) {
        this.writeToNeo(rawDrucksacheWithLabelledMatches, this.graph);
    }
    
    @Override
    public void writeToNeo(RawDrucksacheWithLabelledMatches rawDrucksacheWithLabelledMatches, GraphDatabaseService graph) {
        try (Transaction tx = graph.beginTx()) {
            
            Node drucksachenNode = createDrucksacheNode(rawDrucksacheWithLabelledMatches, graph);
            
            createRelationshipsToGazetteer(rawDrucksacheWithLabelledMatches, drucksachenNode, graph);
            
            tx.success();
        }
        
    }
    

    private void createRelationshipsToGazetteer(RawDrucksacheWithLabelledMatches rawDrucksacheWithLabelledMatches, Node drucksachenNode, GraphDatabaseService graph) {
        /*
         * Go through the labelled matches.
         */
        for (Map.Entry<String, Matches> matchesEntry : rawDrucksacheWithLabelledMatches.getMatchesMap().entrySet()) {
            String labelText = matchesEntry.getKey();
            Matches matches = matchesEntry.getValue();
            
            Label neoLabel =  DynamicLabel.label(labelText);
            
            // matches in header...
            for (String headerMatch : matches.getMatchesInHeader()) {
                createRelationship(neoLabel, drucksachenNode, headerMatch, IN_HEADER);
            }
            
            // matches in body...
            for (String bodyMatch : matches.getMatchesInBody()) {
                createRelationship(neoLabel, drucksachenNode, bodyMatch, IN_BODY);
            }
        }
        
    }

    private void createRelationship(Label neoLabel, Node drucksachenNode, String match, String relationshipProperty) {
        ResourceIterable<Node> targetResourceIterable = graph.findNodesByLabelAndProperty(neoLabel, GazetteerNames.NAME, match);
        for (Node targetNode : targetResourceIterable) {
             Relationship rel = drucksachenNode.createRelationshipTo(targetNode, GazetteerRelationshipTypes.REFERS_TO);
             rel.setProperty(REF_LOCATION, relationshipProperty);
        }
        
    }

    private Node createDrucksacheNode(RawDrucksacheWithLabelledMatches rawDrucksacheWithLabelledMatches, GraphDatabaseService graph) {
        Node drucksacheNode = graph.createNode();
        RawDrucksache original = rawDrucksacheWithLabelledMatches.getOriginal();
        // set some properties
        drucksacheNode.setProperty(DRUCKSACHE_ID, original.getDrucksachenId());
        drucksacheNode.setProperty(ORIGINAL_URL, original.getOriginalUrl().toExternalForm());
        if (original.getDate().isPresent()) {
            drucksacheNode.setProperty(DATE, this.dateFormat.print(original.getDate().get()));    
        }
        if (original.getExtractedProperties().containsKey(DrucksachenPropertyKeys.BETREFF)) {
            drucksacheNode.setProperty(HEADER, original.getExtractedProperties().get(DrucksachenPropertyKeys.BETREFF));
        }
        drucksacheNode.addLabel(DynamicLabel.label(DRUCKSACHE));
        return drucksacheNode;
    }

    
}