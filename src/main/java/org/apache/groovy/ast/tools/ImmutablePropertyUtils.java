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
package org.apache.groovy.ast.tools;

import groovy.transform.ImmutableOptions;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.runtime.ReflectionMethodInvoker;
import org.codehaus.groovy.transform.AbstractASTTransformation;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isOrImplements;

public class ImmutablePropertyUtils {
    private static final ClassNode CLONEABLE_TYPE = make(Cloneable.class);
    private static final ClassNode DATE_TYPE = make(Date.class);
    private static final ClassNode REFLECTION_INVOKER_TYPE = make(ReflectionMethodInvoker.class);
    private static final Class<? extends Annotation> IMMUTABLE_OPTIONS_CLASS = ImmutableOptions.class;
    public static final ClassNode IMMUTABLE_OPTIONS_TYPE = makeWithoutCaching(IMMUTABLE_OPTIONS_CLASS, false);
    private static final String MEMBER_KNOWN_IMMUTABLE_CLASSES = "knownImmutableClasses";
    private static final String MEMBER_KNOWN_IMMUTABLES = "knownImmutables";
    /*
              Currently leaving BigInteger and BigDecimal in list but see:
              http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6348370

              Also, Color is not final so while not normally used with child
              classes, it isn't strictly immutable. Use at your own risk.

              This list can by extended by providing "known immutable" classes
              via Immutable.knownImmutableClasses
             */
    private static final Set<String> BUILTIN_IMMUTABLES = new HashSet<String>(Arrays.asList(
            "boolean",
            "byte",
            "char",
            "double",
            "float",
            "int",
            "long",
            "short",
            "java.lang.Class",
            "java.lang.Boolean",
            "java.lang.Byte",
            "java.lang.Character",
            "java.lang.Double",
            "java.lang.Float",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Short",
            "java.lang.String",
            "java.math.BigInteger",
            "java.math.BigDecimal",
            "java.awt.Color",
            "java.net.URI",
            "java.util.UUID",
            "java.time.DayOfWeek",
            "java.time.Duration",
            "java.time.Instant",
            "java.time.LocalDate",
            "java.time.LocalDateTime",
            "java.time.LocalTime",
            "java.time.Month",
            "java.time.MonthDay",
            "java.time.OffsetDateTime",
            "java.time.OffsetTime",
            "java.time.Period",
            "java.time.Year",
            "java.time.YearMonth",
            "java.time.ZonedDateTime",
            "java.time.ZoneOffset",
            "java.time.ZoneRegion",
            "java.time.chrono.ChronoLocalDate",
            "java.time.chrono.ChronoLocalDateTime",
            "java.time.chrono.Chronology",
            "java.time.chrono.ChronoPeriod",
            "java.time.chrono.ChronoZonedDateTime",
            "java.time.chrono.Era",
            "java.time.format.DecimalStyle",
            "java.time.format.FormatStyle",
            "java.time.format.ResolverStyle",
            "java.time.format.SignStyle",
            "java.time.format.TextStyle",
            "java.time.temporal.IsoFields",
            "java.time.temporal.JulianFields",
            "java.time.temporal.ValueRange",
            "java.time.temporal.WeekFields",
            "java.io.File"
    ));

    private static final Set<String> BUILTIN_IMMUTABLE_ANNOTATIONS = new HashSet<String>(Arrays.asList(
            "groovy.transform.Immutable",
            "groovy.transform.KnownImmutable",
//            "javax.annotation.concurrent.Immutable", // its RetentionPolicy is CLASS, can not be got via reflection
            "net.jcip.annotations.Immutable" // supported by Findbugs and IntelliJ IDEA
    ));

    private ImmutablePropertyUtils() { }

    public static Expression cloneArrayOrCloneableExpr(Expression fieldExpr, ClassNode type) {
        Expression smce = callX(
                REFLECTION_INVOKER_TYPE,
                "invoke",
                args(
                        fieldExpr,
                        constX("clone"),
                        new ArrayExpression(ClassHelper.OBJECT_TYPE.makeArray(), Collections.emptyList())
                )
        );
        return castX(type, smce);
    }

    public static boolean implementsCloneable(ClassNode fieldType) {
        return isOrImplements(fieldType, CLONEABLE_TYPE);
    }

    public static Expression cloneDateExpr(Expression origDate) {
        return ctorX(DATE_TYPE, callX(origDate, "getTime"));
    }

    public static boolean derivesFromDate(ClassNode fieldType) {
        return fieldType.isDerivedFrom(DATE_TYPE);
    }

    public static String createErrorMessage(String className, String fieldName, String typeName, String mode) {
        return "Unsupported type (" + prettyTypeName(typeName) + ") found for field '" + fieldName + "' while " + mode + " immutable class " + className + ".\n" +
                "Immutable classes only support properties with effectively immutable types including:\n" +
                "- Strings, primitive types, wrapper types, Class, BigInteger and BigDecimal, enums\n" +
                "- classes annotated with @KnownImmutable and known immutables (java.awt.Color, java.net.URI)\n" +
                "- Cloneable classes, collections, maps and arrays, and other classes with special handling\n" +
                "  (java.util.Date and various java.time.* classes and interfaces)\n" +
                "Other restrictions apply, please see the groovydoc for " + IMMUTABLE_OPTIONS_TYPE.getNameWithoutPackage() + " for further details";
    }

    private static String prettyTypeName(String name) {
        return name.equals("java.lang.Object") ? name + " or def" : name;
    }

    public static boolean isKnownImmutableType(ClassNode fieldType, List<String> knownImmutableClasses) {
        if (builtinOrDeemedType(fieldType, knownImmutableClasses))
            return true;
        if (!fieldType.isResolved())
            return false;
        if ("java.util.Optional".equals(fieldType.getName()) && fieldType.getGenericsTypes() != null && fieldType.getGenericsTypes().length == 1) {
            GenericsType optionalType = fieldType.getGenericsTypes()[0];
            if (optionalType.isResolved() && !optionalType.isPlaceholder() && !optionalType.isWildcard()) {
                ClassNode valueType = optionalType.getType();
                if (builtinOrDeemedType(valueType, knownImmutableClasses)) return true;
                if (valueType.isEnum()) return true;
            }
        }
        return fieldType.isEnum() ||
                ClassHelper.isPrimitiveType(fieldType) ||
                hasImmutableAnnotation(fieldType);
    }

    private static boolean builtinOrDeemedType(ClassNode fieldType, List<String> knownImmutableClasses) {
        return isBuiltinImmutable(fieldType.getName()) || knownImmutableClasses.contains(fieldType.getName()) || hasImmutableAnnotation(fieldType);
    }

    private static boolean hasImmutableAnnotation(ClassNode type) {
        List<AnnotationNode> annotations = type.getAnnotations();
        for (AnnotationNode next : annotations) {
            String name = next.getClassNode().getName();
            if (matchingImmutableMarkerName(name)) return true;
        }
        return false;
    }

    private static boolean matchingImmutableMarkerName(String name) {
        return BUILTIN_IMMUTABLE_ANNOTATIONS.contains(name);
    }

    public static boolean isBuiltinImmutable(String typeName) {
        return BUILTIN_IMMUTABLES.contains(typeName);
    }

    private static boolean hasImmutableAnnotation(Class clazz) {
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation next : annotations) {
            String name = next.annotationType().getName();
            if (matchingImmutableMarkerName(name)) return true;
        }
        return false;
    }

    public static boolean builtinOrMarkedImmutableClass(Class<?> clazz) {
        return isBuiltinImmutable(clazz.getName()) || hasImmutableAnnotation(clazz);
    }

    public static List<String> getKnownImmutables(AbstractASTTransformation xform, ClassNode cNode) {
        List<AnnotationNode> annotations = cNode.getAnnotations(ImmutablePropertyUtils.IMMUTABLE_OPTIONS_TYPE);
        AnnotationNode anno = annotations.isEmpty() ? null : annotations.get(0);
        final List<String> immutables = new ArrayList<String>();
        if (anno == null) return immutables;

        final Expression expression = anno.getMember(MEMBER_KNOWN_IMMUTABLES);
        if (expression == null) return immutables;

        if (!(expression instanceof ListExpression)) {
            xform.addError("Use the Groovy list notation [el1, el2] to specify known immutable property names via \"" + MEMBER_KNOWN_IMMUTABLES + "\"", anno);
            return immutables;
        }

        final ListExpression listExpression = (ListExpression) expression;
        for (Expression listItemExpression : listExpression.getExpressions()) {
            if (listItemExpression instanceof ConstantExpression) {
                immutables.add((String) ((ConstantExpression) listItemExpression).getValue());
            }
        }
        if (!xform.checkPropertyList(cNode, immutables, "knownImmutables", anno, "immutable class", false)) return immutables;

        return immutables;
    }

    public static List<String> getKnownImmutableClasses(AbstractASTTransformation xform, ClassNode cNode) {
        List<AnnotationNode> annotations = cNode.getAnnotations(ImmutablePropertyUtils.IMMUTABLE_OPTIONS_TYPE);
        AnnotationNode anno = annotations.isEmpty() ? null : annotations.get(0);
        final List<String> immutableClasses = new ArrayList<String>();

        if (anno == null) return immutableClasses;
        final Expression expression = anno.getMember(MEMBER_KNOWN_IMMUTABLE_CLASSES);
        if (expression == null) return immutableClasses;

        if (!(expression instanceof ListExpression)) {
            xform.addError("Use the Groovy list notation [el1, el2] to specify known immutable classes via \"" + MEMBER_KNOWN_IMMUTABLE_CLASSES + "\"", anno);
            return immutableClasses;
        }

        final ListExpression listExpression = (ListExpression) expression;
        for (Expression listItemExpression : listExpression.getExpressions()) {
            if (listItemExpression instanceof ClassExpression) {
                immutableClasses.add(listItemExpression.getType().getName());
            }
        }

        return immutableClasses;
    }
}
