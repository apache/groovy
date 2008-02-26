package groovy.ui.view

import java.awt.BorderLayout
import java.awt.GridBagConstraints
import javax.swing.SwingConstants

statusPanel = panel(constraints: BorderLayout.SOUTH) {
    gridBagLayout()
    separator(constraints:gbc(gridwidth:GridBagConstraints.REMAINDER, fill:GridBagConstraints.HORIZONTAL))
    status = label('Welcome to Groovy.',
        constraints:gbc(weightx:1.0,
            anchor:GridBagConstraints.WEST,
            fill:GridBagConstraints.HORIZONTAL,
            insets: [1,3,1,3])
    )
    separator(orientation:SwingConstants.VERTICAL, constraints:gbc(fill:GridBagConstraints.VERTICAL))
    rowNumAndColNum = label('1:1', constraints:gbc(insets: [1,3,1,3]))
}
