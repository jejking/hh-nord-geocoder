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
package info.jejking.hamburg.nord.geocoder;

import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Abstract class with some shared functionality.
 * 
 * @author jejking
 * @param <T> type to be imported to neo
 */
public abstract class AbstractNeoImporter<T> {

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

}
