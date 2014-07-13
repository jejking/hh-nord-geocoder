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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Point;

/**
 * Value class to represent an Open Street Map node.
 * 
 * @author jejking
 *
 */
public final class OsmNode extends OsmComponent {

    private final Point point;
    
    /**
     * Constructor. No argument may be <code>null</code>.
     * 
     * @param metadata metadata
     * @param properties properties
     * @param point geometry constructed from the <tt>lat</tt> and <tt>lon</tt> attributes
     * @throws NullPointerException if any argument is <code>null</code>
     */
    public OsmNode(OsmMetadata metadata, ImmutableMap<String, String> properties, Point point) {
        super(metadata, properties);
        this.point = checkNotNull(point);
    }



    /**
     * @return the point
     */
    public Point getPoint() {
        return point;
    }



    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OsmNode [point=" + point + ", toString()=" + super.toString() + "]";
    }

    
    
}
