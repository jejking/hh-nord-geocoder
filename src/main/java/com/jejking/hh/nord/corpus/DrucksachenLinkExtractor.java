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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.ImmutableList;

/**
 * This is a utility class to extract links web pages with query parameters (not resources, as they unfortunately do not
 * appear to have URIs) which represent <i>Drucksachen</i> in the Hamburg ALLRIS installation.
 * 
 * <p>
 * We assume that we have the ability to append a flag indicating that links to the entire data set can be exported on a
 * single page of HTML (albeit broken HTML) and that such a page may actually be stored locally as a copy.
 * </p>
 * 
 * <p>
 * As each "page" contains its own metadata, we are only looking to extract the relevant set of links.
 * </p>
 * 
 * @author jejking
 * 
 */
public class DrucksachenLinkExtractor implements Callable<ImmutableList<URL>> {

    private final InputStream input;

    public DrucksachenLinkExtractor(InputStream input) {
        this.input = checkNotNull(input);
    }

    @Override
    public ImmutableList<URL> call() throws Exception {

        ImmutableList.Builder<URL> listBuilder = ImmutableList.builder();
        Document htmlDoc = Jsoup.parse(input, null,
                "http://ratsinformation.web.hamburg.de:85/bi/vo040.asp?showall=true");
        Elements linkElements = htmlDoc.select("#rismain > table > tbody > tr > td > a");

        for (Element linkElement : linkElements) {
            String href = linkElement.attr("href");
            listBuilder.add(new URL(href));
        }

        return listBuilder.build();
    }

    public static void main(String[] args) throws Exception {
        URL copy = new URL(args[0]);
        DrucksachenLinkExtractor extractor = new DrucksachenLinkExtractor(copy.openStream());
        ImmutableList<URL> urls = extractor.call();

        System.out.println("Found " + urls.size() + " urls");

    }

}
