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
package org.codehaus.groovy.transform.traitx

import org.junit.jupiter.api.Test

/**
 * Member access in trait code: which accessor or field a property name or an
 * accessor call refers to, for every form of reference, in a method body, a
 * closure and a lambda, in dynamic, type-checked and statically compiled code.
 * <p>
 * The executable companion to "Member access in trait code" in the traits
 * guide. {@code RULE} gives what that section specifies for each cell.
 * {@code OPEN} lists the cells where the implementation is known to do
 * something else, with what it does; the tests assert the rule everywhere
 * else and the recorded behaviour for the open cells. An open cell that
 * starts to follow the rule fails its test: remove it from {@code OPEN}.
 * <p>
 * In every script the trait declares a member {@code label} that yields
 * {@code 'trait'} and the implementing class declares {@code getLabel()}
 * that yields {@code 'class'}.
 */
final class TraitMemberAccessMatrix {

    private static final List<String> MODES = ['', '@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic']
    private static final List<String> MODE_NAMES = ['dynamic', '@TypeChecked', '@CompileStatic']

    /** The forms of reference; the first four are in a method body. */
    private static final Map<String, String> FORMS = [
        'label'              : 'label',
        'this.label'         : 'this.label',
        'getLabel()'         : 'getLabel()',
        'this.getLabel()'    : 'this.getLabel()',
        'closure label'      : '({ -> label })()',
        'closure this.label' : '({ -> this.label })()',
        'closure getLabel()' : '({ -> getLabel() })()',
        'lambda label'       : '((java.util.function.Supplier<String>) (() -> label)).get()',
        'lambda this.label'  : '((java.util.function.Supplier<String>) (() -> this.label)).get()',
        'lambda getLabel()'  : '((java.util.function.Supplier<String>) (() -> getLabel())).get()',
    ]

    private static final List<String> CLOSURE_NAME_FORMS = ['closure label', 'closure this.label', 'lambda label', 'lambda this.label']
    private static final List<String> BODY_FORMS = ['label', 'this.label', 'getLabel()', 'this.getLabel()']

    private static Map<String, String> rule(Closure<String> expected) {
        FORMS.keySet().collectEntries { [it, expected(it)] }
    }

    //--------------------------------------------------------------------------
    // instance members

    @Test // instance rules 1 and 2
    void instanceAccessorOnlyProperty() {
        check "String getLabel() { 'trait' }", "String getLabel() { 'class' }", false,
            rule { 'class' },
            // a statically compiled lambda in a trait method does not get the trait instance
            ['lambda getLabel()': [null, null, 'ClassCastException']]
    }

    @Test // instance rules 1, 3, 4 and 5
    void declaredInstanceProperty() {
        check "String label = 'trait'", "String getLabel() { 'class' }", false,
            rule { it in ['label', 'this.label', 'lambda label', 'lambda this.label'] ? 'trait' : 'class' },
            ['lambda getLabel()': [null, null, 'ClassCastException']]
    }

    @Test // as above, when the implementing class redeclares the property
    void declaredInstancePropertyRedeclaredByClass() {
        check "String label = 'trait'", "String label = 'class'", false,
            rule { it in ['label', 'this.label', 'lambda label', 'lambda this.label'] ? 'trait' : 'class' },
            ['lambda getLabel()': [null, null, 'ClassCastException']]
    }

    //--------------------------------------------------------------------------
    // static members

    @Test // static rules 2 and 4
    void staticAccessorOnlyProperty() {
        check "static String getLabel() { 'trait' }", "static String getLabel() { 'class' }", true,
            rule { 'trait' },
            // a name in a closure or lambda is resolved by the closure, whose owner is the implementing class
            CLOSURE_NAME_FORMS.collectEntries { [it, ['class', 'class', null]] }
    }

    @Test // static rules 3 and 4
    void virtualStaticAccessorOnlyProperty() {
        check "@groovy.transform.Virtual static String getLabel() { 'trait' }", "static String getLabel() { 'class' }", true,
            rule { it in BODY_FORMS ? 'class' : 'trait' },
            CLOSURE_NAME_FORMS.collectEntries { [it, ['class', 'class', 'class']] }
    }

    @Test // static rules 3 and 4, for an accessor that reads trait state
    void virtualStaticAccessorBackedByPrivateField() {
        check "private static String value = 'trait'; @groovy.transform.Virtual static String getLabel() { value }", "static String getLabel() { 'class' }", true,
            rule { it in BODY_FORMS ? 'class' : 'trait' },
            CLOSURE_NAME_FORMS.collectEntries { [it, ['class', 'class', 'class']] }
    }

    @Test // static rule 5
    void declaredStaticProperty() {
        check "static String label = 'trait'", "static String getLabel() { 'class' }", true,
            rule { 'trait' }, [:]
    }

    //--------------------------------------------------------------------------

    private static void check(String traitMember, String classMember, boolean isStatic, Map<String, String> rule, Map<String, List<String>> open) {
        List<String> problems = []
        FORMS.each { form, expression ->
            MODES.eachWithIndex { mode, i ->
                String script = isStatic ? """
                    $mode trait T {
                        $traitMember
                        static String result() { $expression }
                    }
                    class C implements T { $classMember }
                    C.result()
                """ : """
                    $mode trait T {
                        $traitMember
                        String result() { $expression }
                    }
                    class C implements T { $classMember }
                    new C().result()
                """
                String actual
                try {
                    actual = new GroovyShell().evaluate(script)
                } catch (Throwable t) {
                    actual = t.class.simpleName
                }
                String recorded = open[form]?.get(i)
                if (recorded == null) {
                    if (actual != rule[form]) {
                        problems << "$form, ${MODE_NAMES[i]}: the rule gives '${rule[form]}' but got '$actual'".toString()
                    }
                } else if (actual == rule[form]) {
                    problems << "$form, ${MODE_NAMES[i]}: now follows the rule ('$actual'); remove it from the open cells".toString()
                } else if (actual != recorded) {
                    problems << "$form, ${MODE_NAMES[i]}: open cell recorded as '$recorded' but got '$actual' (the rule gives '${rule[form]}')".toString()
                }
            }
        }
        assert problems.empty : problems.join('\n')
    }
}
