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
package com.jejking.hh.nord.gazetteer.osm.streets;

import static com.jejking.hh.nord.gazetteer.GazetteerEntryTypes.NUMBERED_DISTRICT;
import static com.jejking.hh.nord.gazetteer.GazetteerEntryTypes.STREET;
import static com.jejking.hh.nord.gazetteer.GazetteerLayerNames.GEO;
import static com.jejking.hh.nord.gazetteer.GazetteerRelationshipTypes.CONTAINS;

import java.util.List;

import org.neo4j.gis.spatial.GeometryEncoder;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.WKBGeometryEncoder;
import org.neo4j.gis.spatial.pipes.AbstractFilterGeoPipe;
import org.neo4j.gis.spatial.pipes.GeoPipeFlow;
import org.neo4j.gis.spatial.pipes.GeoPipeline;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Class to associate streets with administrative polygons of 
 * the lowest level. Higher-level polygon relationships are transitive
 * and do not need to be declared explicitly.

 * @author jejking
 *
 */
public class StreetToAdminPolygonMapper {

    /**
     * Does the work. Assumes that the graph database service has been
     * preloaded with adminstrative polygons and street geometries.
     * 
     * @param graph
     */
    public void mapStreetsToPolygons(GraphDatabaseService graph) {
        
        try (Transaction tx = graph.beginTx()) {
            SpatialDatabaseService spatial = new SpatialDatabaseService(graph);
            Layer adminLayer = spatial.getLayer(GEO);
            
            GeometryEncoder encoder = new WKBGeometryEncoder();
            encoder.init(adminLayer);
            
            GlobalGraphOperations ops = GlobalGraphOperations.at(graph);
            
            for (Node streetNode : ops.getAllNodesWithLabel(DynamicLabel.label(STREET))) {
            
                Geometry geometry = encoder.decodeGeometry(streetNode);
                
                List<Node> numberedDistrictNodes = GeoPipeline
                        .startIntersectSearch(adminLayer, geometry)
                        .addPipe(new FilterTopLevelPolygons())
                        .toNodeList();
                
                for (Node numberedDistrict : numberedDistrictNodes) {
                    
                    numberedDistrict.createRelationshipTo(streetNode, CONTAINS);
                }
                
            }
            
            tx.success();

        }
        
        
    }
    
    static class FilterTopLevelPolygons extends AbstractFilterGeoPipe {
        
        @Override
        protected boolean validate(GeoPipeFlow flow) {
            
            if (flow.getRecord().getGeomNode().hasLabel(DynamicLabel.label(NUMBERED_DISTRICT))) {
                return true;
            }
            return false;
            
            
        }
    }

}
