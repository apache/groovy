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
package org.apache.groovy.ginq

import groovy.test.GroovyAssert
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.tools.GroovyClass

import java.lang.reflect.Field
import java.util.concurrent.atomic.AtomicInteger

/**
 * Runs one existing GINQ script in each real checking mode.
 * The script text is not copied: the same characters are compiled four times.
 * {@link Kind#DYNAMIC} is the default {@code GroovyShell}. The other modes put
 * the checking annotation on the script class before macro expansion, so
 * {@code GQ} / {@code GQL} and the surrounding script see that mode.
 */
@CompileStatic
final class GinqScriptModes {

    static enum Kind {
        DYNAMIC,
        COMPILE_DYNAMIC,
        TYPE_CHECKED,
        COMPILE_STATIC
    }

    private static final AtomicInteger NEXT_ID = new AtomicInteger()
    private static final EnumMap<Kind, AtomicInteger> EXECUTIONS = new EnumMap<>(Kind)
    static {
        for (Kind kind : Kind.values()) {
            EXECUTIONS.put(kind, new AtomicInteger())
        }
        Runtime.runtime.addShutdownHook {
            System.err.println(audit())
        }
    }

    private GinqScriptModes() {
    }

    /**
     * Compiles and runs {@code script} in every {@link Kind}.
     * A failure names the mode and leaves the original cause attached.
     *
     * @param script the script already used by a GINQ test
     */
    static void runAll(String script) {
        for (Kind kind : Kind.values()) {
            run(kind, script)
        }
    }

    /**
     * Runs the script in the two dynamic modes. Used when static checking
     * rejects the script for a reason the dynamic assertions do not have.
     *
     * @param script the script already used by a GINQ test
     */
    static void expectUnchecked(String script) {
        run(Kind.DYNAMIC, script)
        run(Kind.COMPILE_DYNAMIC, script)
    }

    /**
     * Compiles the script in the checked modes and requires each one to
     * reject it with every listed diagnostic. The modes still run.
     *
     * @param script the script already used by a GINQ test
     * @param diagnostics text that each checked failure must contain
     */
    static void expectCheckedRejection(String script, String... diagnostics) {
        for (Kind kind : [Kind.TYPE_CHECKED, Kind.COMPILE_STATIC]) {
            AssertionError error = null
            try {
                run(kind, script)
            } catch (AssertionError caught) {
                error = caught
            }
            if (error == null) {
                throw new AssertionError('[' + kind.name() + '] compiled a script that static checking rejects')
            }
            String message = error.message == null ? '' : error.message
            for (String diagnostic : diagnostics) {
                if (!message.contains(diagnostic)) {
                    throw error
                }
            }
        }
    }

    /**
     * @return how many scripts each mode has executed in this JVM
     */
    static String audit() {
        StringBuilder report = new StringBuilder('GINQ script modes:')
        for (Kind kind : Kind.values()) {
            report.append(' ').append(kind.name()).append('=').append(EXECUTIONS.get(kind).get())
        }
        return report.toString()
    }

    private static void run(Kind kind, String script) {
        EXECUTIONS.get(kind).incrementAndGet()
        try {
            if (kind == Kind.DYNAMIC) {
                GroovyAssert.assertScript(script)
            } else {
                compileAndRun(kind, script)
            }
        } catch (Throwable failure) {
            throw new AssertionError('[' + kind.name() + '] ' + messageOf(failure), failure)
        }
    }

    private static void compileAndRun(Kind kind, String script) {
        ClassLoader parent = Thread.currentThread().contextClassLoader
        if (parent == null) {
            parent = GinqScriptModes.classLoader
        }
        CompilerConfiguration configuration = new CompilerConfiguration()
        GroovyClassLoader loader = new GroovyClassLoader(parent, configuration)
        CompilationUnit unit = new CompilationUnit(configuration, null, loader)
        String className = 'GinqCase' + NEXT_ID.incrementAndGet()
        unit.addSource(className + '.groovy', script)
        applyModeBeforeMacros(unit, kind)
        unit.compile()

        Class<?> scriptClass = null
        for (GroovyClass generated : unit.classes) {
            Class<?> defined = loader.defineClass(generated.name, generated.bytes)
            if (className == generated.name) {
                scriptClass = defined
            }
        }
        if (scriptClass == null) {
            throw new IllegalStateException('no script class ' + className)
        }
        Script instance = (Script) scriptClass.getDeclaredConstructor().newInstance()
        instance.run()
    }

    /**
     * The macro transform is registered when the compilation unit is constructed,
     * after {@code source.convert()}. The checking annotation has to exist before
     * that transform lowers {@code GQ}, so it is inserted between those two operations.
     */
    private static void applyModeBeforeMacros(CompilationUnit unit, Kind kind) {
        Field field = CompilationUnit.getDeclaredField('phaseOperations')
        field.accessible = true
        Deque[] operations = (Deque[]) field.get(unit)
        Deque conversion = operations[Phases.CONVERSION]
        List existing = new ArrayList(conversion)
        conversion.clear()
        conversion.add(existing.get(0))
        conversion.add((CompilationUnit.ISourceUnitOperation) { SourceUnit source ->
            for (ClassNode type : source.AST.classes) {
                if (type.script) {
                    type.addAnnotation(modeAnnotation(kind))
                }
            }
        })
        for (int i = 1; i < existing.size(); i++) {
            conversion.add(existing.get(i))
        }
    }

    private static AnnotationNode modeAnnotation(Kind kind) {
        Class<?> annotationType
        if (kind == Kind.TYPE_CHECKED) {
            annotationType = TypeChecked
        } else if (kind == Kind.COMPILE_DYNAMIC) {
            annotationType = CompileDynamic
        } else {
            annotationType = CompileStatic
        }
        return new AnnotationNode(ClassHelper.make(annotationType))
    }

    private static String messageOf(Throwable failure) {
        String message = failure.message
        if (message == null || message.isEmpty()) {
            return failure.getClass().name
        }
        return message
    }
}
