package com.ensipoly.match3.models.events;


/**
 * Event with score and combo of the "round"
 */
public class ScoreEvent implements EventAcceptor {

    private int combo;
    private int score;

    public ScoreEvent(int score, int combo) {
        this.combo = combo;
        this.score = score;
    }

    public int getCombo() {
        return combo;
    }

    public int getScore() {
        return score;
    }

    @Override
    public void accept(EventVisitor ev) {
        ev.visit(this);
    }

    @Override
    public String toString() {
        return "ScoreEvent : " + score + " x" + combo;
    }
}
