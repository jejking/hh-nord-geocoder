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
package com.jejking.hh.nord.gazetteer;

/**
 * Defines some "types" from the point of a view of a typical geocoding gazetteer
 * which defines triples of "Name, Geometry, Type". In this sense it is a label.
 * 
 * <p>The types are defined as strings so that the set is dynamically extensible,
 * for instance when importing assorted points of interest from Open Street Map.</p>
 * 
 * @author jejking
 *
 */
public interface GazetteerEntryTypes {

    public static final String GAZETTEER = "GAZETTEER";
    
    public static final String CITY = "CITY";
    public static final String BOROUGH = "BOROUGH";
    public static final String NAMED_AREA = "NAMED_AREA";
    public static final String NUMBERED_DISTRICT = "NUMBERED_DISTRICT";
    
    public static final String STREET = "STREET";
    
    public static final String BUILDING = "BUILDING";
    
    public static final String TRANSPORT_STOP = "TRANSPORT_STOP";
    public static final String PLACE_OF_WORSHIP = "PLACE_OF_WORSHIP";
    public static final String SCHOOL = "SCHOOL";
    public static final String UNIVERSITY = "UNIVERSITY";
    public static final String EMERGENCY_SERVICES = "EMERGENCY_SERVICES";
    public static final String ADMINISTRATION = "ADMINISTRATION";
    public static final String PARK = "PARK";
    public static final String WATERWAY = "WATERWAY";
    public static final String THEATRE = "THEATRE";
    public static final String CINEMA = "CINEMA";
    public static final String LIBRARY = "LIBRARY";
    public static final String HOSPITAL = "HOSPITAL";
    public static final String PUBLIC_BUILDING = "PUBLIC_BUILDING";

    
}
