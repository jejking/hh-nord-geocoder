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
package info.jejking.hamburg.nord.drucksachen.matcher;

import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Data class encapsulating keyword matches found.
 *  
 * @author jejking
 *
 */
public final class Matches {

    private final ImmutableSet<String> matchesInBody;
    private final ImmutableSet<String> matchesInHeader;
    
    /**
     * Constructor.
     * @param matchesInBody set of string matches in body, may be empty, never <code>null</code>
     * @param matchesInHeader set of string matches in header, may be empty, never <code>null</code>
     */
    public Matches(ImmutableSet<String> matchesInBody, ImmutableSet<String> matchesInHeader) {
        super();
        this.matchesInBody = checkNotNull(matchesInBody);
        this.matchesInHeader = checkNotNull(matchesInHeader);
    }

    
    public ImmutableSet<String> getMatchesInBody() {
        return matchesInBody;
    }
    
    
    public ImmutableSet<String> getMatchesInHeader() {
        return matchesInHeader;
    }
    
}