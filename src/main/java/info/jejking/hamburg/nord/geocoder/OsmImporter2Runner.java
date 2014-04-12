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
 *    
 */
package info.jejking.hamburg.nord.geocoder;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.gis.spatial.osm.OSMImporter2;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

/**
 * Runs our copy of the OSMImporter with some hardcoded paths.
 */
public class OsmImporter2Runner {

    public static void main(String[] args) throws Exception {
        OSMImporter2 importer = new OSMImporter2("hamburg-nord.osm");
        importer.setCharset(Charset.forName("UTF-8"));

        Map<String, String> config = new HashMap<String, String>();
        config.put("neostore.nodestore.db.mapped_memory", "256M");
        config.put("dump_configuration", "true");
        config.put("use_memory_mapped_buffers", "true");

        BatchInserter batchInserter = BatchInserters.inserter("/home/jejking/tmp/neo4j-osm", config);

        importer.importFile(batchInserter,
                "/home/jejking/Open University/TM470/open street map data hamburg/data/hamburg-nord.osm", false);
        batchInserter.shutdown();

        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase("/home/jejking/tmp/neo4j-osm");
        importer.reIndex(db);
        db.shutdown();
    }
}
