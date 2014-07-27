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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

/**
 * Class to download the binary content at a list of URLs to
 * local file storage pending further work. A delay between 2 and 10 seconds is introduced
 * to avoid hammering the target server.
 * 
 * <p>The URLs are hex-encoded to avoid any conflicts with file system paths, etc and
 * the downloaded content is gzipped to save space.</p>. 
 *
 * @author jejking
 *
 */
public class DrucksachenHtmlFetcher {

    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    
    private final long[] totalDelayHolder = new long[1];
    private final int[] actualCount = new int[1];
    
    /**
     * Schedules tasks to fetch all the specified URLs leaving a random duration between the 
     * execution of each task. 
     * 
     * @param urlsToFetch
     * @param storageDirectory
     */
	public void fetchUrls(final ImmutableList<URL> urlsToFetch, final Path storageDirectory) {
		
	    Observable<Runnable> tasks = Observable.from(urlsToFetch)
	        .filter(new Func1<URL, Boolean>() {

                @Override
                public Boolean call(URL url) {
                    String encodedUrl = fileNameFromUrl(url) + ".gz";
                    Path filePath = storageDirectory.resolve(encodedUrl);
                    // retain only URLs for which we have no record yet so as not to download them twice
                    return Files.notExists(filePath, LinkOption.NOFOLLOW_LINKS);
                }
	            
	        })
	        .map(new Func1<URL, Runnable>() {
	            @Override
	            public Runnable call(final URL url) {
                    return new Runnable() {
                        
                        @Override
                        public void run() {
                            
                            try {
                                File target = storageDirectory.resolve(fileNameFromUrl(url) + ".gz").toFile();
                                try(GzipCompressorOutputStream outputStream = new GzipCompressorOutputStream(
                                                                                new BufferedOutputStream(
                                                                                    new FileOutputStream(target)))) {
                                    Resources.copy(url, outputStream);  
                                    System.out.println("Copied " + url + " to " + target);
                                }
                                
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            
                        }
                    };
	                
	            };
	        });
	    
	        tasks.subscribe(new Action1<Runnable>() {

                Random random = new Random();
                long cumulativeDelayInSeconds = 0;
                int count = 0;
               
                @Override
                public void call(Runnable runnable) {
                    count++;
                    DrucksachenHtmlFetcher.this.scheduledExecutorService.schedule(runnable, cumulativeDelayInSeconds, TimeUnit.SECONDS);
                    // at least two seconds, at most 10
                    cumulativeDelayInSeconds = cumulativeDelayInSeconds + 2 + random.nextInt(9);
                    DrucksachenHtmlFetcher.this.totalDelayHolder[0] = cumulativeDelayInSeconds;
                    DrucksachenHtmlFetcher.this.actualCount[0] = count;
                }
            
        });
        
	    System.out.println("Scheduled " + actualCount[0] + " tasks");
	    System.out.println("Estimated duration " + totalDelayHolder[0] + " seconds");
	        
	    try {
	        this.scheduledExecutorService.shutdown();
	        // + 60 to allow task to finish comfortably...
            boolean finishedOK = this.scheduledExecutorService.awaitTermination(this.totalDelayHolder[0] + 60, TimeUnit.SECONDS);
            if (finishedOK) {
                System.out.println("Finished all tasks. Scheduled executor service shutdown.");
            } else {
                System.out.println("Executor service shutdown, but not all tasks completed.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
		
	}

    private String fileNameFromUrl(URL url) {
        return Hex.encodeHexString(url.toString().getBytes(Charsets.UTF_8));
    }
	
}
