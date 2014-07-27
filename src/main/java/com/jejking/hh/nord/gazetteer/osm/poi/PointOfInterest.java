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

import com.google.common.base.Optional;
import com.jejking.hh.nord.gazetteer.GazetteerEntryTypes;
import com.vividsolutions.jts.geom.Point;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Value class describing a point of interest, which may 
 * have a name and may have an address in the form of a house number
 * and street (the city being assumed to be Hamburg).
 * 
 * @author jejking
 */
public final class PointOfInterest {

	private final String label;
	private final Point point;
	
	private final Optional<String> houseNumber;
	private final Optional<String> street;
	private final Optional<String> name;
	
	/**
	 * Constructor.
	 * 
	 * @param label drawn from {@link GazetteerEntryTypes}. May not be <code>null</code> or empty.
	 * @param point geometry representing the point of interest. If drawn from a way or relation, then the centroid. May not be <code>null</code>.
	 * @param houseNumber optional house number, may not be <code>null</code>.
	 * @param street optional street name, may not be <code>null</code>
	 * @param name optional name, may not be <code>null</code>
	 * @throws NullPointerException if any parameter is <code>null</code>
	 * @throws IllegalArgumentException if label set is empty
	 */
	public PointOfInterest(String label, Point point,
			Optional<String> houseNumber, Optional<String> street,
			Optional<String> name) {
		super();
		
		this.label = checkNotNull(label);
		checkArgument(!label.trim().isEmpty());
		
		this.point = checkNotNull(point);
		this.houseNumber = checkNotNull(houseNumber);
		this.street = checkNotNull(street);
		this.name = checkNotNull(name);
	}

	
    public String getLabel() {
        return label;
    }

	public Point getPoint() {
		return point;
	}

	public Optional<String> getHouseNumber() {
		return houseNumber;
	}

	public Optional<String> getStreet() {
		return street;
	}

	public Optional<String> getName() {
		return name;
	}

	@Override
	public String toString() {
		return "PointOfInterest [label=" + label + ", point=" + point
				+ ", houseNumber=" + houseNumber + ", street=" + street
				+ ", name=" + name + "]";
	}
	
	
	
}
