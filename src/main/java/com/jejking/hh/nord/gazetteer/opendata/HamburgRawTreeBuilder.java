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
package com.jejking.hh.nord.gazetteer.opendata;


import java.io.IOException;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.jejking.hh.nord.UniversalNamespaceCache;
import com.jejking.hh.nord.gazetteer.GazetteerEntryTypes;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

/**
 * Class to build a simple tree of nodes extracted from the WFS XML files obtained from the Hamburg Open Data portal.
 * Each node consists of a name, a string representing a list of EPSG coordinate pairs and a map of children.
 * 
 * <p>The class assumes that the data files are held locally as resources accessible on the classpath. They are included
 * in the distribution as per Hamburg Open Data License. Copyright of these belongs to Freie und Hansestadt Hamburg.</p>
 * 
 * @author jejking
 * 
 */
public class HamburgRawTreeBuilder {

    private final DocumentBuilderFactory documentBuilderFactory;
    private final XPathFactory xPathFactory;
    private final Properties boroughLookUp;
    
    /**
     * Constructor. Loads the mapping between <i>Bezirk</i> IDs and names.
     */
    public HamburgRawTreeBuilder() {
        this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
        this.documentBuilderFactory.setNamespaceAware(true);
        
        this.xPathFactory = XPathFactory.newInstance();
        this.boroughLookUp = loadBezirkProperties();
    }
    
    /**
     * Constructs a hierarchical tree of nodes:
     * <ul>
     * <li>the city</li>
     * <li>the borough</li>
     * <li>the official named areas</li>
     * <li>the official numbered districts</li>
     * </ul>
     * 
     * <p>Each is labelled with its name and provides a raw polygon string
     * that ultimately represents a list of of co-ordinate pairs from the 
     * coordinate reference system EPSG 25832.</p>
     *  
     * @return root node with children
     */
    public AdminAreaTreeNode<String> buildRawTree() {
        // root node. We haven't defined a boundary here, as it's not needed for our purposes.
        AdminAreaTreeNode<String> hamburg = new AdminAreaTreeNode<String>("Hamburg", 
                                                           GazetteerEntryTypes.CITY, 
                                                           "");
        
        addBoroughs(hamburg);
        addNamedAreas(hamburg);
        addNumberedDistricts(hamburg);

        return hamburg;
    }

    private void addBoroughs(AdminAreaTreeNode<String> hamburg) {
        
        Document boroughsDocument = getDocument("/bezirke.xml");
        
        /*
         * There are 7 boroughs, the xpath *should* give 14 elements,
         * alternating the ID and the polygon data.
         * 
         * But - it returns 15, there are two polygons for Wandsbek. Not clear why.
         */
        NodeList data = evaluateXPath(
                        "//gml:featureMember/INSPIRE:Bezirk/INSPIRE:OBJECTID/text() | //gml:posList/text()",
                        boroughsDocument);
        
        // stride through the node list in intervals of 2
        for (int i = 0; i < data.getLength(); i = i + 2) {
            String id = data.item(i).getTextContent();
            String name = boroughLookUp.getProperty(id);
            String polygon = data.item(i + 1).getTextContent().trim();
            AdminAreaTreeNode<String> boroughNode = new AdminAreaTreeNode<String>(name, GazetteerEntryTypes.BOROUGH, polygon);
            hamburg.getChildren().put(name, boroughNode);
            
            // nasty workaround needed as Wandsbek turns out to have two polygons
            // so we skip the second one for now...
            if (i + 2 < data.getLength() && data.item(i + 2).getTextContent().length() != 1) {
                i++;
            }
        }
    }
    
    private void addNamedAreas(AdminAreaTreeNode<String> hamburg) {
        Document namedAreasDocument = getDocument("/stadtteile.xml");
                
        /*
         * There are 104 named areas, this *should* returns 312 elements:
         * -> polygon, area name, borough name.
         * It actually returns 314 elements as Duvenstedt and Volksdorf are given two polygons.
         */
        NodeList data = evaluateXPath(
                        "//gml:featureMember/INSPIRE:Stadtteile/INSPIRE:Stadtteil/text() | //gml:posList/text() | //gml:featureMember/INSPIRE:Stadtteile/INSPIRE:Bezirk/text()",
                        namedAreasDocument);

        for (int i = 0; i < data.getLength(); i = i + 3) {
            String polygon = data.item(i).getTextContent().trim();
            
            // if we get another polygon (identified as starting with a digit), then skip forward...
            if (data.item(i + 1).getTextContent().trim().matches("\\d(.)*")) {
                i++; // we've got either Duvenstedt or Volksdorf, skip the extra polygon...
            }
            String areaName = normalisePauliAndGeorg(data.item(i + 1).getTextContent());
            String boroughName = data.item(i + 2).getTextContent();
            
            AdminAreaTreeNode<String> namedAreaNode = new AdminAreaTreeNode<String>(areaName, GazetteerEntryTypes.NAMED_AREA, polygon);
            AdminAreaTreeNode<String> boroughNode = hamburg.getChildren().get(boroughName);
            boroughNode.getChildren().put(areaName, namedAreaNode);
        }
        
    }
    
    /*
     * We need to do this as the numbered districts refer to the named areas
     * St. Pauli and St. Georg (with whitespace) whereas the named areas list
     * omits the whitespace. The version with whitespace is the correct one
     * in normal usage.
     */
    private String normalisePauliAndGeorg(final String name) {
        
        String normalised = name;
        
        if (normalised.equals("St.Pauli")) {
            normalised = "St. Pauli"; // with space!
        }
        
        if (normalised.equals("St.Georg")) {
            normalised = "St. Georg";
        }
        
        return normalised;
    }

    private void addNumberedDistricts(AdminAreaTreeNode<String> hamburg) {
        Document numberedDistrictsDocument = getDocument("/ortsteile.xml");
        
        /*
         * There are 180 numbered Ortsteile, this *should* returns 540 elements: 
         *          -> polygon, area name, numbered district.
         * 
         * It actually returns 542 elements as the numbered districts for Duvenstedt and Volksdorf 
         * are given two polygons.
         */
        NodeList data = evaluateXPath(
              "//gml:featureMember/INSPIRE:Ortsteile/INSPIRE:Stadtteil/text() | //gml:posList/text() | //gml:featureMember/INSPIRE:Ortsteile/INSPIRE:Ortsteilnummer/text()",
              numberedDistrictsDocument);

        for (int i = 0; i < data.getLength(); i = i + 3) {
            String polygon = data.item(i).getTextContent().trim();
            
            // if we get another polygon (identified as starting with more than 3 digits)
            if (data.item(i + 1).getTextContent().trim().matches("\\d{4,}(.)*")) {
                i++; // we've got either Duvenstedt or Volksdorf, skip the extra polygon...
            }
            
            String areaName = data.item(i + 1).getTextContent();
            String districtNumber = data.item(i + 2).getTextContent();

            
            AdminAreaTreeNode<String> numberedDistrictNode = new AdminAreaTreeNode<String>(districtNumber, GazetteerEntryTypes.NUMBERED_DISTRICT, polygon);
            
            // the first digit of the Ortsteil number encodes the borough, you have to know this ;)
            String boroughName = this.boroughLookUp.getProperty(districtNumber.substring(0, 1));
            AdminAreaTreeNode<String> boroughNode = hamburg.getChildren().get(boroughName);
            AdminAreaTreeNode<String> namedAreaNode = boroughNode.getChildren().get(areaName);
            namedAreaNode.getChildren().put(districtNumber, numberedDistrictNode);    
        }
        
    }

    // - util methods
    
    private Document getDocument(String path) {
        try {
            DocumentBuilder documentBuilder = this.documentBuilderFactory.newDocumentBuilder();
            Document doc = documentBuilder.parse(HamburgRawTreeBuilder.class.getResourceAsStream(path));
            return doc;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Properties loadBezirkProperties() {
        Properties bezirkProps = new Properties();
        try {
            bezirkProps.load(HamburgRawTreeBuilder.class.getResourceAsStream("/bezirke.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bezirkProps;
    }
    
    private NodeList evaluateXPath(String expressionString, Document document) {
        try {
            XPath xpath = this.xPathFactory.newXPath();
            xpath.setNamespaceContext(new UniversalNamespaceCache(document, true));
            
            XPathExpression xPathExpression = xpath.compile(expressionString);
            
            return (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
}
