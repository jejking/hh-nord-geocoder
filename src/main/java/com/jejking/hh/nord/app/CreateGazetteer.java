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
import static com.jejking.hh.nord.AbstractNeoImporter.registerShutdownHook;

import java.util.concurrent.TimeUnit;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.google.common.base.Stopwatch;
import com.jejking.hh.nord.gazetteer.opendata.CreatePartialGazetteerWithOpenData;
import com.jejking.hh.nord.gazetteer.osm.poi.WritePointsOfInterest;
import com.jejking.hh.nord.gazetteer.osm.streets.MapStreetsToPolygons;
import com.jejking.hh.nord.gazetteer.osm.streets.WriteStreets;
import com.vividsolutions.jts.geom.GeometryFactory;

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
        
        CreatePartialGazetteerWithOpenData.writeHamburgPolygons(graph);
        System.out.println("Wrote hamburg polygons. Elapsed time: " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
        
        WriteStreets.writeStreets(geometryFactory, graph);
        System.out.println("Wrote streets. Elapsed time: " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
        
        MapStreetsToPolygons.mapStreetsToAdminPolygons(graph);
        System.out.println("Linked streets to polygons. Elapsed time: " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
        
        WritePointsOfInterest.writePointsOfInterest(geometryFactory, graph);
        System.out.println("Wrote points of interest. Elapsed time: " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
        
        graph.shutdown();
        System.out.println("Done. Elapsed time: " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
    }

    

   

}
