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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.jejking.hh.nord.gazetteer.GazetteerEntryTypes;

/**
 * Tests {@link GazetteerKeywordMatcher}.
 * 
 * @author jejking
 *
 */
public class GazetteerKeywordMatcherTest {
    
    @Test
    public void findsSingleWordNames() {
        GazetteerKeywordMatcher matcher = new GazetteerKeywordMatcher(
                                            ImmutableList.of("Foostraße", "Kuhkamp", "Katzenstraße"), 
                                            GazetteerEntryTypes.STREET);
        
        ImmutableSet<String> matches = matcher.call("In der Foostraße gibt es eine Bar. Am Kuhkamp dagegen, gibt es keine");
        assertTrue(matches.contains("Foostraße"));
        assertTrue(matches.contains("Kuhkamp"));
        assertFalse(matches.contains("Katzenstraße"));
    }
    
    @Test
    public void findsDoubleWordNames() {
        GazetteerKeywordMatcher matcher = new GazetteerKeywordMatcher(
                ImmutableList.of("Mundsburger Damm", "Winterhuder Weg", "Winterhuder Marktplatz"), 
                GazetteerEntryTypes.STREET);

        ImmutableSet<String> matches = matcher.call("Am Mundsburger Damm passierte ein Unfall. Der Winterhuder Weg blieb unfallfrei.");
        assertTrue(matches.contains("Mundsburger Damm"));
        assertTrue(matches.contains("Winterhuder Weg"));
        assertFalse(matches.contains("Winterhuder Marktplatz"));
    }
    
    @Test
    public void findsMultipleWordNames() {
        // we love Platt :)
        GazetteerKeywordMatcher matcher = new GazetteerKeywordMatcher(
                ImmutableList.of("Op de Wisch", "Op de Elg", "Op'n Hesel"), 
                GazetteerEntryTypes.STREET);
        
        ImmutableSet<String> matches = matcher.call("Die Straßennamedn Op de Wisch und Op'n Hesel kommen aus dem Plattdeutschen");
        assertTrue(matches.contains("Op de Wisch"));
        assertTrue(matches.contains("Op'n Hesel"));
        assertFalse(matches.contains("Op de Elg"));
    }
    
    @Test
    public void doesNotEmitPartialWordMatches() {
        GazetteerKeywordMatcher matcher = new GazetteerKeywordMatcher(
                ImmutableList.of("Mundsburg", "Winterhuder Weg", "Winterhuder Marktplatz"), 
                GazetteerEntryTypes.STREET);

        ImmutableSet<String> matches = matcher.call("An der Mundsburger Brücke gibt es eine Bushaltestelle. Es gibt auch welche am Winterhuder Weg.");
        assertFalse(matches.contains("Mundsburg"));
        assertTrue(matches.contains("Winterhuder Weg"));
        assertFalse(matches.contains("Winterhuder Marktplatz"));
    }
    
    @Test
    public void doesNotEmitMatchForTypos() {
        GazetteerKeywordMatcher matcher = new GazetteerKeywordMatcher(
                ImmutableList.of("Foostraße", "Kuhkamp", "Katzenstraße"), 
                GazetteerEntryTypes.STREET);

        ImmutableSet<String> matches = matcher.call("In der Foostrasse gibt es eine Bar. Am Kuhkamp dagegen, gibt es keine");
        assertFalse(matches.contains("Foostraße"));
        assertTrue(matches.contains("Kuhkamp"));
        assertFalse(matches.contains("Katzenstraße"));
    }
    
    @Test
    public void isMatcherReusable() {
        
        final GazetteerKeywordMatcher matcher = new GazetteerKeywordMatcher(
                ImmutableList.of("Foostraße", "Kuhkamp", "Katzenstraße"), 
                GazetteerEntryTypes.STREET);
        final ConcurrentLinkedQueue<Throwable> exceptionQueue = new ConcurrentLinkedQueue<>();
        
        Runnable matchingTask1 = new Runnable() {
            
            @Override
            public void run() {
                
                try {
                    ImmutableSet<String> matches = matcher.call("In der Foostraße gibt es eine Bar. Am Kuhkamp dagegen, gibt es keine");
                    assertTrue(matches.contains("Foostraße"));
                    assertTrue(matches.contains("Kuhkamp"));
                    assertFalse(matches.contains("Katzenstraße"));
                } catch (Throwable error) {
                    exceptionQueue.add(error);
                }
            }
        };
        
        for (int i = 0; i < 1000; i++) { 
            matchingTask1.run();
        }
        
        assertTrue("Queue contains " + exceptionQueue.size() + " exceptions", exceptionQueue.isEmpty());
        
    }
    
    
   

}
