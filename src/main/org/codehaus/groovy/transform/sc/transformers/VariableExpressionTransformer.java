/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform.sc.transformers;

import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

/**
 * Transformer for VariableExpression the bytecode backend wouldn't be able to
 * handle otherwise. 
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class VariableExpressionTransformer {

    public Expression transformVariableExpression(VariableExpression expr) {
        // we need to transform variable expressions that go to a delegate
        // to a property expression, as ACG would loose the information
        // in processClassVariable before it reaches any makeCall, that could
        // handle it
        Object val = expr.getNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER);
        if (val==null) return expr;
        PropertyExpression pexp = new PropertyExpression(new VariableExpression("this"), expr.getName());
        pexp.copyNodeMetaData(expr);
        pexp.setImplicitThis(true);
        return pexp;
    }
}
