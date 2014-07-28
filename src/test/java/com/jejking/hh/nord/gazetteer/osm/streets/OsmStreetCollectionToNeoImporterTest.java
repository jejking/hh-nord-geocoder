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

import static com.jejking.hh.nord.gazetteer.GazetteerEntryTypes.STREET;
import static com.jejking.hh.nord.AbstractNeoImporter.GAZETTEER_FULLTEXT;
import static com.jejking.hh.nord.gazetteer.GazetteerPropertyNames.NAME;
import static com.jejking.hh.nord.gazetteer.GazetteerLayerNames.GEO;
import static com.jejking.hh.nord.gazetteer.GazetteerPropertyNames.TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jaitools.jts.CoordinateSequence2D;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.pipes.GeoPipeline;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

import com.jejking.hh.nord.GeographicFunctions;
import com.jejking.hh.nord.TestUtil;
import com.jejking.hh.nord.gazetteer.GazetteerEntryTypes;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;


public class OsmStreetCollectionToNeoImporterTest {

    private static GraphDatabaseService graph;
    private static SpatialDatabaseService spatialDatabaseService;
    
    @BeforeClass
    public static void init() {
        
        graph = TestUtil.createTestDatabase();
        spatialDatabaseService = new SpatialDatabaseService(graph);
        
        TestUtil.writeHamburgNordOsmStreetsToGraph(graph);
        
    }
    
    @Test
    public void writtenToNeo() {
        // retrieve Itzehoer Weg and Mundsburger Damm
        
        try (Transaction tx = graph.beginTx()) {
            
            try (ResourceIterator<Node> iterator = graph
                                                    .findNodesByLabelAndProperty(
                                                            DynamicLabel.label(GazetteerEntryTypes.STREET),
                                                            NAME, "Itzehoer Weg")
                                                    .iterator()) {
                Node hh = iterator.next();
                
                assertTrue(hh.hasLabel(DynamicLabel.label(STREET)));
                
                assertFalse(iterator.hasNext());
            }
            
            try (ResourceIterator<Node> iterator = graph
                    .findNodesByLabelAndProperty(
                            DynamicLabel.label(GazetteerEntryTypes.STREET),
                            NAME, "Mundsburger Damm")
                    .iterator()) {
                Node hh = iterator.next();
                
                assertTrue(hh.hasLabel(DynamicLabel.label(STREET)));
                assertFalse(iterator.hasNext());
            }
            
        }
        
    }
    
    @Test
    public void fullTextQuery() {
        try (Transaction tx = graph.beginTx()) {
            // test with get...
            Index<Node> fullText = graph.index().forNodes(GAZETTEER_FULLTEXT);
            
            int mundsCount = 0;
            boolean foundBruecke = false;
            boolean foundDamm = false;
            for (Node mundsburg : fullText.query(NAME + ":Mundsburg* AND " + TYPE + ":" + STREET)) {
                mundsCount++;  
                if (mundsburg.getProperty(NAME).equals("Mundsburger Brücke")) {
                    foundBruecke = true;
                }
                if (mundsburg.getProperty(NAME).equals("Mundsburger Damm")) {
                    foundDamm = true;
                }
            }
            
            assertEquals(2, mundsCount);
            assertTrue(foundBruecke);
            assertTrue(foundDamm);
        }
    }
    
    @Test
    public void spatialQuery() {
        Layer streets = spatialDatabaseService.getLayer(GEO);
        // bus stop Mundsburger Brücke stadtauswärts (37, 172)
        Point point = new Point(new CoordinateSequence2D(10.0206119, 53.5660032), streets.getGeometryFactory());
        
        Envelope env = GeographicFunctions.computeEnvelopeAroundPoint(point, 50);
        
        int streetCount = 0;
        boolean foundHartwicus = false;
        boolean foundMundsburgerBruecke = false;
        boolean foundMundsburgerDamm = false;
        boolean foundArmgartstraße = false;
        
        try (Transaction tx = graph.beginTx()) {
            List<Node> nodes = GeoPipeline
                                    .startIntersectWindowSearch(streets, env)
                                    .toNodeList();
            for (Node streetNode : nodes) {
                String streetName = (String) streetNode.getProperty(NAME);
                streetCount++;
                switch (streetName) {
                    case "Hartwicusstraße" : foundHartwicus = true; break;
                    case "Mundsburger Brücke" : foundMundsburgerBruecke = true; break;
                    case "Mundsburger Damm" : foundMundsburgerDamm = true; break;
                    case "Armgartstraße" : foundArmgartstraße = true; break;
                    default : continue;
                }
            }
            assertEquals(4, streetCount);
            assertTrue(foundHartwicus);
            assertTrue(foundMundsburgerBruecke);
            assertTrue(foundMundsburgerDamm);
            assertTrue(foundArmgartstraße);
        }
        
    }
    
    

    @AfterClass
    public static void tearDown() {
        graph.shutdown();
    }

}
