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
package groovy.swing.factory

import javax.swing.*
import java.awt.*

class EtchedBorderFactory extends SwingBorderFactory {

    final int type;

    public EtchedBorderFactory(int newType) {
        type = newType;
    }

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        builder.context.applyBorderToParent = attributes.remove('parent')
        // no if-else-if chain so that we can have one attribute failure block
        if (attributes.containsKey("highlight")) {
            Color highlight = attributes.remove("highlight")
            Color shadow = attributes.remove("shadow")
            if (highlight && shadow && !attributes) {
                return BorderFactory.createEtchedBorder(type, highlight, shadow);
            }
        }
        if (attributes) {
            throw new RuntimeException("$name only accepts no attributes, or highlight: and shadow: attributes")
        }
        return BorderFactory.createEtchedBorder(type);
    }

}