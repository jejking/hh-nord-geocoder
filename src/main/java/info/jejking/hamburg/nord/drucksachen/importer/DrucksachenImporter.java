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

import info.jejking.hamburg.nord.drucksachen.allris.RawDrucksache;
import info.jejking.hamburg.nord.drucksachen.matcher.DrucksachenGazetteerKeywordMatcher;
import info.jejking.hamburg.nord.drucksachen.matcher.GazetteerKeywordMatcher;
import info.jejking.hamburg.nord.geocoder.AbstractNeoImporter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.neo4j.graphdb.GraphDatabaseService;

import com.google.common.collect.ImmutableMap;

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
public class DrucksachenImporter extends AbstractNeoImporter<Iterable<File>> {

    private final RawDrucksachenLabeller rawDrucksachenLabeller;
    
    public DrucksachenImporter(ImmutableMap<String, DrucksachenGazetteerKeywordMatcher> matchersMap) {
        this.rawDrucksachenLabeller = new RawDrucksachenLabeller(matchersMap);
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
