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

import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Data class encapsulating keyword matches found.
 *  
 * @author jejking
 *
 */
public final class Matches {

    private final ImmutableMap<String, Integer> matchesInBody;
    private final ImmutableMap<String, Integer> matchesInHeader;
    
    /**
     * Constructor.
     * @param matchesInBody map of string matches and occurrence counts in body, may be empty, never <code>null</code>
     * @param matchesInHeader map of string matches and occurrence counts in header, may be empty, never <code>null</code>
     */
    public Matches(ImmutableMap<String, Integer> matchesInBody, ImmutableMap<String, Integer> matchesInHeader) {
        super();
        this.matchesInBody = checkNotNull(matchesInBody);
        this.matchesInHeader = checkNotNull(matchesInHeader);
    }

    
    public ImmutableMap<String, Integer> getMatchesInBody() {
        return matchesInBody;
    }
    
    
    public ImmutableMap<String, Integer> getMatchesInHeader() {
        return matchesInHeader;
    }
    
}