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
import info.jejking.hamburg.nord.geocoder.GeographicFunctions;

import java.util.Iterator;
import java.util.List;

import org.jaitools.jts.CoordinateSequence2D;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.SpatialRecord;
import org.neo4j.gis.spatial.pipes.GeoPipeline;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.test.TestGraphDatabaseFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Tests that the adminstrative polygons are imported 
 * correctly into the Neo4j database.
 * 
 * @author jejking
 *
 */
public class HamburgPolygonTreeToNeoImporterTest {

    private static GraphDatabaseService graph;
    private static NamedNode<Polygon> polygonHamburg;
    private static ExecutionEngine executionEngine;
    
    private static SpatialDatabaseService spatialDatabaseService;
    
    @BeforeClass
    public static void init() {
        
        CoordinateConverter converter = new CoordinateConverter();
        
        graph = new TestGraphDatabaseFactory()
                    .newImpermanentDatabaseBuilder()
                    .newGraphDatabase();
        
        buildIndexes();
        
        
        executionEngine = new ExecutionEngine(graph);
        spatialDatabaseService = new SpatialDatabaseService(graph);
        
        HamburgRawTreeBuilder builder = new HamburgRawTreeBuilder();
        NamedNode<String> hh = builder.buildRawTree();
        polygonHamburg = converter.fixRoot(converter.rawToPolygon(hh));
        
        HamburgPolygonTreeToNeoImporter importer = new HamburgPolygonTreeToNeoImporter();
        importer.writeToNeo(polygonHamburg, graph);
    }
    
    private static void buildIndexes() {
        
        // we want an additional index on adminstrative area - name
        try (Transaction tx = graph.beginTx()) {
            Schema schema = graph.schema();
            schema
                .indexFor(DynamicLabel.label(GazetteerEntryTypes.ADMIN_AREA))
                .on("NAME")
                .create();
            tx.success();
        }
    }

    @AfterClass
    public static void tearDown() {
        graph.shutdown();
    }

    @Test
    public void isHamburgThere() {
        try (Transaction tx = graph.beginTx()) {
            
            try (ResourceIterator<Node> iterator = graph
                                                    .findNodesByLabelAndProperty(
                                                            DynamicLabel.label(GazetteerEntryTypes.ADMIN_AREA),
                                                            "NAME", "Hamburg")
                                                    .iterator()) {
                Node hh = iterator.next();
                
                assertTrue(hh.hasLabel(DynamicLabel.label(GazetteerEntryTypes.CITY)));
                
                int countRels = 0;
                Iterator<Relationship> hhRels = hh.getRelationships(Direction.OUTGOING, GazetteerRelationshipTypes.CONTAINS).iterator();
                while (hhRels.hasNext()) {
                    hhRels.next();
                    countRels++;
                }
                assertEquals(7,  countRels);
            }
        }
    }
    
    @Test
    public void areBoroughsThere() {
        try (Transaction tx = graph.beginTx()) {
            
            String query = "MATCH (b:BOROUGH) RETURN b";
            
            ExecutionResult result = executionEngine.execute(query);
            Iterator<Node> boroughNodes = result.columnAs( "b" );
            
            int stadtTeilCount = 0;
            while (boroughNodes.hasNext()) {
                Node boroughNode = boroughNodes.next();
                assertTrue(boroughNode.hasRelationship(Direction.INCOMING, GazetteerRelationshipTypes.CONTAINED_IN)); // from HH
                Iterator<Relationship> boroughRels = boroughNode.getRelationships(Direction.OUTGOING, GazetteerRelationshipTypes.CONTAINS).iterator();
                while (boroughRels.hasNext()) {
                    boroughRels.next();
                    stadtTeilCount++;
                }
                
            }
            assertEquals(104, stadtTeilCount);
        }
    }
    
    @Test
    public void isUhlenhorstThere() {
        try (Transaction tx = graph.beginTx()) {
            
            String query = "MATCH (u:NAMED_AREA { NAME:'Uhlenhorst' }) RETURN u";
            
            ExecutionResult result = executionEngine.execute(query);
            Iterator<Node> boroughNodes = result.columnAs("u");
            
            Node uhlenhorst = boroughNodes.next();
            // is uhlenhorst contained in Nord?
            Node nord1 = uhlenhorst.getSingleRelationship(GazetteerRelationshipTypes.CONTAINED_IN, Direction.OUTGOING).getEndNode();
            assertEquals("Hamburg-Nord", nord1.getProperty("NAME"));
            
            // does Hamburg-Nord contain uhlenhorst?
            Node nord2 = uhlenhorst.getSingleRelationship(GazetteerRelationshipTypes.CONTAINS, Direction.INCOMING).getStartNode();
            assertEquals("Hamburg-Nord", nord2.getProperty("NAME"));
            
            // recall that uhlenhorst has 2 numbered districts, 414 and 415
            Iterator<Relationship> uhlenhorstRels = uhlenhorst.getRelationships(GazetteerRelationshipTypes.CONTAINS, Direction.OUTGOING).iterator();
            boolean found414 = false;
            boolean found415 = false;
            
            int uhlenhorstRelCount = 0;
            while (uhlenhorstRels.hasNext()) {
                Node ortsTeil = uhlenhorstRels.next().getEndNode();
                uhlenhorstRelCount++;
                if (ortsTeil.getProperty("NAME").equals("414")) {
                    found414 = true;
                }
                if (ortsTeil.getProperty("NAME").equals("415")) {
                    found415 = true;
                }
            }
            assertEquals(2, uhlenhorstRelCount);
            assertTrue(found414);
            assertTrue(found415);
        }
    }
    
    @Test
    public void spatialQueryOneHundredMetresOfLiteraturHausHamburg() {
        // Literaturhaus is at 53.568118, 10.016442 according to Google Maps
        Layer administrative = spatialDatabaseService.getLayer(HamburgPolygonTreeToNeoImporter.ADMINISTRATIVE);
        
        Point point = new Point(new CoordinateSequence2D(10.016442, 53.568118), administrative.getGeometryFactory());
        
        
        // we should find 415, Uhlenhorst, Hamburg-Nord and Hamburg ...
        boolean found415 = false;
        boolean foundUhlenhorst = false;
        boolean foundHamburgNord = false;
        boolean foundHamburg = false;
        
        Envelope env = GeographicFunctions.computeEnvelopeAroundPoint(point, 100);
        try (Transaction tx = graph.beginTx()) {
            List<Node> nodes = GeoPipeline
            						.startIntersectWindowSearch(administrative, env)
            						.toNodeList();
            for (Node node : nodes) {
                switch ((String) node.getProperty("NAME")) {
                	case "415" : found415 = true; break;
                	case "Uhlenhorst" : foundUhlenhorst = true; break;
                	case "Hamburg-Nord" : foundHamburgNord = true; break;
                	case "Hamburg" : foundHamburg = true; break;
                }
            }
            tx.success();
        }
        
        assertTrue(found415);
        assertTrue(foundUhlenhorst);
        assertTrue(foundHamburgNord);
        assertTrue(foundHamburg);
    }

    
}
