package com.ensipoly.match3.activities;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class GameActivity extends AppCompatActivity implements Observer, EventVisitor {

    private static final String TAG = "GameActivity";
    private Grid grid;
    private GridLayout gridLayout;
    private Queue<EventAcceptor> listEvents = new ConcurrentLinkedQueue<>();
    private boolean addList;

    private int turnsLeft;
    private int minScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        int level = getIntent().getIntExtra(GameMenuFragment.LEVEL, 1);
        try {

            initGame(level);
            grid = new Grid(new BufferedReader(new InputStreamReader(getAssets().open("level" + level + ".data"))));
            grid.addObserver(this);
            gridLayout = (GridLayout) findViewById(R.id.grid);
            gridLayout.setRowCount(grid.getRowCount());
            gridLayout.setColumnCount(grid.getColumnCount());
            for (int x = 0; x < grid.getRowCount(); x++) {
                for (int y = 0; y < grid.getColumnCount(); y++) {

                    ImageView imageView;
                    // if it's not recycled, initialize some attributes
                    imageView = new ImageView(this);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.rowSpec = GridLayout.spec(x, 1.0f);
                    params.columnSpec = GridLayout.spec(y, 1.0f);
                    imageView.setLayoutParams(params);

                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setAdjustViewBounds(true);
                    imageView.setPadding(8, 8, 8, 8);
                    imageView.setImageDrawable(getDrawable(grid.getToken(x, y)));
                    final int xpos = x;
                    final int ypos = y;
                    imageView.setLongClickable(true);

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

            if(grid.isThereAnyCombinationRemaining())
                Log.e(TAG, "There is combination");
            else
                Log.e(TAG, "There is no combination");

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    /**************************
     *                        *
     *    Helper functions    *
     *                        *
     **************************/

    private GradientDrawable getDrawable(Token token){
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(Color.parseColor(token.toString()));
        return shape;
    }

    private void setClickable(boolean clickable){
        for(int i=0;i<gridLayout.getChildCount();i++)
            gridLayout.getChildAt(i).setLongClickable(clickable);
    }

    private int convert(int x,int y){
        return x*grid.getColumnCount()+y;
    }

    private void initGame(int level){
        switch (level){
            case 1:
                turnsLeft = 6;
                minScore = 800;
                break;
            case 2:
                turnsLeft = 10;
                minScore = 1200;
                break;
            case 3:
                turnsLeft = 10;
                minScore = 1400;
                break;
            case 4:
                turnsLeft = 10;
                minScore = 1800;
                break;
        }
    }

    /***************************
     *                         *
     *    Visitor functions    *
     *                         *
     ***************************/

    @Override
    public void visit(AddEvent add) {
        if(addList){
            listEvents.add(add);
        }else {
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
        if(addList){
            listEvents.add(re);
        }else {
            int x = re.getX();
            int y = re.getY();
            gridLayout.getChildAt(convert(x, y)).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void visit(MoveEvent move) {
        if(addList){
            listEvents.add(move);
        }else {
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
        if(addList){
            listEvents.add(swap);
        }else {
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
        if(addList){
            addList= false;
            listEvents.add(end);
            doEvents();
        }else {
            if(end.isEndGame()){
                //TODO
            }else
                setClickable(true);

        }
    }

    @Override
    public void visit(ScoreEvent score) {
        Toast.makeText(GameActivity.this, score.toString(), Toast.LENGTH_SHORT).show();
    }

    private void doEvents(){
        if(listEvents.size()==0)
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

    /****************************
     *                          *
     *    Observer functions    *
     *                          *
     ****************************/

    @Override
    public void update(Observable observable, Object o) {
        if (o instanceof EventAcceptor) {
            ((EventAcceptor) o).accept(this);
        }
    }

    /***********************
     *                     *
     *    Gesture class    *
     *                     *
     ***********************/

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;
        private int x;
        private int y;

        MyGestureDetector(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Direction dir = null;
            // right to left swipe
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Toast.makeText(GameActivity.this, "Left Swipe", Toast.LENGTH_SHORT).show();
                dir = Direction.LEFT;
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Toast.makeText(GameActivity.this, "Right Swipe", Toast.LENGTH_SHORT).show();
                dir = Direction.RIGHT;
            } else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                Toast.makeText(GameActivity.this, "Up Swipe", Toast.LENGTH_SHORT).show();
                dir= Direction.UP;
            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                Toast.makeText(GameActivity.this, "Down Swipe", Toast.LENGTH_SHORT).show();
                dir = Direction.DOWN;
            }
            if(dir==null)
                return false;
            if(grid.isSwapPossible(x,y, dir)){
                setClickable(false);
                addList = true;
                int score = grid.swapElements(x,y,dir);
                Toast.makeText(GameActivity.this, "Score = " + score, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(GameActivity.this, "Swap Impossible", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }
    }



}
