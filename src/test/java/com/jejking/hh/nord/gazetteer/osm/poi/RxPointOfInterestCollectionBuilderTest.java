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
package com.jejking.hh.nord.gazetteer.osm.poi;

import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.houseNumber;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.inner;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.multipolygon;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.outer;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.type;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.name;
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
import com.jejking.hh.nord.gazetteer.osm.RelationWaysToPolygonTest;
import com.jejking.hh.nord.gazetteer.osm.poi.PointOfInterest;
import com.jejking.hh.nord.gazetteer.osm.poi.RxPointOfInterestCollectionBuilder;
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
 * Tests for {@link RxPointOfInterestCollectionBuilder}.
 * 
 * @author jejking
 *
 */
public class RxPointOfInterestCollectionBuilderTest {

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
    
    private RxPointOfInterestCollectionBuilder builder;
    
    @Before
    public void init() {
        this.builder = new RxPointOfInterestCollectionBuilder(geometryFactory);
    }
    

    @Test
    public void poisBuiltFromNodes() {
        OsmNode retain = buildTestOsmNode(ImmutableMap.of(name, "foo", "amenity", "theatre"));
        OsmNode remove = buildTestOsmNode(ImmutableMap.of("foo", "bar"));
        Observable<OsmNode> nodeObservable = Observable.from(retain, remove);
        
        ImmutableList.Builder<PointOfInterest> poiListBuilder = ImmutableList.builder();
        
        builder.attachNodePointOfInterestBuilderTo(nodeObservable, poiListBuilder);
        
        ImmutableList<PointOfInterest> poiList = poiListBuilder.build();
        
        assertEquals(1, poiList.size());

        PointOfInterest poi = poiList.get(0);
        
        assertFalse(poi.getStreet().isPresent());
        assertTrue(poi.getLabels().contains(GazetteerEntryTypes.THEATRE));
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
                                                                "amenity", "school",
                                                                type, multipolygon),
                                                ImmutableList.of(way1, way2, way3));
        
        
        ImmutableList.Builder<PointOfInterest> poiListBuilder = ImmutableList.builder();
        builder.attachRelationPointOfInterestBuilderTo(Observable.from(relation),
                                                        RelationWaysToPolygonTest.buildOsmLineStrings(),
                                                        poiListBuilder);
        
        PointOfInterest poi = poiListBuilder.build().get(0);
        assertFalse(poi.getStreet().isPresent());
        assertEquals("22", poi.getHouseNumber().get());
        assertTrue(poi.getLabels().contains(GazetteerEntryTypes.SCHOOL));
        
        assertEquals(geometryFactory.createPoint(new Coordinate(4.5, 3.5)), poi.getPoint());
        
    }
    
    @Test
    public void canReadUhlenhorst() {
        
        ImmutableList<PointOfInterest> pois = builder.pointsOfInterestFromStream(RxPointOfInterestCollectionBuilder.class.getResourceAsStream("/uhlenhorst-direct-export.osm"));
        
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
        
        assertEquals(lerchenfeldGymnasium.getPoint().getX(), 10.03019, 0.00001);
        assertEquals(lerchenfeldGymnasium.getPoint().getY(), 53.56902, 0.00001);
    }

}
