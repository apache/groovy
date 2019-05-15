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
package groovy.ui

import groovy.transform.CompileStatic
import org.codehaus.groovy.runtime.InvokerHelper

import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JComponent
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.GraphicsConfiguration
import java.awt.GraphicsDevice
import java.awt.GraphicsEnvironment
import java.awt.Image
import java.awt.Transparency
import java.awt.Window
import java.awt.image.BufferedImage

@CompileStatic
@Deprecated
class OutputTransforms {

    @Lazy static List<Closure> localTransforms = loadOutputTransforms()

    static List<Closure> loadOutputTransforms() {
        def transforms = []

        //
        // load user local transforms
        //
        def userHome = new File(System.getProperty('user.home'))
        def groovyDir = new File(userHome, '.groovy')
        def userTransforms = new File(groovyDir, "OutputTransforms.groovy")
        if (userTransforms.exists()) {
            GroovyShell shell = new GroovyShell()
            shell.setVariable('transforms', transforms)
            shell.evaluate(userTransforms)
        }

        //
        // built-in transforms
        //

        // any non-window GUI components, such as  a heavyweight button or a
        // Swing component, gets passed if it has no parent set (the parent
        // clause is to keep buttons from disappearing from user shown forms)
        transforms << { it -> if ((it instanceof Component) && !(it instanceof Window) && (it.parent == null)) it }

        // remaining components get printed to an image
        transforms << { it ->
            if (it instanceof JComponent) {
                Dimension d = it.size
                if (d.width == 0) {
                    d = it.preferredSize
                    it.size = d
                }

                GraphicsEnvironment ge = GraphicsEnvironment.localGraphicsEnvironment
                GraphicsDevice gs = ge.defaultScreenDevice
                GraphicsConfiguration gc = gs.defaultConfiguration

                BufferedImage image = gc.createCompatibleImage(d.width as int, d.height as int, Transparency.TRANSLUCENT)
                Graphics2D g2 = image.createGraphics()
                it.print(g2)
                g2.dispose()
                new ImageIcon(image)
            }
        }

        // icons get passed, they can be rendered multiple times so no parent check
        transforms << { it -> if (it instanceof Icon) it }

        // Images become ImageIcons
        transforms << { it -> if (it instanceof Image) new ImageIcon(it)}

        // final case, non-nulls just get inspected as strings
        transforms << { it -> if (it != null) "${InvokerHelper.inspect(it)}" }

        return (List<Closure>) transforms
    }

    static transformResult(base, List<Closure> transforms = localTransforms) {
        for (Closure c : transforms) {
            def result = c(base as Object)
            if (result != null)  {
                return result
            }
        }
        return base
    }
}
