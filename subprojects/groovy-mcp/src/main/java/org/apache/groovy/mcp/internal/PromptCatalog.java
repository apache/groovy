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
package org.apache.groovy.mcp.internal;

import org.apache.groovy.mcp.McpServerConfig;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Prompt templates that tell a model to use compiler facts.
 * The source argument is data inside the prompt, not code the server runs.
 */
public final class PromptCatalog {

    private static final Set<String> EXPLAIN = Set.of("code", "question");
    private static final Set<String> REVIEW = Set.of("code");

    private final int maxSourceChars;

    /**
     * @param config launch limits, including the source cap
     */
    public PromptCatalog(final McpServerConfig config) {
        this.maxSourceChars = config.maxSourceChars();
    }

    /**
     * @return prompt descriptors
     */
    public List<Map<String, Object>> list() {
        return List.of(
                prompt("explain-groovy", "Explain Groovy",
                        "Explain Groovy source from compiler facts, without running it.",
                        List.of(argument("code", "Groovy source.", true),
                                argument("question", "What to explain. Optional.", false))),
                prompt("review-groovy", "Review Groovy",
                        "Review Groovy source from diagnostics and hover, without running it.",
                        List.of(argument("code", "Groovy source.", true)))
        );
    }

    /**
     * @param name prompt name
     * @param arguments argument object, or {@code null}
     * @return a {@code prompts/get} result
     */
    public Map<String, Object> get(final String name, final Object arguments) {
        if ("explain-groovy".equals(name)) {
            ArgumentMap args = ArgumentMap.of(arguments, EXPLAIN);
            return message("explain-groovy", explain(code(args), args.optionalString("question")));
        }
        if ("review-groovy".equals(name)) {
            ArgumentMap args = ArgumentMap.of(arguments, REVIEW);
            return message("review-groovy", review(code(args)));
        }
        throw new ProtocolException(-32602, "unknown prompt");
    }

    private String code(final ArgumentMap args) {
        String source = args.requiredString("code");
        if (source.length() > maxSourceChars) {
            throw new ProtocolException(-32602, "code exceeds the source limit");
        }
        return source;
    }

    private static String explain(final String source, final String question) {
        String asked = question == null || question.isBlank() ? "Explain what this Groovy source does." : question;
        return asked + "\nUse groovy_diagnostics and groovy_describe. Positions are 0-based UTF-16. "
                + "Do not run the source.\n```groovy\n" + source + "\n```";
    }

    private static String review(final String source) {
        return "Review this Groovy source. Call groovy_diagnostics first. "
                + "An empty groovy_rename means the binding is not proven. Do not run the source.\n```groovy\n"
                + source + "\n```";
    }

    private static Map<String, Object> message(final String description, final String text) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type", "text");
        content.put("text", text);
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", content);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("description", description);
        result.put("messages", List.of(message));
        return result;
    }

    private static Map<String, Object> prompt(final String name, final String title, final String description,
                                              final List<Map<String, Object>> arguments) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("title", title);
        map.put("description", description);
        map.put("arguments", arguments);
        Map<String, Object> annotations = new LinkedHashMap<>();
        annotations.put("audience", List.of("assistant"));
        annotations.put("priority", 0.6d);
        map.put("annotations", Collections.unmodifiableMap(annotations));
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, Object> argument(final String name, final String description, final boolean required) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("description", description);
        map.put("required", required);
        return Collections.unmodifiableMap(map);
    }
}
