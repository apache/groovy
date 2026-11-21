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

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.transform.stc.AbstractTypeCheckingExtension
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor
import org.codehaus.groovy.transform.stc.StaticTypesMarker

/**
 * Replaces {@code choose(String)} with {@code choose(CharSequence)} so a query
 * clause can be checked against a target another extension already selected.
 */
class ChooseChecks extends AbstractTypeCheckingExtension {

    ChooseChecks(StaticTypeCheckingVisitor typeCheckingVisitor) {
        super(typeCheckingVisitor)
    }

    @Override
    void onMethodSelection(Expression expression, MethodNode method) {
        if (method.name != 'choose' || method.parameters.length != 1) {
            return
        }
        if (method.parameters[0].type.name != ClassHelper.STRING_TYPE.name) {
            return
        }
        MethodNode sequence = method.declaringClass.getDeclaredMethods('choose').find { MethodNode candidate ->
            candidate.parameters.length == 1 && candidate.parameters[0].type.name == 'java.lang.CharSequence'
        }
        if (sequence != null) {
            expression.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, sequence)
        }
    }
}
