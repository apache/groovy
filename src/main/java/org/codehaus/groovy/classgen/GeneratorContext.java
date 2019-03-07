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
 * A context shared across generations of a class and its inner classes
 */
public class GeneratorContext {

    private int innerClassIdx = 1;
    private int closureClassIdx = 1;
    private int lambdaClassIdx = 1;
    private final CompileUnit compileUnit;
    
    public GeneratorContext(CompileUnit compileUnit) {
        this.compileUnit = compileUnit;
    }

    public GeneratorContext(CompileUnit compileUnit, int innerClassOffset) {
        this.compileUnit = compileUnit;
        this.innerClassIdx = innerClassOffset;
    }

    public int getNextInnerClassIdx() {
        return innerClassIdx++;
    }

    public CompileUnit getCompileUnit() {
        return compileUnit;
    }

    public String getNextClosureInnerName(ClassNode owner, ClassNode enclosingClass, MethodNode enclosingMethod) {
        return getNextInnerName(owner, enclosingClass, enclosingMethod, "closure");
    }

    public String getNextLambdaInnerName(ClassNode owner, ClassNode enclosingClass, MethodNode enclosingMethod) {
        return getNextInnerName(owner, enclosingClass, enclosingMethod, "lambda");
    }

    private String getNextInnerName(ClassNode owner, ClassNode enclosingClass, MethodNode enclosingMethod, String classifier) {
        String methodName = "";
        if (enclosingMethod != null) {
            methodName = enclosingMethod.getName();

            if (enclosingClass.isDerivedFrom(ClassHelper.CLOSURE_TYPE)) {
                methodName = "";
            } else {
                methodName = "_" + encodeAsValidClassName(methodName);
            }
        }

        return methodName + "_" + classifier + closureClassIdx++;
    }


    private static final int MIN_ENCODING = ' ';
    private static final int MAX_ENCODING = ']';
    private static final boolean[] CHARACTERS_TO_ENCODE = new boolean[MAX_ENCODING-MIN_ENCODING+1];
    static {
        CHARACTERS_TO_ENCODE[' '-MIN_ENCODING] = true;
        CHARACTERS_TO_ENCODE['!'-MIN_ENCODING] = true;
        CHARACTERS_TO_ENCODE['/'-MIN_ENCODING] = true;
        CHARACTERS_TO_ENCODE['.'-MIN_ENCODING] = true;
        CHARACTERS_TO_ENCODE[';'-MIN_ENCODING] = true;
        CHARACTERS_TO_ENCODE['$'-MIN_ENCODING] = true;
        CHARACTERS_TO_ENCODE['<'-MIN_ENCODING] = true;
        CHARACTERS_TO_ENCODE['>'-MIN_ENCODING] = true;
        CHARACTERS_TO_ENCODE['['-MIN_ENCODING] = true;
        CHARACTERS_TO_ENCODE[']'-MIN_ENCODING] = true;
        CHARACTERS_TO_ENCODE[':'-MIN_ENCODING] = true;
        CHARACTERS_TO_ENCODE['\\'-MIN_ENCODING] = true;
    }

    public static String encodeAsValidClassName(String name) {
        final int l = name.length();
        StringBuilder b = null;
        int lastEscape = -1;
        for(int i = 0; i < l; ++i) {
            final int encodeIndex = name.charAt(i) - MIN_ENCODING;
            if (encodeIndex >= 0 && encodeIndex < CHARACTERS_TO_ENCODE.length) {
                if (CHARACTERS_TO_ENCODE[encodeIndex]) {
                    if(b == null) {
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
        if(b == null) return name;
        if (lastEscape == -1) throw new GroovyBugError("unexpected escape char control flow in "+name);
        b.append(name, lastEscape + 1, l);
        return b.toString();
    }
}
