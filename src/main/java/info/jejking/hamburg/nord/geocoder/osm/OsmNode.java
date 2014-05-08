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
import java.util.Map;

import com.vividsolutions.jts.geom.Point;

/**
 * Value class to represent an Open Street Map node.
 * 
 * @author jejking
 *
 */
public final class OsmNode {

    private final long id;
    private final Point point;
    private final Map<String, String> properties;
    
    /**
     * Constructs using a point (composed from the lat, lon properties)
     * and arbitrary further properties.
     * 
     * @param point
     * @param properties
     */
    public OsmNode(long id, Point point, Map<String, String> properties) {
        super();
        this.id = id;
        this.point = point;
        this.properties = Collections.unmodifiableMap(properties);
    }


    
    /**
     * @return the point
     */
    public Point getPoint() {
        return point;
    }


    
    /**
     * @return the properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    
    public long getId() {
        return id;
    }



    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((point == null) ? 0 : point.hashCode());
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
        if (!(obj instanceof OsmNode)) {
            return false;
        }
        OsmNode other = (OsmNode) obj;
        if (id != other.id) {
            return false;
        }
        if (point == null) {
            if (other.point != null) {
                return false;
            }
        } else if (!point.equals(other.point)) {
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
        return "OsmNode [id=" + id + ", point=" + point.toText() + ", properties=" + properties + "]";
    }

        
    
    
}
