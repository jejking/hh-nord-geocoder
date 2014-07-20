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
package com.jejking.hh.nord.gazetteer.osm.streets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;

import com.jejking.hh.nord.TestUtil;
import com.jejking.hh.nord.gazetteer.GazetteerEntryTypes;
import com.jejking.hh.nord.gazetteer.GazetteerPropertyNames;
import com.jejking.hh.nord.gazetteer.GazetteerRelationshipTypes;
import com.jejking.hh.nord.gazetteer.osm.streets.StreetToAdminPolygonMapper;

/**
 * Tests mapping of streets to administrative polygons of
 * type {@link GazetteerEntryTypes#NUMBERED_DISTRICT}, the lowest level.
 * 
 * @author jejking
 *
 */
public class StreetToAdminPolygonMapperTest {

    private static GraphDatabaseService graphDatabaseService;
    
    @BeforeClass
    public static void init() {
        graphDatabaseService = TestUtil.createTestDatabase();
        System.out.println("created database");
        TestUtil.writeHamburgPolygonsToGraph(graphDatabaseService);
        System.out.println("wrote polygons");
        TestUtil.writeHamburgNordOsmStreetsToGraph(graphDatabaseService);
        System.out.println("wrote streets");
        
        StreetToAdminPolygonMapper mapper = new StreetToAdminPolygonMapper();
        mapper.mapStreetsToPolygons(graphDatabaseService);
        
        System.out.println("mapped streets to districts");
    }
    
   
    
    @Test
    public void peterstrasseIn106() {
        try(Transaction tx = graphDatabaseService.beginTx()) {
            Node peterStr = getNode("Peterstraße");
            
            
            printNode(peterStr);
            
            assertTrue(peterStr.hasRelationship(Direction.INCOMING, GazetteerRelationshipTypes.CONTAINS));
            
            Node node106 = peterStr.getRelationships(Direction.INCOMING, GazetteerRelationshipTypes.CONTAINS)
                       .iterator().next().getStartNode();
            assertEquals("106", node106.getProperty(GazetteerPropertyNames.NUMBER));
            
            boolean found106ContainsRelForPeterstr = nodeContainsStreet(node106, "Peterstraße");
            
            assertTrue(found106ContainsRelForPeterstr);
        }
    }
    
    @Test
    public void schwanenwikIn415and416() {
        try(Transaction tx = graphDatabaseService.beginTx()) {
            Node schwanenwik = getNode("Schwanenwik");
            
            
            printNode(schwanenwik);
            
            boolean containedIn415 = false;
            boolean containedIn416 = false;
            
            Iterator<Relationship> sRels = schwanenwik.getRelationships(Direction.INCOMING, GazetteerRelationshipTypes.CONTAINS).iterator();
            while (sRels.hasNext()) {
                Relationship rel = sRels.next(); {
                    String startNodeName = (String) rel.getStartNode().getProperty(GazetteerPropertyNames.NUMBER);
                    switch (startNodeName) {
                        case "415" : containedIn415 = true; break;
                        case "416" : containedIn416 = true; break;
                        default : fail("Contained in " + startNodeName + ", which is wrong");
                    }
                }
                Node startNode = rel.getStartNode();
                assertTrue(nodeContainsStreet(startNode, "Schwanenwik"));
            }
            
            assertTrue(containedIn415);
            assertTrue(containedIn416);
        }
    }



    private boolean nodeContainsStreet(Node containingNode, String streetName) {
        boolean foundStreetInNodeRelationships = false;
        Iterator<Relationship> nodeContainsRelIterator = containingNode
                                                                .getRelationships(
                                                                        Direction.OUTGOING,
                                                                        GazetteerRelationshipTypes.CONTAINS)
                                                                .iterator();
        while (nodeContainsRelIterator.hasNext()) {
            Relationship rel = nodeContainsRelIterator.next();
            if (rel.getEndNode().getProperty(GazetteerPropertyNames.NAME).equals(streetName)) {
                foundStreetInNodeRelationships = true;
                break;
            }
        }
        return foundStreetInNodeRelationships;
    }



    private Node getNode(String streetName) {
        ResourceIterable<Node> streetIterable = 
                graphDatabaseService.findNodesByLabelAndProperty(
                                        DynamicLabel.label(GazetteerEntryTypes.STREET), 
                                        GazetteerPropertyNames.NAME,
                                        streetName);
        
        Node streetNode = streetIterable.iterator().next();
        return streetNode;
    }
    
    
    private void printNode(Node node) {
        Iterator<String> propKeys = node.getPropertyKeys().iterator();
        while (propKeys.hasNext()) {
            System.out.println(node.getProperty(propKeys.next()));
        }
        
        Iterator<Relationship> rels = node.getRelationships().iterator();
        while (rels.hasNext()) {
            Relationship rel = rels.next();
            if (rel.getStartNode().equals(node)) {
                System.out.println("outgoing");
            } else {
                System.out.println("incoming");
            }
            System.out.println(rel.getType());
            if (rel.getOtherNode(node).hasProperty("NAME")) {
                System.out.println(rel.getOtherNode(node).getProperty("NAME"));
            }
        }
    }
}
