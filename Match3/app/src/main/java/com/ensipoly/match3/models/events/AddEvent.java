package com.ensipoly.match3.models.events;

import com.ensipoly.match3.models.Token;


/**
 * Event to add a token
 */
public class AddEvent implements EventAcceptor {

    private int x;
    private int y;
    private Token token;

    /**
     * @param x     x coordinate
     * @param y     y coordinate
     * @param token token to add
     */
    public AddEvent(int x, int y, Token token) {
        this.x = x;
        this.y = y;
        this.token = token;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Token getToken() {
        return token;
    }

    @Override
    public void accept(EventVisitor ev) {
        ev.visit(this);
    }

    @Override
    public String toString() {
        return "AddEvent " + x + " " + y + " " + token;
    }
}
