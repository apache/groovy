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
package groovy.text.markup

/**
 * Adapts Grails taglibs for use in the template engine. An adapter may be created like this:
 *
 * <pre>
 *     def model = [:]
 *     def tpl = template.make(model)
 *     model.g = new TagLibAdapter(tpl)
 *     model.g.registerTagLib(SimpleTagLib)
 * </pre>
 *
 * where <i>SimpleTagLib</i> is:
 *
 * <pre>
 *     class SimpleTagLib {
 *        def emoticon = { attrs, body ->
 *            out << body() << (attrs.happy == 'true' ? " :-)" : " :-(")
 *        }
 *     }
 * </pre>
 *
 * Then it can be used inside a template like this:
 *
 * <pre>
 *     g.emoticon(happy:'true') { 'Hi John' }
 * </pre>
 *
 * Performance-wise, it would be better to reimplement the taglib, but this makes it easier to reuse
 * existing code.
 */
class TagLibAdapter {
    private final BaseTemplate template
    private final List<Object> tagLibs = []

    public TagLibAdapter(BaseTemplate tpl) {
        this.template = tpl
    }

    public void registerTagLib(Class tagLibClass) {
        tagLibs.add(tagLibClass.newInstance())
    }

    public void registerTagLib(Object tagLib) {
        tagLibs.add(tagLib)
    }

    public Object methodMissing(String name, args) {
        for (Object tagLib : tagLibs) {
            def p = tagLib."$name"
            if (p instanceof Closure) {
                def clone = p.rehydrate(template, template, template)
                return clone.call(*args)
            }
        }
        throw new MissingMethodException(name, TagLibAdapter, args)
    }
}
