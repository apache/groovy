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
package org.apache.groovy.asciidoctor;

import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.InlineMacroProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * Inline macro processor that generates documentation links.
 * Used for jdk:, gjdk:, gapi:, and gapid: macros in Groovy's asciidoc sources.
 * <p>
 * The base URL is read from a document attribute named "{macroName}-base-url".
 * For example, the "jdk" macro reads the "jdk-base-url" attribute.
 */
public class DocLinkMacroProcessor extends InlineMacroProcessor {

    private final String baseUrlAttribute;
    private final boolean directPath;

    /**
     * @param macroName     the macro name (e.g. "jdk", "gapi")
     * @param directPath    if true, appends the class path directly to the base URL
     *                      (for gapid); if false, appends via "?" query separator (for jdk/gjdk/gapi)
     */
    public DocLinkMacroProcessor(String macroName, boolean directPath) {
        super(macroName);
        this.baseUrlAttribute = macroName + "-base-url";
        this.directPath = directPath;
    }

    @Override
    public PhraseNode process(StructuralNode parent, String target, Map<String, Object> attributes) {
        String baseUrl = (String) parent.getDocument().getAttribute(baseUrlAttribute);
        String[] parts = target.split("#", 2);
        String className = parts[0];
        String anchor = parts.length > 1 ? parts[1] : null;

        String href;
        if ("index".equals(className)) {
            href = baseUrl;
        } else {
            String classPath = className.replace('.', '/') + ".html";
            if (directPath) {
                href = baseUrl + classPath + (anchor != null ? "#" + anchor : "");
            } else {
                href = baseUrl + "?" + classPath + (anchor != null ? "#" + anchor : "");
            }
        }

        Map<String, Object> options = new HashMap<>();
        options.put("type", ":link");
        options.put("target", href);
        Object text = attributes.get("text");
        return createPhraseNode(parent, "anchor", text != null ? (String) text : target, attributes, options);
    }
}
