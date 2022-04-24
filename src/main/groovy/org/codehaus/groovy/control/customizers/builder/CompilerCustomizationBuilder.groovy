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

package org.codehaus.groovy.control.customizers.builder

import groovy.transform.AutoFinal
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * <p>A builder which allows easy configuration of compilation customizers. Instead of creating
 * various compilation customizers by hand, you may use this builder instead, which provides a
 * shorter syntax and removes most of the verbosity.
 */
@AutoFinal @CompileStatic
class CompilerCustomizationBuilder extends FactoryBuilderSupport {

    static CompilerConfiguration withConfig(CompilerConfiguration config, @DelegatesTo(type='org.codehaus.groovy.control.customizers.builder.CompilerCustomizationBuilder') Closure spec) {
        config.invokeMethod('addCompilationCustomizers', new CompilerCustomizationBuilder().invokeMethod('customizers', spec))
        config
    }

    //--------------------------------------------------------------------------

    CompilerCustomizationBuilder() {
        registerFactory('customizers', new CustomizersFactory()) // root

        registerFactory('ast', new ASTTransformationCustomizerFactory())
        registerFactory('imports', new ImportCustomizerFactory())
        registerFactory('inline', new InlinedASTCustomizerFactory())
        registerFactory('secureAst', new SecureASTCustomizerFactory())
        registerFactory('source', new SourceAwareCustomizerFactory())
    }

    @Override
    protected Object postNodeCompletion(Object parent, Object node) {
        Object value = super.postNodeCompletion(parent, node)
        Object factory = getContextAttribute(CURRENT_FACTORY)
        if (factory instanceof PostCompletionFactory) {
            value = factory.postCompleteNode(this, parent, value)
            setParent(parent, value)
        }
        value
    }
}
