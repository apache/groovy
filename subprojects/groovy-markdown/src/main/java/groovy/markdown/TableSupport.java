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

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableBody;
import org.commonmark.ext.gfm.tables.TableCell;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Holds all references to the optional GFM tables extension. Loading this class
 * triggers resolution of the extension types, so {@link MarkdownSlurper} only
 * touches it after confirming the extension jar is on the classpath.
 */
final class TableSupport {

    private TableSupport() {
    }

    /**
     * Creates the CommonMark extensions required to parse GFM-style tables.
     *
     * @return the table parsing extensions
     */
    static List<Extension> extensions() {
        return List.of(TablesExtension.create());
    }

    /**
     * If {@code node} is a {@link TableBlock}, convert it to a node map; otherwise return null.
     */
    static Map<String, Object> tryConvertTable(Node node) {
        if (!(node instanceof TableBlock)) return null;
        return convertTable((TableBlock) node);
    }

    private static Map<String, Object> convertTable(TableBlock block) {
        List<String> headers = new ArrayList<>();
        List<String> alignments = new ArrayList<>();
        List<Map<String, String>> rows = new ArrayList<>();
        for (Node child = block.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof TableHead) {
                TableRow headRow = firstChildOfType(child, TableRow.class);
                if (headRow != null) {
                    for (Node cell = headRow.getFirstChild(); cell != null; cell = cell.getNext()) {
                        if (cell instanceof TableCell tc) {
                            headers.add(MarkdownSlurper.textOf(tc));
                            TableCell.Alignment a = tc.getAlignment();
                            alignments.add(a == null ? null : a.name().toLowerCase(Locale.ROOT));
                        }
                    }
                }
            } else if (child instanceof TableBody) {
                for (Node row = child.getFirstChild(); row != null; row = row.getNext()) {
                    if (row instanceof TableRow) {
                        Map<String, String> rowMap = new LinkedHashMap<>();
                        int i = 0;
                        for (Node cell = row.getFirstChild(); cell != null; cell = cell.getNext()) {
                            if (cell instanceof TableCell) {
                                String key = i < headers.size() ? headers.get(i) : "col" + (i + 1);
                                rowMap.put(key, MarkdownSlurper.textOf(cell));
                                i++;
                            }
                        }
                        rows.add(rowMap);
                    }
                }
            }
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", "table");
        m.put("headers", headers);
        m.put("alignments", alignments);
        m.put("rows", rows);
        return m;
    }

    private static <T extends Node> T firstChildOfType(Node parent, Class<T> type) {
        for (Node n = parent.getFirstChild(); n != null; n = n.getNext()) {
            if (type.isInstance(n)) return type.cast(n);
        }
        return null;
    }
}
