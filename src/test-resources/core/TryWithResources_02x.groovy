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
import groovy.transform.CompileStatic

import java.io.*


class Resource implements Closeable {
    int resourceId;
    static closedResourceIds = [];
    static exMsg = "failed to close";

    public Resource(int resourceId) {
        this.resourceId = resourceId;
    }

    public void close() {
        if (3 == resourceId) throw new IOException(exMsg);

        closedResourceIds << resourceId
    }
}

// test case 1
def a = 1;
Resource resource1 = new Resource(1)
try (resource1) {
    a = 2;
}
assert Resource.closedResourceIds == [1]
assert 2 == a

// test case 2
Resource.closedResourceIds = []
final exMsg = "resource not found";
try {
    // try { ... } should throw the IOException, while the resource should be closed
    Resource r1 = new Resource(2)
    try (r1) {
        throw new FileNotFoundException(exMsg)
    }
} catch(FileNotFoundException e) {
    assert exMsg == e.getMessage()
}
assert Resource.closedResourceIds == [2]

// test case 3
Resource.closedResourceIds = []
a = 1;
try {
    Resource r1 = new Resource(3)
    try (r1) {
        a = 2;
    }
} catch (IOException e) {
    assert Resource.exMsg == e.getMessage()
}
assert 2 == a;
assert Resource.closedResourceIds == []

// test case 4
Resource.closedResourceIds = []
try {
    // try { ... } should throw the IOException, while the resource should be closed
    Resource r1 = new Resource(3)
    try (r1) {
        throw new FileNotFoundException(exMsg)
    }
} catch(FileNotFoundException e) {
    assert exMsg == e.getMessage()

    def suppressedExceptions = e.getSuppressed();
    assert suppressedExceptions.length == 1
    assert suppressedExceptions[0] instanceof IOException
    assert suppressedExceptions[0].getMessage() == Resource.exMsg
}
assert Resource.closedResourceIds == []


// test case 5
Resource.closedResourceIds = []
a = 1;
Resource rr1 = new Resource(5)
try (rr1;
Resource r2 = new Resource(6);) {
    a = 2;
}
assert Resource.closedResourceIds == [6, 5]
assert 2 == a

// test case 6
Resource.closedResourceIds = []
a = 1;
Resource rr2 = new Resource(6)
try (Resource r1 = new Resource(5);
rr2;
Resource r3 = new Resource(7);) {
    a = 2;
}
assert Resource.closedResourceIds == [7, 6, 5]
assert 2 == a


// test case 7
Resource.closedResourceIds = []
Resource rrr1 = new Resource(7)
try (rrr1) {
    throw new FileNotFoundException(exMsg)
} catch(FileNotFoundException e) {
    assert exMsg == e.getMessage()
}
assert Resource.closedResourceIds == [7]

// test case 8
Resource rrr2 = new Resource(8)
Resource.closedResourceIds = []
try (Resource r1 = new Resource(7);
rrr2) {
    throw new FileNotFoundException(exMsg)
} catch(FileNotFoundException e) {
    assert exMsg == e.getMessage()
}
assert Resource.closedResourceIds == [8, 7]


// test case 9
Resource.closedResourceIds = []
a = 1;
Resource rrrr1 = new Resource(3)
try (rrrr1) {
    a = 2;
} catch (IOException e) {
    assert Resource.exMsg == e.getMessage()
}
assert 2 == a;
assert Resource.closedResourceIds == []


// test case 10
Resource.closedResourceIds = []
a = 1;
Resource rrrrr1 = new Resource(3);
Resource rrrrr2 = new Resource(4);
try (rrrrr1; rrrrr2) {
    a = 2;
} catch (IOException e) {
    assert Resource.exMsg == e.getMessage()
}
assert 2 == a;
assert Resource.closedResourceIds == [4]

// test case 11
Resource.closedResourceIds = []
a = 1;
Resource rrrrrr0 = new Resource(2);
Resource rrrrrr2 = new Resource(4)
try (rrrrrr0;
Resource rrrrrr1 = new Resource(3); rrrrrr2) {
    a = 2;
} catch (IOException e) {
    assert Resource.exMsg == e.getMessage()
}
assert 2 == a;
assert Resource.closedResourceIds == [4, 2]


// test case 12
Resource.closedResourceIds = []
Resource rrrrrrr1 = new Resource(3);
Resource rrrrrrr2 = new Resource(4)
try (rrrrrrr1;
rrrrrrr2) {
    throw new FileNotFoundException(exMsg)
} catch(FileNotFoundException e) {
    assert exMsg == e.getMessage()

    def suppressedExceptions = e.getSuppressed();
    assert suppressedExceptions.length == 1
    assert suppressedExceptions[0] instanceof IOException
    assert suppressedExceptions[0].getMessage() == Resource.exMsg
}
assert Resource.closedResourceIds == [4]

// test case 13
Resource.closedResourceIds = []
Resource rrrrrrrr2 = new Resource(4)
try (Resource r0 = new Resource(2);
Resource r1 = new Resource(3);
rrrrrrrr2) {
    throw new FileNotFoundException(exMsg)
} catch(FileNotFoundException e) {
    assert exMsg == e.getMessage()

    def suppressedExceptions = e.getSuppressed();
    assert suppressedExceptions.length == 1
    assert suppressedExceptions[0] instanceof IOException
    assert suppressedExceptions[0].getMessage() == Resource.exMsg
}
assert Resource.closedResourceIds == [4, 2]

// test case 14
Resource.closedResourceIds = []
a = 1;
Resource rrrrrrrrr1 = new Resource(1)
try (rrrrrrrrr1) {
    a += 2;
    Resource rrrrrrrrr2 = new Resource(2);
    Resource rrrrrrrrr4 = new Resource(4)
    try (rrrrrrrrr2; rrrrrrrrr4) {
        a += 3;
        Resource rrrrrrrrr6 = new Resource(6)
        try (Resource r5 = new Resource(5); rrrrrrrrr6; Resource r7 = new Resource(7)) {
            a += 4;
            try {
                Resource rrrrrrrrr3 = new Resource(3)
                try (rrrrrrrrr3) {
                    a += 5;
                }
            } catch (IOException e) {
                assert Resource.exMsg == e.getMessage()
            }
        }
    } catch(Exception e) {
        // ignored
    } finally {
        a += 10
    }
}
assert Resource.closedResourceIds == [7, 6, 5, 4, 2, 1]
assert 25 == a

// test case 15
@CompileStatic
void tryWithResources() {
    Resource.closedResourceIds = []
    int cs = 1;
    Resource r1 = new Resource(1)
    try (r1) {
        cs += 2;
        Resource r2 = new Resource(2);
        Resource r4 = new Resource(4)
        try (r2; r4) {
            cs += 3;
            Resource r6 = new Resource(6);
            try (Resource r5 = new Resource(5); r6; Resource r7 = new Resource(7)) {
                cs += 4;
                try {
                    Resource r3 = new Resource(3)
                    try (r3) {
                        cs += 5;
                    }
                } catch (IOException e) {
                    assert Resource.exMsg == e.getMessage()
                }
            }
        } catch(Exception e) {
            // ignored
        } finally {
            cs += 10
        }
    }
    assert Resource.closedResourceIds == [7, 6, 5, 4, 2, 1]
    assert 25 == cs
}

tryWithResources()


// test case 16
Resource.closedResourceIds = []
a = 1;
Resource rrrrrrrrrr1 = new Resource(
        1
)
try (
        rrrrrrrrrr1
        Resource r2 = new Resource(2)
) {
    a = 2;
}
assert Resource.closedResourceIds == [2, 1]
assert 2 == a

// test case 17
Resource.closedResourceIds = []
a = 1;
rrrrrrrrrrr2 = new Resource(2)
try (r1 = new Resource(1)
rrrrrrrrrrr2) {
    a = 2;
}
assert Resource.closedResourceIds == [2, 1]
assert 2 == a

