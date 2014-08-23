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
package com.jejking.hh.nord.corpus;

import static org.junit.Assert.*;

import java.net.URL;


import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jejking.hh.nord.corpus.DrucksacheDateEnhancer;
import com.jejking.hh.nord.corpus.RawDrucksache;

/**
 * Test for {@link DrucksacheDateEnhancer}.
 * 
 * @author jejking
 *
 */
public class DrucksacheDateEnhancerTest {

    private final DrucksacheDateEnhancer dateEnhancer = new DrucksacheDateEnhancer();
    
    @Test
    public void unchangedIfDatePresent() throws Exception {
        RawDrucksache rawDrucksache = new RawDrucksache(
                                            "foo", 
                                            new URL("http://foo.com/bar"),
                                            Optional.of(new LocalDate(2014, DateTimeConstants.JUNE, 14)), 
                                                ImmutableMap.of("foo", "bar"), 
                                                ImmutableList.of("foo", "bar"));    
        RawDrucksache enhanced = this.dateEnhancer.call(rawDrucksache);
        assertSame(rawDrucksache, enhanced);
        
    }
    
    @Test
    public void unchangedIfDateCouldNotBeFound() throws Exception {
        Optional<LocalDate> localDate = Optional.absent();
        RawDrucksache rawDrucksache = new RawDrucksache(
                "foo", 
                new URL("http://foo.com/bar"),
                localDate, 
                ImmutableMap.of("foo", "bar"), 
                ImmutableList.of("foo", "bar"));
        
        RawDrucksache enhanced = this.dateEnhancer.call(rawDrucksache);
        assertSame(rawDrucksache, enhanced);
        
    }
    
    @Test
    public void unchangedIfDatePatternMatchedButNotReallyADate() throws Exception {
        Optional<LocalDate> localDate = Optional.absent();
        RawDrucksache rawDrucksache = new RawDrucksache(
                "foo", 
                new URL("http://foo.com/bar"),
                localDate, 
                ImmutableMap.of("foo", "bar"), 
                ImmutableList.of("foo 14.14.2014", "bar")); // not a date, but matches regex
        
        RawDrucksache enhanced = this.dateEnhancer.call(rawDrucksache);
        assertSame(rawDrucksache, enhanced);
    }
    
    @Test
    public void newDateAddedIfFound() throws Exception {
        Optional<LocalDate> localDate = Optional.absent();
        RawDrucksache rawDrucksache = new RawDrucksache(
                "foo", 
                new URL("http://foo.com/bar"),
                localDate, 
                ImmutableMap.of("foo", "bar"), 
                ImmutableList.of("foo", "bar  14.06.2014")); // a date, matches regex
        
        RawDrucksache enhanced = this.dateEnhancer.call(rawDrucksache);
        assertNotSame(rawDrucksache, enhanced);
        
        LocalDate expected = new LocalDate(2014, DateTimeConstants.JUNE, 14);
        assertEquals(expected, enhanced.getDate().get());
        
    }
    
    @Test
    public void firstDateAddedIfManyFindable() throws Exception {
        Optional<LocalDate> localDate = Optional.absent();
        RawDrucksache rawDrucksache = new RawDrucksache(
                "foo", 
                new URL("http://foo.com/bar"),
                localDate, 
                ImmutableMap.of("foo", "bar"), 
                ImmutableList.of("13.24.2345 foo 14.06.2014", "13.05.2014 wibble wobble"));
        
        RawDrucksache enhanced = this.dateEnhancer.call(rawDrucksache);
        assertNotSame(rawDrucksache, enhanced);
        
        LocalDate expected = new LocalDate(2014, DateTimeConstants.JUNE, 14);
        assertEquals(expected, enhanced.getDate().get());
    }
    

}
