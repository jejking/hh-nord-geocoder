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
package com.jejking.hh.nord.gazetteer.osm.streets;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.jejking.hh.nord.gazetteer.osm.WayNdsToLineString;
import com.jejking.osm.OsmNode;
import com.jejking.osm.OsmWay;
import com.jejking.osm.RxOsmParser;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * Class to extract street names and geometries from an Open Street Map XML file.
 * 
 * @author jejking
 */
public class RxOsmStreetCollectionBuilder {

    private static final String HIGHWAY = "highway";
    private static final String NAME = "name";
    private static final String PUBLIC_TRANSPORT = "public_transport";

    private final Map<Long, Point> osmPoints = new HashMap<>();
    private final Map<String, Geometry> osmNamedStreets = new HashMap<>();
    private final GeometryFactory geometryFactory;

    private static final ImmutableSet<String> acceptableHighwayTypes = ImmutableSet.of(
                                                                            "motorway",
                                                                            "trunk",
                                                                            "primary",
                                                                            "secondary", 
                                                                            "tertiary", 
                                                                            "unclassified", 
                                                                            "residential", 
                                                                            "service", 
                                                                            "road", 
                                                                            "pedestrian");
    private static final ImmutableSet<String> streetsToFilter = ImmutableSet.of("Plan", "Galerie");

    /**
     * Constructor.
     * 
     * @param geometryFactory
     *            a geometry factory, may not be <code>null</code>
     * @throws NullPointerException
     *             if geometry factory <code>null</code>
     */
    public RxOsmStreetCollectionBuilder(GeometryFactory geometryFactory) {
        this.geometryFactory = checkNotNull(geometryFactory);
    }

    /**
     * Constructs an immutable map of street name to geometry from XML Open Street Map data flowing from the input
     * stream.
     * 
     * @param inputStream
     *            with XML data
     * @return map of street name to geometry mappings
     */
    public Map<String, Geometry> streetsFromStream(InputStream inputStream) {

        RxOsmParser parser = new RxOsmParser(inputStream);
        attachNodeMapBuilder(parser);
        attachWayBuilder(parser);

        parser.parseOsmStream();

        return ImmutableMap.copyOf(osmNamedStreets);

    }

    private void attachWayBuilder(RxOsmParser parser) {
        // first, filter to retain just the right sort of Highway, and just those with names
        parser.getWayObservable().filter(new Func1<OsmWay, Boolean>() {

            @Override
            public Boolean call(OsmWay way) {
                return way.getProperties().containsKey(HIGHWAY) && way.getProperties().containsKey(NAME)
                        && acceptableHighwayTypes.contains(way.getProperties().get(HIGHWAY));
            }
        }).filter(new Func1<OsmWay, Boolean>() {

            // Excludes "highways" that are also things like platforms, bus stops, etc.
            @Override
            public Boolean call(OsmWay way) {
                return !way.getProperties().containsKey(PUBLIC_TRANSPORT);
            }
        }).filter(new Func1<OsmWay, Boolean>() {

            // work around to exclude streets with common names
            @Override
            public Boolean call(OsmWay way) {
                return !streetsToFilter.contains(way.getProperties().get(NAME));
            }
        }).subscribe(new Action1<OsmWay>() {

            final WayNdsToLineString wayNdsToLineString = new WayNdsToLineString(geometryFactory, osmPoints);

            @Override
            public void call(OsmWay way) {
                String name = way.getProperties().get(NAME);

                // assemble geometry from the points referenced in nd child elements of way
                LineString wayLineString = wayNdsToLineString.call(way.getNdRefs());

                if (osmNamedStreets.containsKey(name)) {
                    // if we already have the name of the street, then perform a union with existing geometry
                    osmNamedStreets.put(name, wayLineString.union(osmNamedStreets.get(name)));
                } else {
                    osmNamedStreets.put(name, wayLineString);
                }

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
