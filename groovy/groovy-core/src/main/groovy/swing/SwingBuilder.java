/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package groovy.swing;

import groovy.lang.Closure;
import groovy.model.DefaultTableModel;
import groovy.model.ValueHolder;
import groovy.model.ValueModel;
import groovy.swing.impl.ComponentFacade;
import groovy.swing.impl.ContainerFacade;
import groovy.swing.impl.DefaultAction;
import groovy.swing.impl.Factory;
import groovy.swing.impl.Startable;
import groovy.swing.impl.TableLayout;
import groovy.swing.impl.TableLayoutCell;
import groovy.swing.impl.TableLayoutRow;
import groovy.util.BuilderSupport;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * A helper class for creating Swing widgets using GroovyMarkup
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class SwingBuilder extends BuilderSupport {

    private Log log = LogFactory.getLog(getClass());
    private Map factories = new HashMap();
    private Object constraints;

    public SwingBuilder() {
        registerWidgets();
    }

    protected void setParent(Object parent, Object child) {
        if (child instanceof Action) {
            Action action = (Action) child;
            InvokerHelper.setProperty(parent, "action", action);
            Object keyStroke = action.getValue("KeyStroke");
            //System.out.println("keystroke: " + keyStroke + " for: " + action);
            if (parent instanceof JComponent) {
                JComponent component = (JComponent) parent;
                KeyStroke stroke = null;
                if (keyStroke instanceof String) {
                    stroke = KeyStroke.getKeyStroke((String) keyStroke);
                }
                else if (keyStroke instanceof KeyStroke) {
                    stroke = (KeyStroke) keyStroke;
                }
                if (stroke != null) {
                    String key = action.toString();
                    component.getInputMap().put(stroke, key);
                    component.getActionMap().put(key, action);
                }
            }
        }
        else if (child instanceof LayoutManager) {
            if (parent instanceof RootPaneContainer) {
                RootPaneContainer rpc = (RootPaneContainer) parent;
                parent = rpc.getContentPane();
            }
            InvokerHelper.setProperty(parent, "layout", child);
        }
        else if (parent instanceof JTable && child instanceof TableColumn) {
            JTable table = (JTable) parent;
            TableColumn column = (TableColumn) child;
            table.addColumn(column);
        }
        else {
            Component component = null;
            if (child instanceof Component) {
                component = (Component) child;
            }
            else if (child instanceof ComponentFacade) {
                ComponentFacade facade = (ComponentFacade) child;
                component = facade.getComponent();
            }
            if (component != null) {
                if (parent instanceof JFrame && component instanceof JMenuBar) {
                    JFrame frame = (JFrame) parent;
                    frame.setJMenuBar((JMenuBar) component);
                }
                else if (parent instanceof RootPaneContainer) {
                    RootPaneContainer rpc = (RootPaneContainer) parent;
                    rpc.getContentPane().add(component);
                }
                else if (parent instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane) parent;
                    scrollPane.setViewportView(component);
                }
                else if (parent instanceof JSplitPane) {
                    JSplitPane splitPane = (JSplitPane) parent;
                    if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
                        if (splitPane.getTopComponent() == null) {
                            splitPane.setTopComponent(component);
                        }
                        else {
                            splitPane.setBottomComponent(component);
                        }
                    }
                    else {
                        if (splitPane.getLeftComponent() == null) {
                            splitPane.setLeftComponent(component);
                        }
                        else {
                            splitPane.setRightComponent(component);
                        }
                    }
                }
                else if (parent instanceof JMenuBar && component instanceof JMenu) {
                    JMenuBar menuBar = (JMenuBar) parent;
                    menuBar.add((JMenu) component);
                }
                else if (parent instanceof Container) {
                    Container container = (Container) parent;
                    if (constraints != null) {
                        container.add(component, constraints);
                    }
                    else {
                        container.add(component);
                    }
                }
                else if (parent instanceof ContainerFacade) {
                    ContainerFacade facade = (ContainerFacade) parent;
                    facade.addComponent(component);
                }
            }
        }
    }

    protected void nodeCompleted(Object parent, Object node) {
        // set models after the node has been completed
        if (node instanceof TableModel && parent instanceof JTable) {
            JTable table = (JTable) parent;
            TableModel model = (TableModel) node;
            table.setModel(model);
        }
        if (node instanceof Startable) {
            Startable startable = (Startable) node;
            startable.start();
        }
    }

    protected Object createNode(Object name) {
        return createNode(name, Collections.EMPTY_MAP);
    }

    protected Object createNode(Object name, Object value) {
        Object widget = createNode(name);
        if (widget != null && value instanceof String) {
            InvokerHelper.invokeMethod(widget, "setText", value);
        }
        return widget;
    }

    protected Object createNode(Object name, Map attributes) {
        constraints = attributes.remove("constraints");
        Object widget = null;
        Factory factory = (Factory) factories.get(name);
        if (factory != null) {
            try {
                widget = factory.newInstance(attributes);
                if (widget == null) {
                    log.warn("Factory for name: " + name + " returned null");
                }
                else {
                    if (log.isDebugEnabled()) {
                        log.debug("For name: " + name + " created widget: " + widget);
                    }
                }
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to create component for" + name + " reason: " + e, e);
            }
        }
        else {
            log.warn("Could not find match for name: " + name);
        }
        if (widget != null) {
            if (widget instanceof Action) {
                /** @todo we could move this custom logic into the MetaClass for Action */
                Action action = (Action) widget;
                Closure closure = (Closure) attributes.remove("closure");
                if (closure != null && action instanceof DefaultAction) {
                    DefaultAction defaultAction = (DefaultAction) action;
                    defaultAction.setClosure(closure);
                }
                for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String actionName = (String) entry.getKey();

                    // typically standard Action names start with upper case, so lets upper case it            
                    actionName = capitalize(actionName);
                    Object value = entry.getValue();

                    action.putValue(actionName, value);
                }

            }
            else {
                // set the properties
                for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String property = entry.getKey().toString();
                    Object value = entry.getValue();
                    InvokerHelper.setProperty(widget, property, value);
                }
            }
        }
        return widget;
    }

    protected String capitalize(String text) {
        char ch = text.charAt(0);
        if (Character.isUpperCase(ch)) {
            return text;
        }
        StringBuffer buffer = new StringBuffer(text.length());
        buffer.append(Character.toUpperCase(ch));
        buffer.append(text.substring(1));
        return buffer.toString();
    }

    protected void registerWidgets() {
        registerBeanFactory("action", DefaultAction.class);
        registerBeanFactory("button", JButton.class);
        registerBeanFactory("buttonGroup", ButtonGroup.class);
        registerBeanFactory("checkBox", JCheckBox.class);
        registerBeanFactory("checkBoxMenuItem", JCheckBoxMenuItem.class);

        registerFactory("comboBox", new Factory() {
            public Object newInstance(Map properties)
                throws InstantiationException, InstantiationException, IllegalAccessException {
                return createComboBox(properties);
            }
        });

        registerBeanFactory("desktopPane", JDesktopPane.class);

        registerFactory("dialog", new Factory() {
            public Object newInstance(Map properties)
                throws InstantiationException, InstantiationException, IllegalAccessException {
                return createDialog(properties);
            }
        });

        registerBeanFactory("editorPane", JEditorPane.class);
        registerBeanFactory("fileChooser", JFileChooser.class);
        registerBeanFactory("frame", JFrame.class);
        registerBeanFactory("internalFrame", JInternalFrame.class);
        registerBeanFactory("label", JLabel.class);
        registerBeanFactory("list", JList.class);

        registerFactory("map", new Factory() {
            public Object newInstance(Map properties)
                throws InstantiationException, InstantiationException, IllegalAccessException {
                return properties;
            }
        });

        registerBeanFactory("menu", JMenu.class);
        registerBeanFactory("menuBar", JMenuBar.class);
        registerBeanFactory("menuItem", JMenuItem.class);
        registerBeanFactory("panel", JPanel.class);
        registerBeanFactory("passwordField", JPasswordField.class);
        registerBeanFactory("popupMenu", JPopupMenu.class);
        registerBeanFactory("progressBar", JProgressBar.class);
        registerBeanFactory("radioButton", JRadioButton.class);
        registerBeanFactory("radioButtonMenuItem", JRadioButtonMenuItem.class);
        registerBeanFactory("optionPane", JOptionPane.class);
        registerBeanFactory("scrollPane", JScrollPane.class);
        registerBeanFactory("separator", JSeparator.class);

        registerFactory("splitPane", new Factory() {
            public Object newInstance(Map properties) {
                JSplitPane answer = new JSplitPane();
                answer.setLeftComponent(null);
                answer.setRightComponent(null);
                answer.setTopComponent(null);
                answer.setBottomComponent(null);
                return answer;
            }
        });

        // Box related layout components
        registerFactory("hbox", new Factory() {
            public Object newInstance(Map properties) {
                return Box.createHorizontalBox();
            }
        });
        registerFactory("vbox", new Factory() {
            public Object newInstance(Map properties) {
                return Box.createVerticalBox();
            }
        });

        registerBeanFactory("tabbedPane", JTabbedPane.class);
        registerBeanFactory("table", JTable.class);

        registerBeanFactory("textArea", JTextArea.class);
        registerBeanFactory("textPane", JTextPane.class);
        registerBeanFactory("textField", JTextField.class);
        registerBeanFactory("toggleButton", JToggleButton.class);
        registerBeanFactory("tree", JTree.class);
        registerBeanFactory("toolBar", JToolBar.class);

        // MVC models        
        registerFactory("tableModel", new Factory() {
            public Object newInstance(Map properties) {
                ValueModel model = (ValueModel) properties.remove("model");
                if (model == null) {
                    Object list = properties.remove("list");
                    if (list == null) {
                        list = new ArrayList();
                    }
                    model = new ValueHolder(list);
                }
                return new DefaultTableModel(model);
            }
        });

        registerFactory("propertyColumn", new Factory() {
            public Object newInstance(Map properties) {
                Object current = getCurrent();
                if (current instanceof DefaultTableModel) {
                    DefaultTableModel model = (DefaultTableModel) current;
                    Object header = properties.remove("header");
                    if (header == null) {
                        header = "";
                    }
                    String property = (String) properties.remove("propertyName");
                    if (property == null) {
                        throw new IllegalArgumentException("Must specify a property for a propertyColumn");
                    }
                    Class type = (Class) properties.remove("type");
                    if (type == null) {
                        type = Object.class;
                    }
                    return model.addPropertyColumn(header, property, type);
                }
                else {
                    throw new RuntimeException("propertyColumn must be a child of a tableModel");
                }
            }
        });

        registerFactory("closureColumn", new Factory() {
            public Object newInstance(Map properties) {
                Object current = getCurrent();
                if (current instanceof DefaultTableModel) {
                    DefaultTableModel model = (DefaultTableModel) current;
                    Object header = properties.remove("header");
                    if (header == null) {
                        header = "";
                    }
                    Closure readClosure = (Closure) properties.remove("read");
                    if (readClosure == null) {
                        throw new IllegalArgumentException("Must specify 'read' Closure property for a closureColumn");
                    }
                    Closure writeClosure = (Closure) properties.remove("write");
                    Class type = (Class) properties.remove("type");
                    if (type == null) {
                        type = Object.class;
                    }
                    return model.addClosureColumn(header, readClosure, writeClosure, type);
                }
                else {
                    throw new RuntimeException("propertyColumn must be a child of a tableModel");
                }
            }
        });

        // table layout
        registerBeanFactory("tableLayout", TableLayout.class);
        registerFactory("tr", new Factory() {
            public Object newInstance(Map properties) {
                Object parent = getCurrent();
                if (parent instanceof TableLayout) {
                    return new TableLayoutRow((TableLayout) parent);
                }
                else {
                    throw new RuntimeException("'tr' must be within a 'tableLayout'");
                }
            }
        });
        registerFactory("td", new Factory() {
            public Object newInstance(Map properties) {
                Object parent = getCurrent();
                if (parent instanceof TableLayoutRow) {
                    return new TableLayoutCell((TableLayoutRow) parent);
                }
                else {
                    throw new RuntimeException("'td' must be within a 'tr'");
                }
            }
        });
    }

    protected Object createDialog(Map properties) {
        Object owner = properties.remove("owner");
        if (owner instanceof Frame) {
            return new JDialog((Frame) owner);
        }
        else if (owner instanceof Dialog) {
            return new JDialog((Dialog) owner);
        }
        else {
            return new JDialog();
        }
    }

    protected Object createComboBox(Map properties) {
        Object items = properties.remove("items");
        if (items instanceof Vector) {
            return new JComboBox((Vector) items);
        }
        else if (items instanceof List) {
            List list = (List) items;
            return new JComboBox(list.toArray());
        }
        else if (items instanceof Object[]) {
            return new JComboBox((Object[]) items);
        }
        else {
            return new JComboBox();
        }
    }

    protected void registerBeanFactory(String name, final Class beanClass) {
        registerFactory(name, new Factory() {
            public Object newInstance(Map properties) throws InstantiationException, IllegalAccessException {
                return beanClass.newInstance();
            }
        });

    }

    protected void registerFactory(String name, Factory factory) {
        factories.put(name, factory);
    }
}
