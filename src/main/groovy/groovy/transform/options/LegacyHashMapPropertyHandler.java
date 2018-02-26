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

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.TupleConstructorASTTransformation;

import java.util.HashMap;
import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.equalsNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.findArg;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifElseS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isOneX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isTrueX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * The {@code @Immutable} transformation in earlier versions of Groovy tried to be smart
 * in the case of an immutable class with a single HashMap property, the supplied Map constructor
 * tried to be compatible with both expected tuple behavior and expected named-argument behavior
 * by peeking into the supplied map and guessing as to which approach might be applicable.
 * Recent versions of Groovy now allow both {@code @TupleConstructor} and {@code @MapConstructor}
 * annotations to co-exist which provide's a more flexible solution to this problem. While more
 * flexible, the new approach isn't fully compatible with the previous approach. If for some
 * reason you need the old behavior, you can try this property handler. Some features of the
 * new approach won't be available to you.
 *
 * @since 2.5.0
 */
public class LegacyHashMapPropertyHandler extends ImmutablePropertyHandler {
    private static final ClassNode HMAP_TYPE = makeWithoutCaching(HashMap.class, false);

    private Statement createLegacyConstructorStatementMapSpecial(FieldNode fNode) {
        final Expression fieldExpr = varX(fNode);
        final ClassNode fieldType = fieldExpr.getType();
        final Expression initExpr = fNode.getInitialValueExpression();
        final Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression) initExpr).isNullExpression())) {
            assignInit = assignS(fieldExpr, ConstantExpression.EMPTY_EXPRESSION);
        } else {
            assignInit = assignS(fieldExpr, cloneCollectionExpr(initExpr, fieldType));
        }
        Expression namedArgs = findArg(fNode.getName());
        Expression baseArgs = varX("args");
        Statement assignStmt = ifElseS(
                equalsNullX(namedArgs),
                ifElseS(
                        isTrueX(callX(baseArgs, "containsKey", constX(fNode.getName()))),
                        assignS(fieldExpr, namedArgs),
                        assignS(fieldExpr, cloneCollectionExpr(baseArgs, fieldType))),
                ifElseS(
                        isOneX(callX(baseArgs, "size")),
                        assignS(fieldExpr, cloneCollectionExpr(namedArgs, fieldType)),
                        assignS(fieldExpr, cloneCollectionExpr(baseArgs, fieldType)))
        );
        return ifElseS(equalsNullX(baseArgs), assignInit, assignStmt);
    }

    @Override
    public boolean validateAttributes(AbstractASTTransformation xform, AnnotationNode anno) {
        return !(xform instanceof TupleConstructorASTTransformation) && super.validateAttributes(xform, anno);
    }

    @Override
    public boolean validateProperties(AbstractASTTransformation xform, BlockStatement body, ClassNode cNode, List<PropertyNode> props) {
        if (!(props.size() == 1 && props.get(0).getType().equals(HMAP_TYPE))) {
            xform.addError("Error during " + xform.getAnnotationName() + " processing. Property handler " + getClass().getName() + " only accepts a single HashMap property", props.size() == 1 ? props.get(0) : cNode);
            return false;
        }
        return true;
    }

    @Override
    public Statement createPropInit(AbstractASTTransformation xform, AnnotationNode anno, ClassNode cNode, PropertyNode pNode, Parameter namedArgsMap) {
        FieldNode fNode = pNode.getField();
        if (fNode.isFinal() && fNode.isStatic()) return null;
        if (fNode.isFinal() && fNode.getInitialExpression() != null) {
            return checkFinalArgNotOverridden(cNode, fNode);
        }
        return createLegacyConstructorStatementMapSpecial(fNode);
    }
}
