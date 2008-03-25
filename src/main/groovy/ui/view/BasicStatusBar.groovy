package groovy.ui.view

import java.awt.BorderLayout
import java.awt.GridBagConstraints
import javax.swing.SwingConstants

statusPanel = panel(constraints: BorderLayout.SOUTH) {
    gridBagLayout()
    separator(gridwidth:GridBagConstraints.REMAINDER, fill:GridBagConstraints.HORIZONTAL)
    status = label('Welcome to Groovy.',
        weightx:1.0,
        anchor:GridBagConstraints.WEST,
        fill:GridBagConstraints.HORIZONTAL,
        insets: [1,3,1,3])
    separator(orientation:SwingConstants.VERTICAL, fill:GridBagConstraints.VERTICAL)
    rowNumAndColNum = label('1:1', insets: [1,3,1,3])
}
