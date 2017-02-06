package com.ensipoly.match3.models.events;


/**
 * Remove a token
 */
public class RemoveEvent implements EventAcceptor {

    private int x;
    private int y;

    public RemoveEvent(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public void accept(EventVisitor ev) {
        ev.visit(this);
    }

    @Override
    public String toString() {
        return "RemoveEvent " + x + " " + y;
    }

}
