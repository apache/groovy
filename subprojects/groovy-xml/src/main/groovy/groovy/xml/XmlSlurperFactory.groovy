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

/**
 * Factory method targetting dynamic contexts which allows a new {@code XmlSlurper} to be created.
 * It is intended to assist with migration from Groovy 2.5.x to Groovy 3+.
 * Code using this factory will not need to change moving from 2.x to 3+.
 * In Groovy 2.5.x, a {@code groovy.util.XmlSlurper} will be returned.
 * For Groovy 3+, a {@code groovy.xml.XmlSlurper} will be returned.
 * For contexts requiring additional static type checking, it is recommended simply to use the
 * normal constructor and change the package to {@code groovy.xml} prior to reaching Groovy 4.
 */
class XmlSlurperFactory {
    static newSlurper(... args) {
        new XmlSlurper(*args)
    }
}
