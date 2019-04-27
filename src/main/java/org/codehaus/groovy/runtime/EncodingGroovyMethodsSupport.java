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
package org.codehaus.groovy.runtime;

/**
 * Keep this constant in a separate file as it is troublesome for Antlr to parse for doc purposes.
 */
public class EncodingGroovyMethodsSupport {
    static final byte[] TRANSLATE_TABLE = (
            "\u0042\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
                    //           \t    \n                \r
                    + "\u0042\u0041\u0041\u0042\u0042\u0041\u0042\u0042"
                    //
                    + "\u0042\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
                    //
                    + "\u0042\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
                    //     sp     !     "     #     $     %     &     '
                    + "\u0041\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
                    //      (     )     *     +     ,     -     .     /
                    + "\u0042\u0042\u0042\u003E\u0042\u0042\u0042\u003F"
                    //      0     1     2     3     4     5     6     7
                    + "\u0034\u0035\u0036\u0037\u0038\u0039\u003A\u003B"
                    //      8     9     :     ;     <     =     >     ?
                    + "\u003C\u003D\u0042\u0042\u0042\u0040\u0042\u0042"
                    //      @     A     B     C     D     E     F     G
                    + "\u0042\u0000\u0001\u0002\u0003\u0004\u0005\u0006"
                    //      H     I J K     L     M N     O
                    + "\u0007\u0008\t\n\u000B\u000C\r\u000E"
                    //      P     Q     R     S     T     U     V     W
                    + "\u000F\u0010\u0011\u0012\u0013\u0014\u0015\u0016"
                    //      X     Y     Z     [     \     ]     ^     _
                    + "\u0017\u0018\u0019\u0042\u0042\u0042\u0042\u0042"
                    //      '     a     b     c     d     e     f     g
                    + "\u0042\u001A\u001B\u001C\u001D\u001E\u001F\u0020"
                    //      h i     j     k     l     m     n     o
                    + "\u0021\"\u0023\u0024\u0025\u0026\u0027\u0028"
                    //      p     q     r     s     t     u     v     w
                    + "\u0029\u002A\u002B\u002C\u002D\u002E\u002F\u0030"
                    //      x     y     z
                    + "\u0031\u0032\u0033").getBytes();

    static final byte[] TRANSLATE_TABLE_URLSAFE = (
            "\u0042\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
                    //           \t    \n                \r
                    + "\u0042\u0041\u0041\u0042\u0042\u0041\u0042\u0042"
                    //
                    + "\u0042\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
                    //
                    + "\u0042\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
                    //     sp     !     "     #     $     %     &     '
                    + "\u0041\u0042\u0042\u0042\u0042\u0042\u0042\u0042"
                    //      (     )     *     +     ,     -     .     /
                    + "\u0042\u0042\u0042\u0042\u0042\u003E\u0042\u0042"
                    //      0     1     2     3     4     5     6     7
                    + "\u0034\u0035\u0036\u0037\u0038\u0039\u003A\u003B"
                    //      8     9     :     ;     <     =     >     ?
                    + "\u003C\u003D\u0042\u0042\u0042\u0040\u0042\u0042"
                    //      @     A     B     C     D     E     F     G
                    + "\u0042\u0000\u0001\u0002\u0003\u0004\u0005\u0006"
                    //      H     I J K     L     M N     O
                    + "\u0007\u0008\t\n\u000B\u000C\r\u000E"
                    //      P     Q     R     S     T     U     V     W
                    + "\u000F\u0010\u0011\u0012\u0013\u0014\u0015\u0016"
                    //      X     Y     Z     [     \     ]     ^     _
                    + "\u0017\u0018\u0019\u0042\u0042\u0042\u0042\u003F"
                    //      '     a     b     c     d     e     f     g
                    + "\u0042\u001A\u001B\u001C\u001D\u001E\u001F\u0020"
                    //      h i     j     k     l     m     n     o
                    + "\u0021\"\u0023\u0024\u0025\u0026\u0027\u0028"
                    //      p     q     r     s     t     u     v     w
                    + "\u0029\u002A\u002B\u002C\u002D\u002E\u002F\u0030"
                    //      x     y     z
                    + "\u0031\u0032\u0033").getBytes();
}
