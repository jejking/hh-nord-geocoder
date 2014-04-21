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
 * Some trivial validation routines for which it is not worth
 * importing further libraries.
 * 
 * @author jejking
 *
 */
public class SimpleValidator {

    /**
     * Tests if string passed in is <code>null</code> or empty
     * when trimmed of whitespace.
     * 
     * @param string input
     * @return <code>true</code> if input is null or empty if trimmed,
     *          else <code>false</code>.
     */
    public static boolean isNullOrEmptyString(String string) {
        if (string == null) {
            return true;
        }
        if (string.trim().isEmpty()) {
            return true;
        }
        return false;
    }

}
