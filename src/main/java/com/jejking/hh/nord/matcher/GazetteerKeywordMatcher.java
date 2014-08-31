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
package com.jejking.hh.nord.matcher;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.HashMap;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import rx.functions.Func1;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Function that, given a set of keywords to look for (in our case a list of street names,
 * named points of interest or named administrative districts), identifies those which
 * can be found in a text supplied to the function as a parameter.
 * 
 * <p>The function essentially wraps an implementation of the Aho-Corasick algorithm.</p>
 * 
 * @author jejking
 *
 */
public final class GazetteerKeywordMatcher implements Func1<String, ImmutableMap<String, Integer>> {

    private final ImmutableSet<String> keywordSet;
    private final Trie ahoCorasickTrie;
    private final String entryType;
    
    /**
     * Constructor.
     * 
     * @param keyWords non-null iterable of strings used to build the internal {@link Trie} structure.
     * @param entryType non-null label stating what type of gazetteer entry the strings designate. 
     */
    public GazetteerKeywordMatcher(Iterable<String> keyWords, String entryType) {
        
        this.keywordSet = ImmutableSet.copyOf(checkNotNull(keyWords));
        this.ahoCorasickTrie = new Trie().onlyWholeWords().removeOverlaps();
        for (String keyWord : keywordSet) {
            this.ahoCorasickTrie.addKeyword(keyWord);
        }
        
        this.entryType = checkNotNull(entryType);
        
    }
    
    
    @Override
    public ImmutableMap<String, Integer> call(String textToMatchAgainst) {
        
        HashMap<String, Integer> tempMap = new HashMap<>();

        Collection<Emit> emittedMatches = this.ahoCorasickTrie.parseText(textToMatchAgainst);
        
        for (Emit emittedMatch : emittedMatches) {
            String keyword = emittedMatch.getKeyword();
            if (tempMap.containsKey(keyword)) {
                tempMap.put(keyword, tempMap.get(keyword) + 1);
            } else {
                tempMap.put(keyword, Integer.valueOf(1));
            }
        }
        return ImmutableMap.copyOf(tempMap);
    }
    
    
    public String getEntryType() {
        return entryType;
    }
    
    public ImmutableSet<String> getKeywordSet() {
        return keywordSet;
    }

}
