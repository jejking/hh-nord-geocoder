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
package com.jejking.hh.nord.gazetteer.osm.poi;

import static com.jejking.hh.nord.gazetteer.GazetteerPropertyNames.HOUSE_NUMBER;
import static com.jejking.hh.nord.gazetteer.GazetteerPropertyNames.NAME;
import static com.jejking.hh.nord.gazetteer.GazetteerLayerNames.GEO;
import static com.jejking.hh.nord.gazetteer.GazetteerPropertyNames.TYPE;

import java.util.List;

import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

import com.jejking.hh.nord.AbstractNeoImporter;
import com.jejking.hh.nord.gazetteer.GazetteerEntryTypes;
import com.jejking.hh.nord.gazetteer.GazetteerRelationshipTypes;

/**
 * Writes {@link PointOfInterest} instances to Neo4j. In particular,
 * connections are made, where possible, to street instances already in the
 * database.
 * 
 * @author jejking
 */
public class PointOfInterestToNeoImporter extends AbstractNeoImporter<List<PointOfInterest>> {

    public void writeToNeo(List<PointOfInterest> pois, GraphDatabaseService graph) {
        SpatialDatabaseService spatialDatabaseService = new SpatialDatabaseService(graph);
        
        int i = 0;
        for (PointOfInterest poi : pois) {
            try (Transaction tx = graph.beginTx()) {
                EditableLayer poiLayer = getEditableLayer(spatialDatabaseService, GEO);
                Index<Node> fullText = graph.index().forNodes(GAZETTEER_FULLTEXT);
                
                addPoi(poi, poiLayer, fullText);
                tx.success();
                i++;
                
                if ( i % 1000 == 0) {
                	System.out.println("Written " + i + " points of interest of " + pois.size());
                }
            } catch (Exception e) {
            	e.printStackTrace();
			}
        }
        
    }

    private void addPoi(PointOfInterest poi, EditableLayer poiLayer, Index<Node> fullText) {

        try {
        	SpatialDatabaseRecord record = createSpatialDatabaseRecord(poi, poiLayer);
        	Node neoNode = record.getGeomNode();
            
            labelNode(poi, neoNode);
            
            linkNodeToStreet(poi, neoNode, fullText.getGraphDatabase());
            
            doFullTextIndexing(poi, neoNode, fullText);
        } catch (Exception e) {
        	System.err.println("Error inserting poi: " + poi);
        	e.printStackTrace();
        }
        
        
        
    }

    private void linkNodeToStreet(PointOfInterest poi, Node neoNode, GraphDatabaseService graphDatabaseService) {
        if (poi.getStreet().isPresent()) {
            
            try (ResourceIterator<Node> iterator = graphDatabaseService
                    .findNodesByLabelAndProperty(
                            DynamicLabel.label(GazetteerEntryTypes.STREET),
                            NAME, poi.getStreet().get()).iterator()) {
            	
            	if (iterator.hasNext()) {
            		Node streetNode = iterator.next();
            		if (streetNode != null) {
                        streetNode.createRelationshipTo(neoNode, GazetteerRelationshipTypes.CONTAINS);
                    }
            	} else {
            		System.err.println("No result for street " + poi.getStreet().get());
            	}
            	if (iterator.hasNext()) {
            		System.err.println("More than one result for street " + poi.getStreet().get());
            	}
            	
            }

        }
        
    }

    private void doFullTextIndexing(PointOfInterest poi, Node neoNode, Index<Node> fullText) {
        if (poi.getName().isPresent()) {
            fullText.add(neoNode, NAME, poi.getName().get());
            fullText.add(neoNode, TYPE, poi.getLabel());
        }
        
    }

    private void labelNode(PointOfInterest poi, Node neoNode) {
        neoNode.addLabel(DynamicLabel.label(poi.getLabel()));
    }

    private SpatialDatabaseRecord createSpatialDatabaseRecord(PointOfInterest poi, EditableLayer poiLayer) {
        SpatialDatabaseRecord record = poiLayer.add(poi.getPoint());
        Node node = record.getGeomNode();
        if (poi.getName().isPresent()) {
            node.setProperty(NAME, poi.getName().get());
        }
        if (poi.getHouseNumber().isPresent()) {
            node.setProperty(HOUSE_NUMBER, poi.getHouseNumber().get());
        }
        
        return record;
    }

    
    
}
