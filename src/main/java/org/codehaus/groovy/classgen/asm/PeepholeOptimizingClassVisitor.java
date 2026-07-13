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

import org.codehaus.groovy.control.CompilerConfiguration;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Class-level installation point for Groovy's single-pass bytecode peephole layer.
 * <p>
 * Wraps each {@link MethodVisitor} returned by the delegate's
 * {@link #visitMethod(int, String, String, String, String[])} with
 * {@link PeepholeOptimizingMethodVisitor}, so <em>every</em> method body receives
 * the same stack-local compaction — user methods, constructors, static initializers,
 * and synthetic helpers alike (MOP bridges, call-site array initializers,
 * {@code class$}/{@code $get$} resolvers, large-list init chunks, and so on).
 * <p>
 * Centralizing the wrap here avoids ad-hoc
 * {@code new PeepholeOptimizingMethodVisitor(...)} at individual emission sites and
 * keeps compaction in lockstep with {@link OperandStack}, which emits integer and
 * other primitive constants via {@code visitLdcInsn} and relies on the peephole
 * visitor to narrow them to {@code ICONST_*}, {@code BIPUSH}, {@code SIPUSH}, etc.
 * <p>
 * Installed once by {@link WriterController} when the class visitor chain is built
 * (optionally outside a {@code LoggableClassVisitor} / {@code TraceClassVisitor}
 * pair used for classgen logging). Field and attribute visits are left unchanged;
 * only method bodies are optimized.
 *
 * @see PeepholeOptimizingMethodVisitor
 * @see WriterController
 * @since 6.0.0
 */
public final class PeepholeOptimizingClassVisitor extends ClassVisitor {

    /**
     * Creates a visitor that peephole-optimizes every method written to
     * {@code classVisitor}.
     *
     * @param classVisitor the next visitor in the class-generation chain
     *        (typically a {@link org.objectweb.asm.ClassWriter}, optionally
     *        preceded by logging / tracing adapters)
     */
    public PeepholeOptimizingClassVisitor(final ClassVisitor classVisitor) {
        super(CompilerConfiguration.ASM_API_VERSION, classVisitor);
    }

    /**
     * {@inheritDoc}
     * <p>
     * When the delegate returns a non-{@code null} visitor, the result is a
     * {@link PeepholeOptimizingMethodVisitor} (or the same instance if the
     * delegate already wrapped the method). A {@code null} from the delegate is
     * propagated unchanged so methods can still be skipped per the ASM contract.
     */
    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String[] exceptions) {
        return PeepholeOptimizingMethodVisitor.wrap(super.visitMethod(access, name, descriptor, signature, exceptions));
    }
}
