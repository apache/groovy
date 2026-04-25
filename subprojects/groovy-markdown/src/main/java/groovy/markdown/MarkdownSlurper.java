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
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.commonmark.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parses <a href="https://commonmark.org/">CommonMark</a> Markdown into a
 * {@link MarkdownDocument} backed by nested lists and maps.
 * <p>
 * Usage:
 * <pre><code class="language-groovy groovyTestCase">
 * def doc = new groovy.markdown.MarkdownSlurper().parseText('# Hello')
 * assert doc.headings[0].text == 'Hello'
 * </code></pre>
 *
 * GFM-style tables are supported via an optional extension. Call
 * {@link #enableTables(boolean) enableTables(true)} after adding
 * {@code org.commonmark:commonmark-ext-gfm-tables} to the runtime classpath.
 *
 * @since 6.0.0
 */
@Incubating
public class MarkdownSlurper {

    private static final String TABLES_EXT_CLASS = "org.commonmark.ext.gfm.tables.TablesExtension";

    private boolean tablesEnabled;

    /**
     * Enable GFM-style tables. Requires {@code commonmark-ext-gfm-tables} on the classpath.
     *
     * @param enable whether to enable table parsing
     * @return this slurper for chaining
     * @throws MarkdownRuntimeException if {@code enable} is true but the extension jar is missing
     */
    public MarkdownSlurper enableTables(boolean enable) {
        if (enable) {
            try {
                Class.forName(TABLES_EXT_CLASS);
            } catch (ClassNotFoundException | LinkageError e) {
                throw new MarkdownRuntimeException(
                        "GFM tables extension not on classpath. Add 'org.commonmark:commonmark-ext-gfm-tables' to enable tables.",
                        e);
            }
        }
        this.tablesEnabled = enable;
        return this;
    }

    public MarkdownDocument parseText(String md) {
        if (md == null || md.isEmpty()) {
            return new MarkdownDocument(List.of());
        }
        return parse(new StringReader(md));
    }

    public MarkdownDocument parse(Reader reader) {
        try {
            Node doc = buildParser().parseReader(reader);
            return new MarkdownDocument(blocksToList(doc));
        } catch (IOException e) {
            throw new MarkdownRuntimeException(e);
        }
    }

    public MarkdownDocument parse(InputStream stream) {
        return parse(new InputStreamReader(stream));
    }

    public MarkdownDocument parse(File file) throws IOException {
        return parse(file.toPath());
    }

    public MarkdownDocument parse(Path path) throws IOException {
        try (InputStream stream = Files.newInputStream(path)) {
            return parse(new InputStreamReader(stream));
        }
    }

    private Parser buildParser() {
        Parser.Builder b = Parser.builder();
        if (tablesEnabled) {
            b.extensions(TableSupport.extensions());
        }
        return b.build();
    }

    private List<Map<String, Object>> blocksToList(Node parent) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Node n = parent.getFirstChild(); n != null; n = n.getNext()) {
            result.add(nodeToMap(n));
        }
        return result;
    }

    private Map<String, Object> nodeToMap(Node node) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (node instanceof Heading h) {
            m.put("type", "heading");
            m.put("level", h.getLevel());
            m.put("text", textOf(h));
            m.put("children", blocksToList(h));
        } else if (node instanceof Paragraph) {
            m.put("type", "paragraph");
            m.put("children", blocksToList(node));
        } else if (node instanceof FencedCodeBlock fcb) {
            m.put("type", "code_block");
            m.put("lang", fcb.getInfo() == null ? "" : fcb.getInfo());
            m.put("text", fcb.getLiteral());
        } else if (node instanceof IndentedCodeBlock) {
            m.put("type", "code_block");
            m.put("lang", "");
            m.put("text", ((IndentedCodeBlock) node).getLiteral());
        } else if (node instanceof BulletList) {
            m.put("type", "list");
            m.put("ordered", false);
            m.put("items", listItems(node));
        } else if (node instanceof OrderedList ol) {
            m.put("type", "list");
            m.put("ordered", true);
            Integer start = ol.getMarkerStartNumber();
            m.put("start", start == null ? 1 : start);
            m.put("items", listItems(node));
        } else if (node instanceof ListItem) {
            m.put("type", "list_item");
            m.put("text", textOf(node));
            m.put("children", blocksToList(node));
        } else if (node instanceof BlockQuote) {
            m.put("type", "block_quote");
            m.put("children", blocksToList(node));
        } else if (node instanceof ThematicBreak) {
            m.put("type", "thematic_break");
        } else if (node instanceof HtmlBlock) {
            m.put("type", "html_block");
            m.put("text", ((HtmlBlock) node).getLiteral());
        } else if (node instanceof HtmlInline) {
            m.put("type", "html_inline");
            m.put("text", ((HtmlInline) node).getLiteral());
        } else if (node instanceof Text) {
            m.put("type", "text");
            m.put("value", ((Text) node).getLiteral());
        } else if (node instanceof Code) {
            m.put("type", "inline_code");
            m.put("text", ((Code) node).getLiteral());
        } else if (node instanceof Emphasis) {
            m.put("type", "emphasis");
            m.put("children", blocksToList(node));
        } else if (node instanceof StrongEmphasis) {
            m.put("type", "strong");
            m.put("children", blocksToList(node));
        } else if (node instanceof Link l) {
            m.put("type", "link");
            m.put("href", l.getDestination());
            m.put("title", l.getTitle());
            m.put("text", textOf(l));
            m.put("children", blocksToList(l));
        } else if (node instanceof Image img) {
            m.put("type", "image");
            m.put("src", img.getDestination());
            m.put("title", img.getTitle());
            m.put("alt", textOf(img));
        } else if (node instanceof HardLineBreak) {
            m.put("type", "hard_line_break");
        } else if (node instanceof SoftLineBreak) {
            m.put("type", "soft_line_break");
        } else {
            if (tablesEnabled) {
                Map<String, Object> tableMap = TableSupport.tryConvertTable(node);
                if (tableMap != null) return tableMap;
            }
            m.put("type", node.getClass().getSimpleName().toLowerCase(Locale.ROOT));
            List<Map<String, Object>> children = blocksToList(node);
            if (!children.isEmpty()) m.put("children", children);
        }
        return m;
    }

    private List<Map<String, Object>> listItems(Node listNode) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (Node n = listNode.getFirstChild(); n != null; n = n.getNext()) {
            if (n instanceof ListItem) items.add(nodeToMap(n));
        }
        return items;
    }

    static String textOf(Node node) {
        StringBuilder sb = new StringBuilder();
        appendText(node, sb);
        return sb.toString();
    }

    private static void appendText(Node node, StringBuilder sb) {
        for (Node n = node.getFirstChild(); n != null; n = n.getNext()) {
            if (n instanceof Text) {
                sb.append(((Text) n).getLiteral());
            } else if (n instanceof Code) {
                sb.append(((Code) n).getLiteral());
            } else if (n instanceof HardLineBreak || n instanceof SoftLineBreak) {
                sb.append(' ');
            } else {
                appendText(n, sb);
            }
        }
    }
}
