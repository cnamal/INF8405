package com.ensipoly.match3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.view.View;
import android.widget.FrameLayout;

import java.io.InputStream;

/**
 * Created by namalgac on 2/4/17.
 */

public class GIFView extends View {
    Movie movie,movie1;
    InputStream is=null,is1=null;
    long moviestart;
    int container_width;
    int container_height;
    FrameLayout.LayoutParams params;
    int lrPadding;
    int tbPadding;

    public GIFView(Context context) {
        super(context);
        is=context.getResources().openRawResource(R.raw.fireworks);
        movie=Movie.decodeStream(is);
        lrPadding = movie.width()/2+movie.width()/3;
        tbPadding = movie.height()/2;
        params = new FrameLayout.LayoutParams(movie.width()+lrPadding,movie.height()+tbPadding);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        setLayoutParams(params);
        super.onDraw(canvas);
        long now=android.os.SystemClock.uptimeMillis();
        if (moviestart == 0)
            moviestart = now;
        int relTime = (int)((now - moviestart) % movie.duration()) ;
        movie.setTime(relTime);
        movie.draw(canvas,lrPadding/2,tbPadding/2);
        this.invalidate();
    }

}
