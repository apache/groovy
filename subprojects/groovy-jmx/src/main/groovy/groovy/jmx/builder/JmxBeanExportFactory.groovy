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
package groovy.jmx.builder

/**
 * This factory returns a container node for all other nodes that are used to collect meta data for resources that
 * are exported to the MBeanServer for management.
 * <p>
 * Supported syntax:
 * <pre>
 * def jmx = new JmxBuilder()
 * jmx.export(registrationPolicy:"replace|ignore|error") {
 *     bean(...)
 * }
 * </pre>
 * <p>
 * registrationPolicy indicates how resources will be registered:
 * "replace" - replaces existing bean,  <br>
 * "ignore" - ignores the registration request if bean already exists.<br>
 * "error" - throws error if bean is already registered.
 */
class JmxBeanExportFactory extends AbstractFactory {
    // def server
    def registrationPolicy

    Object newInstance(FactoryBuilderSupport builder, Object nodeName, Object nodeArgs, Map nodeAttribs) {
        registrationPolicy = nodeAttribs?.remove("policy") ?: nodeAttribs?.remove("regPolicy") ?: "replace"
        return []
    }

    boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node, Map nodeAttribs) {
        return true
    }

    boolean isLeaf() {
        return false
    }
}