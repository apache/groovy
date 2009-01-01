/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.xml

import groovy.xml.streamingmarkupsupport.AbstractStreamingBuilder
import groovy.xml.streamingmarkupsupport.StreamingMarkupWriter
import groovy.xml.streamingmarkupsupport.BaseMarkupBuilder

/**
 * <p>A builder class for creating XML markup.  This implementation uses a 
 * {@link groovy.lang.Writer} to handle output.</p>
 * 
 * <p>Example:</p>
 * <pre>new StreamingMarkupBuilder().bind {
 *   root {
 *     a( a1:'one' ) {
 *       b { mkp.yield( '3 < 5' ) }
 *       c( a2:'two', 'blah' )
 *     }
 *   }
 * }.toString()</pre>
 * Will return the following String, without newlines or indentation:  
 * <pre>&lt;root&gt;
 *   &lt;a a1='one'&gt;
 *     &lt;b&gt;3 &amp;lt; 5&lt;/b&gt;
 *     &lt;c a2='two'&gt;blah&lt;/c&gt;
 *   &lt;/a&gt;
 * &lt;/root&gt;</pre> 
 *
 */
class StreamingMarkupBuilder extends AbstractStreamingBuilder {
    def pendingStack = []
    def commentClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out ->
        out.unescaped() << "<!--"
        out.escaped() << body
        out.unescaped() << "-->"
    }
    def piClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out ->
        attrs.each {target, instruction ->
            out.unescaped() << "<?"
            if (instruction instanceof Map) {
                out.unescaped() << target
                instruction.each {name, value ->
                    if (value.toString().contains('"')) {
                        out.unescaped() << " $name='$value'"
                    } else {
                        out.unescaped() << " $name=\"$value\""
                    }
                }
            } else {
                out.unescaped() << "$target $instruction"
            }
            out.unescaped() << "?>"
        }
    }
    def declarationClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out ->
        out.unescaped() << '<?xml version="1.0"'
        if (out.encodingKnown) out.escaped() << " encoding=\"${out.encoding}\""
        out.unescaped() << '?>\n'
    }
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
    def unescapedClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out ->
        out.unescaped() << body
    }
    def tagClosure = {tag, doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, body, out ->
        if (prefix != "") {
            if (!(namespaces.containsKey(prefix) || pendingNamespaces.containsKey(prefix))) {
                throw new GroovyRuntimeException("Namespace prefix: ${prefix} is not bound to a URI")
            }

            if (prefix != ":") tag = prefix + ":" + tag
        }

        out = out.unescaped() << "<${tag}"

        attrs.each {key, value ->
            if (key.contains('$')) {
                def parts = key.tokenize('$')

                if (namespaces.containsKey(parts[0]) || pendingNamespaces.containsKey(parts[0])) {
                    key = parts[0] + ":" + parts[1]
                } else {
                    throw new GroovyRuntimeException("bad attribute namespace tag: ${parts[0]} in ${key}")
                }
            }

            out << " ${key}='"
            out.writingAttribute = true
            "${value}".build(doc)
            out.writingAttribute = false
            out << "'"
        }

        def hiddenNamespaces = [:]

        pendingNamespaces.each {key, value ->
            hiddenNamespaces[key] = namespaces[key]
            namespaces[key] = value
            out << ((key == ":") ? " xmlns='" : " xmlns:${key}='")
            out.writingAttribute = true
            "${value}".build(doc)
            out.writingAttribute = false
            out << "'"
        }

        if (body == null) {
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
     * <p>Returns a {@link Writable} object, which may be used to render
     * the markup directly to a String, or send the output to a stream.</p>
     * <p>Examples:</p>
     * <pre>
     * // get the markup as a string:
     * new StreamingMarkupBuilder.bind { div { out << "hello world" } }.toString()
     * 
     * // send the output directly to a file:
     * new StreamingMarkupBuilder.bind { div { out << "hello world" } } \
     * 	 .writeTo( new File('myFile.xml').newWriter() )
     * </pre>
     * @return a {@link Writable} to render the markup
     */
    public bind(closure) {
        def boundClosure = this.builder.bind(closure);
        def enc = encoding; // take a snapshot of the encoding when the closure is bound to the builder

        {out ->
            out = new StreamingMarkupWriter(out, enc)
            boundClosure.trigger = out
            out.flush()
        }.asWritable()
    }
}
