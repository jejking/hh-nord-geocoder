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
package info.jejking.hamburg.nord.geocoder.osm;

import info.jejking.hamburg.nord.geocoder.osm.RelationWaysToPolygon;
import info.jejking.osm.OsmMetadataHolder;
import info.jejking.osm.OsmRelation;
import info.jejking.osm.OsmRelation.Member;
import info.jejking.osm.OsmRelation.Member.MemberType;

import java.util.HashMap;
import java.util.Map;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.inner;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.multipolygon;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.outer;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.type;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link RelationWaysToPolygon}.
 * 
 * @author jejking
 *
 */
public class RelationWaysToPolygonTest {

    private static GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
    private Map<Long, LineString> osmLineStrings;
    
    private RelationWaysToPolygon relationWaysToPolygon;
    
    private static final OsmMetadataHolder DUMMY_METADATA = new OsmMetadataHolder(
            1L, 
            Optional.of(1L), 
            Optional.of(new DateTime()), 
            Optional.of(1L), 
            Optional.of(1L), 
            Optional.of("foo"));
    
    public RelationWaysToPolygonTest() {
        this.osmLineStrings = buildOsmLineStrings();
        
        this.relationWaysToPolygon = new RelationWaysToPolygon(geometryFactory, osmLineStrings);
    }
    
    static Map<Long, LineString> buildOsmLineStrings() {
        Map<Long, LineString> lineStrings = new HashMap<>();
        
        lineStrings.put(1L, geometryFactory.createLineString(coordinates1()));
        lineStrings.put(2L, geometryFactory.createLineString(coordinates2()));
        lineStrings.put(3L, geometryFactory.createLineString(coordinates3()));
        
        return lineStrings;
    }

    @Test
    public void doesItWorkOk() {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put(type, multipolygon);
        
        OsmRelation.Member way1 = new Member(MemberType.WAY, 1L, Optional.of(outer));
        OsmRelation.Member way2 = new Member(MemberType.WAY, 2L, Optional.of(inner));
        OsmRelation.Member way3 = new Member(MemberType.WAY, 3L, Optional.of(inner));
        
        OsmRelation relation = new OsmRelation(DUMMY_METADATA, builder.build(), ImmutableList.of(way1, way2, way3));
        
        Optional<Polygon> poly = this.relationWaysToPolygon.call(relation);
        assertEquals("POLYGON ((1 1, 8 1, 8 6, 1 6, 1 1), (6 2, 7 2, 7 3, 6 3, 6 2), (2 4, 3 4, 3 5, 2 5, 2 4))",
                        poly.get().toText());
    }

    static Coordinate[] coordinates3() {
        Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(2, 4);
        coordinates[1] = new Coordinate(3, 4);
        coordinates[2] = new Coordinate(3, 5);
        coordinates[3] = new Coordinate(2, 5);
        coordinates[4] = new Coordinate(2, 4);
        return coordinates;
    }

    static  Coordinate[] coordinates2() {
        Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(6, 2);
        coordinates[1] = new Coordinate(7, 2);
        coordinates[2] = new Coordinate(7, 3);
        coordinates[3] = new Coordinate(6, 3);
        coordinates[4] = new Coordinate(6, 2);
        return coordinates;
    }

    static  Coordinate[] coordinates1() {
        Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(1, 1);
        coordinates[1] = new Coordinate(8, 1);
        coordinates[2] = new Coordinate(8, 6);
        coordinates[3] = new Coordinate(1, 6);
        coordinates[4] = new Coordinate(1, 1);
        return coordinates;
    }

}
