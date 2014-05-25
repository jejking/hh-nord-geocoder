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
package info.jejking.hamburg.nord.geocoder.hh;

import static org.junit.Assert.*;

import java.util.Map;

import info.jejking.hamburg.nord.geocoder.hh.CoordinateConverter;
import info.jejking.hamburg.nord.geocoder.hh.HamburgRawTreeBuilder;
import info.jejking.hamburg.nord.geocoder.hh.CoordinateConverter.PolygonConversion;
import info.jejking.hamburg.nord.geocoder.hh.CoordinateConverter.WktToGeometry;
import info.jejking.hamburg.nord.geocoder.hh.NamedTreeNode;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;


public class CoordinateConverterTest {

    @Test
    public void createWktPolygonFromRaw() {
        String in = "0 0 1 1 3 3.5";
        String expected =  "POLYGON((0 0, 1 1, 3 3.5))";
        
        CoordinateConverter.PolygonConversion converter = new PolygonConversion();
        assertEquals(expected, converter.convert(in));
    }
    
    @Test
    public void createPolygonFromWkt() {
        // these results obtained from the helpful website at http://cs2cs.mygeodata.eu/
        double[][] expected = new double[][] {
                                                {4.51125611529d, 0.0d},
                                                {4.51126507418d, 0.000405871916451d},
                                                {4.5112829855d, 0.00315678165038d},
                                                {4.51125611529d, 0.0d}
                                             };
        
        CoordinateConverter.WktToGeometry convert = new WktToGeometry();
        Polygon output = convert.convert("POLYGON((0 0, 1 45, 3 350, 0 0))");
        Coordinate[] coords = output.getCoordinates();
        
        for (int i = 0; i < coords.length; i++) {
            Coordinate coord = coords[i];
            assertEquals(expected[i][0], coord.x, 0.0000000001);
            assertEquals(expected[i][1], coord.y, 0.0000000001);
        }
        
    }
    
    @Test
    public void convertRawHamburgData() {
        HamburgRawTreeBuilder builder = new HamburgRawTreeBuilder();
        NamedTreeNode<String> rawHamburg = builder.buildRawTree();
        @SuppressWarnings("unused")
        NamedTreeNode<Polygon> polygonHamburg = new CoordinateConverter().rawToPolygon(rawHamburg);
        // just runs through to prove it doesn't crash out :)
    }
    
    public static class StringToIntConverter implements CoordinateConverter.Conversion<String, Integer> {

        @Override
        public Integer convert(String from) {
            return Integer.valueOf(from);
        }
        
    }
    
    @Test
    public void testRecursiveConversion() {
        NamedTreeNode<String> root = new NamedTreeNode<String>("root", "foo", "0");
        NamedTreeNode<String> one = new NamedTreeNode<String>("one", "foo", "1");
        NamedTreeNode<String> ten = new NamedTreeNode<String>("ten", "foo", "10");
        NamedTreeNode<String> two = new NamedTreeNode<String>("two","foo", "2");
        
        root.getChildren().put("one", one);
        root.getChildren().put("two", two);
        
        one.getChildren().put("ten", ten);
        
        CoordinateConverter converter = new CoordinateConverter();
        NamedTreeNode<Integer> rootInt = converter.convert(root, new StringToIntConverter());
        
        assertEquals(0, rootInt.getContent().intValue());
        assertEquals("root", rootInt.getName());
        
        Map<String, NamedTreeNode<Integer>> rootIntChildren = rootInt.getChildren();
        assertEquals(2, rootIntChildren.size());
        
        NamedTreeNode<Integer> oneInt = rootIntChildren.get("one");
        assertEquals(1, oneInt.getContent().intValue());
        
        NamedTreeNode<Integer> twoInt = rootIntChildren.get("two");
        assertEquals(2, twoInt.getContent().intValue());
        
        NamedTreeNode<Integer> tenInt = oneInt.getChildren().get("ten");
        assertEquals(10, tenInt.getContent().intValue());
    }
    
    @Test
    public void fixRootCreatesHamburgPolygon() {
        HamburgRawTreeBuilder builder = new HamburgRawTreeBuilder();
        NamedTreeNode<String> rawHamburg = builder.buildRawTree();
        NamedTreeNode<Polygon> roughRootPolygonHamburg = new CoordinateConverter().rawToPolygon(rawHamburg);
        @SuppressWarnings("unused")
        NamedTreeNode<Polygon> fixedRootPolygonHamburg = new CoordinateConverter().fixRoot(roughRootPolygonHamburg);
    }

}
