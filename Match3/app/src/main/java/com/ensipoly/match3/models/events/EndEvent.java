package com.ensipoly.match3.models.events;

/**
 * Created by namalgac on 1/24/17.
 */

public class EndEvent implements EventAcceptor {
    @Override
    public void accept(EventVisitor ev) {
        ev.visit(this);
    }
}
