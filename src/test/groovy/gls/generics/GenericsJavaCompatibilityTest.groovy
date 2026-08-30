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
 *
 * Spec citations are the Java Language Specification, Java SE 25 Edition.
 * The suite is organised around the four JCK/JLS dimensions for generics:
 * <ol>
 * <li>compile-time type safety and erasure, including bridge methods
 *     (JLS 4.6, 8.4.8.3);</li>
 * <li>advanced syntax and bounds — wildcards, F-bounds, nested and
 *     multi-parameter types (JLS 4.4–4.5, 4.9);</li>
 * <li>runtime reflection of {@code Signature} attributes
 *     ({@link Class#getGenericSuperclass()},
 *     {@link java.lang.reflect.ParameterizedType#getActualTypeArguments()},
 *     {@link java.lang.reflect.GenericArrayType});</li>
 * <li>raw-type backward compatibility (JLS 4.8, 5.1.9).</li>
 * </ol>
 * Categories 1–15 below map onto those dimensions. Category 16 spells out
 * the JCK-shaped cases that were not previously a dedicated test.
 * Each test method names the JLS sections it exercises. Gaps where static
 * Groovy currently diverges from javac are marked {@link org.junit.jupiter.api.Disabled}.
 * The oracle is javac as shipped with Java SE 25.
 */
final class GenericsJavaCompatibilityTest {

    // =========================================================================
    // Category 1: Generic class and interface declarations and type bounds (JLS 4.4, 8.1.2, 9.1.2)
    // =========================================================================

    /**
     * JLS 8.1.2, 4.4, 4.5: a generic class {@code Box<T>} declares a type parameter
     * that each parameterization substitutes. {@code new Box<>("...")} uses diamond
     * inference (JLS 15.9.3).
     */
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

    /**
     * JLS 8.1.2, 4.5: a generic class may declare several type parameters
     * ({@code Pair<K, V>}); the parameterization must supply exactly that many
     * arguments.
     */
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

    /**
     * JLS 9.1.2, 8.1.5, 8.4.8.1: a class may implement a parameterization of a
     * generic interface and override its methods with the substituted argument types.
     */
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

    /**
     * JLS 4.4: a type-variable bound {@code T extends Number} restricts each
     * type argument to a subtype of that bound; members of {@code Number} are
     * available on values of type {@code T}.
     */
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

    /**
     * JLS 4.4, 4.9: an intersection bound {@code T extends Number & Comparable<T> & Serializable}
     * requires a class type (or type variable) first, then interfaces. The erasure of
     * {@code T} is the leftmost bound.
     */
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

    /**
     * JLS 4.4, 8.4.4: F-bounded polymorphism {@code T extends Comparable<T>} lets a
     * generic method compare elements of {@code List<T>} using the bound's members.
     */
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
    // Category 2: Generic methods and constructors (JLS 8.4.4, 8.8.4, 15.12, 18.5)
    // =========================================================================

    /**
     * JLS 8.4.4, 18.5.1, 18.5.2: invocation of a generic method {@code <T> List<T> makePairList(T, T)}
     * infers {@code T} from the argument types and the assignment target.
     */
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

    /**
     * JLS 15.12.2: type arguments of a generic method may be written explicitly
     * ({@code Helper.<String>identity(...)}) instead of being inferred.
     */
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

    /**
     * JLS 8.8.4, 15.9.3: a constructor may declare its own type parameters, independent
     * of any class type parameters, and callers may pass explicit constructor type arguments.
     */
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
                    TypeHolder h1 = new <String>TypeHolder("text");
                    TypeHolder h2 = new <Integer>TypeHolder(42);
                    return h1.getTypeName() + "-" + h2.getTypeName();
                }
            }
        '''
        assertPositive(className, javaSrc, 'String-Integer')
    }

    /**
     * JLS 8.4.1, 9.6.4.7: a generic varargs method {@code <T> List<T> collect(T...)} is
     * applicable by variable-arity invocation (JLS 15.12.2.4). {@code @SafeVarargs}
     * documents that the heap-pollution risk is contained.
     */
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
    // Category 3: Wildcards, containment and capture conversion (JLS 4.5.1, 5.1.10)
    // =========================================================================

    /**
     * JLS 4.5.1 (Example 4.5.1-1): {@code Collection<?>} accepts any parameterization.
     * {@code ? extends Object} is equivalent to unbounded {@code ?}. Elements are
     * readable as {@code Object}.
     */
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

    /**
     * JLS 4.5.1: {@code List<? extends Number>} is a producer (contains {@code Integer},
     * {@code Double}, ...). Subtyping follows containment: {@code List<Integer> <= List<? extends Number>}.
     */
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

    /**
     * JLS 4.5.1: {@code List<? super Integer>} is a consumer; {@code Integer} may be
     * added because the unknown element type is a supertype of {@code Integer}.
     */
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

    /**
     * JLS 4.5: a wildcard type argument is within bounds of {@code T extends Number}
     * when capture conversion yields a well-formed glb with {@code Number}.
     * Unbounded {@code ?} is always within bounds. {@code ? extends Object} is
     * also legal: {@code glb(Object, Number)} is {@code Number}. javac 25 accepts
     * both; it does <em>not</em> reject {@code NumberBox<? extends Object>}.
     */
    @Test
    void testBoundedTypeParameterAcceptsUnboundedAndObjectWildcards() {
        final String className = 'gls.generics.test.NumberBoxWildcards'
        final String javaSrc = '''
            package gls.generics.test;
            public class NumberBoxWildcards {
                public static class NumberBox<T extends Number> {
                    public final T value;
                    public NumberBox(T value) { this.value = value; }
                }
                public static int test() {
                    NumberBox<Integer> box = new NumberBox<Integer>(Integer.valueOf(7));
                    NumberBox<?> unbounded = box;
                    NumberBox<? extends Object> objectBound = box;
                    NumberBox<? extends Number> numberBound = box;
                    NumberBox<? extends Integer> integerBound = box;
                    NumberBox<? super Integer> superInteger = box;
                    NumberBox<? super Number> superNumber = new NumberBox<Number>(Integer.valueOf(1));
                    return (unbounded != null && objectBound != null && numberBound != null
                            && integerBound != null && superInteger != null && superNumber != null) ? 1 : 0;
                }
            }
        '''
        assertPositive(className, javaSrc, 1)
    }

    /**
     * JLS 4.5: {@code ? extends String} is not within bounds of {@code T extends Number}
     * because {@code glb(String, Number)} is an empty intersection of two classes.
     * javac 25: "type argument ? extends String is not within bounds of type-variable T".
     */
    @Test
    void testNegativeWildcardUpperBoundDisjointFromTypeParameterBound() {
        final String className = 'gls.generics.test.NumberBoxExtendsString'
        final String javaSrc = '''
            package gls.generics.test;
            public class NumberBoxExtendsString {
                public static class NumberBox<T extends Number> {}
                public NumberBox<? extends String> field;
            }
        '''
        assertNegativeCompile(className, javaSrc, "not within bounds", "not a valid substitute for the bounded parameter")
    }

    /**
     * JLS 4.5: {@code ? super Object} is not within bounds of {@code T extends Number}
     * because the lower bound {@code Object} is not a subtype of {@code Number}.
     */
    @Test
    void testNegativeWildcardLowerBoundOutsideTypeParameterBound() {
        final String className = 'gls.generics.test.NumberBoxSuperObject'
        final String javaSrc = '''
            package gls.generics.test;
            public class NumberBoxSuperObject {
                public static class NumberBox<T extends Number> {}
                public NumberBox<? super Object> field;
            }
        '''
        assertNegativeCompile(className, javaSrc, "not within bounds", "not a valid substitute for the bounded parameter")
    }

    /**
     * JLS 5.1.10: capture conversion turns {@code List<?>} into {@code List<capture#1-of ?>}
     * so a helper {@code <T> void swapHelper(List<T>, ...)} can read and write the same {@code T}.
     */
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
    // Category 4: Diamond and target-type inference (JLS 15.9.3, 18.5.2)
    // =========================================================================

    /**
     * JLS 15.9.1, 15.9.3, 18.5.2: diamond {@code <>} infers nested type arguments of
     * {@code Map<String, List<Map<Integer, String>>>} from the assignment target.
     */
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

    /**
     * JLS 15.9.3, 15.9.5 (JEP 213): diamond is legal on an anonymous class
     * {@code new Processor<>() { ... }} when the target type supplies the type arguments.
     */
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
        assertPositive(className, javaSrc, 'Processed: Data')
    }

    /**
     * JLS 18.5.2: the assignment target of a poly invocation {@code <T> List<T> empty()}
     * is a witness for {@code T}, so {@code List<String> s = empty()} and
     * {@code List<Integer> i = empty()} are both well-typed.
     */
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
    // Category 5: Covariant returns and bridges (JLS 8.4.8.3, 15.12.4.5)
    // =========================================================================

    /**
     * JLS 8.4.8.3 (covariant returns) and 15.12.4.5: {@code String get()} overrides
     * {@code T get()} of {@code Supplier<String>}. A bridge with the erased signature
     * is invoked through a raw {@code Supplier} and still returns the {@code String}.
     */
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
    // Category 6: Raw types and unchecked conversion (JLS 4.8, 5.1.9)
    // =========================================================================

    /**
     * JLS 4.8, 5.1.9: assignment of a parameterized type to a raw type (and the reverse)
     * is an unchecked conversion. The raw {@code List} may be mutated without compile-time
     * argument checks.
     */
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
    // Category 7: Type variables in throws (JLS 8.4.6)
    // =========================================================================

    /**
     * JLS 8.4.6 (Example 8.4.6-1): a type variable whose bound is a subtype of
     * {@code Throwable} may appear in a {@code throws} clause, so
     * {@code <E extends Exception> void runAction(Action<E>) throws E} is legal.
     */
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
    // Category 8: Negative well-formedness (JLS 4.5, 4.10.2, 8.1.2, 8.1.4, 8.1.5)
    // =========================================================================

    /**
     * JLS 8.1.2: it is a compile-time error if a generic class is a direct or
     * indirect subclass of {@code Throwable}.
     */
    @Test
    void testNegativeGenericSubclassOfThrowable() {
        final String className = 'gls.generics.test.GenEx'
        final String javaSrc = '''
            package gls.generics.test;
            public class GenEx<T> extends Exception {
                private T info;
            }
        '''
        assertNegativeCompile(className, javaSrc, "generic class may not extend", "generic class may not extend")
    }

    /**
     * JLS 4.5: a parameterized type is well-formed only if the number of type
     * arguments equals the number of type parameters ({@code List} has one).
     */
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

    /**
     * JLS 4.5: the same arity rule applies wherever a parameterized type is used,
     * including a method formal ({@code Map} has two type parameters).
     */
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

    /**
     * JLS 4.10.2: parameterized types are invariant in their type arguments.
     * {@code ArrayList<Integer>} is not a subtype of {@code List<Number>}.
     */
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

    /**
     * JLS 4.5, 4.4: a type argument must be a subtype of every type in the
     * corresponding bound after substitution. {@code Object} is not a
     * {@code Number & Comparable<Object>}.
     */
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

    /**
     * JLS 8.1.4 (and 8.1.5): if a superclass or superinterface type has type
     * arguments, none of those arguments may be wildcards.
     */
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

    /**
     * JLS 8.1.5: a superinterface type may not specify a wildcard
     * ({@code implements List<?>}).
     */
    @Test
    void testNegativeWildcardInImplementsClause() {
        final String className = 'gls.generics.test.WildcardImplements'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.List;
            public class WildcardImplements implements List<?> {
            }
        '''
        assertNegativeCompile(className, javaSrc, "unexpected type", "A supertype may not specify a wildcard type")
    }

    /**
     * JLS 4.5: a parameterized type {@code C<T1,...,Tn>} is well-formed only if
     * {@code C} names a generic class or interface. {@code Date} takes no type parameters.
     */
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

    /**
     * JLS 8.1.5 (Example 8.1.5-3): a class may not implement two different
     * parameterizations of the same generic interface (needed for translation by
     * erasure, JLS 4.6).
     */
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
        assertNegativeCompile(className, javaSrc, "different arguments", "cannot be implemented more than once with different arguments")
    }

    /**
     * JLS 5.3, 4.5.2: invocation of {@code List<String>#add} requires an argument
     * compatible with the substituted element type {@code String}.
     */
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

    /**
     * JLS 4.8, 4.12.2, 5.1.9: heap pollution through a raw {@code Sink} makes a later
     * use of the erased {@code accept(T)} throw {@code ClassCastException} at the
     * compiler-inserted cast (JLS 15.5).
     */
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
    // Category 9: Inheritance, enclosing types and bound forwarding (JLS 8.1.3–8.1.5, 4.5)
    // =========================================================================

    /**
     * JLS 8.1.4, 4.5.2: a non-generic subclass may extend a parameterization
     * ({@code Named extends Holder<String>}); inherited members have the substituted types.
     */
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

    /**
     * JLS 8.1.2, 4.5.2: a subclass may forward its type parameter to a generic
     * superclass ({@code Box<T> extends Holder<T>}).
     */
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

    /**
     * JLS 9.1.2, 9.1.3: a generic interface may extend a parameterization of
     * another generic interface ({@code SrcNum extends Src<Number>}).
     */
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

    /**
     * JLS 8.1.3, 6.5.5.1: a non-static inner class is not a static context, so it
     * may refer to a type parameter of the enclosing generic class.
     */
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

    /**
     * JLS 4.5: a nested parameterization such as {@code Outer<String>.Inner<Integer>}
     * (a "rare" type) is well-formed when both the enclosing type and the member
     * type are generic.
     */
    @Test
    void testRareTypeQualifiedInnerClass() {
        final String className = 'gls.generics.test.RareTypeInner'
        final String javaSrc = '''
            package gls.generics.test;
            public class RareTypeInner {
                public static class Outer<T> {
                    public class Inner<U> {
                        private final U u;
                        Inner(U u) { this.u = u; }
                        public U getU() { return u; }
                    }
                    public Inner<Integer> make(Integer u) { return new Inner<Integer>(u); }
                }
                public static String test() {
                    Outer<String> outer = new Outer<String>();
                    Outer<String>.Inner<Integer> inner = outer.make(Integer.valueOf(7));
                    return String.valueOf(inner.getU());
                }
            }
        '''
        assertPositive(className, javaSrc, '7')
    }

    /**
     * JLS 4.5, 4.11: a rare type may appear as a field type and a method parameter
     * or return type; the Signature attribute records the enclosing type arguments.
     */
    @Test
    void testRareTypeFieldAndMethodSignatures() {
        final String className = 'gls.generics.test.RareTypeSignatures'
        final String javaSrc = '''
            package gls.generics.test;
            public class RareTypeSignatures {
                public static class Outer<T> {
                    public class Inner<U> {
                        public U u;
                    }
                }
                public Outer<String>.Inner<Integer> field;
                public Outer<String>.Inner<Integer> echo(Outer<String>.Inner<Integer> p) { return p; }
                public static String test() { return "ok"; }
            }
        '''
        assertPositive(className, javaSrc, 'ok', true)
    }

    /**
     * JLS 4.5: {@code A<String>.B.C<Integer>} is a parameterized type even though
     * the middle member {@code B} is not generic, because type arguments appear
     * in the enclosing type.
     */
    @Test
    void testRareTypeUnparameterizedMiddleSignatures() {
        final String className = 'gls.generics.test.RareTypeMiddle'
        final String javaSrc = '''
            package gls.generics.test;
            public class RareTypeMiddle {
                public static class A<T> {
                    public class B {
                        public class C<U> {
                            public U u;
                        }
                    }
                }
                public A<String>.B.C<Integer> field;
                public static String test() { return "ok"; }
            }
        '''
        assertPositive(className, javaSrc, 'ok', true)
    }

    /**
     * JLS 15.9, 8.1.3: a qualified class instance creation
     * {@code new Outer<T>.Inner<Integer>(...)} instantiates the inner class of the
     * enclosing parameterization.
     */
    @Test
    void testRareTypeInstanceCreationInsideOuter() {
        final String className = 'gls.generics.test.RareTypeNew'
        final String javaSrc = '''
            package gls.generics.test;
            public class RareTypeNew {
                public static class Outer<T> {
                    public class Inner<U> {
                        private final U u;
                        Inner(U u) { this.u = u; }
                        public U getU() { return u; }
                    }
                    public Inner<Integer> make() {
                        return new Outer<T>.Inner<Integer>(Integer.valueOf(7));
                    }
                }
                public static String test() {
                    return String.valueOf(new Outer<String>().make().getU());
                }
            }
        '''
        assertPositive(className, javaSrc, '7')
    }

    /**
     * JLS 8.1.1.4, 8.1.2: a {@code static} nested class has no enclosing instance
     * and must not depend on the outer class's type parameters; it may declare its own.
     */
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

    /**
     * JLS 4.4: a type-variable bound may itself be a type variable
     * ({@code U extends T}). {@code T} is inferred as a supertype of the argument.
     */
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

    /**
     * JLS 8.1.2 (Example 8.1.2-1): type parameters in one section may mention each
     * other, including a later parameter ({@code T extends List<X>, X extends Number}).
     * A parameter may not depend on itself.
     */
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

    /**
     * JLS 4.4: an F-bound {@code T extends Entity<T>} encodes a self type so
     * {@code fluent()} can return the concrete subclass rather than the raw base.
     */
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

    /**
     * JLS 4.4 (Example 4.4-1), 4.9: the members of {@code T extends Number & Comparable<T>}
     * are the members of that intersection, so both {@code intValue()} and
     * {@code compareTo} are applicable.
     */
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

    /**
     * JLS 4.4, 8.1.2: independent type parameters may each carry their own bound
     * ({@code A extends CharSequence, B extends Number}).
     */
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
    // Category 10: Generic methods of generic types; arrays as type arguments (JLS 8.4.4, 4.5.1)
    // =========================================================================

    /**
     * JLS 8.4.4: a generic method of a generic class introduces method type
     * parameters distinct from the class's ({@code Box<T>#<U> U map(Function<T, U>)}).
     */
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

    /**
     * JLS 4.5.1: a type argument is a reference type or a wildcard. An array type
     * such as {@code String[]} is a reference type, so {@code List<String[]>} is well-formed.
     */
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

    /**
     * JLS 18.5.2: nested poly invocations {@code id(id("ok"))} constrain the inner
     * and outer inference variables from the same target type.
     */
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

    /**
     * JLS 8.4.2, 8.4.8.1: a generic method overrides another when the signatures are
     * override-equivalent, including after renaming type parameters with matching bounds.
     */
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
    // Category 11: PECS, nested wildcards and function variance (JLS 4.5.1, 4.10.2)
    // =========================================================================

    /**
     * JLS 4.5.1 containment and 4.10.2: {@code Collections.copy(List<? super T>, List<? extends T>)}
     * is the PECS form of a copy — destination is a consumer, source is a producer.
     */
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

    /**
     * JLS 4.5.1: wildcards nest. {@code List<? extends List<? extends Number>>} contains
     * {@code List<List<Integer>>} by applying containment recursively.
     */
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

    /**
     * JLS 4.5.1: {@code List<Integer>#sort} accepts {@code Comparator<? super Integer>},
     * so a {@code Comparator<Number>} may order a list of {@code Integer}.
     */
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

    /**
     * JLS 4.5.1, 9.9: {@code Function<? super T, ? extends R>} is the flexible function
     * type — argument consumer, result producer — used by {@code apply}.
     */
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

    /**
     * JLS 4.5.2, 5.1.10: capture of {@code List<? super Integer>} yields an unknown
     * supertype of {@code Integer}; reading an element therefore has type {@code Object}.
     */
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

    /**
     * JLS 4.5.1, 5.2: the null reference is assignment-compatible with every reference
     * type, so {@code list.add(null)} is legal even on {@code List<?>}.
     */
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
    // Category 12: Invocation-context diamond, unchecked casts, nested members (JLS 15.9.3, 5.1.9, 4.5)
    // =========================================================================

    /**
     * JLS 15.9.3, 18.5.2, 5.3: the invocation context of {@code len(List<String>)} is a
     * target-type witness for {@code new ArrayList<>()}.
     */
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

    /**
     * JLS 5.5, 5.1.9: a cast to a non-reifiable parameterized type is unchecked.
     * The value is then used as {@code List<String>} at compile time.
     */
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

    /**
     * JLS 5.1.9, 4.8: assigning a raw type to a parameterization is an unchecked
     * conversion; subsequent uses see the parameterized type.
     */
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

    /**
     * JLS 4.5, 9.5: {@code Map.Entry<K, V>} is a member interface of a generic type.
     * {@code Map.Entry<String, Integer>} is a parameterization of that member.
     */
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
    // Category 13: Covariant override, lub, generic records, reifiable instanceof (JLS 8.4.8.3, 4.10.4, 8.10, 4.7)
    // =========================================================================

    /**
     * JLS 8.4.8.3: {@code String id(String)} in {@code Child extends Parent<String>}
     * is return-type-substitutable for {@code T id(T)} and overrides it.
     */
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

    /**
     * JLS 15.25.3, 4.10.4: a reference conditional expression has the lub of its
     * operand types ({@code Class<?>} for class literals, {@code CharSequence} for
     * {@code String} and {@code StringBuilder}).
     */
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

    /**
     * JLS 8.10, 8.1.2: a record class may be generic. Canonical accessors and the
     * compact constructor use the substituted component types.
     */
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

    /**
     * JLS 4.7, 15.20.2: {@code List<?>}, {@code ArrayList<?>}, {@code Class<?>}
     * and {@code Map<?,?>} are reifiable (every type argument is an unbounded
     * wildcard), as is the raw type {@code List}. A parameterized left operand
     * such as {@code List<String> list} is legal; {@code null instanceof List<?>}
     * is well-typed and yields {@code false}.
     */
    @Test
    void testReifiableInstanceofUnboundedWildcard() {
        final String className = 'gls.generics.test.ReifiableInstanceof'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.ArrayList;
            import java.util.HashMap;
            import java.util.List;
            import java.util.Map;
            public class ReifiableInstanceof {
                public static boolean test() {
                    List<String> list = new ArrayList<String>();
                    List<?> wildcard = list;
                    Map<String, Integer> map = new HashMap<String, Integer>();
                    Class<?> cls = ReifiableInstanceof.class.getClass();
                    List<String> none = null;
                    return (list instanceof List<?>)
                            && (list instanceof ArrayList<?>)
                            && (list instanceof List)
                            && (wildcard instanceof List<?>)
                            && (map instanceof Map<?, ?>)
                            && (cls instanceof Class<?>)
                            && !(none instanceof List<?>);
                }
            }
        '''
        assertPositive(className, javaSrc, true)
    }

    /**
     * JLS 4.7, 15.20.2: an array type is reifiable when its element type is
     * reifiable, so {@code o instanceof List<?>[]} is legal.
     */
    @Test
    void testReifiableInstanceofArrayOfUnboundedWildcard() {
        final String className = 'gls.generics.test.ReifiableInstanceofArray'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.List;
            public class ReifiableInstanceofArray {
                public static boolean test() {
                    Object o = new List[0];
                    return o instanceof List<?>[];
                }
            }
        '''
        assertPositive(className, javaSrc, true)
    }

    /**
     * JLS 4.7, 15.20.2: a non-static member type of a reifiable type is itself
     * reifiable, so {@code o instanceof Outer<?>.Inner} is legal when {@code Inner}
     * is not generic.
     */
    @Test
    void testReifiableInstanceofRareTypeUnboundedEnclosing() {
        final String className = 'gls.generics.test.ReifiableRareInstanceof'
        final String javaSrc = '''
            package gls.generics.test;
            public class ReifiableRareInstanceof {
                public static class Outer<T> {
                    public class Inner {}
                }
                public static boolean test() {
                    return !(null instanceof Outer<?>.Inner);
                }
            }
        '''
        assertPositive(className, javaSrc, true)
    }

    /**
     * JLS 4.7, 15.20.2 / JDK {@code T6665356}: when both the enclosing type and
     * the member are generic, {@code Outer.Inner} (raw) and
     * {@code Outer<?>.Inner<?>} (all unbounded wildcards) are reifiable.
     */
    @Test
    void testReifiableInstanceofRawAndFullyWildcardNested() {
        final String className = 'gls.generics.test.ReifiableNestedInstanceof'
        final String javaSrc = '''
            package gls.generics.test;
            public class ReifiableNestedInstanceof {
                public static class Outer<T> {
                    public class Inner<U> {}
                }
                public static boolean test() {
                    return !(null instanceof Outer.Inner)
                            && !(null instanceof Outer<?>.Inner<?>);
                }
            }
        '''
        assertPositive(className, javaSrc, true)
    }

    /**
     * JLS 4.4, 4.5.1, 8.4.4, 13.1: the reflective Signature of type parameters,
     * bounds, wildcards, generic methods and generic superclasses must match
     * between javac and static Groovy.
     */
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
    // Category 14: Additional negatives both compilers share (JLS 4.7, 6.5.5.1, 4.5.1)
    // =========================================================================

    /**
     * JLS 4.7, 15.20.2: {@code List<String>} is not reifiable, so
     * {@code o instanceof List<String>} is a compile-time error.
     */
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

    /**
     * JLS 4.7, 15.20.2: {@code Map<String,Integer>} is not reifiable.
     */
    @Test
    void testNegativeNonReifiableInstanceofMap() {
        final String className = 'gls.generics.test.NonReifiableInstanceofMap'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.Map;
            public class NonReifiableInstanceofMap {
                public static boolean test(Object o) {
                    return o instanceof Map<String, Integer>;
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "cannot be safely cast", "Cannot perform instanceof")
    }

    /**
     * JLS 15.20.2: a non-reifiable {@code instanceof} is still illegal when the
     * check is negated ({@code !(o instanceof Map<String,Integer>)}).
     */
    @Test
    void testNegativeNegatedNonReifiableInstanceof() {
        final String className = 'gls.generics.test.NegatedNonReifiableInstanceof'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.Map;
            public class NegatedNonReifiableInstanceof {
                public static boolean test(Object o) {
                    return !(o instanceof Map<String, Integer>);
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "cannot be safely cast", "Cannot perform instanceof")
    }

    /**
     * JLS 4.7, 15.20.2: {@code List<? extends CharSequence>} is not reifiable
     * because the wildcard is bounded (GROOVY-11585).
     */
    @Test
    void testNegativeInstanceofUpperBoundedWildcard() {
        final String className = 'gls.generics.test.InstanceofUpperBoundedWildcard'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.List;
            public class InstanceofUpperBoundedWildcard {
                public static boolean test(Object o) {
                    return o instanceof List<? extends CharSequence>;
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "cannot be safely cast", "Cannot perform instanceof")
    }

    /**
     * JLS 4.7, 15.20.2: {@code List<? super Integer>} is not reifiable because
     * the wildcard is bounded (GROOVY-11585).
     */
    @Test
    void testNegativeInstanceofLowerBoundedWildcard() {
        final String className = 'gls.generics.test.InstanceofLowerBoundedWildcard'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.List;
            public class InstanceofLowerBoundedWildcard {
                public static boolean test(Object o) {
                    return o instanceof List<? super Integer>;
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "cannot be safely cast", "Cannot perform instanceof")
    }

    /**
     * JLS 4.7, 15.20.2: {@code Outer<String>.Inner} is not reifiable — a nested
     * type is reifiable only as a non-static member of a reifiable type, and
     * {@code Outer<String>} is not. Contrast
     * {@link #testReifiableInstanceofRareTypeUnboundedEnclosing()}.
     */
    @Test
    void testNegativeInstanceofRareType() {
        final String className = 'gls.generics.test.InstanceofRareType'
        final String javaSrc = '''
            package gls.generics.test;
            public class InstanceofRareType {
                public static class Outer<T> {
                    public class Inner {}
                }
                public static boolean test(Object o) {
                    return o instanceof Outer<String>.Inner;
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "cannot be safely cast", "Cannot perform instanceof")
    }

    /**
     * JLS 4.7, 15.20.2: {@code List<String>[]} is not reifiable (the component
     * type is not).
     */
    @Test
    void testNegativeInstanceofArrayOfParameterizedType() {
        final String className = 'gls.generics.test.InstanceofGenericArray'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.List;
            public class InstanceofGenericArray {
                public static boolean test(Object o) {
                    return o instanceof List<String>[];
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "cannot be safely cast", "Cannot perform instanceof")
    }

    /**
     * JLS 4.7, 15.20.2: {@code Map<?,String>} is not reifiable — every type
     * argument must be an unbounded wildcard.
     */
    @Test
    void testNegativeInstanceofMixedWildcard() {
        final String className = 'gls.generics.test.InstanceofMixedWildcard'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.Map;
            public class InstanceofMixedWildcard {
                public static boolean test(Object o) {
                    return o instanceof Map<?, String>;
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "cannot be safely cast", "Cannot perform instanceof")
    }

    /**
     * JLS 4.7, 15.20.2: a type variable is not reifiable, so {@code o instanceof T}
     * is a compile-time error.
     */
    @Test
    void testNegativeInstanceofTypeParameter() {
        final String className = 'gls.generics.test.InstanceofTypeParameter'
        final String javaSrc = '''
            package gls.generics.test;
            public class InstanceofTypeParameter {
                public static <T> boolean test(Object o) {
                    return o instanceof T;
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "cannot be safely cast", "type parameter")
    }

    /**
     * JLS 4.7, 15.20.2: an array of a type variable is not reifiable
     * ({@code o instanceof T[]}).
     */
    @Test
    void testNegativeInstanceofTypeParameterArray() {
        final String className = 'gls.generics.test.InstanceofTypeParameterArray'
        final String javaSrc = '''
            package gls.generics.test;
            public class InstanceofTypeParameterArray {
                public static <T> boolean test(Object o) {
                    return o instanceof T[];
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "cannot be safely cast", "type parameter")
    }

    /**
     * JLS 4.5 / JDK {@code T6665356}: {@code Outer<?>.Inner} is malformed when
     * {@code Inner} is generic (raw member of a parameterized enclosing type).
     */
    @Test
    void testNegativeInstanceofRawGenericMemberOfParameterizedEnclosing() {
        final String className = 'gls.generics.test.InstanceofRawMemberOfParameterized'
        final String javaSrc = '''
            package gls.generics.test;
            public class InstanceofRawMemberOfParameterized {
                public static class Outer<T> {
                    public class Inner<U> {}
                }
                public static boolean test(Object o) {
                    return o instanceof Outer<?>.Inner;
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "improperly formed type", "parameterized enclosing type")
    }

    /**
     * JLS 4.8 / JDK {@code T6665356}: {@code Outer.Inner<?>} gives type arguments
     * on a member of a raw type.
     */
    @Test
    void testNegativeInstanceofTypeArgsOnRawNested() {
        final String className = 'gls.generics.test.InstanceofParamOnRawNested'
        final String javaSrc = '''
            package gls.generics.test;
            public class InstanceofParamOnRawNested {
                public static class Outer<T> {
                    public class Inner<U> {}
                }
                public static boolean test(Object o) {
                    return o instanceof Outer.Inner<?>;
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "improperly formed type", "raw")
    }

    /**
     * JLS 4.8 / JDK {@code T6665356}: the same malformed type is illegal as a
     * field type, not only as an {@code instanceof} operand.
     */
    @Test
    void testNegativeTypeArgsOnRawNestedField() {
        final String className = 'gls.generics.test.RawNestedField'
        final String javaSrc = '''
            package gls.generics.test;
            public class RawNestedField {
                public static class Outer<T> {
                    public class Inner<U> {}
                }
                public Outer.Inner<?> field;
            }
        '''
        assertNegativeCompile(className, javaSrc, "improperly formed type", "raw")
    }

    /**
     * JLS 4.7, 15.20.2: {@code Class<C>} is not reifiable (the type argument is not
     * an unbounded wildcard), so it may not be used with {@code instanceof}.
     */
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

    /**
     * JLS 4.7, 15.20.2 / JDK {@code InstanceOf3}: {@code Class<? extends C>} is
     * not reifiable (the wildcard is bounded).
     */
    @Test
    void testNegativeInstanceofClassUpperBoundedWildcard() {
        final String className = 'gls.generics.test.InstanceofClassUpperBound'
        final String javaSrc = '''
            package gls.generics.test;
            public class InstanceofClassUpperBound {
                public static boolean test() {
                    return InstanceofClassUpperBound.class.getClass() instanceof Class<? extends InstanceofClassUpperBound>;
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "cannot be converted", "Cannot perform instanceof")
    }

    /**
     * JLS 6.5.5.1, 8.1.2: a type parameter of the enclosing class is not in scope
     * from a static nested class.
     */
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
        assertNegativeCompile(className, javaSrc, "non-static type variable", "cannot be referenced from a static context")
    }

    /**
     * JLS 8.3.1.1, 6.5.5.1: a {@code static} field is a static context and may not
     * refer to a type parameter of the enclosing class.
     */
    @Test
    void testNegativeTypeParameterStaticField() {
        final String className = 'gls.generics.test.StaticTypeParamField'
        final String javaSrc = '''
            package gls.generics.test;
            public class StaticTypeParamField<T> {
                static T field;
            }
        '''
        assertNegativeCompile(className, javaSrc, "non-static type variable", "cannot be referenced from a static context")
    }

    /**
     * JLS 4.5.1, 5.1.10: after capture, the element type of {@code List<? extends Number>}
     * is an unknown subtype of {@code Number}; {@code Integer} is not a subtype of that
     * capture, so {@code add} is rejected.
     */
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
    // Category 15: Erasure, reifiable arrays, static members, remaining JLS restrictions
    // =========================================================================

    /**
     * JLS 4.6, 8.1.2: the erasure of {@code G<T1,...,Tn>} is {@code |G|}. All
     * parameterizations of a generic class therefore share one run-time class.
     */
    @Test
    void testErasureSharesRuntimeClass() {
        final String className = 'gls.generics.test.ErasureRuntimeClass'
        final String javaSrc = '''
            package gls.generics.test;
            public class ErasureRuntimeClass {
                public static class Box<T> {
                    public final T value;
                    public Box(T value) { this.value = value; }
                }
                public static boolean test() {
                    Box<String> strings = new Box<>("a");
                    Box<Integer> ints = new Box<>(Integer.valueOf(1));
                    return strings.getClass() == ints.getClass();
                }
            }
        '''
        assertPositive(className, javaSrc, true, true)
    }

    /**
     * JLS 4.5.1 containment and 4.10.2: {@code List<Integer>} is a subtype of
     * {@code List<? extends Number>} even though it is not a subtype of
     * {@code List<Number>} (invariance is covered by
     * {@link #testNegativeGenericInvarianceAssignment()}).
     */
    @Test
    void testWildcardContainmentAssignment() {
        final String className = 'gls.generics.test.WildcardContainment'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.ArrayList;
            import java.util.List;
            public class WildcardContainment {
                public static int test() {
                    List<Integer> ints = new ArrayList<Integer>();
                    ints.add(Integer.valueOf(4));
                    List<? extends Number> nums = ints;
                    List<?> any = ints;
                    return nums.get(0).intValue() + any.size();
                }
            }
        '''
        assertPositive(className, javaSrc, 5)
    }

    /**
     * JLS 15.10.1, 4.7: an array creation is legal when the component type is
     * reifiable. {@code List<?>} is reifiable, so {@code new List<?>[n]} is allowed.
     */
    @Test
    void testReifiableGenericArrayCreation() {
        final String className = 'gls.generics.test.ReifiableArrayCreation'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.ArrayList;
            import java.util.List;
            public class ReifiableArrayCreation {
                public static int test() {
                    List<?>[] lists = new List<?>[1];
                    lists[0] = new ArrayList<String>();
                    return lists.length;
                }
            }
        '''
        assertPositive(className, javaSrc, 1)
    }

    /**
     * JLS 4.8: the type of a member of a raw type is the erasure of that member
     * as declared. {@code Box.get()} therefore has return type {@code Object}.
     */
    @Test
    void testRawTypeMemberErasure() {
        final String className = 'gls.generics.test.RawMemberErasure'
        final String javaSrc = '''
            package gls.generics.test;
            public class RawMemberErasure {
                public static class Box<T> {
                    private final T value;
                    public Box(T value) { this.value = value; }
                    public T get() { return value; }
                }
                @SuppressWarnings({"rawtypes", "unchecked"})
                public static String test() {
                    Box raw = new Box("raw");
                    Object value = raw.get();
                    return value.getClass().getSimpleName();
                }
            }
        '''
        assertPositive(className, javaSrc, 'String')
    }

    /**
     * JLS 4.5.2: a static member of a generic type must be referred to using the
     * generic type name, not a parameterization ({@code Cell.id()}, not
     * {@code Cell<String>.id()}).
     */
    @Test
    void testStaticMemberViaGenericTypeName() {
        final String className = 'gls.generics.test.StaticViaGenericName'
        final String javaSrc = '''
            package gls.generics.test;
            public class StaticViaGenericName {
                public static class Cell<T> {
                    public static String id() { return "cell"; }
                }
                public static String test() {
                    return Cell.id();
                }
            }
        '''
        assertPositive(className, javaSrc, 'cell', true)
    }

    /**
     * JLS 15.8.3: in a generic class {@code C<F1,...,Fn>}, the type of {@code this}
     * is {@code C<F1,...,Fn>}, so a method may return {@code this} at that type.
     */
    @Test
    void testThisTypeInGenericClass() {
        final String className = 'gls.generics.test.GenericThisType'
        final String javaSrc = '''
            package gls.generics.test;
            public class GenericThisType {
                public static class Box<T> {
                    public T value;
                    public Box(T value) { this.value = value; }
                    public Box<T> self() { return this; }
                }
                public static String test() {
                    return new Box<String>("me").self().value;
                }
            }
        '''
        assertPositive(className, javaSrc, 'me', true)
    }

    /**
     * JLS 15.9.3, 18.5.2: a diamond class instance creation in a return context
     * takes the method result type as its target type.
     */
    @Test
    void testDiamondInferredFromReturnType() {
        final String className = 'gls.generics.test.DiamondReturn'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.ArrayList;
            import java.util.List;
            public class DiamondReturn {
                public static List<String> empty() {
                    return new ArrayList<>();
                }
                public static int test() {
                    return empty().size();
                }
            }
        '''
        assertPositive(className, javaSrc, 0)
    }

    /**
     * JLS 4.5: primitive types cannot be type arguments ({@code List<int>} is
     * not well-formed). The corresponding boxed reference type must be used.
     */
    @Test
    void testNegativePrimitiveTypeArgument() {
        final String className = 'gls.generics.test.PrimitiveTypeArg'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.List;
            public class PrimitiveTypeArg {
                public List<int> list;
            }
        '''
        assertNegativeCompile(className, javaSrc, "unexpected type", "primitive type")
    }

    /**
     * JLS 15.10.1, 4.7: it is a compile-time error to create an array whose
     * component type is not reifiable ({@code new List<String>[n]}).
     */
    @Test
    void testNegativeNonReifiableArrayCreation() {
        final String className = 'gls.generics.test.GenericArrayCreation'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.List;
            public class GenericArrayCreation {
                public static Object test() {
                    return new List<String>[1];
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "generic array creation", "generic array creation")
    }

    /**
     * JLS 15.10.1, 4.7: a parameterized type is reifiable only when every type
     * argument is an unbounded wildcard. {@code List<? extends Number>} is not
     * reifiable, so {@code new List<? extends Number>[n]} is a compile-time error.
     * The legal counterpart is {@link #testReifiableGenericArrayCreation()}.
     */
    @Test
    void testNegativeBoundedWildcardArrayCreation() {
        final String className = 'gls.generics.test.BoundedWildcardArrayCreation'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.List;
            public class BoundedWildcardArrayCreation {
                public static Object test() {
                    return new List<? extends Number>[1];
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "generic array creation", "generic array creation")
    }

    /**
     * JLS 4.7, 15.10.1: {@code Outer<?>} is reifiable and a non-static member
     * type of a reifiable type is reifiable, so {@code new Outer<?>.Inner[n]}
     * is a legal array creation. javac 25 accepts this; Groovy must as well.
     */
    @Test
    void testReifiableRareTypeArrayCreation() {
        final String className = 'gls.generics.test.RareTypeReifiableArray'
        final String javaSrc = '''
            package gls.generics.test;
            public class RareTypeReifiableArray {
                public static class Outer<T> {
                    public class Inner {}
                }
                public static int test() {
                    Outer<?>.Inner[] arr = new Outer<?>.Inner[0];
                    return arr.length;
                }
            }
        '''
        assertPositive(className, javaSrc, 0)
    }

    /**
     * JLS 4.7, 15.10.1: the generic-member counterpart is also reifiable when
     * every type argument is an unbounded wildcard
     * ({@code new Outer<?>.InnerG<?>[n]}).
     */
    @Test
    void testReifiableRareTypeGenericInnerArrayCreation() {
        final String className = 'gls.generics.test.RareTypeReifiableGenericInnerArray'
        final String javaSrc = '''
            package gls.generics.test;
            public class RareTypeReifiableGenericInnerArray {
                public static class Outer<T> {
                    public class InnerG<U> {}
                }
                public static int test() {
                    Outer<?>.InnerG<?>[] arr = new Outer<?>.InnerG<?>[0];
                    return arr.length;
                }
            }
        '''
        assertPositive(className, javaSrc, 0)
    }

    /**
     * JLS 4.5.2, 6.5.5: a static member type may not be selected from a
     * parameterized type ({@code new Outer<?>.Nested[n]}).
     */
    @Test
    void testNegativeStaticNestedTypeViaParameterizedEnclosing() {
        final String className = 'gls.generics.test.StaticNestedViaParameterized'
        final String javaSrc = '''
            package gls.generics.test;
            public class StaticNestedViaParameterized {
                public static class Outer<T> {
                    public static class Nested {}
                }
                public static Object test() {
                    return new Outer<?>.Nested[0];
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "cannot select a static class from a parameterized type", "static nested type")
    }

    /**
     * JLS 9.5, 6.5.5: a nested interface is implicitly static, so
     * {@code implements Outer<String>.Inner} is the same error as
     * {@code new Outer<?>.Nested[n]}.
     */
    @Test
    void testNegativeImplementsStaticNestedViaParameterizedEnclosing() {
        final String className = 'gls.generics.test.ImplementsStaticNestedViaParameterized'
        final String javaSrc = '''
            package gls.generics.test;
            public class ImplementsStaticNestedViaParameterized {
                public static class Outer<T> {
                    public interface Inner<U> {
                        U id(U u);
                    }
                }
                public static class Impl implements Outer<String>.Inner<Integer> {
                    public Integer id(Integer u) { return u; }
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "cannot select a static class from a parameterized type", "static nested type")
    }

    /**
     * JLS 9.5, 6.5.5: {@code Map.Entry} is a nested interface, so
     * {@code new Map<?,?>.Entry[n]} cannot select it from a parameterization.
     */
    @Test
    void testNegativeMapEntryViaParameterizedEnclosing() {
        final String className = 'gls.generics.test.MapEntryViaParameterized'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.Map;
            public class MapEntryViaParameterized {
                public static Object test() {
                    return new Map<?,?>.Entry[0];
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "cannot select a static class from a parameterized type", "static nested type")
    }

    /**
     * A generic member type used raw under a parameterized enclosing type is
     * malformed ({@code new Outer<?>.InnerG[n]}).
     */
    @Test
    void testNegativeRawGenericMemberOfParameterizedEnclosing() {
        final String className = 'gls.generics.test.RawMemberOfParameterized'
        final String javaSrc = '''
            package gls.generics.test;
            public class RawMemberOfParameterized {
                public static class Outer<T> {
                    public class InnerG<U> {}
                }
                public static Object test() {
                    return new Outer<?>.InnerG[0];
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "improperly formed type", "parameterized enclosing type")
    }

    /**
     * JLS 15.9.1: a class instance creation may not instantiate a type variable
     * ({@code new T()}).
     */
    @Test
    void testNegativeInstantiateTypeVariable() {
        final String className = 'gls.generics.test.NewTypeVariable'
        final String javaSrc = '''
            package gls.generics.test;
            public class NewTypeVariable {
                public static <T> T make() {
                    return new T();
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "type parameter", "type parameter")
    }

    /**
     * JLS 15.8.2: a class literal's {@code TypeName} may not denote a type
     * variable ({@code T.class}).
     */
    @Test
    void testNegativeTypeVariableClassLiteral() {
        final String className = 'gls.generics.test.TypeVarClassLiteral'
        final String javaSrc = '''
            package gls.generics.test;
            public class TypeVarClassLiteral {
                public static <T> Class<T> token() {
                    return T.class;
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "cannot select from a type variable", "type parameter")
    }

    /**
     * JLS 4.5.2: it is illegal to refer to a static member of a generic type
     * through a parameterization ({@code Cell<String>.id()}).
     */
    @Test
    void testNegativeStaticMemberViaParameterizedType() {
        final String className = 'gls.generics.test.StaticViaParameterized'
        final String javaSrc = '''
            package gls.generics.test;
            public class StaticViaParameterized {
                public static class Cell<T> {
                    public static String id() { return "cell"; }
                }
                public static String test() {
                    return Cell<String>.id();
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "illegal start of type", "parameterization")
    }

    /**
     * JLS 8.4.2, 8.4.8.3 (Example 8.4.8.3-4): two methods of a class may not
     * have override-equivalent signatures after erasure
     * ({@code m(List<String>)} and {@code m(List<Integer>)} both erase to {@code m(List)}).
     */
    @Test
    void testNegativeSameErasureOverload() {
        final String className = 'gls.generics.test.SameErasureOverload'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.List;
            public class SameErasureOverload {
                public void m(List<String> a) {}
                public void m(List<Integer> a) {}
            }
        '''
        assertNegativeCompile(className, javaSrc, "erasure", "duplicates another method")
    }

    /**
     * JLS 15.9: a wildcard may not appear after {@code new} in a class instance
     * creation expression ({@code new ArrayList<?>()}).
     */
    @Test
    void testNegativeWildcardAfterNew() {
        final String className = 'gls.generics.test.WildcardAfterNew'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.ArrayList;
            public class WildcardAfterNew {
                public static Object test() {
                    return new ArrayList<?>();
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "unexpected type", "wildcard type")
    }

    /**
     * JLS 15.9: diamond {@code <>} may not be combined with explicit constructor
     * type arguments.
     */
    @Test
    void testNegativeDiamondAndConstructorTypeArguments() {
        final String className = 'gls.generics.test.DiamondAndCtorArgs'
        final String javaSrc = '''
            package gls.generics.test;
            public class DiamondAndCtorArgs {
                public static class Box<T> {
                    public final T value;
                    public <U> Box(T value, U extra) { this.value = value; }
                }
                public static Object test() {
                    return new <Integer>Box<>("x", Integer.valueOf(1));
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "explicit type parameters for constructor", "Cannot use diamond")
    }

    /**
     * JLS 8.9: an enum class may not declare type parameters.
     */
    @Test
    void testNegativeGenericEnum() {
        final String className = 'gls.generics.test.GenericEnum'
        final String javaSrc = '''
            package gls.generics.test;
            public class GenericEnum {
                public enum Kind<T> { A }
            }
        '''
        assertNegativeCompile(className, javaSrc, "enum", "enum")
    }

    /**
     * JLS 4.4: a class type or type variable may appear only as the first type
     * of a bound; additional bounds must be interfaces
     * ({@code T extends Comparable<T> & Number} is illegal).
     */
    @Test
    void testNegativeClassTypeNotFirstInBound() {
        final String className = 'gls.generics.test.ClassBoundNotFirst'
        final String javaSrc = '''
            package gls.generics.test;
            public class ClassBoundNotFirst {
                public static <T extends Comparable<T> & Number> T id(T value) {
                    return value;
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "interface", "interface")
    }

    /**
     * JLS 15.10.1: the component type of an array creation may not be a type
     * variable ({@code new T[n]}).
     */
    @Test
    void testNegativeTypeParameterArrayCreation() {
        final String className = 'gls.generics.test.TypeParamArray'
        final String javaSrc = '''
            package gls.generics.test;
            public class TypeParamArray {
                public static <T> T[] make(int n) {
                    return new T[n];
                }
            }
        '''
        assertNegativeCompile(className, javaSrc, "generic array creation", "generic array creation")
    }

    // =========================================================================
    // Category 16: JCK four-dimension coverage (Java SE 25)
    //   1. erasure and bridges    2. multi-parameter / nested syntax
    //   3. Signature reflection   4. raw-type backward compatibility
    // =========================================================================

    /**
     * JLS 4.6: the erasure of an unbounded type variable is {@code Object};
     * the erasure of {@code T extends Number} is {@code Number}. The generic
     * return type remains a {@link TypeVariable}.
     */
    @Test
    void testErasureOfUnboundedAndBoundedTypeVariables() {
        final String className = 'gls.generics.test.ErasureOfTypeVariables'
        final String javaSrc = '''
            package gls.generics.test;
            import java.lang.reflect.Method;
            import java.lang.reflect.TypeVariable;
            public class ErasureOfTypeVariables {
                public static class Box<T> {
                    public T id(T t) { return t; }
                }
                public static class NumBox<T extends Number> {
                    public T id(T t) { return t; }
                }
                public static String test() throws Exception {
                    Method boxId = Box.class.getMethod("id", Object.class);
                    Method numId = NumBox.class.getMethod("id", Number.class);
                    boolean boxErasure = boxId.getReturnType() == Object.class
                            && boxId.getGenericReturnType() instanceof TypeVariable;
                    boolean numErasure = numId.getReturnType() == Number.class
                            && numId.getGenericReturnType() instanceof TypeVariable;
                    return (boxErasure && numErasure) ? "ok" : "fail";
                }
            }
        '''
        assertPositive(className, javaSrc, 'ok', true)
    }

    /**
     * JLS 8.4.8.3, 15.12.4.5: a covariant override of {@code T get()} as
     * {@code String get()} is compiled with a bridge {@code Object get()} so a
     * raw or erased invoke still reaches the specialized method.
     */
    @Test
    void testCovariantOverrideDeclaresBridgeMethod() {
        final String className = 'gls.generics.test.CovariantBridgePresent'
        final String javaSrc = '''
            package gls.generics.test;
            import java.lang.reflect.Method;
            public class CovariantBridgePresent {
                public interface Supplier<T> {
                    T get();
                }
                public static class StringSupplier implements Supplier<String> {
                    @Override
                    public String get() { return "bridged"; }
                }
                public static String test() {
                    boolean bridge = false;
                    boolean specialized = false;
                    for (Method m : StringSupplier.class.getDeclaredMethods()) {
                        if (!"get".equals(m.getName()) || m.getParameterCount() != 0) {
                            continue;
                        }
                        if (m.isBridge() && m.getReturnType() == Object.class) {
                            bridge = true;
                        }
                        if (!m.isBridge() && m.getReturnType() == String.class) {
                            specialized = true;
                        }
                    }
                    Supplier raw = new StringSupplier();
                    Object viaErased = raw.get();
                    return (bridge && specialized && "bridged".equals(viaErased)) ? "ok" : "fail";
                }
            }
        '''
        assertPositive(className, javaSrc, 'ok')
    }

    /**
     * JLS 8.1.2: three independent class type parameters {@code Triple<A, B, C>}
     * are substituted together; diamond infers all three from the constructor
     * arguments and the assignment target.
     */
    @Test
    void testTripleTypeParameters() {
        final String className = 'gls.generics.test.TripleTypeParams'
        final String javaSrc = '''
            package gls.generics.test;
            public class TripleTypeParams {
                public static class Triple<A, B, C> {
                    public final A a;
                    public final B b;
                    public final C c;
                    public Triple(A a, B b, C c) {
                        this.a = a;
                        this.b = b;
                        this.c = c;
                    }
                }
                public static String test() {
                    Triple<String, Integer, Double> t = new Triple<>("k", Integer.valueOf(1), Double.valueOf(2.0d));
                    return t.a + t.b + t.c.intValue();
                }
            }
        '''
        assertPositive(className, javaSrc, 'k12', true)
    }

    /**
     * JLS 4.5, 13.1: {@code Class#getGenericSuperclass()} of a class that
     * extends {@code Box<String>} is the parameterized type {@code Box<String>},
     * and {@code getActualTypeArguments()[0]} is {@code String.class}.
     */
    @Test
    void testGenericSuperclassActualTypeArguments() {
        final String className = 'gls.generics.test.GenericSuperclassArgs'
        final String javaSrc = '''
            package gls.generics.test;
            import java.lang.reflect.ParameterizedType;
            import java.lang.reflect.Type;
            public class GenericSuperclassArgs {
                public static class Box<T> {}
                public static class NamedBox extends Box<String> {}
                public static class StringIterable implements Iterable<String> {
                    @Override
                    public java.util.Iterator<String> iterator() {
                        return java.util.Collections.emptyIterator();
                    }
                }
                public static String test() {
                    Type sc = NamedBox.class.getGenericSuperclass();
                    ParameterizedType box = (ParameterizedType) sc;
                    Class<?> arg = (Class<?>) box.getActualTypeArguments()[0];
                    String ifaceArg = "missing";
                    for (Type t : StringIterable.class.getGenericInterfaces()) {
                        if (t instanceof ParameterizedType) {
                            ParameterizedType p = (ParameterizedType) t;
                            if (p.getRawType() == Iterable.class) {
                                ifaceArg = ((Class<?>) p.getActualTypeArguments()[0]).getSimpleName();
                            }
                        }
                    }
                    return arg.getName() + "|" + ifaceArg;
                }
            }
        '''
        assertPositive(className, javaSrc, 'java.lang.String|String', true)
    }

    /**
     * JLS 4.5, 10.1, 13.1: a field of type {@code List<String>[]} is a generic
     * array type in the Signature attribute. {@link GenericArrayType} reports
     * the component as {@code List<String>}. Creation of such an array remains
     * illegal; see {@link #testNegativeNonReifiableArrayCreation()}.
     */
    @Test
    void testGenericArrayTypeFieldSignature() {
        final String className = 'gls.generics.test.GenericArrayField'
        final String javaSrc = '''
            package gls.generics.test;
            import java.lang.reflect.GenericArrayType;
            import java.lang.reflect.ParameterizedType;
            import java.lang.reflect.Type;
            import java.util.List;
            public class GenericArrayField {
                public List<String>[] rows;
                public static String test() throws Exception {
                    Type t = GenericArrayField.class.getField("rows").getGenericType();
                    if (!(t instanceof GenericArrayType)) {
                        return "not-array:" + t;
                    }
                    Type component = ((GenericArrayType) t).getGenericComponentType();
                    if (!(component instanceof ParameterizedType)) {
                        return "not-param:" + component;
                    }
                    return ((Class<?>) ((ParameterizedType) component).getActualTypeArguments()[0]).getSimpleName();
                }
            }
        '''
        assertPositive(className, javaSrc, 'String', true)
    }

    /**
     * JLS 8.8.4, 13.1: a generic constructor {@code <T> CtorBox(T)} records its
     * type parameter on the constructor, independent of any class type parameters.
     */
    @Test
    void testGenericConstructorTypeParameterSignature() {
        final String className = 'gls.generics.test.GenericCtorSignature'
        final String javaSrc = '''
            package gls.generics.test;
            import java.lang.reflect.Constructor;
            import java.lang.reflect.TypeVariable;
            public class GenericCtorSignature {
                public static class CtorBox {
                    public final Object value;
                    public <T> CtorBox(T t) { this.value = t; }
                }
                public static int test() {
                    int found = 0;
                    for (Constructor<?> c : CtorBox.class.getDeclaredConstructors()) {
                        if (c.getParameterCount() == 1) {
                            TypeVariable<?>[] tps = c.getTypeParameters();
                            if (tps.length == 1 && "T".equals(tps[0].getName())) {
                                found++;
                            }
                        }
                    }
                    return found;
                }
            }
        '''
        assertPositive(className, javaSrc, 1, true)
    }

    /**
     * JLS 4.8: a raw subclass {@code class Raw extends Box} uses the erasure of
     * {@code Box}. {@code get()} therefore has compile-time type {@code Object}.
     */
    @Test
    void testRawSubclassOfGenericClass() {
        final String className = 'gls.generics.test.RawSubclass'
        final String javaSrc = '''
            package gls.generics.test;
            public class RawSubclass {
                public static class Box<T> {
                    public final T value;
                    public Box(T value) { this.value = value; }
                    public T get() { return value; }
                }
                @SuppressWarnings({"rawtypes", "unchecked"})
                public static class Raw extends Box {
                    public Raw(Object value) { super(value); }
                }
                @SuppressWarnings({"rawtypes", "unchecked"})
                public static String test() {
                    Raw raw = new Raw("hi");
                    Object value = raw.get();
                    return value.toString();
                }
            }
        '''
        assertPositive(className, javaSrc, 'hi')
    }

    /**
     * JLS 8.4.8.1, 4.8, 5.1.9: a raw subclass may override {@code name(T)} with
     * {@code name(Object)} (the erasure). Invocation through {@code Super<String>}
     * still dispatches to the subclass. javac 25 accepts this as an unchecked override.
     */
    @Test
    void testUncheckedOverrideOfGenericMethod() {
        final String className = 'gls.generics.test.UncheckedOverride'
        final String javaSrc = '''
            package gls.generics.test;
            public class UncheckedOverride {
                public static class Super<T> {
                    public String name(T t) { return "super"; }
                }
                @SuppressWarnings({"rawtypes", "unchecked"})
                public static class Child extends Super {
                    public String name(Object t) { return "child"; }
                }
                public static String test() {
                    Super<String> s = new Child();
                    return s.name("x");
                }
            }
        '''
        assertPositive(className, javaSrc, 'child')
    }

    // =========================================================================
    // Category 17: Overload resolution against erasure; F-bounded copy; intersection
    // erasure; Collection<Object> vs Collection<?> (JLS 4.4–4.6, 4.8, 5.1.10, 15.12.2)
    // =========================================================================

    /**
     * JLS 4.6, 15.12.2: inside {@code <T> generic(T t)} the compile-time type of
     * {@code t} is the erasure of {@code T} ({@code Object}), so {@code f(t)} binds
     * to {@code f(Object)} even when the invocation is {@code generic("string")}.
     */
    @Test
    void testGenericMethodOverloadResolvesAgainstErasure() {
        final String className = 'gls.generics.test.GenericOverloadErasure'
        final String javaSrc = '''
            package gls.generics.test;
            public class GenericOverloadErasure {
                public static String f(Object s) { return "object"; }
                public static String f(String s) { return "string"; }
                public static <T> String generic(T t) { return f(t); }
                public static String test() {
                    return generic("string");
                }
            }
        '''
        assertPositive(className, javaSrc, 'object')
    }

    /**
     * JLS 4.4: {@code Copyable<T extends Copyable<T>>} with {@code T copy()} makes
     * {@code node.copy().copy()} well-typed, because the result of {@code copy()}
     * is the same F-bounded type.
     */
    @Test
    void testFBoundedCopyAllowsChainedCopy() {
        final String className = 'gls.generics.test.FBoundedCopy'
        final String javaSrc = '''
            package gls.generics.test;
            public class FBoundedCopy {
                public interface Copyable<T extends Copyable<T>> {
                    T copy();
                }
                public static class Node implements Copyable<Node> {
                    @Override
                    public Node copy() { return this; }
                    public String id() { return "n"; }
                }
                public static String test() {
                    return new Node().copy().copy().id();
                }
            }
        '''
        assertPositive(className, javaSrc, 'n', true)
    }

    /**
     * JLS 4.4, 4.6, 4.9: the {@code Collections.max} shape
     * {@code <T extends Object & Comparable<? super T>> T max(Collection<? extends T>)}
     * erases the return type to the leftmost bound {@code Object}.
     */
    @Test
    void testIntersectionMaxErasureIsLeftmostBound() {
        final String className = 'gls.generics.test.IntersectionMaxErasure'
        final String javaSrc = '''
            package gls.generics.test;
            import java.lang.reflect.Method;
            import java.util.Collection;
            public class IntersectionMaxErasure {
                public static <T extends Object & Comparable<? super T>> T max(Collection<? extends T> coll) {
                    return null;
                }
                public static String test() throws Exception {
                    Method m = IntersectionMaxErasure.class.getMethod("max", Collection.class);
                    return m.getReturnType() == Object.class ? "ok" : m.getReturnType().getName();
                }
            }
        '''
        assertPositive(className, javaSrc, 'ok')
    }

    /**
     * JLS 4.5, 4.8: {@code Collection<Object>} accepts any reference.
     * {@code Collection<?>} is a capture of some unknown element type; see
     * {@link #testNegativeAddNonNullThroughUnboundedWildcard()}.
     */
    @Test
    void testCollectionObjectIsHeterogeneous() {
        final String className = 'gls.generics.test.HeterogeneousCollectionObject'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.ArrayList;
            import java.util.Collection;
            public class HeterogeneousCollectionObject {
                public static int test() {
                    Collection<Object> hetero = new ArrayList<Object>();
                    hetero.add("a");
                    hetero.add(Integer.valueOf(1));
                    return hetero.size();
                }
            }
        '''
        assertPositive(className, javaSrc, 2)
    }

    /**
     * JLS 5.1.10: after capture, the element type of {@code Collection<?>} is a
     * fresh type variable; {@code String} is not a subtype of that capture, so
     * {@code add("x")} is a compile-time error. {@code add(null)} remains legal
     * ({@link #testWildcardAllowsAddingNull()}).
     */
    @Test
    void testNegativeAddNonNullThroughUnboundedWildcard() {
        final String className = 'gls.generics.test.UnboundedWildcardAdd'
        final String javaSrc = '''
            package gls.generics.test;
            import java.util.Collection;
            public class UnboundedWildcardAdd {
                public static void test(Collection<?> c) {
                    c.add("x");
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
            String args = (typeArgs == null || typeArgs.length == 0) ? '' :
                '<' + typeArgs.collect { typeDesc(it) }.join(', ') + '>'
            if (p.ownerType != null) {
                String raw = rawName(p.rawType)
                int cut = Math.max(raw.lastIndexOf('$'), raw.lastIndexOf('.'))
                String simple = cut < 0 ? raw : raw.substring(cut + 1)
                return typeDesc(p.ownerType) + '$' + simple + args
            }
            return typeDesc(p.rawType) + args
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
