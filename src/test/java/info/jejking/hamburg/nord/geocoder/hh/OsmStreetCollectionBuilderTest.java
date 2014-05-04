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

import org.junit.BeforeClass;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * Tests for {@link OsmStreetCollectionBuilder}.
 * 
 * @author jejking
 *
 */
public class OsmStreetCollectionBuilderTest {

	private static OsmStreetCollectionBuilder builder;
	private static Map<String, Geometry> osmWays;

	@BeforeClass
	public static void init() {
		builder = new OsmStreetCollectionBuilder();
        osmWays = builder.buildRawStreetCollection();
        
	}
	
    @Test
    public void assemblesOsmNodes() {
        Map<Long, Point> osmNodes = builder.getOsmNodes();
        
        // <node id="122332" lat="53.5390647" lon="10.0322542"/> first node
        assertTrue(osmNodes.containsKey(Long.valueOf(122332L)));
        Point firstPoint = osmNodes.get(Long.valueOf(122332L));
        assertEquals(firstPoint.getX(), 10.0322542, 0.0000001);
        assertEquals(firstPoint.getY(), 53.5390647, 0.0000001);
        
        // <node id="2761334954" lat="53.5454436" lon="10.0026311"/> -- last node
        assertTrue(osmNodes.containsKey(Long.valueOf(2761334954L)));
        Point secondPoint = osmNodes.get(Long.valueOf(2761334954L));
        assertEquals(secondPoint.getX(), 10.0026311, 0.0000001);
        assertEquals(secondPoint.getY(), 53.5454436, 0.0000001);
    }
    
    @Test
    public void assemblesWays() {
    	// Itzehoer Weg
    	Geometry itzehoerWeg = osmWays.get("Itzehoer Weg");
    	assertNotNull(itzehoerWeg);
    	System.out.println("Itzehoer Weg: " + itzehoerWeg.toText());
    	
    	// Mundsburger Damm
    	Geometry mundsburgerDamm = osmWays.get("Mundsburger Damm");
    	assertNotNull(mundsburgerDamm);
    	System.out.println("Mundsburger Damm " + mundsburgerDamm.toText());
    	
    	// Foostraße
    	Geometry fooStrasse = osmWays.get("Foostraße");
    	assertNull(fooStrasse);
    }

}
