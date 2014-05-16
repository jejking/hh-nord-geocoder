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

import info.jejking.osm.OsmNode;
import info.jejking.osm.OsmWay;
import info.jejking.osm.RxOsmParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class to extract street names and geometries from an
 * Open Street Map XML file.
 * 
 * @author jejking
 */
public class RxOsmStreetCollectionBuilder {

	private static final String HIGHWAY = "highway";
	private static final String NAME = "name";
	
	private final Map<Long, Point> osmPoints = new HashMap<>();
	private final Map<String, Geometry> osmNamedStreets = new HashMap<>();
	private final GeometryFactory geometryFactory;
	
	/**
	 * Constructor.
	 * @param geometryFactory a geometry factory, may not be <code>null</code>
	 * @throws NullPointerException if geometry factory <code>null</code>
	 */
	public RxOsmStreetCollectionBuilder(GeometryFactory geometryFactory) {
		this.geometryFactory = checkNotNull(geometryFactory);
	}
	
	/**
	 * Constructs a map of street name to geometry from XML Open Street Map
	 * data flowing from the input stream. 
	 * 
	 * @param inputStream with XML data
	 * @return map of street name to geometry mappings
	 */
	public Map<String, Geometry> streetsFromStream(InputStream inputStream) {
		
		RxOsmParser parser = new RxOsmParser(inputStream);
		attachNodeMapBuilder(parser);
		attachWayBuilder(parser);
		
		parser.parseOsmStream();
		
		return osmNamedStreets;
		
	}

	private void attachWayBuilder(RxOsmParser parser) {
		parser.getWayObservable().filter(new Func1<OsmWay, Boolean>() {

			@Override
			public Boolean call(OsmWay way) {
				return way.getProperties().containsKey(HIGHWAY) && way.getProperties().containsKey(NAME);
			}
		}).subscribe(new Action1<OsmWay>() {

			@Override
			public void call(OsmWay way) {
				String name = way.getProperties().get(NAME);
				// assemble geometry from the points referenced in nd child elements of way
			    LineString wayLineString = buildLineString(way.getNdRefs(), name);
				
				if (osmNamedStreets.containsKey(name)) {
				    // if we already have the name of the street, then perform a union with existing geometry
					osmNamedStreets.put(name, wayLineString.union(osmNamedStreets.get(name)));
				} else {
					osmNamedStreets.put(name, wayLineString);
				}
				
			}
			
			private LineString buildLineString(List<Long> ndList, String name) {
				List<Point> pointList = new ArrayList<>(ndList.size());
				
				// find all the referenced points. Ignore any we can't find, perhaps
				// because they were orphaned as we cut the extract around Nord.
				for (Long osmId : ndList) {
					Point point = osmPoints.get(osmId);
					if (point != null) {
						pointList.add(point);
					}
				}
				
				// create a line string from the nodes....
				Coordinate[] coordinates = new Coordinate[pointList.size()];
				
				for (int i = 0; i < pointList.size(); i++) {
					coordinates[i] = pointList.get(i).getCoordinate();
				}
				if (coordinates.length == 1) {
					System.err.println("Only one node for way: " + name);
				}
				return geometryFactory.createLineString(coordinates);
			}
			
		});
		
	}

	private void attachNodeMapBuilder(RxOsmParser parser) {
		
		Observable<OsmNode> nodeObservable = parser.getNodeObservable();
		nodeObservable.subscribe(new Action1<OsmNode>() {

			@Override
			public void call(OsmNode node) {
				RxOsmStreetCollectionBuilder.this.osmPoints.put(node.getId(), node.getPoint());
			}
		});
		
	}
}
