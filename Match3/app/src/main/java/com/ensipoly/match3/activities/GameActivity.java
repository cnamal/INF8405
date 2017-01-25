package com.ensipoly.match3.activities;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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


public class GameActivity extends FragmentActivity implements Observer, EventVisitor {

    private static final String TAG = "GameActivity";
    private Grid grid;
    private GridLayout gridLayout;
    private boolean clickable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        int level = getIntent().getIntExtra(GameMenuFragment.LEVEL, 1);
        try {

            grid = new Grid(new BufferedReader(new InputStreamReader(getAssets().open("level" + level + ".data"))));
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

                    GradientDrawable shape = new GradientDrawable();
                    shape.setShape(GradientDrawable.OVAL);
                    shape.setColor(Color.parseColor(grid.getToken(x, y).toString()));
                    imageView.setImageDrawable(shape);
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
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void visit(AddEvent add) {
        Log.d(TAG, "Visit add :" + add.toString());
    }

    @Override
    public void visit(RemoveEvent re) {
        Log.d(TAG, "Visit remove :" + re.toString());
    }

    @Override
    public void visit(MoveEvent move) {
        Log.d(TAG, "Visit move :" + move.toString());
    }

    @Override
    public void visit(SwapEvent swap) {
        Log.d(TAG, "Visit swap :" + swap.toString());
    }

    @Override
    public void visit(EndEvent end) {
        setClickable(true);
    }

    @Override
    public void visit(ScoreEvent score) {
        Log.d(TAG, "Visit combo :" + score.toString());
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o instanceof EventAcceptor) {
            ((EventAcceptor) o).accept(this);
        }
    }

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
                grid.swapElements(x,y,dir);
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }
    }

    private void setClickable(boolean clickable){
        for(int i=0;i<gridLayout.getChildCount();i++)
            gridLayout.getChildAt(i).setLongClickable(clickable);
    }
}
