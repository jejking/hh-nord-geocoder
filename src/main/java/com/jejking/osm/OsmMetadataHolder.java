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
package com.jejking.osm;

import org.joda.time.DateTime;

import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract value class holding attributes shared by nodes, ways and relations
 * as found in Open Street Map .osm XML files.
 *  
 * @author jejking
 *
 */
public final class OsmMetadataHolder implements OsmMetadata {

    private final Long id;
    private final Optional<Long> version;
    private final Optional<DateTime> timestamp;
    private final Optional<Long> changeset;
    private final Optional<Long> uid;
    private final Optional<String> user;
    
    /**
     * Constructor. No property may be null - if not present, use {@link Optional#absent()}.
     * 
     * @param id numerical identifier, corresponds to the <tt>id</tt> attribute.
     * @param version value of the <tt>version</tt> attribute, if present
     * @param timestamp value of the <tt>timestamp</tt> attribute, if present
     * @param changeset value of the <tt>changeset</tt> attribute, if present
     * @param uid value of the <tt>uid</tt> attribute if present
     * @param user value of the <tt>user</tt> attribute if present.
     * @throws NullPointerException if any argument is <code>null</code>
     */
    public OsmMetadataHolder(Long id, Optional<Long> version,
            Optional<DateTime> timestamp, Optional<Long> changeset, Optional<Long> uid, Optional<String> user) {
        super();
        
        this.id = checkNotNull(id);
        this.version = checkNotNull(version);
        this.timestamp = checkNotNull(timestamp);
        this.changeset = checkNotNull(changeset);
        this.uid = checkNotNull(uid);
        this.user = checkNotNull(user);
    }


    
    /* (non-Javadoc)
     * @see com.jejking.osm.OsmMetadata#getId()
     */
    @Override
    public Long getId() {
        return id;
    }


    
     
    /**
     * @return the version
     */
    public Optional<Long> getVersion() {
        return version;
    }


    
    /* (non-Javadoc)
     * @see com.jejking.osm.OsmMetadata#getTimestamp()
     */
    @Override
    public Optional<DateTime> getTimestamp() {
        return timestamp;
    }


    
    /* (non-Javadoc)
     * @see com.jejking.osm.OsmMetadata#getChangeset()
     */
    @Override
    public Optional<Long> getChangeset() {
        return changeset;
    }


    
    /* (non-Javadoc)
     * @see com.jejking.osm.OsmMetadata#getUid()
     */
    @Override
    public Optional<Long> getUid() {
        return uid;
    }


    
    /* (non-Javadoc)
     * @see com.jejking.osm.OsmMetadata#getUser()
     */
    @Override
    public Optional<String> getUser() {
        return user;
    }



    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((changeset == null) ? 0 : changeset.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        result = prime * result + ((uid == null) ? 0 : uid.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }



    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OsmMetadataHolder)) {
            return false;
        }
        OsmMetadataHolder other = (OsmMetadataHolder) obj;
        if (changeset == null) {
            if (other.changeset != null) {
                return false;
            }
        } else if (!changeset.equals(other.changeset)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
       
        if (timestamp == null) {
            if (other.timestamp != null) {
                return false;
            }
        } else if (!timestamp.equals(other.timestamp)) {
            return false;
        }
        if (uid == null) {
            if (other.uid != null) {
                return false;
            }
        } else if (!uid.equals(other.uid)) {
            return false;
        }
        if (user == null) {
            if (other.user != null) {
                return false;
            }
        } else if (!user.equals(other.user)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }



    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OsmMetadataHolder [id=" + id + ", version=" + version + ", timestamp="
                + timestamp + ", changeset=" + changeset + ", uid=" + uid + ", user=" + user + "]";
    }
    
    
}
