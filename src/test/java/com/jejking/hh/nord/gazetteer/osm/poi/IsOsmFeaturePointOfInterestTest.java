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
package com.jejking.hh.nord.gazetteer.osm.poi;


import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.jejking.hh.nord.gazetteer.osm.DummyOsmComponent;
import com.jejking.hh.nord.gazetteer.osm.poi.IsOsmFeaturePointOfInterest;
import com.jejking.osm.OsmMetadataHolder;

import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link IsOsmFeaturePointOfInterest}.
 * 
 * @author jejking
 * 
 */
public class IsOsmFeaturePointOfInterestTest {

    private static final IsOsmFeaturePointOfInterest PREDICATE = new IsOsmFeaturePointOfInterest();
    
    private static final OsmMetadataHolder DUMMY_METADATA = new OsmMetadataHolder(
                                                                    1L, 
                                                                    Optional.of(1L), 
                                                                    Optional.of(new DateTime()), 
                                                                    Optional.of(1L), 
                                                                    Optional.of(1L), 
                                                                    Optional.of("foo"));
    
    private DummyOsmComponent buildTestObject(ImmutableMap<String, String> props) {
        return new DummyOsmComponent(DUMMY_METADATA, props);
    }
    
    
    @Test
    public void filterRetainsPublicTransportStationsAndStops() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(publicTransport, station);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(PREDICATE.call(c1));
        
        ImmutableMap.Builder<String, String> builder2 = ImmutableMap.builder();
        builder2.put(publicTransport, stopPosition);
        DummyOsmComponent c2 = buildTestObject(builder2.build());
        assertTrue(PREDICATE.call(c2));
        
        ImmutableMap.Builder<String, String> builder3 = ImmutableMap.builder();
        builder3.put(publicTransport, "foo");
        DummyOsmComponent c3 = buildTestObject(builder3.build());
        assertFalse(PREDICATE.call(c3));
    }

    @Test
    public void filterRetainsRailwayStations() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(railway, station);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(PREDICATE.call(c1));

        ImmutableMap.Builder<String, String> builder2 = ImmutableMap.builder();
        builder2.put(railway, "track");
        DummyOsmComponent c2 = buildTestObject(builder2.build());
        assertFalse(PREDICATE.call(c2));
    }
    
    @Test
    public void filterRetainsPlacesOfWorship() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(amenity, placeOfWorship);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(PREDICATE.call(c1));
        
    }

    @Test
    public void filterRetainsSchools() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(amenity, school);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(PREDICATE.call(c1));
    }
    
    @Test
    public void filterRetainsUniversities() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(amenity, university);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(PREDICATE.call(c1));
    }

 

    @Test
    public void filterRetainsEmergencyServices() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(emergency, "Rettung");
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(PREDICATE.call(c1));
        
        ImmutableMap.Builder<String, String> builder2 = ImmutableMap.builder();
        builder2.put(emergency, "Feuerwache");
        DummyOsmComponent c2 = buildTestObject(builder2.build());
        assertTrue(PREDICATE.call(c2));
    }
    
    @Test
    public void filterRetainsFireStations() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(amenity, firestation);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(PREDICATE.call(c1));
    }

    @Test
    public void filterRetainsPoliceStations() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(amenity, police);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(PREDICATE.call(c1));
    }

    @Test
    public void filterRetainsPublicAdministrationBuildings() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(amenity, publicBuilding);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(PREDICATE.call(c1));
    }
    
    @Test
    public void filterRetainsTheatres() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(amenity, theatre);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(PREDICATE.call(c1));
    }
    
    @Test
    public void filterRetainsNamedParks() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(leisure, park);
        builder1.put(name, "Foopark");
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(PREDICATE.call(c1));
    }
    
    @Test
    public void filterDoesNotRetainUnnamedParks() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(leisure, park);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertFalse(PREDICATE.call(c1));
    }
    
    @Test
    public void filterRetainsNamedNaturalWater() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(natural, water);
        builder1.put(name, "Alster");
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(PREDICATE.call(c1));
    }
    
    @Test
    public void filterDoesNotRetainsUnnamedNaturalWater() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(natural, water);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertFalse(PREDICATE.call(c1));
    }
    
    @Test
    public void filterRetainsNamedCanals() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(waterway, canal);
        builder1.put(name, "Fookanal");
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(PREDICATE.call(c1));
    }
    
    @Test
    public void filterDoesNotRetainUnnamedCanals() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(waterway, canal);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertFalse(PREDICATE.call(c1));
    }
    
    @Test
    public void filterRetainsNamedRivers() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(waterway, river);
        builder1.put(name, "Wandse");
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(PREDICATE.call(c1));
    }

    @Test
    public void filterDoesNotRetainUnnamedRivers() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(waterway, river);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertFalse(PREDICATE.call(c1));
    }
    
    @Test
    public void filterRetainsCinemas() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(amenity, cinema);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(PREDICATE.call(c1));
    }

    @Test
    public void filterRetainsLibraries() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(amenity, library);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(PREDICATE.call(c1));
    }

    @Test
    public void filterRetainsHospital() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(amenity, hospital);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(PREDICATE.call(c1));
    }
    
    
    @Test
    public void filterDoesNotRetainsBuildingNodesWithHouseNumbers() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(houseNumber, "34");
        builder1.put("building", "yes");
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertFalse(PREDICATE.call(c1));
        
    }

}
