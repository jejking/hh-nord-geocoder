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

import static com.jejking.hh.nord.AbstractNeoImporter.registerShutdownHook;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.google.common.base.Stopwatch;
import com.vividsolutions.jts.geom.GeometryFactory;


/**
 * Method to import points of interest from Open Street Map into the gazetteer. Assumes
 * gazetteer and schema have been initialised and that streets have also been imported.
 * 
 * @author jejking
 *
 */
public class WritePointsOfInterest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        GraphDatabaseService graph = new GraphDatabaseFactory().newEmbeddedDatabase(args[0]);
        registerShutdownHook(graph);
        
        WritePointsOfInterest.writePointsOfInterest(geometryFactory, graph);
        System.out.println("Wrote points of interest. Elapsed time: " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
    }

    public static void writePointsOfInterest(GeometryFactory geometryFactory, GraphDatabaseService graph) {
        RxPointOfInterestCollectionBuilder builder = new RxPointOfInterestCollectionBuilder(geometryFactory);
        try {
            List<PointOfInterest> pois = builder
                    .pointsOfInterestFromStream(new BZip2CompressorInputStream(
                            WritePointsOfInterest.class.getResourceAsStream("/hamburg-nord-tm470.osm.bz2")));
    
            PointOfInterestToNeoImporter importer = new PointOfInterestToNeoImporter();
            importer.writeToNeo(pois, graph);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
