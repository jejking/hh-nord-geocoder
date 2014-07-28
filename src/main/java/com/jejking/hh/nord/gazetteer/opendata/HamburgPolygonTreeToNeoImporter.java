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


import static com.jejking.hh.nord.gazetteer.GazetteerEntryTypes.NUMBERED_DISTRICT;
import static com.jejking.hh.nord.gazetteer.GazetteerLayerNames.GEO;
import static com.jejking.hh.nord.gazetteer.GazetteerPropertyNames.NAME;
import static com.jejking.hh.nord.gazetteer.GazetteerPropertyNames.NUMBER;
import static com.jejking.hh.nord.gazetteer.GazetteerPropertyNames.TYPE;
import static com.jejking.hh.nord.gazetteer.GazetteerRelationshipTypes.CONTAINS;

import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

import com.jejking.hh.nord.AbstractNeoImporter;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Class to insert hierarchy of {@link GazetteerEntry} instances
 * into a Neo4j {@link GraphDatabaseService}. 
 * @author jejking
 *
 */
public class HamburgPolygonTreeToNeoImporter extends AbstractNeoImporter<AdminAreaTreeNode<Polygon>> {

    /**
     * Writes the root node to the graph database, along with all children,
     * constructing the correct node types and relationships.
     * 
     * @param root
     * @param graph
     */
    public void writeToNeo(AdminAreaTreeNode<Polygon> root, GraphDatabaseService graph) {
        SpatialDatabaseService spatialDatabaseService = new SpatialDatabaseService(graph);
        
        try (Transaction tx = graph.beginTx()) {
            EditableLayer administrative = getEditableLayer(spatialDatabaseService, GEO);
            Index<Node> fullText = graph.index().forNodes(GAZETTEER_FULLTEXT);
            addAdministrativeNode(administrative, fullText, null, root);
            tx.success();
        }
        
        
    }
    
    private Node addAdministrativeNode(EditableLayer layer, Index<Node> fullText, Node neoParent, AdminAreaTreeNode<Polygon> child) {
        
        // create the child node
        Node neoChildNode = addNamedNodeToLayer(layer, fullText, child);
        
        // if parent not null, add hierarchy relationships to the node
        if (neoParent != null) {
            createRelationships(neoParent, neoChildNode);
        }
        
        // recurse down the child's children....
        for (AdminAreaTreeNode<Polygon> childNode : child.getChildren().values()) {
            addAdministrativeNode(layer, fullText, neoChildNode, childNode);
        }
        
        return neoChildNode;
    }
    
    private void createRelationships(Node neoParent, Node neoChildNode) {
        neoParent.createRelationshipTo(neoChildNode, CONTAINS);
    }

    private Node addNamedNodeToLayer(EditableLayer layer, Index<Node> fullText, AdminAreaTreeNode<Polygon> node) {
    	
    	String key = node.getType().equals(NUMBERED_DISTRICT) ? NUMBER : NAME;
    	
        SpatialDatabaseRecord record = layer.add(node.getContent(), new String[]{key}, new Object[]{node.getName()});
        Node neoNode = record.getGeomNode();
        neoNode.addLabel(DynamicLabel.label(node.getType()));
       
        fullText.add(neoNode, key, node.getName());
        fullText.add(neoNode, TYPE, node.getType());
        
        return neoNode;
    }
    
}
