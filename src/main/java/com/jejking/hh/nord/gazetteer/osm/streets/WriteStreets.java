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

import static com.jejking.hh.nord.AbstractNeoImporter.registerShutdownHook;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.google.common.base.Stopwatch;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Method to import streets from Open Street Map into the gazetteer. Assumes
 * gazetteer and schema have been initialised.
 * 
 * @author jejking
 *
 */
public class WriteStreets {

	/**
	 * Imports an extract from Open Street Map into Neo4j.
	 * @param args directory in which the Neo4j database exists.
	 */
	public static void main(String[] args) {
		Stopwatch stopwatch = Stopwatch.createStarted();
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        GraphDatabaseService graph = new GraphDatabaseFactory().newEmbeddedDatabase(args[0]);
		registerShutdownHook(graph);
		
		WriteStreets.writeStreets(geometryFactory, graph);
		System.out.println("Wrote streets. Elapsed time: " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");

	}

    public static void writeStreets(GeometryFactory geometryFactory, GraphDatabaseService graph) {
        RxOsmStreetCollectionBuilder builder = new RxOsmStreetCollectionBuilder(geometryFactory);
        try {
            Map<String, Geometry> streets = builder
                                        .streetsFromStream(
                                                new BZip2CompressorInputStream(
                                                    WriteStreets.class.getResourceAsStream("/hamburg-nord-tm470.osm.bz2")));
            OsmStreetCollectionToNeoImporter importer = new OsmStreetCollectionToNeoImporter();
            importer.writeToNeo(streets, graph);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
