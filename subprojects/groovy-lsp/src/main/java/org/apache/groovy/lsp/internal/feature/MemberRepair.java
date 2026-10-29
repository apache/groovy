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
package org.apache.groovy.lsp.internal.feature;

import org.apache.groovy.lsp.internal.compile.CompilerSettings;
import org.apache.groovy.lsp.internal.compile.GroovyCompiler;
import org.apache.groovy.lsp.internal.workspace.TextDocument;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.eclipse.lsp4j.Position;

/**
 * A parsable stand-in for a member access the real buffer rejected,
 * so the declarations above the caret can still be typed.
 */
final class MemberRepair {

    /**
     * Compiles a parsable stand-in for a member access the real buffer rejected.
     * The caret line becomes {@code <receiver>.lsp}, and a leading {@code def}
     * on a bare name is dropped so {@code def a.} still sees the earlier {@code a}.
     */
    static ModuleNode repairedModule(final TextDocument document, final Position position,
                                             final ClassLoader parentLoader,
                                             final CompilerConfiguration hostConfiguration,
                                             final CompilerSettings settings) {
        if (document == null || position == null) {
            return null;
        }
        String repaired = repairMemberAccess(document.getText(), position.getLine(), position.getCharacter());
        if (repaired == null || repaired.equals(document.getText())) {
            return null;
        }
        return GroovyCompiler.compileFragment("repair.groovy", repaired, settings, parentLoader,
                hostConfiguration, Phases.SEMANTIC_ANALYSIS);
    }

    static String repairMemberAccess(final String text, final int line, final int character) {
        if (text == null) {
            return null;
        }
        int[] range = lineRange(text, Math.max(line, 0));
        String sourceLine = text.substring(range[0], range[1]);
        int caret = Math.min(Math.max(character, 0), sourceLine.length());
        int end = ReceiverInference.receiverEndOffset(sourceLine, caret);
        String receiver = sourceLine.substring(0, end).trim();
        if (receiver.startsWith("def") && receiver.length() > 3 && Character.isWhitespace(receiver.charAt(3))) {
            String rest = receiver.substring(3).trim();
            if (isIdentifier(rest)) {
                receiver = rest;
            }
        }
        if (receiver.isEmpty()) {
            return text;
        }
        return text.substring(0, range[0]) + receiver + ".lsp" + text.substring(range[1]);
    }

    static boolean isIdentifier(final String text) {
        if (text == null || text.isEmpty() || !Character.isJavaIdentifierStart(text.codePointAt(0))) {
            return false;
        }
        int index = Character.charCount(text.codePointAt(0));
        while (index < text.length()) {
            int cp = text.codePointAt(index);
            if (!Character.isJavaIdentifierPart(cp) && cp != '$') {
                return false;
            }
            index += Character.charCount(cp);
        }
        return true;
    }

    static int[] lineRange(final String text, final int line) {
        int current = 0;
        int start = 0;
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '\n' || c == '\r') {
                if (current == line) {
                    return new int[] {start, i};
                }
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i += 1;
                }
                current += 1;
                start = i + 1;
            }
            i++;
        }
        if (current == line) {
            return new int[] {start, text.length()};
        }
        return new int[] {text.length(), text.length()};
    }
}
