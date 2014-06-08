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
package info.jejking.hamburg.nord.drucksachen.matcher;

import info.jejking.hamburg.nord.drucksachen.allris.RawDrucksache;
import info.jejking.hamburg.nord.geocoder.GazetteerEntryTypes;
import info.jejking.hamburg.nord.geocoder.osm.RxOsmStreetCollectionBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.geotools.geometry.jts.JTSFactoryFinder;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Class which shows how a directory full of serialised {@link RawDrucksache}
 * objects can be mapped to streets obtained using {@lnk RxOsmParser}.
 * 
 * @author jejking
 *
 */
public class DrucksachenToStreetsMapperDemo {

    /**
     * @param args directory to read from, directory to write to
     */
    public static void main(String[] args) throws Exception {
        
        RxOsmStreetCollectionBuilder streetCollectionBuilder = new RxOsmStreetCollectionBuilder(JTSFactoryFinder.getGeometryFactory());
        
        Map<String, Geometry> streets = streetCollectionBuilder.streetsFromStream(new BZip2CompressorInputStream(
                DrucksachenToStreetsMapperDemo.class.getResourceAsStream("/hamburg-nord-tm470.osm.bz2")));
        

        DrucksachenGazetteerKeywordMatcher matcher = new DrucksachenGazetteerKeywordMatcher(streets.keySet(), GazetteerEntryTypes.STREET);
        
        final File inputDirectory = new File(args[0]);
        final File outputDirectory = new File(args[1]);
        Observable.from(inputDirectory.list())
        .map(new Func1<String, File>() {

            @Override
            public File call(String fileName) {
                return new File(inputDirectory.getPath() + File.separator + fileName);
            }
            
        })
        .map(new Func1<File, RawDrucksache>() {

            @Override
            public RawDrucksache call(File file) {
                try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                    
                    RawDrucksache rawDrucksache = (RawDrucksache) ois.readObject();
                    return rawDrucksache;
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                
                
            }
            
        })
        .map(matcher)
        .subscribe(new Action1<RawDrucksacheWithMatchesOfType>() {

            @Override
            public void call(RawDrucksacheWithMatchesOfType t1) {
                File outputFile = new File(outputDirectory.getPath() + File.separator + t1.getOriginal().getDrucksachenId().replace("/", "-"));
                try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile))) {
                    for (String text : t1.getOriginal().getExtractedContent()) {
                        bufferedWriter.write(text);
                        bufferedWriter.write("\n");
                    }
                    bufferedWriter.write("Matched in Header: \n");
                    for (String matchedName : t1.getMatchesInHeader()) {
                        bufferedWriter.write(matchedName);
                        bufferedWriter.write("\n");
                    }
                    bufferedWriter.write("Matched in Body: \n");
                    for (String matchedName : t1.getMatchesInBody()) {
                        bufferedWriter.write(matchedName);
                        bufferedWriter.write("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }
            
        });
        
    }

}
