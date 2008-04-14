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
 * accepts values in leiu of attributes:
 *  int - all of top, left, bottom, right
 *  [int, int, int, int] - top, left, bottom, right
 * accepts attributes when no value present:
 *  top: int, left: int, bottom: int, right: int
 *
 */
class EmptyBorderFactory extends SwingBorderFactory {

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        builder.context.applyBorderToParent = attributes.remove('parent')
        
        if (!attributes) {
            if (value instanceof Integer) {
                return BorderFactory.createEmptyBorder(value, value, value, value)
            } else if (value instanceof List && value.size() == 4) {
                // we need GDK methods on Collection for boolean .any{} and boolean .all{} :(
                boolean ints = true
                value.each {ints = ints & it instanceof Integer}
                if (ints) {
                    return BorderFactory.createEmptyBorder(*value);
                }
            }
            throw new RuntimeException("$name only accepts a single integer or an array of four integers as a value argument");
        }
        if (value == null) {
            int top = attributes.remove("top")
            int left = attributes.remove("left")
            int bottom = attributes.remove("bottom")
            int right = attributes.remove("right")
            if ((top == null) || (top == null) || (top == null) || (top == null) || attributes) {
                throw new RuntimeException("When $name is called it must be call with top:, left:, bottom:, right:, and no other attributes")
            }
            return BorderFactory.createEmptyBorder(top, left, bottom, right);
        }
        throw new RuntimeException("$name cannot be called with both an argulent value and attributes")
    }

}