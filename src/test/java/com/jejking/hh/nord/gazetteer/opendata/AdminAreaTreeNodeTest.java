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
package com.jejking.hh.nord.gazetteer.opendata;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import rx.functions.Func1;

/**
 * Tests for {@link AdminAreaTreeNode}.
 * 
 * @author jejking
 *
 */
public class AdminAreaTreeNodeTest {

	@Test
	public void fMapWorksAsExpected() {
		AdminAreaTreeNode<String> root = givenAnInputTree();
        
        AdminAreaTreeNode<Integer> rootInt = root.fmap(new Func1<String, Integer>() {

			@Override
			public Integer call(String t1) {
				return Integer.valueOf(t1);
			}
        	
        }).call(root);
        
        thenTheResultIsAsExpected(rootInt);
	}

	private void thenTheResultIsAsExpected(AdminAreaTreeNode<Integer> rootInt) {
		assertEquals(0, rootInt.getContent().intValue());
        assertEquals("root", rootInt.getName());
        
        Map<String, AdminAreaTreeNode<Integer>> rootIntChildren = rootInt.getChildren();
        assertEquals(2, rootIntChildren.size());
        
        AdminAreaTreeNode<Integer> oneInt = rootIntChildren.get("one");
        assertEquals(1, oneInt.getContent().intValue());
        
        AdminAreaTreeNode<Integer> twoInt = rootIntChildren.get("two");
        assertEquals(2, twoInt.getContent().intValue());
        
        AdminAreaTreeNode<Integer> tenInt = oneInt.getChildren().get("ten");
        assertEquals(10, tenInt.getContent().intValue());
	}

	private AdminAreaTreeNode<String> givenAnInputTree() {
		AdminAreaTreeNode<String> root = new AdminAreaTreeNode<String>("root", "foo", "0");
        AdminAreaTreeNode<String> one = new AdminAreaTreeNode<String>("one", "foo", "1");
        AdminAreaTreeNode<String> ten = new AdminAreaTreeNode<String>("ten", "foo", "10");
        AdminAreaTreeNode<String> two = new AdminAreaTreeNode<String>("two","foo", "2");
        
        root.getChildren().put("one", one);
        root.getChildren().put("two", two);
        
        one.getChildren().put("ten", ten);
		return root;
	}
	
}
