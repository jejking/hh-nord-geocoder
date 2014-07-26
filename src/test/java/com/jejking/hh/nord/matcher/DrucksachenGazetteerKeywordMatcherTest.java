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
package com.jejking.hh.nord.matcher;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link DrucksachenGazetteerKeywordMatcher}.
 * @author jejking
 *
 */
public class DrucksachenGazetteerKeywordMatcherTest {

    @Test
    public void filtersFreieUndHansestadtHamburg() {
        String in = "Die Freie und Hansestadt Hamburg ist ein Stadtstaat. Hamburg liegt an der Elbe.";
        String out = DrucksachenGazetteerKeywordMatcher.filterAuthorityNames(in);
        assertEquals("Die  ist ein Stadtstaat. Hamburg liegt an der Elbe.", out);
    }

}
