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

import static com.google.common.base.Preconditions.checkNotNull;
import info.jejking.osm.OsmNode;
import info.jejking.osm.RxOsmParser;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Class to assemble useful descriptions of buildings and other points
 * of interest from an Open Street Map file for further processing.
 * 
 * @author jejking
 *
 */
public class RxBuildingAndPOICollectionBuilder {
	
	private static final String publicTransport = "public_transport";
	private static final String station = "station";
	private static final String stopPosition = "stop_position";
	private static final String railway = "railway";
	private static final String amenity = "amenity";
	private static final String placeOfWorship = "place_of_worship";
	private static final String school = "school";
	private static final String university = "university";
	private static final String police = "police";
	private static final String firestation = "fire_station";
	private static final String theatre = "theatre";
	private static final String cinema = "cinema";
	private static final String library = "library";
	private static final String hospital = "hospital";
	private static final String publicBuilding = "public_building";
	
	private static final String emergency = "emergency";
	private static final String leisure = "leisure";
	private static final String park = "park"; // (but only include if name:?) is set.

	private static final String natural= "natural";
	private static final String water = "water";
	private static final String waterway = "waterway";
	private static final String canal = "canal"; // only if named
	private static final String river = "river"; // only if named

	private static final String houseNumber = "addr:housenumber";
	private static final String street = "addr:street";
	private static final String name = "name";
	
	private static final ImmutableMap<String, ImmutableSet<String>> interestingTags;
	
	private static final ImmutableMap<String, ImmutableSet<String>> interestingTagsWithNameOnly;
	
	static {
		ImmutableSet<String> emptyStringSet = ImmutableSet.of(); // signifies all values acceptable
		ImmutableMap.Builder<String, ImmutableSet<String>> interestingTagsBuilder = ImmutableMap.builder();
		interestingTagsBuilder
			.put(publicTransport, ImmutableSet.of(station, stopPosition))
			.put(railway, ImmutableSet.of(station))
			.put(amenity, ImmutableSet.of(placeOfWorship, school, university, police, firestation, theatre, cinema, library, hospital, publicBuilding))
			.put(emergency, emptyStringSet)
			.put(houseNumber, emptyStringSet);
		interestingTags = interestingTagsBuilder.build();
		
		ImmutableMap.Builder<String, ImmutableSet<String>> interestingTagsWithNameOnlyBuilder = ImmutableMap.builder();
		interestingTagsWithNameOnlyBuilder
			.put(leisure, ImmutableSet.of(park))
			.put(natural, ImmutableSet.of(water))
			.put(waterway, ImmutableSet.of(canal, river));
		interestingTagsWithNameOnly = interestingTagsWithNameOnlyBuilder.build();
		
	}

	private final GeometryFactory geometryFactory;
	private final Map<Long, Point> osmPoints = new HashMap<>();
	private ImmutableList.Builder<PointOfInterest> pointOfInterestListBuilder;
	
	/**
	 * Constructor.
	 * @param geometryFactory a geometry factory, may not be <code>null</code>
	 * @throws NullPointerException if geometry factory <code>null</code>
	 */
	public RxBuildingAndPOICollectionBuilder(GeometryFactory geometryFactory) {
		this.geometryFactory = checkNotNull(geometryFactory);
	}
	
	/**
	 * Constructs a list of points of interest (including buildings) from
	 * an input stream containing Open Street Map data. 
	 * 
	 * @param inputStream with XML data
	 * @return map of street name to geometry mappings
	 */
	public List<PointOfInterest> pointsOfInterestFromStream(final InputStream inputStream) {
		this.pointOfInterestListBuilder = new ImmutableList.Builder<>();
		RxOsmParser rxOsmParser = new RxOsmParser(inputStream);
		attachNodeGeometryMapBuilder(rxOsmParser);
		attachNodeToPointOfInterestFilterAndTransformer(rxOsmParser);
		
		return this.pointOfInterestListBuilder.build();
	}
	
	private void attachNodeToPointOfInterestFilterAndTransformer(
			RxOsmParser rxOsmParser) {
		Observable<OsmNode> nodeObservable = rxOsmParser.getNodeObservable();
		
		// retain nodes that represent buildings and points of interest...
		nodeObservable.filter(new Func1<OsmNode, Boolean>() {

			@Override
			public Boolean call(OsmNode osmNode) {
				ImmutableMap<String, String> props = osmNode.getProperties();
				for (String interestingTag : interestingTags.keySet()) {
					// all values are ok if we attached empty set to allowed values
					if (props.containsKey(interestingTag) && interestingTags.get(interestingTag).isEmpty()) {
						return Boolean.TRUE;
					}
					// retain if an interesting tag and value is one we are interested in
					if (props.containsKey(interestingTag) 
							&& hasInterestingValue(interestingTags,  props, interestingTag)) {
						return Boolean.TRUE;
					}
				}
				for (String tagInterestingIfNamed : interestingTagsWithNameOnly.keySet()) {
					if (props.containsKey(tagInterestingIfNamed)
							&& hasInterestingValue(interestingTagsWithNameOnly, props,tagInterestingIfNamed)
							&& props.containsKey(name)) {
						return Boolean.TRUE;
					}
				}
				return Boolean.FALSE;
			}

			private boolean hasInterestingValue(ImmutableMap<String, ImmutableSet<String>> map,
					ImmutableMap<String, String> props,
					String tagInterestingIfNamed) {
				return map.get(tagInterestingIfNamed).contains(props.get(tagInterestingIfNamed));
			}

			
			
		});
		
	}

	private void attachNodeGeometryMapBuilder(RxOsmParser rxOsmParser) {
		
		Observable<OsmNode> nodeObservable = rxOsmParser.getNodeObservable();
		nodeObservable.subscribe(new Action1<OsmNode>() {

			@Override
			public void call(OsmNode node) {
				RxBuildingAndPOICollectionBuilder.this.osmPoints.put(node.getId(), node.getPoint());
			}
		});
		
	}
	
}
