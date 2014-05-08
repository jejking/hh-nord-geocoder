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
package info.jejking.hamburg.nord.geocoder.hh;

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

/**
 * Tests mapping of streets to administrative polygons of
 * type {@link GazetteerEntryTypes#NUMBERED_DISTRICT}, the lowest level.
 * 
 * @author jejking
 *
 */
public class StreetToPolygonMapperTest {

    private static GraphDatabaseService graphDatabaseService;
    
    @BeforeClass
    public static void init() {
        graphDatabaseService = TestUtil.createTestDatabase();
        System.out.println("created database");
        TestUtil.writeHamburgPolygonsToGraph(graphDatabaseService);
        System.out.println("wrote polygons");
        TestUtil.writeOsmStreetsToGraph(graphDatabaseService);
        System.out.println("wrote streets");
        
        StreetToPolygonMapper mapper = new StreetToPolygonMapper();
        mapper.mapStreetsToPolygons(graphDatabaseService);
        
        System.out.println("mapped streets to districts");
    }
    
   
    
    @Test
    public void peterstrasseIn106() {
        try(Transaction tx = graphDatabaseService.beginTx()) {
            Node peterStr = getNode("Peterstraße");
            
            
            printNode(peterStr);
            
            assertTrue(peterStr.hasRelationship(Direction.OUTGOING, GazetteerRelationshipTypes.CONTAINED_IN));
            assertTrue(peterStr.hasRelationship(Direction.INCOMING, GazetteerRelationshipTypes.CONTAINS));
            
            Node node106 = peterStr.getRelationships(Direction.OUTGOING, GazetteerRelationshipTypes.CONTAINED_IN)
                       .iterator().next().getEndNode();
            assertEquals("106", node106.getProperty(GazetteerNames.NAME));
            
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
            
            Iterator<Relationship> sRels = schwanenwik.getRelationships(Direction.OUTGOING, GazetteerRelationshipTypes.CONTAINED_IN).iterator();
            while (sRels.hasNext()) {
                Relationship rel = sRels.next(); {
                    String endNodeName = (String) rel.getEndNode().getProperty(GazetteerNames.NAME);
                    switch (endNodeName) {
                        case "415" : containedIn415 = true; break;
                        case "416" : containedIn416 = true; break;
                        default : fail("Contained in " + endNodeName + ", which is wrong");
                    }
                }
                Node endNode = rel.getEndNode();
                assertTrue(nodeContainsStreet(endNode, "Schwanenwik"));
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
            if (rel.getEndNode().getProperty(GazetteerNames.NAME).equals(streetName)) {
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
                                        GazetteerNames.NAME,
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