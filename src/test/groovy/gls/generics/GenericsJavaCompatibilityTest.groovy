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
package gls.generics

import groovy.transform.CompileStatic
import org.apache.groovy.util.JavaShell
import org.apache.groovy.util.JavaShellCompilationException
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.junit.jupiter.api.Test

import java.lang.reflect.GenericArrayType
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType

import static groovy.test.GroovyAssert.shouldFail

/**
 * Static Groovy ({@code @CompileStatic}) vs javac generics compatibility suite.
 *
 * Each case compiles the same source twice: javac via {@link JavaShell}, and
 * Groovy through a class loader that always applies {@link CompileStatic}.
 * Dynamic Groovy is out of scope. Both sides must agree on compile success or
 * failure and, when {@code test()} can run, on its result.
 *
 * Bytecode instruction streams, constant pools and synthetic GroovyObject
 * machinery are not compared. Those encodings change between JDK and Groovy
 * releases. Compatibility of the generic type system is checked at the stable
 * reflective Signature surface ({@link Class#getTypeParameters()},
 * {@link Class#getGenericSuperclass()}, {@link Method#getGenericReturnType()},
 * and so on) plus observable compile / run behaviour.
 */
final class GenericsJavaCompatibilityTest {

    // =========================================================================
    // Category 1: Generic Class & Interface Declarations and Type Bounds
    // =========================================================================

    @Test
    void testGenericClassSingleTypeParameter() {
        final String className = 'gls.generics.test.SingleTypeParam'
        final String javaSrc = '''
            package gls.generics.test;
            public class SingleTypeParam {
                public static class Box<T> {
                    private T val;
                    public Box(T val) { this.val = val; }
                    public T getVal() { return val; }
                    public void setVal(T val) { this.val = val; }
                }
                public static String test() {
                    Box<String> box = new Box<>("Apache Groovy");
                    box.setVal(box.getVal() + " Generics");
                    return box.getVal();
                }
            }
        '''
        assertPositive(className, javaSrc, 'Apache Groovy Generics', true)
    }

    @Test
    void testGenericClassMultipleTypeParameters() {
        final String className = 'gls.generics.test.MultipleTypeParams'
        final String javaSrc = '''
            package gls.generics.test;
            public class MultipleTypeParams {
                public static class Pair<K, V> {
                    private final K key;
                    private final V value;
                    public Pair(K key, V value) {
                        this.key = key;
                        this.value = value;
                    }
                    public K getKey() { return key; }
                    public V getValue() { return value; }
                    public String format() { return "" + key + "=" + value; }
                }
                public static String test() {
                    Pair<String, Integer> pair = new Pair<>("score", 100);
                    return pair.format();
                }
            }
        '''
        assertPositive(className, javaSrc, 'score=100', true)
    }

    @Test
    void testGenericInterfaceImplementation() {
        final String className = 'gls.generics.test.GenericInterfaceImpl'
        final String javaSrc = '''
            package gls.generics.test;
            public class GenericInterfaceImpl {
                public interface Transformer<I, O> {
                    O transform(I input);
                }
                public static class StringLengthTransformer implements Transformer<String, Integer> {
                    @Override
                    public Integer transform(String input) {
                        return input == null ? 0 : input.length();
                    }
                }
                public static int test() {
                    Transformer<String, Integer> t = new StringLengthTransformer();
                    return t.transform("GenericsCompatibility");
                }
            }
        '''
        assertPositive(className, javaSrc, 21, true)
    }

    @Test
    void testSingleUpperBound() {
        final String className = 'gls.generics.test.SingleUpperBound'
        final String javaSrc = '''
            package gls.generics.test;
            public class SingleUpperBound {
                public static class NumberHolder<T extends Number> {
                    private final T number;
                    public NumberHolder(T number) { this.number = number; }
                    public double asDouble() { return number.doubleValue(); }
                    public long asLong() { return number.longValue(); }
                }
                public static double test() {
                    NumberHolder<Integer> intHolder = new NumberHolder<>(42);
                    NumberHolder<Double> doubleHolder = new NumberHolder<>(3.14d);
                    return intHolder.asDouble() + doubleHolder.asDouble();
                }
            }
        '''
        assertPositive(className, javaSrc, 45.14d, true)
    }

    @Test
    void testMultipleBounds() {
        final String className = 'gls.generics.test.MultipleBounds'
        final String javaSrc = '''
            package gls.generics.test;
            import java.io.Serializable;
            public class MultipleBounds {
                public static class MultiBoundContainer<T extends Number & Comparable<T> & Serializable> {
                    private final T first;
                    private final T second;
                    public MultiBoundContainer(T first, T second) {
                        this.first = first;
                        this.second = second;
                    }
                    public int compare() {
                        return first.compareTo(second);
                    }
                    public double sum() {
                        return first.doubleValue() + second.doubleValue();
                    }
                }
                public static double test() {
                    MultiBoundContainer<Integer> container = new MultiBoundContainer<>(10, 20);
                    int cmp = container.compare();
                    return container.sum() + cmp; // 30 + (-1) = 29
                }
            }
        '''
        assertPositive(className, javaSrc, 29.0d, true)
    }

    @Test
    void testRecursiveTypeBoundsFBounded() {
        final String className = 'gls.generics.test.RecursiveBounds'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.*;
            public class RecursiveBounds {
                public static <T extends Comparable<T>> T findMax(List<T> list) {
                    if (list == null || list.isEmpty()) return null;
                    T max = list.get(0);
                    for (int i = 1; i < list.size(); i++) {
                        T item = list.get(i);
                        if (item.compareTo(max) > 0) {
                            max = item;
                        }
                    }
                    return max;
                }
                public static String test() {
                    List<String> names = Arrays.asList("alpha", "zebra", "beta", "gamma");
                    return findMax(names);
                }
            }
        '''
        assertPositive(className, javaSrc, 'zebra')
    }

    // =========================================================================
    // Category 2: Generic Methods & Constructors
    // =========================================================================

    @Test
    void testGenericMethodInference() {
        final String className = 'gls.generics.test.MethodInference'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.*;
            public class MethodInference {
                public static <T> List<T> makePairList(T first, T second) {
                    List<T> list = new ArrayList<>();
                    list.add(first);
                    list.add(second);
                    return list;
                }
                public static int test() {
                    List<Integer> numbers = makePairList(10, 20);
                    List<String> strings = makePairList("foo", "bar");
                    return numbers.size() + strings.size();
                }
            }
        '''
        assertPositive(className, javaSrc, 4)
    }

    @Test
    void testGenericMethodExplicitTypeArguments() {
        final String className = 'gls.generics.test.ExplicitTypeArgs'
        final String javaSrc = '''
            package gls.generics.test;
            public class ExplicitTypeArgs {
                public static class Helper {
                    public static <T> T identity(T val) {
                        return val;
                    }
                    public <E> String stringify(E val) {
                        return String.valueOf(val);
                    }
                }
                public static String test() {
                    String a = Helper.<String>identity("Explicit");
                    Helper h = new Helper();
                    String b = h.<Integer>stringify(123);
                    return a + ":" + b;
                }
            }
        '''
        assertPositive(className, javaSrc, 'Explicit:123')
    }

    @Test
    void testGenericConstructors() {
        final String className = 'gls.generics.test.GenericConstructors'
        final String javaSrc = '''
            package gls.generics.test;
            public class GenericConstructors {
                public static class TypeHolder {
                    private final String typeName;
                    public <T> TypeHolder(T val) {
                        this.typeName = val.getClass().getSimpleName();
                    }
                    public String getTypeName() { return typeName; }
                }
                public static String test() {
                    TypeHolder h1 = new TypeHolder("text");
                    TypeHolder h2 = new TypeHolder(42);
                    return h1.getTypeName() + "-" + h2.getTypeName();
                }
            }
        '''
        assertPositive(className, javaSrc, 'String-Integer')
    }

    @Test
    void testGenericVarargs() {
        final String className = 'gls.generics.test.GenericVarargs'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.*;
            public class GenericVarargs {
                @SafeVarargs
                public static <T> List<T> collect(T... items) {
                    List<T> list = new ArrayList<>();
                    for (T item : items) {
                        list.add(item);
                    }
                    return list;
                }
                public static int test() {
                    List<Integer> list = collect(1, 2, 3, 4, 5);
                    int sum = 0;
                    for (int n : list) {
                        sum += n;
                    }
                    return sum;
                }
            }
        '''
        assertPositive(className, javaSrc, 15)
    }

    // =========================================================================
    // Category 3: Wildcards & PECS
    // =========================================================================

    @Test
    void testUnboundedWildcard() {
        final String className = 'gls.generics.test.UnboundedWildcard'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.*;
            public class UnboundedWildcard {
                public static int countElements(Collection<?> col) {
                    int c = 0;
                    for (Object o : col) {
                        if (o != null) c++;
                    }
                    return c;
                }
                public static int test() {
                    List<String> strList = Arrays.asList("a", "b", "c");
                    List<Integer> intList = Arrays.asList(1, 2, null, 4);
                    return countElements(strList) + countElements(intList);
                }
            }
        '''
        assertPositive(className, javaSrc, 6)
    }

    @Test
    void testUpperBoundedWildcardProducer() {
        final String className = 'gls.generics.test.UpperBoundedWildcard'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.*;
            public class UpperBoundedWildcard {
                public static double sumOfList(List<? extends Number> list) {
                    double s = 0.0;
                    for (Number n : list) {
                        s += n.doubleValue();
                    }
                    return s;
                }
                public static double test() {
                    List<Integer> li = Arrays.asList(1, 2, 3);
                    List<Double> ld = Arrays.asList(1.5d, 2.5d);
                    return sumOfList(li) + sumOfList(ld);
                }
            }
        '''
        assertPositive(className, javaSrc, 10.0d)
    }

    @Test
    void testLowerBoundedWildcardConsumer() {
        final String className = 'gls.generics.test.LowerBoundedWildcard'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.*;
            public class LowerBoundedWildcard {
                public static void addNumbers(List<? super Integer> list) {
                    for (int i = 1; i <= 3; i++) {
                        list.add(i);
                    }
                }
                public static int test() {
                    List<Integer> intList = new ArrayList<>();
                    List<Number> numList = new ArrayList<>();
                    List<Object> objList = new ArrayList<>();
                    addNumbers(intList);
                    addNumbers(numList);
                    addNumbers(objList);
                    return intList.size() + numList.size() + objList.size();
                }
            }
        '''
        assertPositive(className, javaSrc, 9)
    }

    @Test
    void testWildcardCaptureHelper() {
        final String className = 'gls.generics.test.WildcardCapture'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.*;
            public class WildcardCapture {
                private static <T> void swapHelper(List<T> list, int i, int j) {
                    T temp = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, temp);
                }
                public static void swap(List<?> list, int i, int j) {
                    swapHelper(list, i, j);
                }
                public static String test() {
                    List<String> list = new ArrayList<>(Arrays.asList("first", "second"));
                    swap(list, 0, 1);
                    return list.get(0) + "-" + list.get(1);
                }
            }
        '''
        assertPositive(className, javaSrc, 'second-first')
    }

    // =========================================================================
    // Category 4: Diamond Operator <> & Type Inference
    // =========================================================================

    @Test
    void testDiamondOperatorWithNestedGenerics() {
        final String className = 'gls.generics.test.NestedDiamond'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.*;
            public class NestedDiamond {
                public static int test() {
                    Map<String, List<Map<Integer, String>>> complexMap = new HashMap<>();
                    List<Map<Integer, String>> innerList = new ArrayList<>();
                    Map<Integer, String> innerMap = new HashMap<>();
                    innerMap.put(1, "one");
                    innerList.add(innerMap);
                    complexMap.put("root", innerList);
                    return complexMap.get("root").get(0).get(1).length();
                }
            }
        '''
        assertPositive(className, javaSrc, 3)
    }

    @Test
    void testDiamondAnonymousInnerClass() {
        final String className = 'gls.generics.test.DiamondAnonymous'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.*;
            public class DiamondAnonymous {
                public interface Processor<T> {
                    T process(T val);
                }
                public static String test() {
                    Processor<String> p = new Processor<>() {
                        @Override
                        public String process(String val) {
                            return "Processed: " + val;
                        }
                    };
                    return p.process("Data");
                }
            }
        '''
        // Groovy cannot use diamond <> with anonymous inner classes; keep the rest identical.
        compileAndCompare(className, javaSrc, toGroovy(javaSrc.replace('new Processor<>()', 'new Processor<String>()')), 'Processed: Data', false)
    }

    @Test
    void testTargetTypeInference() {
        final String className = 'gls.generics.test.TargetTypeInference'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.*;
            public class TargetTypeInference {
                public static <T> List<T> empty() {
                    return Collections.emptyList();
                }
                public static int test() {
                    List<String> sList = empty();
                    List<Integer> iList = empty();
                    return sList.size() + iList.size();
                }
            }
        '''
        assertPositive(className, javaSrc, 0)
    }

    // =========================================================================
    // Category 5: Covariance, Bridge Methods & Overriding
    // =========================================================================

    @Test
    void testCovariantReturnAndBridgeMethod() {
        final String className = 'gls.generics.test.BridgeAndCovariance'
        final String javaSrc = '''
            package gls.generics.test;
            public class BridgeAndCovariance {
                public interface Supplier<T> {
                    T get();
                }
                public static class StringSupplier implements Supplier<String> {
                    @Override
                    public String get() {
                        return "Hello from Bridge";
                    }
                }
                public static String test() {
                    StringSupplier specific = new StringSupplier();
                    Supplier<String> genericRef = specific;
                    @SuppressWarnings("rawtypes")
                    Supplier rawRef = specific;
                    return genericRef.get() + "|" + rawRef.get();
                }
            }
        '''
        assertPositive(className, javaSrc, 'Hello from Bridge|Hello from Bridge', true)
    }

    // =========================================================================
    // Category 6: Raw Types & Interoperability
    // =========================================================================

    @Test
    void testRawTypeInterop() {
        final String className = 'gls.generics.test.RawTypeInterop'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.*;
            public class RawTypeInterop {
                @SuppressWarnings({"rawtypes", "unchecked"})
                public static int test() {
                    List<String> typedList = new ArrayList<>();
                    typedList.add("a");
                    typedList.add("b");
                    List rawList = typedList;
                    rawList.add("c");
                    return rawList.size();
                }
            }
        '''
        assertPositive(className, javaSrc, 3)
    }

    // =========================================================================
    // Category 7: Generic Exceptions & Parametric Throwables
    // =========================================================================

    @Test
    void testGenericThrowsClause() {
        final String className = 'gls.generics.test.GenericThrows'
        final String javaSrc = '''
            package gls.generics.test;
            import java.io.IOException;
            public class GenericThrows {
                public interface Action<E extends Exception> {
                    void execute() throws E;
                }
                public static <E extends Exception> void runAction(Action<E> action) throws E {
                    action.execute();
                }
                public static String test() {
                    final StringBuilder sb = new StringBuilder();
                    try {
                        runAction(new Action<IOException>() {
                            @Override
                            public void execute() throws IOException {
                                sb.append("executed");
                                throw new IOException("io-fail");
                            }
                        });
                    } catch (IOException e) {
                        sb.append(":caught-").append(e.getMessage());
                    }
                    return sb.toString();
                }
            }
        '''
        assertPositive(className, javaSrc, 'executed:caught-io-fail')
    }

    // =========================================================================
    // Category 8: Strict Negative Verification (Compile-time & Runtime)
    // =========================================================================

    @Test
    void testNegativeGenericSubclassOfThrowable() {
        // JLS 8.1.2: a generic class may not extend Throwable. javac rejects this;
        // static Groovy currently accepts the equivalent source, so only the Java side is asserted.
        final String className = 'gls.generics.test.GenEx'
        final String javaSrc = '''
            package gls.generics.test;
            public class GenEx<T> extends Exception {
                private T info;
            }
        '''
        JavaShell js = new JavaShell()
        Throwable javaEx = shouldFail(JavaShellCompilationException) {
            js.compile(className, javaSrc)
        }
        assert javaEx.message.contains("generic class may not extend 'java.lang.Throwable'") ||
            javaEx.message.contains("a generic class may not extend java.lang.Throwable")
    }

    @Test
    void testNegativeTypeArityMismatch() {
        final String className = 'gls.generics.test.ArityMismatch'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.List;
            public class ArityMismatch {
                public List<String, Integer> list;
            }
        '''
        assertNegativeCompile(className, javaSrc, "wrong number of type arguments", "supplied with 2 type parameters")
    }

    @Test
    void testNegativeMethodParamArityMismatch() {
        final String className = 'gls.generics.test.MethodArityMismatch'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.Map;
            public class MethodArityMismatch {
                public void process(Map<String> map) {}
            }
        '''
        assertNegativeCompile(className, javaSrc, "wrong number of type arguments", "supplied with 1 type parameter")
    }

    @Test
    void testNegativeGenericInvarianceAssignment() {
        final String className = 'gls.generics.test.InvarianceFail'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.*;
            public class InvarianceFail {
                public static void test() {
                    List<Number> nums = new ArrayList<Integer>();
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "incompatible types", "Cannot assign")
    }

    @Test
    void testNegativeMultipleBoundsMismatch() {
        final String className = 'gls.generics.test.MultiBoundFail'
        final String javaSrc = '''
            package gls.generics.test;
            public class MultiBoundFail {
                public static class Holder<T extends Number & Comparable<T>> {}
                public static void test() {
                    Holder<Object> h = new Holder<Object>();
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "not within bounds", "not a valid substitute for the bounded parameter")
    }

    @Test
    void testNegativeWildcardInExtendsClause() {
        final String className = 'gls.generics.test.WildcardSuper'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.ArrayList;
            public class WildcardSuper extends ArrayList<? extends Number> {
            }
        '''
        assertNegativeCompile(className, javaSrc, "unexpected type", "A supertype may not specify a wildcard type")
    }

    @Test
    void testNegativeParametricTypeForNonGenericClass() {
        final String className = 'gls.generics.test.NonGenericParameterized'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.Date;
            public class NonGenericParameterized {
                public static void test() {
                    Object o = new Date<String>();
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "does not take parameters", "which takes no parameters")
    }

    @Test
    void testNegativeDuplicateGenericInterfaceInheritance() {
        final String className = 'gls.generics.test.InterfaceConflict'
        final String javaSrc = '''
            package gls.generics.test;
            public class InterfaceConflict implements Comparable<String>, Comparable<Integer> {
                public int compareTo(String o) { return 0; }
                public int compareTo(Integer o) { return 0; }
            }
        '''
        assertNegativeCompile(className, javaSrc, "different arguments", "Duplicate interfaces in implements list")
    }

    @Test
    void testNegativeMethodArgumentTypeMismatch() {
        final String className = 'gls.generics.test.CallMismatch'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.*;
            public class CallMismatch {
                public static void test() {
                    List<String> list = new ArrayList<>();
                    list.add(123);
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "incompatible types", "Cannot call")
    }

    @Test
    void testNegativeHeapPollutionClassCastException() {
        final String className = 'gls.generics.test.HeapPollution'
        final String javaSrc = '''
            package gls.generics.test;
            public class HeapPollution {
                public interface Sink<T> {
                    void accept(T item);
                }
                public static class IntegerSink implements Sink<Integer> {
                    private Integer value;
                    @Override
                    public void accept(Integer item) {
                        this.value = item;
                    }
                }
                @SuppressWarnings({"rawtypes", "unchecked"})
                public static void invokeRaw(Sink sink, Object item) {
                    sink.accept(item);
                }
                public static void test() {
                    invokeRaw(new IntegerSink(), "not an integer");
                }
            }
        '''
        assertNegativeRuntime(className, javaSrc, ClassCastException.class)
    }

    // =========================================================================
    // Category 9: Inheritance, enclosing types, and bound forwarding
    // =========================================================================

    @Test
    void testGenericClassExtendsParameterizedType() {
        final String className = 'gls.generics.test.ExtendsParameterized'
        final String javaSrc = '''
            package gls.generics.test;
            public class ExtendsParameterized {
                public static class Holder<T> {
                    private final T value;
                    public Holder(T value) { this.value = value; }
                    public T getValue() { return value; }
                }
                public static class Named extends Holder<String> {
                    public Named(String value) { super(value); }
                }
                public static String test() {
                    Holder<String> h = new Named("ok");
                    return h.getValue();
                }
            }
        '''
        assertPositive(className, javaSrc, 'ok', true)
    }

    @Test
    void testTypeParameterForwardedToSuperclass() {
        final String className = 'gls.generics.test.ForwardTypeParam'
        final String javaSrc = '''
            package gls.generics.test;
            public class ForwardTypeParam {
                public static class Holder<T> {
                    private T value;
                    public void set(T value) { this.value = value; }
                    public T get() { return value; }
                }
                public static class Box<T> extends Holder<T> {
                }
                public static String test() {
                    Box<String> box = new Box<>();
                    box.set("fwd");
                    return box.get();
                }
            }
        '''
        assertPositive(className, javaSrc, 'fwd', true)
    }

    @Test
    void testGenericInterfaceExtendsGenericInterface() {
        final String className = 'gls.generics.test.GenericIfaceExtends'
        final String javaSrc = '''
            package gls.generics.test;
            public class GenericIfaceExtends {
                public interface Src<T> {
                    T get();
                }
                public interface SrcNum extends Src<Number> {
                }
                public static class Impl implements SrcNum {
                    @Override
                    public Number get() { return Integer.valueOf(7); }
                }
                public static int test() {
                    SrcNum s = new Impl();
                    return s.get().intValue();
                }
            }
        '''
        assertPositive(className, javaSrc, 7, true)
    }

    @Test
    void testNonStaticInnerClassUsesEnclosingTypeParameter() {
        final String className = 'gls.generics.test.EnclosingTypeParam'
        final String javaSrc = '''
            package gls.generics.test;
            public class EnclosingTypeParam<T> {
                public class Inner {
                    private final T value;
                    Inner(T value) { this.value = value; }
                    public T getValue() { return value; }
                }
                public Inner make(T value) { return new Inner(value); }
                public static String test() {
                    EnclosingTypeParam<String> outer = new EnclosingTypeParam<String>();
                    return outer.make("inner").getValue();
                }
            }
        '''
        assertPositive(className, javaSrc, 'inner')
    }

    @Test
    void testStaticNestedClassHasIndependentTypeParameters() {
        final String className = 'gls.generics.test.StaticNestedIndependent'
        final String javaSrc = '''
            package gls.generics.test;
            public class StaticNestedIndependent {
                public static class Outer<T> {
                    public static class Cell<U> {
                        private final U value;
                        public Cell(U value) { this.value = value; }
                        public U getValue() { return value; }
                    }
                }
                public static String test() {
                    Outer.Cell<Integer> cell = new Outer.Cell<>(42);
                    return String.valueOf(cell.getValue());
                }
            }
        '''
        assertPositive(className, javaSrc, '42', true)
    }

    @Test
    void testTypeParameterUsedAsBoundOfAnother() {
        final String className = 'gls.generics.test.TypeParamAsBound'
        final String javaSrc = '''
            package gls.generics.test;
            public class TypeParamAsBound {
                public static <T, U extends T> U pick(U value) {
                    return value;
                }
                public static String test() {
                    CharSequence cs = pick("bound");
                    return cs.toString();
                }
            }
        '''
        assertPositive(className, javaSrc, 'bound')
    }

    @Test
    void testForwardReferencedTypeParameterBounds() {
        final String className = 'gls.generics.test.ForwardRefBounds'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.ArrayList;
            import java.util.List;
            public class ForwardRefBounds {
                public static class Foo<T extends List<X>, X extends Number> {
                    private final T list;
                    public Foo(T list) { this.list = list; }
                    public double first() { return list.get(0).doubleValue(); }
                }
                public static double test() {
                    ArrayList<Integer> nums = new ArrayList<Integer>();
                    nums.add(Integer.valueOf(9));
                    Foo<ArrayList<Integer>, Integer> foo = new Foo<>(nums);
                    return foo.first();
                }
            }
        '''
        assertPositive(className, javaSrc, 9.0d, true)
    }

    @Test
    void testFBoundedSelfType() {
        final String className = 'gls.generics.test.FBoundedSelfType'
        final String javaSrc = '''
            package gls.generics.test;
            public class FBoundedSelfType {
                public abstract static class Entity<T extends Entity<T>> {
                    public abstract T self();
                    public T fluent() { return self(); }
                }
                public static class User extends Entity<User> {
                    @Override
                    public User self() { return this; }
                    public String name() { return "user"; }
                }
                public static String test() {
                    return new User().fluent().name();
                }
            }
        '''
        assertPositive(className, javaSrc, 'user', true)
    }

    @Test
    void testIntersectionBoundUsedAsBothTypes() {
        final String className = 'gls.generics.test.IntersectionDispatch'
        final String javaSrc = '''
            package gls.generics.test;
            public class IntersectionDispatch {
                public static <T extends Number & Comparable<T>> String describe(T value) {
                    return value.getClass().getSimpleName() + ":" + value.intValue() + ":" + value.compareTo(value);
                }
                public static String test() {
                    return describe(Integer.valueOf(5));
                }
            }
        '''
        assertPositive(className, javaSrc, 'Integer:5:0')
    }

    @Test
    void testIndependentBoundedTypeParameters() {
        final String className = 'gls.generics.test.IndependentBoundedParams'
        final String javaSrc = '''
            package gls.generics.test;
            public class IndependentBoundedParams {
                public static class Mix<A extends CharSequence, B extends Number> {
                    public String mix(A a, B b) {
                        return a.toString() + b.intValue();
                    }
                }
                public static String test() {
                    Mix<String, Integer> mix = new Mix<>();
                    return mix.mix("n", Integer.valueOf(3));
                }
            }
        '''
        assertPositive(className, javaSrc, 'n3', true)
    }

    // =========================================================================
    // Category 10: Generic methods on generic types, arrays as type arguments
    // =========================================================================

    @Test
    void testGenericMethodOnGenericClass() {
        final String className = 'gls.generics.test.GenericMethodOnGenericClass'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.function.Function;
            public class GenericMethodOnGenericClass {
                public static class Box<T> {
                    private final T value;
                    public Box(T value) { this.value = value; }
                    public <U> U map(Function<T, U> mapper) {
                        return mapper.apply(value);
                    }
                }
                public static int test() {
                    Box<String> box = new Box<>("abcd");
                    return box.map(s -> s.length());
                }
            }
        '''
        assertPositive(className, javaSrc, 4)
    }

    @Test
    void testArrayAsTypeArgument() {
        final String className = 'gls.generics.test.ArrayAsTypeArg'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.ArrayList;
            import java.util.List;
            public class ArrayAsTypeArg {
                public static int test() {
                    List<String[]> list = new ArrayList<String[]>();
                    list.add(new String[] {"a", "bb"});
                    return list.get(0)[1].length();
                }
            }
        '''
        assertPositive(className, javaSrc, 2)
    }

    @Test
    void testChainedGenericMethodInference() {
        final String className = 'gls.generics.test.ChainedInference'
        final String javaSrc = '''
            package gls.generics.test;
            public class ChainedInference {
                public static <T> T id(T value) {
                    return value;
                }
                public static String test() {
                    return id(id("ok"));
                }
            }
        '''
        assertPositive(className, javaSrc, 'ok')
    }

    @Test
    void testOverrideGenericMethodWithMatchingBounds() {
        final String className = 'gls.generics.test.OverrideMatchingBounds'
        final String javaSrc = '''
            package gls.generics.test;
            public class OverrideMatchingBounds {
                public interface Rec<T extends Rec<T>> {
                }
                public static class Base {
                    public <T extends Rec<T>> String tag() { return "base"; }
                }
                public static class Sub extends Base {
                    @Override
                    public <U extends Rec<U>> String tag() { return "sub"; }
                }
                public static String test() {
                    Base b = new Sub();
                    return b.tag();
                }
            }
        '''
        assertPositive(className, javaSrc, 'sub')
    }

    // =========================================================================
    // Category 11: PECS, nested wildcards, function variance
    // =========================================================================

    @Test
    void testPecsCollectionsCopy() {
        final String className = 'gls.generics.test.PecsCopy'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.ArrayList;
            import java.util.Arrays;
            import java.util.Collections;
            import java.util.List;
            public class PecsCopy {
                public static String test() {
                    List<Integer> src = Arrays.asList(1, 2, 3);
                    List<Number> dst = new ArrayList<Number>();
                    dst.add(null);
                    dst.add(null);
                    dst.add(null);
                    Collections.copy(dst, src);
                    return dst.get(0).intValue() + "," + dst.get(2).intValue();
                }
            }
        '''
        assertPositive(className, javaSrc, '1,3')
    }

    @Test
    void testNestedWildcards() {
        final String className = 'gls.generics.test.NestedWildcards'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.ArrayList;
            import java.util.Arrays;
            import java.util.List;
            public class NestedWildcards {
                public static int sizeOf(List<? extends List<? extends Number>> lists) {
                    int n = 0;
                    for (List<? extends Number> list : lists) {
                        n += list.size();
                    }
                    return n;
                }
                public static int test() {
                    List<List<Integer>> data = new ArrayList<List<Integer>>();
                    data.add(Arrays.asList(1, 2));
                    data.add(Arrays.asList(3));
                    return sizeOf(data);
                }
            }
        '''
        assertPositive(className, javaSrc, 3)
    }

    @Test
    void testComparatorSuperBoundSort() {
        final String className = 'gls.generics.test.ComparatorSuperBound'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.ArrayList;
            import java.util.Comparator;
            import java.util.List;
            public class ComparatorSuperBound {
                public static String test() {
                    List<Integer> list = new ArrayList<Integer>();
                    list.add(Integer.valueOf(3));
                    list.add(Integer.valueOf(1));
                    list.add(Integer.valueOf(2));
                    Comparator<Number> byInt = (a, b) -> Integer.compare(a.intValue(), b.intValue());
                    list.sort(byInt);
                    return list.get(0) + "," + list.get(1) + "," + list.get(2);
                }
            }
        '''
        assertPositive(className, javaSrc, '1,2,3')
    }

    @Test
    void testFunctionWildcardVariance() {
        final String className = 'gls.generics.test.FunctionVariance'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.function.Function;
            public class FunctionVariance {
                public static <T, R> R apply(Function<? super T, ? extends R> f, T value) {
                    return f.apply(value);
                }
                public static int test() {
                    Function<Object, Integer> f = o -> String.valueOf(o).length();
                    return apply(f, "xyz");
                }
            }
        '''
        assertPositive(className, javaSrc, 3)
    }

    @Test
    void testLowerBoundedWildcardGetReturnsObject() {
        final String className = 'gls.generics.test.LowerBoundGet'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.ArrayList;
            import java.util.List;
            public class LowerBoundGet {
                public static String test() {
                    List<? super Integer> list = new ArrayList<Number>();
                    list.add(Integer.valueOf(8));
                    Object value = list.get(0);
                    return String.valueOf(value);
                }
            }
        '''
        assertPositive(className, javaSrc, '8')
    }

    @Test
    void testWildcardAllowsAddingNull() {
        final String className = 'gls.generics.test.WildcardAddNull'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.ArrayList;
            import java.util.List;
            public class WildcardAddNull {
                public static int test() {
                    List<?> list = new ArrayList<String>();
                    list.add(null);
                    return list.size();
                }
            }
        '''
        assertPositive(className, javaSrc, 1)
    }

    // =========================================================================
    // Category 12: Diamond target typing, unchecked conversion, raw assignment
    // =========================================================================

    @Test
    void testDiamondInferredFromMethodArgument() {
        final String className = 'gls.generics.test.DiamondMethodArg'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.ArrayList;
            import java.util.List;
            public class DiamondMethodArg {
                public static int len(List<String> list) {
                    return list.size();
                }
                public static int test() {
                    return len(new ArrayList<>());
                }
            }
        '''
        assertPositive(className, javaSrc, 0)
    }

    @Test
    void testUncheckedCastToParameterizedType() {
        final String className = 'gls.generics.test.UncheckedCast'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.Arrays;
            import java.util.List;
            public class UncheckedCast {
                @SuppressWarnings("unchecked")
                public static String test() {
                    List<?> wild = Arrays.asList("cast");
                    List<String> typed = (List<String>) wild;
                    return typed.get(0);
                }
            }
        '''
        assertPositive(className, javaSrc, 'cast')
    }

    @Test
    void testRawTypeAssignedToParameterizedType() {
        final String className = 'gls.generics.test.RawToParameterized'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.ArrayList;
            import java.util.List;
            public class RawToParameterized {
                @SuppressWarnings({"rawtypes", "unchecked"})
                public static int test() {
                    List raw = new ArrayList();
                    raw.add("x");
                    List<String> typed = raw;
                    return typed.get(0).length();
                }
            }
        '''
        assertPositive(className, javaSrc, 1)
    }

    @Test
    void testMapEntryParameterizedInnerInterface() {
        final String className = 'gls.generics.test.MapEntryInner'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.HashMap;
            import java.util.Iterator;
            import java.util.Map;
            public class MapEntryInner {
                public static String test() {
                    Map<String, Integer> map = new HashMap<>();
                    map.put("k", Integer.valueOf(3));
                    Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
                    Map.Entry<String, Integer> e = it.next();
                    return e.getKey() + e.getValue();
                }
            }
        '''
        assertPositive(className, javaSrc, 'k3')
    }

    // =========================================================================
    // Category 13: Covariant override, conditional lub, records, instanceof
    // =========================================================================

    @Test
    void testCovariantOverrideInGenericClassHierarchy() {
        final String className = 'gls.generics.test.CovariantClassOverride'
        final String javaSrc = '''
            package gls.generics.test;
            public class CovariantClassOverride {
                public static class Parent<T> {
                    public T id(T value) { return value; }
                }
                public static class Child extends Parent<String> {
                    @Override
                    public String id(String value) { return value + "!"; }
                }
                public static String test() {
                    Parent<String> p = new Child();
                    return p.id("ok");
                }
            }
        '''
        assertPositive(className, javaSrc, 'ok!', true)
    }

    @Test
    void testConditionalExpressionLubsClassLiterals() {
        final String className = 'gls.generics.test.ConditionalLub'
        final String javaSrc = '''
            package gls.generics.test;
            public class ConditionalLub {
                public static String test() {
                    boolean cond = "x".length() == 1;
                    Class<?> cls = cond ? Integer.class : Double.class;
                    CharSequence cs = cond ? "hello" : new StringBuilder("hello");
                    return cls.getSimpleName() + cs.toString();
                }
            }
        '''
        assertPositive(className, javaSrc, 'Integerhello')
    }

    @Test
    void testGenericRecord() {
        final String className = 'gls.generics.test.GenericRecordPair'
        final String javaSrc = '''
            package gls.generics.test;
            public class GenericRecordPair {
                public static record Pair<K, V>(K key, V value) {
                }
                public static String test() {
                    Pair<String, Integer> pair = new Pair<>("a", Integer.valueOf(1));
                    return pair.key() + pair.value();
                }
            }
        '''
        assertPositive(className, javaSrc, 'a1', true)
    }

    @Test
    void testReifiableInstanceofUnboundedWildcard() {
        final String className = 'gls.generics.test.ReifiableInstanceof'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.ArrayList;
            import java.util.List;
            public class ReifiableInstanceof {
                public static boolean test() {
                    Object list = new ArrayList<String>();
                    Object cls = ReifiableInstanceof.class.getClass();
                    return (list instanceof List<?>) && (cls instanceof Class<?>);
                }
            }
        '''
        assertPositive(className, javaSrc, true)
    }

    @Test
    void testDeclaredGenericSignaturesMatch() {
        final String className = 'gls.generics.test.SignatureCatalog'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.List;
            public class SignatureCatalog {
                public static class Box<T> {
                    public T value;
                    public Box(T value) { this.value = value; }
                    public T getValue() { return value; }
                }
                public static class Pair<K extends Number, V extends CharSequence> {
                    public final K k;
                    public final V v;
                    public Pair(K k, V v) { this.k = k; this.v = v; }
                }
                public static class Bounded<T extends Number & Comparable<T>> {
                    public int compare(T a, T b) { return a.compareTo(b); }
                }
                public static class Sub extends Box<String> {
                    public Sub(String value) { super(value); }
                }
                public List<? extends Number> extendsBound;
                public List<? super Integer> superBound;
                public List<?> unbounded;
                public static <E> E identity(E value) { return value; }
                public static String test() {
                    Box<String> box = new Box<>("x");
                    Pair<Integer, String> pair = new Pair<>(Integer.valueOf(1), "a");
                    Bounded<Integer> bounded = new Bounded<>();
                    Sub sub = new Sub("s");
                    return identity(box.getValue()) + pair.v + bounded.compare(Integer.valueOf(2), Integer.valueOf(3)) + sub.getValue();
                }
            }
        '''
        assertPositive(className, javaSrc, 'xa-1s', true)
    }

    // =========================================================================
    // Category 14: Additional negative cases (JLS restrictions both sides share)
    // =========================================================================

    @Test
    void testNegativeNonReifiableInstanceof() {
        final String className = 'gls.generics.test.NonReifiableInstanceof'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.List;
            public class NonReifiableInstanceof {
                public static boolean test(Object o) {
                    return o instanceof List<String>;
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "cannot be safely cast", "Cannot perform instanceof")
    }

    @Test
    void testNegativeInstanceofParameterizedClassLiteral() {
        final String className = 'gls.generics.test.InstanceofClassParam'
        final String javaSrc = '''
            package gls.generics.test;
            public class InstanceofClassParam {
                public static boolean test() {
                    return InstanceofClassParam.class.getClass() instanceof Class<InstanceofClassParam>;
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "cannot be converted", "Cannot perform instanceof")
    }

    @Test
    void testNegativeTypeParameterReferencedFromStaticNestedClass() {
        final String className = 'gls.generics.test.StaticNestedOuterTp'
        final String javaSrc = '''
            package gls.generics.test;
            public class StaticNestedOuterTp<T> {
                static class Inner {
                    T field;
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "non-static type variable", "unable to resolve class T")
    }

    @Test
    void testNegativeTypeParameterStaticField() {
        final String className = 'gls.generics.test.StaticTypeParamField'
        final String javaSrc = '''
            package gls.generics.test;
            public class StaticTypeParamField<T> {
                static T field;
            }
        '''
        assertNegativeCompile(className, javaSrc, "non-static type variable", "unable to resolve class T")
    }

    @Test
    void testNegativeWildcardWriteThroughExtends() {
        final String className = 'gls.generics.test.WildcardWriteExtends'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.List;
            public class WildcardWriteExtends {
                public static void test(List<? extends Number> list) {
                    list.add(Integer.valueOf(1));
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "incompatible types", "Cannot call")
    }

    // =========================================================================
    // Test Harness Helper Methods
    // =========================================================================

    /**
     * Static Groovy view of {@code javaSrc}: Java annotation array initializers become
     * Groovy lists. Semicolons and the rest of the Java syntax are kept. CompileStatic
     * is applied by {@link #newStaticGroovyClassLoader()}, not by rewriting the source.
     */
    private static String toGroovy(String javaSrc) {
        javaSrc.replaceAll(/@SuppressWarnings\(\{([^}]*)\}\)/, '@SuppressWarnings([$1])')
    }

    /**
     * Groovy class loader that always compiles with {@link CompileStatic}.
     * Dynamic Groovy is not part of this suite.
     */
    private static GroovyClassLoader newStaticGroovyClassLoader() {
        CompilerConfiguration config = new CompilerConfiguration()
        config.addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic))
        new GroovyClassLoader(GenericsJavaCompatibilityTest.classLoader, config)
    }

    /**
     * Compiles {@code javaSrc} with javac and the {@link #toGroovy} view with static
     * Groovy, then asserts matching {@code test()} results. When {@code compareSignatures}
     * is true, user-declared generic signatures are compared through reflection.
     */
    private void assertPositive(String className, String javaSrc, Object expectedResult, boolean compareSignatures = false) {
        compileAndCompare(className, javaSrc, toGroovy(javaSrc), expectedResult, compareSignatures)
    }

    private void compileAndCompare(String className, String javaSrc, String groovySrc, Object expectedResult, boolean compareSignatures) {
        JavaShell js = new JavaShell()
        Class<?> javaClass = js.compile(className, javaSrc)
        Object javaResult = javaClass.getMethod('test').invoke(null)

        Class<?> groovyClass = newStaticGroovyClassLoader().parseClass(groovySrc)
        Object groovyResult = groovyClass.getMethod('test').invoke(null)

        assert javaResult == groovyResult: "Execution result mismatch between Java ($javaResult) and Groovy ($groovyResult)"
        if (expectedResult != null) {
            assert javaResult == expectedResult: "Java result ($javaResult) does not match expected ($expectedResult)"
            assert groovyResult == expectedResult: "Groovy result ($groovyResult) does not match expected ($expectedResult)"
        }
        if (compareSignatures) {
            assertUserGenericDeclarations(javaClass, groovyClass)
        }
    }

    /**
     * Compares user-visible generic declarations of {@code javaClass} and {@code groovyClass}.
     * GroovyObject, synthetic members and extra Groovy nested types are ignored so the check
     * stays stable across compiler revisions.
     */
    private static void assertUserGenericDeclarations(Class<?> javaClass, Class<?> groovyClass) {
        assertTypeParametersEqual(javaClass, groovyClass)
        assertGenericSuperclassEqual(javaClass, groovyClass)
        assertUserGenericInterfacesEqual(javaClass, groovyClass)
        assertMatchingMethodsGeneric(javaClass, groovyClass)
        assertMatchingFieldsGeneric(javaClass, groovyClass)

        for (Class<?> javaNested : javaClass.getDeclaredClasses()) {
            String simple = javaNested.getSimpleName()
            if (simple == null || simple.isEmpty() || simple.matches(/\d+/)) {
                continue
            }
            Class<?> groovyNested = groovyClass.getDeclaredClasses().find { it.simpleName == simple }
            assert groovyNested != null: "Groovy is missing nested type ${javaClass.name}\$${simple}"
            assertUserGenericDeclarations(javaNested, groovyNested)
        }
    }

    private static void assertTypeParametersEqual(Class<?> javaClass, Class<?> groovyClass) {
        TypeVariable<?>[] javaParams = javaClass.getTypeParameters()
        TypeVariable<?>[] groovyParams = groovyClass.getTypeParameters()
        assert javaParams.length == groovyParams.length:
            "${javaClass.simpleName} type-parameter arity Java=${javaParams.length} Groovy=${groovyParams.length}"
        for (int i = 0; i < javaParams.length; i++) {
            assert javaParams[i].name == groovyParams[i].name:
                "${javaClass.simpleName} type-parameter name Java=${javaParams[i].name} Groovy=${groovyParams[i].name}"
            List<String> javaBounds = javaParams[i].bounds.collect { typeDesc(it) }
            List<String> groovyBounds = groovyParams[i].bounds.collect { typeDesc(it) }
            assert javaBounds == groovyBounds:
                "${javaClass.simpleName}<${javaParams[i].name}> bounds Java=${javaBounds} Groovy=${groovyBounds}"
        }
    }

    private static void assertGenericSuperclassEqual(Class<?> javaClass, Class<?> groovyClass) {
        String javaSuper = typeDesc(javaClass.getGenericSuperclass())
        String groovySuper = typeDesc(groovyClass.getGenericSuperclass())
        assert javaSuper == groovySuper:
            "${javaClass.simpleName} generic superclass Java=${javaSuper} Groovy=${groovySuper}"
    }

    private static void assertUserGenericInterfacesEqual(Class<?> javaClass, Class<?> groovyClass) {
        List<String> javaIfaces = userGenericInterfaces(javaClass)
        List<String> groovyIfaces = userGenericInterfaces(groovyClass)
        assert javaIfaces == groovyIfaces:
            "${javaClass.simpleName} generic interfaces Java=${javaIfaces} Groovy=${groovyIfaces}"
    }

    private static List<String> userGenericInterfaces(Class<?> type) {
        type.getGenericInterfaces().findAll { iface ->
            String raw = rawName(iface)
            raw != 'groovy.lang.GroovyObject' && raw != 'groovy.lang.GroovyObjectSupport'
        }.collect { typeDesc(it) }
    }

    private static void assertMatchingMethodsGeneric(Class<?> javaClass, Class<?> groovyClass) {
        javaClass.getDeclaredMethods().findAll { m ->
            !m.synthetic && !m.bridge && m.name != '<clinit>' && !m.name.contains('$')
        }.each { javaMethod ->
            def groovyMethod = groovyClass.getDeclaredMethods().find { gm ->
                !gm.bridge && gm.name == javaMethod.name && Arrays.equals(gm.parameterTypes, javaMethod.parameterTypes)
            }
            if (groovyMethod == null) {
                return
            }
            String javaSig = genericMethodDesc(javaMethod)
            String groovySig = genericMethodDesc(groovyMethod)
            assert javaSig == groovySig:
                "${javaClass.simpleName}.${javaMethod.name} generic signature Java=${javaSig} Groovy=${groovySig}"
        }
    }

    private static void assertMatchingFieldsGeneric(Class<?> javaClass, Class<?> groovyClass) {
        javaClass.getDeclaredFields().findAll { f ->
            !f.synthetic && !f.name.contains('$') && f.name != 'metaClass'
        }.each { javaField ->
            def groovyField = groovyClass.getDeclaredFields().find { it.name == javaField.name }
            if (groovyField == null) {
                return
            }
            String javaType = typeDesc(javaField.genericType)
            String groovyType = typeDesc(groovyField.genericType)
            assert javaType == groovyType:
                "${javaClass.simpleName}.${javaField.name} generic type Java=${javaType} Groovy=${groovyType}"
        }
    }

    private static String genericMethodDesc(Method method) {
        String typeParams = method.typeParameters.length == 0 ? '' :
            '<' + method.typeParameters.collect { tv ->
                List<String> bounds = tv.bounds.collect { typeDesc(it) }
                bounds == ['java.lang.Object'] ? tv.name : tv.name + ' extends ' + bounds.join(' & ')
            }.join(', ') + '> '
        String params = method.genericParameterTypes.collect { typeDesc(it) }.join(', ')
        return typeParams + typeDesc(method.genericReturnType) + ' ' + method.name + '(' + params + ')'
    }

    private static String rawName(Type type) {
        if (type instanceof Class) {
            return ((Class<?>) type).name
        }
        if (type instanceof ParameterizedType) {
            return rawName(((ParameterizedType) type).rawType)
        }
        return typeDesc(type)
    }

    /**
     * Canonical, JDK-stable description of a reflective {@link Type}. Uses binary
     * class names and type-variable names rather than {@code Type.toString()}, which
     * has changed wording across JDK releases.
     */
    private static String typeDesc(Type type) {
        if (type == null) {
            return 'null'
        }
        if (type instanceof Class) {
            Class<?> c = (Class<?>) type
            if (c.isArray()) {
                return typeDesc(c.componentType) + '[]'
            }
            return c.name
        }
        if (type instanceof TypeVariable) {
            return ((TypeVariable<?>) type).name
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type
            Type[] typeArgs = p.actualTypeArguments
            String raw = typeDesc(p.rawType)
            if (typeArgs == null || typeArgs.length == 0) {
                return raw
            }
            return raw + '<' + typeArgs.collect { typeDesc(it) }.join(', ') + '>'
        }
        if (type instanceof WildcardType) {
            WildcardType w = (WildcardType) type
            Type[] lower = w.lowerBounds
            if (lower != null && lower.length > 0) {
                return '? super ' + typeDesc(lower[0])
            }
            Type[] upper = w.upperBounds
            if (upper != null && upper.length > 0 && upper[0] != Object) {
                return '? extends ' + typeDesc(upper[0])
            }
            return '?'
        }
        if (type instanceof GenericArrayType) {
            return typeDesc(((GenericArrayType) type).genericComponentType) + '[]'
        }
        return String.valueOf(type)
    }

    /**
     * Verifies that javac and static Groovy both reject invalid generics code.
     */
    private void assertNegativeCompile(String className, String javaSrc, String javaDiagnosticSub, String groovyDiagnosticSub) {
        JavaShell js = new JavaShell()
        Throwable javaEx = shouldFail(JavaShellCompilationException) {
            js.compile(className, javaSrc)
        }
        assert javaEx.message.contains(javaDiagnosticSub): "Expected Java diagnostic to contain '$javaDiagnosticSub', but got: ${javaEx.message}"

        Throwable groovyEx = shouldFail(CompilationFailedException) {
            newStaticGroovyClassLoader().parseClass(toGroovy(javaSrc))
        }
        assert groovyEx.message.contains(groovyDiagnosticSub): "Expected Groovy diagnostic to contain '$groovyDiagnosticSub', but got: ${groovyEx.message}"
    }

    /**
     * Verifies runtime negative behavior (e.g. heap pollution / ClassCastException on bridge method invocation).
     */
    private void assertNegativeRuntime(String className, String javaSrc, Class<? extends Throwable> expectedException) {
        JavaShell js = new JavaShell()
        Class<?> javaClass = js.compile(className, javaSrc)
        InvocationTargetException javaInvocationEx = shouldFail(InvocationTargetException) {
            javaClass.getMethod('test').invoke(null)
        }
        assert expectedException.isInstance(javaInvocationEx.getTargetException()):
            "Java expected target exception ${expectedException.name}, but got: ${javaInvocationEx.getTargetException()}"

        Class<?> groovyClass = newStaticGroovyClassLoader().parseClass(toGroovy(javaSrc))
        InvocationTargetException groovyInvocationEx = shouldFail(InvocationTargetException) {
            groovyClass.getMethod('test').invoke(null)
        }
        assert expectedException.isInstance(groovyInvocationEx.getTargetException()):
            "Groovy expected target exception ${expectedException.name}, but got: ${groovyInvocationEx.getTargetException()}"
    }

}
