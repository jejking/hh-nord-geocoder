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
package com.jejking.hh.nord.gazetteer.osm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.functions.Func1;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;

/**
 * Class to compute a {@link Geometry}, in this case a {@link LineString} or
 * a {@link LinearRing} (if the line string is closed from a list of <tt>nd</tt> identifiers. 
 * The lookup table to map the identifier to a {@link Point} is supplied in 
 * the constructor (which sort of closes the function).
 * 
 * @author jejking
 *
 */
class WayNdsToLineString implements Func1<List<Long>, Geometry> {

    private final GeometryFactory geometryFactory;
    private final Map<Long, Point> knownOsmPoints;
    
    /**
     * Constructor.
     * @param geometryFactory may not be <code>null</code>
     * @param knownOsmPoints may not be <code>null</code>
     */
    public WayNdsToLineString(GeometryFactory geometryFactory, Map<Long, Point> knownOsmPoints) {
        this.knownOsmPoints = knownOsmPoints;
        this.geometryFactory = geometryFactory;
    }
    
    
    @Override
    public LineString call(List<Long> wayNdList) {
        List<Point> pointList = new ArrayList<>(wayNdList.size());
        
        // find all the referenced points. Ignore any we can't find, perhaps
        // because they were orphaned as we cut the extract around Nord.
        for (Long osmId : wayNdList) {
            Point point = this.knownOsmPoints.get(osmId);
            if (point != null) {
                pointList.add(point);
            }
        }
        
        // create a line string from the nodes....
        Coordinate[] coordinates = new Coordinate[pointList.size()];
        
        for (int i = 0; i < pointList.size(); i++) {
            coordinates[i] = pointList.get(i).getCoordinate();
        }
        return geometryFactory.createLineString(coordinates);
    }


}
