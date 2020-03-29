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
package org.codehaus.groovy.ant;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <p><code>FileIterator</code> is an iterator over a
 * number of files from a collection of FileSet instances.
 */
public class FileIterator implements Iterator<File> {

    /* The iterator over the FileSet objects */
    private final Iterator<FileSet> fileSetIterator;

    /* The Ant project */
    private final Project project;

    /* The directory scanner */
    private DirectoryScanner ds;

    /* The file names in the current FileSet scan */
    private String[] files;

    /* The current index into the file name array */
    private int fileIndex = -1;

    /* The next File object we'll iterate over */
    private File nextFile;

    /* Have we set a next object? */
    private boolean nextObjectSet = false;

    /* Return only directories? */
    private final boolean iterateDirectories;

    public FileIterator(Project project, Iterator<FileSet> fileSetIterator) {
        this(project, fileSetIterator, false);
    }

    public FileIterator(Project project, Iterator<FileSet> fileSetIterator, boolean iterateDirectories) {
        this.project = project;
        this.fileSetIterator = fileSetIterator;
        this.iterateDirectories = iterateDirectories;
    }

    // Iterator interface
    //-------------------------------------------------------------------------

    /**
     * @return true if there is another object that matches the given predicate
     */
    public boolean hasNext() {
        if (nextObjectSet) {
            return true;
        } else {
            return setNextObject();
        }
    }

    /**
     * @return the next object which matches the given predicate
     */
    public File next() {
        if (!nextObjectSet) {
            if (!setNextObject()) {
                throw new NoSuchElementException();
            }
        }
        nextObjectSet = false;
        return nextFile;
    }

    /**
     * throws UnsupportedOperationException
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /*
     * Set nextObject to the next object. If there are no more
     * objects then return false. Otherwise, return true.
     */
    private boolean setNextObject() {
        while (true) {
            while (ds == null) {
                if (!fileSetIterator.hasNext()) {
                    return false;
                }
                FileSet fs = fileSetIterator.next();
                ds = fs.getDirectoryScanner(project);
                ds.scan();
                if (iterateDirectories) {
                    files = ds.getIncludedDirectories();
                } else {
                    files = ds.getIncludedFiles();
                }
                if (files.length > 0) {
                    fileIndex = -1;
                    break;
                } else {
                    ds = null;
                }
            }

            if (ds != null && files != null) {
                if (++fileIndex < files.length) {
                    nextFile = new File(ds.getBasedir(), files[fileIndex]);
                    nextObjectSet = true;
                    return true;
                } else {
                    ds = null;
                }
            }
        }
    }
}


