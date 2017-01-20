package com.ensipoly.match3.models;

/**
 * Created by Adrien on 20/01/2017.
 */

/* For Parsing and level files, the current transcription is :
RED = 0
BLUE = 1
GREEN = 2
ORANGE = 3
YELLOW = 4
PURPLE = 5
 */

public enum Token {
    RED,
    BLUE,
    GREEN,
    ORANGE,
    YELLOW,
    PURPLE;

    public static Token convertToToken(int value){
        switch(value){
            case 0:
                return Token.RED;
            case 1:
                return Token.BLUE;
            case 2:
                return Token.GREEN;
            case 3:
                return Token.ORANGE;
            case 4:
                return Token.YELLOW;
            default:
                return Token.PURPLE;
        }
    }
}
