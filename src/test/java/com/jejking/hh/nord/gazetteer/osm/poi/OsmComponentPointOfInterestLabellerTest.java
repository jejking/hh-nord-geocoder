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

import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.jejking.hh.nord.gazetteer.GazetteerEntryTypes;
import com.jejking.hh.nord.gazetteer.osm.DummyOsmComponent;
import com.jejking.hh.nord.gazetteer.osm.poi.OsmComponentPointOfInterestLabeller;
import com.jejking.osm.OsmMetadataHolder;

/**
 * Tests for {@link OsmComponentPointOfInterestLabeller}.
 * 
 * @author jejking
 *
 */
public class OsmComponentPointOfInterestLabellerTest {

    private static final OsmComponentPointOfInterestLabeller LABELLER = new OsmComponentPointOfInterestLabeller();
    
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
    public void buildingsNotLabelled() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(building, "yes");
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertFalse(LABELLER.call(c1).contains(GazetteerEntryTypes.BUILDING));
    }
    
    @Test
    public void cinemasLabelled() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(amenity, cinema);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(LABELLER.call(c1).contains(GazetteerEntryTypes.CINEMA));
    }
    
    @Test
    public void firestationLabelledAsEmergencyServices() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(amenity, firestation);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(LABELLER.call(c1).contains(GazetteerEntryTypes.EMERGENCY_SERVICES));
    }
    
    @Test
    public void policeLabelledAsEmergencyServices() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(amenity, police);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(LABELLER.call(c1).contains(GazetteerEntryTypes.EMERGENCY_SERVICES));
    }
    
    @Test
    public void libraryLabelled() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(amenity, library);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(LABELLER.call(c1).contains(GazetteerEntryTypes.LIBRARY));
    }
    
    @Test
    public void hospitalLabelled() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(amenity, hospital);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(LABELLER.call(c1).contains(GazetteerEntryTypes.HOSPITAL));
    }
    
    
    @Test
    public void schoolLabelled() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(amenity, school);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(LABELLER.call(c1).contains(GazetteerEntryTypes.SCHOOL));
    }
    
    @Test
    public void universityLabelled() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(amenity, university);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(LABELLER.call(c1).contains(GazetteerEntryTypes.UNIVERSITY)); 
    }
    
    @Test
    public void placeOfWorshipLabelled() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(amenity, placeOfWorship);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(LABELLER.call(c1).contains(GazetteerEntryTypes.PLACE_OF_WORSHIP));
    }
    
    
    @Test
    public void emergencyServicesLabelled() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(emergency, "Rettung");
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(LABELLER.call(c1).contains(GazetteerEntryTypes.EMERGENCY_SERVICES));
    }
    
    @Test
    public void parkLabelled() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(leisure, park);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(LABELLER.call(c1).contains(GazetteerEntryTypes.PARK));
        
        ImmutableMap.Builder<String, String> builder2 = ImmutableMap.builder();
        builder2.put(leisure, "Schimmbad");
        DummyOsmComponent c2 = buildTestObject(builder2.build());
        assertFalse(LABELLER.call(c2).contains(GazetteerEntryTypes.PARK));
    }
    
    @Test
    public void publicTransportStationLabelledAsPublicTransport() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(publicTransport, station);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(LABELLER.call(c1).contains(GazetteerEntryTypes.TRANSPORT_STOP));
    }
    
    @Test
    public void publicTransportStopLabelledAsPublicTransport() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(publicTransport, stopPosition);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(LABELLER.call(c1).contains(GazetteerEntryTypes.TRANSPORT_STOP));
    }
    
    @Test
    public void railwayLabelledAsPublicTransport() {
        ImmutableMap.Builder<String, String> builder1 = ImmutableMap.builder();
        builder1.put(railway, station);
        DummyOsmComponent c1 = buildTestObject(builder1.build());
        assertTrue(LABELLER.call(c1).contains(GazetteerEntryTypes.TRANSPORT_STOP));
    }

}
