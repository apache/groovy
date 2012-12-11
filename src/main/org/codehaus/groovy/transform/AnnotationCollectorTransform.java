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

import groovy.transform.AnnotationCollector;

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

/**
 * This class is the base for any annotation alias processor. 
 * @see AnnotationCollector
 * @see AnnotationCollectorTransform#visit(AnnotationNode, AnnotationNode, AnnotatedNode, SourceUnit)
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class AnnotationCollectorTransform {

    /**
     * Adds a new syntax error to the source unit and then continues.
     * 
     * @param message   the message
     * @param node      the node for the error report
     * @param source    the source unit for the error report
     */
    protected void addError(String message, ASTNode node, SourceUnit source) {
        source.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(new SyntaxException(
                message,  node.getLineNumber(), node.getColumnNumber(), node.getLastLineNumber(), node.getLastColumnNumber()
                ), source));
    }

    /**
     * Returns a list of Expressions for the value attribute of the given 
     * AnnotationNode. Should the node value not be a ListExpression of String 
     * ConstantExpression, errors will be added to the source unit.
     * 
     * @param collector     the node containing the value member with the list
     * @param source        the source unit for error reporting
     * @return              a list of string constants
     */
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

    /**
     * Implementation method of the alias annotation processor. This method will 
     * get the list of annotations we aliased from collector and adds it to
     * aliasAnnotationUsage. The method will also map all members from 
     * aliasAnnotationUsage to the aliased nodes. Should a member stay unmapped,
     * we will ad an error. Further processing of those members is done by the
     * annotations.
     * 
     * @param collector                 reference to the annotation with {@link AnnotationCollector}
     * @param aliasAnnotationUsage      reference to the place of usage of the alias
     * @param aliasAnnotated            reference to the node that has been annotated by the alias
     * @param source                    source unit for error reporting
     * @return list of the new AnnotationNodes
     */
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
