package com.ensipoly.match3.models.events;

import com.ensipoly.match3.models.Token;

/**
 * Created by namalgac on 1/24/17.
 */

public class AddEvent implements EventAcceptor {

    private int x;
    private int y;
    private Token token;

    public AddEvent(int x, int y, Token token){
        this.x= x;
        this.y=y;
        this.token = token;
    }

    public int getX(){
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
    public String toString(){
        return "AddEvent "+x+ " " + y + " " + token;
    }
}
