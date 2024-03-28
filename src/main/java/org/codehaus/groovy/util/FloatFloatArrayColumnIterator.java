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
package org.codehaus.groovy.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * An iterator providing the columns of a float[][].
 * In the case of an array with different size rows,
 * the number of columns will be equal to the length of the smallest row.
 *
 * @since 5.0.0
 */
public class FloatFloatArrayColumnIterator implements Iterator<float[]> {
    private final float[][] array;
    private int columnIndex = 0;
    private int numColumns;

    public FloatFloatArrayColumnIterator(final float[][] array) {
        Objects.requireNonNull(array);
        this.array = array;
        numColumns = Integer.MAX_VALUE;
        for (float[] row: array) {
            if (row.length < numColumns) numColumns = row.length;
        }
        if (numColumns == Integer.MAX_VALUE) numColumns = 0;
    }

    @Override
    public boolean hasNext() {
        return columnIndex < numColumns;
    }

    @Override
    public float[] next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        float[] col = new float[array.length];
        for (int r = 0; r < array.length; r++) {
            col[r] = array[r][columnIndex];
        }
        columnIndex++;
        return col;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not supported for arrays");
    }
}
