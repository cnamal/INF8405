package com.ensipoly.match3.models;

import android.util.Log;

import java.util.Random;


/**
 * For Parsing and level files, the current transcription is :
 * RED = 0
 * BLUE = 1
 * GREEN = 2
 * ORANGE = 3
 * YELLOW = 4
 * PURPLE = 5
 */
public enum Token {
    RED,
    BLUE,
    GREEN,
    ORANGE,
    YELLOW,
    PURPLE;

    private static final int size = Token.values().length;

    public static Token convertToToken(int value) {
        switch (value) {
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

    public static Token generateRandomToken() {
        Random rand = new Random();
        return Token.convertToToken(rand.nextInt(size));
    }

    public String toString() {
        switch (this) {
            case RED:
                return "red";
            case BLUE:
                return "blue";
            case GREEN:
                return "green";
            case ORANGE:
                return "#FFA500";
            case YELLOW:
                return "yellow";
            case PURPLE:
                return "purple";
            default:
                Log.e("Token", "Unhandled color");
                return "black";
        }
    }
}
