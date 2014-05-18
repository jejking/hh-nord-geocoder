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
import info.jejking.osm.OsmNode;
import info.jejking.osm.RxOsmParser;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
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
	
	private static final IsInterestingOsmFeaturePredicate isInterestingOsmFeaturePredicate = new IsInterestingOsmFeaturePredicate();
	private static final OsmComponentLabeller osmComponentLabeller = new OsmComponentLabeller();
	
	
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
		attachNodePointOfInterestBuilderTo(rxOsmParser.getNodeObservable(), this.pointOfInterestListBuilder);
		
		return this.pointOfInterestListBuilder.build();
	}
	

    void attachNodePointOfInterestBuilderTo(Observable<OsmNode> nodeObservable, final Builder<PointOfInterest> poiListBuilder) {
  	
		// retain nodes that represent buildings and points of interest...
		nodeObservable
		    .filter(isInterestingOsmFeaturePredicate)
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
