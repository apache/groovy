/*
 * Copyright 2003-2012 the original author or authors.
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
package org.codehaus.groovy.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.control.SourceUnit;

public class AnnotationCollectorTransform {

    public List<AnnotationNode> visit(AnnotationNode collector, AnnotationNode aliasAnnotation, AnnotatedNode origin, SourceUnit source) {
        Expression memberValue = collector.getMember("value");
        if (!(memberValue instanceof ListExpression)) return Collections.EMPTY_LIST;
        ListExpression memberListExp = (ListExpression) memberValue;
        List<Expression> memberList = memberListExp.getExpressions();
        if (memberList.size()==0) return Collections.EMPTY_LIST;
        ArrayList<AnnotationNode> ret = new ArrayList(memberList.size());
        for (Expression e: memberList) {
            if (!(e instanceof ClassExpression)) continue;
            AnnotationNode toAdd = new AnnotationNode(e.getType());
            toAdd.setSourcePosition(aliasAnnotation);
            ret.add(toAdd);
        }
        return ret;
    }

}
