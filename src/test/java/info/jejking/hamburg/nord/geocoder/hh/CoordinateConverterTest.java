package info.jejking.hamburg.nord.geocoder;

import static org.junit.Assert.*;

import java.util.Map;

import info.jejking.hamburg.nord.geocoder.hh.CoordinateConverter;
import info.jejking.hamburg.nord.geocoder.hh.HamburgRawTreeBuilder;
import info.jejking.hamburg.nord.geocoder.hh.CoordinateConverter.PolygonConversion;
import info.jejking.hamburg.nord.geocoder.hh.CoordinateConverter.WktToGeometry;
import info.jejking.hamburg.nord.geocoder.hh.NamedNode;

import org.junit.Assert;
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
        NamedNode<String> rawHamburg = builder.buildRawTree();
        NamedNode<Polygon> polygonHamburg = new CoordinateConverter().rawToPolygon(rawHamburg);
    }
    
    public static class StringToIntConverter implements CoordinateConverter.Conversion<String, Integer> {

        @Override
        public Integer convert(String from) {
            return Integer.valueOf(from);
        }
        
    }
    
    @Test
    public void testRecursiveConversion() {
        NamedNode<String> root = new NamedNode<String>("root", "0");
        NamedNode<String> one = new NamedNode<String>("one", "1");
        NamedNode<String> ten = new NamedNode<String>("ten", "10");
        NamedNode<String> two = new NamedNode<String>("two", "2");
        
        root.getChildren().put("one", one);
        root.getChildren().put("two", two);
        
        one.getChildren().put("ten", ten);
        
        CoordinateConverter converter = new CoordinateConverter();
        NamedNode<Integer> rootInt = converter.convert(root, new StringToIntConverter());
        
        assertEquals(0, rootInt.getContent().intValue());
        assertEquals("root", rootInt.getName());
        
        Map<String, NamedNode<Integer>> rootIntChildren = rootInt.getChildren();
        assertEquals(2, rootIntChildren.size());
        
        NamedNode<Integer> oneInt = rootIntChildren.get("one");
        assertEquals(1, oneInt.getContent().intValue());
        
        NamedNode<Integer> twoInt = rootIntChildren.get("two");
        assertEquals(2, twoInt.getContent().intValue());
        
        NamedNode<Integer> tenInt = oneInt.getChildren().get("ten");
        assertEquals(10, tenInt.getContent().intValue());
    }

}