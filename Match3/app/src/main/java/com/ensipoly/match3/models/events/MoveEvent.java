package com.ensipoly.match3.models.events;

import com.ensipoly.match3.models.Token;

/**
 * Created by namalgac on 1/24/17.
 */

public class MoveEvent implements EventAcceptor {


    private int prevX;
    private int newX;
    private int y;
    private Token t;

    public MoveEvent(int prevX, int newX, int y, Token t){
        this.prevX = prevX;
        this.newX = newX;
        this.y = y;
        this.t = t;
    }

    public int getPrevX() {
        return prevX;
    }

    public int getNewX() {
        return newX;
    }

    public int getY() {
        return y;
    }

    public Token getToken() {
        return t;
    }


    @Override
    public void accept(EventVisitor ev) {
        ev.visit(this);
    }

    @Override
    public String toString(){
        return "MoveEvent ("+ prevX + "," + y+") -> (" + newX+","+y+")";
    }
}
