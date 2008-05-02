/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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
package groovy.swing.impl;

import groovy.lang.Closure;

import javax.swing.ListCellRenderer;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Danno.Ferrin
 */
public class ClosureRenderer implements ListCellRenderer, TableCellRenderer {

    Closure update;
    List children = new ArrayList();

    JList list;
    JTable table;
    Object value;
    boolean selected;
    boolean focused;
    int row;
    int column;

    public ClosureRenderer() {
        this(null);
    }

    public ClosureRenderer(Closure c) {
        setUpdate(c);
    }


    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        this.list = list;
        this.table = null;
        this.value = value;
        this.row = index;
        this.column = -1;
        this.selected = isSelected;
        this.focused = cellHasFocus;

        return render();
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.list = null;
        this.table = table;
        this.value = value;
        this.row = row;
        this.column = column;
        this.selected = isSelected;
        this.focused = hasFocus;

        return render();
    }

    private Component render() {
        Object o = update.call();
        if (o instanceof Component) {
            return (Component) o;
        } else {
            return (Component) children.get(0);
        }
    }

    public Closure getUpdate() {
        return update;
    }

    public void setUpdate(Closure update) {
        if (update != null) {
            update.setDelegate(this);
            update.setResolveStrategy(Closure.DELEGATE_FIRST);
        }
        this.update = update;
    }

    public List getChildren() {
        return children;
    }

    public JList getList() {
        return list;
    }

    public JTable getTable() {
        return table;
    }

    public Object getValue() {
        return value;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isFocused() {
        return focused;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
}
