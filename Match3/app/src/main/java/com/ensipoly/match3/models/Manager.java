package com.ensipoly.match3.models;

/**
 * Created by Adrien on 20/01/2017.
 */

/*
    Class Manager
    Singleton class (only one instance)

    It's the game manager, contains all the data through the execution.
    At each level, a new grid is parsed and store into this manager.
 */

public final class Manager {

    /* Pattern Singleton */
    private static volatile Manager instance = null;

    // Private constructor, to override the default public one.
    private Manager(){
        super();
    }

    static public Manager getInstance(){
        if( Manager.instance == null){
            Manager.instance = new Manager();
        }
        return Manager.instance;
    }
    /* End of Singleton Pattern*/

    // Constant relative path to level data.
    // Each level need to respect this format, if X is the level number : "levelX.data"
    private final String levelDataPath = "\\..\\..\\..\\..\\levelData";
    private final String managerSavePath = "";

    // Number of levels currently implemented
    private final int maxLevel = 4;

    // Private members
    private Grid grid = null;           // Store the current grid, null if not in game view
    private int score = 0;              // Store the current score
    private int level = 0;              // Current level, 0 if not in game view
    private int movesRemaining = 0;     // Number of moves remaining. When hit 0 it's game over

    private int maxLevelUnlocked = 1;   // Maximum level unlocked

    /*
        Getters
    */

    public int getScore(){
        return this.score;
    }

    public int getMovesRemaining(){
        return this.getMovesRemaining();
    }

    public Grid getGrid(){
        return this.grid;
    }

    public int getMaxLevelUnlocked(){
        return this.maxLevelUnlocked;
    }

    public int getLevel() {
        return level;
    }

    /**
        Load a new level. level must be between 1 and this.maxLevel
        @param level The level to load
    */
    public void loadLevel(int level){

    }

    /**
        Restart the level. Fails if this.level is currently equal to 0
        @param joker If set to true, do not reset the score. Useful for debug
     */
    public void restartLevel(Boolean joker){

    }

    /**
        Function called when the game is over.
        Behaviour still need to be defined
     */
    private void gameOver(){

    }

    /**
        Function called if the level has been completed
        Update the maxLevelUnlocked
        Behaviour still need to be defined
     */
    private void success(){

    }

    /**
        Save the manager if the game is closed.
     */
    public void saveManager(){

    }

    /**
        Restore the manager. Do nothing if the manager has never been saved.
     */
    public void restoreManager(){

    }
    /**
        Use a move. Checks also if 0 is reached.
     */
    public void useMove(){

    }

    /**
        Change the score value after a good action
        Checks also if the max score is reached
        @param value The value to add to the score
     */
    private void addScore(int value){

    }

    /**
        Reset score
     */
    private void resetScore(){
        this.score = 0;
    }



    /**
        Make the move to the grid and update the score.
        @param x1 X coordinate of the first token
        @param y1 Y coordinate of the first token
        @param x2 X coordinate of the second token
        @param y2 Y coordinate of the second token
     */
    public void makeMove(int x1, int y1, int x2, int y2){

    }




}
