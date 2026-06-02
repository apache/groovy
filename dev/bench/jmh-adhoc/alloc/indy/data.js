window.BENCHMARK_DATA = {
  "lastUpdate": 1780368626039,
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
      }
    ]
  }
}