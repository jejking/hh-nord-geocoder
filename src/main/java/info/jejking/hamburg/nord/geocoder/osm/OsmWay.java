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
package info.jejking.hamburg.nord.geocoder.osm;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Value class representing an Open Street Map <code>way</code>, but
 * with <code>nd ref</code> attributes replaced with object references.
 * 
 * @author jejking
 *
 */
public final class OsmWay {

    private final long id;
    private final List<OsmNode> nodes;
    private final Map<String, String> properties;
    
    /**
     * Constructs value object.
     * 
     * @param id
     * @param nodeRefs
     * @param properties
     */
    public OsmWay(long id, List<OsmNode> nodes, Map<String, String> properties) {
        super();
        this.id = id;
        this.nodes = Collections.unmodifiableList(nodes);
        this.properties = Collections.unmodifiableMap(properties);
    }

    
    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    
    /**
     * @return the nodes
     */
    public List<OsmNode> getNodes() {
        return nodes;
    }

    
    /**
     * @return the properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
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
        if (!(obj instanceof OsmWay)) {
            return false;
        }
        OsmWay other = (OsmWay) obj;
        if (id != other.id) {
            return false;
        }
        if (nodes == null) {
            if (other.nodes != null) {
                return false;
            }
        } else if (!nodes.equals(other.nodes)) {
            return false;
        }
        if (properties == null) {
            if (other.properties != null) {
                return false;
            }
        } else if (!properties.equals(other.properties)) {
            return false;
        }
        return true;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OsmWay [id=" + id + ", nodes=" + nodes + ", properties=" + properties + "]";
    }
    
    
    
}

