package com.ensipoly.project.strategy;

import android.view.View;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.GoogleMap;

public abstract class Strategy implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    protected TextView mInfoView;
    protected GoogleMap mMap;

    protected FloatingActionMenu menu;
    protected FloatingActionButton undo;
    protected FloatingActionButton cancel;
    protected FloatingActionButton done;

    public static class StrateyParameters {
        public FloatingActionMenu menu;
        public FloatingActionButton undo;
        public FloatingActionButton cancel;
        public FloatingActionButton done;
        public TextView infoView;
        public GoogleMap map;
    }

    protected Strategy(StrateyParameters params) {
        mInfoView = params.infoView;
        mMap = params.map;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        menu = params.menu;
        undo = params.undo;
        cancel = params.cancel;
        done = params.done;
    }

    public abstract boolean onBackPressed();

    protected void show(FloatingActionButton button) {
        if (button.getVisibility() != View.GONE)
            return;

        if (menu.isOpened())
            button.setVisibility(View.VISIBLE);
        else
            button.setVisibility(View.INVISIBLE);

        button.setLabelVisibility(View.VISIBLE);
    }

    protected void hide(FloatingActionButton button) {
        button.setVisibility(View.GONE);
        button.setLabelVisibility(View.GONE);
    }
}
