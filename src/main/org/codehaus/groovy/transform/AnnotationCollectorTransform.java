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

import java.lang.reflect.Method;
import java.util.*;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import static org.objectweb.asm.Opcodes.*;

/**
 * This class is the base for any annotation alias processor. 
 * @see AnnotationCollector
 * @see AnnotationCollectorTransform#visit(AnnotationNode, AnnotationNode, AnnotatedNode, SourceUnit)
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public class AnnotationCollectorTransform {

    private static List<AnnotationNode> getMeta(ClassNode cn) {
        List<AnnotationNode> meta = cn.getNodeMetaData(AnnotationCollector.class);
        if (meta == null) {
            if (cn.isPrimaryClassNode()) {
                meta = getTargetListFromAnnotations(cn);
            } else {
                meta = getTargetListFromClass(cn);
            }
            cn.setNodeMetaData(AnnotationCollector.class, meta);
        }
        return meta;
    }

    /**
     * Class used by {@link CompilationUnit} to transform the alias class
     * into what is needed by the compiler. This means removing invalid
     * modifiers, interfaces and superclasses, as well as adding a static
     * value method returning our serialized version of the data for processing
     * from a precompiled state. By doing this the old annotations will be
     * removed as well 
     * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
     */
    public static class ClassChanger {
        
        /**
         * Method to transform the given ClassNode, if it is annotated with 
         * {@link AnnotationCollector}. See class description for what the
         * transformation includes.
         */
        public void transformClass(ClassNode cn) {
            AnnotationNode collector = null;
            for (ListIterator<AnnotationNode> it = cn.getAnnotations().listIterator(); it.hasNext();) {
                AnnotationNode an = it.next();
                if (an.getClassNode().getName().equals(AnnotationCollector.class.getName())) {
                    collector = an;
                    break;
                };
            }
            if (collector==null) return;
            
            // force final class, remove interface, annotation, enum and abstract modifiers
            cn.setModifiers((ACC_FINAL+cn.getModifiers()) & ~(ACC_ENUM|ACC_INTERFACE|ACC_ANNOTATION|ACC_ABSTRACT));
            // force Object super class
            cn.setSuperClass(ClassHelper.OBJECT_TYPE);
            // force no interfaces implemented
            cn.setInterfaces(ClassNode.EMPTY_ARRAY);

            // add static value():Object[][] method
            List<AnnotationNode> meta = getMeta(cn); 
            List<Expression> outer = new ArrayList<Expression>(meta.size());
            for (AnnotationNode an : meta) {
                Expression serialized = serialize(an);
                outer.add(serialized);
            }

            ArrayExpression ae = new ArrayExpression(ClassHelper.OBJECT_TYPE.makeArray(), outer);
            Statement code = new ReturnStatement(ae);
            cn.addMethod(   "value", ACC_PUBLIC+ACC_STATIC,
                            ClassHelper.OBJECT_TYPE.makeArray().makeArray(), 
                            Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY,
                            code);

            // remove annotations
            for (ListIterator<AnnotationNode> it = cn.getAnnotations().listIterator(); it.hasNext();) {
                AnnotationNode an = it.next();
                if (an==collector) continue;
                it.remove();
            }
        }

        private Expression serialize(Expression e) {
            if (e instanceof AnnotationConstantExpression) {
                AnnotationConstantExpression ace = (AnnotationConstantExpression) e;
                return serialize((AnnotationNode) ace.getValue());
            } else if (e instanceof ListExpression) {
                boolean annotationConstant = false;
                ListExpression le = (ListExpression) e;
                List<Expression> list = le.getExpressions();
                List<Expression> newList = new ArrayList<Expression>(list.size());
                for (Expression exp: list) {
                    annotationConstant = annotationConstant || exp instanceof AnnotationConstantExpression;
                    newList.add(serialize(exp));
                }
                ClassNode type = ClassHelper.OBJECT_TYPE;
                if (annotationConstant) type = type.makeArray();
                return new ArrayExpression(type, newList);
            }
            return e;
        }

        private Expression serialize(AnnotationNode an) {
            MapExpression map = new MapExpression();
            for (String key : an.getMembers().keySet()) {
                map.addMapEntryExpression(new ConstantExpression(key), serialize(an.getMember(key)));
            }
            List<Expression> l = new ArrayList<Expression>(2);
            l.add(new ClassExpression(an.getClassNode()));
            l.add(map);
            ArrayExpression ae = new ArrayExpression(ClassHelper.OBJECT_TYPE, l);
            return ae;
        }
    }
    
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

    private List<AnnotationNode> getTargetListFromValue(AnnotationNode collector, AnnotationNode aliasAnnotationUsage, SourceUnit source) {
        Expression memberValue = collector.getMember("value");
        if (memberValue == null) return Collections.EMPTY_LIST;
        if (!(memberValue instanceof ListExpression)) {
            addError("Annotation collector expected a list of classes, but got a "+memberValue.getClass(), collector, source);
            return Collections.EMPTY_LIST;
        }
        ListExpression memberListExp = (ListExpression) memberValue;
        List<Expression> memberList = memberListExp.getExpressions();
        if (memberList.size()==0) return Collections.EMPTY_LIST;
        ArrayList<AnnotationNode> ret = new ArrayList<AnnotationNode>();
        for (Expression e : memberList) {
            AnnotationNode toAdd = new AnnotationNode(e.getType());
            toAdd.setSourcePosition(aliasAnnotationUsage);
            ret.add(toAdd);
        }
        return ret;
    }

    private List<AnnotationNode> getStoredTargetList(AnnotationNode aliasAnnotationUsage, SourceUnit source) {
        ClassNode alias = aliasAnnotationUsage.getClassNode().redirect();
        List<AnnotationNode> ret = getMeta(alias);
        return copy(ret, aliasAnnotationUsage);
    }

    private List<AnnotationNode> copy(List<AnnotationNode> orig, AnnotationNode aliasAnnotationUsage) {
        if (orig.isEmpty()) return orig;
        List<AnnotationNode> ret = new ArrayList<AnnotationNode>(orig.size());
        for (AnnotationNode an : orig) {
            AnnotationNode newAn = new AnnotationNode(an.getClassNode());
            newAn.getMembers().putAll(an.getMembers());
            newAn.setSourcePosition(aliasAnnotationUsage);
            ret.add(newAn);
        }
        return ret;
    }

    private static List<AnnotationNode> getTargetListFromAnnotations(ClassNode alias) {
        List<AnnotationNode> annotations = alias.getAnnotations();
        if (annotations.size() < 2) return Collections.EMPTY_LIST;
        
        ArrayList<AnnotationNode> ret = new ArrayList<AnnotationNode>(annotations.size());
        for (AnnotationNode an : annotations) {
            ClassNode type = an.getClassNode();
            if (type.getName().equals(AnnotationCollector.class.getName())) continue;
            AnnotationNode toAdd = new AnnotationNode(type);
            toAdd.getMembers().putAll(an.getMembers());
            ret.add(toAdd);
        }
        return ret;
    }

    private static List<AnnotationNode> getTargetListFromClass(ClassNode alias) {
        Class<?> c = alias.getTypeClass();
        Object[][] data;
        try {
            Method m = c.getMethod("value");
            data = (Object[][]) m.invoke(null);
        } catch (Exception e) {
            throw new GroovyBugError(e);
        }
        return makeListOfAnnotations(data);
    }
    
    private static List<AnnotationNode> makeListOfAnnotations(Object[][] data) {
        if (data.length==0) return Collections.EMPTY_LIST;

        ArrayList<AnnotationNode> ret = new ArrayList<AnnotationNode>(data.length);
        for (Object[] inner : data) {
            Class anno = (Class) inner[0];
            AnnotationNode toAdd = new AnnotationNode(ClassHelper.make(anno));
            ret.add(toAdd);

            @SuppressWarnings("unchecked")
            Map<String,Object> member = (Map<String, Object>) inner[1];
            if (member.size()==0) continue;
            Map<String, Expression> generated = new HashMap<String, Expression>(member.size());
            for (String name : member.keySet()) {
                Object val = member.get(name);
                generated.put(name, makeExpression(val));
            }
            toAdd.getMembers().putAll(generated);
        }
        return ret;
    }
    
    private static Expression makeExpression(Object o) {
        if (o instanceof Class) return new ClassExpression(ClassHelper.make((Class) o));
        //TODO: value as Annotation here!
        if (o instanceof Object[][]) {
            List<AnnotationNode> annotations = makeListOfAnnotations((Object[][])o);
            ListExpression le = new ListExpression();
            for (AnnotationNode an : annotations) {
                le.addExpression(new AnnotationConstantExpression(an));
            }
            return le;
        } else if (o instanceof Object[]) {
            ListExpression le = new ListExpression();
            Object[] values = (Object[]) o;
            for (Object val : values) {
                le.addExpression(makeExpression(val));
            }
            return le;
        }
        return new ConstantExpression(o,true);
    }
    
    /**
     * Returns a list of AnnotationNodes for the value attribute of the given 
     * AnnotationNode. 
     * 
     * @param collector     the node containing the value member with the list
     * @param source        the source unit for error reporting
     * @return              a list of string constants
     */
    protected List<AnnotationNode> getTargetAnnotationList(AnnotationNode collector, AnnotationNode aliasAnnotationUsage, SourceUnit source) {
        List<AnnotationNode> stored     = getStoredTargetList(aliasAnnotationUsage, source);
        List<AnnotationNode> targetList = getTargetListFromValue(collector, aliasAnnotationUsage, source);
        int size = targetList.size()+stored.size();
        if (size==0) return Collections.EMPTY_LIST;
        ArrayList<AnnotationNode> ret = new ArrayList<AnnotationNode>(size);
        ret.addAll(stored);
        ret.addAll(targetList);

        return ret;
    }

    /**
     * Implementation method of the alias annotation processor. This method will 
     * get the list of annotations we aliased from the collector and adds it to
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
        List<AnnotationNode> ret =  getTargetAnnotationList(collector, aliasAnnotationUsage, source);
        Set<String> unusedNames = new HashSet<String>(aliasAnnotationUsage.getMembers().keySet());
        
        for (AnnotationNode an: ret) {
            for (String name : aliasAnnotationUsage.getMembers().keySet()) {
                if (an.getClassNode().hasMethod(name, Parameter.EMPTY_ARRAY)) {
                    unusedNames.remove(name);
                    an.setMember(name, aliasAnnotationUsage.getMember(name));
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
