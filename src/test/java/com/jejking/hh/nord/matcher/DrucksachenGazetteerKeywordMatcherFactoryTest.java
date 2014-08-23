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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jejking.hh.nord.TestUtil;
import com.jejking.hh.nord.corpus.RawDrucksache;
import com.jejking.hh.nord.gazetteer.GazetteerEntryTypes;

import static org.junit.Assert.*;

/**
 * Tests for {@link DrucksachenGazetteerKeywordMatcherFactory}.
 * 
 * @author jejking
 *
 */
public class DrucksachenGazetteerKeywordMatcherFactoryTest {

    private static GraphDatabaseService graph;
    
    @BeforeClass
    public static void init() {
        graph = TestUtil.createTestDatabase();
        TestUtil.writeHamburgPolygonsToGraph(graph);
        TestUtil.writeUhlenhorstOsmStreetsToGraph(graph);
        TestUtil.writeUhlenhorstPoisToGraph(graph);
        
    }
    
    @Test
    public void matcherCreatedForEachLabelSupplied() {
        DrucksachenGazetteerKeywordMatcherFactory factory = new DrucksachenGazetteerKeywordMatcherFactory();
        Map<String, DrucksachenGazetteerKeywordMatcher> matchers = factory
                                                                    .createKeywordMatchersFromGazetteer(graph, 
                                                                            ImmutableList.of(GazetteerEntryTypes.STREET,
                                                                                             GazetteerEntryTypes.SCHOOL));
        
        assertEquals(2, matchers.size());
        assertTrue(matchers.containsKey(GazetteerEntryTypes.STREET));
        assertTrue(matchers.containsKey(GazetteerEntryTypes.SCHOOL));
        
        assertTrue(matchers.get(GazetteerEntryTypes.STREET).getGazetteerKeywordMatcher().getKeywordSet().contains("Mundsburger Damm"));
        assertTrue(matchers.get(GazetteerEntryTypes.STREET).getGazetteerKeywordMatcher().getKeywordSet().contains("Mundsburger Damms"));
        assertTrue(matchers.get(GazetteerEntryTypes.STREET).getGazetteerKeywordMatcher().getKeywordSet().contains("Mundsburger Dammes"));
        assertTrue(matchers.get(GazetteerEntryTypes.SCHOOL).getGazetteerKeywordMatcher().getKeywordSet().contains("Gymnasium Lerchenfeld"));
    }

    @Test
    public void runsThrough() {
        DrucksachenGazetteerKeywordMatcherFactory factory = new DrucksachenGazetteerKeywordMatcherFactory();
        ImmutableMap<String, DrucksachenGazetteerKeywordMatcher> matcherMap = factory.createKeywordMatchersFromGazetteer(graph, ImmutableList.of(
                GazetteerEntryTypes.NAMED_AREA,
                GazetteerEntryTypes.STREET,
                GazetteerEntryTypes.SCHOOL,
                GazetteerEntryTypes.HOSPITAL,
                GazetteerEntryTypes.CINEMA,
                GazetteerEntryTypes.UNIVERSITY));
        
        RawDrucksache drucksache = makeTestDrucksache();
        
        // we must run some sample tests to see if it's worked....
        DrucksachenGazetteerKeywordMatcher namedAreaMatcher = matcherMap.get(GazetteerEntryTypes.NAMED_AREA);
        Matches adminAreaMatches = namedAreaMatcher.call(drucksache);
        // expect to find in body, Hohenfelde and Uhlenhorst
        assertTrue(adminAreaMatches.getMatchesInHeader().isEmpty());
        assertTrue(adminAreaMatches.getMatchesInBody().containsKey("Hohenfelde"));
        assertTrue(adminAreaMatches.getMatchesInBody().containsKey("Uhlenhorst"));
        
        
        DrucksachenGazetteerKeywordMatcher streetMatcher = matcherMap.get(GazetteerEntryTypes.STREET);
        Matches streetMatches = streetMatcher.call(drucksache);
        assertTrue(streetMatches.getMatchesInHeader().containsKey("Uhlenhorster Weg"));
        assertTrue(streetMatches.getMatchesInBody().containsKey("Mundsburger Damm"));
        
        DrucksachenGazetteerKeywordMatcher poiMatcher = matcherMap.get(GazetteerEntryTypes.SCHOOL);
        Matches poiMatches = poiMatcher.call(drucksache);
        assertTrue(poiMatches.getMatchesInHeader().isEmpty());
        assertTrue(poiMatches.getMatchesInBody().containsKey("Gymnasium Lerchenfeld"));
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
