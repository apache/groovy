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
package groovy.xml.streamingmarkupsupport

class AbstractStreamingBuilder {
    def badTagClosure = {tag, doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, Object[] rest ->
        def uri = pendingNamespaces[prefix]
        if (uri == null) {
            uri = namespaces[prefix]
        }
        throw new GroovyRuntimeException("Tag ${tag} is not allowed in namespace ${uri}")
    }
    def namespaceSetupClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, Object[] rest ->
        attrs.each { key, value ->
            if ( key == "") {
                key = ":"    // marker for default namespace
            }
            value = value.toString()     // in case it's not a string
            if (namespaces[key] != value) {
                pendingNamespaces[key] = value
            }
            if (!namespaceSpecificTags.containsKey(value)) {
                def baseEntry = namespaceSpecificTags[':']
                namespaceSpecificTags[value] = [baseEntry[0], baseEntry[1], [:]].toArray()
            }
        }
    }
    def aliasSetupClosure = {doc, pendingNamespaces, namespaces, namespaceSpecificTags, prefix, attrs, Object[] rest ->
        attrs.each { key, value ->
            if (value instanceof Map) {
                // key is a namespace prefix value is the mapping
                def info = null
                if (namespaces.containsKey(key)) {
                    info = namespaceSpecificTags[namespaces[key]]
                } else if (pendingNamespaces.containsKey(key)) {
                    info = namespaceSpecificTags[pendingNamespaces[key]]
                } else {
                    throw new GroovyRuntimeException("namespace prefix ${key} has not been declared")
                }
                value.each { to, from -> info[2][to] = info[1].curry(from) }
            } else {
                def info = namespaceSpecificTags[':']
                info[2][key] = info[1].curry(value)
            }
        }
    }
    def getNamespaceClosure = {doc, pendingNamespaces, namespaces, Object[] rest -> [namespaces, pendingNamespaces]}

    def toMapString = { Map instruction ->
        def buf = new StringBuilder()
        instruction.each { name, value ->
            if (value.toString().contains('"'))
                buf.append(" $name='$value'")
            else
                buf.append(" $name=\"$value\"")
        }
        return buf.toString()
    }

    def specialTags = ['declareNamespace':namespaceSetupClosure,
                           'declareAlias':aliasSetupClosure,
                          'getNamespaces':getNamespaceClosure]

    def builder = null
}
