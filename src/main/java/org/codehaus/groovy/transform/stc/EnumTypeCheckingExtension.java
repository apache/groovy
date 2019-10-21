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
package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.SwitchStatement;

import static org.codehaus.groovy.transform.stc.StaticTypesMarker.SWITCH_TYPE;

/**
 * A type checking extension that will take care of handling errors which are specific to enums. In particular, it will
 * handle the enum constants within switch-case statement.
 *
 * @since 3.0.0
 */
public class EnumTypeCheckingExtension extends TypeCheckingExtension {
    public EnumTypeCheckingExtension(StaticTypeCheckingVisitor staticTypeCheckingVisitor) {
        super(staticTypeCheckingVisitor);
    }

    @Override
    public boolean handleUnresolvedVariableExpression(VariableExpression vexp) {
        SwitchStatement switchStatement = this.typeCheckingVisitor.typeCheckingContext.getEnclosingSwitchStatement();

        if (null == switchStatement) return false;

        ClassNode type = switchStatement.getExpression().getType();

        if (null == type) return false;

        if (type.isEnum()) {
            FieldNode fieldNode = type.redirect().getField(vexp.getName());
            if (null != fieldNode && type.equals(fieldNode.getType())) {
                vexp.putNodeMetaData(SWITCH_TYPE, type);
                return true;
            }
        }

        return false;
    }
}
