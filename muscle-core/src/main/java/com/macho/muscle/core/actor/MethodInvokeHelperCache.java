package com.macho.muscle.core.actor;

import com.google.common.collect.Maps;

import java.util.Map;

public class MethodInvokeHelperCache {
    private static final Map<Class<?>, MethodInvokeHelper<?>> classToMethodInvokeHelperMap = Maps.newConcurrentMap();

    public static MethodInvokeHelper<?> getMethodInvokeHelper(Class<?> targetClass) {
        return classToMethodInvokeHelperMap.computeIfAbsent(targetClass, MethodInvokeHelper::new);
    }
}
