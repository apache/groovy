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
package org.apache.groovy.contracts.ast.visitor;

import org.apache.groovy.contracts.generation.Configurator;
import org.apache.groovy.contracts.util.Validate;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.objectweb.asm.Opcodes;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;

/**
 * Makes some initialization in order to use the {@link Configurator} for determining
 * which assertions in what packages will be executed.
 *
 * @see Configurator
 */
public class ConfigurationSetup {

    /**
     * Adds an instance field which allows to control whether GContract assertions
     * are enabled or not. Before assertions are evaluated this field will be checked.
     *
     * @param type the current {@link ClassNode}
     * @see Configurator
     */
    public void init(final ClassNode type) {
        Validate.notNull(type);
        StaticMethodCallExpression checkAssertionsEnabledMethodCall = callX(ClassHelper.makeWithoutCaching(Configurator.class), "checkAssertionsEnabled", args(constX(type.getName())));
        final FieldNode fieldNode = type.addField(BaseVisitor.GCONTRACTS_ENABLED_VAR, Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_FINAL, ClassHelper.boolean_TYPE, checkAssertionsEnabledMethodCall);
        fieldNode.setSynthetic(true);
    }
}
