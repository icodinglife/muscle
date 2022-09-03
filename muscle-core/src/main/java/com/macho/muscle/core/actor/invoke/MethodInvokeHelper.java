//package com.macho.muscle.core.actor.invoke;
//
//import com.esotericsoftware.reflectasm.MethodAccess;
//import com.google.common.collect.Maps;
//import com.macho.muscle.core.utils.MuscleReflectUtil;
//import lombok.Builder;
//import lombok.Data;
//import org.springframework.util.CollectionUtils;
//
//import java.lang.reflect.Method;
//import java.lang.reflect.Modifier;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//
//public class MethodInvokeHelper<T> {
//    private Class<T> targetClass;
//    private final MethodAccess targetMethodAccess;
//    private final Map<String, MethodInvokeProperties> methodNameToIndexMap = Maps.newHashMap();
//
//    public MethodInvokeHelper(Class<T> targetClass) {
//        this.targetClass = targetClass;
//        this.targetMethodAccess = MethodAccess.get(targetClass);
//
//        initMethodIndex();
//    }
//
//    private void initMethodIndex() {
//        List<Method> methods = MuscleReflectUtil.getAllDeclaredMethods(targetClass);
//        if (!CollectionUtils.isEmpty(methods)) {
//            for (Method method : methods) {
//                int modifiers = method.getModifiers();
//                if (Modifier.isPrivate(modifiers) || Modifier.isStatic(modifiers)) {
//                    continue;
//                }
//
//                String methodName = method.getName();
//                Class<?>[] parameterTypes = method.getParameterTypes();
//                String fullMethodName = MuscleReflectUtil.getFullMethodName(method);
//
//                // todo is return future ok?
////                if (method.getReturnType() != void.class && method.getReturnType() != CompletableFuture.class) {
////                    throw new UnsupportedOperationException("must return CompletableFuture");
////                }
//
//                MethodInvokeProperties methodInvokeProperties = MethodInvokeProperties.builder()
//                        .returnVoid(method.getReturnType() == void.class)
//                        .invokeIndex(targetMethodAccess.getIndex(methodName, parameterTypes))
//                        .build();
//
//                methodNameToIndexMap.put(fullMethodName, methodInvokeProperties);
//            }
//        }
//    }
//
//    public <R> CompletableFuture<R> doInvoke(T instance, String fullMethodName, Object[] args) {
//        if (!methodNameToIndexMap.containsKey(fullMethodName)) {
//            throw new IllegalStateException("can not find method:" + fullMethodName);
//        }
//
//        MethodInvokeProperties methodInvokeProperties = methodNameToIndexMap.get(fullMethodName);
//
//        Object result = targetMethodAccess.invoke(instance, methodInvokeProperties.invokeIndex, args);
//
//        if (methodInvokeProperties.returnVoid) {
//            return null;
//        }
//
//        return (CompletableFuture<R>) result;
//    }
//
//    @Data
//    @Builder
//    public static class MethodInvokeProperties {
//        private final boolean returnVoid;
//        private final int invokeIndex;
//    }
//}
