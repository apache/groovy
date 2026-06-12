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
package groovy.junit6.plugin

import groovy.transform.CompileStatic
import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtensionContext

import java.lang.reflect.AnnotatedElement

/**
 * JUnit {@link ExecutionCondition} that evaluates Groovy closures from
 * {@link GroovyEnabledIf} and {@link GroovyDisabledIf} annotations.
 *
 * @since 6.0.0
 */
@CompileStatic
class GroovyConditionExtension implements ExecutionCondition {

    /**
     * Evaluates the Groovy condition annotations attached to the current JUnit element.
     *
     * @param context the JUnit extension context for the current element
     * @return the enablement decision for the element
     */
    @Override
    ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        AnnotatedElement element = context.element.orElse(null)
        if (element == null) {
            return ConditionEvaluationResult.enabled('No element to evaluate')
        }

        GroovyEnabledIf enabledIf = element.getAnnotation(GroovyEnabledIf)
        if (enabledIf != null) {
            return evaluateEnabledIf(enabledIf, context)
        }

        GroovyDisabledIf disabledIf = element.getAnnotation(GroovyDisabledIf)
        if (disabledIf != null) {
            return evaluateDisabledIf(disabledIf, context)
        }

        ConditionEvaluationResult.enabled('No Groovy condition annotation found')
    }

    private static ConditionEvaluationResult evaluateEnabledIf(GroovyEnabledIf annotation, ExtensionContext context) {
        boolean result = evaluateClosure(annotation.value(), context)
        if (result) {
            return ConditionEvaluationResult.enabled(
                    annotation.reason() ?: 'Groovy condition evaluated to true')
        }
        ConditionEvaluationResult.disabled(
                annotation.reason() ?: 'Groovy condition evaluated to false')
    }

    private static ConditionEvaluationResult evaluateDisabledIf(GroovyDisabledIf annotation, ExtensionContext context) {
        boolean result = evaluateClosure(annotation.value(), context)
        if (result) {
            return ConditionEvaluationResult.disabled(
                    annotation.reason() ?: 'Groovy condition evaluated to true')
        }
        ConditionEvaluationResult.enabled(
                annotation.reason() ?: 'Groovy condition evaluated to false')
    }

    private static boolean evaluateClosure(Class closureClass, ExtensionContext context) {
        def delegate = new ConditionEvaluationContext(context)
        Closure closure = (Closure) closureClass.getConstructor(Object, Object)
                .newInstance(null, null)
        closure.delegate = delegate
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call() as boolean
    }
}
