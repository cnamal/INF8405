package com.ensipoly.match3.models.events;

/**
 * Created by namalgac on 1/24/17.
 */

public interface EventVisitor {

    void visit(AddEvent add);
    void visit(RemoveEvent re);
    void visit(MoveEvent move);
    void visit(SwapEvent swap);
    void visit(EndEvent end);
    void visit(ScoreEvent score);
}
