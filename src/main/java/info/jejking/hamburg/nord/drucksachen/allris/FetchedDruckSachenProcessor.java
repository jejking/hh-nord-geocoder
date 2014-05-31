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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Hex;

import com.google.common.base.Optional;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class FetchedDruckSachenProcessor {

    private final AtomicInteger counter = new AtomicInteger();
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        FetchedDruckSachenProcessor proc = new FetchedDruckSachenProcessor();
        proc.preProcessFetchedDocuments(new File(args[0]), new File(args[1]));

    }
    
    public void preProcessFetchedDocuments(final File inputDirectory, final File outputDirectory) {
        Observable.from(inputDirectory.list())
        .map(new Func1<String, File>() {

            @Override
            public File call(String fileName) {
                return new File(inputDirectory.getPath() + File.separator + fileName);
            }
            
        })
        .map(new AllrisHtmlToRawDrucksache())
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
