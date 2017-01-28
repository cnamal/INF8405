package com.ensipoly.match3.models.events;

/**
 * Created by namalgac on 1/24/17.
 */

public class EndEvent implements EventAcceptor {

    private boolean endGame;

    public EndEvent(boolean endGame){
        this.endGame = endGame;
    }

    public boolean isEndGame() {
        return endGame;
    }

    @Override
    public void accept(EventVisitor ev) {
        ev.visit(this);
    }
}
