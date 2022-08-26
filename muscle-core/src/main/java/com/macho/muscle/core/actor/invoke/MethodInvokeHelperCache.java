package com.macho.muscle.core.actor.invoke;

import com.google.common.collect.Maps;

import java.util.Map;

public class MethodInvokeHelperCache {
    private static final Map<Class<?>, MethodInvokeHelper> classToMethodInvokeHelperMap = Maps.newConcurrentMap();

    public static <T> MethodInvokeHelper<T> getMethodInvokeHelper(Class<?> targetClass) {
        return classToMethodInvokeHelperMap.computeIfAbsent(targetClass, MethodInvokeHelper::new);
    }

    public static <T> void registerClass(Class<T> targetClass) {
        classToMethodInvokeHelperMap.put(targetClass, new MethodInvokeHelper<>(targetClass));
    }
}
