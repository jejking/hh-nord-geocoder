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
package com.jejking.hh.nord.gazetteer.osm;

import static com.jejking.hh.nord.gazetteer.GazetteerNames.GAZETTEER_FULLTEXT;
import static com.jejking.hh.nord.gazetteer.GazetteerNames.NAME;
import static com.jejking.hh.nord.gazetteer.GazetteerNames.TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

import com.jejking.hh.nord.TestUtil;
import com.jejking.hh.nord.gazetteer.GazetteerEntryTypes;
import com.jejking.hh.nord.gazetteer.GazetteerNames;
import com.jejking.hh.nord.gazetteer.GazetteerRelationshipTypes;
import com.jejking.hh.nord.gazetteer.osm.PointOfInterestToNeoImporter;

/**
 * Tests {@link PointOfInterestToNeoImporter}.
 * 
 * @author jejking
 *
 */
public class PointOfInterestToNeoImporterTest {

    private static GraphDatabaseService graph;

    @BeforeClass
    public static void init() {
        graph = TestUtil.createTestDatabase();
        TestUtil.writeUhlenhorstOsmStreetsToGraph(graph);
        TestUtil.writeUhlenhorstPoisToGraph(graph);
        
    }
    
    @Test
    public void nodesWereCreatedInNeo() {
        try (Transaction tx = graph.beginTx()) {
            
            try (ResourceIterator<Node> iterator = graph
                                                    .findNodesByLabelAndProperty(
                                                            DynamicLabel.label(GazetteerEntryTypes.SCHOOL),
                                                            NAME, "Gymnasium Lerchenfeld")
                                                    .iterator()) {
                Node school = iterator.next();
                
                assertTrue(school.hasLabel(DynamicLabel.label(GazetteerEntryTypes.SCHOOL)));
                assertFalse(iterator.hasNext());
            }
        }
    }
    
    @Test
    public void nodesWereFullTextIndexed() {
        try (Transaction tx = graph.beginTx()) {
            // test with get...
            Index<Node> fullText = graph.index().forNodes(GAZETTEER_FULLTEXT);
            
            int schoolCount = 0;
            boolean foundGymnasiumLerchenfeld = false;
            for (Node school : fullText.query(NAME + ":Gymnasium* AND " + TYPE + ":" + GazetteerEntryTypes.POINT_OF_INTEREST)) {
                if (school.getProperty(NAME).equals("Gymnasium Lerchenfeld")) {
                    foundGymnasiumLerchenfeld = true;
                }
                schoolCount++;
            }
            assertEquals(1, schoolCount);
            assertTrue(foundGymnasiumLerchenfeld);
        }
    }
    
    @Test
    public void nodesAreLinkedToStreetsWhereAppropriate() {
        try (Transaction tx = graph.beginTx()) {
            
            try (ResourceIterator<Node> iterator = graph
                                                    .findNodesByLabelAndProperty(
                                                            DynamicLabel.label(GazetteerEntryTypes.SCHOOL),
                                                            NAME, "Gymnasium Lerchenfeld")
                                                    .iterator()) {
                Node school = iterator.next();
                
                Relationship contains = school.getRelationships(GazetteerRelationshipTypes.CONTAINS, Direction.INCOMING).iterator().next();
                Node lerchenfeld = contains.getStartNode();
                assertTrue(lerchenfeld.hasLabel(DynamicLabel.label(GazetteerEntryTypes.STREET)));
                assertEquals("Lerchenfeld", lerchenfeld.getProperty(NAME));
                
                assertEquals(lerchenfeld, contains.getOtherNode(school));
                assertEquals("10", contains.getProperty(GazetteerNames.HOUSE_NUMBER));
                
            }
        }
    }

}
