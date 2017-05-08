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
import java.sql.SQLException

try {
    assert true;
} finally {
    return 0;
}

try {
    assert true;
} catch(Exception e) {
    assert false;
}

try {
    assert true;
} catch(Exception
            e) {
    assert false;
}

try {
    assert true;
} catch(e) {
    assert false;
}

try {
    assert true;
} catch(e) {
    assert false;
} catch(t) {
    assert false;
}

try {
    assert true;
} catch(final e) {
    assert false;
}

try {
    assert true;
} catch(final Exception e) {
    assert false;
}

try {
    assert true;
} catch(IOException e) {
    assert false;
} catch(Exception e) {
    assert false;
}

try {
    assert true;
} catch(IOException e) {
    assert false;
} catch(Exception e) {
    assert false;
} finally {
    return 0;
}

try
{
    assert true;
}
catch(IOException e)
{
    assert false;
}
catch(Exception e)
{
    assert false;
}
finally
{
    return 0;
}

try {
    assert true;
} catch(Exception e) {
    assert false;
} finally {
    return 0;
}

try {
    assert true;
} catch(NullPointerException | IOException e) {
    assert false;
} finally {
    return 0;
}

try {
    assert true;
} catch(NullPointerException | IOException e) {
    assert false;
}

try {
    assert true;
} catch(NullPointerException |
        IOException |
        SQLException
                e) {
    assert false;
}
