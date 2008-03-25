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

/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision$
 * @since Groovy 1.1
 *
 * The real interesting part of this example are in the three properties of button:
 *
 *           text: bind(source:textField, sourceProperty:'text'),
 *           margin: bind(source:slider, sourceProperty:'value', converter:{[it, it, it, it] as Insets}),
 *           enabled: bind(source:checkBox, sourceProperty:'selected')
 *
 * This is where the real magic goes on, causing the button to react to the changes
 * in the source widgets values.
 */

import groovy.swing.SwingBuilder
import java.awt.GridBagConstraints as gb
import java.awt.Insets

sb = SwingBuilder.build() {
    frame = frame(defaultCloseOperation:javax.swing.JFrame.DISPOSE_ON_CLOSE) {
        gridBagLayout()

        label("Text:", anchor:gb.WEST, insets:[6,6,3,3] as Insets)
        textField = textField("Change Me!", fill:gb.HORIZONTAL, gridwidth:gb.REMAINDER, insets:[6,3,3,6] as Insets)

        label("Margin:", anchor:gb.WEST, insets:[3,6,3,3] as Insets)
        slider = slider(value:5, fill:gb.HORIZONTAL, gridwidth:gb.REMAINDER, insets:[3,3,3,6] as Insets)

        panel()
        checkBox = checkBox("Enbled", anchor:gb.WEST, gridwidth:gb.REMAINDER, insets:[3,3,3,6] as Insets)

        separator(fill:gb.HORIZONTAL, gridwidth:gb.REMAINDER)

        button(anchor:gb.CENTER, gridwidth:gb.REMAINDER, gridheight:gb.REMAINDER, weightx:1.0, weighty:1.0, insets:[3,6,6,6] as Insets,
            text: bind(source:textField, sourceProperty:'text'),
            margin: bind(source:slider, sourceProperty:'value', converter:{[it, it, it, it] as Insets}),
            enabled: bind(source:checkBox, sourceProperty:'selected')
        )
    }
}

frame.pack()
frame.setSize(frame.width + 100, frame.height + 200)
frame.show()
