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
package com.jejking.hh.nord.matcher;

import static com.jejking.hh.nord.corpus.DrucksacheNames.DRUCKSACHE;
import static com.jejking.hh.nord.corpus.DrucksacheNames.DRUCKSACHE_ID;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;

import com.google.common.collect.ImmutableMap;
import com.jejking.hh.nord.AbstractNeoImporter;
import com.jejking.hh.nord.corpus.DrucksacheDateEnhancer;
import com.jejking.hh.nord.corpus.RawDrucksache;
import com.jejking.hh.nord.corpus.RawDrucksachenLabeller;

import rx.Observable;
import rx.functions.Func1;


/**
 * Class to import a directory full of serialised {@link RawDrucksache} instances into 
 * Neo4j, creating references as we go to the gazetteer entries which could be matched
 * to the text using the {@link GazetteerKeywordMatcher} instances populated from the 
 * Gazetteer.
 * 
 * @author jejking
 *
 */
public class ImportAndMatch extends AbstractNeoImporter<Iterable<File>> {

    private final RawDrucksachenLabeller rawDrucksachenLabeller;
    
    /**
     * Constructor. Needs to be supplied with a suitably initialised map of {@link DrucksachenGazetteerKeywordMatcher}
     * instances. The keys must correspond to Node {@link Label} instances in the Neo4j gazetteer
     * in order to be effective.
     *  
     * @param matchersMap may not be <code>null</code>
     * @throws NullPointerException if param is <code>null</code>
     */
    public ImportAndMatch(ImmutableMap<String, DrucksachenGazetteerKeywordMatcher> matchersMap) {
        this.rawDrucksachenLabeller = new RawDrucksachenLabeller(matchersMap);
    }
    
    /**
     * Creates indices.
     * 
     * @param graph
     */
    public void createDrucksachenIndexes(GraphDatabaseService graph) {
        try (Transaction tx = graph.beginTx()) {
            Schema schema = graph.schema();
            schema
                .indexFor(DynamicLabel.label(DRUCKSACHE))
                .on(DRUCKSACHE_ID)
                .create();
            tx.success();
        }
    }
    
    
    @Override
    public void writeToNeo(Iterable<File> files, final GraphDatabaseService graph) {
        Observable.from(files)
        .map(new Func1<File, RawDrucksache>() {

            @Override
            public RawDrucksache call(File t1) {
                try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(t1)))) {
                    return (RawDrucksache) ois.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            
        })
        .map(new DrucksacheDateEnhancer())
        .map(rawDrucksachenLabeller)
        .subscribe(new RawDrucksacheWithLabelledMatchesNeoImporter(graph));
        
        
    }

}
