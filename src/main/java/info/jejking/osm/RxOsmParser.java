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

import static com.google.common.base.Preconditions.checkNotNull;
import info.jejking.osm.OsmRelation.Member;
import info.jejking.osm.OsmRelation.Member.MemberType;

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
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import rx.Observable;
import rx.observables.ConnectableObservable;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Streaming parser for Open Street Map XML that exposes Rx [@link Observable}s for the underlying
 * objects encoded as XML in the input stream.
 * 
 * <p>Usage: construct the class, passing an input stream as a parameter. Attach subscribers
 * or observers to the {@link Observable} instances exposed for each basic Open Street Map type.
 * Then call the {@link RxOsmParser#parseOsmStream()} method - this connects to each observer
 * in the right order (node, then way, then relation) as determined by the OSM XML conventions.
 *  
 * @author jejking
 *
 */
public class RxOsmParser  {

	private final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

	private final ConnectableObservable<OsmNode> nodeObservable;
	private final ConnectableObservable<OsmWay> wayObservable;
	private final ConnectableObservable<OsmRelation> relationObservable;
	
	
	private final XMLEventReader filtered;
	private final InputStream inputStream;
	
	public RxOsmParser(InputStream inputStream) {
		
		this.inputStream = checkNotNull(inputStream);
		XMLInputFactory f = XMLInputFactory.newInstance();
        try {
            this.filtered = getFilteredXmlEventReaderForStream(inputStream, f);
            
            NodeIterator nodeIterator = new NodeIterator(filtered, geometryFactory);
            WayIterator wayIterator = new WayIterator(filtered);
            RelationIterator relationIterator = new RelationIterator(filtered);
            
            this.nodeObservable = fromIterable(fromIterator(nodeIterator));
            this.wayObservable = fromIterable(fromIterator(wayIterator));
            this.relationObservable = fromIterable(fromIterator(relationIterator));
            
            
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
	}
	
	
    private <T> ConnectableObservable<T> fromIterable(Iterable<T> iterable) {
		Observable<T> observable = Observable.from(iterable);
		return observable.publish();
	}


	private <T> Iterable<T> fromIterator(final Iterator<T> iterator) {
		return new Iterable<T>() {

			@Override
			public Iterator<T> iterator() {
				return iterator;
			}
		};
	}

	public Observable<OsmNode> getNodeObservable() {
		return this.nodeObservable;
	}

	
	public Observable<OsmWay> getWayObservable() {
		return this.wayObservable;
	}

	public Observable<OsmRelation> getRelationObservable() {
		return this.relationObservable;
	}
	

	/**
     * Parses the XML, notifying the relevant {@link SimpleObservable} instances
     * as each relevant element is completely parsed.
     * 
     * @param inputStream stream containing OpenStreetMap XML, should not be <code>null</code>
     */
    public void parseOsmStream() {

    	this.nodeObservable.connect();
    	this.wayObservable.connect();
    	this.relationObservable.connect();
    	
    	try {
			filtered.close();
			inputStream.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    	
    }

	private XMLEventReader getFilteredXmlEventReaderForStream(
			InputStream inputStream, XMLInputFactory f)
			throws XMLStreamException {
		return f.createFilteredReader(f.createXMLEventReader(inputStream), new EventFilter() {
		    
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
	}
    
 
    
    /**
     * Gathers together functionality to build an iterator of higher level types
     * out of an xml event stream.
     *  
     * @param <T>
     */
    private static abstract class XmlEventAbstractIterator<T> extends AbstractIterator<T> {
        
        
        private static final class StringToDateTime implements Function<String, DateTime> {
          //    2014-05-14T14:12:39Z
            private static final DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
            public DateTime apply(String in) {
                return dateTimeFormat.parseDateTime(in).withZone(DateTimeZone.UTC);
            }
        }

        private static final class StringToLong implements Function<String, Long> {

            public Long apply(String in) {
                return Long.valueOf(in);
            }
        }

        protected static final StringToLong stringToLong = new StringToLong();
        protected static final StringToDateTime stringToDateTime = new StringToDateTime();
        
        protected static final QName id = new QName("id");
        protected static final QName version = new QName("version");
        protected static final QName timestamp = new QName("timestamp");
        protected static final QName changeset = new QName("changeset");
        protected static final QName uid = new QName("uid");
        protected static final QName user = new QName("user");
        
        protected static final QName ref = new QName("ref");
        
        protected final XMLEventReader xmlEventReader;
        protected final String elementName;
        
        
        /**
         * Constructor. The assumption is that a given type is associated with
         * an element of a given name which must be supplied so that the xml
         * event reader can be used by subclasses to build the type up from the underlying
         * events. The iterator keeps on returning type instances whilst elements
         * of the supplied name can be read from the event stream. If the next element
         * is named differently, or is the end of the document, then the iteration
         * is completed.
         * 
         * @param xmlEventReader
         * @param elementName
         */
        public XmlEventAbstractIterator(XMLEventReader xmlEventReader, String elementName) {
            this.xmlEventReader = xmlEventReader;
            this.elementName = elementName;
        }
        
        /**
         * Looks forward in the event stream - and checks if the next event (which should
         * be a start element) has the same name as was supplied to the iterator in
         * its constructor.
         * 
         * @param elementName
         * @return <code>true</code> if next element has same name, else <code>false</code>
         */
        private boolean nextElementHasSameName() {
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
        
        /**
         * Gets the value of the attribute from the element. It is expected to be there. In this
         * sense, the method is like a validity check on the XML in the absence of a formal schema.
         * 
         * @param element
         * @param name
         * @return value
         * @throws IllegalStateException if attribute missing.
         */
        protected String getMandatoryAttributeFromElement(StartElement element, QName name) {
            Attribute attr = element.getAttributeByName(name);
            if (attr == null) {
                throw new IllegalStateException("Element " + element.getName() + " missing attribute" + name);
            }
            return attr.getValue();
        }
        
        /**
         * Gets optional value. It may be there - or may have been filtered away. If not there,
         * then {@link Optional#absent()} is returned.
         * 
         * @param element
         * @param name
         * @return
         */
        protected Optional<String> getOptionalAttributeFromElement(StartElement element, QName name) {
            Attribute attr = element.getAttributeByName(name);
            if (attr == null) {
                return Optional.absent();
            } else {
                return Optional.of(attr.getValue());
            }
        }
        
        /**
         * Builds standard attribute based metadata from the element.
         * @param startElement
         * @return metadata
         */
        protected OsmMetadata buildOsmMetaFromElement(StartElement startElement) {
            
            Long  idValue = Long.valueOf(getMandatoryAttributeFromElement(startElement, id));
            Optional<Long> versionValue = getOptionalAttributeFromElement(startElement, version).transform(stringToLong);
            Optional<DateTime> timestampValue = getOptionalAttributeFromElement(startElement, timestamp).transform(stringToDateTime);
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
        
        /**
         * Assembles properties by iterating over nested "tag" elements.
         * @return immutable map of properties
         */
        protected ImmutableMap<String, String> buildProperties() {
            ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
            TagIterator tagIterator = new TagIterator(xmlEventReader);
            while (tagIterator.hasNext()) {
                OsmTag tag = tagIterator.next();
                mapBuilder.put(tag.getKey(), tag.getValue());
            }
            return mapBuilder.build();
        }
        
        @Override
        protected final T computeNext() {
            /*
             * While the next element has the same name, continue. Construction
             * of types is delegated to sub-classes.
             */
            while (nextElementHasSameName()) {
                return buildNext();
            }
            return endOfData();
        }
        
        /**
         * Assemble object from XML event stream reader.
         * @return
         */
        abstract T buildNext();

        
    }

    /**
     * Iterates over tag elements.
     */
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
                xmlEventReader.nextTag(); // and ignore end element...
                return new OsmTag(key, value);
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
            
        }
        
    }
    
    /**
     * Iterates over node elements. Constructs {@link Point} objects from the 
     * latitude and longitude pairs.
     *
     */
    private static class NodeIterator extends XmlEventAbstractIterator<OsmNode> {

        private final GeometryFactory geometryFactory;
        
        private static final QName lat = new QName("lat");
        private static final QName lon = new QName("lon");
        
        public NodeIterator(XMLEventReader xmlEventReader, GeometryFactory geometryFactory) {
            super(xmlEventReader, "node");
            this.geometryFactory = geometryFactory;
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
    
    /**
     * Iterates over the "nd" elements that are child elements of "way".
     * @author jejking
     */
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
    
    /**
     * Iterates over "member" sub-elements of "relation".
     */
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
            switch (typeValue) {
                case "node" : return MemberType.NODE;
                case "way": return MemberType.WAY;
                case "relation" : return MemberType.RELATION;
                default : throw new IllegalArgumentException("Relation type " + typeValue + " not expected");
            }
               
        }

        private Optional<String> buildRole(StartElement nodeStartElement) {
            Optional<String> roleValue = getOptionalAttributeFromElement(nodeStartElement, role);
            if (roleValue.isPresent() && roleValue.get().isEmpty()) {
                roleValue = Optional.absent();
            }
            return roleValue;
        }
        
    }
    
    /**
     * Iterates over "way" elements.
     */
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
                    Long ndVal = ndIterator.next();
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
    
    /**
     * Iterates over "relation" elements.
     */
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
