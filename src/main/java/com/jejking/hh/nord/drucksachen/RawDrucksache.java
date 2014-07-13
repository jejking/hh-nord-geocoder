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
package com.jejking.hh.nord.drucksachen;

import java.io.Serializable;
import java.net.URL;

import org.joda.time.LocalDate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Type safe representation of key information to be extracted
 * from the HTML representing a <i>Drucksache</i>. In this sense,
 * represents the results of the first stage of processing the corpus. 
 * 
 * @author jejking
 *
 */
public final class RawDrucksache implements Serializable {

	/**
     * Default.
     */
    private static final long serialVersionUID = 1L;
    
    private final String drucksachenId;
	private final URL originalUrl;
	private final Optional<LocalDate> date;
	private final ImmutableMap<String, String> extractedProperties;
	private final ImmutableList<String> extractedContent;

    /**
     * Constructor.
     * 
     * @param drucksachenId id
     * @param originalUrl the originating URL
     * @param date optional date (if available), may not be <code>null</code>
     * @param extractedProperties properties (e.g. type, status, etc)
     * @param extractedContent plain text content extracted from the HTML
     */
	public RawDrucksache(String drucksachenId, URL originalUrl, Optional<LocalDate> date, ImmutableMap<String, String> extractedProperties,
            ImmutableList<String> extractedContent) {
        super();
        this.drucksachenId = checkNotNull(drucksachenId);
        this.originalUrl = checkNotNull(originalUrl);
        this.date = checkNotNull(date);
        this.extractedProperties = checkNotNull(extractedProperties);
        this.extractedContent = checkNotNull(extractedContent);
    }
	
	/**
	 * Returns a new instance which makes a copy of <code>this</code> with the exception of the new date 
	 * (for example if a date is subsequently discovered).
	 * 
	 * @param newOptionalDate new optional date, may not be <code>null</code>. Ideally, should have a value.
	 * @return new instance with new date set.
	 */
	public RawDrucksache withNewOptionalDate(Optional<LocalDate> newOptionalDate) {
        return new RawDrucksache(this.drucksachenId, 
                                 this.originalUrl, 
                                 newOptionalDate, 
                                 this.extractedProperties, 
                                 this.extractedContent);
    }
	
	
    /**
     * @return the drucksachenId
     */
    public String getDrucksachenId() {
        return drucksachenId;
    }

    
    /**
     * @return the originalUrl
     */
    public URL getOriginalUrl() {
        return originalUrl;
    }

    
    /**
     * @return the extractedProperties
     */
    public ImmutableMap<String, String> getExtractedProperties() {
        return extractedProperties;
    }

    
    /**
     * @return the extractedContent
     */
    public ImmutableList<String> getExtractedContent() {
        return extractedContent;
    }
    
    
    

    
    /**
     * @return the date
     */
    public Optional<LocalDate> getDate() {
        return date;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RawDrucksache [drucksachenId=" + drucksachenId + ", originalUrl=" + originalUrl + ", date=" + date
                + ", extractedProperties=" + extractedProperties + ", extractedContent=" + extractedContent + "]";
    }


    


    

	
	
	
}
