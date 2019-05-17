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

import groovy.namespace.QName;

/**
 * A simple helper class which acts as a factory of {@link QName} instances.
 * 
 */
public class Namespace {

    private String uri;
    private String prefix;

    public Namespace() {
    }

    public Namespace(String uri) {
        this.uri = uri.trim();
    }

    public Namespace(String uri, String prefix) {
        this.uri = uri.trim();
        this.prefix = prefix.trim();
    }

    /**
     * Returns the QName for the given localName.
     * 
     * @param localName
     *            the local name within this
     */
    public QName get(String localName) {
        if (uri != null && uri.length() > 0) {
            if (prefix != null) {
                return new QName(uri, localName, prefix);
            }
            else {
                return new QName(uri, localName);
            }
        }
        else {
            return new QName(localName);
        }
    }

    /**
     * Returns the prefix mapped to this namespace
     * 
     * @return the prefix assigned to this namespace or null if no namespace is
     *         mapped.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the URI of this namespace
     * 
     * @return the URI of this namespace
     */
    public String getUri() {
        return uri;
    }

}
