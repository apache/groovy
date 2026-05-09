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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.MethodNode;

/**
 * A context shared across generations of a class and its inner classes.
 */
public class GeneratorContext {

    private int innerClassIdx = 1;
    private int closureClassIdx = 1;
    private int syntheticMethodIdx = 0;
    private final CompileUnit compileUnit;

    /**
     * Creates a new generator context for the given compile unit.
     *
     * @param compileUnit the compile unit this context is associated with
     */
    public GeneratorContext(final CompileUnit compileUnit) {
        this.compileUnit = compileUnit;
    }

    /**
     * Creates a new generator context with a custom inner class index offset.
     *
     * @param compileUnit the compile unit this context is associated with
     * @param innerClassOffset the starting index for inner class naming
     */
    public GeneratorContext(final CompileUnit compileUnit, final int innerClassOffset) {
        this.compileUnit = compileUnit;
        this.innerClassIdx = innerClassOffset;
    }

    // for ACG nestmate determination !
           int getClosureClassIndex() {
        return closureClassIdx;
    }

    /**
     * Returns and increments the inner class index for generating unique inner class names.
     *
     * @return the next available inner class index
     */
    public int getNextInnerClassIdx() {
        return innerClassIdx++;
    }

    /**
     * Returns the compile unit associated with this context.
     *
     * @return the compile unit
     */
    public CompileUnit getCompileUnit() {
        return compileUnit;
    }

    /**
     * Generates the next unique closure inner class name.
     *
     * @param owner the owner class (currently unused but kept for API compatibility)
     * @param enclosingClass the class that encloses the closure
     * @param enclosingMethod the method that encloses the closure, or null if at class level
     * @return a unique name for the closure inner class
     */
    public String getNextClosureInnerName(final ClassNode owner, final ClassNode enclosingClass, final MethodNode enclosingMethod) {
        return getNextInnerName(enclosingClass, enclosingMethod, "closure");
    }

    /**
     * Generates the next unique lambda inner class name.
     *
     * @param owner the owner class (currently unused but kept for API compatibility)
     * @param enclosingClass the class that encloses the lambda
     * @param enclosingMethod the method that encloses the lambda, or null if at class level
     * @return a unique name for the lambda inner class
     */
    public String getNextLambdaInnerName(final ClassNode owner, final ClassNode enclosingClass, final MethodNode enclosingMethod) {
        return getNextInnerName(enclosingClass, enclosingMethod, "lambda");
    }

    private String getNextInnerName(final ClassNode enclosingClass, final MethodNode enclosingMethod, final String classifier) {
        String typeName = "_" + classifier + closureClassIdx++;
        if (enclosingMethod != null && !ClassHelper.isGeneratedFunction(enclosingClass)) {
            typeName = "_" + encodeAsValidClassName(enclosingMethod.getName()) + typeName;
        }
        return typeName;
    }

    /**
     * Generates a unique synthetic method name for a constructor reference.
     *
     * @param enclosingMethodNode the method that contains the constructor reference, or null if at class level
     * @return a unique synthetic method name for the constructor reference
     */
    public String getNextConstructorReferenceSyntheticMethodName(final MethodNode enclosingMethodNode) {
        return "ctorRef$"
                + (null == enclosingMethodNode
                        ? ""
                        : enclosingMethodNode.getName().replace("<", "").replace(">", "") + "$" )
                + syntheticMethodIdx++;
    }

    private static final boolean[] CHARACTERS_TO_ENCODE;
    private static final int MIN_ENCODING, MAX_ENCODING;
    static {
        char[] chars = {' ', '!', '"', '#', '$', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '@', '[', '\\', ']', '^', '{', '}', '~'};

        MIN_ENCODING = chars[0];
        MAX_ENCODING = chars[chars.length - 1];
        CHARACTERS_TO_ENCODE = new boolean[MAX_ENCODING - MIN_ENCODING + 1];

        for (char c : chars) {
            CHARACTERS_TO_ENCODE[c - MIN_ENCODING] = true;
        }
    }

    /**
     * Encodes a name to be a valid Java class name by replacing special characters with underscores.
     * Characters such as operators, punctuation, and other invalid class name characters are encoded.
     * The special names "module-info" and "package-info" are preserved unchanged.
     *
     * @param name the name to encode
     * @return the encoded class name safe for use as a Java identifier
     */
    public static String encodeAsValidClassName(final String name) {
        if ("module-info".equals(name) || "package-info".equals(name)) return name;

        int lastEscape = -1;
        StringBuilder b = null;
        final int n = name.length();
        for (int i = 0; i < n; i += 1) {
            int encodeIndex = name.charAt(i) - MIN_ENCODING;
            if (encodeIndex >= 0 && encodeIndex < CHARACTERS_TO_ENCODE.length) {
                if (CHARACTERS_TO_ENCODE[encodeIndex]) {
                    if (b == null) {
                        b = new StringBuilder(name.length() + 3);
                        b.append(name, 0, i);
                    } else {
                        b.append(name, lastEscape + 1, i);
                    }
                    b.append('_');
                    lastEscape = i;
                }
            }
        }
        if (b == null) return name;
        if (lastEscape == -1) throw new GroovyBugError("unexpected escape char control flow in " + name);
        b.append(name, lastEscape + 1, n);
        return b.toString();
    }
}
