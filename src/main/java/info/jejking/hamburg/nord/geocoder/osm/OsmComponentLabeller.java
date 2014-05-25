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
package info.jejking.hamburg.nord.geocoder.osm;

import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.amenity;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.building;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.cinema;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.emergency;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.firestation;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.hospital;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.leisure;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.library;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.park;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.placeOfWorship;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.police;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.publicBuilding;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.publicTransport;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.railway;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.school;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.theatre;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.university;

import java.util.Map;

import com.google.common.collect.ImmutableSet;

import info.jejking.hamburg.nord.geocoder.GazetteerEntryTypes;
import info.jejking.osm.OsmComponent;
import rx.functions.Func1;

/**
 * Function to map an {@link OsmComponent} via its properties to a set of labels
 * from {@link GazetteerEntryTypes} for use in Neo4j.
 * 
 * @author jejking
 *
 */
class OsmComponentLabeller implements Func1<OsmComponent, ImmutableSet<String>> {


    @Override
    public ImmutableSet<String> call(OsmComponent osmComponent) {
            ImmutableSet.Builder<String> setBuilder = ImmutableSet.builder();
            Map<String, String> props = osmComponent.getProperties();
            if (props.containsKey(building)) {
                setBuilder.add(GazetteerEntryTypes.BUILDING);
            }
            if (props.containsKey(amenity)) {
                String value = props.get(amenity);
                switch (value) {
                    case cinema : setBuilder.add(GazetteerEntryTypes.CINEMA); break;
                    case hospital : setBuilder.add(GazetteerEntryTypes.HOSPITAL); break;
                    case police : setBuilder.add(GazetteerEntryTypes.EMERGENCY_SERVICES); break;
                    case firestation : setBuilder.add(GazetteerEntryTypes.EMERGENCY_SERVICES); break;
                    case library: setBuilder.add(GazetteerEntryTypes.LIBRARY); break;
                    case school : setBuilder.add(GazetteerEntryTypes.SCHOOL); break;
                    case theatre : setBuilder.add(GazetteerEntryTypes.THEATRE); break;
                    case university : setBuilder.add(GazetteerEntryTypes.UNIVERSITY); break;
                    case placeOfWorship : setBuilder.add(GazetteerEntryTypes.PLACE_OF_WORSHIP); break;
                    case publicBuilding : setBuilder.add(GazetteerEntryTypes.PUBLIC_BUILDING); break;
                    default: ; // nothing
                }
            }
    
            if (props.containsKey(emergency)) {
                setBuilder.add(GazetteerEntryTypes.EMERGENCY_SERVICES);
            }
            
            if (props.containsKey(leisure) && props.get(leisure).equals(park)) {
                setBuilder.add(GazetteerEntryTypes.PARK);
            }

            if (props.containsKey(publicTransport) || props.containsKey(railway)) {
                setBuilder.add(GazetteerEntryTypes.TRANSPORT_STOP);
            }
            return setBuilder.build();
        }
    

}
