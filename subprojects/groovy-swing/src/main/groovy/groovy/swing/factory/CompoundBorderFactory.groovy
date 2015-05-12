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

import javax.swing.border.Border
import javax.swing.border.CompoundBorder

class CompoundBorderFactory extends SwingBorderFactory {

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        builder.context.applyBorderToParent = attributes.remove('parent')

        Border border  = null
        if (value instanceof List) {
            switch (value.size()) {
                case 0:
                    throw new RuntimeException("$name does not accept an empty array as an value argument")
                case 1:
                    border = value[0]
                    break
                case 2:
                    border = new CompoundBorder(value[0], value[1])
                    break
                case 3:
                default:
                    border = new CompoundBorder(value[0], value[1])
                    border = value[2..-1].inject(border) {that, it -> new CompoundBorder(that, it) }
                    break;
            }
        }

        if (!border && attributes) {
            if (value) {
                throw new RuntimeException("$name only accepts an array of borders as a value argument")
            }
            def inner = attributes.remove("inner")
            def outer = attributes.remove("outer")
            if (inner instanceof Border && outer instanceof Border) {
                border = new CompoundBorder(outer, inner)
            }
        }

        if (!border) {
            throw new RuntimeException("$name only accepts an array of javax.swing.border.Border or an inner: and outer: attribute")
        }

        return border
    }
}