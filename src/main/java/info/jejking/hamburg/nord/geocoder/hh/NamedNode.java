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
package info.jejking.hamburg.nord.geocoder.hh;

import java.util.HashMap;
import java.util.Map;

/**
 * Trivial class for creating a tree of named key-value pairs.
 * @author jejking
 *
 * @param <T> the type
 */
public class NamedNode<T> {

    private final String name;
    private final String type;
    private final HashMap<String, NamedNode<T>> children = new HashMap<String, NamedNode<T>>();
    private final T content;
    
    public NamedNode(String name, String type, T content) {
        super();
        this.name = name;
        this.type = type;
        this.content = content;
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
    public Map<String, NamedNode<T>> getChildren() {
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
        if (!(obj instanceof NamedNode)) {
            return false;
        }
        NamedNode<?> other = (NamedNode<?>) obj;
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
        return "NamedNode [name=" + name + ", type=" + type + ", children=" + children + ", content=" + content + "]";
    }

    
    
        
    
    
}
