window.BENCHMARK_DATA = {
  "lastUpdate": 1783330226198,
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
        "date": 1780368625494,
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
            "value": 9808,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96208.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960210.5,
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
            "value": 0.3,
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
            "value": 32.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 9776,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96172.6,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960154.5,
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
            "value": 52.8,
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
            "value": 720081.2,
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
            "value": 10260,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100260.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000250.5,
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
            "value": 56.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10192,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100226.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000206.5,
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
            "value": 26.5,
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
            "value": 82.8,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 9820,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96220.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960222.5,
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
            "value": 32.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 9764,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96180.8,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960166.5,
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
            "value": 9,
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
        "date": 1780375385587,
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
            "value": 720081.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 9808,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96208.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960222.4,
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
            "value": 32.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 9764,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96169.7,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960154.4,
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
            "value": 720081.2,
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
            "value": 760121.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 10260,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100248.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000262.5,
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
            "value": 56.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10192,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100210.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000206.5,
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
            "value": 131.8,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 9832,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96232.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960210.4,
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
            "value": 32.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 9752,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96165.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960178.5,
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
          "id": "653195f4057c7f77032b10b766562502db2705b4",
          "message": "Use log y-axis for JMH adhoc throughput charts\n\nLarger workload sizes span ~2 decades of throughput, so a linear axis\nflattened the size=10000 group into indistinguishable slivers despite a\nreal ~12x best-vs-worst spread within each size. Matches the allocation\ncharts, which were already log.\n\nCo-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>",
          "timestamp": "2026-06-02T06:58:05Z",
          "url": "https://github.com/apache/groovy/commit/653195f4057c7f77032b10b766562502db2705b4"
        },
        "date": 1780395591282,
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
            "value": 720081.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 9820,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96208.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960210.4,
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
            "value": 32.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 9752,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96182.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960166.4,
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
            "value": 104.6,
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
            "value": 720081.2,
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
            "value": 760121.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 10260,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100248.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000262.5,
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
            "value": 56.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10192,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100224,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000218.5,
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
            "value": 25.5,
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
            "value": 76,
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
            "value": 9808,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96208.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960210.4,
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
            "value": 32.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 9752,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96178.6,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960166.4,
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
            "value": 0,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findH_closuresCurryWith (size=10000)",
            "value": 57,
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
        "date": 1780910612709,
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
            "value": 720113.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 9820,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96220.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960210.6,
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
            "value": 32.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 9764,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96201.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960166.6,
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
            "value": 104.2,
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
            "value": 10248,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100248.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000262.7,
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
            "value": 56.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10204,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100229.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000194.7,
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
            "value": 24.2,
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
            "value": 130.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 9820,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96220.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960210.7,
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
            "value": 32.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 9764,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96195.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960154.7,
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
            "value": 0.7,
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
            "value": 57,
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
        "date": 1781517243621,
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
            "value": 720081.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 9808,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96220.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960222.4,
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
            "value": 32.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 9776,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96166.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960178.4,
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
            "value": 720081.2,
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
            "value": 760121.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 10248,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100260.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000274.5,
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
            "value": 56.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10216,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100222.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000218.4,
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
            "value": 26.1,
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
            "value": 131.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 9808,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96208.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960210.4,
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
            "value": 32.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 9752,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96173.6,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960154.4,
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
          "id": "9488b37ca09b89db266a82453f34e1e3f7b75a72",
          "message": "use basic cache-provider",
          "timestamp": "2026-06-21T04:04:22Z",
          "url": "https://github.com/apache/groovy/commit/9488b37ca09b89db266a82453f34e1e3f7b75a72"
        },
        "date": 1782123330769,
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
            "value": 720081.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 9820,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96220.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960222.4,
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
            "value": 32.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 9764,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96178,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960166.4,
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
            "value": 104.7,
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
            "value": 720081.2,
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
            "value": 760121.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 10248,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100260.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000262.4,
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
            "value": 56.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10216,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100219.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000218.5,
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
            "value": 131,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 9808,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96220.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960210.5,
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
            "value": 32.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 9764,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96187.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960166.4,
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
            "value": 56.7,
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
        "date": 1782724888396,
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
            "value": 720081.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 9820,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96208.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960222.4,
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
            "value": 32.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 9776,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96190.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960166.4,
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
            "value": 720081.2,
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
            "value": 760121.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 10260,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100248.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000250.4,
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
            "value": 56.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10204,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100207,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000206.5,
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
            "value": 82.9,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 9832,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96220.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960234.4,
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
            "value": 32.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 9764,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96175.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960166.5,
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
            "value": 2.1,
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
        "date": 1783330225074,
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
            "value": 720081.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 9820,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 96232.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 960210.4,
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
            "value": 32.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=100)",
            "value": 9764,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=1000)",
            "value": 96190.8,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960178.4,
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
            "value": 104.4,
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
            "value": 720081.2,
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
            "value": 760121.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 10248,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 100272.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000262.4,
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
            "value": 56.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=100)",
            "value": 10204,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=1000)",
            "value": 100206.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000206.4,
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
            "value": 25.6,
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
            "value": 9820,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 96220.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960210.4,
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
            "value": 32.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=100)",
            "value": 9752,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96186.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960166.5,
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
            "value": 56.7,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          }
        ]
      }
    ]
  }
}