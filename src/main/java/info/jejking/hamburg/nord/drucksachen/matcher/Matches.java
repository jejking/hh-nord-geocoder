package info.jejking.hamburg.nord.drucksachen.matcher;

import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Matches {

    private final ImmutableSet<String> matchesInBody;
    private final ImmutableSet<String> matchesInHeader;
    
    /**
     * Constructor.
     * @param matchesInBody set of string matches in body, may be empty, never <code>null</code>
     * @param matchesInHeader set of string matches in header, may be empty, never <code>null</code>
     */
    public Matches(ImmutableSet<String> matchesInBody, ImmutableSet<String> matchesInHeader) {
        super();
        this.matchesInBody = checkNotNull(matchesInBody);
        this.matchesInHeader = checkNotNull(matchesInHeader);
    }

    
    public ImmutableSet<String> getMatchesInBody() {
        return matchesInBody;
    }
    
    
    public ImmutableSet<String> getMatchesInHeader() {
        return matchesInHeader;
    }
    
}