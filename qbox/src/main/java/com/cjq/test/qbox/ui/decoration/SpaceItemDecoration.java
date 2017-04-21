package com.cjq.test.qbox.ui.decoration;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by KAT on 2016/9/13.
 */
public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    public SpaceItemDecoration(int space, boolean isHorizontal) {
        this.space = space;
        this.isHorizontal = isHorizontal;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        if(parent.getChildAdapterPosition(view) != 0) {
            if (isHorizontal) {
                outRect.top = space;
            } else {
                outRect.left = space;
            }
        }
    }

    private final int space;
    private final boolean isHorizontal;
}
