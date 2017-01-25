package com.ensipoly.match3.models.events;

/**
 * Created by namalgac on 1/24/17.
 */

public interface EventAcceptor {
    void accept(EventVisitor ev);
}
