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

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;

import info.jejking.hamburg.nord.drucksachen.allris.RawDrucksache;
import info.jejking.hamburg.nord.drucksachen.importer.DrucksachenImporter;
import info.jejking.hamburg.nord.drucksachen.matcher.DrucksachenGazetteerKeywordMatcher;
import info.jejking.hamburg.nord.drucksachen.matcher.DrucksachenGazetteerKeywordMatcherFactory;

/**
 * Given a gazetteer Neo4j Database, as created by {@link CreateGazetteer},
 * and a directory of files representing serialised {@link RawDrucksache} instances,
 * this class co-ordinates the creation of keyword matchers followed by the matching
 * and import pipeline.
 * 
 * @see DrucksachenGazetteerKeywordMatcherFactory
 * @see DrucksachenImporter
 * @author jejking
 *
 */
public class CreateSpatialDrucksachenRepository {

    /**
     * Runs the main importer logic. Supply two parameters: directory where
     * the Neo4j database is located, directory where the serialised {@link RawDrucksache}
     * instances are to be found, each in its own file.
     * 
     * @param args
     */
    public static void main(String[] args) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        GraphDatabaseService graph = new GraphDatabaseFactory().newEmbeddedDatabase(args[0]);
        registerShutdownHook(graph);
        System.out.println("Started graph database after " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
        
        DrucksachenGazetteerKeywordMatcherFactory matcherFactory = new DrucksachenGazetteerKeywordMatcherFactory();
        ImmutableMap<String, DrucksachenGazetteerKeywordMatcher> matchersMap = matcherFactory.createKeywordMatchersFromGazetteer(graph);
        
        DrucksachenImporter importer = new DrucksachenImporter(matchersMap);
        System.out.println("Initialised matchers after " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
        
        importer.createDrucksachenIndexes(graph);
        System.out.println("Created indexes for Drucksachen after " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
        
        File drucksachenDirectory = new File(args[1]); 
        importer.writeToNeo(Arrays.asList(drucksachenDirectory.listFiles()), graph);
        System.out.println("Completed import after " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
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
