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
package groovy.io

import java.nio.CharBuffer

class LineColumnReaderTest extends GroovyTestCase {

    String text = '''L'invitation au voyage

Mon enfant, ma soeur,
Songe à la douceur
D'aller là-bas vivre ensemble !
Aimer à loisir,
Aimer et mourir
Au pays qui te ressemble !
Les soleils mouillés
De ces ciels brouillés
Pour mon esprit ont les charmes
Si mystérieux
De tes traîtres yeux,
Brillant à travers leurs larmes.

Là, tout n'est qu'ordre et beauté,
Luxe, calme et volupté.

Des meubles luisants,
Polis par les ans,
Décoreraient notre chambre ;
Les plus rares fleurs
Mêlant leurs odeurs
Aux vagues senteurs de l'ambre,
Les riches plafonds,
Les miroirs profonds,
La splendeur orientale,
Tout y parlerait
À l'âme en secret
Sa douce langue natale.

Là, tout n'est qu'ordre et beauté,
Luxe, calme et volupté.

Vois sur ces canaux
Dormir ces vaisseaux
Dont l'humeur est vagabonde ;
C'est pour assouvir
Ton moindre désir
Qu'ils viennent du bout du monde.
- Les soleils couchants
Revêtent les champs,
Les canaux, la ville entière,
D'hyacinthe et d'or ;
Le monde s'endort
Dans une chaude lumière.

Là, tout n'est qu'ordre et beauté,
Luxe, calme et volupté.'''

    def reader = new LineColumnReader(new StringReader(text))

    void testReadLine() {
        reader.withReader { LineColumnReader r ->
            assert r.readLine() == "L'invitation au voyage"
            assert r.line == 1 && r.column == 23
            assert r.readLine() == ""
            assert r.line == 2 && r.column == 1
            assert r.readLine() == "Mon enfant, ma soeur,"
            assert r.line == 3 && r.column == 22
        }
    }

    void testReadLineWholeFile() {
        reader.withReader { LineColumnReader r ->
            int linesRead = 0
            String line = ""
            for(;;) {
                if (line == null) break
                line = r.readLine()
                if (line != null) linesRead++
            }

            assert linesRead == 49
        }
    }

    void testSkip() {
        reader.withReader { LineColumnReader r ->
            r.skip(13)
            assert r.readLine() == "au voyage"
        }
    }

    void testWindowsNewLine() {
        def reader = new LineColumnReader(new StringReader("12345\r\nABCDEF\r1234"))
        reader.withReader { LineColumnReader r ->
            assert r.readLine() == "12345"
            assert r.line == 1 && r.column == 6
            assert r.readLine() == "ABCDEF"
            assert r.line == 2 && r.column == 7
            assert r.readLine() == "1234"
            assert r.line == 3 && r.column == 5
        }
    }

    void testReadCharBuffer() {
        shouldFail(UnsupportedOperationException) {
            reader.withReader { LineColumnReader r ->
                def buffer = CharBuffer.allocate(13)
                r.read(buffer)
            }
        }
    }
}
