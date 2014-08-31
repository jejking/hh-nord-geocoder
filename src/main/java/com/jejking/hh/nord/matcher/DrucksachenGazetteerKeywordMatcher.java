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
package com.jejking.hh.nord.matcher;

import rx.functions.Func1;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.jejking.hh.nord.corpus.DrucksachenPropertyKeys;
import com.jejking.hh.nord.corpus.RawDrucksache;

/**
 * Function that matches a {@link RawDrucksache} to a pair containing the input item (for
 * further processing such as metadata) with the results of matching it against the 
 * keyword list supplied.
 * 
 * @author jejking
 *
 */
public class DrucksachenGazetteerKeywordMatcher implements Func1<RawDrucksache, Matches> {

    public static final ImmutableSet<String> authorityNamesToFilter = ImmutableSet.of("Freie und Hansestadt Hamburg",
                                                                                      "Bezirksversammlung Hamburg-Nord",
                                                                                      "Bezirksamt Hamburg-Nord",
                                                                                      "Regionalausschuss Barmbek-Uhlenhorst-Hohenfelde-Dulsberg",
                                                                                      "Regionalausschuss Eppendorf-Winterhude",
                                                                                      "Regionalausschuss Langenhorn-Fuhlsbüttel-Alsterdorf-Groß Borstel"); 
    
    private final GazetteerKeywordMatcher gazetteerKeywordMatcher;
    
    /**
     * Constructor, essentially assembles a {@link GazetteerKeywordMatcher}.
     * @param keyWords list of named entities of a given type from the gazetteer
     * @param entryType label describing type of entities named in the keyword iterable
     */
    public DrucksachenGazetteerKeywordMatcher(Iterable<String> keyWords, String entryType) {
        this.gazetteerKeywordMatcher = new GazetteerKeywordMatcher(keyWords, entryType);
    }
    

    @Override
    public Matches call(RawDrucksache rawDrucksache) {
       
        ImmutableMap<String, Integer> matchesInBody = this.gazetteerKeywordMatcher.call(filterAuthorityNames(concatenateContent(rawDrucksache)));
        ImmutableMap<String, Integer> matchesInHeader = this.gazetteerKeywordMatcher.call(extractTitle(rawDrucksache));
        return new Matches(matchesInBody, matchesInHeader);
    }
    
    /**
     * Gets wrapped matcher.
     * @return matcher
     */
    public GazetteerKeywordMatcher getGazetteerKeywordMatcher() {
        return gazetteerKeywordMatcher;
    }


    static String filterAuthorityNames(String concatenatedContent) {
        String filtered = concatenatedContent;
        for (String toFilter : authorityNamesToFilter) {
            filtered = filtered.replace(toFilter, "");
        }
        return filtered;
    }


    String extractTitle(RawDrucksache rawDrucksache) {
        if (rawDrucksache.getExtractedProperties().containsKey(DrucksachenPropertyKeys.BETREFF)) {
            return rawDrucksache.getExtractedProperties().get(DrucksachenPropertyKeys.BETREFF);
        } else {
            return "";
        }
    }


    String concatenateContent(RawDrucksache rawDrucksache) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String content : rawDrucksache.getExtractedContent()) {
            stringBuilder.append(content);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }



}
