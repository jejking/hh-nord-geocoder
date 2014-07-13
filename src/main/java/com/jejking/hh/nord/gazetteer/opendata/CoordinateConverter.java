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
package com.jejking.hh.nord.gazetteer.opendata;


import java.util.Iterator;
import java.util.Map;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.jejking.hh.nord.gazetteer.GazetteerEntryTypes;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Class, interface and functions to handle the conversion between
 * the raw input extracted from the Hamburg WFS services and polygons
 * in WGS 84.
 * 
 * @author jejking
 *
 */
public class CoordinateConverter {

    /**
     * Converts a raw input node where the string represents a list of space separated
     * Strings representing floating point coordinates in the EPSG 25832 Coordinate 
     * Representation System to a nodes of polygon geometries in WGS 84 CRS.
     * 
     * @param rawRoot
     * @return
     */
    public NamedTreeNode<Polygon> rawToPolygon(NamedTreeNode<String> rawRoot) {
       return convert(rawRoot, compose(new PolygonConversion(), new WktToGeometry()));
    }
    
    /**
     * Fixes our root node by creating a polygon geometry from union
     * of the children.
     * 
     * @param root
     * @return
     */
    public NamedTreeNode<Polygon> fixRoot(NamedTreeNode<Polygon> root) {
        
        Geometry unionOfBoroughs = computeUnionOfBoroughs(root);
        Polygon boundaryAsPolygon = convertToPolygon(unionOfBoroughs);
       
        NamedTreeNode<Polygon> fixedRoot = new NamedTreeNode<Polygon>("Hamburg", GazetteerEntryTypes.CITY, boundaryAsPolygon);
        fixedRoot.getChildren().putAll(root.getChildren());
        return fixedRoot;
    }

    private Polygon convertToPolygon(Geometry unionOfBoroughs) {
        LinearRing boundaryAsRing = (LinearRing) unionOfBoroughs;
        
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        Polygon boundaryAsPolygon = geometryFactory.createPolygon(boundaryAsRing);
        return boundaryAsPolygon;
    }

    private Geometry computeUnionOfBoroughs(NamedTreeNode<Polygon> root) {
        Iterator<NamedTreeNode<Polygon>> iterator = root.getChildren().values().iterator();
        Geometry unionOfBoroughs = iterator.next().getContent();
        
        
        while (iterator.hasNext()) {
            unionOfBoroughs = unionOfBoroughs.union(iterator.next().getContent());
        }
        
        unionOfBoroughs = unionOfBoroughs.getBoundary();
        return unionOfBoroughs;
    }
    
    /**
     * Recursively converts a NamedTreeNode<F> to a NamedTreeNode<T>. Names are retained throughout.
     * @param from from
     * @param conversion the function to apply to the payload
     * @return converted NamedTreeNode<T>
     */
    public <T, F> NamedTreeNode<T> convert(NamedTreeNode<F> from, Conversion<F, T> conversion) {
        // convert node content itself, keeping the name
        NamedTreeNode<T> to = new NamedTreeNode<T>(from.getName(), from.getType(), conversion.convert(from.getContent()));
        
        // apply same conversion to children, they retain the same name
        Map<String, NamedTreeNode<T>> toChildren = to.getChildren();
        for (String fromChildKey : from.getChildren().keySet()) {
            toChildren.put(fromChildKey, convert(from.getChildren().get(fromChildKey), conversion));
        }
        return to;
    }
    
    /**
     * Trivial function interface.
     *
     * @param <F> from type
     * @param <T> to type
     */
    public interface Conversion<F, T> {
        /**
         * Applies a mapping function.
         * @param from
         * @return converted
         */
        public T convert(F from);
    }
    
    /**
     * Chains the two conversions together.
     * @param first the first conversion
     * @param second the second conversion
     * @return conversion where the second is applied to the output of the first.
     */
    public static <F, T, V> Conversion<F, T> compose(final Conversion<F, V> first, final Conversion<V, T> second) {
        return new Conversion<F, T>() {
            
            public T convert(F from) {
                return second.convert(first.convert(from));
            }
        };
    }
    
    /**
     * Function class to convert the "raw" data from the WFS service
     * into the WKT form for a POLYGON.
     *
     */
    public static class PolygonConversion implements Conversion<String, String> {

        @Override
        public String convert(String from) {
            StringBuilder stringBuilder = new StringBuilder("POLYGON((");
            
            String[] parts = from.split(" ");
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
    
    /**
     * Function class to convert a String representing WKT for a Polygon
     * into the corresponding geometry class.
     */
    public static class WktToGeometry implements Conversion<String, Polygon> {

        private final CRSAuthorityFactory factory;
        private final CoordinateReferenceSystem fromCrs;
        private final CoordinateReferenceSystem toCrs;
        
        private final MathTransform transform;
        private final GeometryFactory geometryFactory;
        
        public WktToGeometry() {
            try {
                this.factory = CRS.getAuthorityFactory(true);
                this.fromCrs = factory.createCoordinateReferenceSystem("urn:ogc:def:crs:EPSG:6.9:25832");
                this.toCrs = DefaultGeographicCRS.WGS84;
                this.transform = CRS.findMathTransform(fromCrs, toCrs);
                this.geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        @Override
        public Polygon convert(String from) {
            try {
                WKTReader reader = new WKTReader(geometryFactory);
                Polygon polygonFrom = (Polygon) reader.read(from);
                return (Polygon) JTS.transform(polygonFrom, transform);
            } catch (Exception e) {
                //throw new RuntimeException("Could not convert " + from, e);
                return null; // not nice..
            }
        }
        
    }
    
    
}
