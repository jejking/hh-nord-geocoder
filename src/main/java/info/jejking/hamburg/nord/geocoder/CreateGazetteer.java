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
package info.jejking.hamburg.nord.geocoder;

import static info.jejking.hamburg.nord.geocoder.GazetteerEntryTypes.ADMIN_AREA;
import static info.jejking.hamburg.nord.geocoder.GazetteerEntryTypes.STREET;
import static info.jejking.hamburg.nord.geocoder.GazetteerNames.GAZETTEER_FULLTEXT;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import info.jejking.hamburg.nord.geocoder.hh.CoordinateConverter;
import info.jejking.hamburg.nord.geocoder.hh.HamburgPolygonTreeToNeoImporter;
import info.jejking.hamburg.nord.geocoder.hh.HamburgRawTreeBuilder;
import info.jejking.hamburg.nord.geocoder.hh.NamedTreeNode;
import info.jejking.hamburg.nord.geocoder.osm.OsmStreetCollectionToNeoImporter;
import info.jejking.hamburg.nord.geocoder.osm.PointOfInterest;
import info.jejking.hamburg.nord.geocoder.osm.PointOfInterestToNeoImporter;
import info.jejking.hamburg.nord.geocoder.osm.RxBuildingAndPOICollectionBuilder;
import info.jejking.hamburg.nord.geocoder.osm.RxOsmStreetCollectionBuilder;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.helpers.collection.MapUtil;

import com.google.common.base.Stopwatch;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Creates a neo4j database and populates it with hierarchical geographical data from the City of Hamburg and Open
 * Street Map for use in geocoding.
 * 
 * @author jejking
 * 
 */
public class CreateGazetteer {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        GraphDatabaseService graph = new GraphDatabaseFactory().newEmbeddedDatabase(args[0]);
        registerShutdownHook(graph);
        
        // we want an additional index on adminstrative area - name
        setUpIndexes(graph);
        System.out.println("Setup indexes. Elapsed time: " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
        
        writeHamburgPolygons(graph);
        System.out.println("Wrote hamburg polygons. Elapsed time: " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
        
        writeStreets(geometryFactory, graph);
        System.out.println("Wrote streets. Elapsed time: " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
        
        mapStreetsToAdminPolygons(graph);
        System.out.println("Linked streets to polygons. Elapsed time: " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
        
        writeBuildingsAndPointsOfInterest(geometryFactory, graph);
        System.out.println("Wrote buildings and points of interest. Elapsed time: " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
        
        graph.shutdown();
        System.out.println("Done. Elapsed time: " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
    }

    private static void writeBuildingsAndPointsOfInterest(GeometryFactory geometryFactory, GraphDatabaseService graph) {
        RxBuildingAndPOICollectionBuilder builder = new RxBuildingAndPOICollectionBuilder(geometryFactory);
        try {
            List<PointOfInterest> pois = builder
                    .pointsOfInterestFromStream(new BZip2CompressorInputStream(
                            CreateGazetteer.class.getResourceAsStream("/hamburg-nord-tm470.osm.bz2")));

            PointOfInterestToNeoImporter importer = new PointOfInterestToNeoImporter();
            importer.writeToNeo(pois, graph);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void mapStreetsToAdminPolygons(GraphDatabaseService graph) {
        StreetToAdminPolygonMapper streetToAdminPolygonMapper = new StreetToAdminPolygonMapper();
        streetToAdminPolygonMapper.mapStreetsToPolygons(graph);
    }

    private static void writeStreets(GeometryFactory geometryFactory, GraphDatabaseService graph) {
        RxOsmStreetCollectionBuilder builder = new RxOsmStreetCollectionBuilder(geometryFactory);
        try {
            Map<String, Geometry> streets = builder
                                        .streetsFromStream(
                                                new BZip2CompressorInputStream(
                                                    CreateGazetteer.class.getResourceAsStream("/hamburg-nord-tm470.osm.bz2")));
            OsmStreetCollectionToNeoImporter importer = new OsmStreetCollectionToNeoImporter();
            importer.writeToNeo(streets, graph);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeHamburgPolygons(GraphDatabaseService graph) {
        HamburgRawTreeBuilder hamburgRawTreeBuilder = new HamburgRawTreeBuilder();
        NamedTreeNode<String> hamburgNodes = hamburgRawTreeBuilder.buildRawTree();
        CoordinateConverter converter = new CoordinateConverter();
        NamedTreeNode<Polygon> hamburgPolygons = converter.fixRoot(converter.rawToPolygon(hamburgNodes));
        HamburgPolygonTreeToNeoImporter hamburgPolygonTreeToNeoImporter = new HamburgPolygonTreeToNeoImporter();
        hamburgPolygonTreeToNeoImporter.writeToNeo(hamburgPolygons, graph);
    }

    private static void setUpIndexes(GraphDatabaseService graph) {
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
            
            schema
                .indexFor(DynamicLabel.label(GazetteerEntryTypes.POINT_OF_INTEREST))
                .on("NAME")
                .create();
            
            IndexManager indexManager = graph.index();
            @SuppressWarnings("unused")
            Index<Node> fullText = indexManager.forNodes(GAZETTEER_FULLTEXT,
                                MapUtil.stringMap(IndexManager.PROVIDER, "lucene",
                                                  "type", "fulltext"));
            
            tx.success();
        }
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }

}