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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import rx.functions.Func1;

import com.google.common.collect.ImmutableSet;

/**
 * Function, that given a set of keywords to look for (in our case a list of street names,
 * named points of interest or named administrative districts), identifies those which
 * can be found in a text supplied to the function as a parameter.
 * 
 * <p>The function essentially wraps an implementation of the Aho-Corasick algorithm.</p>
 * 
 * @author jejking
 *
 */
public class GazetteerKeywordMatcher implements Func1<String, ImmutableSet<String>> {

    private final Trie ahoCorasickTrie;
    private final String entryType;
    
    /**
     * Constructor.
     * 
     * @param keyWords non-null iterable of strings used to build the internal {@link Trie} structure.
     * @param entryType non-null label stating what type of gazetteer entry the strings designate. 
     */
    public GazetteerKeywordMatcher(Iterable<String> keyWords, String entryType) {
        
        this.ahoCorasickTrie = new Trie().onlyWholeWords().removeOverlaps();
        for (String keyWord : checkNotNull(keyWords)) {
            this.ahoCorasickTrie.addKeyword(keyWord);
        }
        
        this.entryType = checkNotNull(entryType);
        
    }
    
    
    @Override
    public ImmutableSet<String> call(String textToMatchAgainst) {
        
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        
        Collection<Emit> emittedMatches = this.ahoCorasickTrie.parseText(textToMatchAgainst);
        
        for (Emit emittedMatch : emittedMatches) {
            builder.add(emittedMatch.getKeyword());
        }
        
        
        return builder.build();
    }
    
    
    public String getEntryType() {
        return entryType;
    }


}
