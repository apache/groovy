/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
 * $Id$
 */
package org.codehaus.groovy.ant;

import java.io.File;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

/** 
 * <p><code>FileIterator</code> is an iterator over a 
 * number of files from a collection of FileSet instances.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision$
 */
public class FileIterator implements Iterator {

    /** The iterator over the FileSet objects */
    private Iterator fileSetIterator;
    
    /** The Ant project */
    private Project project;
    
    /** The directory scanner */
    private DirectoryScanner ds;
    
    /** The file names in the current FileSet scan */
    private String[] files;
    
    /** The current index into the file name array */
    private int fileIndex = -1;
    
    /** The next File object we'll iterate over */
    private File nextFile;

    /** Have we set a next object? */
    private boolean nextObjectSet = false;

    /** Return only directories? */
    private boolean iterateDirectories = false;

    public FileIterator(Project project,
                        Iterator fileSetIterator) {
        this( project, fileSetIterator, false);
    }

    public FileIterator(Project project,
                        Iterator fileSetIterator,
                        boolean iterateDirectories) {
        this.project = project;
        this.fileSetIterator = fileSetIterator;
        this.iterateDirectories = iterateDirectories;
    }
    
    // Iterator interface
    //-------------------------------------------------------------------------
    
    /** @return true if there is another object that matches the given predicate */
    public boolean hasNext() {
        if ( nextObjectSet ) {
            return true;
        } 
        else {
            return setNextObject();
        }
    }

    /** @return the next object which matches the given predicate */
    public Object next() {
        if ( !nextObjectSet ) {
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

    /**
     * Set nextObject to the next object. If there are no more 
     * objects then return false. Otherwise, return true.
     */
    private boolean setNextObject() {
        while (true) {
            while (ds == null) {
                if ( ! fileSetIterator.hasNext() ) {
                    return false;
                }
                FileSet fs = (FileSet) fileSetIterator.next();
                ds = fs.getDirectoryScanner(project);
                ds.scan();
                if (iterateDirectories) {
                    files = ds.getIncludedDirectories();
                } 
                else {
                    files = ds.getIncludedFiles();
                }
                if ( files.length > 0 ) {
                    fileIndex = -1;
                    break;
                }
                else {
                    ds = null;
                }
            }
        
            if ( ds != null && files != null ) {
                if ( ++fileIndex < files.length ) {
                    nextFile = new File( ds.getBasedir(), files[fileIndex] );
                    nextObjectSet = true;
                    return true;
                }
                else {
                    ds = null;
                }
            }
        }
    }
}


