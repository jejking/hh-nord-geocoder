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
package info.jejking.osm;

import com.google.common.collect.ImmutableMap;

/**
 * Simple interface to give type safe, immutable view 
 * on to key-value string pairs.
 * @author jejking
 *
 */
public interface OsmProperties {

    /**
     *
     * @return key value pairs, may be empty, never <code>null</code>
     */
    public ImmutableMap<String, String> getProperties();
    
}
