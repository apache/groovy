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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Inline macro processor that generates documentation links.
 * Used for jdk:, gjdk:, gapi:, and gapid: macros in Groovy's asciidoc sources.
 * <p>
 * The base URL is read from a document attribute named "{macroName}-base-url".
 * For example, the "jdk" macro reads the "jdk-base-url" attribute.
 *
 * @since 6.0.0
 */
public class DocLinkMacroProcessor extends InlineMacroProcessor {

    /** Sentinel for classes that could not be resolved, so misses are cached too. */
    private static final String UNRESOLVED = "";

    private static final Map<String, String> DOC_PATH_CACHE = new ConcurrentHashMap<>();

    private final String baseUrlAttribute;
    private final boolean directPath;
    private final boolean moduleQualified;

    /**
     * Creates an inline macro processor for a specific documentation link family.
     *
     * @param macroName     the macro name (e.g. "jdk", "gapi")
     * @param directPath    if true, appends the class path directly to the base URL
     *                      (for gapid); if false, appends via "?" query separator (for gjdk/gapi)
     */
    public DocLinkMacroProcessor(String macroName, boolean directPath) {
        this(macroName, directPath, false);
    }

    /**
     * Creates an inline macro processor for a specific documentation link family.
     *
     * @param macroName       the macro name (e.g. "jdk", "gapi")
     * @param directPath      if true, appends the class path directly to the base URL
     *                        (for gapid); if false, appends via "?" query separator (for gjdk/gapi)
     * @param moduleQualified if true, prefixes the class path with the JDK module the class
     *                        belongs to, as required by JDK 9+ javadoc layouts (for jdk)
     */
    public DocLinkMacroProcessor(String macroName, boolean directPath, boolean moduleQualified) {
        super(macroName);
        this.baseUrlAttribute = macroName + "-base-url";
        this.directPath = directPath;
        this.moduleQualified = moduleQualified;
    }

    /**
     * Builds the module-qualified javadoc path for the supplied class, as used by JDK 9+
     * javadoc layouts, for example "java.desktop/javax/swing/JTabbedPane.html".
     *
     * @param className the fully qualified class name
     * @return the path, or null if the class is not a resolvable named-module JDK class
     */
    private static String jdkDocPath(String className) {
        String cached = DOC_PATH_CACHE.computeIfAbsent(className, name -> {
            Class<?> clazz = loadJdkClass(name);
            if (clazz == null) {
                return UNRESOLVED;
            }
            Module module = clazz.getModule();
            if (module == null || !module.isNamed()) {
                return UNRESOLVED;
            }
            String packageName = clazz.getPackageName();
            String prefix = packageName.isEmpty() ? "" : packageName.replace('.', '/') + "/";
            String nested = packageName.isEmpty()
                    ? clazz.getName()
                    : clazz.getName().substring(packageName.length() + 1);
            // javadoc separates enclosing classes with a dot, not the binary name's dollar
            return module.getName() + "/" + prefix + nested.replace('$', '.') + ".html";
        });
        return UNRESOLVED.equals(cached) ? null : cached;
    }

    /**
     * Loads a class shipped with the JDK.
     * <p>
     * The lookup goes through the platform class loader so that only JDK classes resolve;
     * Groovy's own classes deliberately do not. A dotted nested class name such as
     * "java.util.Map.Entry" is retried with its trailing segments turned into the binary
     * "$" form, so both spellings work in the macro.
     *
     * @param className the fully qualified class name
     * @return the class, or null if it is not a JDK class
     */
    private static Class<?> loadJdkClass(String className) {
        ClassLoader loader = ClassLoader.getPlatformClassLoader();
        for (String candidate = className; ; ) {
            try {
                return Class.forName(candidate, false, loader);
            } catch (ClassNotFoundException | LinkageError e) {
                int lastDot = candidate.lastIndexOf('.');
                if (lastDot < 0) {
                    return null;
                }
                candidate = candidate.substring(0, lastDot) + '$' + candidate.substring(lastDot + 1);
            }
        }
    }

    /**
     * Creates a link phrase for the supplied documentation target.
     *
     * @param parent the parent node receiving the rendered phrase
     * @param target the macro target containing the class name and optional anchor
     * @param attributes the macro attributes, including optional display text
     * @return the rendered documentation link phrase
     */
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
            String suffix = anchor != null ? "#" + anchor : "";
            String docPath = moduleQualified ? jdkDocPath(className) : null;
            if (docPath != null) {
                href = baseUrl + docPath + suffix;
            } else if (moduleQualified) {
                // module could not be resolved, so fall back to the pre-JDK 9 index form
                href = baseUrl + "index.html?" + classPath + suffix;
            } else if (directPath) {
                href = baseUrl + classPath + suffix;
            } else {
                href = baseUrl + "?" + classPath + suffix;
            }
        }

        Map<String, Object> options = new HashMap<>();
        options.put("type", ":link");
        options.put("target", href);
        // the display text arrives as the first positional attribute; "text" is only set
        // when it was spelled out as jdk:some.Class[text=...]
        Object text = attributes.get("text");
        if (text == null) {
            text = attributes.get("1");
        }
        return createPhraseNode(parent, "anchor", text != null ? text.toString() : target, attributes, options);
    }
}
