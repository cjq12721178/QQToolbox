package com.cjq.lib.weisi;

import org.junit.Test;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static org.junit.Assert.*;

/**
 * Created by CJQ on 2018/3/14.
 */

public class CallJavaScript {

    @Test
    public void test_js_fun() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("rhino");
        String jsFunStr = "function buildValue(v) { return v/1000; } buildValue(value)";
        Compilable compilable = (Compilable) engine;
        //Bindings bindings = engine.createBindings();
        Bindings bindings;
        double expect = 0.0;
        try {
            CompiledScript jsFun = compilable.compile(jsFunStr);
            bindings = jsFun.getEngine().getBindings(ScriptContext.ENGINE_SCOPE);
            if (bindings == null) {
                System.out.println("create bindings");
                bindings = jsFun.getEngine().createBindings();
            }
            bindings.put("value", 23456);
//            Object result = jsFun.eval(bindings);
//            expect = Double.parseDouble(result.toString());
            expect = (double) jsFun.eval(bindings);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        assertEquals(expect, 23.456, 0.0001);
    }

    @Test
    public void test_js_fun2() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("rhino");
        String jsFunStr = "function buildValue(v) { return v/1000; } buildValue(value)";
        Compilable compilable = (Compilable) engine;
        double expect = 0.0;
        try {
            CompiledScript jsFun = compilable.compile(jsFunStr);
            ScriptContext context = jsFun.getEngine().getContext();
            context.setAttribute("value", 23456, ScriptContext.ENGINE_SCOPE);
            expect = (double) jsFun.eval();
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        assertEquals(expect, 23.456, 0.0001);
    }
}
