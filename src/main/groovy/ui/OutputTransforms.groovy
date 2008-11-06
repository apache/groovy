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

import java.awt.Component
import java.awt.Image
import javax.swing.Icon
import javax.swing.ImageIcon

public class OutputTransforms {

    @Lazy static List<Closure> localTransforms = loadOutputTransforms()

    static List<Closure> loadOutputTransforms() {
        List<Closure> transforms = []

        //
        // load user local transforms
        //
        def userHome = new File(System.getProperty('user.home'))
        def groovyDir = new File(userHome, '.groovy')
        def userTransforms = new File(groovyDir, "ConsoleOutputTransforms.groovy")
        if (userTransforms.exists()) {
            GroovyShell tempShell  = new GroovyShell()
            tempShell.context.transforms = transforms
            tempShell.evaluate(userTransforms)
            transforms = tempShell.context.transforms
        }

        //
        // built-in transforms
        //

        // any jcomponents, such as  a heavyweight button or a Swing component,
        // gets passed if it has no parent set (tne parent clause is to
        // keep buttons from disappearing from user shown forms)
        transforms += { it -> if ((it instanceof Component) && (it.parent == null)) it }

        // icons get passed, they can be rendered multiple times so no parent check
        transforms += { it -> if (it instanceof Icon) it }

        // Images become ImageIcons
        transforms += { it -> if (it instanceof Image) new ImageIcon(it)}

        // final case, non-nulls just get inspected as strings
        transforms += { it -> if (it != null) "${InvokerHelper.inspect(it)}" }

        return transforms
    }

    static def transformResult(def base, List<Closure> transforms = localTransforms) {
        for (Closure c : transforms) {
            def result = c(base)
            if (result != null)  {
                return result
            }
        }
        return base
    }

}