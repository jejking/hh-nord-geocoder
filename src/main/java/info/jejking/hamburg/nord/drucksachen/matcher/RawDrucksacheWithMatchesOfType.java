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

import info.jejking.hamburg.nord.drucksachen.allris.RawDrucksache;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Data class that represents the result of associating a {@link RawDrucksache}
 * with matches against a set of keywords representing gazetteer entries of 
 * a given type.
 * 
 * @author jejking
 *
 */
public class RawDrucksacheWithMatchesOfType {

    private final RawDrucksache original;
    private final ImmutableSet<String> matchesInBody;
    private final ImmutableSet<String> matchesInHeader;
    private final String entryType;
    
    /**
     * Constructor.
     * @param original the original
     * @param matchesInBody set of matches from the body of the document
     * @param matchesInHeader set of matches from the "Betreff" property, if any
     * @param entryType the type of gazetteer entry being referred to
     * @throws NullPointerException if any parameter is <code>null</code>
     */
    public RawDrucksacheWithMatchesOfType(RawDrucksache original, ImmutableSet<String> matchesInBody, ImmutableSet<String> matchesInHeader, String entryType) {
        super();
        this.original = checkNotNull(original);
        this.matchesInBody = checkNotNull(matchesInBody);
        this.matchesInHeader = checkNotNull(matchesInHeader);
        this.entryType = checkNotNull(entryType);
    }

    
    /**
     * @return the original
     */
    public RawDrucksache getOriginal() {
        return original;
    }

    /**
     * @return the matchesInBody
     */
    public ImmutableSet<String> getMatchesInBody() {
        return matchesInBody;
    }


    
    /**
     * @return the matchesInHeader
     */
    public ImmutableSet<String> getMatchesInHeader() {
        return matchesInHeader;
    }


    /**
     * @return the entryType
     */
    public String getEntryType() {
        return entryType;
    }
    
    

}
