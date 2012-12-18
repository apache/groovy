/*
* Copyright 2003-2012 the original author or authors.
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
package groovy.transform

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.control.SourceUnit

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy;

class AnnotationCollectorTest extends GroovyTestCase {

    static class MyProcessor extends org.codehaus.groovy.transform.AnnotationCollectorTransform {
        public List<AnnotationNode> visit(AnnotationNode collector, AnnotationNode aliasAnnotationUsage, AnnotatedNode aliasAnnotated, SourceUnit source) {
            def excludes = aliasAnnotationUsage.getMember("excludes")
            if (excludes) {
                addError("use myex instead of excludes", aliasAnnotationUsage, source)
                return []
            }
            def myex = aliasAnnotationUsage.getMembers().remove("myex")
            if (myex) aliasAnnotationUsage.addMember("excludes",myex);
            return super.visit(collector, aliasAnnotationUsage, aliasAnnotated, source)
        }
    }

    public void assertScript(String script) {
        GroovyShell shell = new GroovyShell(this.class.classLoader)
        shell.evaluate(script, getTestClassName())
    }

    public void shouldNotCompile(String script, Closure failureAction) {
        GroovyShell shell = new GroovyShell(this.class.classLoader)
        try {
            shell.parse(script, getTestClassName())
            assert false
        } catch (org.codehaus.groovy.control.MultipleCompilationErrorsException mce) {
            failureAction(mce)
        }
    }

    void testSimpleUsage() {
        assertScript """
            @groovy.transform.PreCompiledAlias
            class Foo {
                Integer a, b
            }
            assert Foo.class.annotations.size()==3 
            assert new Foo(1,2).toString() == "Foo(1, 2)"
        """

        assertScript """
            import groovy.transform.*
            @AnnotationCollector([ToString, EqualsAndHashCode, Immutable])
            @interface NotPreCompiledAlias {}

            @NotPreCompiledAlias
            class Foo {
                Integer a, b
            }
            assert Foo.class.annotations.size()==3
            assert new Foo(1,2).toString() == "Foo(1, 2)"
        """
    }

    void testUsageWithArgument() {
        assertScript """
            import groovy.transform.*

            @PreCompiledAlias(excludes=["a"])
            class Foo {
                Integer a, b
            }
            assert Foo.class.annotations.size()==3 
            assert new Foo(1,2).toString() == "Foo(2)"
        """

        assertScript """
            import groovy.transform.*
            @AnnotationCollector([ToString, EqualsAndHashCode, Immutable])
            @interface NotPreCompiledAlias {}

            @NotPreCompiledAlias(excludes=["a"])
            class Foo {
                Integer a, b
            }
            assert Foo.class.annotations.size()==3 
            assert new Foo(1,2).toString() == "Foo(2)"
        """
    }
    
    void testClosureAnnotation() {
        assertScript """
            import groovy.transform.*
            @AnnotationCollector([ConditionalInterrupt])
            @interface NotPreCompiledAlias {}

            @NotPreCompiledAlias(applyToAllClasses=false, value={ counter++> 10})
            class X {
                def counter = 0
                def method() {
                  4.times {null}
                 }
            }
            def x = new X(counter:20)
            try {
                x.method()
                assert false
            } catch (InterruptedException ie)  {
                assert true
            }
        """
        assertScript """
            import groovy.transform.*
    
            @OtherPreCompiledAlias(applyToAllClasses=false, value={ counter++> 10})
            class X {
                def counter = 0
                def method() {
                  4.times {null}
                 }
            }
            def x = new X(counter:20)
            try {
                x.method()
                assert false
            } catch (InterruptedException ie)  {
                assert true
            }
        """
    }

    void testAST() {
        assertScript """
            import groovy.transform.*
            @AnnotationCollector([ToString, EqualsAndHashCode, Immutable])
            @interface Alias {}

            @Alias(excludes=["a"])
            @ASTTest(phase=org.codehaus.groovy.control.CompilePhase.INSTRUCTION_SELECTION, value={
                def annotations = node.annotations
                assert annotations.size() == 4 //ASTTest + 3
                annotations.each {
                    assert it.lineNumber == 6 || it.classNode.name.contains("ASTTest")
                }
            })
            class Foo {
                Integer a, b
            }
            assert Foo.class.annotations.size() == 4
            assert new Foo(1,2).toString() == "Foo(2)"
        """
    }
    
    void testConflictingAnnotations() {
        shouldNotCompile """
            import groovy.transform.*
            @interface ConflictingA {String foo()}
            @interface ConflictingB {int foo()}

            @AnnotationCollector([ConflictingA, ConflictingB])
            @interface Alias {}

            @Alias(foo="1") class X{}
        """, { exception ->
            exception.message.contains("line 9, column 24")
            exception.message.contains("Attribute 'foo' should have type 'java.lang.Integer'")
        }
    }

    void testCustomProcessor() {
        assertScript """
            import groovy.transform.*
            @AnnotationCollector(value=[ToString, EqualsAndHashCode, Immutable], processor='groovy.transform.AnnotationCollectorTest\$MyProcessor')
            @interface Alias {}

            @Alias(myex=["a"])
            class Foo {
                Integer a, b
            }
            assert Foo.class.annotations.size()==3 
            assert new Foo(1,2).toString() == "Foo(2)"
        """
    }
    
    void testProcessorThrowingCustomMessage() {
        shouldNotCompile """
            import groovy.transform.*
            @AnnotationCollector(value=[ToString, EqualsAndHashCode, Immutable], processor='groovy.transform.AnnotationCollectorTest\$MyProcessor')
            @interface Alias {}

            @Alias(excludes=["a"])
            class Foo {
                Integer a, b
            }
            assert Foo.class.annotations.size()==3 
            assert new Foo(1,2).toString() == "Foo(2)"
        """, { ex ->
            assert ex.message.contains("use myex instead of excludes @ line 6, column 13")
        }
    }
    
    void testWrongProcessorName() {
        shouldNotCompile """
            import groovy.transform.*
            @AnnotationCollector(value=[ToString, EqualsAndHashCode, Immutable], processor='MyProcessor')
            @interface Alias {}

            @Alias(excludes=["a"])
            class Foo {
                Integer a, b
            }
            assert Foo.class.annotations.size()==3 
            assert new Foo(1,2).toString() == "Foo(2)"
        """, { ex ->
            assert ex.message.contains("Could not find class for Transformation Processor MyProcessor declared by Alias")
        }
    }

    void testAnnotationOnAnnotation() {
        assertScript """
            import groovy.transform.*

            @PreCompiledAlias3
            class Foo {
                Integer a, b
            }
            assert Foo.class.annotations.size()==2
            assert new Foo(1,2).toString() == "Foo(2)"
        """

        assertScript """
            import groovy.transform.*
            @Immutable
            @ToString(excludes=["a"])
            @AnnotationCollector()
            class Alias {}

            @Alias
            class Foo {
                Integer a, b
            }
            assert Foo.class.annotations.size()==2
            assert new Foo(1,2).toString() == "Foo(2)"
        """
    }

    void testAnnotationTakingAnnotationParams() {
        assertScript """
            import groovy.transform.*

            @TheSuperGroovyHeroes
            class Team {}

            assert Team.class.annotations.size() == 1
            assert Team.class.annotations[0] instanceof GroovyCoreTeam
            assert Team.class.annotations[0].value().size() == 4
            assert Team.class.annotations[0].value().collect { it.value() } == ['Paul', 'Cedric', 'Jochen', 'Guillaume']
        """

        assertScript """
            import groovy.transform.*

            @GroovyCoreTeam([
                @GroovyDeveloper('Paul'),
                @GroovyDeveloper('Cedric'),
                @GroovyDeveloper('Jochen'),
                @GroovyDeveloper('Guillaume')
            ])
            @AnnotationCollector
            @interface SuperHeroes {}

            @SuperHeroes
            class Team {}

            assert Team.class.annotations.size() == 1
            assert Team.class.annotations[0] instanceof GroovyCoreTeam
            assert Team.class.annotations[0].value().size() == 4
            assert Team.class.annotations[0].value().collect { it.value() } == ['Paul', 'Cedric', 'Jochen', 'Guillaume']

        """
    }
}

@AnnotationCollector([ToString, EqualsAndHashCode, Immutable])
@interface PreCompiledAlias {}

@AnnotationCollector([ConditionalInterrupt])
@interface OtherPreCompiledAlias {}

@Immutable
@ToString(excludes=["a"])
@AnnotationCollector()
class PreCompiledAlias3 {}

@Retention(RetentionPolicy.RUNTIME)
@interface GroovyCoreTeam {
    GroovyDeveloper[] value()
}

@Retention(RetentionPolicy.RUNTIME)
@interface GroovyDeveloper {
    String value() default "";
}

@GroovyCoreTeam([
    @GroovyDeveloper('Paul'),
    @GroovyDeveloper('Cedric'),
    @GroovyDeveloper('Jochen'),
    @GroovyDeveloper('Guillaume')
])
@AnnotationCollector
@interface TheSuperGroovyHeroes {}
