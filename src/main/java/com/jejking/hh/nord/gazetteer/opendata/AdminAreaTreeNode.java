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

import java.util.HashMap;
import java.util.Map;

import rx.functions.Action1;
import rx.functions.Func1;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Trivial class for creating a tree of named key-value pairs.
 * @author jejking
 *
 * @param <T> the type
 */
public class AdminAreaTreeNode<T> {

    private final String name;
    private final String type;
    private final HashMap<String, AdminAreaTreeNode<T>> children = new HashMap<String, AdminAreaTreeNode<T>>();
    private final T content;
    
    /**
     * Constructor.
     * @param name name of node, may not be <code>null</code> or empty
     * @param type indication of type of node, may not be <code>null</code> or empty
     * @param content the content, may not be <code>null</code>
     */
    public AdminAreaTreeNode(String name, String type, T content) {
        super();
        checkArgument(name != null && name.trim().length() > 0, name);
        checkArgument(name != null && name.trim().length() > 0, type);
        
        this.name = name;
        this.type = type;
        this.content = checkNotNull(content);
    }

    
    /**
     * @return the content
     */
    public T getContent() {
        return content;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * 
     * @return the type
     */
    public String getType() {
        return type;
    }
    
    /**
     * @return the children
     */
    public Map<String, AdminAreaTreeNode<T>> getChildren() {
        return children;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((children == null) ? 0 : children.hashCode());
        result = prime * result + ((content == null) ? 0 : content.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AdminAreaTreeNode)) {
            return false;
        }
        AdminAreaTreeNode<?> other = (AdminAreaTreeNode<?>) obj;
        if (children == null) {
            if (other.children != null) {
                return false;
            }
        } else if (!children.equals(other.children)) {
            return false;
        }
        if (content == null) {
            if (other.content != null) {
                return false;
            }
        } else if (!content.equals(other.content)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AdminAreaTreeNode [name=" + name + ", type=" + type + ", children=" + children + ", content=" + content + "]";
    }

    /**
     * Creates function to map over all tree from function to map over content types in tree, essentially making
     * the class a functor.
     *  
     * @param f function from T to U
     * @return function to map across 
     */
	public <U> Func1<AdminAreaTreeNode<T>, AdminAreaTreeNode<U>> fmap(final Func1<T, U> f) {
		return new Func1<AdminAreaTreeNode<T>, AdminAreaTreeNode<U>>() {

			@Override
			public AdminAreaTreeNode<U> call(AdminAreaTreeNode<T> from) {
				AdminAreaTreeNode<U> to = new AdminAreaTreeNode<U>(from.getName(), from.getType(), f.call(from.getContent()));
		        
		        // apply same conversion recursively to children, they retain the same name and type label
		        Map<String, AdminAreaTreeNode<U>> toChildren = to.getChildren();
		        for (String fromChildKey : from.getChildren().keySet()) {
		            toChildren.put(fromChildKey, this.call(from.getChildren().get(fromChildKey)));
		        }
		        return to;
			}
			
		};
	}
	
	/**
	 * Descends tree of nodes applying action to node and its children.
	 * @param action some side-effecting action
	 */
	public void forEach(final Action1<AdminAreaTreeNode<T>> action) {
	    
	    action.call(this);
	    for (AdminAreaTreeNode<T> childNode : this.children.values()) {
	        childNode.forEach(action);
	    }
	}
        
    
    
}
