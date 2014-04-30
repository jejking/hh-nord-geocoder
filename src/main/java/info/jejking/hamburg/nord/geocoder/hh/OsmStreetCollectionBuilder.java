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
package info.jejking.hamburg.nord.geocoder.hh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.geotools.geometry.jts.JTSFactoryFinder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * Class to 
 * @author jejking
 *
 */
public class OsmStreetCollectionBuilder {

    private final HashMap<Long, Point> osmNodes = new HashMap<>();
    private final HashMap<String, Geometry> osmNamedStreets = new HashMap<>();
    private final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
    
    public Map<String, Geometry> buildRawStreetCollection() {
    
        parseFile();
        return this.osmNamedStreets;
    }

    
    private Point buildPoint(double lat, double lon) {
        
        return geometryFactory.createPoint(new Coordinate(lon, lat));
    }

    Map<Long, Point> getOsmNodes() {
        return this.osmNodes;
    }
    
    
    private void parseFile() {
        XMLInputFactory f = XMLInputFactory.newInstance();
        
        try {
            XMLEventReader eventReader = f.createXMLEventReader(OsmStreetCollectionBuilder.class.getResourceAsStream("/highwaysNoVersionsNoRelationsWithNamesOnly.osm"));
            while (eventReader.hasNext()) {
            	if (eventReader.peek().isEndDocument()) {
            		break;
            	}
            	XMLEvent xmlEvent = eventReader.nextTag();
            	if (xmlEvent.isStartElement()) {
            		StartElement startElement = xmlEvent.asStartElement();
                	String elementName = startElement.getName().getLocalPart();
                	switch (elementName) {
                		case "node" : handleNodeElement(startElement, eventReader);
                						break;
                		case "way" : handleWayElement(startElement, eventReader);
                						break;
                	    default: continue;
                	}
            	} else {
            		continue;
            	}
            	
            }
            
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

	private void handleWayElement(StartElement startElement, XMLEventReader eventReader) {
		try {
			List<Long> ndList = new ArrayList<>();
			String name = null;
			// expect set of nd elements, then tag elements
			XMLEvent xmlEvent = null;
			do {
				xmlEvent = eventReader.nextTag();
				if (xmlEvent.isStartElement()) {
					StartElement el = xmlEvent.asStartElement();
					switch(el.getName().getLocalPart()) {
						case "nd" : handleNdElement(ndList, el);
										break;
						case "tag" : if (name == null) name = handleTagELement(el);
										break;
						default: continue;
					}
					
				}
				
			} while (!xmlEvent.isEndElement()
					|| (xmlEvent.isEndElement() && !xmlEvent.asEndElement()
							.getName().getLocalPart().equals("way")));
			
			handleWay(ndList, name);
			
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
		
	}


	private void handleWay(List<Long> ndList, String name) {
		LineString wayLineString = buildLineString(ndList, name);
		
		if (this.osmNamedStreets.containsKey(name)) {
			
			this.osmNamedStreets.put(name, wayLineString.union(this.osmNamedStreets.get(name)));
		} else {
			this.osmNamedStreets.put(name, wayLineString);
		}
	}


	private LineString buildLineString(List<Long> ndList, String name) {
		List<Point> pointList = new ArrayList<>(ndList.size());
		for (Long osmId : ndList) {
			Point point = this.osmNodes.get(osmId);
			if (point != null) {
				pointList.add(point);
			}
		}
		
		// create a line string from the nodes....
		Coordinate[] coordinates = new Coordinate[pointList.size()];
		
		for (int i = 0; i < pointList.size(); i++) {
			coordinates[i] = pointList.get(i).getCoordinate();
		}
		if (coordinates.length == 1) {
			System.err.println("Only one node for way: " + name);
		}
		return this.geometryFactory.createLineString(coordinates);
	}


	private String handleTagELement(StartElement el) {
		if (el.getAttributeByName(new QName("k")).getValue().equals("name")) {
			return el.getAttributeByName(new QName("v")).getValue();
		} else {
			return null;
		}
	}


	private void handleNdElement(List<Long> ndList, StartElement el) {
		ndList.add(Long.parseLong(el.getAttributeByName(new QName("ref")).getValue()));
	}

	private void handleNodeElement(StartElement startElement, XMLEventReader eventReader) {
		
		Long osmId = Long.valueOf(startElement.getAttributeByName(new QName("id")).getValue());
		
		double lat = Double.parseDouble(startElement.getAttributeByName(new QName("lat")).getValue());
        double lon = Double.parseDouble(startElement.getAttributeByName(new QName("lon")).getValue());
        
        Point prev = this.osmNodes.put(osmId, buildPoint(lat, lon));
        if (prev != null) {
            System.err.println("unexpected duplicate osm node: " + osmId);
        }
        
        try {
			while(!eventReader.peek().isEndElement() || (eventReader.peek().isEndElement() && !eventReader.peek().asEndElement().getName().getLocalPart().equals("node"))) {
				eventReader.nextEvent();
			}
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}

	}
    
    
    
}
