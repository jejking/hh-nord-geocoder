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
package com.jejking.hh.nord.app;

import static com.jejking.hh.nord.AbstractNeoImporter.setupSchema;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.google.common.base.Stopwatch;
import com.jejking.hh.nord.gazetteer.opendata.CoordinateConverter;
import com.jejking.hh.nord.gazetteer.opendata.HamburgPolygonTreeToNeoImporter;
import com.jejking.hh.nord.gazetteer.opendata.HamburgRawTreeBuilder;
import com.jejking.hh.nord.gazetteer.opendata.NamedTreeNode;
import com.jejking.hh.nord.gazetteer.osm.OsmStreetCollectionToNeoImporter;
import com.jejking.hh.nord.gazetteer.osm.PointOfInterest;
import com.jejking.hh.nord.gazetteer.osm.PointOfInterestToNeoImporter;
import com.jejking.hh.nord.gazetteer.osm.RxBuildingAndPOICollectionBuilder;
import com.jejking.hh.nord.gazetteer.osm.RxOsmStreetCollectionBuilder;
import com.jejking.hh.nord.gazetteer.osm.StreetToAdminPolygonMapper;
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
        setupSchema(graph);
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

    

    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }

}
