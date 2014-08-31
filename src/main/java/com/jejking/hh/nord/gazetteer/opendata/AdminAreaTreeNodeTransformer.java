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

import java.util.Iterator;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import rx.functions.Func1;

import com.google.common.base.Optional;
import com.jejking.hh.nord.gazetteer.GazetteerEntryTypes;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Function that transforms the results of parsing and assembling
 * the raw Hamburg admin area Open Data into a similarly structured tree that
 * contains polygons in WGS 84 and with a full root node.
 * 
 * @author jejking
 *
 */
public class AdminAreaTreeNodeTransformer implements Func1<AdminAreaTreeNode<String>, AdminAreaTreeNode<Polygon>> {

    
    GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

    @Override
    public AdminAreaTreeNode<Polygon> call(AdminAreaTreeNode<String> input) {

        AdminAreaTreeNode<String> wktStrings = input.fmap(new CreateWkt()).call(input);
        AdminAreaTreeNode<Optional<Polygon>> epsgPolygons = wktStrings.fmap(new CreatePolygon()).call(wktStrings);
        AdminAreaTreeNode<Optional<Polygon>> wgs84Polygons = epsgPolygons.fmap(new ConvertPolygonToWGS84()).call(
                epsgPolygons);
        AdminAreaTreeNode<Polygon> fixedRoot = new BuildHamburgRootPolygon().call(wgs84Polygons);

        return fixedRoot;

    }

    private static MathTransform buildMathTransform() {
        try {
            CRSAuthorityFactory factory = CRS.getAuthorityFactory(true);
            CoordinateReferenceSystem fromCrs = factory.createCoordinateReferenceSystem("urn:ogc:def:crs:EPSG:6.9:25832");
            CoordinateReferenceSystem toCrs = DefaultGeographicCRS.WGS84;
            return CRS.findMathTransform(fromCrs, toCrs);
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }

    }

    final class CreateWkt implements Func1<String, String> {
    
        @Override
        public String call(String in) {
            StringBuilder stringBuilder = new StringBuilder("POLYGON((");
            
            String[] parts = in.split(" ");
            for (int i = 0; i < parts.length; i++) {
                if (i % 2 == 0) {
                    stringBuilder.append(parts[i] + " ");
                } else {
                    stringBuilder.append(parts[i]);
                    if (i < parts.length - 2) {
                        stringBuilder.append(", ");
                    }
                }
                
            }
            
            stringBuilder.append("))");
            return stringBuilder.toString();
        }
    }
    
    final class CreatePolygon implements Func1<String, Optional<Polygon>> {
        @Override
        public Optional<Polygon> call(String in) {
            WKTReader reader = new WKTReader(geometryFactory);
            try {
                return Optional.of((Polygon) reader.read(in));
            } catch (ParseException e) {
                return Optional.absent();
            }
        }
    }
    
    final class ConvertPolygonToWGS84 implements Func1<Optional<Polygon>, Optional<Polygon>> {

        // convert to WGS 84
        private final MathTransform transform = buildMathTransform();

        @Override
        public Optional<Polygon> call(Optional<Polygon> in) {

            if (in.isPresent()) {
                try {
                    return Optional.of((Polygon) JTS.transform(in.get(), transform));
                } catch (MismatchedDimensionException | TransformException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return Optional.absent();
            }
        }
    }

    final class BuildHamburgRootPolygon implements
            Func1<AdminAreaTreeNode<Optional<Polygon>>, AdminAreaTreeNode<Polygon>> {

        // give the root node ("Hamburg") a polygon of its own
        @Override
        public AdminAreaTreeNode<Polygon> call(AdminAreaTreeNode<Optional<Polygon>> in) {
            Geometry unionOfBoroughs = computeUnionOfBoroughs(in);
            Polygon boundaryAsPolygon = convertToPolygon(unionOfBoroughs);

            AdminAreaTreeNode<Polygon> fixedRoot = new AdminAreaTreeNode<Polygon>("Hamburg", GazetteerEntryTypes.CITY,
                    boundaryAsPolygon);

            Func1<AdminAreaTreeNode<Optional<Polygon>>, AdminAreaTreeNode<Polygon>> f = in
                    .fmap(new Func1<Optional<Polygon>, Polygon>() {

                        @Override
                        public Polygon call(Optional<Polygon> t1) {
                            return t1.get();
                        }

                    });

            for (String key : in.getChildren().keySet()) {
                fixedRoot.getChildren().put(key, f.call(in.getChildren().get(key)));
            }

            return fixedRoot;
        }

        private Polygon convertToPolygon(Geometry unionOfBoroughs) {
            LinearRing boundaryAsRing = (LinearRing) unionOfBoroughs;

            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
            Polygon boundaryAsPolygon = geometryFactory.createPolygon(boundaryAsRing);
            return boundaryAsPolygon;
        }

        private Geometry computeUnionOfBoroughs(AdminAreaTreeNode<Optional<Polygon>> root) {
            Iterator<AdminAreaTreeNode<Optional<Polygon>>> iterator = root.getChildren().values().iterator();
            // we basically start at the first borough and add to that....
            Geometry unionOfBoroughs = iterator.next().getContent().get();

            while (iterator.hasNext()) {
                unionOfBoroughs = unionOfBoroughs.union(iterator.next().getContent().get());
            }

            unionOfBoroughs = unionOfBoroughs.getBoundary();
            return unionOfBoroughs;

        }
    }

}
