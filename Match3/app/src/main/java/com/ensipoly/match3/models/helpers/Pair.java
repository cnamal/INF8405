package com.ensipoly.match3.models.helpers;


/**
 * Generic class of pairs
 *
 * @param <X> first type
 * @param <Y> second type
 */
public class Pair<X, Y> {

    public final X x;
    public final Y y;

    public Pair(X x, Y y) {
        this.x = x;
        this.y = y;
    }

}
