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

public interface OsmMetadata {

    /**
     * @return the id
     */
    public abstract Long getId();

    /**
     * @return the timestamp
     */
    public abstract Optional<DateTime> getTimestamp();

    /**
     * @return the changeset
     */
    public abstract Optional<Long> getChangeset();

    /**
     * @return the uid
     */
    public abstract Optional<Long> getUid();

    /**
     * @return the user
     */
    public abstract Optional<String> getUser();
    
    /**
     * @return the version
     */
    public abstract Optional<Long> getVersion();

}