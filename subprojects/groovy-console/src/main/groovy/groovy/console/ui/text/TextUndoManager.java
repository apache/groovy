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
package groovy.ui.text;

import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.beans.PropertyChangeListener;

/**
 * To use this, simply drop this as an UndoableEditListener into your document,
 * and then create actions to call undo/redo as needed (checking can undo/redo
 * first, of course).
 */
public class TextUndoManager extends UndoManager {

    private SwingPropertyChangeSupport propChangeSupport =
            new SwingPropertyChangeSupport(this);

    private StructuredEdit compoundEdit = new StructuredEdit();

    private long firstModified;

    private UndoableEdit modificationMarker = editToBeUndone();

    /**
     * Creates a new instance of TextUndoManager.
     */
    public TextUndoManager() {
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        propChangeSupport.addPropertyChangeListener(pcl);
    }

    public void die() {
        boolean undoable = canUndo();
        super.die();
        firePropertyChangeEvent(UndoManager.UndoName, undoable, canUndo());
    }

    public void discardAllEdits() {
        boolean undoable = canUndo();
        boolean redoable = canRedo();

        boolean changed = hasChanged();
        super.discardAllEdits();
        modificationMarker = editToBeUndone();

        firePropertyChangeEvent(UndoManager.UndoName, undoable, canUndo());
        firePropertyChangeEvent(UndoManager.UndoName, redoable, canRedo());
    }

    protected void firePropertyChangeEvent(String name,
                                           boolean oldValue,
                                           boolean newValue) {
        propChangeSupport.firePropertyChange(name, oldValue, newValue);
    }

    public boolean hasChanged() {
        return modificationMarker != editToBeUndone();
    }

    public void redo() throws javax.swing.undo.CannotRedoException {
        compoundEdit.end();

        if (firstModified == 0) {
            firstModified = ((StructuredEdit) editToBeRedone()).editedTime();
        }

        boolean undoable = canUndo();

        boolean changed = hasChanged();
        super.redo();

        firePropertyChangeEvent(UndoManager.UndoName, undoable, canUndo());
    }

    protected void redoTo(UndoableEdit edit) {
        compoundEdit.end();

        if (firstModified == 0) {
            firstModified = ((StructuredEdit) editToBeRedone()).editedTime();
        }

        boolean undoable = canUndo();

        boolean changed = hasChanged();
        super.redoTo(edit);

        firePropertyChangeEvent(UndoManager.UndoName, undoable, canUndo());

    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        propChangeSupport.removePropertyChangeListener(pcl);
    }

    public void reset() {
        boolean changed = modificationMarker != editToBeUndone();
        if (changed) {
            modificationMarker = editToBeUndone();
        }
    }

    protected void trimEdits(int from, int to) {
        boolean undoable = canUndo();
        boolean redoable = canRedo();

        boolean changed = hasChanged();
        super.trimEdits(from, to);

        firePropertyChangeEvent(UndoManager.UndoName, undoable, canUndo());
        firePropertyChangeEvent(UndoManager.RedoName, redoable, canRedo());
    }

    public void undo() throws javax.swing.undo.CannotUndoException {
        compoundEdit.end();

        UndoableEdit edit = editToBeUndone();
        if (((StructuredEdit) editToBeUndone()).editedTime() ==
                firstModified) {
            firstModified = 0;
        } else if (firstModified == 0) {
            firstModified = ((StructuredEdit) editToBeUndone()).editedTime();
        }

        boolean redoable = canRedo();
        boolean changed = hasChanged();
        super.undo();
        firePropertyChangeEvent(UndoManager.RedoName, redoable, canRedo());
    }

    public void undoableEditHappened(UndoableEditEvent uee) {
        UndoableEdit edit = uee.getEdit();
        boolean undoable = canUndo();

        long editTime = System.currentTimeMillis();

        if (firstModified == 0 ||
                editTime - compoundEdit.editedTime() > 700) {
            compoundEdit.end();
            compoundEdit = new StructuredEdit();
        }
        compoundEdit.addEdit(edit);

        firstModified = firstModified == 0 ?
                compoundEdit.editedTime() : firstModified;

        if (lastEdit() != compoundEdit) {
            boolean changed = hasChanged();
            addEdit(compoundEdit);
            firePropertyChangeEvent(UndoManager.UndoName, undoable, canUndo());
        }

    }

    private static class StructuredEdit extends CompoundEdit {

        private long editedTime;

        public boolean addEdit(UndoableEdit edit) {
            boolean result = super.addEdit(edit);
            if (result && editedTime == 0) {
                editedTime = System.currentTimeMillis();
            }
            return result;
        }

        public boolean canUndo() {
            return !edits.isEmpty();
        }

        protected long editedTime() {
            return editedTime;
        }

        public boolean isInProgress() {
            return false;
        }
    }
}
