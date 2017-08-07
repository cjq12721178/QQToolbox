package com.cjq.lib.weisi.sensor;

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
    public String interpret(double value) {
        return paraphrases.get(value);
    }
}
