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
package gls.annotations.closures

import gls.CompilableTestSupport
import groovy.test.NotYetImplemented

import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Retention
import java.lang.reflect.Modifier

class AnnotationClosureTest extends CompilableTestSupport {
    def answer = new Object() {
        def answer() { 42 }
    }
    
    void testGep3InClosure() {
        shouldCompile """
            @interface Bar{Class value();}
            class Foo {
              @Bar({ sleep 1 })
              def baz() {}
            }
        """
    }

    void testAllowedAsValueForAnnotationElementOfTypeClass() {
        shouldCompile """
import gls.annotations.closures.AnnWithClassElement

@AnnWithClassElement(elem = { 1 + 2 })
class Foo {}
        """
    }

    // TODO: two compile errors instead of one, odd error message

    void testNotAllowedAsValueForAnnotationElementOfOtherType() {
        shouldNotCompile """
import gls.annotations.closures.AnnWithStringElement

@AnnWithStringElement(elem = { 1 + 2 })
class Foo {}
        """
    }

    void testIsCompiledToPublicClass() {
        def closureClass = ClassWithAnnClosure.getAnnotation(AnnWithClassElement).elem()
        assert Modifier.isPublic(closureClass.modifiers)
    }

    void testDefaultValueIsCompiledToPublicClass() {
        def closureClass = ClosureAsDefaultValue.getAnnotation(AnnWithDefaultValue).elem()
        assert Modifier.isPublic(closureClass.modifiers)
    }

    void testCanBeUsedAsDefaultValue() {
        def closureClass = ClosureAsDefaultValue.getAnnotation(AnnWithDefaultValue).elem()
        def closure = closureClass.newInstance(null, null)

        assert closure.call() == 3
    }

    void testCanBeNested() {
        def closureClass = NestedClosure.getAnnotation(AnnWithClassElement).elem()
        def closure = closureClass.newInstance(null, null)

        assert closure.call(9) == 9
    }

    void testWorksOnInnerClass() {
        def closureClass = ClassWithAnnClosure.InnerClassWithAnnClosure.getAnnotation(AnnWithClassElement).elem()
        def closure = closureClass.newInstance(null, null)

        assert closure.call() == 3
    }

    void testWorksOnNestedClass() {
        def closureClass = ClassWithAnnClosure.NestedClassWithAnnClosure.getAnnotation(AnnWithClassElement).elem()
        def closure = closureClass.newInstance(null, null)

        assert closure.call() == 3
    }

    void testWorksOnNestedAnnotation() {
        def closureClass = NestedAnnotation.getAnnotation(AnnWithNestedAnn).elem().elem()
        def closure = closureClass.newInstance(null, null)

        assert closure.call() == 3
    }

    void testWorksOnNestedAnnotationWithDefaultValue() {
        def closureClass = NestedAnnotationWithDefault.getAnnotation(AnnWithNestedAnnWithDefault).elem().elem()
        def closure = closureClass.newInstance(null, null)

        assert closure.call() == 3
    }

    void testMayContainGString() {
        def closureClass = ClosureWithGString.getAnnotation(AnnWithClassElement).elem()
        def closure = closureClass.newInstance(null, null)

        assert closure.call([1, 2, 3]) == "list has 3 elements"
    }

    void testDoesNoHarmOnAnnotationWithSourceRetention() {
        shouldCompile """
import java.lang.annotation.*

@Retention(RetentionPolicy.SOURCE)
@interface AnnWithSourceRetention {
    Class elem()
}

@AnnWithSourceRetention(elem = { 1 + 2 })
class Foo {}
        """
    }

    void testDoesNoHarmOnAnnotationWithClassRetention() {
        shouldCompile """
import java.lang.annotation.*

@Retention(RetentionPolicy.CLASS)
@interface AnnWithClassRetention {
    Class elem()
}

@AnnWithClassRetention(elem = { 1 + 2 })
class Foo {}
        """
    }
    
    @NotYetImplemented
    void testAnnotationOnAnonymousMethod() {
        shouldCompile """        
            import java.lang.annotation.*

            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            @interface Bar{Class value();}

            return new Object() {
              @Bar({})
              String toString() {}
            }
"""
    }
}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnWithClassElement {
    Class elem()
}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnWithStringElement {
    String elem()
}

@AnnWithClassElement(elem = { 1 + 2 })
class ClassWithAnnClosure {
    @AnnWithClassElement(elem = { 1 + 2 })
    class InnerClassWithAnnClosure {}

    @AnnWithClassElement(elem = { 1 + 2 })
    static class NestedClassWithAnnClosure {}
}

@JavaAnnotationWithClassElement(elem = { 1 + 2 })
class ClassWithJavaAnnClosure {}

@AnnWithClassElement(elem = { def nested = { it }; nested(it) })
class NestedClosure {}

@AnnWithDefaultValue
class ClosureAsDefaultValue {}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnWithDefaultValue {
    Class elem() default { 1 + 2 }
}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnWithNestedAnn {
    AnnWithClassElement elem()
}

@AnnWithNestedAnn(elem = @AnnWithClassElement(elem = { 1 + 2 }))
class NestedAnnotation {}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnWithNestedAnnWithDefault {
    AnnWithDefaultValue elem()
}

@AnnWithNestedAnnWithDefault(elem = @AnnWithDefaultValue())
class NestedAnnotationWithDefault {}

@AnnWithClassElement(elem = { list -> "list has ${list.size()} elements" })
class ClosureWithGString {}
