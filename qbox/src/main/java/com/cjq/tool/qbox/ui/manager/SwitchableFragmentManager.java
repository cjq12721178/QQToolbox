package com.cjq.tool.qbox.ui.manager;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;


/**
 * Created by CJQ on 2017/7/6.
 */

public class SwitchableFragmentManager<F extends Fragment & OnDataSetChangedListener> {

    private static final int MESSAGE_DATA_SET_CHANGED = 1;

    private F mCurrentFragment;
    private final FragmentManager mFragmentManager;
    private final @IdRes int mParentViewId;
    private final String[] mFragmentTags;
    private final Class<F>[] mFragmentClasses;

    public SwitchableFragmentManager(FragmentManager fragmentManager,
                                     int parentViewId,
                                     String[] fragmentTags,
                                     Class<F>[] fragmentClasses) {
        if (fragmentManager == null) {
            throw new NullPointerException("fragment manager can not be null");
        }
        if (isTagsNullOrEmpty(fragmentTags) || isClassesNullOrIllegal(fragmentClasses)) {
            throw new NullPointerException("fragment tags and classes can not be null");
        }
        if (fragmentTags.length != fragmentClasses.length) {
            throw new IllegalArgumentException("fragment tags and classes should be coupled");
        }
        mFragmentManager = fragmentManager;
        mParentViewId = parentViewId;
        mFragmentTags = fragmentTags;
        mFragmentClasses = fragmentClasses;
    }

    private boolean isTagsNullOrEmpty(String[] tags) {
        if (tags == null) {
            return true;
        }
        for (int i = 0;i < tags.length;++i) {
            if (TextUtils.isEmpty(tags[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean isClassesNullOrIllegal(Class<F>[] classes) {
        if (classes == null) {
            return true;
        }
        for (int i = 0;i < classes.length;++i) {
            if (classes[i] == null ||
                    !Fragment.class.isAssignableFrom(classes[i]) ||
                    !OnDataSetChangedListener.class.isAssignableFrom(classes[i])) {
                return true;
            }
        }
        return false;
    }

    public F getCurrentFragment() {
        return mCurrentFragment;
    }

    public F switchTo(int fragmentIndex) {
        return switchTo(fragmentIndex < 0 || fragmentIndex >= mFragmentTags.length
                ? null
                : mFragmentTags[fragmentIndex]);
    }

    public F switchTo(String tag) {
        F from = mCurrentFragment;
        F to = TextUtils.isEmpty(tag)
                ? null : getFragmentByTag(tag);
        if (from == to) {
            return mCurrentFragment;
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (from != null) {
            transaction.hide(from);
        }
        boolean needNotifyDataSetChanged = false;
        if (to != null) {
            if (!to.isAdded()) {
                needNotifyDataSetChanged = true;
                transaction.add(mParentViewId, to, tag);
            }
            if (to.isHidden()) {
                transaction.show(to);
            }
        }
        transaction.commit();
        mCurrentFragment = to;
        if (needNotifyDataSetChanged) {
            notifyDataSetChanged();
        }
        return from;
    }

    public F getFragmentByTag(String tag) {
        Fragment result = mFragmentManager.findFragmentByTag(tag);
        return result == null ? createFragment(tag) : (F)result;
    }

    private F createFragment(String tag) {
        for (int i = 0;i < mFragmentTags.length;++i) {
            if (mFragmentTags[i].equals(tag)) {
                try {
                    return mFragmentClasses[i].newInstance();
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }

    public void notifyDataSetChanged() {
        mHandler.sendEmptyMessage(MESSAGE_DATA_SET_CHANGED);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_DATA_SET_CHANGED) {
                if (mCurrentFragment != null) {
                    mCurrentFragment.onDataSetChanged();
                }
            }
        }
    };
}
