package info.jejking.hamburg.nord.geocoder.hh;

import org.neo4j.graphdb.RelationshipType;

public enum GazetteerRelationshipTypes implements RelationshipType {
    CONTAINS,
    CONTAINED_IN;
}