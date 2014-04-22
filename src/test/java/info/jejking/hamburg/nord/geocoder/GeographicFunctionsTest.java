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
package info.jejking.hamburg.nord.geocoder;

import static info.jejking.hamburg.nord.geocoder.GeographicFunctions.computeEnvelopeAroundPoint;
import static java.lang.Math.sqrt;
import static org.junit.Assert.assertEquals;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.GeodeticCalculator;
import org.jaitools.jts.CoordinateSequence2D;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;


/**
 * Test class.
 * @author jejking
 *
 */
public class GeographicFunctionsTest {

    private Point point = new Point(new CoordinateSequence2D(10.016442, 53.568118), 
            JTSFactoryFinder.getGeometryFactory(null));
    
    @Test(expected = IllegalArgumentException.class)
    public void computeEnvelopeAroundPointRejectsNullPoint() {
        computeEnvelopeAroundPoint(null, 100);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void computeEnvelopeAroundPointRejectsNegativeRadius() {
        computeEnvelopeAroundPoint(point, -100);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void computeEnvelopeAroundPointRejectsZeroRadius() {
        computeEnvelopeAroundPoint(point, 0);
    }
    
    @Test
    public void computeEnvelopeAroundPointComputesEnvelope() {
        
        Envelope env = computeEnvelopeAroundPoint(point, 100);
        
        GeodeticCalculator geodeticCalculator = new GeodeticCalculator();
        geodeticCalculator.setStartingGeographicPoint(point.getX(), point.getY());
        geodeticCalculator.setDestinationGeographicPoint(env.getMinX(), env.getMinY());
        
        assertEquals(sqrt(2*100*100), geodeticCalculator.getOrthodromicDistance(), 0.0001);
        assertEquals(-135, geodeticCalculator.getAzimuth(), 0.0001);
        
        geodeticCalculator.setDestinationGeographicPoint(env.getMaxX(), env.getMaxY());
        assertEquals(sqrt(2*100*100), geodeticCalculator.getOrthodromicDistance(), 0.0001);
        assertEquals(45, geodeticCalculator.getAzimuth(), 0.0001);
        
                
    }
    
    public static void main(String[] args) {
        Point point = new Point(new CoordinateSequence2D(10.016442, 53.568118), 
                                        JTSFactoryFinder.getGeometryFactory(null));
        
        System.out.println(point.toText());
    }

}
