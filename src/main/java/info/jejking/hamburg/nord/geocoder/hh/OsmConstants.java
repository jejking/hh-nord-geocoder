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
package info.jejking.hamburg.nord.geocoder.hh;

/**
 * Subset of terms used in Open Street Map tagging which we 
 * are interested in using.
 *  
 * @author jejking
 *
 */
public interface OsmConstants {

    String building = "building";
    String publicTransport = "public_transport";
    String station = "station";
    String stopPosition = "stop_position";
    String railway = "railway";
    String amenity = "amenity";
    String placeOfWorship = "place_of_worship";
    String school = "school";
    String university = "university";
    String police = "police";
    String firestation = "fire_station";
    String theatre = "theatre";
    String cinema = "cinema";
    String library = "library";
    String hospital = "hospital";
    String publicBuilding = "public_building";
    
    String emergency = "emergency";
    String leisure = "leisure";
    String park = "park"; // (but only include if name:?) is set.

    String natural= "natural";
    String water = "water";
    String waterway = "waterway";
    String canal = "canal"; // only if named
    String river = "river"; // only if named

    String houseNumber = "addr:housenumber";
    String street = "addr:street";
    String name = "name";
    
    String type = "type";
    String multipolygon = "multipolygon";
    
    String way = "way";
    String outer = "outer";
    String inner = "inner";
    
}
