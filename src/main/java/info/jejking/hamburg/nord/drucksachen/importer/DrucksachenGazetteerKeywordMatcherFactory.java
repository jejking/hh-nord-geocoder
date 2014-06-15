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
package info.jejking.hamburg.nord.drucksachen.importer;

import info.jejking.hamburg.nord.drucksachen.matcher.DrucksachenGazetteerKeywordMatcher;
import info.jejking.hamburg.nord.geocoder.GazetteerEntryTypes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Class to create appropriately labelled {@link DrucksachenGazetteerKeywordMatcher}
 * populated from the Neo4j gazetteer.
 * 
 * @author jejking
 */
public class DrucksachenGazetteerKeywordMatcherFactory {

    /**
     * Creates matchers for {@link GazetteerEntryTypes#NAMED_AREA}, {@link GazetteerEntryTypes#STREET} and
     * {@link GazetteerEntryTypes#POINT_OF_INTEREST}.
     * 
     * @param graph
     * @return
     */
    public ImmutableMap<String, DrucksachenGazetteerKeywordMatcher> createKeywordMatchersFromGazetteer(GraphDatabaseService graph) {
        
        ImmutableMap.Builder<String, DrucksachenGazetteerKeywordMatcher> mapBuilder = ImmutableMap.builder(); 
        
        ImmutableList<String> labels = ImmutableList.of(
                                        GazetteerEntryTypes.NAMED_AREA,
                                        GazetteerEntryTypes.STREET,
                                        GazetteerEntryTypes.POINT_OF_INTEREST);
        
        try(Transaction tx = graph.beginTx()) {
            ExecutionEngine executionEngine = new ExecutionEngine(graph);
            
            
            for (String label : labels) {
                String query = "match (n:" + label + ") where n.NAME IS NOT NULL return n.NAME";
                Iterator<String> nameIterator = executionEngine.execute(query).columnAs("n.NAME");
                DrucksachenGazetteerKeywordMatcher matcher = new DrucksachenGazetteerKeywordMatcher(
                                                                ImmutableList.copyOf(nameIterator), label);
                mapBuilder.put(label, matcher);
            }
        }
        
        
        
        return mapBuilder.build();
    }


}
