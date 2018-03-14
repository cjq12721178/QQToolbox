package com.cjq.lib.weisi.node;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Created by CJQ on 2018/3/14.
 */

public class ScriptValueCorrector implements ValueCorrector {

    private final CompiledScript mScript;

    private ScriptValueCorrector(@NonNull CompiledScript script) {
        mScript = script;
    }

    @Override
    public double correct(double value) {
        mScript.getEngine().getContext().setAttribute("v", value, ScriptContext.ENGINE_SCOPE);
        try {
            return (double) mScript.eval();
        } catch (Exception e) {
            return value;
        }
    }

    public static class Builder {

        private final String mFunctionTemplate = "function correctValue(v) { return %s; } correctValue(v)";
        private final Map<String, ScriptValueCorrector> mCorrectorMap = new HashMap<>();
        private Compilable mCompilable;

        //function示例：
        //1. v / 1000
        //2. 10 + v * 2
        public void putScript(String label, String function) {
            if (TextUtils.isEmpty(label) || TextUtils.isEmpty(function)) {
                return;
            }
            try {
                CompiledScript script = getCompilable().compile(String.format(mFunctionTemplate, function));
                ScriptValueCorrector corrector = new ScriptValueCorrector(script);
                if (doubleIsDifferent(1, corrector.correct(1), 0.0001)) {
                    mCorrectorMap.put(label, corrector);
                }
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }

        public ScriptValueCorrector getCorrector(String label) {
            return mCorrectorMap.get(label);
        }

        private Compilable getCompilable() {
            if (mCompilable == null) {
                ScriptEngineManager manager = new ScriptEngineManager();
                ScriptEngine engine = manager.getEngineByName("rhino");
                mCompilable = (Compilable) engine;
            }
            return mCompilable;
        }

        private boolean doubleIsDifferent(double d1, double d2, double delta) {
            if (Double.compare(d1, d2) == 0) {
                return false;
            }
            if ((Math.abs(d1 - d2) <= delta)) {
                return false;
            }
            return true;
        }
    }
}
