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
package info.jejking.hamburg.nord.geocoder.osm;

import info.jejking.hamburg.nord.geocoder.osm.OsmRelation.Member;
import info.jejking.hamburg.nord.geocoder.osm.OsmRelation.Member.MemberType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Parser for Open Street Map XML. The class exposes {@link SimpleObservable} instances
 * for {@link OsmNode}, {@link OsmWay} and {@link OsmRelation} classes. Clients should use
 * these to register {@link SimpleObserver} instances. They should then call the 
 * {@link OsmParser#parseOsmStream(InputStream)}, supplying an input stream that delivers
 * the XML. 
 * 
 * <p>The parser notifies the observers (synchronously) as each object is generated from the
 * underlying XML.</p>
 * 
 * <p>The class should not be considered thread safe.</p>
 * 
 * @author jejking
 *
 */
public class OsmParser  {

    private final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        
    private final SimpleObservable<OsmNode> osmNodeObservable = new SimpleObservable<>();
    private final SimpleObservable<OsmWay>  osmWayObservable = new SimpleObservable<>();
    private final SimpleObservable<OsmRelation>  osmRelationObservable = new SimpleObservable<>();
    
    /**
     * Parses the XML, notifying the relevant {@link SimpleObservable} instances
     * as each relevant element is completely parsed.
     * 
     * @param inputStream stream containing OpenStreetMap XML, should not be <code>null</code>
     */
    public void parseOsmStream(InputStream inputStream) {
        XMLInputFactory f = XMLInputFactory.newInstance();
        try {
            XMLEventReader filtered = f.createFilteredReader(f.createXMLEventReader(inputStream), new EventFilter() {
                
                final List<String> interestingElements = ImmutableList.of("node", "way", "relation", "tag", "nd", "member");
                
                boolean isInterestingStartElement(StartElement startElement) {
                    String name = startElement.getName().getLocalPart();
                    return interestingElements.contains(name);
                }
                
                private boolean isInterestingEndElement(EndElement endElement) {
                    String name = endElement.getName().getLocalPart();
                    return interestingElements.contains(name);
                }
                
                // OSM only uses attributes and child elements, we can discard other events
                @Override
                public boolean accept(XMLEvent event) {
                    
                    if (event.isStartElement()) {
                        return isInterestingStartElement(event.asStartElement()); 
                    }
                    
                    if (event.isEndElement()) {
                        return isInterestingEndElement(event.asEndElement());
                    }
                    
                    if (event.isAttribute()) {
                        return true;
                    }

                    return false;
                    
                }

    
            });
            
            IteratorToObservable<OsmNode> nodeIteratorToObservable = new IteratorToObservable<>(new NodeIterator(filtered, geometryFactory), osmNodeObservable);
            IteratorToObservable<OsmWay> wayIteratorToObservable = new IteratorToObservable<>(new WayIterator(filtered), osmWayObservable);
            IteratorToObservable<OsmRelation> relationIteratorToObservable = new IteratorToObservable<>(new RelationIterator(filtered), osmRelationObservable);
            
            nodeIteratorToObservable.iterateAndNotify();
            wayIteratorToObservable.iterateAndNotify();
            relationIteratorToObservable.iterateAndNotify();
            
            filtered.close();
            inputStream.close();
        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Obtains the observable for {@link OsmNode}. Use this
     * to register {@link SimpleObserver} instances to handle nodes.
     * 
     * @return observable
     */
    public SimpleObservable<OsmNode> getOsmNodeObservable() {
        return osmNodeObservable;
    }
    
    /**
     * Obtains the observable for {@link OsmWay}. Use this 
     * to register {@link SimpleObserver} instances to handle ways.
     * 
     * @return observable
     */
    public SimpleObservable<OsmWay> getOsmWayObservable() {
        return osmWayObservable;
    }
    
    /**
     * Obtains the observable for {@link OsmRelation}. Use this 
     * to register {@link SimpleObserver} instances to handle relations.
     * 
     * @return observable
     */
    public SimpleObservable<OsmRelation> getOsmRelationObservable() {
        return osmRelationObservable;
    }
    
    /**
     * Bridges iterators to {@link SimpleObservable}.
     * 
     * @param <T>
     */
    private static class IteratorToObservable<T> {
        
        private final Iterator<T> iterator;
        private final SimpleObservable<T> observable;
        
        /**
         * Constructor.
         * @param iterator the iterator to iterate over
         * @param observable the observable to notify with objects extracted from the iterator
         */
        public IteratorToObservable(Iterator<T> iterator, SimpleObservable<T> observable) {
            super();
            this.iterator = iterator;
            this.observable = observable;
        }
        
        /**
         * Goes through the iterator completely, notifying the observer on
         * each object extrated from the iterator.
         */
        public void iterateAndNotify() {
            try {
                while (iterator.hasNext()) {
                    observable.next(iterator.next());
                }
            } catch (Exception e) {
                observable.error(e);
            }
            observable.completed();
        }
    }
    
    private static abstract class XmlEventAbstractIterator<T> extends AbstractIterator<T> {
        
        private static final class StringToLong implements Function<String, Long> {

            public Long apply(String in) {
                return Long.valueOf(in);
            }
        }

        protected static final StringToLong stringToLong = new StringToLong();
        
        protected static final QName id = new QName("id");
        protected static final QName version = new QName("version");
        protected static final QName timestamp = new QName("timestamp");
        protected static final QName changeset = new QName("changeset");
        protected static final QName uid = new QName("uid");
        protected static final QName user = new QName("user");
        
        protected static final QName ref = new QName("ref");
        
        protected final XMLEventReader xmlEventReader;
        protected final String elementName;
        
        
        
        public XmlEventAbstractIterator(XMLEventReader xmlEventReader, String elementName) {
            this.xmlEventReader = xmlEventReader;
            this.elementName = elementName;
        }
        
        private boolean nextElementIs(String elementName) {
            try {
                if (xmlEventReader.peek() != null && xmlEventReader.peek().isStartElement()) {
                    StartElement startElement = xmlEventReader.peek().asStartElement();
                    if (startElement.getName().getLocalPart().equals(elementName)) {
                        return true;
                    }
                }
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
            return false;
        }
        
        protected String getMandatoryAttributeFromElement(StartElement element, QName name) {
            Attribute attr = element.getAttributeByName(name);
            if (attr == null) {
                throw new IllegalStateException("Element " + element.getName() + " missing attribute" + name);
            }
            return attr.getValue();
        }
        
        protected Optional<String> getOptionalAttributeFromElement(StartElement element, QName name) {
            Attribute attr = element.getAttributeByName(name);
            if (attr == null) {
                return Optional.absent();
            } else {
                return Optional.of(attr.getValue());
            }
        }
        
        protected OsmMetadata buildOsmMetaFromElement(StartElement startElement) {
            
            Long  idValue = Long.valueOf(getMandatoryAttributeFromElement(startElement, id));
            Optional<Long> versionValue = getOptionalAttributeFromElement(startElement, version).transform(stringToLong);
            
            Optional<DateTime> timestampValue = getOptionalAttributeFromElement(startElement, timestamp).transform(new Function<String, DateTime>() {
                public DateTime apply(String in) {
                    return ISODateTimeFormat.basicDateTimeNoMillis().parseDateTime(in);
                }
            });
            
            Optional<Long> changesetValue = getOptionalAttributeFromElement(startElement, changeset).transform(stringToLong);
            Optional<Long> uidValue = getOptionalAttributeFromElement(startElement, uid).transform(stringToLong);
            Optional<String> userValue = getOptionalAttributeFromElement(startElement, user);
            
            OsmMetadataHolder holder = new OsmMetadataHolder(idValue, 
                                                             versionValue, 
                                                             timestampValue, 
                                                             changesetValue,
                                                             uidValue,
                                                             userValue);
            return holder;
        }
        
        @Override
        protected final T computeNext() {
            while (nextElementIs(this.elementName)) {
                return buildNext();
            }
            return endOfData();
        }
        
        abstract T buildNext();

        protected ImmutableMap<String, String> buildProperties() {
            ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
            TagIterator tagIterator = new TagIterator(xmlEventReader);
            while (tagIterator.hasNext()) {
                OsmTag tag = tagIterator.next();
                mapBuilder.put(tag.getKey(), tag.getValue());
            }
            return mapBuilder.build();
        }
        
    }

    private static class TagIterator extends XmlEventAbstractIterator<OsmTag> {

        private static final QName k = new QName("k");
        private static final QName v = new QName("v");
        
        public TagIterator(XMLEventReader xmlEventReader) {
            super(xmlEventReader, "tag");
        }

        @Override
        OsmTag buildNext() {
            try {
                StartElement startElement = xmlEventReader.nextTag().asStartElement();
                String key = startElement.getAttributeByName(k).getValue();
                String value = startElement.getAttributeByName(v).getValue();
                xmlEventReader.nextTag(); // and ignore...
                return new OsmTag(key, value);
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
            
        }
        
    }
    
    
    private static class NodeIterator extends XmlEventAbstractIterator<OsmNode> {

        private GeometryFactory geometryFactory;
        
        private static final QName lat = new QName("lat");
        private static final QName lon = new QName("lon");
        
        public NodeIterator(XMLEventReader xmlEventReader, GeometryFactory geometryFactory) {
            super(xmlEventReader, "node");
        }

        @Override
        OsmNode buildNext() {
            
            try {
                StartElement nodeStartElement = xmlEventReader.nextTag().asStartElement();
                OsmMetadata metadata = buildOsmMetaFromElement(nodeStartElement);
                Point point = buildPoint(nodeStartElement);
                ImmutableMap<String, String> properties = buildProperties();
                xmlEventReader.nextTag(); // go to end of element, discard event
                return new OsmNode(metadata, properties, point);
            } catch (XMLStreamException e) {
                throw new RuntimeException();
            }
        }

        private Point buildPoint(StartElement nodeStartElement) {
            double latValue = Double.parseDouble(getMandatoryAttributeFromElement(nodeStartElement, lat));
            double lonValue = Double.parseDouble(getMandatoryAttributeFromElement(nodeStartElement, lon));
            
            return geometryFactory.createPoint(new Coordinate(lonValue, latValue));
        }

        
    }
    
    private static class NdIterator extends XmlEventAbstractIterator<Long> {

        
        
        public NdIterator(XMLEventReader xmlEventReader) {
            super(xmlEventReader, "nd");
        }

        @Override
        Long buildNext() {
            try {
                StartElement startElement = xmlEventReader.nextTag().asStartElement();
                String refValue = startElement.getAttributeByName(ref).getValue();
                xmlEventReader.nextTag(); // and ignore...
                return Long.valueOf(refValue);
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
        }
        
    }
    
    private static class MemberIterator extends XmlEventAbstractIterator<OsmRelation.Member> {

        private static final QName type = new QName("type");
        private static final QName role = new QName("role");
        
        public MemberIterator(XMLEventReader xmlEventReader) {
            super(xmlEventReader, "member");
        }

        @Override
        Member buildNext() {
            try {
                StartElement nodeStartElement = xmlEventReader.nextTag().asStartElement();
                OsmRelation.Member.MemberType typeValue = buildType(nodeStartElement);
                Long refValue = buildRef(nodeStartElement);
                Optional<String> roleValue = buildRole(nodeStartElement);
                xmlEventReader.nextTag(); // go to end of element, discard event
                return new OsmRelation.Member(typeValue, refValue, roleValue);
            } catch (XMLStreamException e) {
                throw new RuntimeException();
            }
        }

        private Long buildRef(StartElement nodeStartElement) {
            String refValue = getMandatoryAttributeFromElement(nodeStartElement, ref);
            return Long.valueOf(refValue);
        }

        private MemberType buildType(StartElement nodeStartElement) {
            String typeValue = getMandatoryAttributeFromElement(nodeStartElement, type);
            if (typeValue.equals("node")) {
                return MemberType.NODE;
            }
            if (typeValue.equals("way")) {
                return MemberType.WAY;
            }
            throw new IllegalArgumentException("Relation type " + typeValue + " not expected");
        }

        private Optional<String> buildRole(StartElement nodeStartElement) {
            Optional<String> roleValue = getOptionalAttributeFromElement(nodeStartElement, role);
            if (roleValue.isPresent() && roleValue.get().isEmpty()) {
                roleValue = Optional.absent();
            }
            return roleValue;
        }
        
    }
    
    
    private static class WayIterator extends XmlEventAbstractIterator<OsmWay> {

        public WayIterator(XMLEventReader xmlEventReader) {
            super(xmlEventReader, "way");
        }

        @Override
        OsmWay buildNext() {
            try {
                StartElement wayStartElement = xmlEventReader.nextTag().asStartElement();
                OsmMetadata metadata = buildOsmMetaFromElement(wayStartElement);
                
                ImmutableList.Builder<Long> ndListBuilder = ImmutableList.builder();
                
                NdIterator ndIterator = new NdIterator(xmlEventReader);
                while(ndIterator.hasNext()) {
                    Long ndVal = ndIterator.buildNext();
                    ndListBuilder.add(ndVal);
                }
                
                ImmutableMap<String, String> properties = buildProperties();
                xmlEventReader.nextTag(); // go to end of element, discard event
                return new OsmWay(metadata, properties, ndListBuilder.build());
            } 
            catch (XMLStreamException e) {
                throw new RuntimeException();
            }
        }
        
    }
    
    private static class RelationIterator extends XmlEventAbstractIterator<OsmRelation> {

        public RelationIterator(XMLEventReader xmlEventReader) {
            super(xmlEventReader, "relation");
        }

        @Override
        OsmRelation buildNext() {
            try {
                StartElement relationStartElement = xmlEventReader.nextTag().asStartElement();
                OsmMetadata metadata = buildOsmMetaFromElement(relationStartElement);
                
                ImmutableList.Builder<OsmRelation.Member> mbrListBuilder = ImmutableList.builder();
                
                MemberIterator memberIterator = new MemberIterator(xmlEventReader);
                while(memberIterator.hasNext()) {
                    OsmRelation.Member member = memberIterator.next();
                    mbrListBuilder.add(member);
                }
                
                ImmutableMap<String, String> properties = buildProperties();
                xmlEventReader.nextTag(); // go to end of element, discard event
                
                return new OsmRelation(metadata, properties, mbrListBuilder.build());
                
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
        }


        
    }
    
}
