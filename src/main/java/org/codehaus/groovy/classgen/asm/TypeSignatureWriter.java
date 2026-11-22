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
import org.codehaus.groovy.ast.GenericsType;

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
            BytecodeHelper.addSubTypes(ret, signatureArguments(printType), "<", ">");
            return;
        }
        ret.append(BytecodeHelper.getTypeDescription(printType, false));
        BytecodeHelper.addSubTypes(ret, signatureArguments(printType), "<", ">");
    }

    /**
     * Type arguments safe to write for a resolved declaration. A signature
     * cannot name arguments a class does not declare, so a count that does
     * not match the declaration is omitted. An unresolved name is left
     * unchanged. A source use site that does not match is rejected before
     * bytecode; a type built later, such as a lowest upper bound, may still
     * carry arguments this method has to drop.
     */
    private static GenericsType[] signatureArguments(final ClassNode printType) {
        GenericsType[] useSite = printType.getGenericsTypes();
        if (useSite == null || useSite.length == 0) {
            return useSite;
        }
        int declared = declaredTypeParameters(printType);
        if (declared >= 0 && declared != useSite.length) {
            return null;
        }
        return useSite;
    }

    /**
     * Number of type parameters the class declares, or {@code -1} when this
     * writer does not know. Unresolved names are not loaded here. A primary
     * node with no module is not a source class, so generics on it are not
     * a declaration.
     */
    private static int declaredTypeParameters(final ClassNode printType) {
        if (printType.isGenericsPlaceHolder()) return -1;
        ClassNode declared = printType.redirect();
        if (declared.isArray()) return -1;
        if (declared.isPrimaryClassNode()) {
            if (declared.getModule() == null) return 0;
            GenericsType[] formals = declared.getGenericsTypes();
            return formals == null ? 0 : formals.length;
        }
        if (!declared.isResolved()) return -1;
        if (declared != printType) {
            GenericsType[] formals = declared.getGenericsTypes();
            if (formals != null) return formals.length;
        }
        return declared.getTypeClass().getTypeParameters().length;
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
