/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.ast.decompiled

import org.codehaus.groovy.ast.ClassNode

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * @author Peter Gromov
 */
interface Intf {}

class SuperClass {}

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER])
@interface Anno {
    String stringAttr() default ""
    SomeEnum enumAttr() default SomeEnum.FOO
    Class clsAttr() default Object
    boolean booleanAttr() default true
    int[] intArrayAttr() default []
    Class[] classArrayAttr() default []
    Anno[] annoArrayAttr() default []
}

enum SomeEnum {
    FOO, BAR
}

@SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
@Anno(
        stringAttr = "s",
        enumAttr = SomeEnum.BAR,
        intArrayAttr = [4, 2],
        clsAttr = String,
        classArrayAttr = [AsmDecompilerTestData],
        annoArrayAttr = [@Anno, @Anno(booleanAttr = false)]
)
class AsmDecompilerTestData extends SuperClass implements Intf {
    @Anno
    protected aField

    AsmDecompilerTestData(boolean b) {}

    @Anno
    ClassNode objectMethod() { null }

    void withParametersThrowing(@Anno int a, AsmDecompilerTestData[] b) throws IOException { }

    int[][] primitiveArrayMethod() { null }
}
