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
package info.jejking.hamburg.nord.geocoder.hh;

import info.jejking.osm.OsmComponent;
import info.jejking.osm.OsmMetadata;

import com.google.common.collect.ImmutableMap;

/**
 * Only for test purposes, excludes need for concrete subclasses.
 * 
 * @author jejking
 *
 */
class DummyOsmComponent extends OsmComponent {

    /**
     * Constructor.
     * @param metadata
     * @param properties
     */
    public DummyOsmComponent(OsmMetadata metadata, ImmutableMap<String, String> properties) {
        super(metadata, properties);
    }
    
}