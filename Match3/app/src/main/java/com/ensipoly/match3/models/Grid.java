package com.ensipoly.match3.models;

import android.util.Log;

import com.ensipoly.match3.models.helpers.Pair;

import com.ensipoly.match3.models.events.EndEvent;
import com.ensipoly.match3.models.events.SwapEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;

/**
 * Created by Adrien on 20/01/2017.
 */

/**
    Grid class
    The grid class contains all the information concerning a grid.
    It need to be initialized with a filename, corresponding to a formatted textfile.
    The format needs to respect this :
    First line          : sizeX sizeY maxMoves scoreGoal
    sizeX next lines    : "sizeY int correponding to the color"
 */
public class Grid extends Observable{

    private static final String TAG = "Grid";

    private final int MIN_TOKENS_ALIGNED = 3;
    private final int THREE_ALIGNED_VALUE = 100;
    private final int FOUR_ALIGNED_VALUE = 200;
    private final int FIVE_ALIGNED_VALUE = 300;

    private int sizeX;
    private int sizeY;
    private Token[][] items;
    private int maxMoves;
    private int scoreGoal;

    public Grid(BufferedReader filename){
        parseGrid(filename);
    }


    private void parseGrid(BufferedReader bufRead){
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
            System.out.print("Not correctly formatted");
            System.out.print(e.getMessage());
        }
    }

    public int getMaxMoves(){
        return this.maxMoves;
    }

    public int getScoreGoal(){
        return this.scoreGoal;
    }

    /**
        Swap places between [x1, y1] and [x2, y2]
        If a combination is detected, start a combo and loop until there is
        no combination left.

        @param x1 X coordinate of the first token
        @param y1 Y coordinate of the first token
        @param x2 X coordinate of the second token
        @param y2 Y coordinate of the second token
        @return the score of the whole move.
     */
    private int swapElements(int x1, int y1, int x2, int y2){
        // Notify UI to swap elements on the screen
        setChanged();
        notifyObservers(new SwapEvent(x1, y1, x2, y2));

        // Actually swap elements
        swapItems(x1, y1, x2, y2);

        // Set the tokens to remove and to analyze
        ArrayList<Pair<Integer,Integer>> tokensToRemove = new ArrayList<>();
        ArrayList<Pair<Integer,Integer>> tokensToAnalyze = new ArrayList<>();

        // For the first pass (swapping), we analyze both of the swapped tokens
        tokensToAnalyze.add(new Pair<>(x1, y1));
        tokensToAnalyze.add(new Pair<>(x2, y2));

        // The final score
        int res = 0;

        // The combo multiplier
        int combo = 1;

        // Used to stop combos
        boolean end = false;

        while(!end) {
            // We need to be sure this list is empty
            tokensToRemove.clear();

            // For all token to analyze
            for (Pair<Integer, Integer> p :
                    tokensToAnalyze) {

                // If the token to analyze was already found to remove, continue
                if(tokensToRemove.contains(p))
                    continue;

                if (numberOfTokensHorizontally(p.x, p.y) >= MIN_TOKENS_ALIGNED) {
                    int numberLeft = numberOfTokensLeft(p.x, p.y);
                    int numberRight = numberOfTokensRight(p.x, p.y);
                    for (int y = p.y - numberLeft; y <= p.y + numberRight; y++) {
                        tokensToRemove.add(new Pair<>(p.x, y));
                    }

                    res += score(numberLeft + numberRight + 1) * combo;
                }

                if (numberOfTokensVertically(p.x, p.y) >= MIN_TOKENS_ALIGNED) {
                    int numberUp = numberOfTokensUp(p.x, p.y);
                    int numberDown = numberOfTokensDown(p.x, p.y);
                    for (int x = p.x - numberUp; x <= p.x + numberDown; x++) {
                        tokensToRemove.add(new Pair<>(x, p.y));
                    }
                    res += score(numberUp + numberDown + 1) * combo;
                }
            }

            // Remove all the tokens and store the lowest token to shift for each column
            int[] bottomLines = removeTokens(tokensToRemove);

            Log.d(TAG, "Old grid state");
            Log.d(TAG, this.toString());

            // Shift all lines following bottomLines returned by removeTokens
            // Add random token at the top
            // TODO : UPDATE THE VIEW
            for (int i = 0; i < sizeY; ++i) {
                moveTokens(i, bottomLines[i]);
            }

            Log.d(TAG, "New grid state");
            Log.d(TAG, this.toString());

            // We need to analyze tokens that has been changed
            tokensToAnalyze.clear();
            for(int i = 0; i < sizeY; ++i){
                if(bottomLines[i] != -1){
                    for(int j = bottomLines[i]; j >= 0; --j){
                        if(isCombinationPossible(j,i))
                            tokensToAnalyze.add(new Pair<>(j, i));
                    }
                }
            }

            // If we have found no new token to analyze, end.
            end = tokensToAnalyze.isEmpty();

            // If we go for another loop, increase the combo multiplier
            combo++;
        }

        // End of the event
        setChanged();
        notifyObservers(new EndEvent());
        return res;
    }

    /**
     * Swap places between [x1, y1] and the direction
     * If a combination is detected, start a combo and loop until there is
     * no combination left.
     * @param x1 X coordinate of the first token
     * @param y1 Y coordinate of the first token
     * @param direction Where to look at to swap
     * @return The whole score
     */
    public int swapElements(int x1, int y1, Direction direction){
        int x2 = x1;
        int y2 = y1;
        if(direction == Direction.DOWN && x2 < sizeX - 1){
            x2++;
        } else if(direction == Direction.UP && x2 > 0){
            x2--;
        } else if(direction == Direction.LEFT && y2 > 0){
            y2--;
        } else if(direction == Direction.RIGHT && y2 < sizeY - 1){
            y2++;
        }
        return swapElements(x1,y1,x2,y2);
    }

    /**
     * Swap items in the grid.
     * It must been between grid boundaries
     * @param x1 X coordinate of the first token
     * @param y1 Y coordinate of the first token
     * @param x2 X coordinate of the second token
     * @param y2 Y coordinate of the second token
     */
    private void swapItems(int x1, int y1, int x2, int y2){
        Token saveToken = getToken(x1,y1);
        items[x1][y1] = getToken(x2,y2);
        items[x2][y2] = saveToken;
    }

    /**
     * Remove all the tokens into tokenToRemove and return an array which size is sizeY
     * and its elements are which line to start to move down. It's the lowest token to shift for each column
     * @param tokensToRemove
     * @return
     */
    private int[] removeTokens(ArrayList<Pair<Integer, Integer>> tokensToRemove){
       int[] res = new int[sizeY];
        for (int i=0; i < sizeY; ++i){
            res[i] = -1;
        }
        for (Pair<Integer, Integer> p:
                tokensToRemove) {
            items[p.x][p.y] = null;
            if (res[p.y] <= p.x){
                res[p.y] = p.x;
            }
        }
        return res;
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
    private boolean isCombinationPossible(int x, int y){
        return( numberOfTokensHorizontally(x, y) >= MIN_TOKENS_ALIGNED ||
                numberOfTokensVertically(x, y) >= MIN_TOKENS_ALIGNED);
    }

    /**
     * Return true if you can switch the token at [x,y] with the one adjacent to it
     * following the direction @dir.
     * @param x1
     * @param y1
     * @param dir
     * @return
     */
    public boolean isSwapPossible(int x1, int y1, Direction dir){
        int x2 = x1;
        int y2 = y1;
        if(dir == Direction.DOWN && x2 < sizeX - 1){
            x2++;
        } else if(dir == Direction.UP && x2 > 0){
            x2--;
        } else if(dir == Direction.LEFT && y2 > 0){
            y2--;
        } else if(dir == Direction.RIGHT && y2 < sizeY - 1){
            y2++;
        } else {
            return false;
        }

        this.swapItems(x1, y1, x2, y2);
        boolean res =  (isCombinationPossible(x1,y1) || isCombinationPossible(x2,y2));
        this.swapItems(x1, y1, x2, y2);
        return res;
    }

    /**
        Return the number of identical tokens at [x,y] horizontally
        @param x X coordinate of the token
        @param y Y coordinate of the token
     */
    private int numberOfTokensHorizontally(int x, int y){
        return numberOfTokensLeft(x,y) + numberOfTokensRight(x,y) + 1;
    }

    /**
     Return the number of identical tokens at [x,y] vertically
     @param x X coordinate of the token
     @param y Y coordinate of the token
     */
    private int numberOfTokensVertically(int x, int y){
        return numberOfTokensUp(x, y) + numberOfTokensDown(x, y) + 1;
    }

    private int numberOfTokensLeft(int x, int y) {
        int res = 0;
        Token token = getToken(x, y);
        int yCurr = y - 1;
        while (yCurr >= 0 && getToken(x, yCurr).equals(token)) {
            yCurr--;
            res++;
        }
        return res;
    }

    private int numberOfTokensRight(int x, int y) {
        int res = 0;
        int yCurr = y+1;
        Token token = getToken(x, y);
        while(yCurr < sizeY && getToken(x, yCurr).equals(token)){
            yCurr++;
            res++;
        }
        return res;
    }

    private int numberOfTokensUp(int x, int y) {
        int res = 0;
        Token token = getToken(x, y);
        int xCurr = x - 1;
        while (xCurr >= 0 && getToken(xCurr, y).equals(token)) {
            xCurr--;
            res++;
        }
        return res;
    }

    private int numberOfTokensDown(int x, int y) {
        int res = 0;
        Token token = getToken(x, y);
        int xCurr = x+1;
        while(xCurr < sizeX && getToken(xCurr, y).equals(token)){
            xCurr++;
            res++;
        }
        return res;
    }

    /**
        Moves down to gap lines, tokens in the column c, starting from line x
        Generate random tokens at the top
        @param c The column to consider
        @param x The first line to consider
     */
    private void moveTokens(int c, int x){
        for(int currX = x; currX >= 0; --currX){
            int lookUp = 1;
            items[currX][c] = null;
            // We're looking up until we reach a not null token
            while(currX - lookUp >= 0){
                if(items[currX - lookUp][c] != null) {
                    items[currX][c] = items[currX - lookUp][c];
                    items[currX - lookUp][c] = null;
                    break;
                }
                lookUp++;
                // TODO : Can insert move here
            }
            // If we reached the top without success, generate random
            if(items[currX][c] == null)
                items[currX][c] = Token.generateRandomToken();
        }
    }

    public int getColumnCount(){
        return sizeY;
    }

    public int getRowCount(){
        return sizeX;
    }

    public Token getToken(int x,int y){
        return items[x][y];
    }

    @Override
    public String toString(){
        String res = "";
        for(int i = 0; i < sizeX; i++){
            for(int j = 0; j < sizeY; j++){
                if(getToken(i,j) == null){
                    res += "X";
                } else {
                    switch(getToken(i,j)){
                        case BLUE:
                            res += "B";
                            break;
                        case RED:
                            res += "R";
                            break;
                        case PURPLE:
                            res += "P";
                            break;
                        case YELLOW:
                            res += "Y";
                            break;
                        case ORANGE:
                            res += "O";
                            break;
                        case GREEN:
                            res += "G";
                            break;
                    }
                }
                res += "\t";
            }
            res += "\n";
        }
        return res;
    }

    private int score(int aligned){
        switch(aligned){
            case 3:
                return THREE_ALIGNED_VALUE;
            case 4:
                return FOUR_ALIGNED_VALUE;
            case 5:
                return FIVE_ALIGNED_VALUE;
        }
        return 0;
    }

}
