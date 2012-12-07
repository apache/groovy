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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

public class AnnotationCollectorTransform {

    protected void addError(String message, ASTNode node, SourceUnit source) {
        source.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(new SyntaxException(
                message,  node.getLineNumber(), node.getColumnNumber(), node.getLastLineNumber(), node.getLastColumnNumber()
                ), source));
    }
    
    protected List<Expression> getTargetAnnotationList(AnnotationNode collector, SourceUnit source) {
        Expression memberValue = collector.getMember("value");
        if (!(memberValue instanceof ListExpression)) {
            addError("Annotation collector expected a list of classes, but got a "+memberValue.getClass(), collector, source);
            return Collections.EMPTY_LIST;
        }
        ListExpression memberListExp = (ListExpression) memberValue;
        List<Expression> memberList = memberListExp.getExpressions();
        if (memberList.size()==0) return Collections.EMPTY_LIST;
        return memberList;
    }
    
    public List<AnnotationNode> visit(AnnotationNode collector, AnnotationNode aliasAnnotationUsage, AnnotatedNode aliasAnnotated, SourceUnit source) {
        List<Expression> targetAnnotationList = getTargetAnnotationList(collector, source);
        ArrayList<AnnotationNode> ret = new ArrayList(targetAnnotationList.size());
        Set<String> unusedNames = new HashSet(aliasAnnotationUsage.getMembers().keySet());
        
        for (Expression e: targetAnnotationList) {
            if (!(e instanceof ClassExpression)) {
                addError("Annotation collector expected a ClassExpression, but got "+e.getClass(), collector, source);
                continue;
            }
            AnnotationNode toAdd = new AnnotationNode(e.getType());
            toAdd.setSourcePosition(aliasAnnotationUsage);
            ret.add(toAdd);

            for (String name : aliasAnnotationUsage.getMembers().keySet()) {
                if (e.getType().hasMethod(name, Parameter.EMPTY_ARRAY)) {
                    unusedNames.remove(name);
                    toAdd.addMember(name, aliasAnnotationUsage.getMember(name));
                }
            }
        }

        if (unusedNames.size()>0) {
            String message = "Annotation collector got unmapped names "+unusedNames.toString()+".";
            addError(message, aliasAnnotationUsage, source);
        }

        return ret;
    }
}
