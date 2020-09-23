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
package groovy.transform.options;

import groovy.transform.stc.POJO;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.MapConstructorASTTransformation;

import java.util.List;

import static org.apache.groovy.ast.tools.ConstructorNodeUtils.checkPropNamesS;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.equalsNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

public class DefaultPropertyHandler extends PropertyHandler {
    private static final ClassNode POJO_TYPE = make(POJO.class);

    @Override
    public boolean validateAttributes(AbstractASTTransformation xform, AnnotationNode anno) {
        boolean success = true;
        //success |= isValidAttribute(xform, anno, "");
        return success;
    }

    @Override
    public boolean validateProperties(AbstractASTTransformation xform, BlockStatement body, ClassNode cNode, List<PropertyNode> props) {
        if (xform instanceof MapConstructorASTTransformation) {
            VariableExpression namedArgs = varX("args");
            body.addStatement(ifS(equalsNullX(namedArgs), assignS(namedArgs, new MapExpression())));
            boolean pojo = !cNode.getAnnotations(POJO_TYPE).isEmpty();
            body.addStatement(checkPropNamesS(namedArgs, pojo, props));
        }
        return super.validateProperties(xform, body, cNode, props);
    }

    @Override
    public Statement createPropInit(AbstractASTTransformation xform, AnnotationNode anno, ClassNode cNode, PropertyNode pNode, Parameter namedArgsMap) {
        String name = pNode.getName();
        FieldNode fNode = pNode.getField();
        boolean useSetters = xform.memberHasValue(anno, "useSetters", true);
        boolean hasSetter = cNode.getProperty(name) != null && !fNode.isFinal();
        if (namedArgsMap != null) {
            return assignFieldS(useSetters, namedArgsMap, name);
        } else {
            Expression var = varX(name);
            if (useSetters && hasSetter) {
                return setViaSetterS(name, var);
            } else {
                return assignToFieldS(name, var);
            }
        }
    }

    private static Statement assignToFieldS(String name, Expression var) {
        return assignS(propX(varX("this"), name), var);
    }

    private static Statement setViaSetterS(String name, Expression var) {
        return stmt(callThisX(getSetterName(name), var));
    }

    private static Statement assignFieldS(boolean useSetters, Parameter map, String name) {
        ArgumentListExpression nameArg = args(constX(name));
        MethodCallExpression var = callX(varX(map), "get", nameArg);
        var.setImplicitThis(false);
        MethodCallExpression containsKey = callX(varX(map), "containsKey", nameArg);
        containsKey.setImplicitThis(false);
        return ifS(containsKey, useSetters ?
                setViaSetterS(name, var) :
                assignToFieldS(name, var));
    }
}
