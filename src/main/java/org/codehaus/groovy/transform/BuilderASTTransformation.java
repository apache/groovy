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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.tools.BeanUtils;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import static groovy.transform.Undefined.isUndefined;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstancePropertyFields;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSuperPropertyFields;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;

/**
 * Handles generation of code for the {@link Builder} annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class BuilderASTTransformation extends AbstractASTTransformation implements CompilationUnitAware, TransformWithPriority {

    private static final Class<?> MY_CLASS = Builder.class;
    private static final ClassNode MY_TYPE = make(MY_CLASS);
    /**
     * String representation of the @Builder annotation for error reporting.
     */
    public static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    /**
     * Empty array of ClassNode for use in method signatures.
     */
    public static final ClassNode[] NO_EXCEPTIONS = ClassNode.EMPTY_ARRAY;
    /**
     * Empty array of Parameter for use in method signatures.
     */
    public static final Parameter[] NO_PARAMS = Parameter.EMPTY_ARRAY;

    @Override
    public int priority() {
        return -5;
    }

    private CompilationUnit compilationUnit;

    @Override
    public void setCompilationUnit(final CompilationUnit unit) {
        this.compilationUnit = unit;
    }

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode || parent instanceof MethodNode) {
            if (parent instanceof ClassNode cn) {
                if (!checkNotInterface(cn, MY_TYPE_NAME)) {
                    return;
                }
                if (hasAnnotation(cn, RecordTypeASTTransformation.MY_TYPE)) {
                    // we'll later create a tuple constructor and move the builder annotation
                    // to it but let's create a mock constructor node for now
                    int size = cn.getProperties().size();
                    Parameter[] params = new Parameter[size];
                    List<PropertyNode> properties = cn.getProperties();
                    for (int i = 0; i < size; i++) {
                        PropertyNode pn = properties.get(i);
                        params[i] = param(pn.getType(), pn.getName());
                    }
                    parent = new ConstructorNode(0, params, null, null);
                    parent.setNodeMetaData("PSEUDO_CONSTRUCTOR", Boolean.TRUE);
                    parent.setDeclaringClass(cn);
                }
            }
            if (parent instanceof MethodNode && !checkStatic((MethodNode) parent, MY_TYPE_NAME)) return;
            final GroovyClassLoader classLoader = compilationUnit != null ? compilationUnit.getTransformLoader() : source.getClassLoader();
            final BuilderStrategy strategy = createBuilderStrategy(anno, classLoader);
            if (strategy == null) return;

            // GEP-21 Shape C: discard any stubber-emitted placeholders before
            // the strategy runs — covers the inner builder class added to the
            // module, the placeholder methods on it, and the static factory
            // on the buildee. Use removeMethod() rather than removeIf() on
            // method lists, since ClassNode keeps a parallel name->methods map.
            if (parent instanceof ClassNode buildee) {
                buildee.getModule().getClasses().removeIf(StubberSupport::isStub);
                List<MethodNode> stubs = new ArrayList<>();
                for (MethodNode m : buildee.getMethods()) {
                    if (StubberSupport.isStub(m)) stubs.add(m);
                }
                stubs.forEach(buildee::removeMethod);
            }

            strategy.build(this, parent, anno);
        }
    }

    /**
     * Strategy interface for builder code generation.
     */
    public interface BuilderStrategy {
        /**
         * Generates the builder for one annotated declaration.
         *
         * @param transform transformation that reports errors and reads
         *        annotation members
         * @param annotatedNode class, method, or constructor the builder is
         *        generated for
         * @param anno the {@code @Builder} annotation on that declaration
         */
        void build(BuilderASTTransformation transform, AnnotatedNode annotatedNode, AnnotationNode anno);
    }

    /**
     * Base class for builder generation strategies.
     */
    public abstract static class AbstractBuilderStrategy implements BuilderStrategy {
        /**
         * Lists instance property fields of {@code cNode}, skipping internal
         * names. Same as the four-argument form with {@code allNames} false.
         *
         * @param cNode class whose property fields are collected
         * @param includes names to keep; an empty or null list keeps every
         *        name that is not excluded
         * @param excludes names to drop, or null
         * @return the matching properties, in field order
         */
        protected static List<PropertyInfo> getPropertyInfoFromClassNode(ClassNode cNode, List<String> includes, List<String> excludes) {
            return getPropertyInfoFromClassNode(cNode, includes, excludes, false);
        }

        /**
         * Lists instance property fields of {@code cNode}. A name in
         * {@code excludes} is dropped. When {@code includes} is non-empty,
         * a name absent from it is dropped. When {@code allNames} is false,
         * an internal name is dropped as well.
         *
         * @param cNode class whose property fields are collected
         * @param includes names to keep; empty or null does not filter
         * @param excludes names to drop, or null
         * @param allNames {@code true} to keep internal names
         * @return the matching properties, in field order
         */
        protected static List<PropertyInfo> getPropertyInfoFromClassNode(ClassNode cNode, List<String> includes, List<String> excludes, boolean allNames) {
            List<PropertyInfo> props = new ArrayList<>();
            for (FieldNode fNode : getInstancePropertyFields(cNode)) {
                if (shouldSkip(fNode, excludes, includes, allNames)) continue;
                props.add(new PropertyInfo(fNode.getName(), fNode.getType()));
            }
            return props;
        }

        /**
         * Lists writable, non-hidden JavaBean properties of {@code cNode}
         * via {@link Introspector}. Filtering matches
         * {@link AbstractASTTransformation#shouldSkipUndefinedAware(String, List, List, boolean)}.
         * An introspection failure leaves the list collected so far, which
         * may be empty.
         *
         * @param cNode class to introspect; it must already be loadable
         * @param includes names to keep; null does not filter, and an empty
         *        list drops every name
         * @param excludes names to drop, or null
         * @param allNames {@code true} to keep internal names
         * @return the matching properties
         */
        protected static List<PropertyInfo> getPropertyInfoFromBeanInfo(ClassNode cNode, List<String> includes, List<String> excludes, boolean allNames) {
            final List<PropertyInfo> result = new ArrayList<>();
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(cNode.getTypeClass());
                for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
                    if (shouldSkipUndefinedAware(descriptor.getName(), excludes, includes, allNames)) continue;
                    // skip hidden and read-only props
                    if (descriptor.isHidden() || descriptor.getWriteMethod() == null) continue;
                    result.add(new PropertyInfo(descriptor.getName(), make(descriptor.getPropertyType())));
                }
            } catch (IntrospectionException ignore) {
            }
            return result;
        }

        /**
         * Builds a setter name. An empty {@code prefix} yields
         * {@code fieldName} unchanged. Otherwise the result is
         * {@code prefix} plus {@code fieldName} with its first character
         * upper-cased.
         *
         * @param prefix setter prefix, possibly empty
         * @param fieldName property name, with at least one character
         * @return the setter name
         */
        protected String getSetterName(String prefix, String fieldName) {
            return prefix.isEmpty() ? fieldName : prefix + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        }

        /**
         * Reports {@code memberName} when this strategy does not support it.
         * Same as the four-argument form with an empty extra message.
         *
         * @param transform transformation that records the error
         * @param anno annotation being processed
         * @param memberName annotation member to reject when it is set
         * @return {@code true} when an error was recorded
         */
        protected boolean unsupportedAttribute(BuilderASTTransformation transform, AnnotationNode anno, String memberName) {
            return unsupportedAttribute(transform, anno, memberName, "");
        }

        /**
         * Reports an error when {@code memberName} is set to something other
         * than the undefined marker. A missing member, an undefined string,
         * or an undefined class is accepted. {@code extraMessage} is
         * appended, with a leading space, when it is not empty.
         *
         * @param transform transformation that records the error
         * @param anno annotation being processed
         * @param memberName annotation member to reject when it is set
         * @param extraMessage text appended to the error, or empty
         * @return {@code true} when an error was recorded
         */
        protected boolean unsupportedAttribute(BuilderASTTransformation transform, AnnotationNode anno, String memberName, String extraMessage) {
            Object memberValue = transform.getMemberValue(anno, memberName);
            if (memberValue instanceof String && isUndefined((String) memberValue)) return false;
            if (memberValue == null) {
                memberValue = transform.getMemberClassValue(anno, memberName);
                if (memberValue != null && isUndefined((ClassNode) memberValue)) {
                    memberValue = null;
                }
            }
            if (memberValue != null) {
                String message = extraMessage.isEmpty() ? "" : " " + extraMessage;
                transform.addError("Error during " + MY_TYPE_NAME + " processing: Annotation attribute '" + memberName + "' not supported by " + getClass().getName() + message, anno);
                return true;
            }
            return false;
        }

        /**
         * Reports an error when {@code name} is not a property in
         * {@code properties}.
         *
         * @param transform transformation that records the error
         * @param anno annotation the error is reported against
         * @param name property name that was requested
         * @param properties properties this strategy knows about
         */
        protected void checkKnownProperty(BuilderASTTransformation transform, AnnotationNode anno, String name, List<PropertyInfo> properties) {
            for (PropertyInfo prop: properties) {
                if (name.equals(prop.getName())) {
                    return;
                }
            }
            transform.addError("Error during " + MY_TYPE_NAME + " processing: tried to include unknown property '" + name + "'", anno);
        }

        /**
         * Reports an error when no field in {@code fields} is named
         * {@code name}. The error text calls {@code name} a property.
         *
         * @param transform transformation that records the error
         * @param anno annotation the error is reported against
         * @param name field name that was requested
         * @param fields fields this strategy knows about
         */
        protected void checkKnownField(BuilderASTTransformation transform, AnnotationNode anno, String name, List<FieldNode> fields) {
            for (FieldNode field: fields) {
                if (name.equals(field.getName())) {
                    return;
                }
            }
            transform.addError("Error during " + MY_TYPE_NAME + " processing: tried to include unknown property '" + name + "'", anno);
        }

        /**
         * Fills {@code excludes} and {@code includes} from {@code anno}.
         * An {@code includes} member replaces {@code includes}; an
         * {@code excludes} member is appended. When {@code includes} is
         * absent and {@code excludes} is still empty, the lists are
         * filled from a {@code @TupleConstructor} on {@code cNode} if one
         * is present. A one-element {@code includes} list holding the
         * undefined marker is treated as no includes.
         *
         * @param transform transformation that reads members and reports errors
         * @param anno the {@code @Builder} annotation
         * @param cNode class that may carry {@code @TupleConstructor}
         * @param excludes names to exclude; this method appends to it
         * @param includes names to include; this method may clear and refill it
         * @return {@code false} when both includes and excludes are supplied
         */
        protected boolean getIncludeExclude(BuilderASTTransformation transform, AnnotationNode anno, ClassNode cNode, List<String> excludes, List<String> includes) {
            List<String> directExcludes = transform.getMemberStringList(anno, "excludes");
            if (directExcludes != null) excludes.addAll(directExcludes);
            List<String> directIncludes = transform.getMemberStringList(anno, "includes");
            if (directIncludes != null) {
                includes.clear();
                includes.addAll(directIncludes);
            }
            if (directIncludes == null && excludes.isEmpty()) {
                if (transform.hasAnnotation(cNode, TupleConstructorASTTransformation.MY_TYPE)) {
                    AnnotationNode tupleConstructor = cNode.getAnnotations(TupleConstructorASTTransformation.MY_TYPE).get(0);
                    if (excludes.isEmpty()) {
                        List<String>  tupleExcludes = transform.getMemberStringList(tupleConstructor, "excludes");
                        if (tupleExcludes != null) excludes.addAll(tupleExcludes);
                    }
                    if (includes.isEmpty()) {
                        List<String>  tupleIncludes = transform.getMemberStringList(tupleConstructor, "includes");
                        if (tupleIncludes != null) {
                            includes.clear();
                            includes.addAll(tupleIncludes);
                        }
                    }
                }
            }
            List<String> includesToCheck = includes.size() == 1 && isUndefined(includes.get(0)) ? null : includes;
            return transform.checkIncludeExcludeUndefinedAware(anno, excludes, includesToCheck, MY_TYPE_NAME);
        }

        /**
         * Returns the property fields the builder should see. When
         * {@code anno} sets {@code includeSuperProperties} to true, fields
         * from superclasses are included; otherwise only the instance
         * property fields of {@code buildee} are returned.
         *
         * @param transform transformation that reads the annotation member
         * @param anno the {@code @Builder} annotation
         * @param buildee class being built
         * @return those property fields
         */
        protected List<FieldNode> getFields(BuilderASTTransformation transform, AnnotationNode anno, ClassNode buildee) {
           boolean includeSuperProperties = transform.memberHasValue(anno, "includeSuperProperties", true);
           return includeSuperProperties ? getSuperPropertyFields(buildee) : getInstancePropertyFields(buildee);
        }

        /**
         * Lists properties of {@code cNode}, then property fields from
         * {@link #getFields} whose names were not already seen. Superclass
         * properties and static properties are not taken from the property
         * walk. When {@code allProperties} is true, JavaBean getters that
         * have no field are included. Include, exclude, and internal-name
         * filtering matches {@link #getPropertyInfoFromClassNode(ClassNode, List, List, boolean)}.
         *
         * @param transform transformation passed to {@link #getFields}
         * @param anno annotation passed to {@link #getFields}
         * @param cNode class whose properties are collected
         * @param includes names to keep; empty or null does not filter
         * @param excludes names to drop, or null
         * @param allNames {@code true} to keep internal names
         * @param allProperties {@code true} to include getters with no field
         * @return the matching properties
         */
        protected List<PropertyInfo> getPropertyInfoFromClassNode(BuilderASTTransformation transform, AnnotationNode anno, ClassNode cNode, List<String> includes, List<String> excludes, boolean allNames, boolean allProperties) {
            List<PropertyInfo> props = new ArrayList<>();
            List<String> seen = new ArrayList<>();
            for (PropertyNode pNode : BeanUtils.getAllProperties(cNode, false, false, allProperties)) {
                if (shouldSkip(pNode, excludes, includes, allNames)) continue;
                props.add(new PropertyInfo(pNode.getName(), pNode.getType()));
                seen.add(pNode.getName());
            }
            for (FieldNode fNode : getFields(transform, anno, cNode)) {
                if (seen.contains(fNode.getName()) || shouldSkip(fNode, excludes, includes, allNames)) continue;
                props.add(new PropertyInfo(fNode.getName(), fNode.getType()));
            }
            return props;
        }

        /**
         * Lists the properties the builder should copy. A {@code buildee}
         * with no module is read with
         * {@link #getPropertyInfoFromBeanInfo}, and {@code allProperties}
         * is ignored. Otherwise the properties come from the AST.
         *
         * @param transform transformation passed through to the AST walk
         * @param anno annotation passed through to the AST walk
         * @param buildee class being built
         * @param excludes names to drop, or null
         * @param includes names to keep, or null
         * @param allNames {@code true} to keep internal names
         * @param allProperties {@code true} to include getters with no field
         *        on the AST path
         * @return the matching properties
         */
        protected List<PropertyInfo> getPropertyInfos(BuilderASTTransformation transform, AnnotationNode anno, ClassNode buildee, List<String> excludes, List<String> includes, boolean allNames, boolean allProperties) {
            if (buildee.getModule() == null) {
                return getPropertyInfoFromBeanInfo(buildee, includes, excludes, allNames);
            }
            return getPropertyInfoFromClassNode(transform, anno, buildee, includes, excludes, allNames, allProperties);
        }

        /**
         * Holds information about a builder property.
         */
        protected static class PropertyInfo {
            /**
             * Creates a new property information holder.
             *
             * @param name the property name
             * @param type the property class type
             */
            public PropertyInfo(String name, ClassNode type) {
                this.name = name;
                this.type = type;
            }

            private String name;
            private ClassNode type;

            /**
             * Gets the property name.
             *
             * @return the property name
             */
            public String getName() {
                return name;
            }

            /**
             * Gets the property type.
             *
             * @return the property ClassNode
             */
            public ClassNode getType() {
                return type;
            }

            /**
             * Sets the property name.
             *
             * @param name the property name
             */
            public void setName(String name) {
                this.name = name;
            }

            /**
             * Sets the property type.
             *
             * @param type the property ClassNode
             */
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
        ClassNode strategyClass = getMemberClassValue(anno, "builderStrategy", make(DefaultStrategy.class));

        if (strategyClass == null) {
            addError("Couldn't determine builderStrategy class", anno);
            return null;
        }

        String className = strategyClass.getName();
        try {
            Object instance = loader.loadClass(className).getDeclaredConstructor().newInstance();
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
}
