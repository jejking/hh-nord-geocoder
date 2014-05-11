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

/**
 * Simple value class representing a string key-value pair. Correponds to "tag" element.
 * @author jejking
 *
 */
public final class OsmTag {

    private final String key;
    private final String value;
    
    /**
     * Key value pair.
     * @param key 
     * @param value
     */
    public OsmTag(String key, String value) {
        super();
        this.key = key;
        this.value = value;
    }
    
    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }
    
    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }
    
    
    
}
