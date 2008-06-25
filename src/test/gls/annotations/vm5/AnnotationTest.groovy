/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gls.annotations.vm5

import gls.scope.CompilableTestSupport

/**
 * Tests various properties of annotation defintions.
 *
 * @author Jochen Theodorou
 */
class AnnotationTest extends CompilableTestSupport {

  void testPrimitiveDefault() {
    // NOTE: for int anything else than a plain number will fail.
    // For exmaple 1l will fail too, even if it could be converted.
    // If this should be changed, then further discussion is needed.
    shouldNotCompile """
@interface X {
  int x() default "1" // must be integer
}
    """

    shouldCompile """
@interface X {
  int x() default 1
}
    """
  }

  void testArrayDefault() {
    shouldNotCompile """
@interface X {
   String[] x() default "1" // must be list
}
    """

    shouldNotCompile """
@interface X {
   String[] x() default ["1",2] // list must contain elements of correct type
}
    """

    shouldCompile """
@interface X {
   String[] x() default ["a","b"]
}
    """
  }

  void testClassDefault() {
    shouldNotCompile """
@interface X {
   Class x() default "1" // must be list
}
    """

    shouldCompile """
@interface X {
   Class x() default Number.class // class with .class
}
    """

    shouldCompile """
@interface X {
   Class x() default Number
}
    """
  }

  void testEnumDefault() {
    shouldNotCompile """
@interface X {
   ElementType x() default "1" // must be Enum
}
    """

    shouldNotCompile """
@interface X {
   ElementType x() default Target.TYPE // must be Enum of correct type
}
    """

    shouldCompile """
import java.lang.annotation.*

@interface X {
   ElementType x() default ElementType.METHOD
}
    """
  }

  void testAnnotationDefault() {
    shouldNotCompile """
import java.lang.annotation.*
@interface X {
   Target x() default "1" // must be Annotation
}
    """

    shouldNotCompile """
import java.lang.annotation.*

@interface X {
   Target x() default @Retentention // must be correct type
}
    """

    shouldCompile """
import java.lang.annotation.*
@interface X {
   Target x() default @Target([ElementType.TYPE])
}
    """
  }

  void testSelfReference() {
    shouldNotCompile """
@interface X {
   X x() default @X // self reference must not work
}
    """
  }

  void testCyclicReference() {
    shouldNotCompile """
@interface X {
   Y x() default @Y
}

@interface Y {
   X x() default @X
}
    """
  }

  void testParameter() {
    shouldNotCompile """
@interface X {
   String x(int x) default "" // annotation members can't have parameters
}
    """
  }

  void testThrowsClause() {
    shouldNotCompile """
@interface X {
   String x() throws IOException // annotation members can't have exceptions
}
    """
  }

  void testExtends() {
    shouldNotCompile """
@interface X extends Serializable{}
// annotation members can't extend
    """
  }

  void testInvalidMemberType() {
    shouldNotCompile """
@interface X {
   Object x() // Object is no type for constants
}
    """
  }

  void testNull() {
    shouldNotCompile """
@interface X {
   String x() default null // null is no constant for annotations
}
    """
  }

  void testUsage() {
    assertScript """
import java.lang.annotation.*

// a random annnotation type
@Retention(RetentionPolicy.RUNTIME)
@interface MyAnnotation {
  String stringValue()
  int intValue()
  int defaultInt() default 1
  String defaultString() default ""
  Class defaultClass() default Integer.class
  ElementType defaultEnum() default ElementType.TYPE
  Target defaultAnnotation() default @Target([ElementType.TYPE])
}


@MyAnnotation(stringValue = "for class", intValue = 100)
class Foo {}

Annotation[] annotations = Foo.class.annotations
assert annotations.size() == 1
MyAnnotation my = annotations[0]
assert my.stringValue() == "for class"
assert my.intValue() == 100
assert my.defaultInt() == 1
assert my.defaultString() == ""
assert my.defaultClass() == Integer

assert my.defaultEnum() == ElementType.TYPE
assert my.defaultAnnotation() instanceof Target
    """
  }

  void testJavaAnnotationUsageWithGroovyKeyword() {
    assertScript """
package gls.annotations.vm5
import java.lang.annotation.*
@JavaAnnotation(in = 3)
class Foo {}

Annotation[] annotations = Foo.class.annotations
assert annotations.size() == 1
JavaAnnotation my = annotations[0]
assert my.in() == 3
    """
  }

  void testUsageOnClass() {
    assertScript """
import java.lang.annotation.*

@Deprecated
class Foo{}

assert Foo.class.annotations.size() == 1
  """
  }

  void testSingletonArrayUsage() {
    assertScript """
import java.lang.annotation.*

// a random annnotation type
@Retention(RetentionPolicy.RUNTIME)
@interface MyAnnotation {
  String[] things()
}

@MyAnnotation(things = "x")
class Foo {}

Annotation[] annotations = Foo.class.annotations
assert annotations.size() == 1
MyAnnotation my = annotations[0]
assert my.things().size() == 1
assert my.things()[0] == "x"
    """
  }
}
