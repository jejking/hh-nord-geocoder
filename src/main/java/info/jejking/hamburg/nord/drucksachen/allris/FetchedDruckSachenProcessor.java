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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Hex;
import org.joda.time.LocalDate;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

/**
 * Batch processor that to create a directory full of serialised {@link RawDrucksache} objects
 * from a directory full of compressed HTML files extracted from Allris using the {@link DrucksachenHtmlFetcher}. The
 * documents are assigned optional dates given a map of {@link URL} to {@link LocalDate} created from the 
 * Allris Drucksachen index page (or a copy thereof) using {@link DrucksachenLinkAndDateExtractor}. 
 * 
 * @author jejking
 *
 */
public class FetchedDruckSachenProcessor {

    private final AtomicInteger counter = new AtomicInteger();
    
    /**
     * Runs the program. The arguments expected are:
     * <ol>
     * <li>file path to a copy of the HTML Drucksachen index.</li>
     * <li>directory containing compressed HTML files downloaded where the file name is the hex encoded originating URL</li>
     * <li>directory to which serialised {@link RawDrucksache} objects are to be written to</li>
     * </ol>
     * 
     * @param args, as above
     */
    public static void main(String[] args) throws Exception {
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(args[0]));
        DrucksachenLinkAndDateExtractor linkAndDateExtractor = new DrucksachenLinkAndDateExtractor(inputStream);
        
        ImmutableMap<URL, Optional<LocalDate>> urlDateMap = linkAndDateExtractor.call();
        
        FetchedDruckSachenProcessor proc = new FetchedDruckSachenProcessor();
        proc.preProcessFetchedDocuments(new File(args[1]), new File(args[2]), urlDateMap);

    }
    
    public void preProcessFetchedDocuments(final File inputDirectory, final File outputDirectory, ImmutableMap<URL, Optional<LocalDate>> urlDateMap) {
        Observable.from(inputDirectory.list())
        .map(new Func1<String, File>() {

            @Override
            public File call(String fileName) {
                return new File(inputDirectory.getPath() + File.separator + fileName);
            }
            
        })
        .map(new AllrisHtmlToRawDrucksache(urlDateMap))
        .observeOn(Schedulers.io())
        .subscribe(new Action1<Optional<RawDrucksache>>() {

            @Override
            public void call(Optional<RawDrucksache> rawDrucksache) {
                if (rawDrucksache.isPresent()) {
                    File destination = new File(outputDirectory 
                                            + File.separator 
                                            + Hex.encodeHexString(rawDrucksache.get().getDrucksachenId().getBytes(Charsets.UTF_8))
                                            + ".dat");
                    if (!destination.exists()) {
                        try(ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(destination)))) {
                            oos.writeObject(rawDrucksache.get());
                            counter.addAndGet(1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        System.err.println("Duplicate drucksachen-id: " + rawDrucksache.get().getDrucksachenId());
                    }
                }
                
            }
            
        });
        
        System.out.println("Written " + counter.get() + " data sets");
    }

}
