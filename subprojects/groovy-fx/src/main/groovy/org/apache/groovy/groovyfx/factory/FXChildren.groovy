/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.groovy.groovyfx.factory

import javafx.beans.value.ObservableValue
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.chart.Axis
import javafx.scene.chart.PieChart
import javafx.scene.chart.XYChart
import javafx.scene.control.Accordion
import javafx.scene.control.ContextMenu
import javafx.scene.control.Control
import javafx.scene.control.Labeled
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuButton
import javafx.scene.control.MenuItem
import javafx.scene.control.ScrollPane
import javafx.scene.control.SplitPane
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TitledPane
import javafx.scene.control.ToolBar
import javafx.scene.control.Tooltip
import javafx.scene.layout.Pane
import javafx.scene.shape.Path
import javafx.scene.shape.PathElement
import javafx.stage.Stage

/**
 * Attaches a nested builder node to its parent according to the parent's type.
 * <p>
 * Containers receive children in their {@code children}, {@code items},
 * {@code tabs}, {@code menus}, {@code panes}, {@code columns}, {@code data}
 * or {@code elements} list; single-content parents ({@code Tab},
 * {@code ScrollPane}, {@code TitledPane}) take the child as their
 * {@code content}; a {@code Stage} takes a {@code Scene}; any control takes a
 * {@code Tooltip} or {@code ContextMenu}, and a {@code Labeled} takes a node
 * as its {@code graphic}. Values that are not nodes (effects, transforms,
 * images, bindings) are ignored here because they are consumed as attribute
 * values of the parent instead.
 */
class FXChildren {

    private FXChildren() {
    }

    static void attach(Object parent, Object child) {
        if (child == null || child instanceof ObservableValue || child instanceof BorderPanePositionFactory.Position) return
        if (child instanceof Axis) return // supplied to charts through the xAxis and yAxis attributes
        switch (parent) {
            case Stage:
                if (child instanceof Scene) { parent.scene = child; return }
                break
            case TabPane:
                if (child instanceof Tab) { parent.tabs.add(child); return }
                break
            case Tab:
                if (child instanceof Tooltip) { parent.tooltip = child; return }
                if (child instanceof ContextMenu) { parent.contextMenu = child; return }
                if (child instanceof Node) { parent.content = child; return }
                break
            case MenuBar:
                if (child instanceof Menu) { parent.menus.add(child); return }
                break
            case Menu:
            case ContextMenu:
            case MenuButton:
                if (child instanceof MenuItem) { parent.items.add(child); return }
                break
            case ToolBar:
            case SplitPane:
                if (child instanceof Node) { parent.items.add(child); return }
                break
            case Accordion:
                if (child instanceof TitledPane) { parent.panes.add(child); return }
                break
            case ScrollPane:
            case TitledPane:
                if (child instanceof Node) { parent.content = child; return }
                break
            case TableView:
                if (child instanceof TableColumn) { parent.columns.add(child); return }
                break
            case XYChart:
                if (child instanceof XYChart.Series) { parent.data.add(child); return }
                break
            case PieChart:
                if (child instanceof PieChart.Data) { parent.data.add(child); return }
                break
            case Path:
                if (child instanceof PathElement) { parent.elements.add(child); return }
                break
            case Pane:
            case Group:
                if (child instanceof Node) { parent.children.add(child); return }
                break
        }
        if (parent instanceof Control) {
            if (child instanceof Tooltip) { parent.tooltip = child; return }
            if (child instanceof ContextMenu) { parent.contextMenu = child; return }
            if (parent instanceof Labeled && child instanceof Node) { parent.graphic = child; return }
        }
        if (child instanceof Node || child instanceof Tab || child instanceof MenuItem || child instanceof TableColumn || child instanceof Scene) {
            throw new IllegalArgumentException("Cannot add a ${child.getClass().simpleName} to a ${parent.getClass().simpleName}")
        }
    }
}
