/*
 * Copyright 2003-2007 the original author or authors.
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

package org.codehaus.groovy.vmplugin.v5;

import java.lang.reflect.*;
import java.lang.annotation.*;
import java.util.Map;
import java.util.HashMap;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.vmplugin.VMPlugin;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.ClassHelper;

/**
 * java 5 based functions
 * @author Jochen Theodorou
 *
 */
public class Java5 implements VMPlugin { 
    private static Class[] PLUGIN_DGM={PluginDefaultGroovyMethods.class};

    public void setAdditionalClassInformation(ClassNode cn) {
        setGenericsTypes(cn);
    }

    private void setGenericsTypes(ClassNode cn) {
        TypeVariable[] tvs = cn.getTypeClass().getTypeParameters();
        GenericsType[] gts = configureTypeVariable(tvs);
        cn.setGenericsTypes(gts);
    }
    
    private GenericsType[] configureTypeVariable(TypeVariable[] tvs) {
        if (tvs.length==0) return null;
        GenericsType[] gts = new GenericsType[tvs.length];
        for (int i = 0; i < tvs.length; i++) {
            gts[i] = configureTypeVariableDefintion(tvs[i]);
        }
        return gts;
    }

    private GenericsType configureTypeVariableDefintion(TypeVariable tv) {
       ClassNode base = configureTypeVariableReference(tv);
       Type[] tBounds = tv.getBounds();
       if (tBounds.length==0) return new GenericsType(base);
       ClassNode[] cBounds = new ClassNode[tBounds.length];
       for (int i = 0; i < tBounds.length; i++) {
           cBounds[i] = configureType(tBounds[i]);
       }
       GenericsType gt = new GenericsType(base,cBounds,null);
       gt.setPlaceholder(true);
       return gt;
    }
    
    private ClassNode configureType(Type type) {
        if (type instanceof WildcardType) {
            return configureWildcardType((WildcardType) type);
        } else if (type instanceof ParameterizedType) {
            return configureParameterizedType((ParameterizedType) type);
        } else if (type instanceof GenericArrayType) {
            throw new GroovyBugError("Not yet implemented");
        } else if (type instanceof TypeVariable) {
            return configureTypeVariableReference((TypeVariable) type);
        } else if (type instanceof Class) {
            return ClassHelper.makeWithoutCaching((Class) type, false);
        } else {
            throw new GroovyBugError("unknown type: " + type + " := " + type.getClass());
        }        
    }
    
    private ClassNode configureWildcardType(WildcardType wildcardType) {
        throw new GroovyBugError("Not yet implemented");
    }
    
    private ClassNode configureParameterizedType(ParameterizedType parameterizedType) {
        ClassNode base = configureType(parameterizedType.getRawType());
        GenericsType[] gts = configureTypeArguments(parameterizedType.getActualTypeArguments());
        base.setGenericsTypes(gts);
        return base;
    }
    
    private ClassNode configureTypeVariableReference(TypeVariable tv) {
        ClassNode cn = ClassHelper.makeWithoutCaching(tv.getName());
        cn.setGenericsPlaceHolder(true);
        ClassNode cn2 = ClassHelper.makeWithoutCaching(tv.getName());
        GenericsType[] gts = new GenericsType[]{new GenericsType(cn2)};
        cn.setGenericsTypes(gts);
        cn.setRedirect(ClassHelper.OBJECT_TYPE);
        return cn;
    }
    
    private GenericsType[] configureTypeArguments(Type[] ta) {
        if (ta.length==0) return null;
        GenericsType[] gts = new GenericsType[ta.length];
        for (int i = 0; i < ta.length; i++) {
            gts[i] = new GenericsType(configureType(ta[i]));
        }
        return gts;
    }

    public Class[] getPluginDefaultGroovyMethods() {
        return PLUGIN_DGM;
    }

    public void addPhaseOperations(CompilationUnit unit) {
        Map<Integer, ASTTransformationCodeVisitor> transformationVisitors = new HashMap<Integer, ASTTransformationCodeVisitor>();
        ASTTransformationCollectorCodeVisitor annotCollector = new ASTTransformationCollectorCodeVisitor(transformationVisitors);
        unit.addPhaseOperation(annotCollector.getOperation(), Phases.SEMANTIC_ANALYSIS);
        for (int i = Phases.SEMANTIC_ANALYSIS; i <= Phases.ALL; i++) {
            ASTTransformationCodeVisitor TransformationVisitor = new ASTTransformationCodeVisitor();
            transformationVisitors.put(Integer.valueOf(i), TransformationVisitor);
            unit.addPhaseOperation(TransformationVisitor.getOperation(), i);
        }
    }

    public void setAnnotationMetaData(ClassNode cn) {
        Annotation[] annotations =  cn.getTypeClass().getAnnotations();
        for (int i=0; i<annotations.length; i++) {
            Annotation annotation = annotations[i];
            Class type = annotation.annotationType();
            cn.addAnnotation(makeAnnotationNode(annotation));
        }
    }

    private AnnotationNode makeAnnotationNode(Annotation annotation) {
        AnnotationNode node = new AnnotationNode(ClassHelper.make(annotation.annotationType()));
        Class type = annotation.annotationType();
        if (type == Retention.class) {
            Retention r = (Retention) annotation;
            switch (r.value()) {
              case RUNTIME: node.setRuntimeRetention(true); break;
              case SOURCE:  node.setSourceRetention(true); break;
              case CLASS:   node.setClassRetention(true); break;
              default: throw new GroovyBugError("unsupported Retention "+r.value());
            }
        } else if (type == Target.class) {
            Target t = (Target) annotation;
            ElementType[] elements = t.value();
            int bitmap = 0;
            for (int i=0; i<elements.length; i++) {
                switch (elements[i]) {
                    case TYPE: bitmap |= AnnotationNode.TYPE_TARGET; break;
                    case CONSTRUCTOR: bitmap |= AnnotationNode.CONSTRUCTOR_TARGET; break;
                    case METHOD: bitmap |= AnnotationNode.METHOD_TARGET; break;
                    case FIELD: bitmap |= AnnotationNode.FIELD_TARGET; break;
                    case PARAMETER: bitmap |= AnnotationNode.PARAMETER_TARGET; break;
                    case LOCAL_VARIABLE: bitmap |= AnnotationNode.LOCAL_VARIABLE_TARGET; break;
                    case ANNOTATION_TYPE: bitmap |= AnnotationNode.ANNOTATION_TARGET; break;
                    case PACKAGE: bitmap |= AnnotationNode.PACKAGE_TARGET; break;
                    default: throw new GroovyBugError("unsupported Target "+elements[i]);
                }
            }
            node.setAllowedTargets(bitmap);
        }

        return node;
    }

    public void setMethodDefaultValue(MethodNode mn, Method m) {
        Object defaultValue = m.getDefaultValue();
        mn.setCode(new ExpressionStatement(new ConstantExpression(defaultValue)));
        mn.setAnnotationDefault(true);
    }

}
