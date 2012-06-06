/*
 * Copyright 2003-2010 the original author or authors.
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
package groovy.ui.view

import javax.swing.JComponent
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext
import org.codehaus.groovy.runtime.InvokerHelper

build(Defaults)

// change fonts for vista
if (System.properties['os.version'] =~ /6\./) {
    // Vista/Server 2008 or later
    styles.regular[StyleConstants.FontFamily] = 'Consolas'
    styles[StyleContext.DEFAULT_STYLE][StyleConstants.FontFamily] = 'Consolas'

    // in JDK 1.5 we need to turn on anti-aliasing so consoles looks better
    if (System.properties['java.version'] =~ /^1\.5/) {
        key = InvokerHelper.getProperty('com.sun.java.swing.SwingUtilities2' as Class,
            'AA_TEXT_PROPERTY_KEY')
        addAttributeDelegate {builder, node, attributes ->
            if (node instanceof JComponent) {
                node.putClientProperty(key, new Boolean(true));
            }
        }
    }
}
