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
package com.jejking.hh.nord.matcher;

import java.net.MalformedURLException;
import java.net.URL;


import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jejking.hh.nord.drucksachen.DrucksacheNames;
import com.jejking.hh.nord.drucksachen.RawDrucksache;
import com.jejking.hh.nord.gazetteer.GazetteerPropertyNames;
import com.jejking.hh.nord.gazetteer.GazetteerRelationshipTypes;
import com.jejking.hh.nord.matcher.Matches;
import com.jejking.hh.nord.matcher.RawDrucksacheWithLabelledMatches;
import com.jejking.hh.nord.matcher.RawDrucksacheWithLabelledMatchesNeoImporter;

import static org.junit.Assert.*;

/**
 * Tests for {@link RawDrucksacheWithLabelledMatchesNeoImporter}.
 * 
 * @author jejking
 *
 */
public class RawDrucksacheWithLabelledMatchesNeoImporterTest {

    private final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("dd.MM.yyyy");
    
    private GraphDatabaseService graph;
    private RawDrucksacheWithLabelledMatchesNeoImporter importer;
    private RawDrucksacheWithLabelledMatches rawDrucksacheWithLabelledMatches;
    
    @Test
    public void linksDrucksacheToGazetteerEntriesWithDatePresent() {
        
        givenAGraphDatabase();
        givenAGazetteer();
        givenAnImporter();
        givenADrucksacheWithLabelledMatches(new LocalDate(2014, DateTimeConstants.JUNE, 15));
        
        whenTheImporterRuns();
        
        thenTheNodesAndRelationshipsAreCreatedCorrectly();
        
        tearDownGraph();
    }

    private void tearDownGraph() {
        this.graph.shutdown();
    }

    private void givenAGraphDatabase() {
        this.graph =  new TestGraphDatabaseFactory()
        .newImpermanentDatabaseBuilder()
        .newGraphDatabase();
        
    }

    private void thenTheNodesAndRelationshipsAreCreatedCorrectly() {
        try(Transaction tx = this.graph.beginTx()) {
            Node drucksacheNode = drucksacheNodeWasCreated();
//            headerRelationshipsWereCreated(drucksacheNode);
//            bodyRelationshipsWereCreated(drucksacheNode);
            relationshipsWereCreated(drucksacheNode);
            tx.success();
        }
        
    }
    
    private void relationshipsWereCreated(Node drucksacheNode) {
        Iterable<Relationship> relationshipsIterable = drucksacheNode.getRelationships(GazetteerRelationshipTypes.REFERS_TO);
        int relCount = 0;
        
        for (Relationship rel : relationshipsIterable) {
            relCount++;
            if (rel.getEndNode().hasLabel(DynamicLabel.label("foo"))) {
                // has both
                assertTrue(rel.hasProperty(DrucksacheNames.REFS_HEADER));
                assertEquals(33, rel.getProperty(DrucksacheNames.REFS_HEADER));
                assertTrue(rel.hasProperty(DrucksacheNames.REFS_BODY));
                assertEquals(66, rel.getProperty(DrucksacheNames.REFS_BODY));
            }
            if (rel.getEndNode().hasLabel(DynamicLabel.label("bar"))) {
                // only in body
                assertFalse(rel.hasProperty(DrucksacheNames.REFS_HEADER));
                assertTrue(rel.hasProperty(DrucksacheNames.REFS_BODY));
                assertEquals(66, rel.getProperty(DrucksacheNames.REFS_BODY));
            }
        }
        
        assertEquals(3, relCount); // fu, pub and cocktail are the relationships
    }

    
    private Node drucksacheNodeWasCreated() {
        ResourceIterator<Node> iterator = this.graph.findNodesByLabelAndProperty(
                                            DynamicLabel.label(DrucksacheNames.DRUCKSACHE), 
                                            DrucksacheNames.DRUCKSACHE_ID,
                                            this.rawDrucksacheWithLabelledMatches.getOriginal().getDrucksachenId())
                                           .iterator();
        Node drucksacheNode = iterator.next();
        assertNotNull(drucksacheNode);
        
        assertEquals(this.rawDrucksacheWithLabelledMatches.getOriginal().getOriginalUrl().toExternalForm(),
                        drucksacheNode.getProperty(DrucksacheNames.ORIGINAL_URL));
        if (this.rawDrucksacheWithLabelledMatches.getOriginal().getDate().isPresent()) {
            assertEquals(
                    this.dateFormat.print(this.rawDrucksacheWithLabelledMatches.getOriginal().getDate().get()),
                    drucksacheNode.getProperty(DrucksacheNames.DATE));
        } else {
            assertFalse(drucksacheNode.hasProperty(DrucksacheNames.DATE));
        }
        
        assertEquals(this.rawDrucksacheWithLabelledMatches.getOriginal().getExtractedProperties().get("Betreff"),
                drucksacheNode.getProperty(DrucksacheNames.HEADER));
        
        return drucksacheNode;
    }

    private void whenTheImporterRuns() {
        this.importer.call(this.rawDrucksacheWithLabelledMatches);
    }

    private void givenADrucksacheWithLabelledMatches(LocalDate localDate) {
        Optional<LocalDate> optionalLocalDate = Optional.fromNullable(localDate);
        try {
            RawDrucksache original = new RawDrucksache(
                                        "myId", 
                                        new URL("http://foo.com/bar"), 
                                        optionalLocalDate,
                                        ImmutableMap.of("Betreff", "Superspannend"), 
                                        ImmutableList.of("c", "d")); // not relevant at this stage
            
            ImmutableMap.Builder<String, Matches> matchesMapBuilder = ImmutableMap.builder();
            ImmutableMap<String, Integer> emptyMap = ImmutableMap.of();
            // fu of type "foo" referred to in header and body. We want *one* relationship with two properties.
            matchesMapBuilder.put("foo", new Matches(ImmutableMap.of("fu", 66), ImmutableMap.of("fu", 33)));
            // pub, cocktail of type "bar" referred to in body, No header matches
            matchesMapBuilder.put("bar", new Matches(ImmutableMap.of("pub", 66, "cocktail", 66), emptyMap));
            
            
            this.rawDrucksacheWithLabelledMatches = new RawDrucksacheWithLabelledMatches(original,
                                                            matchesMapBuilder.build());
            
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private void givenAnImporter() {
        this.importer = new RawDrucksacheWithLabelledMatchesNeoImporter(this.graph);
    }

    private void givenAGazetteer() {
        // we assemble a totally trivial gazetteer of 4 nodes
        createGazetteerNode("foo", "fu");
        createGazetteerNode("bar", "pub");
        createGazetteerNode("bar", "cocktail");
        createGazetteerNode("foo", "wibble");
    }

    private void createGazetteerNode(String label, String name) {
        try(Transaction tx = this.graph.beginTx()) {
            Node node = this.graph.createNode(DynamicLabel.label(label));
            node.setProperty(GazetteerPropertyNames.NAME, name);
            tx.success();
        }
        
        
    }
}
