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
import groovy.swing.SwingBuilder
import static javax.swing.WindowConstants.*
import static java.awt.GridBagConstraints.*

def bean = new ObservableMap([name:'Alice', phone:'719-555-1212', addr:'42 Other Way'])

SwingBuilder.build {
 frame = frame(
       pack:true, 
       show:true,
       defaultCloseOperation:DISPOSE_ON_CLOSE)
 {
  beanModel = model(bean, bind:false)

  gridBagLayout()

  label('Name:', constraints:gbc(insets:[6,6,3,3]))
  textField(text:beanModel.name,
            columns:20,
            gridwidth:REMAINDER,
            fill:HORIZONTAL,
            weightx:1,
            insets:[6,3,3,6])

  label('Phone:', constraints:gbc(insets:[3,6,3,3]))
  textField(text:beanModel.phone,
            columns:20,
            gridwidth:REMAINDER,
            fill:HORIZONTAL,
            weightx:1,
            insets:[3,3,3,6])

  label('Address:', constraints:gbc(insets:[3,6,3,3]))
  textField(text:beanModel.addr,
            columns:20,
            gridwidth:REMAINDER,
            fill:HORIZONTAL,
            weightx:1,
            insets:[3,3,3,6])

  button('Reset', actionPerformed:{beanModel.update()}, 
                  constraints:gbc(gridwidth:2, 
                                  anchor:EAST, 
                                  weightx:1, 
                                  insets:[9,0,0,6]))
  button('Submit', 
         insets:[9,0,0,0],
         actionPerformed: { 
             beanModel.reverseUpdate()
             output.text = ("name = '$bean.name'\nphone = '$bean.phone'\naddr = '$bean.addr'\n\n")
         })

  separator(gridwidth:REMAINDER,
            fill:HORIZONTAL,
            insets:[3,6,3,6])
  label('Output:',
        gridwidth:REMAINDER,
        anchor:WEST,
        insets:[3,6,3,6])
  scrollPane(preferredSize:[100, 100], 
             gridwidth:REMAINDER,
             fill:BOTH,
             weighty:1,
             insets:[3,6,6,6])
  {
   output = textArea()
  }
 }
}
