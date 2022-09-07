package com.macho.muscle.core.actor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.macho.muscle.core.utils.MuscleReflectUtil;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MethodInvokeHelper<T> {
    private final Class<T> targetClass;
    private final Map<String, MethodHandle> methodNameToIndexMap = Maps.newHashMap();

    public MethodInvokeHelper(Class<T> targetClass) {
        this.targetClass = targetClass;

        try {
            initMethodIndex();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void initMethodIndex() throws NoSuchMethodException, IllegalAccessException {
        List<Method> methods = MuscleReflectUtil.getAllDeclaredMethods(targetClass);
        if (!CollectionUtils.isEmpty(methods)) {
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            for (Method method : methods) {
                MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
                MethodHandle methodHandle = lookup.findVirtual(targetClass, method.getName(), methodType);

                String fullMethodName = MuscleReflectUtil.getFullMethodName(method);

                methodNameToIndexMap.put(fullMethodName, methodHandle);
            }
        }
    }

    public <R> R doInvoke(T target, String fullMethodName, Object[] args) throws Throwable {
        if (!methodNameToIndexMap.containsKey(fullMethodName)) {
            throw new IllegalStateException("can not find method:" + fullMethodName);
        }

        MethodHandle methodHandle = methodNameToIndexMap.get(fullMethodName);
        List<Object> argList = Lists.newArrayList(target);
        if (args != null && args.length > 0) {
            argList.addAll(Arrays.stream(args).toList());
        }

        return (R) methodHandle.invokeWithArguments(argList);
    }
}
