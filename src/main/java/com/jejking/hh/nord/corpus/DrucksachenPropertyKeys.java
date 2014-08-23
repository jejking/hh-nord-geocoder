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
package com.jejking.hh.nord.corpus;

/**
 * Frequently occurring keys for properties of {@link RawDrucksache}.
 * 
 * @author jejking
 *
 */
public interface DrucksachenPropertyKeys {

    public static final String BETREFF = "Betreff"; // what's it all about
    public static final String STATUS = "Status";
    public static final String VERFASSER = "Verfasser"; // who wrote it
    public static final String ART = "Drucksache-Art"; // type
    public static final String FEDERFUEHREND = "Federführend"; // coordinating department
    public static final String BERATUNGSFOLGE = "Beratungsfolge"; // list of meetings where document discussed
    public static final String AKTENZEICHEND = "Aktenzeichen"; // file identifer
    public static final String ANLAGEN = "Anlagen"; // attachments
    
}
