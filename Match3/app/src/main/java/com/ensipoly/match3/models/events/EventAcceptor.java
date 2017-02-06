package com.ensipoly.match3.models.events;


/**
 * Acceptor in visitor pattern
 */
public interface EventAcceptor {
    void accept(EventVisitor ev);
}
