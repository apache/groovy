package groovy.swing;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * A sample table model
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class MyTableModel extends AbstractTableModel {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(MyTableModel.class);

    public MyTableModel() {
    }

    final String[] columnNames = { "First Name", "Last Name", "Sport", "# of Years", "Vegetarian" };
    final Object[][] data = { { "Mary", "Campione", "Snowboarding", new Integer(5), new Boolean(false)}, {
            "Alison", "Huml", "Rowing", new Integer(3), new Boolean(true)
            }, {
            "Kathy", "Walrath", "Chasing toddlers", new Integer(2), new Boolean(false)
            }, {
            "Mark", "Andrews", "Speed reading", new Integer(20), new Boolean(true)
            }, {
            "Angela", "Lih", "Teaching high school", new Integer(4), new Boolean(false)
            }
    };

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col < 2) {
            return false;
        }
        else {
            return true;
        }
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        if (log.isDebugEnabled()) {
            log.debug(
                "Setting value at " + row + "," + col + " to " + value + " (an instance of " + value.getClass() + ")");
        }

        if (data[0][col] instanceof Integer && !(value instanceof Integer)) {
            //With JFC/Swing 1.1 and JDK 1.2, we need to create    
            //an Integer from the value; otherwise, the column     
            //switches to contain Strings.  Starting with v 1.3,   
            //the table automatically converts value to an Integer,
            //so you only need the code in the 'else' part of this 
            //'if' block.                                          
            //XXX: See TableEditDemo.java for a better solution!!!
            try {
                data[row][col] = new Integer(value.toString());
                fireTableCellUpdated(row, col);
            }
            catch (NumberFormatException e) {
                log.error("The \"" + getColumnName(col) + "\" column accepts only integer values.");
            }
        }
        else {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }
    }

}