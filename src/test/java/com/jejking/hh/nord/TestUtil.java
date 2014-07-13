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
package com.jejking.hh.nord;

import static com.jejking.hh.nord.AbstractNeoImporter.setupSchema;
import static com.jejking.hh.nord.gazetteer.GazetteerEntryTypes.ADMIN_AREA;
import static com.jejking.hh.nord.gazetteer.GazetteerEntryTypes.BOROUGH;
import static com.jejking.hh.nord.gazetteer.GazetteerEntryTypes.NAMED_AREA;
import static com.jejking.hh.nord.gazetteer.GazetteerEntryTypes.POINT_OF_INTEREST;
import static com.jejking.hh.nord.gazetteer.GazetteerEntryTypes.STREET;
import static com.jejking.hh.nord.gazetteer.GazetteerNames.GAZETTEER_FULLTEXT;


import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.test.TestGraphDatabaseFactory;

import com.jejking.hh.nord.gazetteer.opendata.CoordinateConverter;
import com.jejking.hh.nord.gazetteer.opendata.HamburgPolygonTreeToNeoImporter;
import com.jejking.hh.nord.gazetteer.opendata.HamburgRawTreeBuilder;
import com.jejking.hh.nord.gazetteer.opendata.NamedTreeNode;
import com.jejking.hh.nord.gazetteer.osm.OsmStreetCollectionToNeoImporter;
import com.jejking.hh.nord.gazetteer.osm.PointOfInterest;
import com.jejking.hh.nord.gazetteer.osm.PointOfInterestToNeoImporter;
import com.jejking.hh.nord.gazetteer.osm.RxBuildingAndPOICollectionBuilder;
import com.jejking.hh.nord.gazetteer.osm.RxOsmStreetCollectionBuilder;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;


/**
 * Helper class to set up database and load data.
 * 
 * @author jejking
 *
 */
public class TestUtil {

    /*
     * Creates fresh test database with some
     * standard schema and full-text indexes set up already. 
     */
    public static GraphDatabaseService createTestDatabase() {
        GraphDatabaseService graph =  new TestGraphDatabaseFactory()
                                                        .newImpermanentDatabaseBuilder()
                                                        .newGraphDatabase();
        
        setupSchema(graph);
        return graph;
        
    }
    
    public static void writeHamburgPolygonsToGraph(GraphDatabaseService graph) {
        CoordinateConverter converter = new CoordinateConverter();
        
        HamburgRawTreeBuilder builder = new HamburgRawTreeBuilder();
        NamedTreeNode<String> hh = builder.buildRawTree();
        NamedTreeNode<Polygon> polygonHamburg = converter.fixRoot(converter.rawToPolygon(hh));
        
        HamburgPolygonTreeToNeoImporter importer = new HamburgPolygonTreeToNeoImporter();
        importer.writeToNeo(polygonHamburg, graph);
    }
    
    public static void writeHamburgNordOsmStreetsToGraph(GraphDatabaseService graph) {
        RxOsmStreetCollectionBuilder builder = new RxOsmStreetCollectionBuilder(JTSFactoryFinder.getGeometryFactory(null));
        try {
        	Map<String, Geometry> streets = builder
        								.streetsFromStream(
        										new BZip2CompressorInputStream(
        										 	TestUtil.class.getResourceAsStream("/hamburg-nord-tm470.osm.bz2")));
        	OsmStreetCollectionToNeoImporter importer = new OsmStreetCollectionToNeoImporter();
            importer.writeToNeo(streets, graph);
        } catch (IOException e) {
        	throw new RuntimeException(e);
        }
        
    }
    
    public static void writeUhlenhorstOsmStreetsToGraph(GraphDatabaseService graph) {
        RxOsmStreetCollectionBuilder builder = new RxOsmStreetCollectionBuilder(JTSFactoryFinder.getGeometryFactory(null));

        Map<String, Geometry> streets = builder
                                    .streetsFromStream(
                                            TestUtil.class.getResourceAsStream("/uhlenhorst-direct-export.osm"));
        OsmStreetCollectionToNeoImporter importer = new OsmStreetCollectionToNeoImporter();
        importer.writeToNeo(streets, graph);
    }
    
    
    public static void writeUhlenhorstPoisToGraph(GraphDatabaseService graph) {
        RxBuildingAndPOICollectionBuilder builder = new RxBuildingAndPOICollectionBuilder(JTSFactoryFinder.getGeometryFactory());
        List<PointOfInterest> pois = builder
                                        .pointsOfInterestFromStream( TestUtil.class.getResourceAsStream("/uhlenhorst-direct-export.osm"));
        
        PointOfInterestToNeoImporter importer = new PointOfInterestToNeoImporter();
        importer.writeToNeo(pois, graph);
        
    }
    
}
