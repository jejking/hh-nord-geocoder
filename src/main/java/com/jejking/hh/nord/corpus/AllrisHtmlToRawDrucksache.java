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

import java.io.BufferedInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.joda.time.LocalDate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import rx.functions.Func1;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class to map a file (previously downloaded from a URL encoded to a hex-string that is the 
 * file's name, and gzipped) to an initial representation we can work with.
 * 
 * @author jejking
 *
 */
public class AllrisHtmlToRawDrucksache implements Func1<File, Optional<RawDrucksache>>{

    private final ImmutableMap<URL, Optional<LocalDate>> urlToDateMap;
    
    public AllrisHtmlToRawDrucksache(ImmutableMap<URL, Optional<LocalDate>> urlToDateMap) {
        this.urlToDateMap = checkNotNull(urlToDateMap);
    }
    
    @Override
    public Optional<RawDrucksache> call(File file) {
        try {
            try(InputStream inputStream = new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                URL originalUrl = originalUrlFromFileName(file);
                
                Document htmlDoc = Jsoup.parse(inputStream, null, "http://ratsinformation.web.hamburg.de:85/bi/vo040.asp?showall=true");
                
                String druckSacheId = druckSacheId(htmlDoc);
                ImmutableMap<String, String> props = druckSachenProperties(htmlDoc);
                ImmutableList<String> contents = druckSachenContents(htmlDoc);
                
                Optional<LocalDate> optionalDate = this.urlToDateMap.get(originalUrl);
                
                RawDrucksache drucksache = new RawDrucksache(druckSacheId, originalUrl, optionalDate, props, contents);
                
                return Optional.of(drucksache);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.absent();
        }
    }


    private URL originalUrlFromFileName(File file) throws MalformedURLException, DecoderException {
        String hexName = file.getName().substring(0, file.getName().length() - 3); // trim off ".gz"
        URL originalUrl = new URL(new String(Hex.decodeHex(hexName.toCharArray()), Charsets.UTF_8));
        return originalUrl;
    }

    private ImmutableList<String> druckSachenContents(Document htmlDoc) {
        /*
         * In this way we can identify the bits of "RTF" like text inserted into the overall HTML.
         * JSoup cleans up the broken HTML removing the xml declaration and inserted html roots
         * that ALLRIS manages to put in.
         */
        Elements contentMetaElements = htmlDoc.getElementsByAttributeValue("name", "generator");
        ImmutableList.Builder<String> listBuilder = ImmutableList.builder();
        
        /*
         * Iterate over our candidates. Sometimes there are several.
         */
        for (Element contentMetaElement : contentMetaElements) {
            StringBuilder contentAsTextBuilder = new StringBuilder();
            Element nextSibling = contentMetaElement.nextElementSibling();
            
            /*
             * In the cleaned up HTML DOM returned by JSoup the "RTF" content is
             * rendered as siblings of the meta node (JSoup having removed the html, head, body
             * elements which should never have been there in the first place). 
             */
            while (nextSibling != null && !nextSibling.tag().equals("meta")) {
                contentAsTextBuilder.append(nextSibling.text());
                nextSibling = nextSibling.nextElementSibling();
            }
            /*
             * Only carry over non-empty content.
             */
            String contentAsText = contentAsTextBuilder.toString();
            if (!removeNonBreakingSpacesAndTrim(contentAsText).isEmpty()) {
                listBuilder.add(contentAsText);
            }
        }
        
        return listBuilder.build();
    }


    private ImmutableMap<String, String> druckSachenProperties(Document htmlDoc) {

        ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
        Elements keyElements = htmlDoc.getElementsByClass("kb1"); // td elements
        for (Element element : keyElements) {
            String key = removeNonBreakingSpacesAndTrim(element.text());
            if (key.endsWith(":")) {
                key = key.substring(0, key.length() - 1);
            }
            if (element.nextElementSibling() != null && !element.nextElementSibling().hasAttr("kb1")) {
                String value = removeNonBreakingSpacesAndTrim(element.nextElementSibling().text());
                
                if ((!key.isEmpty()) && (!value.isEmpty())) {
                    mapBuilder.put(key, value);
                }
            }
        }
        return mapBuilder.build();
    }


    private String removeNonBreakingSpacesAndTrim(String text) {
        // the unicode character for non-breaking space...
        return text.replace('\u00A0', ' ').trim();
    }


    private String druckSacheId(Document htmlDoc) {
        Elements druckSacheIdElememnts = htmlDoc.select("#risname > h1");
        Element druckSacheIdElement = druckSacheIdElememnts.first();
        String elementText = druckSacheIdElement.text();
        String druckSacheId = removeNonBreakingSpacesAndTrim(elementText.substring("Drucksache - ".length()));
        return druckSacheId;
    }

    

}
