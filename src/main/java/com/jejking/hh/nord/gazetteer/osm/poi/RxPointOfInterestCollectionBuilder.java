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
package com.jejking.hh.nord.gazetteer.osm.poi;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.houseNumber;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.multipolygon;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.name;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.natural;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.street;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.type;
import static com.jejking.hh.nord.gazetteer.osm.OsmConstants.waterway;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.jejking.hh.nord.gazetteer.osm.RelationWaysToPolygon;
import com.jejking.hh.nord.gazetteer.osm.WayNdsToLineString;
import com.jejking.osm.OsmComponent;
import com.jejking.osm.OsmNode;
import com.jejking.osm.OsmRelation;
import com.jejking.osm.OsmWay;
import com.jejking.osm.RxOsmParser;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Class to assemble useful descriptions of buildings and other points of interest from an Open Street Map file for
 * further processing.
 * 
 * @author jejking
 * 
 */
public class RxPointOfInterestCollectionBuilder {

    private static final class PointOfInterestBuilder<C extends OsmComponent> implements
            Func1<C, Optional<PointOfInterest>> {

        private final Func1<C, Point> func;

        public PointOfInterestBuilder(Func1<C, Point> func) {
            this.func = func;
        }

        @Override
        public Optional<PointOfInterest> call(C osmComponent) {
            Optional<String> optionalLabel = osmComponentLabeller.call(osmComponent);
            if (optionalLabel.isPresent()) {
                return Optional.of(new PointOfInterest(optionalLabel.get(), func.call(osmComponent), Optional
                        .fromNullable(osmComponent.getProperties().get(houseNumber)), Optional
                        .fromNullable(osmComponent.getProperties().get(street)), Optional.fromNullable(osmComponent
                        .getProperties().get(name))));
            } else {
                return Optional.absent();
            }

        }
    }

    private static final class FilterWaterwaysPredicate implements Func1<OsmComponent, Boolean> {

        @Override
        public Boolean call(OsmComponent osmComponent) {
            if (osmComponent.getProperties().containsKey(waterway) || osmComponent.getProperties().containsKey(natural)) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    }

    private static final IsOsmFeaturePointOfInterest isInterestingOsmFeaturePredicate = new IsOsmFeaturePointOfInterest();
    private static final OsmComponentPointOfInterestLabeller osmComponentLabeller = new OsmComponentPointOfInterestLabeller();

    private final GeometryFactory geometryFactory;

    /**
     * Constructor.
     * 
     * @param geometryFactory
     *            a geometry factory, may not be <code>null</code>
     * @throws NullPointerException
     *             if geometry factory <code>null</code>
     */
    public RxPointOfInterestCollectionBuilder(GeometryFactory geometryFactory) {
        this.geometryFactory = checkNotNull(geometryFactory);
    }

    /**
     * Constructs a list of points of interest (including buildings) from an input stream containing Open Street Map
     * data.
     * 
     * @param inputStream
     *            with XML data
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

        attachRelationPointOfInterestBuilderTo(rxOsmParser.getRelationObservable(), osmLineStrings,
                pointOfInterestListBuilder);

        rxOsmParser.parseOsmStream();
        return pointOfInterestListBuilder.build();
    }

    void attachRelationPointOfInterestBuilderTo(Observable<OsmRelation> relationObservable,
            final Map<Long, LineString> osmLineStrings, final Builder<PointOfInterest> poiListBuilder) {

        relationObservable.filter(isInterestingOsmFeaturePredicate).filter(new FilterWaterwaysPredicate())
                .filter(new Func1<OsmRelation, Boolean>() {

                    @Override
                    public Boolean call(OsmRelation osmRelation) {
                        if (osmRelation.getProperties().containsKey(type)
                                && osmRelation.getProperties().get(type).equals(multipolygon)) {
                            return Boolean.TRUE;
                        }
                        return Boolean.FALSE;
                    }

                }).map(new Func1<OsmRelation, Optional<PointOfInterest>>() {

                    final RelationWaysToPolygon relationWaysToPolygon = new RelationWaysToPolygon(geometryFactory,
                            osmLineStrings);

                    @Override
                    public Optional<PointOfInterest> call(OsmRelation osmRelation) {

                        final Optional<Polygon> polygon = relationWaysToPolygon.call(osmRelation);
                        final PointOfInterestBuilder<OsmRelation> builder = new PointOfInterestBuilder<>(
                                new Func1<OsmRelation, Point>() {

                                    @Override
                                    public Point call(OsmRelation osmRelation) {
                                        return polygon.get().getCentroid();
                                    }

                                });
                        if (polygon.isPresent()) {
                            return builder.call(osmRelation);
                        } else {
                            return Optional.absent();
                        }

                    }

                }).subscribe(new Action1<Optional<PointOfInterest>>() {

                    @Override
                    public void call(Optional<PointOfInterest> poi) {
                        if (poi.isPresent()) {
                            poiListBuilder.add(poi.get());
                        }
                    }

                });

    }

    void attachWayPointOfInterestBuilderTo(Observable<OsmWay> wayObservable, Map<Long, Point> osmPoints,
            final Builder<PointOfInterest> poiListBuilder) {

        final WayNdsToLineString wayNdsToLineString = new WayNdsToLineString(geometryFactory, osmPoints);

        wayObservable.filter(isInterestingOsmFeaturePredicate).filter(new FilterWaterwaysPredicate())
                .map(new PointOfInterestBuilder<OsmWay>(new Func1<OsmWay, Point>() {

                    @Override
                    public Point call(OsmWay osmWay) {
                        return wayNdsToLineString.call(osmWay.getNdRefs()).getCentroid();
                    }

                })).subscribe(new Action1<Optional<PointOfInterest>>() { // add them to our list of points of interest

                            @Override
                            public void call(Optional<PointOfInterest> poi) {
                                if (poi.isPresent()) {
                                    poiListBuilder.add(poi.get());
                                }

                            }
                        });

    }

    void attachNodePointOfInterestBuilderTo(Observable<OsmNode> nodeObservable,
            final Builder<PointOfInterest> poiListBuilder) {

        // retain nodes that represent buildings and points of interest...
        nodeObservable.filter(isInterestingOsmFeaturePredicate).filter(new FilterWaterwaysPredicate())
                .map(new PointOfInterestBuilder<OsmNode>(new Func1<OsmNode, Point>() {

                    @Override
                    public Point call(OsmNode node) {
                        return node.getPoint();
                    }

                })).subscribe(new Action1<Optional<PointOfInterest>>() { // add them to our list of points of interest

                            @Override
                            public void call(Optional<PointOfInterest> poi) {
                                if (poi.isPresent()) {
                                    poiListBuilder.add(poi.get());
                                }

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

    private void attachWayGeometryMapBuilderTo(Observable<OsmWay> wayObservable, final Map<Long, Point> nodePointMap,
            final Map<Long, LineString> osmLineStrings) {

        final WayNdsToLineString wayNdsToLineString = new WayNdsToLineString(geometryFactory, nodePointMap);

        wayObservable.subscribe(new Action1<OsmWay>() {

            @Override
            public void call(OsmWay osmWay) {
                osmLineStrings.put(osmWay.getId(), wayNdsToLineString.call(osmWay.getNdRefs()));
            }
        });
    }

}
