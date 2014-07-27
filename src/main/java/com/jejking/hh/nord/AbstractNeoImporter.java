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
package com.jejking.hh.nord;

import static com.jejking.hh.nord.gazetteer.GazetteerEntryTypes.BOROUGH;
import static com.jejking.hh.nord.gazetteer.GazetteerEntryTypes.NAMED_AREA;
import static com.jejking.hh.nord.gazetteer.GazetteerEntryTypes.NUMBERED_DISTRICT;
import static com.jejking.hh.nord.gazetteer.GazetteerEntryTypes.STREET;
import static com.jejking.hh.nord.gazetteer.GazetteerEntryTypes.SCHOOL;
import static com.jejking.hh.nord.gazetteer.GazetteerEntryTypes.HOSPITAL;
import static com.jejking.hh.nord.gazetteer.GazetteerEntryTypes.CINEMA;
import static com.jejking.hh.nord.gazetteer.GazetteerEntryTypes.THEATRE;
import static com.jejking.hh.nord.gazetteer.GazetteerEntryTypes.UNIVERSITY;

import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.helpers.collection.MapUtil;

import com.google.common.collect.ImmutableList;
import com.jejking.hh.nord.gazetteer.GazetteerPropertyNames;

/**
 * Abstract class with some shared functionality.
 * 
 * @author jejking
 * @param <T> type to be imported to neo
 */
public abstract class AbstractNeoImporter<T> {

	 public static final String GAZETTEER_FULLTEXT = "GAZETTEER_FULLTEXT";
	
    /**
     * Writes the data to the graph database.
     * 
     * @param t data to be written
     * @param graph graph to be written to.
     */
    public abstract void writeToNeo(T t, GraphDatabaseService graph);
    
    /**
     * Gets an {@link EditableLayer}. Returns an existing layer of name requested, else
     * creates a new one with that name.
     * 
     * @param spatialDatabaseService
     * @param name
     * @return
     */
    protected EditableLayer getEditableLayer(SpatialDatabaseService spatialDatabaseService, String name) {
        
        EditableLayer editableLayer = (EditableLayer) spatialDatabaseService.getLayer(name);
        if (editableLayer != null) {
            return editableLayer;
        } else {
            editableLayer = (EditableLayer) spatialDatabaseService.createWKBLayer(name);
            editableLayer.setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84);
            return editableLayer;
        }
    }
    
    public static void setupSchema(GraphDatabaseService graph) {
     // we want an additional index on adminstrative area - name
        try (Transaction tx = graph.beginTx()) {
            Schema schema = graph.schema();
            schema
                .constraintFor(DynamicLabel.label(BOROUGH))
                .assertPropertyIsUnique(GazetteerPropertyNames.NAME)
                .create();
            
            schema
                .constraintFor(DynamicLabel.label(NAMED_AREA))
                .assertPropertyIsUnique(GazetteerPropertyNames.NAME)
                .create();
            
            schema
            	.constraintFor(DynamicLabel.label(NUMBERED_DISTRICT))
            	.assertPropertyIsUnique(GazetteerPropertyNames.NUMBER)
            	.create();
            
            
            for (String label : ImmutableList.of(STREET, SCHOOL, HOSPITAL, CINEMA, THEATRE, UNIVERSITY)) {
                schema
                    .indexFor(DynamicLabel.label(label))
                    .on(GazetteerPropertyNames.NAME)
                    .create();
            }
            
            
            IndexManager indexManager = graph.index();
            @SuppressWarnings("unused")
            Index<Node> fullText = indexManager.forNodes(GAZETTEER_FULLTEXT,
                                MapUtil.stringMap(IndexManager.PROVIDER, "lucene",
                                                  "type", "fulltext"));
            
            tx.success();
        }
    }
    
    public static void registerShutdownHook(final GraphDatabaseService graphDb) {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }

}
