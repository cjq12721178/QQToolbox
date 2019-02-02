package com.cjq.lib.weisi.iot.interpreter;

import android.support.annotation.NonNull;

import java.util.Map;

/**
 * Created by KAT on 2016/11/23.
 */
public class ParaphraseInterpreter implements ValueInterpreter {

    private final Map<Double, String> paraphrases;

    public ParaphraseInterpreter(Map<Double, String> paraphrases) {
        this.paraphrases = paraphrases;
    }

    @Override
    public @NonNull String interpret(double value) {
        String result = paraphrases.get(value);
        if (result != null) {
            return result;
        }
        return "";
    }
}
