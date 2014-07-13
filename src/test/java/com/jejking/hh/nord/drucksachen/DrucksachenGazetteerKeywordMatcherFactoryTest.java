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
package com.jejking.hh.nord.drucksachen;

import static com.jejking.hh.nord.TestUtil.*;
import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;


import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jejking.hh.nord.drucksachen.RawDrucksache;
import com.jejking.hh.nord.gazetteer.GazetteerEntryTypes;
import com.jejking.hh.nord.matcher.DrucksachenGazetteerKeywordMatcher;
import com.jejking.hh.nord.matcher.DrucksachenGazetteerKeywordMatcherFactory;
import com.jejking.hh.nord.matcher.Matches;

/**
 * Simple test of {@link DrucksachenGazetteerKeywordMatcherFactory}.
 * 
 * @author jejking
 *
 */
public class DrucksachenGazetteerKeywordMatcherFactoryTest {

    private GraphDatabaseService graph;
    
    @Before
    public void init() {
        this.graph = createTestDatabase();
        writeHamburgPolygonsToGraph(graph);
        writeUhlenhorstOsmStreetsToGraph(graph);
        writeUhlenhorstPoisToGraph(graph);
    }
    
    @Test
    public void runsThrough() {
        DrucksachenGazetteerKeywordMatcherFactory factory = new DrucksachenGazetteerKeywordMatcherFactory();
        ImmutableMap<String, DrucksachenGazetteerKeywordMatcher> matcherMap = factory.createKeywordMatchersFromGazetteer(graph);
        
        RawDrucksache drucksache = makeTestDrucksache();
        
        // we must run some sample tests to see if it's worked....
        DrucksachenGazetteerKeywordMatcher namedAreaMatcher = matcherMap.get(GazetteerEntryTypes.NAMED_AREA);
        Matches adminAreaMatches = namedAreaMatcher.call(drucksache);
        // expect to find in body, Hohenfelde and Uhlenhorst
        assertTrue(adminAreaMatches.getMatchesInHeader().isEmpty());
        assertTrue(adminAreaMatches.getMatchesInBody().contains("Hohenfelde"));
        assertTrue(adminAreaMatches.getMatchesInBody().contains("Uhlenhorst"));
        
        
        DrucksachenGazetteerKeywordMatcher streetMatcher = matcherMap.get(GazetteerEntryTypes.STREET);
        Matches streetMatches = streetMatcher.call(drucksache);
        assertTrue(streetMatches.getMatchesInHeader().contains("Uhlenhorster Weg"));
        assertTrue(streetMatches.getMatchesInBody().contains("Mundsburger Damm"));
        
        DrucksachenGazetteerKeywordMatcher poiMatcher = matcherMap.get(GazetteerEntryTypes.POINT_OF_INTEREST);
        Matches poiMatches = poiMatcher.call(drucksache);
        assertTrue(poiMatches.getMatchesInHeader().isEmpty());
        assertTrue(poiMatches.getMatchesInBody().contains("Gymnasium Lerchenfeld"));
    }

    private RawDrucksache makeTestDrucksache() {
        try {
            RawDrucksache drucksache = new RawDrucksache("123",
                    new URL("http://foo.com/bar"),
                    Optional.of(new LocalDate(2014, DateTimeConstants.JUNE, 15)),
                    ImmutableMap.of("Betreff", "Uhlenhorster Weg"), 
                    ImmutableList.of("Hohenfelde, Uhlenhorst Mundsburger Damm. Gymnasium Lerchenfeld"));
            return drucksache;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
}
