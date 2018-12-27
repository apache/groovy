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
package org.codehaus.groovy.tools.shell

import org.codehaus.groovy.tools.shell.util.Logger

/**
 * Manages the shells buffers.
 */
class BufferManager
{
    protected final Logger log = Logger.create(this.class)
    
    final List<List<String>> buffers = []

    int selected

    BufferManager() {
        reset()
    }
    
    void reset() {
        buffers.clear()
        
        create(true)
        
        log.debug('Buffers reset')
    }
    
    List<String> current() {
        assert !buffers.isEmpty()
        
        return buffers[selected]
    }
    
    void select(final int index) {
        assert index >= 0 && index < buffers.size()
        
        selected = index
    }

    int create(final boolean select) {
        buffers << []

        def i = buffers.size() - 1

        if (select) {
            this.select(i)
        }
        
        if (log.debugEnabled) {
            log.debug("Created new buffer with index: $i")
        }
        
        return i
    }

    void delete(final int index) {
        assert index >= 0 && index < buffers.size()

        buffers.remove(index)
        
        if (log.debugEnabled) {
            log.debug("Deleted buffer with index: $index")
        }
    }

    int size() {
        return buffers.size()
    }

    //
    // Selected operators
    //
    
    void deleteSelected() {
        delete(selected)

        def i = selected - 1

        if (i < 0) {
            select(0)
        }
        else {
            select(i)
        }
    }

    void clearSelected() {
        current().clear()
    }

    void updateSelected(final List buffer) {
        assert buffer != null
        
        buffers[selected] = buffer
    }
}