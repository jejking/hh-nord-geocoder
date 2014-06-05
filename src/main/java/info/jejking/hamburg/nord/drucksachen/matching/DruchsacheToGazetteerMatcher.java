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
package info.jejking.hamburg.nord.drucksachen.matching;

import info.jejking.hamburg.nord.geocoder.GazetteerEntryTypes;

import java.util.Collection;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Class to find Gazetteer matches in <i>Drucksachen</i> text. 
 * 
 * @author jejking
 */
public class DruchsacheToGazetteerMatcher {

	private final GazetteerEntryTypes gazetteerEntryType;
	private final Trie ahoCorasickTrie;
	
	/**
	 * Constructor. Pass in the names to match against from which the Aho-Corasick
	 * trie is assembled for the matching automaton.
	 * 
	 * @param gazetteerEntryType
	 * @param gazetteerNames
	 */
	public DruchsacheToGazetteerMatcher(GazetteerEntryTypes gazetteerEntryType, ImmutableList<String> gazetteerNames) {
		
		this.gazetteerEntryType = gazetteerEntryType;
		this.ahoCorasickTrie = new Trie().caseInsensitive().removeOverlaps();
		for (String gazetteerName : gazetteerNames) {
			this.ahoCorasickTrie.addKeyword(gazetteerName);
		}
		
	}
	
	/**
	 * Finds those keywords from the gazetteer which are in the 
	 * input text.
	 * 
	 * @param in
	 * @return
	 */
	public ImmutableSet<String> findMatches(String in) {
		Collection<Emit> emits = this.ahoCorasickTrie.parseText(in);
		ImmutableSet.Builder<String> builder = ImmutableSet.builder();
		for (Emit emit : emits) {
			builder.add(emit.getKeyword());
		}
		return builder.build();
	}
	
	public GazetteerEntryTypes getGazetteerEntryType() {
		return gazetteerEntryType;
	}
}
