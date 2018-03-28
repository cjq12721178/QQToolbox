package com.cjq.lib.weisi.util;

import android.support.annotation.NonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by CJQ on 2018/3/28.
 */

public class SimpleReflection {

    private SimpleReflection() {
    }

    public static Type getInterfaceParameterizedType(@NonNull Object interfaceInstance, @NonNull Class interfaceClass, int interfaceTypeParameterPosition) {
        Class c = interfaceInstance.getClass();
        do {
            Type[] interfaceTypes = c.getGenericInterfaces();
            if (interfaceTypes == null) {
                throw new IllegalArgumentException("interfaceInstance may implement an interface");
            }
            Type interfaceType;
            for (int i = 0;i < interfaceTypes.length;++i) {
                interfaceType = interfaceTypes[i];
                if (interfaceType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) interfaceType;
                    if (parameterizedType.getRawType() == interfaceClass) {
                        if (interfaceTypeParameterPosition < 0 || interfaceTypeParameterPosition >= parameterizedType.getActualTypeArguments().length) {
                            throw new IllegalArgumentException("interface type parameter position out of bounds");
                        }
                        return parameterizedType.getActualTypeArguments()[interfaceTypeParameterPosition];
                    }
                }
            }
            c = c.getSuperclass();
        } while (!c.isInstance(Object.class));
        throw new IllegalArgumentException("no proper parameter");
    }

    public static Type getClassParameterizedType(@NonNull Object classInstance, @NonNull Class classClass, int classTypeParameterPosition) {
        Class c = classInstance.getClass();
        do {
            Type interfaceType = c.getGenericSuperclass();
            if (interfaceType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) interfaceType;
                if (classTypeParameterPosition < 0 || classTypeParameterPosition >= parameterizedType.getActualTypeArguments().length) {
                    throw new IllegalArgumentException("class type parameter position out of bounds");
                }
                return parameterizedType.getActualTypeArguments()[classTypeParameterPosition];
            }
            c = c.getSuperclass();
        } while (!c.isInstance(Object.class));
        throw new IllegalArgumentException("no proper parameter");
    }
}
