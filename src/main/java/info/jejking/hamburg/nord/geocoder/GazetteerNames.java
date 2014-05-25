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
package info.jejking.hamburg.nord.geocoder;


/**
 * Constants for property names and the like.
 * 
 * @author jejking
 *
 */
public interface GazetteerNames {

    public static final String NAME = "NAME";
    public static final String TYPE = "TYPE";
    
    public static final String ADMINISTRATIVE_LAYER = "ADMINISTRATIVE_LAYER";
    public static final String STREET_LAYER = "STREET_LAYER";
    
    public static final String GAZETTEER_FULLTEXT = GazetteerEntryTypes.ADMIN_AREA + "-fulltext";
    
   
}
