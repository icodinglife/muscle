package com.macho.muscle.core.utils;

import com.google.common.collect.Lists;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class MuscleReflectUtil {
    /**
     * join method name and parameter type name
     */
    public static final String getFullMethodName(Method method) {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();

        StringBuilder sb = new StringBuilder(methodName);

        if (parameterTypes != null && parameterTypes.length > 0) {
            for (Class<?> parameterType : parameterTypes) {
                sb.append("_").append(parameterType.getName());
            }
        }

        return sb.toString();
    }

    public static List<Method> getAllDeclaredMethods(Class<?> clazz) {
        boolean isInterface = clazz.isInterface();
        if (!isInterface && clazz.getSuperclass() == null && clazz != Object.class) {
            throw new IllegalArgumentException("The class type must not be an interface, a primitive type, or void.");
        }

        List<Method> methods = Lists.newArrayList();

        if (!isInterface) {
            Class nextClass = clazz;
            while (nextClass != Object.class) {
                addDeclaredMethodsToList(nextClass, methods);

                nextClass = nextClass.getSuperclass();
            }
        } else {
            recursiveAddInterfaceMethodsToList(clazz, methods);
        }

        return methods;
    }

    private static void addDeclaredMethodsToList(Class type, List<Method> methods) {
        Method[] declaredMethods = type.getDeclaredMethods();
        for (int i = 0, n = declaredMethods.length; i < n; i++) {
            Method method = declaredMethods[i];

            int modifiers = method.getModifiers();
            if (Modifier.isPrivate(modifiers)) {
                continue;
            }

            methods.add(method);
        }
    }

    private static void recursiveAddInterfaceMethodsToList(Class interfaceType, List<Method> methods) {
        addDeclaredMethodsToList(interfaceType, methods);
        for (Class nextInterface : interfaceType.getInterfaces()) {
            recursiveAddInterfaceMethodsToList(nextInterface, methods);
        }
    }
}
