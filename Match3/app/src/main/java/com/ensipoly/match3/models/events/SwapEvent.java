package com.ensipoly.match3.models.events;

import com.ensipoly.match3.models.Token;


/**
 * Swap two tokens
 */
public class SwapEvent implements EventAcceptor {

    private int x1;
    private int y1;
    private int x2;
    private int y2;
    private Token t1;
    private Token t2;

    /**
     * @param x1 x coordinate of t1
     * @param y1 y coordinate of t1
     * @param t1 first token
     * @param x2 x coordinate of t2
     * @param y2 y coordinate of t2
     * @param t2 second token
     */
    public SwapEvent(int x1, int y1, Token t1, int x2, int y2, Token t2) {
        this.x1 = x1;
        this.y1 = y1;
        this.t1 = t1;
        this.x2 = x2;
        this.y2 = y2;
        this.t2 = t2;
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public Token getT1() {
        return t1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }

    public Token getT2() {
        return t2;
    }

    @Override
    public void accept(EventVisitor ev) {
        ev.visit(this);
    }

    @Override
    public String toString() {
        return "SwapEvent : (" + x1 + "," + y1 + ") (" + x2 + "," + y2 + ")";
    }
}
