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

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

/**
 * This factory generates an {@link ASTTransformationCustomizer ast transformation customizer}.
 * <p>
 * Simple syntax:
 * <pre>builder.ast(ToString)</pre>
 * With AST transformation options:
 * <pre>builder.ast(includeNames:true, ToString)</pre>
 *
 * @since 2.1.0
 */
class ASTTransformationCustomizerFactory extends AbstractFactory {

    @Override
    @CompileStatic
    public boolean isLeaf() {
        true
    }

    @Override
    @CompileStatic
    public boolean onHandleNodeAttributes(final FactoryBuilderSupport builder, final Object node, final Map attributes) {
        false
    }

    @Override
    public Object newInstance(final FactoryBuilderSupport builder, final Object name, final Object value, final Map attributes) throws InstantiationException, IllegalAccessException {
        ASTTransformationCustomizer customizer
        if (attributes) {
            customizer = new ASTTransformationCustomizer(attributes, value)
        } else {
            customizer = new ASTTransformationCustomizer(value)
        }
        customizer
    }
}
