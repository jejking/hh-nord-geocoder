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
package com.jejking.hh.nord.gazetteer.osm.streets;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


import java.util.Map;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.junit.Test;

import com.jejking.hh.nord.gazetteer.osm.streets.RxOsmStreetCollectionBuilder;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Tests {@link RxOsmStreetCollectionBuilder}.
 * 
 * @author jejking
 *
 */
public class RxOsmStreetCollectionBuilderTest {

    @Test
    public void worksAsExpected() {
        RxOsmStreetCollectionBuilder builder = new RxOsmStreetCollectionBuilder(
                JTSFactoryFinder.getGeometryFactory(null));

        Map<String, Geometry> osmWays = builder.streetsFromStream(RxOsmStreetCollectionBuilderTest.class
                .getResourceAsStream("/uhlenhorst-direct-export.osm"));
        // Mundsburger Damm
        Geometry mundsburgerDamm = osmWays.get("Mundsburger Damm");
        assertNotNull(mundsburgerDamm);
        System.out.println("Mundsburger Damm " + mundsburgerDamm.toText());

        // Foostraße
        Geometry fooStrasse = osmWays.get("Foostraße");
        assertNull(fooStrasse);

    }
}
