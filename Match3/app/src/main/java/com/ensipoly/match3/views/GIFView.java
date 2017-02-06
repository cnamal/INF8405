package com.ensipoly.match3.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.view.View;
import android.widget.FrameLayout;

import com.ensipoly.match3.R;

import java.io.InputStream;


/**
 * View for fireworks
 * Main algorithm found here :
 * http://androidosbeginning.blogspot.ca/2010/09/gif-animation-in-android.html
 */
public class GIFView extends View {

    Movie movie;
    InputStream is = null;
    long movieStart;
    FrameLayout.LayoutParams params;
    int lrPadding;
    int tbPadding;

    public GIFView(Context context) {
        super(context);
        is = context.getResources().openRawResource(R.raw.fireworks);
        movie = Movie.decodeStream(is);
        lrPadding = movie.width() / 2 + movie.width() / 3;
        tbPadding = movie.height() / 2;
        params = new FrameLayout.LayoutParams(movie.width() + lrPadding, movie.height() + tbPadding);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        setLayoutParams(params);
        super.onDraw(canvas);
        long now = android.os.SystemClock.uptimeMillis();
        if (movieStart == 0)
            movieStart = now;
        int relTime = (int) ((now - movieStart) % movie.duration());
        movie.setTime(relTime);
        movie.draw(canvas, lrPadding / 2, tbPadding / 2);
        this.invalidate();
    }

}
