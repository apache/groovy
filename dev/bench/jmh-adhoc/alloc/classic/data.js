window.BENCHMARK_DATA = {
  "lastUpdate": 1782724870781,
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
      }
    ]
  }
}