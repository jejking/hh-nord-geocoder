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
package com.jejking.hh.nord;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

import static java.lang.Math.sqrt;

/**
 * Useful functions.
 * 
 * @author jejking
 *
 */
public class GeographicFunctions {

    /**
     * Computes an envelope that is equivalent to a square touching a circle
     * of specified radius around a point. This is useful, for example, when 
     * constraining a search to be "within N meters" of a point as the envelope 
     * can be easily evaluated by a spatial index. The gap at the corners between
     * square and circle must then be filtered in a second step.
     * 
     * <p>The envelope is computed assuming the coordinate system WGS 84.</p>
     * 
     * @param point the point around which to construct an envelope, must not be <code>null</code>.
     * @param radiusInMeters number of meters around the point which are to be encompassed
     *    by the envelope. In other words, it describes the radius of a circle
     *    around the point. Must be a positive integer.
     * @return corresponding envelope touching the logical circle
     * @throws TransformException 
     */
    public static Envelope computeEnvelopeAroundPoint(Point point, int radiusInMeters) {
        if (point == null) {
            throw new IllegalArgumentException("point may not be null");
        }
        if (radiusInMeters <= 0) {
            throw new IllegalArgumentException("radius in meters must be greater be than zero");
        }
        
        try {
            // use pythagoras to work out length of hypotenuse
            final double distanceToCorner = sqrt(2*radiusInMeters*radiusInMeters); 
            
            GeodeticCalculator calc = new GeodeticCalculator(); // default is WGS 84 
            
            // this is the point we start at
            calc.setStartingGeographicPoint(point.getX(), point.getY());
            
            // -135 to the west, so going down to bottom left... (in northern hemisphere)
            calc.setDirection(-135, distanceToCorner);
            Point bottomLeft = JTS.toGeometry(calc.getDestinationPosition());
            
            // 45 degrees to the east, so going up to top right
            calc.setDirection(45, distanceToCorner);
            Point topRight = JTS.toGeometry(calc.getDestinationPosition());
                        
            return new Envelope(bottomLeft.getCoordinate(), topRight.getCoordinate());
        } catch (TransformException e) {
            throw new RuntimeException(e);
        }
        
    }

}
