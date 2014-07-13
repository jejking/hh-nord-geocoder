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
package com.jejking.osm;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.jws.soap.SOAPBinding.Use;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Value class representing a <tt>relation</tt> element in an OSM file.
 * 
 * @author jejking
 *
 */
public final class OsmRelation extends OsmComponent {

    private final ImmutableList<Member> members;
    
    
    /**
     * Constructor. No property may be null - if not present, use {@link Optional#absent()}.
     * 
     * @param metadata
     * @param properties
     * @param members mapping of the <tt>member</tt> subelements
     * 
     * @throws NullPointerException if any parameter is <code>null</code>
     */
    public OsmRelation(OsmMetadata metadata, ImmutableMap<String, String> properties,
            ImmutableList<Member> members) {
        super(metadata, properties);
        this.members = checkNotNull(members);
    }

    /**
     * Returns members.
     * 
     * @return members.
     */
    public ImmutableList<Member> getMembers() {
        return members;
    }

    
    /**
     * Mapped from <tt>member</tt> child element of <tt>relation</tt>.
     * 
     */
    public static class Member {
        
        public enum MemberType {NODE, WAY, RELATION}
        
        private final MemberType type;
        private final Long ref;
        private final Optional<String> role;
        
        /**
         * Constructor.
         * 
         * @param type type, mapped from <tt>type</tt> element
         * @param ref numerical reference, mapped from <tt>ref</tt> element
         * @param role role, {@link Use} {@link Optional#absent()} if empty string
         */
        public Member(MemberType type, Long ref, Optional<String> role) {
            super();
            this.type = type;
            this.ref = ref;
            this.role = role;
        }

        
        
        
        
        
        /**
         * @return the type
         */
        public MemberType getType() {
            return type;
        }





        
        /**
         * @return the ref
         */
        public Long getRef() {
            return ref;
        }





        
        /**
         * @return the role
         */
        public Optional<String> getRole() {
            return role;
        }





        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((ref == null) ? 0 : ref.hashCode());
            result = prime * result + ((role == null) ? 0 : role.hashCode());
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
            if (!(obj instanceof Member)) {
                return false;
            }
            Member other = (Member) obj;
            if (ref == null) {
                if (other.ref != null) {
                    return false;
                }
            } else if (!ref.equals(other.ref)) {
                return false;
            }
            if (role == null) {
                if (other.role != null) {
                    return false;
                }
            } else if (!role.equals(other.role)) {
                return false;
            }
            if (type != other.type) {
                return false;
            }
            return true;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "Member [type=" + type + ", ref=" + ref + ", role=" + role + "]";
        }
        
        
        
    }
    
}
