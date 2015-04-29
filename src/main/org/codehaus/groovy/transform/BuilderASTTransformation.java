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
package org.codehaus.groovy.transform;

import groovy.lang.GroovyClassLoader;
import groovy.transform.CompilationUnitAware;
import groovy.transform.builder.Builder;
import groovy.transform.builder.DefaultStrategy;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.List;

import static groovy.transform.Undefined.isUndefined;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstancePropertyFields;

/**
 * Handles generation of code for the {@link Builder} annotation.
 *
 * @author Marcin Grzejszczak
 * @author Paul King
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class BuilderASTTransformation extends AbstractASTTransformation implements CompilationUnitAware {

    private static final Class MY_CLASS = Builder.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    public static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    public static final ClassNode[] NO_EXCEPTIONS = ClassNode.EMPTY_ARRAY;
    public static final Parameter[] NO_PARAMS = Parameter.EMPTY_ARRAY;

    private CompilationUnit compilationUnit;

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode || parent instanceof MethodNode) {
            if (parent instanceof ClassNode && !checkNotInterface((ClassNode) parent, MY_TYPE_NAME)) return;
            if (parent instanceof MethodNode && !checkStatic((MethodNode) parent, MY_TYPE_NAME)) return;
            final GroovyClassLoader classLoader = compilationUnit != null ? compilationUnit.getTransformLoader() : source.getClassLoader();
            final BuilderStrategy strategy = createBuilderStrategy(anno, classLoader);
            if (strategy == null) return;
            strategy.build(this, parent, anno);
        }
    }

    public interface BuilderStrategy {
        void build(BuilderASTTransformation transform, AnnotatedNode annotatedNode, AnnotationNode anno);
    }

    public abstract static class AbstractBuilderStrategy implements BuilderStrategy {
        protected static List<PropertyInfo> getPropertyInfoFromClassNode(ClassNode cNode, List<String> includes, List<String> excludes) {
            List<PropertyInfo> props = new ArrayList<PropertyInfo>();
            for (FieldNode fNode : getInstancePropertyFields(cNode)) {
                if (shouldSkip(fNode.getName(), excludes, includes)) continue;
                props.add(new PropertyInfo(fNode.getName(), fNode.getType()));
            }
            return props;
        }

        protected String getSetterName(String prefix, String fieldName) {
            return prefix.isEmpty() ? fieldName : prefix + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        }

        protected boolean unsupportedAttribute(BuilderASTTransformation transform, AnnotationNode anno, String memberName) {
            return unsupportedAttribute(transform, anno, memberName, "");
        }

        protected boolean unsupportedAttribute(BuilderASTTransformation transform, AnnotationNode anno, String memberName, String extraMessage) {
            Object memberValue = transform.getMemberValue(anno, memberName);
            if (memberValue != null && memberValue instanceof String && isUndefined((String) memberValue)) return false;
            if (memberValue == null) {
                memberValue = transform.getMemberClassValue(anno, memberName);
                if (memberValue != null && isUndefined((ClassNode) memberValue)) {
                    memberValue = null;
                }
            }
            if (memberValue != null) {
                String message = extraMessage.length() == 0 ? "" : " " + extraMessage;
                transform.addError("Error during " + MY_TYPE_NAME + " processing: Annotation attribute '" + memberName + "' not supported by " + getClass().getName() + message, anno);
                return true;
            }
            return false;
        }

        protected void checkKnownProperty(BuilderASTTransformation transform, AnnotationNode anno, String name, List<PropertyInfo> properties) {
            for (PropertyInfo prop: properties) {
                if (name.equals(prop.getName())) {
                    return;
                }
            }
            transform.addError("Error during " + MY_TYPE_NAME + " processing: tried to include unknown property '" + name + "'", anno);
        }

        protected void checkKnownField(BuilderASTTransformation transform, AnnotationNode anno, String name, List<FieldNode> fields) {
            for (FieldNode field: fields) {
                if (name.equals(field.getName())) {
                    return;
                }
            }
            transform.addError("Error during " + MY_TYPE_NAME + " processing: tried to include unknown property '" + name + "'", anno);
        }

        protected boolean getIncludeExclude(BuilderASTTransformation transform, AnnotationNode anno, ClassNode cNode, List<String> excludes, List<String> includes) {
            List<String> directExcludes = transform.getMemberList(anno, "excludes");
            if (directExcludes != null) excludes.addAll(directExcludes);
            List<String> directIncludes = transform.getMemberList(anno, "includes");
            if (directIncludes != null) includes.addAll(directIncludes);
            if (includes.isEmpty() && excludes.isEmpty()) {
                if (transform.hasAnnotation(cNode, CanonicalASTTransformation.MY_TYPE)) {
                    AnnotationNode canonical = cNode.getAnnotations(CanonicalASTTransformation.MY_TYPE).get(0);
                    if (excludes.isEmpty()) {
                        List<String>  canonicalExcludes = transform.getMemberList(canonical, "excludes");
                        if (canonicalExcludes != null) excludes.addAll(canonicalExcludes);
                    }
                    if (includes.isEmpty()) {
                        List<String>  canonicalIncludes = transform.getMemberList(canonical, "includes");
                        if (canonicalIncludes != null) includes.addAll(canonicalIncludes);
                    }
                }
            }
            return transform.checkIncludeExclude(anno, excludes, includes, MY_TYPE_NAME);
        }

        protected static class PropertyInfo {
            public PropertyInfo(String name, ClassNode type) {
                this.name = name;
                this.type = type;
            }

            private String name;
            private ClassNode type;

            public String getName() {
                return name;
            }

            public ClassNode getType() {
                return type;
            }

            public void setName(String name) {
                this.name = name;
            }

            public void setType(ClassNode type) {
                this.type = type;
            }
        }
    }

    private boolean checkStatic(MethodNode mNode, String annotationName) {
        if (!mNode.isStatic() && !mNode.isStaticConstructor() && !(mNode instanceof ConstructorNode)) {
            addError("Error processing method '" + mNode.getName() + "'. " +
                    annotationName + " not allowed for instance methods.", mNode);
            return false;
        }
        return true;
    }

    private BuilderStrategy createBuilderStrategy(AnnotationNode anno, GroovyClassLoader loader) {
        ClassNode strategyClass = getMemberClassValue(anno, "builderStrategy", ClassHelper.make(DefaultStrategy.class));

        if (strategyClass == null) {
            addError("Couldn't determine builderStrategy class", anno);
            return null;
        }

        String className = strategyClass.getName();
        try {
            Object instance = loader.loadClass(className).newInstance();
            if (instance == null) {
                addError("Can't load builderStrategy '" + className + "'", anno);
                return null;
            }
            if (!BuilderStrategy.class.isAssignableFrom(instance.getClass())) {
                addError("The builderStrategy class '" + strategyClass.getName() + "' on " + MY_TYPE_NAME + " is not a builderStrategy", anno);
                return null;
            }

            return (BuilderStrategy) instance;
        } catch (Exception e) {
            addError("Can't load builderStrategy '" + className + "' " + e, anno);
            return null;
        }
    }

    public void setCompilationUnit(final CompilationUnit unit) {
        this.compilationUnit = unit;
    }
}
