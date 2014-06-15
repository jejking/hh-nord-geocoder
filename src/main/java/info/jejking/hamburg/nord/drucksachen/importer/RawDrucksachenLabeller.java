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

import static com.google.common.base.Preconditions.checkNotNull;
import info.jejking.hamburg.nord.drucksachen.allris.RawDrucksache;
import info.jejking.hamburg.nord.drucksachen.matcher.DrucksachenGazetteerKeywordMatcher;
import info.jejking.hamburg.nord.drucksachen.matcher.Matches;
import info.jejking.hamburg.nord.drucksachen.matcher.RawDrucksacheWithLabelledMatches;

import java.util.Map;

import rx.functions.Func1;

import com.google.common.collect.ImmutableMap;

/**
 * Function to apply a series of {@link DrucksachenGazetteerKeywordMatcher} functions associated
 * with a map of labels to a {@link RawDrucksache} to produce a {@link RawDrucksacheWithLabelledMatches}.
 * @author jejking
 *
 */
public final class RawDrucksachenLabeller implements Func1<RawDrucksache, RawDrucksacheWithLabelledMatches> {
    
    private final ImmutableMap<String, DrucksachenGazetteerKeywordMatcher> matchersMap;
    
    public RawDrucksachenLabeller(ImmutableMap<String, DrucksachenGazetteerKeywordMatcher> matchersMap) {
        this.matchersMap = checkNotNull(matchersMap);
    }
    
    @Override
    public RawDrucksacheWithLabelledMatches call(RawDrucksache rawDrucksache) {
        
        ImmutableMap.Builder<String, Matches> matchesMapBuilder = ImmutableMap.builder();
        
        for (Map.Entry<String, DrucksachenGazetteerKeywordMatcher> entry : matchersMap.entrySet()) {
            matchesMapBuilder.put(entry.getKey(), entry.getValue().call(rawDrucksache));
        }
        
        return new RawDrucksacheWithLabelledMatches(rawDrucksache, matchesMapBuilder.build());
    }
}