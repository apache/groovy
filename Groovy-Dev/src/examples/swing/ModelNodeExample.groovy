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
