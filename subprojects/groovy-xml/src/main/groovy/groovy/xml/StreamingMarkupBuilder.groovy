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
package groovy.xml

import groovy.xml.streamingmarkupsupport.AbstractStreamingBuilder
import groovy.xml.streamingmarkupsupport.BaseMarkupBuilder
import groovy.xml.streamingmarkupsupport.StreamingMarkupWriter

/**
 * A builder class for creating XML markup.  This implementation uses a
 * {@link StreamingMarkupWriter} to handle output.
 * <p>
 * Example:
 * <pre>System.out << new StreamingMarkupBuilder().bind {
 *   root {
 *     a( a1:'one' ) {
 *       b { mkp.yield( '3 < 5' ) }
 *       c( a2:'two', 'blah' )
 *     }
 *   }
 * }</pre>
 * Will output the following String, without newlines or indentation:  
 * <pre>&lt;root&gt;
 *   &lt;a a1='one'&gt;
 *     &lt;b&gt;3 &amp;lt; 5&lt;/b&gt;
 *     &lt;c a2='two'&gt;blah&lt;/c&gt;
 *   &lt;/a&gt;
 * &lt;/root&gt;</pre>
 * Notes:
 * <ul>
 *     <li>that <code>mkp</code> is a special namespace used to escape
 * away from the normal building mode of the builder and get access
 * to helper markup methods 'yield', 'pi', 'comment', 'out',
 * 'namespaces', 'xmlDeclaration' and 'yieldUnescaped'.
 * </li>
 *     <li>Note that tab, newline and carriage return characters are escaped within attributes, i.e. will become &#09;, &#10; and &#13; respectively</li>
 * </ul>
 */
class StreamingMarkupBuilder extends AbstractStreamingBuilder {
    boolean useDoubleQuotes = false
    boolean expandEmptyElements = false
    def getQt() { useDoubleQuotes ? '"' : "'" }
    def pendingStack = []
    
    /**
     * Invoked by calling <code>mkp.comment</code>
     */
    def commentClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out ->
        out.unescaped() << "<!--"
        out.escaped() << body
        out.unescaped() << "-->"
    }

    /**
     * Invoked by calling <code>mkp.pi</code>
     */
    def piClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out ->
        attrs.each {target, instruction ->
            out.unescaped() << "<?"
            if (instruction instanceof Map) {
                out.unescaped() << target
                out.unescaped() << toMapStringClosure(instruction) { value ->
                    def valueStr = value.toString()
                    valueStr.contains('\'') || (useDoubleQuotes && !valueStr.contains('"'))
                }
            } else {
                out.unescaped() << "$target $instruction"
            }
            out.unescaped() << "?>"
        }
    }

    /**
     * Invoked by calling <code>mkp.xmlDeclaration</code>
     */
    def declarationClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out ->
        out.unescaped() << '<?xml version=' + qt + '1.0' + qt
        if (out.encodingKnown) out.escaped() << " encoding=" + qt + out.encoding + qt
        out.unescaped() << '?>\n'
    }

    /**
     * Invoked by calling <code>mkp.yield</code>.  Used to render text to the 
     * output stream.  Any XML reserved characters will be escaped to ensure
     * well-formedness.
     */
    def noopClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out ->
        body.each {
            if (it instanceof Closure) {
                def body1 = it.clone()
                body1.delegate = doc
                body1(doc)
            } else if (it instanceof Buildable) {
                it.build(doc)
            } else {
                out.escaped() << it
            }
        }
    }
    
    /**
     * Invoked by calling <code>mkp.yieldUnescaped</code>.  Used to render 
     * literal text or markup to the output stream.  No escaping is done on the
     * output.
     */
    def unescapedClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out ->
        out.unescaped() << body
    }
    
    def tagClosure = {tag, doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out ->
        boolean pendingIsDefaultNamespace = pendingNamespaces.containsKey(prefix) && !pendingNamespaces[prefix]
        if (prefix != "") {
            if (!(namespaces.containsKey(prefix) || pendingNamespaces.containsKey(prefix))) {
                throw new GroovyRuntimeException("Namespace prefix: ${prefix} is not bound to a URI")
            }
            if (prefix != ":" && !pendingIsDefaultNamespace) tag = prefix + ":" + tag
        }

        out = out.unescaped() << "<${tag}"

        attrs.each {key, value ->
            if (key.contains('$')) {
                def parts = key.tokenize('$')
                String localpart = parts[1].contains("}") ? parts[1].tokenize("}")[1] : parts[1]
                if (namespaces.containsKey(parts[0]) || pendingNamespaces.containsKey(parts[0])) {
                    key = parts[0] + ":" + localpart
                } else {
                    throw new GroovyRuntimeException("bad attribute namespace tag: ${parts[0]} in ${key}")
                }
            }

            out << " ${key}=" + qt
            out.writingAttribute = true
            "${value}".build(doc)
            out.writingAttribute = false
            out << qt
        }

        def hiddenNamespaces = [:]

        pendingNamespaces.each { key, value ->
            if (value) {
                hiddenNamespaces[key] = namespaces[key]
                namespaces[key] = value
                out << ((key == ":") ? " xmlns=" + qt : " xmlns:${key}=" + qt)
                out.writingAttribute = true
                "${value}".build(doc)
                out.writingAttribute = false
                out << qt
            }
        }

        if (body == null && !expandEmptyElements) {
            out << "/>"
        } else {
            out << ">"

            pendingStack.add pendingNamespaces.clone()
            pendingNamespaces.clear()

            body.each {
                if (it instanceof Closure) {
                    def body1 = it.clone()
                    body1.delegate = doc
                    body1(doc)
                } else if (it instanceof Buildable) {
                    it.build(doc)
                } else {
                    out.escaped() << it
                }
            }

            pendingNamespaces.clear()
            pendingNamespaces.putAll pendingStack.pop()

            out << "</${tag}>"
        }

        hiddenNamespaces.each {key, value ->
            if (value == null) {
                namespaces.remove key
            } else {
                namespaces[key] = value
            }
        }
    }

    def builder = null

    StreamingMarkupBuilder() {
        specialTags.putAll(
                ['yield': noopClosure,
                'yieldUnescaped': unescapedClosure,
                'xmlDeclaration': declarationClosure,
                'comment': commentClosure,
                'pi': piClosure])

        def nsSpecificTags = [':': [tagClosure, tagClosure, [:]], // the default namespace
        'http://www.w3.org/XML/1998/namespace': [tagClosure, tagClosure, [:]],
        'http://www.codehaus.org/Groovy/markup/keywords': [badTagClosure, tagClosure, specialTags]]

        this.builder = new BaseMarkupBuilder(nsSpecificTags)
    }

    def encoding = null

    /**
     * Returns a {@link Writable} object, which may be used to render
     * the markup directly to a String, or send the output to a stream.
     * <p>
     * Examples:
     * <pre>
     * // get the markup as a string:
     * new StreamingMarkupBuilder().bind { div { out << "hello world" } }.toString()
     * 
     * // send the output directly to a file:
     * new StreamingMarkupBuilder().bind { div { out << "hello world" } } \
     *      .writeTo( new File('myFile.xml').newWriter() )
     * </pre>
     *
     * @return a {@link Writable} to render the markup
     */
    public bind(closure) {
        def boundClosure = this.builder.bind(closure);
        def enc = encoding; // take a snapshot of the encoding when the closure is bound to the builder

        {out ->
            out = new StreamingMarkupWriter(out, enc, useDoubleQuotes)
            boundClosure.trigger = out
            out.flush()
        }.asWritable()
    }

    /**
     * Convenience method for binding a single node.
     * The call <code>bindNode(node)</code> is equivalent to <code>bind{ out << node }</code>.
     * Returns a {@link Writable} object, which may be used to render
     * the markup directly to a String, or send the output to a stream.
     *
     * @see #bind(Closure)
     * @return a {@link Writable} to render the markup
     */
    public bindNode(node) {
        bind { out << node }
    }
}
