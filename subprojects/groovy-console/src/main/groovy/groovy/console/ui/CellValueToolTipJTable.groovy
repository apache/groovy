package groovy.console.ui

import javax.swing.JTable
import java.awt.event.MouseEvent

class CellValueToolTipJTable extends JTable {
    public String getToolTipText(MouseEvent me) {
        int viewRowIndex = rowAtPoint(me.point)
        int viewColumnIndex = columnAtPoint(me.point)

        def value = getValueAt(viewRowIndex, viewColumnIndex)

        return (value != null ? String.valueOf(value) : null)
    }
}
