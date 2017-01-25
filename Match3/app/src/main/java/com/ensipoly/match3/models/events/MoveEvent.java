package com.ensipoly.match3.models.events;

import java.util.Arrays;

/**
 * Created by namalgac on 1/24/17.
 */

public class MoveEvent implements EventAcceptor {


    private int[] cols;
    private int lowestRow;

    /**
     * Shift the columns in cols downwards. Shifts all rows in [0, lowestRow].
     * Elements in row lowestRow should have been removed before.
     * @param cols
     * @param lowestRow
     */
    public MoveEvent(int[] cols,int lowestRow){
        this.cols = cols;
        this.lowestRow = lowestRow;
    }

    public int[] getCols() {
        return cols;
    }

    public int getLowestRow() {
        return lowestRow;
    }

    @Override
    public void accept(EventVisitor ev) {
        ev.visit(this);
    }

    @Override
    public String toString(){
        return "MoveEvent "+ Arrays.toString(cols) + " " +lowestRow;
    }
}
