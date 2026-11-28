/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.jmx;

import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ParameterTypes;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.wrappers.Wrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Resolves overloaded JMX MBean operations and prepares invocation arguments
 * using Groovy's dynamic parameter distance and coercion rules.
 */
final class MBeanOperationResolver {

    private static final int MAX_ARRAY_DIMENSIONS = 255;

    private static final ParameterTypes EMPTY_PARAMETER_TYPES =
            createParameterTypes(MetaClassHelper.EMPTY_TYPE_ARRAY);

    /**
     * Sentinel cached when an operation's parameter types cannot be resolved
     * from standard (bootstrap or ancestor) class loaders, avoiding repeated
     * {@code ClassNotFoundException} lookups on hot paths while still retrying
     * when a foreign class loader is present.
     */
    private static final ParameterTypes UNRESOLVED_STANDARD =
            createParameterTypes(MetaClassHelper.EMPTY_TYPE_ARRAY);

    private static final Map<String, Class<?>> PRIMITIVE_TYPES = Map.of(
            "boolean", boolean.class,
            "byte", byte.class,
            "char", char.class,
            "short", short.class,
            "int", int.class,
            "long", long.class,
            "float", float.class,
            "double", double.class);

    private MBeanOperationResolver() {
    }

    static List<Operation> filter(List<Operation> grouped, String method, int arity) {
        if (grouped == null) {
            return List.of();
        }
        boolean allMatch = true;
        for (Operation operation : grouped) {
            if (!operation.matches(method, arity)) {
                allMatch = false;
                break;
            }
        }
        if (allMatch) {
            return grouped;
        }
        List<Operation> matched = new ArrayList<>(grouped.size());
        for (Operation operation : grouped) {
            if (operation.matches(method, arity)) {
                matched.add(operation);
            }
        }
        return matched;
    }

    /**
     * Selects the best matching overload using Groovy's dynamic parameter
     * distance and coercion rules. Among candidates with equal distance, a
     * strictly more specific signature wins; any remaining tie or unmatched
     * call falls back to the last registered operation.
     */
    static Selection select(List<Operation> matches, Object[] args) {
        Class<?>[] argTypes = argumentTypes(args);
        Candidate best = null;
        long bestDistance = Long.MAX_VALUE;
        List<Candidate> tied = null;

        for (Operation operation : matches) {
            ParameterTypes types = operation.parameterTypes(argTypes);
            if (types == null || !isApplicable(types, argTypes, args)) {
                continue;
            }
            long distance = MetaClassHelper.calculateParameterDistance(argTypes, types);
            if (best == null || distance < bestDistance) {
                bestDistance = distance;
                best = new Candidate(operation, types);
                tied = null;
            } else if (distance == bestDistance) {
                if (tied == null) {
                    tied = new ArrayList<>(2);
                    tied.add(best);
                }
                tied.add(new Candidate(operation, types));
            }
        }

        if (best == null) {
            return new Selection(matches.get(matches.size() - 1).signature, prepareArguments(args, null));
        }
        if (tied != null) {
            best = breakTie(tied);
        }
        return new Selection(best.operation().signature, prepareArguments(args, best.types()));
    }

    static Object[] prepareArguments(Object[] args, ParameterTypes types) {
        if (args.length == 0) {
            return args;
        }
        CachedClass[] params = types == null ? null : types.getParameterTypes();
        Object[] callArgs = null;
        for (int i = 0; i < args.length; i++) {
            Object original = args[i];
            Object value = unwrap(original);
            if (params != null && value != null) {
                value = params[i].coerceArgument(value);
            }
            if (value != original) {
                if (callArgs == null) {
                    // Use Object[].class so storing a coerced value into a covariant array
                    // (e.g. BigDecimal[] or GString[]) cannot trigger ArrayStoreException
                    callArgs = Arrays.copyOf(args, args.length, Object[].class);
                }
                callArgs[i] = value;
            }
        }
        return callArgs != null ? callArgs : args;
    }

    private static Class<?>[] argumentTypes(Object[] args) {
        return args.length == 0
                ? MetaClassHelper.EMPTY_TYPE_ARRAY
                : MetaClassHelper.convertToTypeArray(args);
    }

    private static boolean isApplicable(ParameterTypes types, Class<?>[] argTypes, Object[] args) {
        CachedClass[] params = types.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            CachedClass param = params[i];
            if (param.isPrimitive() && unwrap(args[i]) == null) {
                return false;
            }
            if (!param.isAssignableFrom(argTypes[i])) {
                return false;
            }
        }
        return true;
    }

    private static Candidate breakTie(List<Candidate> tied) {
        Candidate chosen = tied.get(tied.size() - 1);
        for (Candidate candidate : tied) {
            if (!isDominated(candidate, tied)) {
                chosen = candidate;
            }
        }
        return chosen;
    }

    private static boolean isDominated(Candidate candidate, List<Candidate> tied) {
        Class<?>[] candidateTypes = candidate.types().getNativeParameterTypes();
        for (Candidate other : tied) {
            if (other != candidate && isStrictlyMoreSpecific(other.types().getNativeParameterTypes(), candidateTypes)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isStrictlyMoreSpecific(Class<?>[] left, Class<?>[] right) {
        boolean stricter = false;
        for (int i = 0; i < left.length; i++) {
            if (!right[i].isAssignableFrom(left[i])) {
                return false;
            }
            if (!left[i].isAssignableFrom(right[i])) {
                stricter = true;
            }
        }
        return stricter;
    }

    private static Object unwrap(Object arg) {
        return arg instanceof Wrapper wrapper ? wrapper.unwrap() : arg;
    }

    private static Class<?> loadType(String typeName, Class<?>[] argTypes, int index) {
        if (typeName == null || typeName.isEmpty()) {
            return null;
        }
        int dimensions = 0;
        String baseName = typeName;
        if (baseName.charAt(0) != '[') {
            while (baseName.endsWith("[]")) {
                dimensions++;
                if (dimensions > MAX_ARRAY_DIMENSIONS) {
                    return null;
                }
                baseName = baseName.substring(0, baseName.length() - 2);
            }
        }
        Class<?> type = PRIMITIVE_TYPES.get(baseName);
        if (type == null) {
            type = loadClass(baseName, argTypes, index);
        }
        if (type == null) {
            return null;
        }
        for (int i = 0; i < dimensions; i++) {
            type = type.arrayType();
        }
        return type;
    }

    private static Class<?> loadClass(String className, Class<?>[] argTypes, int index) {
        ClassLoader primary = argTypes[index] == null ? null : argTypes[index].getClassLoader();
        if (primary != null) {
            Class<?> type = tryLoad(className, primary);
            if (type != null) {
                return type;
            }
        }
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if (tccl != null && tccl != primary) {
            Class<?> type = tryLoad(className, tccl);
            if (type != null) {
                return type;
            }
        }
        ClassLoader ownLoader = GroovyMBean.class.getClassLoader();
        if (!sameNonNullLoader(ownLoader, primary) && !sameNonNullLoader(ownLoader, tccl)) {
            Class<?> type = tryLoad(className, ownLoader);
            if (type != null) {
                return type;
            }
        }
        return loadFromOtherArgs(className, argTypes, index, primary, tccl, ownLoader);
    }

    private static boolean sameNonNullLoader(ClassLoader candidate, ClassLoader tried) {
        return tried != null && candidate == tried;
    }

    private static Class<?> loadFromOtherArgs(String className, Class<?>[] argTypes, int skipIndex,
                                              ClassLoader primary, ClassLoader tccl, ClassLoader ownLoader) {
        for (int i = 0; i < argTypes.length; i++) {
            if (i == skipIndex || argTypes[i] == null) {
                continue;
            }
            ClassLoader loader = argTypes[i].getClassLoader();
            if (loader != null && loader != primary && loader != tccl && loader != ownLoader) {
                Class<?> type = tryLoad(className, loader);
                if (type != null) {
                    return type;
                }
            }
        }
        return null;
    }

    private static Class<?> tryLoad(String typeName, ClassLoader loader) {
        try {
            return Class.forName(typeName, false, loader);
        } catch (ClassNotFoundException | LinkageError | SecurityException
                 | IllegalArgumentException | IllegalStateException ex) {
            return null;
        }
    }

    private static boolean hasForeignLoader(Class<?>[] argTypes) {
        if (!isStandardLoader(Thread.currentThread().getContextClassLoader())) {
            return true;
        }
        for (Class<?> argType : argTypes) {
            if (argType != null && !isStandardLoader(argType.getClassLoader())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isStandardLoader(ClassLoader loader) {
        if (loader == null) {
            return true;
        }
        for (ClassLoader ancestor = GroovyMBean.class.getClassLoader(); ancestor != null; ancestor = ancestor.getParent()) {
            if (ancestor == loader) {
                return true;
            }
        }
        return false;
    }

    private static ParameterTypes createParameterTypes(Class<?>[] classes) {
        ParameterTypes types = new ParameterTypes(classes);
        types.getParameterTypes();
        return types;
    }

    static final class Operation {
        final String name;
        final String[] signature;
        // AtomicReference rather than volatile: avoids java:S3077 while publishing ParameterTypes safely
        private final AtomicReference<ParameterTypes> cachedTypes;

        Operation(String name, String[] signature) {
            this.name = name;
            this.signature = signature;
            this.cachedTypes = new AtomicReference<>(signature.length == 0 ? EMPTY_PARAMETER_TYPES : null);
        }

        boolean matches(String method, int arity) {
            return signature.length == arity && Objects.equals(name, method);
        }

        ParameterTypes parameterTypes(Class<?>[] argTypes) {
            ParameterTypes cached = cachedTypes.get();
            if (cached != null) {
                if (cached != UNRESOLVED_STANDARD) {
                    return cached;
                }
                if (!hasForeignLoader(argTypes)) {
                    return null;
                }
            }
            return resolveParameterTypes(argTypes);
        }

        private ParameterTypes resolveParameterTypes(Class<?>[] argTypes) {
            Class<?>[] resolved = new Class<?>[signature.length];
            boolean cacheable = true;
            for (int i = 0; i < signature.length; i++) {
                Class<?> type = loadType(signature[i], argTypes, i);
                if (type == null) {
                    if (!hasForeignLoader(argTypes)) {
                        cachedTypes.compareAndSet(null, UNRESOLVED_STANDARD);
                    }
                    return null;
                }
                if (!isStandardLoader(type.getClassLoader())) {
                    cacheable = false;
                }
                resolved[i] = type;
            }
            ParameterTypes types = createParameterTypes(resolved);
            if (cacheable) {
                cachedTypes.compareAndSet(null, types);
            }
            return types;
        }
    }

    private record Candidate(Operation operation, ParameterTypes types) {
    }

    static final class Selection {
        final String[] signature;
        final Object[] arguments;

        Selection(String[] signature, Object[] arguments) {
            this.signature = signature;
            this.arguments = arguments;
        }
    }
}
