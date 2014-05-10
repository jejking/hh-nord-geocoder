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
package info.jejking.hamburg.nord.geocoder.osm;

/**
 * Subscriber to an event stream from an {@link SimpleObserver}.
 * 
 * @author jejking
 *
 * @param <T>
 */
public interface SimpleObserver<T> {

    /**
     * Notifies of next event.
     * @param value
     */
    void onNext(T value);
    
    /**
     * Notifies of error reading from stream.
     * @param e
     */
    void onError(Exception e);
    
    /**
     * Notifies that stream is empty.
     */
    void onCompleted();
    
}
