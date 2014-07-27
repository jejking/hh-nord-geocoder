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

import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.amenity;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.cinema;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.firestation;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.hospital;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.leisure;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.library;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.park;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.placeOfWorship;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.police;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.publicBuilding;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.publicTransport;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.railway;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.school;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.theatre;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.university;

import java.util.Map;

import com.google.common.base.Optional;
import com.jejking.hh.nord.gazetteer.GazetteerEntryTypes;
import com.jejking.osm.OsmComponent;

import rx.functions.Func1;

/**
 * Function to map an {@link OsmComponent} via its properties a label to be used
 * in Neo4j. As it is possible that no suitable label is found, an optional type
 * is returned.
 * 
 * @author jejking
 *
 */
class OsmComponentPointOfInterestLabeller implements Func1<OsmComponent, Optional<String>> {


    @Override
    public Optional<String> call(OsmComponent osmComponent) {
            Map<String, String> props = osmComponent.getProperties();
            if (props.containsKey(amenity)) {
                String value = props.get(amenity);
                switch (value) {
                    case cinema : return Optional.of(GazetteerEntryTypes.CINEMA);
                    case hospital : return Optional.of(GazetteerEntryTypes.HOSPITAL);
                    case police : return Optional.of(GazetteerEntryTypes.EMERGENCY_SERVICES);
                    case firestation : return Optional.of(GazetteerEntryTypes.EMERGENCY_SERVICES);
                    case library: return Optional.of(GazetteerEntryTypes.LIBRARY);
                    case school : return Optional.of(GazetteerEntryTypes.SCHOOL);
                    case theatre : return Optional.of(GazetteerEntryTypes.THEATRE);
                    case university : return Optional.of(GazetteerEntryTypes.UNIVERSITY);
                    case placeOfWorship : return Optional.of(GazetteerEntryTypes.PLACE_OF_WORSHIP);
                    case publicBuilding : return Optional.of(GazetteerEntryTypes.PUBLIC_BUILDING);
                    default: ; // nothing
                }
            }
            if (props.containsKey(leisure) && props.get(leisure).equals(park)) {
                return Optional.of(GazetteerEntryTypes.PARK);
            }

            if (props.containsKey(publicTransport) || props.containsKey(railway)) {
                return Optional.of(GazetteerEntryTypes.TRANSPORT_STOP);
            }
            return Optional.absent();
        }
    

}
