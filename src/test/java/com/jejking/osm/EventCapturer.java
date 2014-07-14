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

import java.util.LinkedList;
import java.util.List;

import rx.Observer;

class EventCapturer<T> implements Observer<T> {

    List<T> values = new LinkedList<>();
    Throwable e;
    boolean completed;
    
    @Override
    public void onNext(T value) {
        this.values.add(value);
    }

    @Override
    public void onError(Throwable e) {
        this.e = e;
        e.printStackTrace();
    }

    @Override
    public void onCompleted() {
        this.completed = true;
    }
    
}