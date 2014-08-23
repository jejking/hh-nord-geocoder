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
package com.jejking.hh.nord.gazetteer.opendata;

import static com.jejking.hh.nord.AbstractNeoImporter.registerShutdownHook;
import static com.jejking.hh.nord.AbstractNeoImporter.setupSchema;

import java.util.concurrent.TimeUnit;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.google.common.base.Stopwatch;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Class with main method just to run the importer for the Hamburg City
 * Polygons, creating a Neo4j database.
 * 
 * @author jejking
 * 
 */
public class CreatePartialGazetteerWithOpenData {

	/**
	 * Main method.
	 * 
	 * @param args
	 *            , first arg is path to directory in which to create/open a
	 *            Neo4j database with the gazetteer.
	 */
	public static void main(String[] args) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		GraphDatabaseService graph = new GraphDatabaseFactory().newEmbeddedDatabase(args[0]);
		registerShutdownHook(graph);

		// we want an additional index on adminstrative area - name
		setupSchema(graph);
		System.out.println("Setup indexes. Elapsed time: " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");

		CreatePartialGazetteerWithOpenData.writeHamburgPolygons(graph);
		System.out.println("Wrote hamburg polygons. Elapsed time: " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
	}

    public static void writeHamburgPolygons(GraphDatabaseService graph) {
        HamburgRawTreeBuilder hamburgRawTreeBuilder = new HamburgRawTreeBuilder();
        AdminAreaTreeNode<String> hamburgNodes = hamburgRawTreeBuilder.buildRawTree();
    
        AdminAreaTreeNodeTransformer t = new AdminAreaTreeNodeTransformer();
        AdminAreaTreeNode<Polygon> hamburgPolygons = t.call(hamburgNodes);
        
        HamburgPolygonTreeToNeoImporter hamburgPolygonTreeToNeoImporter = new HamburgPolygonTreeToNeoImporter();
        hamburgPolygonTreeToNeoImporter.writeToNeo(hamburgPolygons, graph);
    }

}
