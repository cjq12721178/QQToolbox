package com.cjq.tool.qbox.ui.manager;

import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;


/**
 * Created by CJQ on 2017/7/6.
 */

public class SwitchableFragmentManager<F extends Fragment> {

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
        if (isArrayOrComponentNull(fragmentTags) || isArrayOrComponentNull(fragmentClasses)) {
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

    private <T> boolean isArrayOrComponentNull(T[] array) {
        if (array == null) {
            return true;
        }
        for (int i = 0;i < array.length;++i) {
            if (array[i] == null) {
                return true;
            } else if (array[i] instanceof String && array[i] == "") {
                return true;
            }
        }
        return false;
    }

    public F getCurrentFragment() {
        return mCurrentFragment;
    }

    public F switchTo(int fragmentIndex) {
        if (fragmentIndex < 0 || fragmentIndex >= mFragmentTags.length) {
            throw new IllegalArgumentException("out of fragment tags bounds");
        }
        return switchTo(mFragmentTags[fragmentIndex]);
    }

    public F switchTo(String tag) {
        if (TextUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("fragment tag which to be switched can not be empty");
        }
        F from = mCurrentFragment;
        F to = getFragmentByTag(tag);
        if (from == to) {
            return mCurrentFragment;
        }
        if (to == null) {
            return null;
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (from != null) {
            transaction.hide(from);
        }
        if (!to.isAdded()) {
            transaction.add(mParentViewId, to, tag);
        }
        if (to.isHidden()) {
            transaction.show(to);
        }
        transaction.commit();
        mCurrentFragment = to;
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
        throw new IllegalArgumentException("can not get fragment by tag");
    }
}
