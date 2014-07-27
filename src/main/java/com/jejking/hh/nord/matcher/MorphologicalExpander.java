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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.jejking.hh.nord.gazetteer.GazetteerEntryTypes;

import rx.Observable;
import rx.functions.Func1;

import com.google.common.base.Function;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.readLines;

/**
 * Class that adds some naive declensions to input strings so that the 
 * Aho-Corasick matcher can find them. This is particularly important for
 * genitive forms of street names.
 * 
 * <p>This class is nowhere near complete - and is to be considered a bit of 
 * a crutch to support the basic Aho-Corasick matching which represents the first
 * iteration of matching.</p>
 * 
 * @author jejking
 *
 */
public final class MorphologicalExpander implements Func1<String, Observable<String>> {

    private static final ImmutableList<String> streetSuffixes;
    
    private final String label;
    
    static {
        try {
            List<String> read = readLines(getResource("streetSuffixes.txt"), Charsets.UTF_8);
            streetSuffixes = ImmutableList.copyOf(Iterables.transform(read, new Function<String, String>() {
                
                @Override
                public String apply(String input) {
                    return input.trim().toLowerCase();
                }
                
            }));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    public MorphologicalExpander(String label) {
        this.label = label;
    }


    @Override
    public Observable<String> call(String nameToExpand) {
        if (!this.label.equals(GazetteerEntryTypes.STREET)) {
            return Observable.from(nameToExpand);
        } else {
            return handleStreetName(nameToExpand);
        }
        
        
    }


    private Observable<String> handleStreetName(String nameToExpand) {
        for (String streetSuffixToExpand : streetSuffixes) {
            if (nameToExpand.toLowerCase().endsWith(streetSuffixToExpand)) {
                return morphologicallyExpand(nameToExpand);
            }
        }
        
        return Observable.from(nameToExpand); // nothing to expand....
    }


    private Observable<String> morphologicallyExpand(String nameToExpand) {
        List<String> observableList = new LinkedList<>();
        observableList.add(nameToExpand); // must carry this over
        
        // right now, we'll just limit to expanding genitive on last part of name
        observableList.add(nameToExpand + "s");
        observableList.add(nameToExpand + "es");
        
        return Observable.from(observableList);
    }

}