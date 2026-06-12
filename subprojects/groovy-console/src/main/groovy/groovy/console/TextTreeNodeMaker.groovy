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
package groovy.console

import groovy.console.ui.AstBrowserNodeMaker
import groovy.transform.CompileStatic

/**
 * Factory for the text nodes displayed by ASTBrowser.
 */
@CompileStatic
class TextTreeNodeMaker implements AstBrowserNodeMaker<TextNode> {
    /**
     * Creates a text node without additional metadata.
     *
     * @param userObject value displayed for the node
     * @return the created text node
     */
    TextNode makeNode(Object userObject) {
        new TextNode(userObject)
    }

    /**
     * Creates a text node with additional metadata.
     *
     * @param userObject value displayed for the node
     * @param properties metadata associated with the node
     * @return the created text node
     */
    TextNode makeNodeWithProperties(Object userObject, List<List<?>> properties) {
        new TextNode(userObject, properties)
    }
}
