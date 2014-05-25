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
import static info.jejking.hamburg.nord.geocoder.hh.OsmConstants.houseNumber;
import static info.jejking.hamburg.nord.geocoder.hh.OsmConstants.name;
import static info.jejking.hamburg.nord.geocoder.hh.OsmConstants.street;
import static info.jejking.hamburg.nord.geocoder.hh.OsmConstants.type;
import static info.jejking.hamburg.nord.geocoder.hh.OsmConstants.multipolygon;
import static info.jejking.hamburg.nord.geocoder.hh.OsmConstants.natural;
import static info.jejking.hamburg.nord.geocoder.hh.OsmConstants.waterway;
import info.jejking.osm.OsmComponent;
import info.jejking.osm.OsmNode;
import info.jejking.osm.OsmRelation;
import info.jejking.osm.OsmWay;
import info.jejking.osm.RxOsmParser;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Class to assemble useful descriptions of buildings and other points
 * of interest from an Open Street Map file for further processing.
 * 
 * @author jejking
 *
 */
public class RxBuildingAndPOICollectionBuilder {
	
	private static final class FilterWaterwaysPredicate implements Func1<OsmComponent, Boolean> {

        @Override
        public Boolean call(OsmComponent osmComponent) {
            if (osmComponent.getProperties().containsKey(waterway)
                    || osmComponent.getProperties().containsKey(natural)) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    }

    private static final IsInterestingOsmFeaturePredicate isInterestingOsmFeaturePredicate = new IsInterestingOsmFeaturePredicate();
	private static final OsmComponentLabeller osmComponentLabeller = new OsmComponentLabeller();
	
	
	private final GeometryFactory geometryFactory;
	
		
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
	public ImmutableList<PointOfInterest> pointsOfInterestFromStream(final InputStream inputStream) {
	    ImmutableList.Builder<PointOfInterest> pointOfInterestListBuilder = new ImmutableList.Builder<>();
		Map<Long, Point> osmPoints = new HashMap<>();
		Map<Long, LineString> osmLineStrings = new HashMap<>();
		
		RxOsmParser rxOsmParser = new RxOsmParser(inputStream);
		
		attachNodeGeometryMapBuilderTo(rxOsmParser.getNodeObservable(), osmPoints);
		attachNodePointOfInterestBuilderTo(rxOsmParser.getNodeObservable(), pointOfInterestListBuilder);
		
		attachWayGeometryMapBuilderTo(rxOsmParser.getWayObservable(), osmPoints, osmLineStrings);
		attachWayPointOfInterestBuilderTo(rxOsmParser.getWayObservable(), osmPoints, pointOfInterestListBuilder);
		
		
		attachRelationPointOfInterestBuilderTo(rxOsmParser.getRelationObservable(), osmLineStrings, pointOfInterestListBuilder);
		
		
		rxOsmParser.parseOsmStream();
		return pointOfInterestListBuilder.build();
	}
	
	void attachRelationPointOfInterestBuilderTo(Observable<OsmRelation> relationObservable, final Map<Long, LineString> osmLineStrings, final Builder<PointOfInterest> poiListBuilder) {
		
		relationObservable
			.filter(isInterestingOsmFeaturePredicate)
			.filter(new FilterWaterwaysPredicate())
			.filter(new Func1<OsmRelation, Boolean>() {

				@Override
				public Boolean call(OsmRelation osmRelation) {
					if (osmRelation.getProperties().containsKey(type) 
							&& osmRelation.getProperties().get(type).equals(multipolygon)) {
						return Boolean.TRUE;
					}
					return Boolean.FALSE;
				}
				
			})
			.map(new Func1<OsmRelation, Optional<PointOfInterest>>() {

				final RelationWaysToPolygon relationWaysToPolygon = new RelationWaysToPolygon(geometryFactory, osmLineStrings);
				
				@Override
				public Optional<PointOfInterest> call(OsmRelation osmRelation) {

					Optional<Polygon> polygon = relationWaysToPolygon.call(osmRelation);
					if (polygon.isPresent()) {
						PointOfInterest poi = new PointOfInterest(
								osmComponentLabeller.call(osmRelation), 
								polygon.get().getCentroid(), 
								Optional.fromNullable(osmRelation.getProperties().get(houseNumber)), 
	                            Optional.fromNullable(osmRelation.getProperties().get(street)), 
	                            Optional.fromNullable(osmRelation.getProperties().get(name)));
						return Optional.of(poi);
					} else {
						return Optional.absent();
					}
					
				}
				
			})
			.subscribe(new Action1<Optional<PointOfInterest>>() {

				@Override
				public void call(Optional<PointOfInterest> poi) {
					if (poi.isPresent()) {
						poiListBuilder.add(poi.get());	
					}
				}
				
			});
		
		
	}

	void attachWayPointOfInterestBuilderTo(Observable<OsmWay> wayObservable,
            Map<Long, Point> osmPoints, final Builder<PointOfInterest> poiListBuilder) {
        
        final WayNdsToLineString wayNdsToLineString = new WayNdsToLineString(geometryFactory, osmPoints);
        
        wayObservable
            .filter(isInterestingOsmFeaturePredicate)
            .filter(new FilterWaterwaysPredicate())
            .map(new Func1<OsmWay, PointOfInterest>() {

                @Override
                public PointOfInterest call(OsmWay osmWay) {
                    PointOfInterest poi = new PointOfInterest(
                            osmComponentLabeller.call(osmWay), 
                            wayNdsToLineString.call(osmWay.getNdRefs()).getCentroid(),
                            Optional.fromNullable(osmWay.getProperties().get(houseNumber)), 
                            Optional.fromNullable(osmWay.getProperties().get(street)), 
                            Optional.fromNullable(osmWay.getProperties().get(name)));
                    return poi;
                }
                
            })
            .subscribe(new Action1<PointOfInterest>() { // add them to our list of points of interest

                @Override
                public void call(PointOfInterest poi) {
                    poiListBuilder.add(poi);
                }
            });
        
    }

    void attachNodePointOfInterestBuilderTo(Observable<OsmNode> nodeObservable, final Builder<PointOfInterest> poiListBuilder) {
  	
		// retain nodes that represent buildings and points of interest...
		nodeObservable
		    .filter(isInterestingOsmFeaturePredicate)
		    .filter(new FilterWaterwaysPredicate())
		    .map(new Func1<OsmNode, PointOfInterest>() { // map remaining set to points of interest

                @Override
                public PointOfInterest call(OsmNode osmNode) {
                    PointOfInterest poi = new PointOfInterest(
                                            osmComponentLabeller.call(osmNode), 
                                            osmNode.getPoint(), 
                                            Optional.fromNullable(osmNode.getProperties().get(houseNumber)), 
                                            Optional.fromNullable(osmNode.getProperties().get(street)), 
                                            Optional.fromNullable(osmNode.getProperties().get(name)));
                    return poi;
                }

               
            })
            .subscribe(new Action1<PointOfInterest>() { // add them to our list of points of interest

                @Override
                public void call(PointOfInterest poi) {
                    poiListBuilder.add(poi);
                }
            });
    }

	private void attachNodeGeometryMapBuilderTo(Observable<OsmNode> nodeObservable, final Map<Long, Point> nodePointMap) {
		
		nodeObservable.subscribe(new Action1<OsmNode>() {

			@Override
			public void call(OsmNode node) {
			    nodePointMap.put(node.getId(), node.getPoint());
			}
		});
		
	}
	
	private void attachWayGeometryMapBuilderTo(Observable<OsmWay> wayObservable, final Map<Long, Point> nodePointMap, final Map<Long, LineString> osmLineStrings) {
		
		final WayNdsToLineString wayNdsToLineString = new WayNdsToLineString(geometryFactory, nodePointMap);
		
		wayObservable.subscribe(new Action1<OsmWay>() {

			@Override
			public void call(OsmWay osmWay) {
				osmLineStrings.put(osmWay.getId(), wayNdsToLineString.call(osmWay.getNdRefs()));
			}
		});
	}
	
	 
	
}
