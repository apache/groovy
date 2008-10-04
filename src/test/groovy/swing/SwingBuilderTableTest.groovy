/*
 * Copyright 2003-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.swing

import groovy.model.DefaultTableColumn
import groovy.model.DefaultTableModel
import groovy.util.GroovySwingTestCase
import groovy.model.PropertyModel
import groovy.model.ValueHolder


class SwingBuilderTableTest extends GroovySwingTestCase {

    void testTableColumn() {
      testInEDT {
        // TODO is this required?
        def swing = new SwingBuilder()
        swing.table {
            tableColumn()
        }
      }
    }

    void testPropertyColumn() {
      testInEDT {

        def swing = new SwingBuilder()
        def msg = shouldFail {
            swing.propertyColumn()
        }
        assert msg.contains('propertyColumn must be a child of a tableModel')
        msg = shouldFail {
            swing.table {
                tableModel() {
                    propertyColumn()
                }
            }
        }
        assert msg.contains("Must specify a property for a propertyColumn"):  \
             "Instead found message: " + msg
        swing.table {
            tableModel(id: 'model') {
                propertyColumn(propertyName: 'p')
                propertyColumn(propertyName: 'ph', header: 'header')
                propertyColumn(propertyName: 'pt', type: String)
                propertyColumn(propertyName: 'pth', type: String, header: 'header')
                propertyColumn(propertyName: 'pe', editable: false)
                propertyColumn(propertyName: 'peh', editable: false, header: 'header')
                propertyColumn(propertyName: 'pet', editable: false, type: String,)
                propertyColumn(propertyName: 'peth', editable: false, type: String, header: 'header')
            }
        }
        swing.model.columnList.each {col ->
            def propName = col.valueModel.property
            assert (col.headerValue == 'header') ^ !propName.contains('h')
            assert (col.type == String) ^ !propName.contains('t')
            assert col.valueModel.editable ^ propName.contains('e')
        }
      }
    }

    void testClosureColumn() {
      testInEDT {
        def swing = new SwingBuilder()
        def msg = shouldFail {
            swing.closureColumn()
        }
        assert msg.contains('closureColumn must be a child of a tableModel')
        msg = shouldFail {
            swing.table {
                tableModel() {
                    closureColumn()
                }
            }
        }
        assert msg.contains("Must specify 'read' Closure property for a closureColumn"):  \
             "Instead found message: " + msg
        def closure = {x -> x }
        def table = swing.table {
            tableModel() {
                closureColumn(read: closure, write: closure, header: 'header')
            }
            tableModel(model: new groovy.model.ValueHolder('foo')) {
                closureColumn(read: closure, type: String.class)
            }
            tableModel(list: ['a', 'b']) {
                closureColumn(read: closure, type: String.class)
            }
        }

        assert table.columnModel.class.name == 'groovy.model.DefaultTableModel$MyTableColumnModel'
      }
    }

    void testTableModelChange() {
      testInEDT {
        def swing = new SwingBuilder()
        def table = swing.table {
            tableModel {
                propertyColumn(propertyName: 'p')
                propertyColumn(propertyName: 'ph', header: 'header')
                propertyColumn(propertyName: 'pt', type: String)
            }
        }

        def sorter = new groovy.inspect.swingui.TableSorter(table.model)
        table.model = sorter

        //GROOVY-2111 - resetting the model w/ a pass-through cleared the columns
        assert table.columnModel.columnCount == 3
      }
    }

    void testTableModelChange2() {
      testInEDT {
        def tableData = [
                ["ATHLETEID": 1, "FIRSTNAME": "Bob", "LASTNAME": "Jones", "DATEOFBIRTH": '1875-05-20'],
                ["ATHLETEID": 2, "FIRSTNAME": "Sam", "LASTNAME": "Wilson", "DATEOFBIRTH": '1876-12-15'],
                ["ATHLETEID": 3, "FIRSTNAME": "Jessie", "LASTNAME": "James", "DATEOFBIRTH": '1877-06-12']
        ]

        SwingBuilder swing = new SwingBuilder()

        swing.frame() {
            scrollPane {
                table(id: 'table01') {
                    tableModel(list: tableData, id: 'tableModel01') {
                        propertyColumn(header: 'Athlete ID', propertyName: 'ATHLETEID')
                        propertyColumn(header: 'First Name', propertyName: 'FIRSTNAME')
                        propertyColumn(header: 'Last Name', propertyName: 'LASTNAME')
                        propertyColumn(header: 'Date Of Birth', propertyName: 'DATEOFBIRTH')
                    }
                }
            }
        }

        assert swing.table01.columnModel == swing.table01.model.columnModel

        def list = [['name': 'Fred', 'location': 'London'], ['name': 'Bob', 'location': 'Atlanta']]
        def listModel = new ValueHolder(list)
        def model = new DefaultTableModel(listModel)
        model.addColumn(new DefaultTableColumn("Name", new PropertyModel(model.rowModel, "name")))
        model.addColumn(new DefaultTableColumn("Location", new PropertyModel(model.rowModel, "location")))
        swing.table01.setModel(model)

        assert swing.table01.columnModel == swing.table01.model.columnModel

        // try moiving some columns and verifying values
        def value = swing.table01.getValueAt(0, 0)
        swing.table01.moveColumn(0, 1)
        assert value == swing.table01.getValueAt(0, 1)

        swing.table01.removeColumn(swing.table01.columnModel.getColumn(0))
        assert value == swing.table01.getValueAt(0, 0)
      }
    }

    void testTableModelValues() {
      testInEDT {->

        def squares = [
                [val: 1, square: 1],
                [val: 2, square: 4],
                [val: 3, square: 9],
                [val: 4, square: 16]
        ]

        def swing = new SwingBuilder()
        def frame = swing.frame(title: 'Tabelle',
                windowClosing: { System.exit(0) }) {
            scrollPane {
                table(id: 'table') {
                    tableModel(list: squares) {
                        propertyColumn(header: "Wert", propertyName: "val")
                        closureColumn(header: "Quadrat", read: { it.square })
                    }
                }
            }
        }

        squares.eachWithIndex {it, i ->
            assert swing.table.getValueAt(i, 0) == it.val
            assert swing.table.getValueAt(i, 1) == it.square
        }
      }
    }

    public void testTableSyntheticProperties() {
      testInEDT {

        SwingBuilder swing = new SwingBuilder()
        def tableData = [
                ["ATHLETEID": 1, "FIRSTNAME": "Bob", "LASTNAME": "Jones", "DATEOFBIRTH": '1875-05-20'],
                ["ATHLETEID": 2, "FIRSTNAME": "Sam", "LASTNAME": "Wilson", "DATEOFBIRTH": '1876-12-15'],
                ["ATHLETEID": 3, "FIRSTNAME": "Jessie", "LASTNAME": "James", "DATEOFBIRTH": '1877-06-12']
        ]

        def vectorData = [
                [1, "Bob", "Jones", '1875-05-20'],
                [2, "Sam", "Wilson", '1876-12-15'],
                [3, "Jessie", "James", '1877-06-12']
        ]


        swing.frame() {
            scrollPane {
                table(id: 'table01') {
                    tableModel(list: tableData, id: 'tableModel01') {
                        propertyColumn(header: 'Athlete ID', propertyName: 'ATHLETEID')
                        propertyColumn(header: 'First Name', propertyName: 'FIRSTNAME')
                        propertyColumn(header: 'Last Name', propertyName: 'LASTNAME')
                        propertyColumn(header: 'Date Of Birth', propertyName: 'DATEOFBIRTH')
                    }
                }
            }
            scrollPane {
                table(id: 'table02', model: new javax.swing.table.DefaultTableModel(
                        new Vector(vectorData.collect() {new Vector(it)}),
                        new Vector(['Athlete ID', 'First Name', 'Last Name', 'Date Of Birth']))

                )
            }
        }

        assert swing.table01.selectedElement == null
        assert swing.table01.selectedElements == []
        assert swing.table01.elements == tableData

        assert swing.table02.selectedElement == null
        assert swing.table02.selectedElements == []
        assert swing.table02.elements == vectorData

        swing.table01.addRowSelectionInterval(0, 0)
        assert swing.table01.selectedElement == ["ATHLETEID": 1, "FIRSTNAME": "Bob", "LASTNAME": "Jones", "DATEOFBIRTH": '1875-05-20']
        assert swing.table01.selectedElements == [["ATHLETEID": 1, "FIRSTNAME": "Bob", "LASTNAME": "Jones", "DATEOFBIRTH": '1875-05-20']]

        swing.table02.addRowSelectionInterval(0, 0)
        assert swing.table02.selectedElement == ['Athlete ID': 1, 'First Name': 'Bob', 'Last Name': 'Jones', 'Date Of Birth': '1875-05-20']
        assert swing.table02.selectedElements == [['Athlete ID': 1, 'First Name': 'Bob', 'Last Name': 'Jones', 'Date Of Birth': '1875-05-20']]

        swing.table01.addRowSelectionInterval(2, 2)
        assert swing.table01.selectedElement == ["ATHLETEID": 1, "FIRSTNAME": "Bob", "LASTNAME": "Jones", "DATEOFBIRTH": '1875-05-20']
        assert swing.table01.selectedElements == [[ATHLETEID:1, FIRSTNAME:'Bob', LASTNAME:'Jones', DATEOFBIRTH:'1875-05-20'], [ATHLETEID:3, FIRSTNAME:'Jessie', LASTNAME:'James', DATEOFBIRTH:'1877-06-12']]


        swing.table02.addRowSelectionInterval(2, 2)
        assert swing.table02.selectedElement == ['Athlete ID': 1, 'First Name': 'Bob', 'Last Name': 'Jones', 'Date Of Birth': '1875-05-20']
        assert swing.table02.selectedElements == [['Athlete ID': 1, 'First Name': 'Bob', 'Last Name': 'Jones', 'Date Of Birth': '1875-05-20'], ['Athlete ID':3, 'First Name':'Jessie', 'Last Name':'James', 'Date Of Birth':'1877-06-12']]

        assert swing.table01.elements[0].ATHLETEID == 1
        assert swing.table02.elements[0][0] == 1
        swing.table01.model.setValueAt('x',0,0,)
        swing.table02.model.setValueAt('x',0,0)
        assert swing.table01.elements[0].ATHLETEID == 'x'
        assert swing.table02.elements[0][0] == 'x'
      }
    }

    public void testTableBindSyntheticProperties() {
      testInEDT {

        SwingBuilder swing = new SwingBuilder()
        def tableData = [
                ["ATHLETEID": 1, "FIRSTNAME": "Bob", "LASTNAME": "Jones", "DATEOFBIRTH": '1875-05-20'],
                ["ATHLETEID": 2, "FIRSTNAME": "Sam", "LASTNAME": "Wilson", "DATEOFBIRTH": '1876-12-15'],
                ["ATHLETEID": 3, "FIRSTNAME": "Jessie", "LASTNAME": "James", "DATEOFBIRTH": '1877-06-12']
        ]

        def vectorData = [
                [1, "Bob", "Jones", '1875-05-20'],
                [2, "Sam", "Wilson", '1876-12-15'],
                [3, "Jessie", "James", '1877-06-12']
        ]


        swing.frame() {
            scrollPane {
                table(id: 'table01') {
                    tableModel(list: tableData, id: 'tableModel01') {
                        propertyColumn(header: 'Athlete ID', propertyName: 'ATHLETEID')
                        propertyColumn(header: 'First Name', propertyName: 'FIRSTNAME')
                        propertyColumn(header: 'Last Name', propertyName: 'LASTNAME')
                        propertyColumn(header: 'Date Of Birth', propertyName: 'DATEOFBIRTH')
                    }
                }
            }
            scrollPane {
                table(id: 'table02', model: new javax.swing.table.DefaultTableModel(
                        new Vector(vectorData.collect() {new Vector(it)}),
                        new Vector(['Athlete ID', 'First Name', 'Last Name', 'Date Of Birth']))

                )
            }
            t1e = label(text:bind {table01.elements})
            t1se = label(text:bind {table01.selectedElement})
            t1ses = label(text:bind {table01.selectedElements})
            t2e = label(text:bind {table02.elements})
            t2se = label(text:bind {table02.selectedElement})
            t2ses = label(text:bind {table02.selectedElements})
        }

        assert swing.t1e.text == '[{ATHLETEID=1, FIRSTNAME=Bob, LASTNAME=Jones, DATEOFBIRTH=1875-05-20}, {ATHLETEID=2, FIRSTNAME=Sam, LASTNAME=Wilson, DATEOFBIRTH=1876-12-15}, {ATHLETEID=3, FIRSTNAME=Jessie, LASTNAME=James, DATEOFBIRTH=1877-06-12}]'
        assert swing.t1se.text == null
        assert swing.t1ses.text == '[]'

        assert swing.t2e.text == '[[1, Bob, Jones, 1875-05-20], [2, Sam, Wilson, 1876-12-15], [3, Jessie, James, 1877-06-12]]'
        assert swing.t2se.text == null
        assert swing.t2ses.text == '[]'

        swing.table01.addRowSelectionInterval(0, 0)
        assert swing.t1se.text == '{ATHLETEID=1, FIRSTNAME=Bob, LASTNAME=Jones, DATEOFBIRTH=1875-05-20}'
        assert swing.t1ses.text == '[{ATHLETEID=1, FIRSTNAME=Bob, LASTNAME=Jones, DATEOFBIRTH=1875-05-20}]'

        swing.table02.addRowSelectionInterval(0, 0)
        assert swing.t2se.text == '{Athlete ID=1, First Name=Bob, Last Name=Jones, Date Of Birth=1875-05-20}'
        assert swing.t2ses.text == '[{Athlete ID=1, First Name=Bob, Last Name=Jones, Date Of Birth=1875-05-20}]'

        swing.table01.addRowSelectionInterval(2, 2)
        assert swing.t1se.text == '{ATHLETEID=1, FIRSTNAME=Bob, LASTNAME=Jones, DATEOFBIRTH=1875-05-20}'
        assert swing.t1ses.text == '[{ATHLETEID=1, FIRSTNAME=Bob, LASTNAME=Jones, DATEOFBIRTH=1875-05-20}, {ATHLETEID=3, FIRSTNAME=Jessie, LASTNAME=James, DATEOFBIRTH=1877-06-12}]'

        swing.table02.addRowSelectionInterval(2, 2)
        assert swing.t2se.text == '{Athlete ID=1, First Name=Bob, Last Name=Jones, Date Of Birth=1875-05-20}'
        assert swing.t2ses.text == '[{Athlete ID=1, First Name=Bob, Last Name=Jones, Date Of Birth=1875-05-20}, {Athlete ID=3, First Name=Jessie, Last Name=James, Date Of Birth=1877-06-12}]'

        swing.table01.model.setValueAt('x',0,0,)
        swing.table02.model.setValueAt('x',0,0)
        //FIXME groovy default table model does not fire data cahgne events whe editing throught he model.
        //assert swing.t1e.text == '[{ATHLETEID=x, FIRSTNAME=Bob, LASTNAME=Jones, DATEOFBIRTH=1875-05-20}, {ATHLETEID=2, FIRSTNAME=Sam, LASTNAME=Wilson, DATEOFBIRTH=1876-12-15}, {ATHLETEID=3, FIRSTNAME=Jessie, LASTNAME=James, DATEOFBIRTH=1877-06-12}]'
        assert swing.t2e.text == '[[x, Bob, Jones, 1875-05-20], [2, Sam, Wilson, 1876-12-15], [3, Jessie, James, 1877-06-12]]'
      }
    }
}
