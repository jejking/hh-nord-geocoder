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
package com.jejking.hh.nord.corpus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Optional;

import rx.functions.Func1;

/**
 * In this case, we attempt to find a date in the text of the {@link RawDrucksache} if 
 * one could not be found in the meta-data. The approach taken is quite naive - we know
 * that a certain date format is always used by convention. (dd.MM.yyyy). So we look
 * for instances of that using a regular expression in the extracted content and attempt
 * to parse a {@link LocalDate} out of it - and the first match is then taken as the reference
 * point (there being no obvious heuristic to choose between multiple matches).
 * 
 * <p>If a date is already present or none can be found, the instance passed in is returned
 * unchanged. Otherwise, a copy with the new date value is returned.</p>
 * 
 * @author jejking
 */
public final class DrucksacheDateEnhancer implements Func1<RawDrucksache, RawDrucksache> {
    
    // e.g 06.05.2014, this pattern is a good enough match...
    private final Pattern datePattern = Pattern.compile("(\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d)"); 
    private final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("dd.MM.yyyy");
    

    @Override
    public RawDrucksache call(RawDrucksache rawDrucksache) {
        if (rawDrucksache.getDate().isPresent()) {
            return rawDrucksache; // we have a date, no need to look further for one...
        } else {
            for (String content : rawDrucksache.getExtractedContent()) {
               
                Matcher m = this.datePattern.matcher(content);
                
                while(m.find()) {
                    try {
                        String dateText = m.group(0);
                        LocalDate localDate = dateFormat.parseLocalDate(dateText);
                        return rawDrucksache.withNewOptionalDate(Optional.of(localDate));
                    } catch (Exception e) {}
                }
                
                
                
            }
        }
        // at this point we have found no matches, so give up and return as passed in
        return rawDrucksache;
    }
}