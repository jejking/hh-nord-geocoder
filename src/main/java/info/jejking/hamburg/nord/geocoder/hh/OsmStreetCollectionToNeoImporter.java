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

import static info.jejking.hamburg.nord.geocoder.hh.GazetteerEntryTypes.STREET;
import static info.jejking.hamburg.nord.geocoder.hh.GazetteerNames.ADMINISTRATIVE_LAYER;
import static info.jejking.hamburg.nord.geocoder.hh.GazetteerNames.GAZETTEER_FULLTEXT;
import static info.jejking.hamburg.nord.geocoder.hh.GazetteerNames.NAME;
import static info.jejking.hamburg.nord.geocoder.hh.GazetteerNames.STREET_LAYER;
import static info.jejking.hamburg.nord.geocoder.hh.GazetteerNames.TYPE;

import java.util.Map;
import java.util.Map.Entry;

import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

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
public class OsmStreetCollectionToNeoImporter {

    public void writeToNeo(Map<String, Geometry> streets, GraphDatabaseService graph) {
        SpatialDatabaseService spatialDatabaseService = new SpatialDatabaseService(graph);
        
        try (Transaction tx = graph.beginTx()) {
            EditableLayer adminLayer = getEditableLayer(spatialDatabaseService, ADMINISTRATIVE_LAYER);
            EditableLayer streetLayer = getEditableLayer(spatialDatabaseService, STREET_LAYER);
            Index<Node> fullText = graph.index().forNodes(GAZETTEER_FULLTEXT);
            
            for (Entry<String, Geometry> entry : streets.entrySet()) {
                addStreet(entry.getKey(), entry.getValue(), streetLayer, adminLayer, fullText);
            }
            
            tx.success();
        }
        
    }

    private void addStreet(String name, Geometry geometry, EditableLayer streetLayer, EditableLayer adminLayer, Index<Node> fullText) {
        SpatialDatabaseRecord record = streetLayer.add(geometry, new String[]{NAME}, new Object[]{name});
        Node neoNode = record.getGeomNode();
        neoNode.addLabel(DynamicLabel.label(STREET));
                
        fullText.add(neoNode, NAME, name);
        fullText.add(neoNode, TYPE, STREET);
        
    }

    private EditableLayer getEditableLayer(SpatialDatabaseService spatialDatabaseService, String name) {
        
        EditableLayer editableLayer = (EditableLayer) spatialDatabaseService.getLayer(name);
        if (editableLayer != null) {
            return editableLayer;
        } else {
            editableLayer = (EditableLayer) spatialDatabaseService.createWKBLayer(name);
            editableLayer.setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84);
            return editableLayer;
        }
        
        
    }
    
}
