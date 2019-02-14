package com.cjq.tool.qqtoolbox.switchable_fragment_manager;

import android.support.v4.app.Fragment;

/**
 * Created by CJQ on 2017/7/7.
 */

public abstract class VisualFragment extends Fragment {

    protected Student mStudent;

    public void setStudent(Student student) {
        mStudent = student;
    }

    public static class Student {

        private String mName;
        private int mAge;

        public Student(String name, int age) {
            mName = name;
            mAge = age;
        }

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            mName = name;
        }

        public int getAge() {
            return mAge;
        }

        public void setAge(int age) {
            mAge = age;
        }
    }
}
