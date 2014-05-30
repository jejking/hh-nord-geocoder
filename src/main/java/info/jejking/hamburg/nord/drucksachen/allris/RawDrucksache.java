package info.jejking.hamburg.nord.drucksachen.allris;

import java.net.URL;

import org.joda.time.LocalDate;
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

import com.google.common.collect.ImmutableList;

/**
 * Type safe representation of key information to be extracted
 * from the HTML representing a <i>Drucksache</i>.
 * 
 * @author jejking
 *
 */
public class RawDrucksache {

	private final String id;
	private final String title;
	private final String type;
	private final URL originalUrl;
	private final LocalDate creationDate;
	
	private final ImmutableList<String> extractedHtmlContentItems;

	public RawDrucksache(String id, String title, String type, URL originalUrl,
			LocalDate creationDate,
			ImmutableList<String> extractedHtmlContentItems) {
		super();
		this.id = id;
		this.title = title;
		this.type = type;
		this.originalUrl = originalUrl;
		this.creationDate = creationDate;
		this.extractedHtmlContentItems = extractedHtmlContentItems;
	}
	
	/*
	 * Omitted. Any attachments, links to actual meetings or agendas,
	 * decision status (if any), coordination.
	 */
	
	
	
}
