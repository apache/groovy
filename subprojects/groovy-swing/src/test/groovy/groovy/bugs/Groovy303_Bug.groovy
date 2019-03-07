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
package groovy.bugs

import java.awt.*
import java.awt.event.*
import javax.swing.*

class Groovy303_Bug extends GroovySwingTestCase {
    void testBug() {
      testInEDT {
        def scholastic = new Scholastic()
        scholastic.createUI()
      }
    }
}

class Scholastic implements ActionListener {

    void createUI() {
        println('createUI called')
        def frame = new JFrame("Hello World")
        def contents = frame.getContentPane()
        def pane = new JPanel()
        pane.setLayout(new BorderLayout())
        def button = new JButton("A button")
        button.addActionListener(this)
        pane.add(button, BorderLayout.CENTER)
        contents.add(pane)
        frame.setSize(100, 100)
        //frame.setVisible(true)
        button.doClick()
    }

    void actionPerformed(ActionEvent event) {
        println "hello"
    }
}
