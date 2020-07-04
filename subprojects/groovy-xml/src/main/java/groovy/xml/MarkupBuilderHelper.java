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
package groovy.xml;

import java.util.HashMap;
import java.util.Map;

/**
 * A helper class for MarkupBuilder.
 */
public class MarkupBuilderHelper {
    private final MarkupBuilder builder;

    /**
     * @param builder the builder to delegate to
     */
    public MarkupBuilderHelper(MarkupBuilder builder) {
        this.builder = builder;
    }

    /**
     * Prints data in the body of the current tag, escaping XML entities.
     * For example: <code>mkp.yield('5 &lt; 7')</code>
     *
     * @param value an Object whose toString() representation is to be printed
     */
    public void yield(Object value) {
        this.yield(value.toString());
    }

    /**
     * Prints data in the body of the current tag, escaping XML entities.
     * For example: <code>mkp.yield('5 &lt; 7')</code>
     *
     * @param value text to print
     */
    public void yield(String value) {
        builder.yield(value, true);
    }

    /**
     * Print data in the body of the current tag.  Does not escape XML entities.
     * For example: <code>mkp.yieldUnescaped('I am &lt;i&gt;happy&lt;/i&gt;!')</code>.
     *
     * @param value an Object whose toString() representation is to be printed
     */
    public void yieldUnescaped(Object value) {
        yieldUnescaped(value.toString());
    }

    /**
     * Print data in the body of the current tag.  Does not escape XML entities.
     * For example: <code>mkp.yieldUnescaped('I am &lt;i&gt;happy&lt;/i&gt;!')</code>.
     *
     * @param value the text or markup to print.
     */
    public void yieldUnescaped(String value) {
        builder.yield(value, false);
    }

    /**
     * Produce a comment in the output.
     * <p>
     * <code>mkp.comment 'string'</code> is equivalent to
     * <code>mkp.yieldUnescaped '&lt;!-- string --&gt;'</code>.
     * To create an element with the name 'comment', you need
     * to supply empty attributes, e.g.:
     * <pre>
     * comment('hello1')
     * </pre>
     * or
     * <pre>
     * mkp.comment('hello1')
     * </pre>
     * will produce:
     * <pre>
     * &lt;!-- hello1 --&gt;
     * </pre>
     * while:
     * <pre>
     * comment('hello2', [:])
     * </pre>
     * will produce:
     * <pre>
     * &lt;comment&gt;hello2&lt;/comment&gt;
     * </pre>
     *
     * @param value the text within the comment.
     */
    public void comment(String value) {
        yieldUnescaped("<!-- " + value + " -->");
    }

    /**
     * Produce an XML declaration in the output.
     * For example:
     * <pre>
     * mkp.xmlDeclaration(version:'1.0')
     * </pre>
     *
     * @param args the attributes for the declaration
     */
    public void xmlDeclaration(Map<String, Object> args) {
        Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
        map.put("xml", args);
        pi(map);
    }

    /**
     * Produce an XML processing instruction in the output.
     * For example:
     * <pre>
     * mkp.pi("xml-stylesheet":[href:"mystyle.css", type:"text/css"])
     * </pre>
     *
     * @param args a map with a single entry whose key is the name of the
     *             processing instruction and whose value is the attributes
     *             for the processing instruction.
     */
    public void pi(Map<String, Map<String, Object>> args) {
        builder.pi(args);
    }

}
