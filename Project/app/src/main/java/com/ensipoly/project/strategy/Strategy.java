package com.ensipoly.project.strategy;

import android.content.Intent;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ensipoly.project.MapsActivity;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.GoogleMap;

public abstract class Strategy implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    protected final RecyclerView recyclerView;
    protected TextView mInfoView;
    protected GoogleMap mMap;

    protected FloatingActionMenu menu;
    protected FloatingActionButton undo;
    protected FloatingActionButton cancel;
    protected FloatingActionButton done;
    protected FloatingActionButton go;
    protected FloatingActionButton create;
    protected MapsActivity activity;
    protected BottomSheetBehavior mBottomSheetBehavior1;

    protected static final int MENU = 1;
    protected static final int UNDO = 2;
    protected static final int CANCEL = 4;
    protected static final int DONE = 8;
    protected static final int GO = 16;
    protected static final int CREATE = 32;

    public static class StrategyParameters {
        public BottomSheetBehavior mBottomSheetBehavior1;
        public MapsActivity activity;
        public FloatingActionMenu menu;
        public FloatingActionButton undo;
        public FloatingActionButton cancel;
        public FloatingActionButton done;
        public FloatingActionButton go;
        public FloatingActionButton create;
        public TextView infoView;
        public GoogleMap map;
        public RecyclerView recyclerView;
    }

    protected Strategy(StrategyParameters params) {
        mInfoView = params.infoView;
        mMap = params.map;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        menu = params.menu;
        undo = params.undo;
        cancel = params.cancel;
        done = params.done;
        go = params.go;
        create = params.create;
        activity = params.activity;
        mBottomSheetBehavior1 = params.mBottomSheetBehavior1;
        recyclerView = params.recyclerView;
        initialButtonState();
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

    private boolean isHidden(FloatingActionButton button) {
        return button.getVisibility() == View.GONE;
    }

    private void initialButtonState() {
        initialButtonState(initiallyShownButtons());
    }

    protected void initialButtonState(int flags) {
        int i = 1;
        if (flags == 0) {
            menu.hideMenu(true);
            return;
        } else
            menu.showMenu(true);
        while (true) {
            FloatingActionButton button = getButton(1 << i);
            if (button == null)
                break;
            if ((flags & (1 << i)) != 0 && isHidden(button)) {
                show(button);
            } else if ((flags & (1 << i)) == 0 && !isHidden(button)) {
                hide(button);
            }
            i++;
        }
    }

    abstract protected int initiallyShownButtons();


    private FloatingActionButton getButton(int id) {
        switch (id) {
            case UNDO:
                return undo;

            case CANCEL:
                return cancel;

            case DONE:
                return done;

            case GO:
                return go;

            case CREATE:
                return create;

            default:
                return null;
        }
    }

    public abstract void cleanup();

    protected void switchStrategy(int strategy) {
        activity.switchStrategy(strategy);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        throw new RuntimeException("Stub!");
    }

}
