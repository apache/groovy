/*
 * Copyright 2003-2008 the original author or authors.
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

/**
 * Groovy builder that works with Stax processors
 *
 * @author <a href="dejan@nighttale.net">Dejan Bosanac</a>
 * @author Paul King
 */
public class StaxBuilder extends BuilderSupport {

    def writer

    public StaxBuilder(xmlstreamwriter) {
        writer = xmlstreamwriter
    }

    protected createNode(name) {
        createNode(name, null, null)
    }

    protected createNode(name, value) {
        createNode(name, null, value)
    }

    protected createNode(name, Map attributes) {
        createNode(name, attributes, null)
    }

    protected createNode(name, Map attributes, value) {
        writer.writeStartElement(name.toString())
        if (attributes) {
            attributes.each { k, v ->
                writer.writeAttribute(k.toString(), v.toString())
            }
        }
        if (value) {
            writer.writeCharacters(value.toString())
        }
        name
    }

    protected void nodeCompleted(parent, node) {
        writer.writeEndElement()
        if (!parent) {
            writer.writeEndDocument()
            writer.flush()
        }
    }

    protected void setParent(parent, child) {
    }

}
