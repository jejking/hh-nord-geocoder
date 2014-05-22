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

import info.jejking.osm.OsmRelation;

import java.util.Map;

import rx.functions.Func1;

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Function to map a relation that is a multipolygon to an optional polgyon. The 
 * function returns an actual result if the polygon can be mapped simply (one outer 
 * ring and 1-N inner rings), otherwise the polygon is absent.
 * 
 * @author jejking
 *
 */
class RelationWaysToPolygon implements Func1<OsmRelation, Optional<Polygon>> {

	private final GeometryFactory geometryFactory;
    private final Map<Long, LineString> osmLineStrings;
    
    
	/**
	 * Constructor.
	 * 
	 * @param geometryFactory
	 * @param knownOsmWays
	 */
	public RelationWaysToPolygon(GeometryFactory geometryFactory, Map<Long, LineString> osmLineStrings) {
		super();
		this.geometryFactory = geometryFactory;
		this.osmLineStrings = osmLineStrings;
	}

	@Override
	public Optional<Polygon> call(OsmRelation osmRelation) {
		
		try {
			// get outer....
			
			
			// get any inner....
			return Optional.absent();
			
		} catch (Exception e) {
			e.printStackTrace();
			return Optional.absent();
		}
		
		
		
	}

}
