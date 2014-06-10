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
package info.jejking.hamburg.nord.drucksachen.matcher;

import com.google.common.collect.ImmutableSet;

import info.jejking.hamburg.nord.drucksachen.allris.DrucksachenPropertyKeys;
import info.jejking.hamburg.nord.drucksachen.allris.RawDrucksache;
import rx.functions.Func1;

/**
 * Function that matches a {@link RawDrucksache} to a pair containing the input item (for
 * further processing such as metadata) with the results of matching it against the 
 * keyword list supplied.
 * 
 * @author jejking
 *
 */
public class DrucksachenGazetteerKeywordMatcher implements Func1<RawDrucksache, Matches> {

    private final GazetteerKeywordMatcher gazetteerKeywordMatcher;
    
    public DrucksachenGazetteerKeywordMatcher(Iterable<String> keyWords, String entryType) {
        this.gazetteerKeywordMatcher = new GazetteerKeywordMatcher(keyWords, entryType);
    }
    

    @Override
    public Matches call(RawDrucksache rawDrucksache) {
       
        ImmutableSet.Builder<String> matchesInBodyBuilder = ImmutableSet.builder();
        for (String text : rawDrucksache.getExtractedContent()) {
            matchesInBodyBuilder.addAll(this.gazetteerKeywordMatcher.call(text));
        }
        
        ImmutableSet<String> matchesInHeader = matchHeader(rawDrucksache);
        
        return new Matches(matchesInBodyBuilder.build(), matchesInHeader);
    }


    private ImmutableSet<String> matchHeader(RawDrucksache rawDrucksache) {
        if (rawDrucksache.getExtractedProperties().containsKey(DrucksachenPropertyKeys.BETREFF)) {
            return this.gazetteerKeywordMatcher.call(rawDrucksache.getExtractedProperties().get(DrucksachenPropertyKeys.BETREFF));
        } else {
            return ImmutableSet.of();
        }
    }

}
