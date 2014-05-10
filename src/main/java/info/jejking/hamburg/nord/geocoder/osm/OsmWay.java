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
package info.jejking.hamburg.nord.geocoder.osm;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Value class representing an Open Street Map <code>way</code>, but
 * with <code>nd ref</code> attributes replaced with object references.
 * 
 * @author jejking
 *
 */
public final class OsmWay extends OsmComponent {

    private final ImmutableList<Long> ndRefs;

    /**
     * Constructor. No property may be null - if not present, use {@link Optional#absent()}.
     * 
     * @param metadata
     * @param properties
     * @param ndRefs values of <tt>ref</tt> attributes of <tt>nd</tt> elements
     * @throws NullPointerException if any parameter is <code>null</code>
     */
    public OsmWay(OsmMetadata metadata, ImmutableMap<String, String> properties, 
            ImmutableList<Long> ndRefs) {
        super(metadata, properties);
        this.ndRefs = checkNotNull(ndRefs);
    }

    
    /**
     * @return the ndRefs
     */
    public ImmutableList<Long> getNdRefs() {
        return ndRefs;
    }


    
    
}

