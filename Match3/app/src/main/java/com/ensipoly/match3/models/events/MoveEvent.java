package com.ensipoly.match3.models.events;

import com.ensipoly.match3.models.Token;


/**
 * Move a token (downwards)
 */
public class MoveEvent implements EventAcceptor {


    private int prevX;
    private int newX;
    private int y;
    private Token t;

    /**
     * @param prevX previous x coordinate
     * @param newX  new x coordinate
     * @param y     y coordinate. Is the same since the motions is downwards
     * @param t     token that is moved
     */
    public MoveEvent(int prevX, int newX, int y, Token t) {
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
    public String toString() {
        return "MoveEvent (" + prevX + "," + y + ") -> (" + newX + "," + y + ")";
    }
}
