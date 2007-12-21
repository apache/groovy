package groovy.ui.view

import java.awt.BorderLayout
import java.awt.GridBagConstraints
import javax.swing.SwingConstants

panel(id: 'statusPanel', constraints: BorderLayout.SOUTH) {
    gridBagLayout()
    separator(constraints:gbc(gridwidth:GridBagConstraints.REMAINDER, fill:GridBagConstraints.HORIZONTAL))
    label('Welcome to Groovy.',
        id: 'status',
        constraints:gbc(weightx:1.0,
            anchor:GridBagConstraints.WEST,
            fill:GridBagConstraints.HORIZONTAL,
            insets: [1,3,1,3])
    )
    separator(orientation:SwingConstants.VERTICAL, constraints:gbc(fill:GridBagConstraints.VERTICAL))
    label('1:1',
        id: 'rowNumAndColNum',
        constraints:gbc(insets: [1,3,1,3])
    )
}
