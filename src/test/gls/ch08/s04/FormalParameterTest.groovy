/*
 * Copyright 2003-2010 the original author or authors.
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
package gls.ch08.s04

import gls.CompilableTestSupport

/**
 * a formal parameter is a parameter to a method, this parameter must work
 * as any local variable. But we generally do boxing on local variables, which
 * is not possible for formal parameters. The type is given through the
 * method signature.
 */
class FormalParameterTest extends CompilableTestSupport {

    void testPrimitiveParameterAssignment() {
        // test int and long as they have different lengths on in the bytecode
        assert intMethod(1i, 2i) == 2i
        assert longMethod(1l, 2l) == 2l

    }

    int intMethod(int i, int j) {
        i = j
        return i
    }

    long longMethod(long i, long j) {
        i = j
        return i
    }

    /**
     * Chapter 8:    Classes
     * Section 8.4:  Method Declarations
     * Author:       Ken Barclay
     *
     * File:         arity.method.declaration.classes.8.4.groovy
     *
     * A class declaration may include any number of method declarations including
     *   abstract method declarations.
     *
     * A method is given a name and an optional list of formal parameter declarations
     *   enclosed in parentheses ( and ). A parameter declaration at its simplest is
     *   simply a parameter name. It may be prefixed with a combination of optional
     *   parameter modifiers (def or final), a type, or a type followed by the varargs
     *   symbol (...). Two formal parameters with the same name is disallowed.
     *
     * A formal parameter may be optionally initialized with an expression, referred
     *   to as a default parameter. If the number of actual parameters is fewer than
     *   the number of formal parameters, then each actual is used to initialize, in
     *   order, the non-default formal parameters. When all the actual parameters are
     *   used in this manner, all subsequent formal parameters require default values.
     *
     * If the last formal parameter is a variable arity parameter, it is considered
     *   to define a method that is referred to as a variable arity method. Invocations
     *   of a variable arity method may contain more actual argument expressions than
     *   formal parameters. All the actual argument expressions that do not correspond
     *   to the formal parameters preceding the variable arity parameter will be evaluated
     *   and the results stored into an array that will be passed to the method invocation.
     */
    def dump(age, String... names) {
        names.collect { name ->
            "name: $name age: $age"
        }
    }

    void testVariableArity() {
        def l1 = dump(22, 'Ken', 'Barclay')
        def l2 = ["name: Ken age: 22", "name: Barclay age: 22"]
        l1.eachWithIndex { it, i ->
            assert it == l2[i]
        }
    }

}