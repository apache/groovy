package groovy.bugs

class Groovy3509Bug extends GroovyTestCase {

    void testGPathInconcistency() {
        def data = [
                [a: null],
                [a: [b: 1]],
                [a: [b: 2]]
        ]
        assert data.a.b.sum() == 3

        data = [
                [a: [b: 1]],
                [a: [b: 2]],
                [a: null]
        ]
        assert data.a.b.sum() == 3

        data = [
                [a: [b: [c:1]]],
                [a: [b: null]],
                [a: [b: [c:2]]]
        ]
        assert data.a.b.c.sum() == 3
    }

    void testOriginalCase() {
        // initialize structure
        def root = new Root()
        for (i in 0..2) {
            def level1 = new Level1()
            level1.level2 = new Level2()
            level1.level2.level3 = i
            root.level1 << level1
        }

        // given
        assert root.level1[0].level2.level3 == 0
        assert root.level1[1].level2.level3 == 1
        assert root.level1[2].level2.level3 == 2

        // then
        assert root?.level1?.level2?.level3.sum() == 3

        // but now we have a null property in between
        root.level1[0].level2 = null

        // even with this intermediary null node, we should still get 3
        assert root?.level1?.level2?.level3.sum() == 3
    }
}

class Root {
  List level1 = []
}

class Level2 {
  Integer level3
}

class Level1 {
  Level2 level2
}
