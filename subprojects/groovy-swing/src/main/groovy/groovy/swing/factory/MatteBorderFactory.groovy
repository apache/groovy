/*
 * Copyright 2007 the original author or authors.
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
package groovy.swing.factory

import javax.swing.BorderFactory

/**
 * matteBorder requires essentially two parameter, a mat definition and a
 * border size definition.  The value argument can accept either of these:
 * any of a java.awt.Color, a javax.swing.Icon, an integer, or a 4 arg
 * integer array.  The remaining parameter must be define via attributes,
 * either an icon: or color: attribute and a size: or top:, left:, bottom:,
 * and right: attribute.
 */
class MatteBorderFactory extends SwingBorderFactory {

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        builder.context.applyBorderToParent = attributes.remove('parent')
        
        def matte
        def border
        if (attributes.containsKey('icon'))  {
            matte = attributes.remove('icon')
        } else if (attributes.containsKey('color')) {
            matte = attributes.remove('color')
        } else if (value != null) {
            matte = value
        } else {
            throw new RuntimeException("$name must have a matte defined, either as a value argument or as a color: or icon: attribute");
        }

        if (attributes.containsKey('size')) {
            border = attributes.remove('size');
            border = [border, border, border, border]
        } else if (attributes.containsKey('top')) {
            def top = attributes.remove('top')
            def left = attributes.remove('left')
            def bottom = attributes.remove('bottom')
            def right = attributes.remove('right')
            if ((top == null) || (left == null) || (bottom == null) || (right == null)) {
                throw new RuntimeException("In $name if one of top:, left:, bottom: or right: is specified all must be specified")
            }
            border = [top, left, bottom, right]
        } else if (value != null) {
            if (matte == value) {
                throw new RuntimeException("In $name some attributes are required in addition to the value argument")
            }
            if (value instanceof Integer) {
                border = [value, value, value, value]
            } else {
                border = value
            }
        }

        if (attributes) {
            throw new RuntimeException("$name only supports the attributes [ icon: | color:]  [ size: | ( top: left: bottom: right: ) }")
        }

        // spread list and multi-dispatch.  That's groovy!
        return BorderFactory.createMatteBorder(*border, matte)
    }

}