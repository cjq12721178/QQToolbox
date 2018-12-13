package com.wsn.lib.wsb.util

import com.sun.istack.internal.NotNull

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Created by CJQ on 2018/3/28.
 */

object SimpleReflection {

    @JvmStatic
    fun getInterfaceParameterizedType(interfaceInstance: Any, interfaceClass: Class<*>, interfaceTypeParameterPosition: Int): Type {
        var c: Class<*> = interfaceInstance.javaClass
        do {
            val interfaceTypes = c.genericInterfaces
                    ?: throw IllegalArgumentException("interfaceInstance may implement an interface")
            var interfaceType: Type
            for (i in interfaceTypes.indices) {
                interfaceType = interfaceTypes[i]
                if (interfaceType is ParameterizedType) {
                    val parameterizedType = interfaceType
                    if (parameterizedType.rawType === interfaceClass) {
                        if (interfaceTypeParameterPosition < 0 || interfaceTypeParameterPosition >= parameterizedType.actualTypeArguments.size) {
                            throw IllegalArgumentException("interface type parameter position out of bounds")
                        }
                        return parameterizedType.actualTypeArguments[interfaceTypeParameterPosition]
                    }
                }
            }
            c = c.superclass
        } while (!c.isInstance(Any::class.java))
        throw IllegalArgumentException("no proper parameter")
    }

    @JvmStatic
    fun getClassParameterizedType(classInstance: Any, classTypeParameterPosition: Int): Type {
        var c: Class<*> = classInstance.javaClass
        do {
            val interfaceType = c.genericSuperclass
            if (interfaceType is ParameterizedType) {
                if (classTypeParameterPosition < 0 || classTypeParameterPosition >= interfaceType.actualTypeArguments.size) {
                    throw IllegalArgumentException("class type parameter position out of bounds")
                }
                return interfaceType.actualTypeArguments[classTypeParameterPosition]
            }
            c = c.superclass
        } while (!c.isInstance(Any::class.java))
        throw IllegalArgumentException("no proper parameter")
    }
}
