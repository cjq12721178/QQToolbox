package com.cjq.tool.qbox.ui.gesture;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.cjq.tool.qbox.ui.adapter.HeaderAndFooterWrapper;
import com.cjq.tool.qbox.ui.adapter.RecyclerViewBaseAdapter;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by CJQ on 2018/3/2.
 */

public abstract class SimpleRecyclerViewItemTouchListener
        implements RecyclerView.OnItemTouchListener,
        GestureDetector.OnGestureListener {

    private final RecyclerView mRecyclerView;
    private final GestureDetectorCompat mDetector;
    private Set<Integer> mChildViewIds;
    private Set<Object> mChildViewTags;
    private boolean mMinRangeEnable;

    public SimpleRecyclerViewItemTouchListener(@NonNull RecyclerView rv) {
        this(rv, false);
    }

    public SimpleRecyclerViewItemTouchListener(@NonNull RecyclerView rv, boolean isLongpressEnabled) {
        mRecyclerView = rv;
        mDetector = new GestureDetectorCompat(rv.getContext(), this);
        setIsLongPressEnabled(isLongpressEnabled);
    }

    public SimpleRecyclerViewItemTouchListener setIsLongPressEnabled(boolean enabled) {
        mDetector.setIsLongpressEnabled(enabled);
        return this;
    }

    public SimpleRecyclerViewItemTouchListener addItemChildViewTouchEnabled(@IdRes int id) {
        if (mChildViewIds == null) {
            mChildViewIds = new HashSet<>();
        }
        mChildViewIds.add(id);
        return this;
    }

    public SimpleRecyclerViewItemTouchListener addItemChildViewTouchEnabled(@NonNull Object tag) {
        if (mChildViewTags == null) {
            mChildViewTags = new HashSet<>();
        }
        mChildViewTags.add(tag);
        return this;
    }

    public SimpleRecyclerViewItemTouchListener setMinRangeEnable(boolean enable) {
        mMinRangeEnable = enable;
        return this;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return mDetector.onTouchEvent(e);
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        mDetector.onTouchEvent(e);
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    //一下继承自GestureDetector.OnGestureListener
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        View vItem = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
        if (vItem != null) {
            int position = correctPosition(mRecyclerView.getChildAdapterPosition(vItem));
            if (position != RecyclerView.NO_POSITION) {
                onItemClick(correctView(vItem, e), position);
                return true;
            }
        }
        return false;
    }

    private int correctPosition(int position) {
        if (position == RecyclerView.NO_POSITION) {
            return position;
        }
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter instanceof RecyclerViewBaseAdapter) {
            processBaseAdapter((RecyclerViewBaseAdapter) adapter, position);
            return position;
        } else if (adapter instanceof HeaderAndFooterWrapper) {
            return processWrapperAdapter((HeaderAndFooterWrapper) adapter, position);
        }
        return position;
    }

    private void processBaseAdapter(RecyclerViewBaseAdapter adapter, int position) {
        if (adapter.isUpdateSelectedState()) {
            adapter.toggleSelection(position);
        } else {
            adapter.setSelectedIndex(position);
        }
    }

    private int processWrapperAdapter(HeaderAndFooterWrapper adapter, int position) {
        int realPosition = adapter.getInnerPosition(position);
        if (adapter.getInnerAdapter() instanceof RecyclerViewBaseAdapter) {
            processBaseAdapter((RecyclerViewBaseAdapter) adapter.getInnerAdapter(), realPosition);
        }
        return adapter.isWrapperViewByPosition(position)
                ? RecyclerView.NO_POSITION
                : realPosition;
    }

    private View correctView(View vItem, MotionEvent e) {
        if (mChildViewIds != null) {
            for (int id : mChildViewIds) {
                View vChild = vItem.findViewById(id);
                if (vChild != null && inRangeOfView(vChild, e)) {
                    return vChild;
                }
            }
        }
        if (mChildViewTags != null) {
            for (Object tag: mChildViewTags) {
                View vChild = vItem.findViewWithTag(tag);
                if (vChild != null && inRangeOfView(vChild, e)) {
                    return vChild;
                }
            }
        }
        if (mMinRangeEnable) {
            if (vItem instanceof ViewGroup) {
                View target = findTouchedView((ViewGroup) vItem, e);
                if (target != null) {
                    return target;
                }
            }
        }
//        if (mMinRangeEnable) {
//            if (vItem instanceof ViewGroup) {
//                View target = findTouchedView((ViewGroup) vItem, e);
//                if (target != null) {
//                    return target;
//                }
//            }
//        } else if (mChildViewIds != null) {
//            for (int id : mChildViewIds) {
//                View vChild = vItem.findViewById(id);
//                if (vChild != null && inRangeOfView(vChild, e)) {
//                    return vChild;
//                }
//            }
//        }
        return vItem;
    }

    private View findTouchedView(ViewGroup parent, MotionEvent e) {
        View child;
        for (int i = 0, size = parent.getChildCount();i < size;++i) {
            child = parent.getChildAt(i);
            if (inRangeOfView(child, e)) {
                if (child instanceof ViewGroup) {
                    return findTouchedView((ViewGroup) child, e);
                } else {
                    return child;
                }
            }
        }
        return null;
    }

    private boolean inRangeOfView(View view, MotionEvent ev){
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        if(ev.getRawX() < x
                || ev.getRawX() > (x + view.getWidth())
                || ev.getRawY() < y
                || ev.getRawY() > (y + view.getHeight())){
            return false;
        }
//        if(ev.getX() < x || ev.getX() > (x + view.getWidth()) || ev.getY() < y || ev.getY() > (y + view.getHeight())){
//            return false;
//        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        View vItem = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
        if (vItem != null) {
            int position = correctPosition(mRecyclerView.getChildAdapterPosition(vItem));
            if (position != RecyclerView.NO_POSITION) {
                onItemLongClick(correctView(vItem, e), position);
            }
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    public abstract void onItemClick(View v, int position);

    public void onItemLongClick(View v, int position) {
    }
}
