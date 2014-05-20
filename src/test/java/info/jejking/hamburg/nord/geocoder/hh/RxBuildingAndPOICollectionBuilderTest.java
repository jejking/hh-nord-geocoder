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

import static info.jejking.hamburg.nord.geocoder.hh.OsmConstants.houseNumber;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import info.jejking.osm.OsmMetadataHolder;
import info.jejking.osm.OsmNode;
import info.jejking.osm.OsmWay;

import java.util.Map;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import rx.Observable;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
    

}
