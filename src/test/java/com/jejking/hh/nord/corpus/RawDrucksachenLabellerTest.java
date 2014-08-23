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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;


import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jejking.hh.nord.corpus.RawDrucksache;
import com.jejking.hh.nord.corpus.RawDrucksachenLabeller;
import com.jejking.hh.nord.matcher.DrucksachenGazetteerKeywordMatcher;
import com.jejking.hh.nord.matcher.Matches;
import com.jejking.hh.nord.matcher.RawDrucksacheWithLabelledMatches;

import static org.junit.Assert.*;

/**
 * Test for {@link RawDrucksachenLabeller}.
 * 
 * @author jejking
 *
 */
public class RawDrucksachenLabellerTest {

    private ImmutableMap<String, DrucksachenGazetteerKeywordMatcher> matchersMap;
    private RawDrucksache rawDrucksache;
    private RawDrucksachenLabeller labeller;
    private RawDrucksacheWithLabelledMatches rawDrucksacheWithLabelledMatches;
    
    @Test
    public void runsThrough() {
        givenAMatchersMap();
        givenADrucksache();
        givenALabeller();
        whenTheDrucksacheIsLabelled();
        thenTheDrucksacheIsLabelledCorrectly();
    }


    private void givenALabeller() {
        this.labeller = new RawDrucksachenLabeller(matchersMap);
    }


    private void thenTheDrucksacheIsLabelledCorrectly() {
        assertSame(this.rawDrucksache, this.rawDrucksacheWithLabelledMatches.getOriginal());
        Map<String, Matches> matchesMap = this.rawDrucksacheWithLabelledMatches.getMatchesMap();
        
        assertEquals(2, matchesMap.size());
        
        Matches fooMatches = matchesMap.get("foo");
        assertNotNull(fooMatches);
        assertTrue(fooMatches.getMatchesInHeader().containsKey("foo"));
        assertEquals(1, fooMatches.getMatchesInHeader().size());
        assertTrue(fooMatches.getMatchesInBody().containsKey("fu"));
        assertEquals(1, fooMatches.getMatchesInBody().size());
        
        Matches barMatches = matchesMap.get("bar");
        assertTrue(barMatches.getMatchesInHeader().isEmpty());
        assertTrue(barMatches.getMatchesInBody().containsKey("pub"));
        assertEquals(1, barMatches.getMatchesInBody().size());
    }


    private void whenTheDrucksacheIsLabelled() {
        this.rawDrucksacheWithLabelledMatches = this.labeller.call(this.rawDrucksache);
    }


    private void givenADrucksache() {
        try {
            this.rawDrucksache= new RawDrucksache("anID", 
                                                    new URL("http://foo.com/bar"), 
                                                    Optional.of(new LocalDate(2014, DateTimeConstants.JUNE, 15)),
                                                    ImmutableMap.of("Betreff", "Thing foo", "Wibble", "Wobble"), 
                                                    ImmutableList.of("Down the pub", "Santa fu"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        
    }


    private void givenAMatchersMap() {
        DrucksachenGazetteerKeywordMatcher fooMatcher = new DrucksachenGazetteerKeywordMatcher(ImmutableList.of("foo", "fu"), "foo");
        DrucksachenGazetteerKeywordMatcher barMatcher = new DrucksachenGazetteerKeywordMatcher(ImmutableList.of("bar", "pub"), "bar");
        this.matchersMap = ImmutableMap.of("foo", fooMatcher, "bar", barMatcher);
    }

}
