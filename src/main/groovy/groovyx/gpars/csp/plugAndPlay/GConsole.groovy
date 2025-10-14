// GPars — Groovy Parallel Systems
//
// Copyright © 2008–2010, 2018  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.csp.plugAndPlay

import groovyx.gpars.csp.PAR

import java.awt.BorderLayout
import java.awt.Container
import java.awt.Font
import java.awt.GridLayout
import java.awt.Label

import jcsp.awt.ActiveClosingFrame
import jcsp.awt.ActiveTextArea
import jcsp.awt.ActiveTextEnterField
import jcsp.lang.CSProcess
import jcsp.lang.ChannelInput
import jcsp.lang.ChannelOutput

class GConsole implements CSProcess {

    ChannelInput toConsole
    ChannelOutput fromConsole
    ChannelInput clearInputArea
    String frameLabel = "Groovy Eclipse Console"

    def void run() {
        def main = new ActiveClosingFrame(frameLabel)
        def root = main.activeFrame
        root.layout = new BorderLayout()
        def outLabel = new Label("Output Area", Label.CENTER)
        outLabel.font = new Font("sans-serif", Font.BOLD, 20)
        def inLabel = new Label("Input Area", Label.CENTER)
        inLabel.font = new Font("sans-serif", Font.BOLD, 20)
        def outText = new ActiveTextArea(toConsole, null)
        def inText = new ActiveTextEnterField(clearInputArea, fromConsole)
        def console = new Container()
        console.layout = new GridLayout(4, 1)
        console.add(outLabel)
        console.add(outText)
        console.add(inLabel)
        console.add(inText.activeTextField)
        root.add(console, BorderLayout.CENTER)
        root.pack()
        root.visible = true
        def interfaceProcessList = [main, outText, inText]
        new PAR(interfaceProcessList).run()
    }

}
