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
 */
package com.jejking.hh.nord.gazetteer.osm.poi;


import java.util.Map;

import rx.functions.Func1;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.jejking.osm.OsmComponent;

import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.*;

/**
 * Predicate for use in filtering streams of {@link OsmComponent} to retain only
 * those we are interested in working with and transforming to {@link PointOfInterest} instances.
 * 
 * @author jejking
 *
 */
final class IsOsmFeaturePointOfInterest implements Func1<OsmComponent, Boolean> {

    /*
     * Extracted to facilitate testing.
     */
    
    static final ImmutableMap<String, ImmutableSet<String>> interestingTagsWithNameOnly;
    
    static {
        ImmutableSet<String> emptyStringSet = ImmutableSet.of(); // signifies all values acceptable
        ImmutableMap.Builder<String, ImmutableSet<String>> interestingTagsWithNameOnlyBuilder = ImmutableMap.builder();
        interestingTagsWithNameOnlyBuilder
            .put(leisure, ImmutableSet.of(park))
            .put(natural, ImmutableSet.of(water))
            .put(waterway, ImmutableSet.of(canal, river))
            .put(publicTransport, ImmutableSet.of(station, stopPosition))
            .put(railway, ImmutableSet.of(station))
            .put(amenity, ImmutableSet.of(placeOfWorship, school, university, police, firestation, theatre, cinema, library, hospital, publicBuilding))
            .put(emergency, emptyStringSet);
        interestingTagsWithNameOnly = interestingTagsWithNameOnlyBuilder.build();
        
    }

    @Override
    public Boolean call(OsmComponent component) {
        ImmutableMap<String, String> props = component.getProperties();
        for (String tagInterestingIfNamed : interestingTagsWithNameOnly.keySet()) {
            if (props.containsKey(tagInterestingIfNamed)
                    && hasInterestingValue(interestingTagsWithNameOnly, props, tagInterestingIfNamed)
                    && props.containsKey(name)) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    private boolean hasInterestingValue(ImmutableMap<String, ImmutableSet<String>> map, Map<String, String> props,
            String tagInterestingIfNamed) {
        return map.get(tagInterestingIfNamed).isEmpty()
                || map.get(tagInterestingIfNamed).contains(props.get(tagInterestingIfNamed));
    }
}