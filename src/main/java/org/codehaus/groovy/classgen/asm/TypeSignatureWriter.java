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
package org.codehaus.groovy.classgen.asm;

import org.codehaus.groovy.ast.ClassNode;

/**
 * Writes the JVMS 4.7.9.1 nested form of a parameterized type for a Signature
 * attribute ({@code LOuter<...>.Inner<...>}). Package-private helper of
 * {@link BytecodeHelper}.
 */
final class TypeSignatureWriter {

    private TypeSignatureWriter() {
    }

    /**
     * Writes a class type and its type arguments, using the JLS 4.5 nested form
     * {@code LOuter&lt;...&gt;.Inner&lt;...&gt;} when an enclosing rare type is present.
     */
    static void writeParameterizedClass(final StringBuilder ret, final ClassNode printType) {
        ClassNode owner = printType.getOuterClassType();
        // JVMS 4.7.9.1 nested form is only for a parameterized enclosing type.
        // A raw enclosing type must keep the binary name (Outer$Inner<...>).
        if (owner != null && BytecodeHelper.hasGenerics(owner)) {
            writeParameterizedClass(ret, owner);
            ret.append('.');
            ret.append(innerClassSimpleName(printType, owner));
            BytecodeHelper.addSubTypes(ret, printType.getGenericsTypes(), "<", ">");
            return;
        }
        ret.append(BytecodeHelper.getTypeDescription(printType, false));
        BytecodeHelper.addSubTypes(ret, printType.getGenericsTypes(), "<", ">");
    }

    /**
     * Simple name of {@code inner} relative to {@code owner} for a JVMS 4.7.9.1
     * nested type signature. Accepts both {@code Outer.Inner} (parser) and
     * {@code Outer$Inner} (binary name after resolve); {@code Foo$1} is the
     * same prefix path. If {@code inner} is not nested under {@code owner},
     * the result is the last identifier only — never extra qualification of
     * {@code owner} (so owner {@code Foo} and inner {@code Bar.X} yield {@code X}).
     */
    static String innerClassSimpleName(final ClassNode inner, final ClassNode owner) {
        String innerName = inner.getName();
        String ownerName = owner.getName();
        String nested = nestedNameAfterOwner(innerName, ownerName);
        if (nested != null) {
            return nested.replace('$', '.');
        }
        int sep = Math.max(innerName.lastIndexOf('.'), innerName.lastIndexOf('$'));
        return sep < 0 ? innerName : innerName.substring(sep + 1);
    }

    /**
     * Remainder of {@code innerName} after {@code ownerName} when nested under
     * it ({@code .} or {@code $} separator); otherwise {@code null}.
     */
    static String nestedNameAfterOwner(final String innerName, final String ownerName) {
        int ownerLen = ownerName.length();
        if (innerName.length() <= ownerLen || !innerName.startsWith(ownerName)) {
            return null;
        }
        char sep = innerName.charAt(ownerLen);
        if (sep != '.' && sep != '$') {
            return null;
        }
        return innerName.substring(ownerLen + 1);
    }
}
