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
package com.jejking.hh.nord.gazetteer.opendata;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.base.Optional;
import com.jejking.hh.nord.gazetteer.opendata.AdminAreaTreeNodeTransformer.ConvertPolygonToWGS84;
import com.jejking.hh.nord.gazetteer.opendata.AdminAreaTreeNodeTransformer.CreatePolygon;
import com.jejking.hh.nord.gazetteer.opendata.AdminAreaTreeNodeTransformer.CreateWkt;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Tests for {@link AdminAreaTreeNodeTransformer}.
 * 
 * @author jejking
 * 
 */
public class AdminAreaTreeNodeTransformerTest {

    @Test
    public void wktConversionWorks() {
        String in = "0 0 1 1 3 3.5";
        String expected = "POLYGON((0 0, 1 1, 3 3.5))";

        CreateWkt f = new AdminAreaTreeNodeTransformer().new CreateWkt();
        assertEquals(expected, f.call(in));
    }

    @Test
    public void createPolygonFromWkt() {
        // these results obtained from the helpful website at http://cs2cs.mygeodata.eu/
        double[][] expected = new double[][] { { 4.51125611529d, 0.0d }, { 4.51126507418d, 0.000405871916451d },
                { 4.5112829855d, 0.00315678165038d }, { 4.51125611529d, 0.0d } };

        AdminAreaTreeNodeTransformer transformer = new AdminAreaTreeNodeTransformer();
        CreatePolygon createPolygon = transformer.new CreatePolygon();
        ConvertPolygonToWGS84 convertPolygon = transformer.new ConvertPolygonToWGS84();

        Optional<Polygon> output = convertPolygon.call(createPolygon.call("POLYGON((0 0, 1 45, 3 350, 0 0))"));
        Coordinate[] coords = output.get().getCoordinates();

        for (int i = 0; i < coords.length; i++) {
            Coordinate coord = coords[i];
            assertEquals(expected[i][0], coord.x, 0.0000000001);
            assertEquals(expected[i][1], coord.y, 0.0000000001);
        }

    }

    @Test
    public void fixRootCreatesHamburgPolygon() {
        HamburgRawTreeBuilder builder = new HamburgRawTreeBuilder();
        AdminAreaTreeNode<String> rawHamburg = builder.buildRawTree();

        AdminAreaTreeNodeTransformer transformer = new AdminAreaTreeNodeTransformer();
        transformer.call(rawHamburg); // does it work....
    }

}
