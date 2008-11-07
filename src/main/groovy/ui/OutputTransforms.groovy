/*
 * Copyright 2003-2008 the original author or authors.
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
package groovy.ui

import java.awt.*
import java.awt.image.BufferedImage
import javax.swing.Icon
import javax.swing.ImageIcon
import org.codehaus.groovy.runtime.InvokerHelper

public class OutputTransforms {

    @Lazy static def localTransforms = loadOutputTransforms()

    static def loadOutputTransforms() {
        def transforms = []

        //
        // load user local transforms
        //
        def userHome = new File(System.getProperty('user.home'))
        def groovyDir = new File(userHome, '.groovy')
        def userTransforms = new File(groovyDir, "OutputTransforms.groovy")
        if (userTransforms.exists()) {
            new GroovyShell(transforms:transforms).evaluate(userTransforms)
        }

        //
        // built-in transforms
        //

        // any GUI components, such as  a heavyweight button or a Swing component,
        // gets passed if it has no parent set (tne parent clause is to
        // keep buttons from disappearing from user shown forms)
        transforms << { it -> if ((it instanceof Component) && (it.parent == null)) it }

        // remaining components get printed to an image
        transforms << { it ->
            if (it instanceof javax.swing.JComponent) {
                Dimension d = it.getSize();
                if (d.width == 0) {
                    d = it.getPreferredSize();
                    it.setSize(d);
                }

                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice gs = ge.getDefaultScreenDevice();
                GraphicsConfiguration gc = gs.getDefaultConfiguration();

                BufferedImage image = gc.createCompatibleImage(d.width as int,d.height as int, Transparency.TRANSLUCENT);
                Graphics2D g2 = image.createGraphics();
                it.print(g2)
                g2.dispose()
                new javax.swing.ImageIcon(image)
            }
        }

        // icons get passed, they can be rendered multiple times so no parent check
        transforms << { it -> if (it instanceof Icon) it }

        // Images become ImageIcons
        transforms << { it -> if (it instanceof Image) new ImageIcon(it)}

        // final case, non-nulls just get inspected as strings
        transforms << { it -> if (it != null) "${InvokerHelper.inspect(it)}" }

        return transforms
    }

    static def transformResult(def base, def transforms = localTransforms) {
        for (Closure c : transforms) {
            def result = c(base)
            if (result != null)  {
                return result
            }
        }
        return base
    }

}