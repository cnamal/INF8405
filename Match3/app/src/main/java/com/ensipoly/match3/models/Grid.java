package com.ensipoly.match3.models;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Adrien on 20/01/2017.
 */

/*
    Grid class
    The grid class contains all the information concerning a grid.
    It need to be initialized with a filename, corresponding to a formatted textfile.
    The format needs to respect this :
    First line          : sizeX sizeY maxMoves scoreGoal
    sizeX next lines    : "sizeY int correponding to the color"
 */

public class Grid {

    private final int MIN_TOKENS_ALIGNED = 3;
    private final int THREE_ALIGNED_VALUE = 100;
    private final int FOUR_ALIGNED_VALUE = 200;
    private final int FIVE_ALIGNED_VALUE = 300;

    private int sizeX;
    private int sizeY;
    private Token[][] items;
    private int maxMoves;
    private int scoreGoal;

    public Grid(String filename){
        parseGrid(filename);
    }

    /**
     * Parse the file and put it in the grid
     * @param filename the filename
     */
    private void parseGrid(String filename){
        FileReader input;
        try {
           input = new FileReader(filename);
        } catch(FileNotFoundException e){
            System.out.print(filename + " does not exist...");
            return;
        }
        BufferedReader bufRead = new BufferedReader(input);
        try {
            // Read the first line, containing sizeX, sizeY, maxMoves and scoreGoal
            String myLine = bufRead.readLine();
            String[] array = myLine.split(" ");

            this.sizeX = Integer.parseInt(array[0]);
            this.sizeY = Integer.parseInt(array[1]);
            this.maxMoves = Integer.parseInt(array[2]);
            this.scoreGoal = Integer.parseInt(array[3]);

            items = new Token[sizeX][sizeY];

            // Read all the other lines, containing the level data
            // The file SHOULD be normally formatted
            for(int x = 0; x < this.sizeX; ++x){
                array = bufRead.readLine().split(" ");
                for(int y = 0; y < this.sizeY; ++y){
                    this.items[x][y] = Token.convertToToken(Integer.parseInt(array[y]));
                }
            }
        } catch(IOException e){
            System.out.print(filename + " is not correctly formatted");
            System.out.print(e.getMessage());
            return;
        }
    }

    public int getMaxMoves(){
        return this.maxMoves;
    }

    public int getScoreGoal(){
        return this.scoreGoal;
    }

    /**
        Switch places between [x1, y1] and [x2, y2]
        Therefore calls computeCombinations on each point.
        If a combination is detected, start a combo and loop until there is
        no combination left.

        @param x1 X coordinate of the first token
        @param y1 Y coordinate of the first token
        @param x2 X coordinate of the second token
        @param y2 Y coordinate of the second token
        @return the score of the whole move.
     */
    public int switchElements(int x1, int y1, int x2, int y2){
        return 0;
    }

    /** Check if a combination is possible on [x,y]
        If yes, return the score.
        If no, return 0
        @param x X coordinate of the token
        @param y Y coordinate of the token
     */
    private int computeCombinations(int x, int y){
        return 0;
    }

    /**
        Return true if a combination is possible for the token at [x,y], false otherwise
        @param x X coordinate of the token
        @param y Y coordinate of the token
     */
    private Boolean isCombinationPossible(int x, int y){
        return( this.numberOfTokensDown(x,y) >= MIN_TOKENS_ALIGNED ||
                this.numberOfTokensUp(x,y) >= MIN_TOKENS_ALIGNED ||
                this.numberOfTokensLeft(x,y) >= MIN_TOKENS_ALIGNED ||
                this.numberOfTokensRight(x,y) >= MIN_TOKENS_ALIGNED);
    }

    /**
        Return the number of identical tokens at [x,y] to the left
        @param x X coordinate of the token
        @param y Y coordinate of the token
     */
    private int numberOfTokensLeft(int x, int y){
        return 1;
    }

    /**
        Return the number of identical tokens at [x,y] to the Right
        @param x X coordinate of the token
        @param y Y coordinate of the token
     */
    private int numberOfTokensRight(int x, int y){
        return 1;
    }

    /**
        Return the number of identical tokens at [x,y] upwards
        @param x X coordinate of the token
        @param y Y coordinate of the token
     */
    private int numberOfTokensUp(int x, int y){
        return 1;
    }

    /**
        Return the number of identical tokens at [x,y] downwards
        @param x X coordinate of the token
        @param y Y coordinate of the token
     */
    private int numberOfTokensDown(int x, int y){
        return 1;
    }

    /**
        Moves down to gap lines, tokens in the column c, starting from line x
        Generate random tokens at the top
        @param c The column to consider
        @param x The first line to consider
        @param gap The number of cell to move down
     */
    private void moveTokens(int c, int x, int gap){

    }


}
