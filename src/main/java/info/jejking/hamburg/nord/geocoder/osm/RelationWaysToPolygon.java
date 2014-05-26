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
package info.jejking.hamburg.nord.geocoder.osm;

import info.jejking.osm.OsmRelation;


import info.jejking.osm.OsmRelation.Member;

import java.util.Map;

import rx.functions.Func1;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.inner;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.multipolygon;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.outer;
import static info.jejking.hamburg.nord.geocoder.osm.OsmConstants.type;

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
		    if (osmRelation.getProperties().get(type) != null && 
		            osmRelation.getProperties().get(type).equals(multipolygon)) {

		        LinearRing shell = buildShell(osmRelation); 
		        LinearRing[] holes = buildHoles(osmRelation); 
		        
		        Polygon poly = this.geometryFactory.createPolygon(shell, holes);
		        return Optional.of(poly);

		    }
		    
			// get any inner....
			return Optional.absent();
			
		} catch (Exception e) {
			return Optional.absent();
		}
		
		
		
	}

    private LinearRing[] buildHoles(OsmRelation osmRelation) {
        Iterable<LinearRing> linearRings = Iterables.transform(getInners(osmRelation), new Function<OsmRelation.Member, LinearRing>() {
            
            @Override
            public LinearRing apply(OsmRelation.Member member) {
                return linearRingFromMember(member);
            }
            
        });
        return Iterables.toArray(linearRings, LinearRing.class);
    }
    
    private Iterable<OsmRelation.Member> getInners(OsmRelation osmRelation) {
        return Iterables.filter(osmRelation.getMembers(), new Predicate<OsmRelation.Member>() {
            @Override
            public boolean apply(Member member) {
                if (member.getRole().isPresent() &&
                        member.getRole().get().equals(inner)) {
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private LinearRing buildShell(OsmRelation osmRelation) {
        return linearRingFromMember(getOuter(osmRelation));
    }

    private LinearRing linearRingFromMember(OsmRelation.Member member) {
        return this.geometryFactory
                    .createLinearRing(
                            osmLineStrings.get(member.getRef())
                                .getCoordinates());
    }

    private Member getOuter(OsmRelation osmRelation) {
        return Iterables.filter(osmRelation.getMembers(), new Predicate<OsmRelation.Member>() {
   
            @Override
            public boolean apply(Member member) {
                if (member.getRole().isPresent() &&
                        member.getRole().get().equals(outer)) {
                    return true;
                } else {
                    return false;
                }
            }
        })
        .iterator()
        .next();
    }

}
