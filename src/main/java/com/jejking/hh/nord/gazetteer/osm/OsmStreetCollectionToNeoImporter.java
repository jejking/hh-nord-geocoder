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

import static com.jejking.hh.nord.gazetteer.GazetteerEntryTypes.STREET;
import static com.jejking.hh.nord.gazetteer.GazetteerNames.GAZETTEER_FULLTEXT;
import static com.jejking.hh.nord.gazetteer.GazetteerNames.NAME;
import static com.jejking.hh.nord.gazetteer.GazetteerNames.STREET_LAYER;
import static com.jejking.hh.nord.gazetteer.GazetteerNames.TYPE;


import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

import com.jejking.hh.nord.AbstractNeoImporter;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Imports a collection of name-to-geometry mappings generated
 * from {@link OsmStreetCollectionBuilder} into Neo4j.
 * 
 * 
 * 
 * @author jejking
 *
 */
public class OsmStreetCollectionToNeoImporter extends AbstractNeoImporter<Map<String, Geometry>> {

    public void writeToNeo(Map<String, Geometry> streets, GraphDatabaseService graph) {
        SpatialDatabaseService spatialDatabaseService = new SpatialDatabaseService(graph);
        
        try (Transaction tx = graph.beginTx()) {
            EditableLayer streetLayer = getEditableLayer(spatialDatabaseService, STREET_LAYER);
            Index<Node> fullText = graph.index().forNodes(GAZETTEER_FULLTEXT);
            
            for (Entry<String, Geometry> entry : streets.entrySet()) {
                addStreet(entry.getKey(), entry.getValue(), streetLayer, fullText);
            }
            
            tx.success();
        }
        
    }

    private void addStreet(String name, Geometry geometry, EditableLayer streetLayer, Index<Node> fullText) {
        SpatialDatabaseRecord record = streetLayer.add(geometry, new String[]{NAME}, new Object[]{name});
        Node neoNode = record.getGeomNode();
        neoNode.addLabel(DynamicLabel.label(STREET));
                
        fullText.add(neoNode, NAME, name);
        fullText.add(neoNode, TYPE, STREET);
        
    }
    
}
