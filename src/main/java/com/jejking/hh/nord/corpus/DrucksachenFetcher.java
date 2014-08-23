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
package com.jejking.hh.nord.corpus;

import java.net.URL;
import java.nio.file.Paths;

import com.google.common.collect.ImmutableList;

/**
 * Class to run the {@link DrucksachenHtmlFetcher} links extracted from a URL. The content
 * of the URL is assumed to be an ALLRIS page structured so that the {@link DrucksachenLinkExtractor}
 * can do its work. This is currently the case for
 *  <a href="http://ratsinformation.web.hamburg.de:85/bi/vo040.asp?showall=true">Hamburg Nord ALLRIS</>.
 * 
 * @author jejking
 *
 */
public class DrucksachenFetcher {

    /**
     * Extracts links and downloads all the Drucksachen in such a way as the server is not
     * subjected to excessive traffic.
     * 
     * <p>Arguments:</p>
     * <ol>
     * <li>URL of the link collection</li>
     * <li>root directory to write to</li>
     * <li>name of directory in root directory to write to</li>
     * </ol>
     * 
     * @param args 
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        
       URL sourceUrl = new URL(args[0]); 
       ImmutableList<URL> linksToFetch = new DrucksachenLinkExtractor(sourceUrl.openStream()).call();
       
       System.out.println("Found " + linksToFetch + " links to fetch");
       
       DrucksachenHtmlFetcher htmlFetcher = new DrucksachenHtmlFetcher();
       htmlFetcher.fetchUrls(linksToFetch, Paths.get(args[1], args[2]));

    }

}
