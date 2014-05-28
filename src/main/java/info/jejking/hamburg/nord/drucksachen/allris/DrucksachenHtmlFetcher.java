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
package info.jejking.hamburg.nord.drucksachen.allris;

import java.net.URL;
import java.nio.file.Path;

import com.google.common.collect.ImmutableList;

/**
 * Class to download the binary content at a list of URLs to
 * local file storage pending further work.
 * 
 * <p>The URLs are UUENCODED to avoid any conflicts with file system paths, etc</p>. 
 *
 * @author jejking
 *
 */
public class DrucksachenHtmlFetcher {

	public void fetchUrls(ImmutableList<URL> urlsToFetch, Path storageDirectory) {
		
		// figure out what work we still have to do...
		
		// schedule it....
		
	}
	
}
