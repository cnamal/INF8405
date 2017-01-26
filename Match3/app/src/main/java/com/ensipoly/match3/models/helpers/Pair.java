package com.ensipoly.match3.models.helpers;

/**
 * Created by Adrien on 25/01/2017.
 */

public class Pair<X, Y> {

    public final X x;
    public final Y y;

    public Pair(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Pair<X,Y> other){
        return other.x == this.x && other.y == this.y;
    }
}
