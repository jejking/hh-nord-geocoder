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
package info.jejking.hamburg.nord.geocoder;

import info.jejking.hamburg.nord.drucksachen.allris.RawDrucksache;

/**
 * Names for Neo4j properties for {@link RawDrucksache} representations
 * in the graph.
 * 
 * @author jejking
 *
 */
public interface DrucksachenPropertyNames {

    public static final String DRUCKSACHE_ID = "DRUCKSACHE_ID";
    public static final String ORIGINAL_URL = "ORIGINAL_URL";
    public static final String DATE = "DATE";
    
    public static final String REF_LOCATION = "REF_LOCATION";
    public static final String IN_HEADER = "IN_HEADER";
    public static final String IN_BODY = "IN_BODY";
    
}
