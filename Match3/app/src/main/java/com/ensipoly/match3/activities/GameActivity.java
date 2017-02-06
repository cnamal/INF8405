package com.ensipoly.match3.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ensipoly.match3.R;
import com.ensipoly.match3.fragments.GameMenuFragment;
import com.ensipoly.match3.models.Direction;
import com.ensipoly.match3.models.Grid;
import com.ensipoly.match3.models.Token;
import com.ensipoly.match3.models.events.AddEvent;
import com.ensipoly.match3.models.events.EndEvent;
import com.ensipoly.match3.models.events.EventAcceptor;
import com.ensipoly.match3.models.events.EventVisitor;
import com.ensipoly.match3.models.events.MoveEvent;
import com.ensipoly.match3.models.events.RemoveEvent;
import com.ensipoly.match3.models.events.ScoreEvent;
import com.ensipoly.match3.models.events.SwapEvent;
import com.ensipoly.match3.views.GIFView;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Activity corresponding to the game
 */
public class GameActivity extends AppCompatActivity implements Observer, EventVisitor {

    private static final String TAG = "GameActivity";
    private String SCORE;
    private String TURNS;
    private String MINSCORE;
    private String COMBO;

    private TextView scoreTextView;
    private TextView turnsTextView;
    private TextView minScoreTextView;
    private TextView comboTextView;
    private Grid grid = null;
    private GridLayout gridLayout = null;
    private Queue<EventAcceptor> listEvents = new ConcurrentLinkedQueue<>();
    private boolean addList;

    private int turnsLeft;
    private int minScore;
    private int score;
    private int combo;
    private int level;
    FloatingActionMenu menu;
    private SharedPreferences sharedPref;
    private FloatingActionButton joker;
    private static final int MAX_LEVELS = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // level to display
        level = getIntent().getIntExtra(GameMenuFragment.LEVEL, 1);

        sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        menu = (FloatingActionMenu) findViewById(R.id.fab_menu);

        // if the menu is opened, show grey overlay and pause game (unclickable)
        menu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {

            @Override
            public void onMenuToggle(boolean opened) {
                setClickable(!opened);
                if (opened)
                    findViewById(R.id.overlay).setVisibility(View.VISIBLE);
                else
                    findViewById(R.id.overlay).setVisibility(View.GONE);
            }
        });

        findViewById(R.id.restart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.close(true);

                joker.setVisibility(View.VISIBLE);
                joker.setLabelVisibility(View.VISIBLE);
                listEvents.clear();
                initialize(true);
            }
        });

        findViewById(R.id.quit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.close(true);

                joker.setVisibility(View.VISIBLE);
                joker.setLabelVisibility(View.VISIBLE);
                listEvents.clear();
                quitDialog();
            }
        });

        joker = (FloatingActionButton) findViewById(R.id.joker);
        joker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.close(true);

                listEvents.clear();
                initialize(false);
            }
        });

        SCORE = getString(R.string.score);
        TURNS = getString(R.string.turns_left);
        MINSCORE = getString(R.string.min_score);
        COMBO = getString(R.string.combo);

        scoreTextView = (TextView) findViewById(R.id.score_text);
        minScoreTextView = (TextView) findViewById(R.id.min_score_text);
        turnsTextView = (TextView) findViewById(R.id.turns_left_text);
        comboTextView = (TextView) findViewById(R.id.combo_text);


        initialize(true);
    }

    @Override
    public void onBackPressed() {
        // if no more turns, quit without asking
        if (turnsLeft == 0) {
            super.onBackPressed();
            return;
        }

        // if menu opened, just close it
        if (menu.isOpened()) {
            menu.close(true);
            return;
        }

        // otherwise show quit dialog
        quitDialog();
    }

    /**
     * Helper functions
     **/

    /**
     * Get GradientDrawable that corresponds to the color of a token
     *
     * @param token token that needs to be drawn
     * @return corresponding GradientDrawable
     */
    private GradientDrawable getDrawable(Token token) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(Color.parseColor(token.toString()));
        return shape;
    }

    /**
     * @param clickable if set to false, the grid is unclickable
     */
    private void setClickable(boolean clickable) {
        for (int i = 0; i < gridLayout.getChildCount(); i++)
            gridLayout.getChildAt(i).setLongClickable(clickable);
    }

    /**
     * 2D to 1D index
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return 1D coordinate
     */
    private int convert(int x, int y) {
        return x * grid.getColumnCount() + y;
    }


    /**
     * Initialises the grid
     */
    private void initGrid(boolean reset) {
        try {
            grid = new Grid(new BufferedReader(new InputStreamReader(getAssets().open("level" + level + ".data"))));
            if (reset)
                turnsLeft = grid.getMaxMoves();
            minScore = grid.getScoreGoal();
            grid.addObserver(this); // Observer pattern
            if (gridLayout == null)
                gridLayout = (GridLayout) findViewById(R.id.grid);
            else
                gridLayout.removeAllViews(); // if the grid was previously set, we delete everything

            gridLayout.setRowCount(grid.getRowCount());
            gridLayout.setColumnCount(grid.getColumnCount());

            /**
             * for every cell in the GridLayout, we add the tokens from the Grid
             */
            for (int x = 0; x < grid.getRowCount(); x++) {
                for (int y = 0; y < grid.getColumnCount(); y++) {

                    ImageView imageView;
                    imageView = new ImageView(this);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.rowSpec = GridLayout.spec(x, 1.0f);
                    params.columnSpec = GridLayout.spec(y, 1.0f);
                    imageView.setLayoutParams(params);

                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setAdjustViewBounds(true);
                    imageView.setPadding(8, 8, 8, 8);
                    imageView.setImageDrawable(getDrawable(grid.getToken(x, y)));
                    // reset positions as final to be used in handler
                    final int xpos = x;
                    final int ypos = y;
                    imageView.setLongClickable(true);

                    // set handler (swipe action)
                    imageView.setOnTouchListener(new View.OnTouchListener() {
                        private MyGestureDetector mgd = new MyGestureDetector(xpos, ypos);
                        private GestureDetector gestureDetector = null;

                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            if (gestureDetector == null)
                                gestureDetector = new GestureDetector(GameActivity.this, mgd);
                            return gestureDetector.onTouchEvent(motionEvent);
                        }
                    });

                    gridLayout.addView(imageView);
                }
            }

            if (grid.isThereAnyCombinationRemaining())
                Log.e(TAG, "There is combination");
            else
                Log.e(TAG, "There is no combination");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set a part of the text in green in a TextView
     *
     * @param view      view that is modified
     * @param beginning beginning of the text (remains the default color)
     * @param inGreen   text that will be in green (after beginning)
     */
    private void setGreenText(TextView view, String beginning, String inGreen) {
        String fullText = beginning + inGreen;

        Spannable spannable = new SpannableString(fullText);

        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#76FF03")), beginning.length(), fullText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        view.setText(spannable, TextView.BufferType.SPANNABLE);
    }

    private void updateTextViews() {
        updateTurnsView();
        updateScoreAndComboViews();
    }

    private void updateTurnsView() {
        turnsTextView.setText(TURNS + " " + turnsLeft);
    }

    private void updateScoreAndComboViews() {
        if (score >= minScore)
            setGreenText(scoreTextView, SCORE + " ", score + "");
        else
            scoreTextView.setText(SCORE + " " + score);
        if (combo > 1)
            setGreenText(comboTextView, COMBO + " ", "x" + combo);
        else
            comboTextView.setText(COMBO + " x" + combo);
    }


    private void initialize(int level, boolean resetScore) {
        this.level = level;
        initialize(resetScore);
    }

    private void initialize(boolean resetScore) {
        if (resetScore)
            score = 0;

        combo = 1;
        initGrid(resetScore);

        minScoreTextView.setText(MINSCORE + " " + minScore);
        ((TextView) findViewById(R.id.level_text)).setText(getLevelString());
        updateTextViews();
    }

    private String getLevelString() {
        switch (level) {
            case 1:
                return getString(R.string.level1);
            case 2:
                return getString(R.string.level2);
            case 3:
                return getString(R.string.level3);
            case 4:
                return getString(R.string.level4);
            default:
                return "";
        }
    }

    private void quitDialog() {

        if (turnsLeft == 0) {
            super.onBackPressed();
            return;
        }

        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_warning_black_24dp)
                .setTitle(getString(R.string.quit_alert_title))
                .setMessage(getString(R.string.quit_alert_body))
                .setPositiveButton(getString(R.string.quit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GameActivity.super.onBackPressed();
                    }

                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }


    /**
     * Visitor functions
     * Until we get the EndEvent event, we add all events to a list
     * It allows us to create a loop of events (cf doEvents)
     **/

    @Override
    public void visit(AddEvent add) {
        if (addList) {
            listEvents.add(add);
        } else {
            int x = add.getX();
            int y = add.getY();
            Token token = add.getToken();
            ImageView view = ((ImageView) gridLayout.getChildAt(convert(x, y)));
            view.setImageDrawable(getDrawable(token));
            view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void visit(RemoveEvent re) {
        if (addList) {
            listEvents.add(re);
        } else {
            int x = re.getX();
            int y = re.getY();
            gridLayout.getChildAt(convert(x, y)).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void visit(MoveEvent move) {
        if (addList) {
            listEvents.add(move);
        } else {
            int prevX = move.getPrevX();
            int newX = move.getNewX();
            int y = move.getY();
            Token prevToken = move.getToken();
            ImageView view = ((ImageView) gridLayout.getChildAt(convert(newX, y)));
            view.setImageDrawable(getDrawable(prevToken));
            view.setVisibility(View.VISIBLE);
            gridLayout.getChildAt(convert(prevX, y)).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void visit(SwapEvent swap) {
        if (addList) {
            listEvents.add(swap);
        } else {
            int x1 = swap.getX1();
            int y1 = swap.getY1();
            int x2 = swap.getX2();
            int y2 = swap.getY2();
            Token t1 = swap.getT1();
            Token t2 = swap.getT2();
            ImageView view = ((ImageView) gridLayout.getChildAt(convert(x1, y1)));
            view.setImageDrawable(getDrawable(t2));
            view = ((ImageView) gridLayout.getChildAt(convert(x2, y2)));
            view.setImageDrawable(getDrawable(t1));
        }
    }

    @Override
    public void visit(EndEvent end) {
        if (addList) {
            addList = false; // don't add to list anymore
            listEvents.add(end);
            doEvents(); // main loop
        } else {
            turnsLeft--;
            updateTurnsView();
            combo = 1;
            updateScoreAndComboViews();
            if (end.isEndGame() || turnsLeft == 0) { // no more combinations or turns left
                menu.open(true);
                if (turnsLeft == 0) { //disable joker if no more turns left
                    joker.setLabelVisibility(View.GONE);
                    joker.setVisibility(View.GONE);
                }
                // little surprise if we win the last level ;)
                if (score >= minScore && level == MAX_LEVELS) {
                    Dialog builder = new Dialog(this);
                    builder.setTitle(getString(R.string.congratulations));
                    builder.addContentView(new GIFView(this), new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    builder.show();
                }
            } else
                setClickable(true); // if we can continue to play, allow user input

            // new level unlocked
            if (score >= minScore && level < MAX_LEVELS) {
                int best = sharedPref.getInt(getString(R.string.best_level), -1);
                if (best < 0)
                    Log.e(TAG, "Unexpected best level");
                if (best == level) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(getString(R.string.best_level), level + 1);
                    editor.apply();
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.unlock_alert_title))
                            .setMessage(getString(R.string.unlock_alert_body))
                            .setPositiveButton(getString(R.string.next_level), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    initialize(level + 1, true);
                                }

                            })
                            .setNegativeButton(getString(R.string.continue_button), null)
                            .show();
                }
            }
        }
    }

    @Override
    public void visit(ScoreEvent scoreEvent) {
        if (addList) {
            listEvents.add(scoreEvent);
        } else {
            score += scoreEvent.getScore();
            combo = scoreEvent.getCombo();
            updateScoreAndComboViews();
        }
    }

    /**
     * Main event loop
     */
    private void doEvents() {
        if (listEvents.size() == 0)
            return;
        EventAcceptor event = listEvents.poll();
        event.accept(this);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                doEvents();
            }
        }, 300);
    }

    /**
     * Observer functions
     **/

    @Override
    public void update(Observable observable, Object o) {
        if (o instanceof EventAcceptor) {
            ((EventAcceptor) o).accept(this);
        }
    }


    /**
     * Gesture functions
     **/

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;
        private int x;
        private int y;

        /**
         * Coordinates in grid of the view
         *
         * @param x x coordinate
         * @param y y coordinate
         */
        MyGestureDetector(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Swap detection
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Direction dir = null;

            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
                dir = Direction.LEFT;
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
                dir = Direction.RIGHT;
            else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY)
                dir = Direction.UP;
            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY)
                dir = Direction.DOWN;

            if (dir == null)
                return false;
            if (grid.isSwapPossible(x, y, dir)) {
                setClickable(false);
                addList = true;
                grid.swapElements(x, y, dir);
            } else
                Toast.makeText(GameActivity.this, getString(R.string.swap_impossible), Toast.LENGTH_SHORT).show();

            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }
    }


}
