package info.jejking.hamburg.nord.drucksachen.importer;

import java.util.ArrayList;
import java.util.List;

import info.jejking.hamburg.nord.drucksachen.allris.RawDrucksache;
import info.jejking.hamburg.nord.drucksachen.matcher.DrucksachenGazetteerKeywordMatcher;
import info.jejking.hamburg.nord.drucksachen.matcher.GazetteerKeywordMatcher;
import info.jejking.hamburg.nord.drucksachen.matcher.RawDrucksacheWithMatchesOfType;
import info.jejking.hamburg.nord.geocoder.AbstractNeoImporter;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

public class DrucksachenImporter extends AbstractNeoImporter<RawDrucksache> {

    private final ImmutableList<DrucksachenGazetteerKeywordMatcher> matchers;
    
    public DrucksachenImporter(ImmutableList<DrucksachenGazetteerKeywordMatcher> matchers) {
        this.matchers = checkNotNull(matchers);
    }

    @Override
    public void writeToNeo(RawDrucksache rawDrucksache, GraphDatabaseService graph) {
        RawDrucksache enhanced = attemptToAddDateIfNecessary(rawDrucksache);
        List<RawDrucksacheWithMatchesOfType> matchingResults = new ArrayList<>(this.matchers.size());
        for (DrucksachenGazetteerKeywordMatcher matcher : matchers) {
            matchingResults.add(matcher.call(enhanced));
        }
        
        try (Transaction tx = graph.beginTx()) {
            Node druckSachenNode = writeDrucksache(enhanced);
            
            for (RawDrucksacheWithMatchesOfType match : matchingResults) {
                for (String headerMatch : match.getMatchesInHeader()) {
                    buildRelationshipToNamedGazetteerEntry(druckSachenNode, headerMatch, match.getEntryType(), "HEADER");
                }
                
                for (String bodyMatch : match.getMatchesInBody()) {
                    buildRelationshipToNamedGazetteerEntry(druckSachenNode, bodyMatch, match.getEntryType(), "BODY");
                }
            }
            
            tx.success();
        } 
        
    }

    private void buildRelationshipToNamedGazetteerEntry(Node druckSachenNode, String bodyMatch, String entryType,
            String string) {
        // TODO Auto-generated method stub
        
    }

    private Node writeDrucksache(RawDrucksache enhanced) {
        // TODO Auto-generated method stub
        return null;
    }

    private RawDrucksache attemptToAddDateIfNecessary(RawDrucksache rawDrucksache) {
        // TODO Auto-generated method stub
        return null;
    }

}
 