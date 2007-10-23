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

import javax.swing.border.LineBorder

/**
 * accepts attributes:<br />
 * color: java.awt.Color <br/>
 * color: java.awt.Color, thickness: int <br/>
 * color: java.awt.Color, thickness: int, roundedBorders: boolean <br/>
 *
 */
class LineBorderFactory extends SwingBorderFactory {

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        builder.context.applyBorderToParent = attributes.remove('parent')
        
        def color = attributes.remove("color")
        if (color == null) {
            throw new RuntimeException("color: is a required attribute for $name")
        }
        def thickness = attributes.remove("thickness")
        if (thickness == null) {
            thickness = 1
        }
        def roundedCorners = attributes.remove("roundedCorners")
        if (roundedCorners == null) {
            roundedCorners = false
        }
        if (attributes) {
            throw new RuntimeException("$name does not know how to handle the remaining attibutes: ${attributes.keySet()}")
        }

        return new LineBorder(color, thickness, roundedCorners);
    }

}