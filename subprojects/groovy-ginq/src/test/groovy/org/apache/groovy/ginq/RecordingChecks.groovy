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

import org.codehaus.groovy.ast.expr.MethodCall
import org.codehaus.groovy.transform.stc.AbstractTypeCheckingExtension
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor

/**
 * Records method names the type checker asks an extension about.
 * A query compiled as one dynamic closure would not reach this hook.
 */
class RecordingChecks extends AbstractTypeCheckingExtension {

    static final List<String> NAMES = []

    RecordingChecks(StaticTypeCheckingVisitor typeCheckingVisitor) {
        super(typeCheckingVisitor)
    }

    @Override
    boolean beforeMethodCall(MethodCall call) {
        String name = call.methodAsString
        if (name != null) {
            NAMES.add(name)
        }
        return false
    }
}
