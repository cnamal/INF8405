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

    private int sizeX;
    private int sizeY;
    private Token[][] items;
    private int maxMoves;
    private int scoreGoal;

    public Grid(String filename){
        parseGrid(filename);
    }

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
}
