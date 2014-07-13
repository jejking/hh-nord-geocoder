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
 *    
 */
package com.jejking.osm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import com.jejking.osm.OsmNode;
import com.jejking.osm.OsmRelation;
import com.jejking.osm.OsmWay;
import com.jejking.osm.RxOsmParser;

/**
 * Tests for {@link OsmParser}.
 * 
 * @author jejking
 *
 */
public class RxOsmParserTest {

    private EventCapturer<OsmNode> nodeCapturer;
    private EventCapturer<OsmWay> wayCapturer;
    private EventCapturer<OsmRelation> relationCapturer;
    
    public void setUpAndParse(String resourceName) {
        RxOsmParser rxOsmParser = new RxOsmParser(RxOsmParser.class.getResourceAsStream(resourceName));
        this.nodeCapturer = new EventCapturer<>();
        this.wayCapturer = new EventCapturer<>();
        this.relationCapturer = new EventCapturer<>();
        
        rxOsmParser.getNodeObservable().subscribe(nodeCapturer);
        rxOsmParser.getWayObservable().subscribe(wayCapturer);
        rxOsmParser.getRelationObservable().subscribe(relationCapturer);
        
        rxOsmParser.parseOsmStream();
    }
    
    @Test
    public void runsThroughWithOsmExtract() {
        
        /*
         * Extract direct from Open Street Map website & API.
         */
    	setUpAndParse("/uhlenhorst-direct-export.osm");
    	
        assertTrue(nodeCapturer.completed);
        assertTrue(wayCapturer.completed);
        assertTrue(relationCapturer.completed);
        
        assertFalse(nodeCapturer.values.isEmpty());
        assertFalse(relationCapturer.values.isEmpty());
        assertFalse(wayCapturer.values.isEmpty());
        
    }
    
    @Test
    public void runsThroughWithOsmExtractNoAuthors() {
        
        /*
         * osmconver32 --drop-authors
         */
    	setUpAndParse("/uhlenhorst-direct-export-no-versions.osm");
        assertTrue(nodeCapturer.completed);
        assertTrue(wayCapturer.completed);
        assertTrue(relationCapturer.completed);
        
        assertFalse(nodeCapturer.values.isEmpty());
        assertFalse(relationCapturer.values.isEmpty());
        assertFalse(wayCapturer.values.isEmpty());
    }
    
    @Test
    public void runsThroughWithOsmExtractNoVersion() {
        /*
         * osmfilter --drop-version
         */
    	setUpAndParse("/uhlenhorst-direct-export-no-authors.osm");
        assertTrue(nodeCapturer.completed);
        assertTrue(wayCapturer.completed);
        assertTrue(relationCapturer.completed);
        
        assertFalse(nodeCapturer.values.isEmpty());
        assertFalse(relationCapturer.values.isEmpty());
        assertFalse(wayCapturer.values.isEmpty());
    }
    
    @Test
    public void runsThroughWithOsmExtractNoNodes() {
        /*
         * osmconvert32 --drop-nodes
         */
        setUpAndParse("/uhlenhorst-direct-export-no-nodes.osm");
        assertTrue(nodeCapturer.completed);
        assertTrue(wayCapturer.completed);
        assertTrue(relationCapturer.completed);
        
        assertTrue(nodeCapturer.values.isEmpty());
        assertFalse(relationCapturer.values.isEmpty());
        assertFalse(wayCapturer.values.isEmpty());
    }
    
    @Test
    public void runsThroughWithOsmExtractNoWays() {

        /*
         * osmconvert32 --drop-ways
         */
    	setUpAndParse("/uhlenhorst-direct-export-no-ways.osm");
        assertTrue(nodeCapturer.completed);
        assertTrue(wayCapturer.completed);
        assertTrue(relationCapturer.completed);
        
        assertFalse(nodeCapturer.values.isEmpty());
        assertTrue(wayCapturer.values.isEmpty());
        assertFalse(relationCapturer.values.isEmpty());
        
    }
    
    @Test
    public void runsThroughWithOsmExtractNoRelations() {
        /*
         * osmconvert32 --drop-relations
         */
    	setUpAndParse("/uhlenhorst-direct-export-no-relations.osm");
        assertTrue(nodeCapturer.completed);
        assertTrue(wayCapturer.completed);
        assertTrue(relationCapturer.completed);
        
        assertFalse(nodeCapturer.values.isEmpty());
        assertFalse(wayCapturer.values.isEmpty());
        assertTrue(relationCapturer.values.isEmpty());
    }
    
    @Test
    public void runsThroughWithOsmExtractAllToNodes() {
        /*
         * osmconvert32 --all-to-nodes
         */
    	setUpAndParse("/uhlenhorst-direct-export-all-to-nodes.osm");
        assertTrue(nodeCapturer.completed);
        assertTrue(wayCapturer.completed);
        assertTrue(relationCapturer.completed);
        
        assertFalse(nodeCapturer.values.isEmpty());
        assertTrue(relationCapturer.values.isEmpty());
        assertTrue(wayCapturer.values.isEmpty());
    }
    
    @Test
    public void readsTest1Correctly() {
    	setUpAndParse("/test1.osm");
        assertTrue(nodeCapturer.completed);
        assertTrue(wayCapturer.completed);
        assertTrue(relationCapturer.completed);
        
        assertFalse(nodeCapturer.values.isEmpty());
        assertFalse(relationCapturer.values.isEmpty());
        assertFalse(wayCapturer.values.isEmpty());
        
        checkNode1();
        
        checkNode2();
        
        checkWay3();
        
        checkRelation4();
    }
    
    @Test
    public void readsTest1AddedElementsCorrectly() {
    	setUpAndParse("/test1-addedElements.osm");
        assertFalse(nodeCapturer.completed);
        assertTrue(wayCapturer.completed);
        assertTrue(relationCapturer.completed);
        
        // this cannot be read due to use of characters, etc...
        assertNotNull(nodeCapturer.e);
    }
    
    @Test
    public void readsTest1AddedAttributesCorrectly() {
    	setUpAndParse("/test1-addedAttributes.osm");
        assertTrue(nodeCapturer.completed);
        assertTrue(wayCapturer.completed);
        assertTrue(relationCapturer.completed);
        
        assertFalse(nodeCapturer.values.isEmpty());
        assertFalse(relationCapturer.values.isEmpty());
        assertFalse(wayCapturer.values.isEmpty());
        
        checkNode1();
        
        checkNode2();
        
        checkWay3();
        
        checkRelation4();
    }

    @Test
    public void registersErrorForMissingNodeId() {
    	setUpAndParse("/test1-missing-node-id.osm");
        
        assertNotNull(nodeCapturer.e);
    }
    
    @Test
    public void registersErrorForMissingLat() {
    	setUpAndParse("/test1-missing-node-lat.osm");
        assertNotNull(nodeCapturer.e);

    }
    
    @Test
    public void registersErrorForMissingLon() {
    	setUpAndParse("/test1-missing-node-lon.osm");
        assertNotNull(nodeCapturer.e);
    }
    
    
    private void checkRelation4() {
        //        <relation id="4">
        //            <member type="node" ref="1"/>
        //            <member type="way" ref="3" role=""/>
        //            <member type="relation" ref="666" role="dark-satanic"/>
        //            <tag k="route" v="secret"/>
        //        </relation>
                OsmRelation relation = relationCapturer.values.get(0);
                assertEquals(Long.valueOf(4), relation.getId());
                assertFalse(relation.getVersion().isPresent());
                assertFalse(relation.getChangeset().isPresent());
                assertFalse(relation.getTimestamp().isPresent());
                assertFalse(relation.getUid().isPresent());
                assertFalse(relation.getUser().isPresent());
                assertEquals(1, relation.getProperties().size());
                assertEquals("secret", relation.getProperties().get("route"));
                
                OsmRelation.Member member1 = relation.getMembers().get(0);
                assertEquals(OsmRelation.Member.MemberType.NODE, member1.getType());
                assertEquals(Long.valueOf(1), member1.getRef());
                assertFalse(member1.getRole().isPresent());
                
                OsmRelation.Member member2 = relation.getMembers().get(1);
                assertEquals(OsmRelation.Member.MemberType.WAY, member2.getType());
                assertEquals(Long.valueOf(3), member2.getRef());
                assertFalse(member2.getRole().isPresent());
                
                OsmRelation.Member member3 = relation.getMembers().get(2);
                assertEquals(OsmRelation.Member.MemberType.RELATION, member3.getType());
                assertEquals(Long.valueOf(666), member3.getRef());
                assertEquals("dark-satanic", member3.getRole().get());
    }

    private void checkWay3() {
        OsmWay way = wayCapturer.values.get(0);
        
//        <way id="3" version="1" timestamp="2013-05-14T14:12:39Z" changeset="321" uid="21" user="bar">
//            <nd ref="1" />
//            <nd ref="2" />
//            <tag k="highway" v="motorway"/>
//        </way>
        assertEquals(Long.valueOf(3), way.getId());
        assertEquals(Long.valueOf(1), way.getVersion().get());
        assertEquals( new DateTime(2013, DateTimeConstants.MAY, 14, 14, 12, 39, DateTimeZone.UTC), way.getTimestamp().get());
        assertEquals(Long.valueOf(321), way.getChangeset().get());
        assertEquals(2, way.getNdRefs().size());
        assertTrue(way.getNdRefs().contains(Long.valueOf(1)));
        assertTrue(way.getNdRefs().contains(Long.valueOf(2)));
        assertEquals(1, way.getProperties().size());
        assertEquals("motorway", way.getProperties().get("highway"));
    }

    private void checkNode2() {
        OsmNode secondNode = nodeCapturer.values.get(1);
//        <node id="2" lat="54.23456" lon="11.5432">
//            <tag k="foo" v="bar"/>
//            <tag k="wibble" v="wobble"/>
//        </node>
        assertEquals("bar", secondNode.getProperties().get("foo"));
        assertEquals("wobble", secondNode.getProperties().get("wibble"));
        assertEquals(2, secondNode.getProperties().size());
    }

    private void checkNode1() {
        OsmNode firstNode = nodeCapturer.values.get(0);
        //<node id="1" lat="53.12345" lon="10.2345" version="1" timestamp="2014-05-14T14:12:39Z" changeset="1" uid="1" user="foo"/>
        assertEquals(Long.valueOf(1), firstNode.getId());
        assertEquals(10.2345d, firstNode.getPoint().getX(), 0.0001);
        assertEquals(53.12345d, firstNode.getPoint().getY(), 0.0001);
        assertEquals(Long.valueOf(1), firstNode.getVersion().get());
        assertEquals(Long.valueOf(1), firstNode.getChangeset().get());
        assertEquals(Long.valueOf(1), firstNode.getUid().get());
        assertEquals("foo", firstNode.getUser().get());
        DateTime oneTimestamp = new DateTime(2014, DateTimeConstants.MAY, 14, 14, 12, 39, DateTimeZone.UTC);
        assertEquals(oneTimestamp, firstNode.getTimestamp().get());
        assertTrue(firstNode.getProperties().isEmpty());
    }
}
