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

package com.jejking.hh.nord.matcher;

import static com.jejking.hh.nord.drucksachen.DrucksacheNames.DATE;
import static com.jejking.hh.nord.drucksachen.DrucksacheNames.DRUCKSACHE;
import static com.jejking.hh.nord.drucksachen.DrucksacheNames.DRUCKSACHE_ID;
import static com.jejking.hh.nord.drucksachen.DrucksacheNames.HEADER;
import static com.jejking.hh.nord.drucksachen.DrucksacheNames.REFS_BODY;
import static com.jejking.hh.nord.drucksachen.DrucksacheNames.REFS_HEADER;
import static com.jejking.hh.nord.drucksachen.DrucksacheNames.ORIGINAL_URL;
import static com.jejking.hh.nord.gazetteer.GazetteerPropertyNames.NAME;
import static com.jejking.hh.nord.gazetteer.GazetteerPropertyNames.TYPE;

import java.util.Map;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

import com.google.common.base.Optional;
import com.jejking.hh.nord.AbstractNeoImporter;
import com.jejking.hh.nord.drucksachen.DrucksachenPropertyKeys;
import com.jejking.hh.nord.drucksachen.RawDrucksache;
import com.jejking.hh.nord.gazetteer.GazetteerPropertyNames;
import com.jejking.hh.nord.gazetteer.GazetteerRelationshipTypes;

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

    public RawDrucksacheWithLabelledMatchesNeoImporter(GraphDatabaseService graph) {
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
            for (String headerMatch : matches.getMatchesInHeader().keySet()) {
                createRelationship(neoLabel, drucksachenNode, headerMatch, matches.getMatchesInHeader().get(headerMatch), REFS_HEADER);
            }
            
            // matches in body...
            for (String bodyMatch : matches.getMatchesInBody().keySet()) {
                createRelationship(neoLabel, drucksachenNode, bodyMatch, matches.getMatchesInBody().get(bodyMatch), REFS_BODY);
            }
        }
        
    }

    private void createRelationship(Label neoLabel, Node drucksachenNode, String match, Integer matchCount, String relationshipProperty) {
        
        // find all matches of appropriate type in the neo4j database. Exact match
        ResourceIterable<Node> nodesFromExactMatch = graph.findNodesByLabelAndProperty(neoLabel, GazetteerPropertyNames.NAME, match);
        
        if (nodesFromExactMatch.iterator().hasNext()) {
            createRelationshipsForNodes(drucksachenNode, matchCount, relationshipProperty, nodesFromExactMatch);    
        } else {
            System.out.println("Falling back to full text for match " + match);
            Index<Node> fullText = graph.index().forNodes(GAZETTEER_FULLTEXT);
            // we truncate here as the morphological expansion *adds* suffixes, so take them off..
            String truncatedMatch = match.substring(0, match.length() - 2);
            ResourceIterable<Node> nodesFromFullTextSearch = fullText.query(NAME + ":" + truncatedMatch + "* AND " + TYPE + ":" + neoLabel.name());
            createRelationshipsForNodes(drucksachenNode, matchCount, relationshipProperty, nodesFromFullTextSearch);   
        }
        
        
        
    }

    private void createRelationshipsForNodes(Node drucksachenNode, Integer matchCount, String relationshipProperty,
            ResourceIterable<Node> targetResourceIterable) {
        for (Node targetNode : targetResourceIterable) {
            Relationship rel = null; 
            Optional<Relationship> existingRelationship = getRelationship(drucksachenNode, targetNode);
            if (existingRelationship.isPresent()) {
                rel = existingRelationship.get();
            } else {
                rel = drucksachenNode.createRelationshipTo(targetNode, GazetteerRelationshipTypes.REFERS_TO);
            }
            rel.setProperty(relationshipProperty, matchCount);
        }
    }

    private Optional<Relationship> getRelationship(Node drucksachenNode, Node targetNode) {
        Relationship targetRel = null;
        Iterable<Relationship> relationshipIterable = drucksachenNode.getRelationships(GazetteerRelationshipTypes.REFERS_TO, Direction.OUTGOING);
        for (Relationship rel : relationshipIterable) {
            if (rel.getEndNode().equals(targetNode)) {
                targetRel = rel;
                break;
            }
        }
        return Optional.fromNullable(targetRel);
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