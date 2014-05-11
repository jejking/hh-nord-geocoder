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

import org.joda.time.DateTime;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class OsmComponent implements OsmMetadata, OsmProperties {

    private final OsmMetadata metadata;
    private final ImmutableMap<String, String> properties;
    
    public OsmComponent(OsmMetadata metadata, ImmutableMap<String, String> properties) {
        this.metadata = checkNotNull(metadata);
        this.properties = checkNotNull(properties);
    }
    
    @Override
    public final ImmutableMap<String, String> getProperties() {
        return this.properties;
    }

    /**
     * @return
     * @see info.jejking.osm.OsmMetadata#getId()
     */
    public final Long getId() {
        return metadata.getId();
    }

    /**
     * @return
     * @see info.jejking.osm.OsmMetadata#getTimestamp()
     */
    public final Optional<DateTime> getTimestamp() {
        return metadata.getTimestamp();
    }

    /**
     * @return
     * @see info.jejking.osm.OsmMetadata#getChangeset()
     */
    public final Optional<Long> getChangeset() {
        return metadata.getChangeset();
    }

    /**
     * @return
     * @see info.jejking.osm.OsmMetadata#getUid()
     */
    public final Optional<Long> getUid() {
        return metadata.getUid();
    }

    /**
     * @return
     * @see info.jejking.osm.OsmMetadata#getUser()
     */
    public final Optional<String> getUser() {
        return metadata.getUser();
    }

    @Override
    public final Optional<Long> getVersion() {
        return metadata.getVersion();
    }
    
}
