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

import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * Class that enhances a {@link OsmWay} with a {@link Geometry}
 * and some related operations.
 * 
 * @author jejking
 *
 */
public class OsmWayWithGeometry {

    private final OsmWay way;
    
    public OsmWayWithGeometry(OsmWay way) {
        this.way = way;
    }

    public Geometry getLineString() {
        return null;
    }
    
    public Point getCentroid() {
        return null;
    }
    
    public OsmWayWithGeometry union(OsmWayWithGeometry other) {
        return null;
    }
    
    /**
     * @return
     * @see info.jejking.hamburg.nord.geocoder.osm.OsmWay#getId()
     */
    public long getId() {
        return way.getId();
    }

//    /**
//     * @return
//     * @see info.jejking.hamburg.nord.geocoder.osm.OsmWay#getNodes()
//     */
//    public List<OsmNode> getNodes() {
//        return way.getNodes();
//    }

    /**
     * @return
     * @see info.jejking.hamburg.nord.geocoder.osm.OsmWay#getProperties()
     */
    public Map<String, String> getProperties() {
        return way.getProperties();
    }
    
    
}
