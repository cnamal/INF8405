package com.ensipoly.match3.models.events;


/**
 * Last event that is sent
 */
public class EndEvent implements EventAcceptor {

    private boolean endGame;

    /**
     * @param endGame if no more combinations are possible in the grid, endGame should be set to true
     */
    public EndEvent(boolean endGame) {
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
