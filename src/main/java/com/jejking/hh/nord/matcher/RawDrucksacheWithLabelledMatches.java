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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import com.jejking.hh.nord.drucksachen.RawDrucksache;


/**
 * Data class that represents the result of associating a {@link RawDrucksache}
 * with matches against a set of keywords representing gazetteer entries of 
 * a given type.
 * 
 * @author jejking
 *
 */
public class RawDrucksacheWithLabelledMatches {

    private final RawDrucksache original;
    private final ImmutableMap<String, Matches> matchesMap;
    
    /**
     * Constructor.
     * @param original the original
     * @param matchesMap map of matches keyed on the type
     * @throws NullPointerException if any parameter is <code>null</code>
     */
    public RawDrucksacheWithLabelledMatches(RawDrucksache original, ImmutableMap<String, Matches> matchesMap) {
        super();
        this.original = checkNotNull(original);
        this.matchesMap = checkNotNull(matchesMap);
    }
    
  
    /**
     * @return the original
     */
    public RawDrucksache getOriginal() {
        return original;
    }

    public ImmutableMap<String, Matches> getMatchesMap() {
        return matchesMap;
    }
    
    

}
