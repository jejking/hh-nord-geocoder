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

import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.houseNumber;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.inner;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.multipolygon;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.outer;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.type;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import rx.Observable;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.jejking.hh.nord.gazetteer.GazetteerEntryTypes;
import com.jejking.hh.nord.gazetteer.osm.PointOfInterest;
import com.jejking.hh.nord.gazetteer.osm.RxBuildingAndPOICollectionBuilder;
import com.jejking.osm.OsmMetadataHolder;
import com.jejking.osm.OsmNode;
import com.jejking.osm.OsmRelation;
import com.jejking.osm.OsmWay;
import com.jejking.osm.OsmRelation.Member;
import com.jejking.osm.OsmRelation.Member.MemberType;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Tests for {@link RxBuildingAndPOICollectionBuilder}.
 * 
 * @author jejking
 *
 */
public class RxBuildingAndPOICollectionBuilderTest {

    private static final OsmMetadataHolder DUMMY_METADATA = new OsmMetadataHolder(
            1L, 
            Optional.of(1L), 
            Optional.of(new DateTime()), 
            Optional.of(1L), 
            Optional.of(1L), 
            Optional.of("foo"));
    
    
    private final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
    
    private final Point dummyPoint = geometryFactory.createPoint(new Coordinate(1, 2));

    
    
    private OsmNode buildTestOsmNode(ImmutableMap<String, String> props) {
        return new OsmNode(DUMMY_METADATA, props, dummyPoint);
    }
    
    private RxBuildingAndPOICollectionBuilder builder;
    
    @Before
    public void init() {
        this.builder = new RxBuildingAndPOICollectionBuilder(geometryFactory);
    }
    

    @Test
    public void poisBuiltFromNodes() {
        OsmNode retain = buildTestOsmNode(ImmutableMap.of(houseNumber, "22", "building", "yes"));
        OsmNode remove = buildTestOsmNode(ImmutableMap.of("foo", "bar"));
        Observable<OsmNode> nodeObservable = Observable.from(retain, remove);
        
        ImmutableList.Builder<PointOfInterest> poiListBuilder = ImmutableList.builder();
        
        builder.attachNodePointOfInterestBuilderTo(nodeObservable, poiListBuilder);
        
        ImmutableList<PointOfInterest> poiList = poiListBuilder.build();
        
        assertEquals(1, poiList.size());

        PointOfInterest poi = poiList.get(0);
        
        assertFalse(poi.getStreet().isPresent());
        assertEquals("22", poi.getHouseNumber().get());
        assertTrue(poi.getLabels().contains(GazetteerEntryTypes.BUILDING));
    }

    @Test
    public void poisBuiltFromWays() {
        ImmutableMap<String, String> empty = ImmutableMap.of(); 
        
        // constructs a square from the origin of size 2
        OsmNode bottomLeft = new OsmNode(DUMMY_METADATA, empty, geometryFactory.createPoint(new Coordinate(0, 0)));
        OsmNode bottomRight = new OsmNode(DUMMY_METADATA, empty, geometryFactory.createPoint(new Coordinate(2, 0)));
        OsmNode topRight = new OsmNode(DUMMY_METADATA, empty, geometryFactory.createPoint(new Coordinate(2, 2)));
        OsmNode topLeft = new OsmNode(DUMMY_METADATA, empty, geometryFactory.createPoint(new Coordinate(0, 2)));
        
        OsmWay way = new OsmWay(DUMMY_METADATA, 
                                ImmutableMap.of(houseNumber, "22", "building", "yes"), 
                                ImmutableList.of(1L, 2L, 3L, 4L, 1L));
        
        Map<Long, Point> osmPoints = ImmutableMap.of(1L, bottomLeft.getPoint(), 
                                                     2L, bottomRight.getPoint(),
                                                     3l, topRight.getPoint(), 
                                                     4L, topLeft.getPoint());
        
        ImmutableList.Builder<PointOfInterest> poiListBuilder = ImmutableList.builder();
        builder.attachWayPointOfInterestBuilderTo(Observable.from(way), osmPoints, poiListBuilder);
        
        PointOfInterest poi = poiListBuilder.build().get(0);
        assertFalse(poi.getStreet().isPresent());
        assertEquals("22", poi.getHouseNumber().get());
        assertTrue(poi.getLabels().contains(GazetteerEntryTypes.BUILDING));
        
        // centroid is 1,1 : intersection of diagonals.
        assertEquals(geometryFactory.createPoint(new Coordinate(1, 1)), poi.getPoint());
    }
    
    
    
    @Test
    public void poisBuiltFromRelations() {
        
        OsmRelation.Member way1 = new Member(MemberType.WAY, 1L, Optional.of(outer));
        OsmRelation.Member way2 = new Member(MemberType.WAY, 2L, Optional.of(inner));
        OsmRelation.Member way3 = new Member(MemberType.WAY, 3L, Optional.of(inner));
        
        OsmRelation relation = new OsmRelation(DUMMY_METADATA, 
                                                ImmutableMap.of(houseNumber, "22",
                                                                "building", "yes",
                                                                type, multipolygon),
                                                ImmutableList.of(way1, way2, way3));
        
        
        ImmutableList.Builder<PointOfInterest> poiListBuilder = ImmutableList.builder();
        builder.attachRelationPointOfInterestBuilderTo(Observable.from(relation),
                                                        RelationWaysToPolygonTest.buildOsmLineStrings(),
                                                        poiListBuilder);
        
        PointOfInterest poi = poiListBuilder.build().get(0);
        assertFalse(poi.getStreet().isPresent());
        assertEquals("22", poi.getHouseNumber().get());
        assertTrue(poi.getLabels().contains(GazetteerEntryTypes.BUILDING));
        
        assertEquals(geometryFactory.createPoint(new Coordinate(4.5, 3.5)), poi.getPoint());
        
    }
    
    @Test
    public void canReadUhlenhorst() {
        
        ImmutableList<PointOfInterest> pois = builder.pointsOfInterestFromStream(RxBuildingAndPOICollectionBuilder.class.getResourceAsStream("/uhlenhorst-direct-export.osm"));
        
        // test for some arbitrary points of interest....
        
        // a node, Mundsburger Damm 12, lat="53.5646073" lon="10.0187863"
        PointOfInterest md12 = Iterables.find(pois, new Predicate<PointOfInterest>() {
            @Override
            public boolean apply(PointOfInterest input) {
                if (input.getHouseNumber().isPresent() && input.getHouseNumber().get().equals("12")) {
                    if (input.getStreet().isPresent() && input.getStreet().get().equals("Mundsburger Damm")) {
                        return true;
                    }
                }
                return false;
            }
        });
        assertEquals(md12.getPoint().getCoordinate().x, 10.01878, 0.001);
        assertEquals(md12.getPoint().getCoordinate().y, 53.5646, 0.001);
        
        // a way, uhlenhorster weg 4, ca 53.568621, 10.017030 according to Google Maps
        PointOfInterest uw4 = Iterables.find(pois, new Predicate<PointOfInterest>() {
            @Override
            public boolean apply(PointOfInterest input) {
                if (input.getHouseNumber().isPresent() && input.getHouseNumber().get().equals("4")) {
                    if (input.getStreet().isPresent() && input.getStreet().get().equals("Uhlenhorster Weg")) {
                        return true;
                    }
                }
                return false;
            }
        });
        assertEquals(uw4.getPoint().getCoordinate().x, 10.01703, 0.001);
        assertEquals(uw4.getPoint().getCoordinate().y, 53.5686, 0.001);
        
        
        PointOfInterest lerchenfeldGymnasium = Iterables.find(pois, new Predicate<PointOfInterest>() {
            @Override
            public boolean apply(PointOfInterest input) {
                if (input.getName().isPresent() && input.getName().get().equals("Gymnasium Lerchenfeld")) {
                    return true;
                }
                return false;
            }
        });
        assertTrue(lerchenfeldGymnasium.getLabels().contains(GazetteerEntryTypes.SCHOOL));        
        assertEquals("Lerchenfeld", lerchenfeldGymnasium.getStreet().get());
        assertEquals("10", lerchenfeldGymnasium.getHouseNumber().get());
    }

}
