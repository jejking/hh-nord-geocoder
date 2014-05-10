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

import java.util.LinkedList;

/**
 * Generic observable.
 * @author jejking
 *
 * @param <T>
 */
public class SimpleObservable<T> {

    private final LinkedList<SimpleObserver<T>> subscribers = new LinkedList<>(); 
    
    /**
     * Adds the subscriber.
     * 
     * @param observer. Should not be <code>null</code>.
     */
    void subscribe(SimpleObserver<T> observer) {
        this.subscribers.add(observer);
    }
    
    /**
     * Removes the subscriber, if subscribed.
     * 
     * @param observer
     */
    public void unsubscribe(SimpleObservable<T> observer) {
        this.subscribers.remove(observer);
    }
    
    protected void next(T value) {
        for (SimpleObserver<T> subscriber: subscribers) {
            subscriber.onNext(value);
        }
    }
     
    protected void error(Exception e) {
        for (SimpleObserver<T> subscriber: subscribers) {
            subscriber.onError(e);
        }
    }
    
    protected void completed() {
        for (SimpleObserver<T> subscriber: subscribers) {
            subscriber.onCompleted();
        }
    }
}
