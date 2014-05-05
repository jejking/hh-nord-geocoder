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

import static info.jejking.hamburg.nord.geocoder.hh.GazetteerEntryTypes.ADMIN_AREA;
import static info.jejking.hamburg.nord.geocoder.hh.GazetteerEntryTypes.STREET;
import static info.jejking.hamburg.nord.geocoder.hh.GazetteerNames.GAZETTEER_FULLTEXT;

import java.util.Map;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.test.TestGraphDatabaseFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Helper class to set up database and load data.
 * 
 * @author jejking
 *
 */
class TestUtil {

    /*
     * Creates fresh test database with some
     * standard schema and full-text indexes set up already. 
     */
    static GraphDatabaseService createTestDatabase() {
        GraphDatabaseService graph =  new TestGraphDatabaseFactory()
                                                        .newImpermanentDatabaseBuilder()
                                                        .newGraphDatabase();
        // we want an additional index on adminstrative area - name
        try (Transaction tx = graph.beginTx()) {
            Schema schema = graph.schema();
            schema
                .indexFor(DynamicLabel.label(ADMIN_AREA))
                .on("NAME")
                .create();
            
            schema
                .indexFor(DynamicLabel.label(STREET))
                .on("NAME")
                .create();
            
            
            IndexManager indexManager = graph.index();
            @SuppressWarnings("unused")
            Index<Node> fullText = indexManager.forNodes(GAZETTEER_FULLTEXT,
                                MapUtil.stringMap(IndexManager.PROVIDER, "lucene",
                                                  "type", "fulltext"));
            
            tx.success();
        }
        
        return graph;
        
    }
    
    static void writeHamburgPolygonsToGraph(GraphDatabaseService graph) {
        CoordinateConverter converter = new CoordinateConverter();
        
        HamburgRawTreeBuilder builder = new HamburgRawTreeBuilder();
        NamedNode<String> hh = builder.buildRawTree();
        NamedNode<Polygon> polygonHamburg = converter.fixRoot(converter.rawToPolygon(hh));
        
        HamburgPolygonTreeToNeoImporter importer = new HamburgPolygonTreeToNeoImporter();
        importer.writeToNeo(polygonHamburg, graph);
    }
    
    static void writeOsmStreetsToGraph(GraphDatabaseService graph) {
        OsmStreetCollectionBuilder builder = new OsmStreetCollectionBuilder();
        Map<String, Geometry> streets = builder.buildRawStreetCollection();
        
        OsmStreetCollectionToNeoImporter importer = new OsmStreetCollectionToNeoImporter();
        importer.writeToNeo(streets, graph);
    }
    
}
