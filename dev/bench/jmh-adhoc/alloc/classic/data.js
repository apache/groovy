window.BENCHMARK_DATA = {
  "lastUpdate": 1786351161333,
  "repoUrl": "https://github.com/apache/groovy",
  "entries": {
    "Benchmark": [
      {
        "commit": {
          "author": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "committer": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "id": "e93430ef8273ad3979fce19117ec08c4c4275fea",
          "message": "GROOVY-12034: Apply \"fat-free lambda\" patterns to Groovy (benchmark)",
          "timestamp": "2026-05-23T06:52:34Z",
          "url": "https://github.com/apache/groovy/commit/e93430ef8273ad3979fce19117ec08c4c4275fea"
        },
        "date": 1780368626908,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "countA_captureClosure (size=100)",
            "value": 7280,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=1000)",
            "value": 72112.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=10000)",
            "value": 720081.6,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 10216,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96592.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960596.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 10084,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96472.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960474.6,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=10000)",
            "value": 55.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=100)",
            "value": 7280,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=1000)",
            "value": 72080.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=10000)",
            "value": 720081.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=100)",
            "value": 7720,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=1000)",
            "value": 76120.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=10000)",
            "value": 760121.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 10632,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100644.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000649.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=10000)",
            "value": 24.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10512,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100512.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000514.6,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=10000)",
            "value": 26.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=10000)",
            "value": 85.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 10168,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96580.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960573.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=10000)",
            "value": 1.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 10048,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96448.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960450.6,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=10000)",
            "value": 2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=10000)",
            "value": 104.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "committer": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "id": "e93430ef8273ad3979fce19117ec08c4c4275fea",
          "message": "GROOVY-12034: Apply \"fat-free lambda\" patterns to Groovy (benchmark)",
          "timestamp": "2026-05-23T06:52:34Z",
          "url": "https://github.com/apache/groovy/commit/e93430ef8273ad3979fce19117ec08c4c4275fea"
        },
        "date": 1780375356815,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "countA_captureClosure (size=100)",
            "value": 7280,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=1000)",
            "value": 72112.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=10000)",
            "value": 720081.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 10192,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96592.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960607.7,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 10084,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96472.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960486.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=100)",
            "value": 52,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=10000)",
            "value": 52.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=100)",
            "value": 7280,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=1000)",
            "value": 72080.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=10000)",
            "value": 720081.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=100)",
            "value": 7720,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=1000)",
            "value": 76120.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=10000)",
            "value": 760121.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 10644,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100656.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000648.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10512,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100512.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000538.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=10000)",
            "value": 25.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=10000)",
            "value": 131.6,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 10192,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96580.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960584.8,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=10000)",
            "value": 0.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 10060,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96460.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960450.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=10000)",
            "value": 1.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=10000)",
            "value": 56.7,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "committer": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "id": "653195f4057c7f77032b10b766562502db2705b4",
          "message": "Use log y-axis for JMH adhoc throughput charts\n\nLarger workload sizes span ~2 decades of throughput, so a linear axis\nflattened the size=10000 group into indistinguishable slivers despite a\nreal ~12x best-vs-worst spread within each size. Matches the allocation\ncharts, which were already log.\n\nCo-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>",
          "timestamp": "2026-06-02T06:58:05Z",
          "url": "https://github.com/apache/groovy/commit/653195f4057c7f77032b10b766562502db2705b4"
        },
        "date": 1780395617687,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "countA_captureClosure (size=100)",
            "value": 7280,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=1000)",
            "value": 72112.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=10000)",
            "value": 720081.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 10216,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96592.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960596.8,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 10072,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96472.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960474.6,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=10000)",
            "value": 0.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=100)",
            "value": 7280,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=1000)",
            "value": 72080.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=10000)",
            "value": 720081.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=100)",
            "value": 7720,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=1000)",
            "value": 76120.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=10000)",
            "value": 760121.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 10632,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100644.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000636.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10524,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100512.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000526.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=10000)",
            "value": 24.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=100)",
            "value": 76,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=10000)",
            "value": 83.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 10168,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96568.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960584.8,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 10048,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96448.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960450.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=10000)",
            "value": 1.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=10000)",
            "value": 8.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "committer": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "id": "9f8ebfc8972504541da1c5ec85f80159d1489f8a",
          "message": "GROOVY-12067: IntRange.containsWithinBounds delegates to contains, breaking the continuous-bounds contract",
          "timestamp": "2026-06-08T04:07:38Z",
          "url": "https://github.com/apache/groovy/commit/9f8ebfc8972504541da1c5ec85f80159d1489f8a"
        },
        "date": 1780910598728,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "countA_captureClosure (size=100)",
            "value": 7280,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=1000)",
            "value": 72112.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=10000)",
            "value": 720081.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 10192,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96604.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960607.8,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 10072,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96484.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960498.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=10000)",
            "value": 52.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=100)",
            "value": 7280,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=1000)",
            "value": 72080.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=10000)",
            "value": 720081.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=100)",
            "value": 7720,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=1000)",
            "value": 76120.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=10000)",
            "value": 760121.6,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 10632,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100644.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000649,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10512,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100524.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000526.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=10000)",
            "value": 25.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=10000)",
            "value": 33.7,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 10168,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96568.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960572.8,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 10048,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96448.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960462.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=10000)",
            "value": 1.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=1000)",
            "value": 52,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=10000)",
            "value": 104.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "committer": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "id": "141150926387f5f1739369638d614a74275f2821",
          "message": "GROOVY-12089: Groovy 5 ClassNode.getGetterMethod() can clone a getter with a null exceptions array when @Entity and @Sortable are combined",
          "timestamp": "2026-06-15T08:05:24Z",
          "url": "https://github.com/apache/groovy/commit/141150926387f5f1739369638d614a74275f2821"
        },
        "date": 1781517248471,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "countA_captureClosure (size=100)",
            "value": 7280,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=1000)",
            "value": 72112.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=10000)",
            "value": 720081.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 10192,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96592.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960596.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 10072,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96496.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960486.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=10000)",
            "value": 104.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=100)",
            "value": 7280,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=1000)",
            "value": 72080.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=10000)",
            "value": 720081.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=100)",
            "value": 7720,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=1000)",
            "value": 76120.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=10000)",
            "value": 760121.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 10644,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100632.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000637,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10512,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100536.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000526.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=10000)",
            "value": 24.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=10000)",
            "value": 131.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 10180,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96568.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960584.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=10000)",
            "value": 1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 10072,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96460.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960462.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=10000)",
            "value": 1.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=10000)",
            "value": 8.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "committer": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "id": "9488b37ca09b89db266a82453f34e1e3f7b75a72",
          "message": "use basic cache-provider",
          "timestamp": "2026-06-21T04:04:22Z",
          "url": "https://github.com/apache/groovy/commit/9488b37ca09b89db266a82453f34e1e3f7b75a72"
        },
        "date": 1782123359095,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "countA_captureClosure (size=100)",
            "value": 7280,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=1000)",
            "value": 72112.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=10000)",
            "value": 720081.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 10204,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96592.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960609,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 10072,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96484.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960474.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=10000)",
            "value": 104.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=100)",
            "value": 7280,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=1000)",
            "value": 72080.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=10000)",
            "value": 720081.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=100)",
            "value": 7720,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=1000)",
            "value": 76120.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=10000)",
            "value": 760121.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 10632,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100644.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000637.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10524,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100524.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000514.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=10000)",
            "value": 24.7,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=10000)",
            "value": 131,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 10168,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96568.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960571.7,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=10000)",
            "value": 0.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 10072,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96448.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960462.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=10000)",
            "value": 1.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=1000)",
            "value": 52,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=10000)",
            "value": 104.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "dependabot[bot]",
            "username": "dependabot[bot]",
            "email": "49699333+dependabot[bot]@users.noreply.github.com"
          },
          "committer": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "id": "93f3d169375b5a8129496bdef416200210e44dba",
          "message": "Bump com.gradle.develocity from 4.4.2 to 4.4.3\n\nBumps com.gradle.develocity from 4.4.2 to 4.4.3.\n\n---\nupdated-dependencies:\n- dependency-name: com.gradle.develocity\n  dependency-version: 4.4.3\n  dependency-type: direct:production\n  update-type: version-update:semver-patch\n...\n\nSigned-off-by: dependabot[bot] <support@github.com>",
          "timestamp": "2026-06-28T01:22:31Z",
          "url": "https://github.com/apache/groovy/commit/93f3d169375b5a8129496bdef416200210e44dba"
        },
        "date": 1782724869922,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "countA_captureClosure (size=100)",
            "value": 7280,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=1000)",
            "value": 72112.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=10000)",
            "value": 720081.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 10204,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96616.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960595.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 10072,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96484.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960474.6,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=100)",
            "value": 7280,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=1000)",
            "value": 72080.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=10000)",
            "value": 720081.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=100)",
            "value": 7720,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=1000)",
            "value": 76120.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=10000)",
            "value": 760121.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 10644,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100644.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000649.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10512,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100512.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000514.6,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=10000)",
            "value": 25.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=10000)",
            "value": 131.6,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 10168,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96568.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960585.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=10000)",
            "value": 0.6,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 10060,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96460.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960450.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=10000)",
            "value": 1.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=10000)",
            "value": 56.6,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "committer": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "id": "acc614d5af8b41b4007d8e20fefe1d3256680219",
          "message": "GROOVY-11947: Add timing utility methods (timed, timedNanos) and Timed record to groovy-jdk",
          "timestamp": "2026-07-02T05:27:57Z",
          "url": "https://github.com/apache/groovy/commit/acc614d5af8b41b4007d8e20fefe1d3256680219"
        },
        "date": 1783330286169,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "countA_captureClosure (size=100)",
            "value": 7280,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=1000)",
            "value": 72112.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=10000)",
            "value": 720081.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 10204,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96604.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960596.8,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 10072,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96472.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960474.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=10000)",
            "value": 104.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=100)",
            "value": 7280,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=1000)",
            "value": 72080.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=10000)",
            "value": 720081.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=100)",
            "value": 7720,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=1000)",
            "value": 76120.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=10000)",
            "value": 760121.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 10632,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100632.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000649,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10524,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100524.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000514.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=10000)",
            "value": 25,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=10000)",
            "value": 132.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 10168,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96568.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960572.8,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 10072,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96460.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960450.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=10000)",
            "value": 1.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=10000)",
            "value": 57.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "committer": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "id": "86c6648d1e61436fe513afaa85d8d0c403de3cda",
          "message": "GROOVY-12154: box primitive lambda param when the target SAM param is a reference type\n\nA @CompileStatic lambda declaring a primitive parameter (e.g. (int a) -> ...)\nagainst a generic functional interface (e.g. Function<Integer,Integer>)\ncompiled cleanly but threw at runtime:\n\n  java.lang.invoke.LambdaConversionException:\n    int is not a subtype of class java.lang.Object\n\nAbstractFunctionalInterfaceWriter.convertParameterType always emitted a\nprimitive implementation-method parameter when the lambda declared a\nprimitive, ignoring the target (functional-interface) parameter type. But\nLambdaMetafactory links against the erased SAM signature (Object) and will\nnot unbox to a primitive, so the implementation method must accept the boxed\ntype when the SAM parameter is a reference type. It stays primitive only when\nthe SAM parameter is itself primitive (e.g. IntUnaryOperator), preserving\nGROOVY-9790.",
          "timestamp": "2026-07-12T13:11:53Z",
          "url": "https://github.com/apache/groovy/commit/86c6648d1e61436fe513afaa85d8d0c403de3cda"
        },
        "date": 1783932482297,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "countA_captureClosure (size=100)",
            "value": 7280,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=1000)",
            "value": 72112.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=10000)",
            "value": 720081.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 10192,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96592.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960608.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 10072,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96496.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960474.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=10000)",
            "value": 104.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=100)",
            "value": 7280,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=1000)",
            "value": 72080.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=10000)",
            "value": 720081.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=100)",
            "value": 7720,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=1000)",
            "value": 76120.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=10000)",
            "value": 760121.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 10632,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100644.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000637,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10524,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100512.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000526.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=10000)",
            "value": 24.8,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=10000)",
            "value": 131.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 10168,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96580.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960585.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 10048,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96472.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960462.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=10000)",
            "value": 1.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=10000)",
            "value": 56.8,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Daniel Sun",
            "username": "daniellansun",
            "email": "sunlan@apache.org"
          },
          "committer": {
            "name": "Daniel Sun",
            "username": "daniellansun",
            "email": "sunlan@apache.org"
          },
          "id": "cb27031d8e432f868d54b19ac290d0e61769c78e",
          "message": "GROOVY-12180: Bump antlr to 4.13.2.9",
          "timestamp": "2026-07-19T08:53:23Z",
          "url": "https://github.com/apache/groovy/commit/cb27031d8e432f868d54b19ac290d0e61769c78e"
        },
        "date": 1784537029466,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "countA_captureClosure (size=100)",
            "value": 2512,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=1000)",
            "value": 24112.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=10000)",
            "value": 240113.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 10192,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96592.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960598.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 10072,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96472.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960478.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=10000)",
            "value": 112.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=100)",
            "value": 88,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=1000)",
            "value": 88.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=10000)",
            "value": 88.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=100)",
            "value": 2952,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=1000)",
            "value": 28152.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=10000)",
            "value": 280153.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 10632,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100632.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000638.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10512,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100512.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000518.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=10000)",
            "value": 24.8,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=10000)",
            "value": 139.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 10192,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96592.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960598.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 10072,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96472.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960478.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=10000)",
            "value": 3.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=10000)",
            "value": 112.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Daniel Sun",
            "username": "daniellansun",
            "email": "sunlan@apache.org"
          },
          "committer": {
            "name": "Daniel Sun",
            "username": "daniellansun",
            "email": "sunlan@apache.org"
          },
          "id": "532d38577bb93e6f13bf1ec9ecba84aad02f463f",
          "message": "Detect CI via env vars so Windows/macOS GHA get maxParallelForks=1\n\nPath-only detectCi matched Linux GHA (/home/runner/work/) but not\nWindows (D:\\a\\...) or macOS (/Users/runner/work/), so those runners\nran test workers with local maxParallelForks and saw intermittent\nMessageIOException connection resets during worker teardown.",
          "timestamp": "2026-07-26T11:50:11Z",
          "url": "https://github.com/apache/groovy/commit/532d38577bb93e6f13bf1ec9ecba84aad02f463f"
        },
        "date": 1785142631903,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "countA_captureClosure (size=100)",
            "value": 2512,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=1000)",
            "value": 24112.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=10000)",
            "value": 240113.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 10192,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96592.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960598.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 10072,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96472.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960478.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=10000)",
            "value": 113.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=100)",
            "value": 88,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=1000)",
            "value": 88.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=10000)",
            "value": 88.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=100)",
            "value": 2952,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=1000)",
            "value": 28152.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=10000)",
            "value": 280153.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 10632,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100632.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000650.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10512,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100512.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000518.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=10000)",
            "value": 25.8,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=10000)",
            "value": 140.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 10192,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96592.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960598.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 10072,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96472.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960478,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=10000)",
            "value": 1.7,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=10000)",
            "value": 112.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "committer": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "id": "8c02dbd62392a10a246fcb280150efd2b0f54e7d",
          "message": "GROOVY-12227: GeneratedDispatcher: avoid runtime class definition so\npacked closures work in native images\n\nTwo layered changes to the GROOVY-12151 packed-closure machinery (GEP-27):\n\n1. ClosureWriter now emits into each packed hosting class a private\nstatic $packedDispatchersFactory$ whose body adapts the class's three\nprivate static dispatch tables to their functional interfaces through\nordinary bytecode-level LambdaMetafactory invokedynamic sites and\nreturns them as one Bundle. The dispatcher accessor's bootstrap becomes\nthe new four-argument IndyInterface.packedDispatchers, which receives\nthat factory as a CONSTANT_MethodHandle bootstrap argument.\n\n2. GeneratedDispatcher.bootstrap (four-argument form) just invokes the\nfactory into a ConstantCallSite: linking needs neither a runtime\nLookup.findStatic nor a programmatic LambdaMetafactory call — an\nundeclared reflective lookup and a run-time class definition, the two\noperations ahead-of-time runtimes restrict, so the linkage suits any\nsuch environment. Under GraalVM native image, the verified case, the\nfactory's sites are pre-processed at image build time and its method\nreferences reach the class's own private tables without reflection\nmetadata.\n\nOn a regular JVM the linkage is equivalent: the VM spins the same three\nhidden classes when it links the factory's sites, with the cost moving\nfrom bootstrap-time programmatic LMF to first-invocation indy linkage\n(per packed class: one extra synthetic method, three indy sites, one\nbootstrap argument; the accessor shape is unchanged). The Bundle\nconstructor is publicized because the compiler-emitted factory in the\nhosting class's own package constructs it directly.\n\nThe previous three-argument bootstrap is retained verbatim for class\nfiles emitted by earlier 6.0 pre-releases, which keep linking through\nfindStatic plus programmatic LambdaMetafactory. Conversely, class files\nin the new format need this runtime to link; both directions only\nconcern unreleased 6.0 snapshots.\n\nVerified on GraalVM 25.2.4 (native-image 25.0.4): the packed repro that\npreviously failed with 'Classes cannot be defined at runtime ...\nM$$Lambda...' now runs correctly (single emitted class, 30MB image,\n~12ms total run time), and the tracing agent records zero\npackedDispatch entries for the new bytecode.\n\nPackedDispatcherFactoryTest covers every dispatch shape and the\npropagation of undeclared checked exceptions through both class-file\nformats -- the legacy format recreated by downgrading a freshly\ncompiled accessor's bootstrap reference from the four-argument to the\nthree-argument form, the only difference between the two formats. The\ntest serializes its system-property mutation against other\nproperty-touching tests with @ResourceLock(SYSTEM_PROPERTIES).",
          "timestamp": "2026-08-03T21:12:19Z",
          "url": "https://github.com/apache/groovy/commit/8c02dbd62392a10a246fcb280150efd2b0f54e7d"
        },
        "date": 1786351159857,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "arrayCollectA_captureClosure (size=100)",
            "value": 1480,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "arrayCollectA_captureClosure (size=1000)",
            "value": 15104.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "arrayCollectA_captureClosure (size=10000)",
            "value": 169049.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "arrayCollectC_functionLambda (size=100)",
            "value": 1400,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "arrayCollectC_functionLambda (size=1000)",
            "value": 15048,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "arrayCollectC_functionLambda (size=10000)",
            "value": 168968.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "arrayCollectD_functionMethodRef (size=100)",
            "value": 1400,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "arrayCollectD_functionMethodRef (size=1000)",
            "value": 15048,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "arrayCollectD_functionMethodRef (size=10000)",
            "value": 168968.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "arrayCollectE_baseline (size=100)",
            "value": 440,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "arrayCollectE_baseline (size=1000)",
            "value": 4040,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "arrayCollectE_baseline (size=10000)",
            "value": 40040.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "collectA_captureClosure (size=100)",
            "value": 1488,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "collectA_captureClosure (size=1000)",
            "value": 15112.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "collectA_captureClosure (size=10000)",
            "value": 169056.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "collectC_functionLambda (size=100)",
            "value": 1400,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "collectC_functionLambda (size=1000)",
            "value": 15056,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "collectC_functionLambda (size=10000)",
            "value": 168969.7,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "collectD_functionMethodRef (size=100)",
            "value": 1400,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "collectD_functionMethodRef (size=1000)",
            "value": 15056,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "collectD_functionMethodRef (size=10000)",
            "value": 168969.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "collectE_baseline (size=100)",
            "value": 440,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "collectE_baseline (size=1000)",
            "value": 4040,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "collectE_baseline (size=10000)",
            "value": 40040.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=100)",
            "value": 2512,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=1000)",
            "value": 24112.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countA_captureClosure (size=10000)",
            "value": 240113.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 10192,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96592.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960598.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countE_baseline (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 10096,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96472.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960490.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countG_lambdasCurryWith (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countH_closuresCurryWith (size=10000)",
            "value": 56.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=100)",
            "value": 88,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=1000)",
            "value": 88.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findA_captureClosure (size=10000)",
            "value": 88.8,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=100)",
            "value": 2952,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=1000)",
            "value": 28152.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllA_captureClosure (size=10000)",
            "value": 280153.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 10632,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100632.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000638.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllC_biPredicateParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllD_methodRefParam (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllE_baseline (size=10000)",
            "value": 24.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10524,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100524.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000529.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllG_lambdasCurryWith (size=10000)",
            "value": 25.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=100)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=1000)",
            "value": 24,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllH_closuresCurryWith (size=10000)",
            "value": 139.6,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 10204,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96604.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960598,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findC_biPredicateParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findD_methodRefParam (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findE_baseline (size=10000)",
            "value": 0.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 10096,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96472.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960489.6,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findG_lambdasCurryWith (size=10000)",
            "value": 1.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=100)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=1000)",
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=10000)",
            "value": 112.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "injectA_captureClosure (size=100)",
            "value": 1408,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "injectA_captureClosure (size=1000)",
            "value": 15808.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "injectA_captureClosure (size=10000)",
            "value": 159809,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "injectC_biFunctionLambda (size=100)",
            "value": 1296,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "injectC_biFunctionLambda (size=1000)",
            "value": 15696,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "injectC_biFunctionLambda (size=10000)",
            "value": 159696.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "injectD_biFunctionMethodRef (size=100)",
            "value": 1296,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "injectD_biFunctionMethodRef (size=1000)",
            "value": 15696,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "injectD_biFunctionMethodRef (size=10000)",
            "value": 159696.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "injectE_baseline (size=100)",
            "value": 16,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "injectE_baseline (size=1000)",
            "value": 16,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "injectE_baseline (size=10000)",
            "value": 16.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          }
        ]
      }
    ]
  }
}