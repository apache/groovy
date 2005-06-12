package groovy.model

class TableModelTest extends GroovyTestCase {
    
    void testTableModel() {
        def list = [ ['name':'James', 'location':'London'], ['name':'Bob', 'location':'Atlanta']]
        
        def listModel = new ValueHolder(list)
        
        def model = new DefaultTableModel(listModel)
        def rowModel = model.getRowModel()
        model.addColumn(new DefaultTableColumn("Name", new PropertyModel(rowModel, "name")))
        model.addColumn(new DefaultTableColumn("Location", new PropertyModel(rowModel, "location")))
        
        assert model.getRowCount() == 2
        assert model.getColumnCount() == 2
        assertValueAt(model, 0, 0, 'James')
        assertValueAt(model, 0, 1, 'London')
        assertValueAt(model, 1, 0, 'Bob')
        assertValueAt(model, 1, 1, 'Atlanta')
        
        assert model.getColumnName(0) == 'Name'
        assert model.getColumnName(1) == 'Location'
        
        // lets set some values
        model.setValueAt('Antigua', 0, 1)
        assertValueAt(model, 0, 1, 'Antigua')
        
        // lets check the real model changed too
        def james = list.get(0)
        assert james.location == 'Antigua'
    }
    
    protected void assertValueAt(model, row, col, expected) {
        def value = model.getValueAt(row, col)
        assert value == expected , "for row " + row + " col " + col
    }
}
