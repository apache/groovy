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
package groovy

import gls.CompilableTestSupport
import static java.lang.Boolean.FALSE as F
import static java.text.DateFormat.MEDIUM as M
import static java.util.regex.Pattern.*
import static java.text.DateFormat.MEDIUM
import static junit.framework.Assert.format
import static junit.framework.Assert.assertEquals
import static groovy.StaticImportTarget.x
import static groovy.StaticImportTarget.z // do not remove
import static groovy.StaticImportTarget.getCl
import static java.lang.Math.*
import static java.util.Calendar.getInstance as now
import static groovy.API.*
import static groovy.StaticImportChild.*
import static groovy.bugs.Groovy4145.foo4145
import static groovy.Outer1.*
import static groovy.Outer2.Inner2
import static groovy.Outer2.Inner2 as InnerAlias2
import static java.util.jar.Attributes.*
import static java.util.jar.Attributes.Name as AttrName
// TODO GROOVY-4287: reinstate next two imports
//import static Outer3.*
//import static Outer4.Inner4
import static groovy.Container5087.*
import org.codehaus.groovy.runtime.DefaultGroovyMethods as DGM

class StaticImportTest extends CompilableTestSupport {
    void testFieldWithAliasInExpression() {
        assert !F
    }

    void testMethodAndField() {
        assert cos(2 * PI) == 1.0
    }

    static myStaticMethod() {
        cos(2 * PI)
    }

    void testMethodAndFieldInStaticContext() {
        assert myStaticMethod() == 1.0
    }

    void testMethodAndFieldInClosure() {
        def closure = { cos(2 * PI) }
        assert closure() == 1.0
    }

    void testFieldAsObjectExpression() {
        assert PI.equals(Math.PI)
    }

    void testFieldAsArgumentList() {
        assert ("" + PI.toString()).contains('3.14')
    }

    void testFieldAliasing() {
        assert MEDIUM == M
    }

    void testMethodAliasing() {
        // GROOVY-1809 making this not possible on one line?
        def now = now().time
        assert now.class == Date
    }

    void testWildCardAliasing() {
        assert MULTILINE == java.util.regex.Pattern.MULTILINE
    }

    private format(a, b, c, ignored) { format(a, b, c) }

    void testMethodDefCanUseStaticallyImportedMethodWithSameNameButDiffArgs() {
        assert format("different", "abc", "aBc", 3) == 'different expected:<abc> but was:<aBc>'
    }

    void testAssertEqualsFromJUnit() {
        double[] values = [3.9999, 4.0001, 0.00021, 0.00019]
        assertEquals(values[0], values[1], values[2])
        shouldFail(junit.framework.AssertionFailedError) {
            assertEquals(values[0], values[1], values[3])
        }
    }

    void testStaticImportFromGroovy() {
        def nonstaticval = new StaticImportTarget().y("he", 3)
        def staticval = x("he", 3)
        assert nonstaticval == staticval
    }

    void testStaticImportWithVarArgs() {
        assert noArrayMethod("one", 1) == 'noArrayMethod(one, 1)'
        assert API.arrayMethod("two", 1, 2, 3) == 'arrayMethod(two, 1, 2, 3)'
        assert arrayMethod("three", 1, 2, 3) == 'arrayMethod(three, 1, 2, 3)'
    }

    void testStaticImportFromParentClass() {
        assert cmethod() == 'hello from child'
        assert pmethod() == 'hello from parent'
        assert cfield == 21
        assert pfield == 42
    }
    
    void testStaticImportAndDefaultValue() {
      assertScript """
        import static Foo.*
        import static Bar.*
        
        class Bar {
          static void bar() { 
            assert foo(10,1000) == 1010 
            assert foo(10) == 110
          }
        }
        
        class Foo {
          static int foo(int x, int y = 100) {x+y}
        }
        
        Bar.bar()
      """  
    }

    void testStaticImportProperty() {
        def sources = [
            "class Foo { static x = 'foo'" + " }",
            "class Foo { static x = 'foo'" + "; static getX() { x + '_get' } }",
            "class Foo { static x = 'foo'" + ";                               static void setX(newx) { x = newx + '_set' } }",
            "class Foo { static x = 'foo'" + "; static getX() { x + '_get' }; static void setX(newx) { x = newx + '_set' } }"
        ]
        def imports = [
            "import static Foo.*",
            "import static Foo.getX; import static Foo.setX"
        ]
        def results = [
            "assert x == 'foo';     x = 'bar'; assert getX() == 'bar';         setX('baz'); assert 'baz'         == x",
            "assert x == 'foo_get'; x = 'bar'; assert getX() == 'bar_get';     setX('baz'); assert 'baz_get'     == x",
            "assert x == 'foo';     x = 'bar'; assert getX() == 'bar_set';     setX('baz'); assert 'baz_set'     == x",
            "assert x == 'foo_get'; x = 'bar'; assert getX() == 'bar_set_get'; setX('baz'); assert 'baz_set_get' == x"
        ]
        [0..<sources.size(), 0..<imports.size()].combinations().each { i, j ->
            assertScript sources[i] + "\n" + imports[j] + "\n" + results[i]
        }
    }

    void testStaticImportPropertyBooleanAlternative() {
        def sources = [
            "class Foo { static x = null" + "; static boolean isX() { x } }",
            "class Foo { static x = null" + "; static boolean isX() { x }; static void setX(newx) { x = newx } }"
        ]
        def imports = [
            "import static Foo.*",
            "import static Foo.x",
            "import static Foo.isX; import static Foo.setX"
        ]
        def results = [
            "assert !x; x = true ; assert  isX(); setX(false); assert !x",
            "assert !x; x = false; assert !isX(); setX(true);  assert  x"
        ]
        [0..<sources.size(), 0..<imports.size()].combinations().each { i, j ->
            assertScript sources[i] + "\n" + imports[j] + "\n" + results[i]
        }
    }

    void testStaticImportPropertyWithPublicField() {
        def sources = [
            "class Foo { public static x = 'foo'" + " }",
            "class Foo { public static x = 'foo'" + "; static getX() { x + '_get' } }",
            "class Foo { public static x = 'foo'" + ";                               static void setX(newx) { x = newx + '_set' } }",
            "class Foo { public static x = 'foo'" + "; static getX() { x + '_get' }; static void setX(newx) { x = newx + '_set' } }"
        ]
        def imports = [
            "import static Foo.*",
            "import static Foo.x"
        ]
        def results = [
            "assert x == 'foo';     x = 'bar'; assert      x == 'bar';         x = 'baz'  ; assert 'baz'         == x",
            "assert x == 'foo_get'; x = 'bar'; assert getX() == 'bar_get';     x = 'baz'  ; assert 'baz_get'     == x",
            "assert x == 'foo';     x = 'bar'; assert      x == 'bar_set';     setX('baz'); assert 'baz_set'     == x",
            "assert x == 'foo_get'; x = 'bar'; assert getX() == 'bar_set_get'; setX('baz'); assert 'baz_set_get' == x"
        ]
        [0..<sources.size(), 0..<imports.size()].combinations().each { i, j ->
            assertScript sources[i] + "\n" + imports[j] + "\n" + results[i]
        }
        assertScript sources[3] + """
            import static Foo.getX; import static Foo.setX
            assert getX() == 'foo_get'; setX('bar'); assert getX() == 'bar_set_get'; setX('baz'); assert 'baz_set_get' == getX()
        """
    }

    void testStaticImportPropertyWithAliases() {
        def sources = [
            "class Foo { static x = 'foo'" + " }",
            "class Foo { static x = 'foo'" + "; static getX() { x + '_get' } }",
            "class Foo { static x = 'foo'" + ";                               static void setX(newx) { x = newx + '_set' } }",
            "class Foo { static x = 'foo'" + "; static getX() { x + '_get' }; static void setX(newx) { x = newx + '_set' } }"
        ]
        def imports = [
            "import static Foo.x as z",
            "import static Foo.getX as getZ; import static Foo.setX as setZ"
        ]
        def results = [
            "assert z == 'foo';     z = 'bar'; assert getZ() == 'bar';         setZ('baz'); assert 'baz'         == z",
            "assert z == 'foo_get'; z = 'bar'; assert getZ() == 'bar_get';     setZ('baz'); assert 'baz_get'     == z",
            "assert z == 'foo';     z = 'bar'; assert getZ() == 'bar_set';     setZ('baz'); assert 'baz_set'     == z",
            "assert z == 'foo_get'; z = 'bar'; assert getZ() == 'bar_set_get'; setZ('baz'); assert 'baz_set_get' == z"
        ]
        [0..<sources.size(), 0..<imports.size()].combinations().each { i, j ->
            assertScript sources[i] + "\n" + imports[j] + "\n" + results[i]
        }
    }

    void testConstructorArgsAliasing() {
        // not recommended style to use statics in constructors but supported
        assertScript """
        class Foo {
            static x
        }
        import static Foo.x as z
        new Foo(z:'hi')
        assert z == 'hi'
        """
    }

    void testMethodCallWithThisTargetIsNotResolvedToStaticallyImportedMethod() {
        // not using shouldFail() to avoid closure scope
        try {
            this.z()
            fail()
        } catch (MissingMethodException expected) {}
    }

    void testMethodCallWithSuperTargetIsNotResolvedToStaticallyImportedMethod() {
        // not using shouldFail() to avoid closure scope
        try {
            super.z()
            fail()
        } catch (MissingMethodException expected) {}
    }

    // GROOVY-3945
    void testStaticImportOfAClosureProperty() {
        assert cl() == 'StaticImportTarget#static closure called'
    }

    // GROOVY-4145
    void testStaticPropertyImportedImplementedAsGetter() {
        assert foo4145 == 3
    }

    void testMethodCallExpressionInStaticContextWithInstanceVariableShouldFail() { //GROOVY-4228
        shouldNotCompile '''
            class B {
                def c = new Object()
                static main(args) {
                    c.foo()
                }
            }
        '''
    }

    void testStaticStarImportOfStaticInnerClass() {
        assert Inner1.class.name == 'groovy.Outer1$Inner1'
    }

    void testStaticImportOfStaticInnerClass() {
        assert Inner2.class.name == 'groovy.Outer2$Inner2'
    }

    void testStaticImportOfStaticInnerClassWithAlias() {
        assert InnerAlias2.class.name == 'groovy.Outer2$Inner2'
    }

    void testStaticImportOfStaticInnerClassExistingExternalJar() {
        assert Name.class.name == 'java.util.jar.Attributes$Name'
    }

    void testStaticImportOfStaticInnerClassExistingExternalJarWithAlias() {
        assert AttrName.class.name == 'java.util.jar.Attributes$Name'
    }

    void testExplicitStaticMethodCallHasPrecedenceOverStaticImport() {
        Bar4964.run()
    }

    // TODO GROOVY-4287: reinstate next two tests
//    void testStaticStarImportOfStaticInnerClassExternalClass() {
//        assert Inner3.class.name == 'Outer3$Inner3'
//    }
//
//    void testStaticImportOfStaticInnerClassExternalClass() {
//        assert Inner4.class.name == 'Outer4$Inner4'
//    }

    void testMapIndexInLeftExpressionOfEquals() {
        holder = 'foo'
        def map = [:]
        map[holder] = 'bar'
        assert map.containsKey('foo')
        assert map.foo == 'bar'
    }

    void testConstructorParamInLeftExpressionOfEquals() {
        holder = [:]
        new HolderWrapper(holder).foo = 'baz'
        assert holder.foo == 'baz'
    }

    void testMethodParamInLeftExpressionOfEquals() {
        holder = [[a:1, b:2], [c:3]]
        DGM.find(holder) { it.size() == 2 }.a = 4
        assert holder[0].a == 4
        dgmFind(holder) { it.size() == 1 }.c = 7
        assert holder[1].c == 7
    }

    private <T> T dgmFind(Collection<T> col, Closure clos) {
        DGM.find(col, clos)
    }
}

class API {
    static noArrayMethod(String s, int value) {
        "noArrayMethod(${s}, ${value})"
    }

    static arrayMethod(String s, int[] values) {
        "arrayMethod(${s}, " + values.toList().join(", ") + ")"
    }
}

class StaticImportParent {
  static pfield = 42
  static pmethod() { 'hello from parent' }
}

class StaticImportChild extends StaticImportParent {
  static cfield = 21
  static cmethod() { 'hello from child' }
}

class Outer1 {
    static class Inner1 {}
}

class Outer2 {
    static class Inner2 {}
}

class Foo4964 {
    static doIt() { [k: 'foo'] }
}

import static Foo4964.*
class Bar4964 {
    static doIt() { [k: 'bar'] }

    /*
     The following `run` method is invoked by `testExplicitStaticMethodCallHasPrecedenceOverStaticImport`
     As the method name shows, "ExplicitStaticMethodCall Has Precedence Over StaticImport", but the original test code does not conform to the intention
     */
    static run() {
//        The original test code is commented as follows:
//        assert doIt().k == 'foo'
//        assert doIt() == [k: 'foo']

        assert doIt().k == 'bar'
        assert doIt() == [k: 'bar']

        assert Bar4964.doIt() == [k: 'bar']
        assert Bar4964.doIt().k == 'bar'
    }
}

class Container5087 {
    static holder
}

class HolderWrapper {
    def holder

    HolderWrapper(holder) {
        this.holder = holder
    }

    void setProperty(String name, Object value) {
        holder[name] = value
    }
}
