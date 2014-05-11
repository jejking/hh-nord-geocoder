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
package info.jejking.osm;

import static org.junit.Assert.*;


import org.junit.Test;

/**
 * Quick test of {@link SimpleObservable}.
 * 
 * @author jejking
 *
 */
public class SimpleObservableTest {

    @Test
    public void observerCanBeRegistered() {
        
        SimpleObservable<String> stringObservable = new SimpleObservable<>();
        EventCapturer<String> capturer = new EventCapturer<>();
        stringObservable.subscribe(capturer);
        
        assertFalse(capturer.completed);
        assertTrue(capturer.values.isEmpty());
        assertNull(capturer.e);
        
        stringObservable.next("foo");
        stringObservable.next("bar");
        stringObservable.error(new RuntimeException("test"));
        stringObservable.completed();
        
        assertTrue(capturer.completed);
        assertEquals(2, capturer.values.size());
        assertTrue(capturer.values.contains("foo"));
        assertTrue(capturer.values.contains("bar"));
        
        assertTrue(capturer.e instanceof RuntimeException);
        assertEquals("test", capturer.e.getMessage());
    }
    
    @Test
    public void observerCanBeDeregistered() {
        
        SimpleObservable<String> stringObservable = new SimpleObservable<>();
        EventCapturer<String> capturer = new EventCapturer<>();
        stringObservable.subscribe(capturer);
        
        assertFalse(capturer.completed);
        assertTrue(capturer.values.isEmpty());
        assertNull(capturer.e);
        
        stringObservable.next("foo");
        stringObservable.next("bar");

        
        stringObservable.unsubscribe(capturer);
        
        stringObservable.next("wibble");
        stringObservable.completed();
        
        assertEquals(2, capturer.values.size());
        assertTrue(capturer.values.contains("foo"));
        assertTrue(capturer.values.contains("bar"));
        
        assertFalse(capturer.completed);

    }
    
}
