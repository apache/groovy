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
package groovy.markdown;

import org.apache.groovy.lang.annotation.Incubating;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A parsed Markdown document, exposed as a list of node maps with convenience
 * accessors for the most common navigation patterns.
 * <p>
 * Each node is a {@link Map} with at least a {@code type} key. The raw structure
 * supports all standard Groovy list/map operations (e.g. {@code findAll},
 * {@code each}, {@code *.text}); the convenience accessors below recursively
 * walk the tree so nested code blocks, links, etc. are included.
 *
 * @since 6.0.0
 */
@Incubating
public class MarkdownDocument implements Iterable<Map<String, Object>> {

    private final List<Map<String, Object>> nodes;

    /**
     * Creates a document view over the parsed top-level Markdown nodes.
     *
     * @param nodes the top-level nodes in document order
     */
    public MarkdownDocument(List<Map<String, Object>> nodes) {
        this.nodes = nodes;
    }

    /**
     * The raw top-level nodes of the document.
     */
    public List<Map<String, Object>> getNodes() {
        return nodes;
    }

    /**
     * Returns an iterator over the document's top-level nodes.
     *
     * @return an iterator for the top-level nodes
     */
    @Override
    public Iterator<Map<String, Object>> iterator() {
        return nodes.iterator();
    }

    /**
     * All fenced and indented code blocks anywhere in the document.
     */
    public List<Map<String, Object>> getCodeBlocks() {
        return findAll(n -> "code_block".equals(n.get("type")));
    }

    /**
     * All headings anywhere in the document, in document order.
     */
    public List<Map<String, Object>> getHeadings() {
        return findAll(n -> "heading".equals(n.get("type")));
    }

    /**
     * All links anywhere in the document.
     */
    public List<Map<String, Object>> getLinks() {
        return findAll(n -> "link".equals(n.get("type")));
    }

    /**
     * All tables anywhere in the document. Tables are only produced when
     * {@code enableTables(true)} was set on the slurper.
     */
    public List<Map<String, Object>> getTables() {
        return findAll(n -> "table".equals(n.get("type")));
    }

    /**
     * Plain-text projection of the document — formatting markers are stripped
     * and block-level elements are separated by newlines.
     */
    public String getText() {
        StringBuilder sb = new StringBuilder();
        appendText(nodes, sb);
        return sb.toString();
    }

    /**
     * Return the nodes under the heading with the given text, up to the next
     * heading of the same or higher level. The heading itself is not included.
     * Returns an empty list if no matching heading is found.
     * <p>
     * Only top-level headings are considered; headings nested inside block
     * quotes or list items are not searched, since the section boundary
     * concept does not naturally extend to such positions.
     */
    public List<Map<String, Object>> section(String headingText) {
        for (int i = 0; i < nodes.size(); i++) {
            Map<String, Object> n = nodes.get(i);
            if (!"heading".equals(n.get("type")) || !headingText.equals(n.get("text"))) {
                continue;
            }
            int level = ((Number) n.get("level")).intValue();
            int end = nodes.size();
            for (int j = i + 1; j < nodes.size(); j++) {
                Map<String, Object> m = nodes.get(j);
                if ("heading".equals(m.get("type"))
                        && ((Number) m.get("level")).intValue() <= level) {
                    end = j;
                    break;
                }
            }
            return new ArrayList<>(nodes.subList(i + 1, end));
        }
        return List.of();
    }

    private List<Map<String, Object>> findAll(Predicate<Map<String, Object>> pred) {
        List<Map<String, Object>> out = new ArrayList<>();
        walk(nodes, n -> {
            if (pred.test(n)) out.add(n);
        });
        return out;
    }

    @SuppressWarnings("unchecked")
    private static void walk(List<Map<String, Object>> nodes, Consumer<Map<String, Object>> visitor) {
        for (Map<String, Object> n : nodes) {
            visitor.accept(n);
            Object children = n.get("children");
            if (children instanceof List) {
                walk((List<Map<String, Object>>) children, visitor);
            }
            Object items = n.get("items");
            if (items instanceof List) {
                walk((List<Map<String, Object>>) items, visitor);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void appendText(List<Map<String, Object>> nodes, StringBuilder sb) {
        for (Map<String, Object> n : nodes) {
            String type = (String) n.get("type");
            switch (type) {
                case "text":
                    sb.append(n.get("value"));
                    break;
                case "inline_code":
                case "code_block":
                case "html_block":
                case "html_inline":
                    sb.append(n.get("text"));
                    break;
                case "soft_line_break":
                    sb.append(' ');
                    break;
                case "hard_line_break":
                    sb.append('\n');
                    break;
                case "table":
                    List<String> headers = (List<String>) n.get("headers");
                    if (headers != null) {
                        sb.append(String.join(" | ", headers)).append('\n');
                    }
                    List<Map<String, String>> rows = (List<Map<String, String>>) n.get("rows");
                    if (rows != null && headers != null) {
                        for (Map<String, String> row : rows) {
                            for (int i = 0; i < headers.size(); i++) {
                                if (i > 0) sb.append(" | ");
                                String v = row.get(headers.get(i));
                                sb.append(v == null ? "" : v);
                            }
                            sb.append('\n');
                        }
                    }
                    break;
                default:
                    Object children = n.get("children");
                    if (children instanceof List) {
                        appendText((List<Map<String, Object>>) children, sb);
                    }
                    Object items = n.get("items");
                    if (items instanceof List) {
                        appendText((List<Map<String, Object>>) items, sb);
                    }
            }
            if ("heading".equals(type) || "paragraph".equals(type) || "code_block".equals(type)
                    || "list_item".equals(type) || "block_quote".equals(type)) {
                sb.append('\n');
            }
        }
    }
}
