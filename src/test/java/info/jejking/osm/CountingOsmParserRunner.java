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
package info.jejking.osm;

import java.io.BufferedInputStream;
import java.io.FileInputStream;


public class CountingOsmParserRunner {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String fileName = args[0];
        
        EventCounter<OsmNode> nodeCounter = new EventCounter<>("node");
        EventCounter<OsmWay> wayCounter = new EventCounter<>("way");
        EventCounter<OsmRelation> relCounter = new EventCounter<>("relation");
        
        RxOsmParser parser = new RxOsmParser(new BufferedInputStream(new FileInputStream(fileName)));
        parser.getNodeObservable().subscribe(nodeCounter);
        parser.getWayObservable().subscribe(wayCounter);
        parser.getRelationObservable().subscribe(relCounter);
        parser.parseOsmStream();
        
        System.out.println("done");
        System.out.println("Nodes counted: " + nodeCounter.count);
        System.out.println("Ways counted: " + wayCounter.count);
        System.out.println("Relations counted: " + relCounter.count);
        
    }

}
