window.BENCHMARK_DATA = {
  "lastUpdate": 1786955269368,
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
        "date": 1783932504301,
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
            "value": 960210.7,
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
            "value": 96172.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960154.6,
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
            "value": 76120.2,
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
            "value": 1000250.7,
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
            "value": 100212.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000194.6,
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
            "value": 9752,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=1000)",
            "value": 96165.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960154.6,
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
            "value": 0.5,
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
        "date": 1784537019595,
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
            "value": 240113,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 11408,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 112209.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 1120211.4,
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
            "value": 96184.6,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960155.5,
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
            "value": 89.1,
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
            "value": 280153.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 11848,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 116249.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1160251.4,
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
            "value": 100226.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000195.4,
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
            "value": 27.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 11408,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 112208.8,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 1120211.3,
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
            "value": 96173.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960155.4,
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
        "date": 1785142564486,
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
            "value": 240113.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 11408,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 112209.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 1120211.2,
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
            "value": 960155.3,
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
            "value": 112.6,
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
            "value": 89.2,
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
            "value": 280153.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 11848,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 116249.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1160251.2,
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
            "value": 100224.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000194.9,
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
            "value": 139.8,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=100)",
            "value": 11408,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 112208.8,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 1120211,
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
            "value": 96171.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960155,
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
            "value": 63.2,
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
        "date": 1786351187908,
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
            "value": 169049.2,
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
            "value": 169056.7,
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
            "value": 168970,
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
            "value": 168970.1,
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
            "value": 240113.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=100)",
            "value": 11408,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=1000)",
            "value": 112208.4,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countB_rcurryClosure (size=10000)",
            "value": 1120211.1,
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
            "value": 96181.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960155.1,
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
            "value": 112.5,
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
            "value": 89.2,
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
            "value": 280153.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=100)",
            "value": 11848,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=1000)",
            "value": 116249.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1160251.1,
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
            "value": 100222.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000195.1,
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
            "value": 11408,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=1000)",
            "value": 112208.3,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 1120211.4,
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
            "value": 96171.7,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960155.1,
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
            "value": 112.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "injectA_captureClosure (size=100)",
            "value": 1384,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "injectA_captureClosure (size=1000)",
            "value": 15784.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "injectA_captureClosure (size=10000)",
            "value": 159785.3,
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
            "value": 48.1,
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
          "id": "3a40cf9ad60c61176de41884037cec0fef68b750",
          "message": "Bump org.sonarqube from 7.3.1.8318 to 7.4.0.8496\n\nBumps org.sonarqube from 7.3.1.8318 to 7.4.0.8496.\n\n---\nupdated-dependencies:\n- dependency-name: org.sonarqube\n  dependency-version: 7.4.0.8496\n  dependency-type: direct:production\n  update-type: version-update:semver-minor\n...\n\nSigned-off-by: dependabot[bot] <support@github.com>",
          "timestamp": "2026-08-16T01:22:51Z",
          "url": "https://github.com/apache/groovy/commit/3a40cf9ad60c61176de41884037cec0fef68b750"
        },
        "date": 1786955267891,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "arrayCollectA_captureClosure (size=100)",
            "value": 1456,
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
            "value": 169048.6,
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
            "value": 168968.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "arrayCollectD_functionMethodRef (size=100)",
            "value": 1412,
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
            "value": 168968.2,
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
            "value": 169056.7,
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
            "value": 168970.9,
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
            "value": 168970.4,
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
            "value": 240114.7,
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
            "value": 960210.9,
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
            "value": 1,
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
            "value": 32.1,
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
            "value": 96179.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "countF_sharedRcurry (size=10000)",
            "value": 960155,
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
            "value": 0.1,
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
            "value": 56.1,
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
            "value": 89,
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
            "value": 280152.9,
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
            "value": 100249.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllB_rcurryClosure (size=10000)",
            "value": 1000251.1,
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
            "value": 100220.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findAllF_sharedRcurry (size=10000)",
            "value": 1000195.1,
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
            "value": 80.1,
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
            "value": 96208.5,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findB_rcurryClosure (size=10000)",
            "value": 960210.9,
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
            "value": 32.1,
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
            "value": 96172.2,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "findF_sharedRcurry (size=10000)",
            "value": 960155.1,
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
            "value": 0.2,
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
            "value": 56.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "injectA_captureClosure (size=100)",
            "value": 1384,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "injectA_captureClosure (size=1000)",
            "value": 15784.1,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          },
          {
            "name": "injectA_captureClosure (size=10000)",
            "value": 159784.6,
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
            "value": 48,
            "unit": "B/op",
            "extra": "B/op (gc.alloc.rate.norm)"
          }
        ]
      }
    ]
  }
}