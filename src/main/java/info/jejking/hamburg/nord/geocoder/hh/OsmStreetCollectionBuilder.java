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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
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
    
        Document streetsDocument = loadDocument();
        buildOsmNodes(streetsDocument);
        
        return this.osmNamedStreets;
    }

    
    
    

    private void buildOsmNodes(Document streetsDocument) {
        NodeList osmNodeList = streetsDocument.getElementsByTagName("node");
        for (int i = 0; i < osmNodeList.getLength(); i++) {
            Node osmNode = osmNodeList.item(i);
            NamedNodeMap attrs = osmNode.getAttributes();
            Long osmId = Long.parseLong(((Attr)attrs.getNamedItem("id")).getValue());
            double lat = Double.parseDouble(((Attr)attrs.getNamedItem("lat")).getValue());
            double lon = Double.parseDouble(((Attr)attrs.getNamedItem("lon")).getValue());
            
            Point prev = this.osmNodes.put(osmId, buildPoint(lat, lon));
            if (prev != null) {
                System.err.println("unexpected duplicate osm node: " + osmId);
            }
            
        }
    }
    
    private Point buildPoint(double lat, double lon) {
        
        return geometryFactory.createPoint(new Coordinate(lon, lat));
    }


    private Document loadDocument() {
        
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);
        
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = documentBuilder.parse(OsmStreetCollectionBuilder.class.getResourceAsStream("/highwaysNoVersionsNoRelationsWithNamesOnly.osm"));
            return doc;    
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        
    }
    
    
    Map<Long, Point> getOsmNodes() {
        return this.osmNodes;
    }
    
    
    private void parseFile() {
        XMLInputFactory f = XMLInputFactory.newInstance();
        try {
            XMLEventReader eventReader = f.createXMLEventReader(OsmStreetCollectionBuilder.class.getResourceAsStream("/highwaysNoVersionsNoRelationsWithNamesOnly.osm"));
            
            
        } catch (XMLStreamException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    
}
