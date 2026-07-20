window.BENCHMARK_DATA = {
  "lastUpdate": 1784537027319,
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
        "date": 1780368623282,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.23043568021563585,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02279184008590348,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0022515859146723712,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.11302753634630666,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.01273136483715163,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.001254680379022056,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.5622077175400033,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.14706639400081728,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.014999270277272842,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.5673129848243288,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.14518711061372863,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01481968032003136,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.4606056734328878,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.13815308464600934,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01425240174271818,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.12331680993270866,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.013532589673910498,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0013288462649323827,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.5662376570275247,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.14788263465752355,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.014685844149696143,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7785818189898621,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.2038638701190943,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.018801556564317166,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2518130705225327,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02580367024588808,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0025230988000145066,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.23143660144938857,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02384034343970986,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0023084399008286584,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.11927581790907467,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.012420023462240049,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.001265333019566235,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.591000134809691,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.14931488518117628,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.014760507662376373,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.577892689393813,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.1464738426926641,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01516685030535112,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6339329773884372,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.16737801945881275,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01668231560293638,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.12259282012595926,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.013300248346004472,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.001363031482600768,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.5771817783530786,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.14060532471203396,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.014728604909940565,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6857982045781221,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.19025201824419863,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015160782477572018,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.11866615040117008,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.012932944177428468,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0012946386693730668,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.4670358743764758,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.138595220363894,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.014848191408958794,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.4841882622041178,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.13958387474190087,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.014785312118064064,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.4711082449245894,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.13840314697981565,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01458903228139474,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.1211926151820127,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.013316291188338622,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0013550042559771116,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.458728537730607,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.14709433529694524,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01457525655915744,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.0462224574051717,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.15479053586584673,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.0150722253626648,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
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
        "date": 1780368623282,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.23043568021563585,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02279184008590348,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0022515859146723712,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.11302753634630666,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.01273136483715163,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.001254680379022056,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.5622077175400033,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.14706639400081728,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.014999270277272842,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.5673129848243288,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.14518711061372863,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01481968032003136,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.4606056734328878,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.13815308464600934,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01425240174271818,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.12331680993270866,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.013532589673910498,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0013288462649323827,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.5662376570275247,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.14788263465752355,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.014685844149696143,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7785818189898621,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.2038638701190943,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.018801556564317166,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2518130705225327,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02580367024588808,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0025230988000145066,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.23143660144938857,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02384034343970986,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0023084399008286584,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.11927581790907467,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.012420023462240049,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.001265333019566235,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.591000134809691,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.14931488518117628,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.014760507662376373,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.577892689393813,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.1464738426926641,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01516685030535112,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6339329773884372,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.16737801945881275,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01668231560293638,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.12259282012595926,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.013300248346004472,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.001363031482600768,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.5771817783530786,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.14060532471203396,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.014728604909940565,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6857982045781221,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.19025201824419863,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015160782477572018,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.11866615040117008,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.012932944177428468,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0012946386693730668,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.4670358743764758,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.138595220363894,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.014848191408958794,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.4841882622041178,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.13958387474190087,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.014785312118064064,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.4711082449245894,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.13840314697981565,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01458903228139474,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.1211926151820127,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.013316291188338622,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0013550042559771116,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.458728537730607,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.14709433529694524,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01457525655915744,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.0462224574051717,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.15479053586584673,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.0150722253626648,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
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
        "date": 1780375354007,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2464434015325434,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02467520262593805,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002386467239103899,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12309476075134071,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013737441334068784,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013648031928931818,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6324267454408363,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16536746212958162,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015379651666824656,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6270176737798405,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16477501060782002,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015695609349139612,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6088593616643057,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.1565124823435271,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015022965245936737,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.13191316423788518,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014580990680433304,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.00143999427880103,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6595430641639726,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1653253817335768,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01536314601455489,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.9823874865726325,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.21566979941059858,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.019852099071276167,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2686118716890463,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.026788248802028902,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.00265238789256043,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.24596436949657616,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.025111971486700867,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002418966363281142,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.1189545356626196,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013452047746170103,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013570670192990348,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.685186676610599,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16298129155667773,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01558775703058709,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6560684651192275,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16481954814213245,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01583662992065244,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.7672653341490037,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.1814504520167692,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.017061480336351503,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.13322142909242427,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.012906595162790843,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.001415856283760366,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6734730507334834,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1671687424762665,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015679144002555163,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7335977187884464,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.19751403882840185,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015400683200830514,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12632831148471374,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013568672723203553,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013752130483032266,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6245861917933087,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15147823586894663,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015607528527109307,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6021872959005647,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15118023677322914,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015553814516434528,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.5459225449898075,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.15832007606711343,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015791033102918857,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.11531330455662328,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014683829483526537,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0015017900843575047,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.5941067896775514,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1594680141344296,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015106154528343374,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.095561454261551,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1892354453330048,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.016715077031194788,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
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
        "date": 1780395613618,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.24184945612811296,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.024049077325935857,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0024457443495541466,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12492738683646179,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.01368045831956482,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.001388695539748109,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6447529969092873,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16476666880905513,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015141225400220809,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6318233033431375,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16444810502123705,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015508908814262604,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.5700935157529736,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.15786859618913657,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015151094725427139,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.12866341767090267,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014353843021069035,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0013756140589729895,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.640514821981951,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16292010527951273,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015012132763859631,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.2398761533842464,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.21774815096403097,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.020312115245885397,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.26725676776335494,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.026702372750205778,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002661012127114247,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.24558791788753878,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.023939263531265853,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0024482502048331373,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12310657863007468,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013480684913620266,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013476593706387716,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.648591235828358,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.169661986707068,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01527362470412317,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.7097926333351112,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.1623805112292901,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015516111587278513,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.7754047288055865,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.1832512060991278,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.016937958160268763,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.12984745720312052,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.013926404021226423,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014374423158994198,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6528092718143061,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.166090452838707,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015299928173295032,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6772575580635245,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1911899480801111,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015902250391416226,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12453947458405172,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013404637580746958,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013758686956817417,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6209197490386447,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.1502906449696604,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015296710733247352,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6259779625177544,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15112984659524706,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01557697143886454,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.5642145647526666,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.16081817447029756,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01573833503757508,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.13581315723891718,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014874339887455049,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.001478511325586292,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.5652334138182575,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.15661562590617556,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015178325706072382,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.101823531692762,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.188385823618261,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.018209749608767128,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
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
        "date": 1780910595815,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.24525003304599044,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.024774452146917692,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0024452812886014688,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12552716175951809,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013601510523330326,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013554819764947288,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6570980578019507,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16510547038300707,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01544638886129425,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6381655752405737,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16661396170037215,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015796090835365813,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6056886958248355,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.15983577534583765,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.014963205167307245,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.1229281288490071,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014329311697530411,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014231720852133662,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6417453835936868,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16666615747698887,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015597766564465982,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.168499412480853,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.21568325957315487,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.020035046745729167,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2560931142178463,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.027001088965350927,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002659930240298314,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2495152224592716,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.024892210925175705,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002225210676944933,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12361422689471809,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013533395297986212,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013287271099425544,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6908649784087775,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16308828575091866,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015393098704681729,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.688319972748658,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.1670028886290966,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015491606852211732,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.8509833325696134,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.18144350586154775,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01707346113750049,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.13140784470531153,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.013878870393943948,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014174239548880273,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6815702945461388,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16704014850842194,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.0156726213909923,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7304453635874537,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.19109918984478805,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.016503019383363092,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12492363738862737,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.0136424818828574,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.001378190154207204,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6288907905093097,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.1517366412585617,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015570889304754632,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6226712668227783,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15577253300623553,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01569190093204314,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.525949543194177,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.1584709366388352,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.016294126358235093,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.13586615740497493,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014670514545313262,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014802644588902173,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.584136036019311,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16249036329664873,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015289014395912643,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.028919835320779,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.18630584446347648,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01591897237307978,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
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
        "date": 1781517243848,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2438522186822849,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.024371538897887388,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0024275797210850923,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12419316823320463,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013724470747852071,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013633946219378033,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.643390732003283,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16363241353367752,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015601432167512707,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6598983463921342,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16430353887943475,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015776674055605344,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.5701054334389355,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.16120940256840424,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015079943589242606,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.13257828498459995,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014331904689093167,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.001441610041928817,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6522132538431424,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16447113671098712,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015293943532358,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.2321325819244535,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.2164790246977942,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01954092257694549,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.26521436467528636,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02700028970227967,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0026342628119560086,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2458849631517527,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02501291234390473,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0024403560748582093,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12295770091302256,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013299867593268663,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013385939860108948,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.720846540373919,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.1620472954683601,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015868400213792915,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6878668804135273,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16576900038203995,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015746941744841446,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.8451569504338843,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.1857114273036759,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.016923897192434166,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.13122186100843974,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.013566365058289846,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014211833399325022,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.705360631560172,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1628898626408934,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01524723607406837,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7881486211078528,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.19724460152298473,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015550005050060123,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12659901387583122,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013599728043244757,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013633769935761269,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6282327589270955,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15557505325075455,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015646698961385704,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6144571960849727,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15127190908031035,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015568969829132806,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.5639687856789743,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.15953968270224844,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015415060123378583,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.13244740668453342,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014619627325550883,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014657450081264028,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.5587663401598773,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.157518052955743,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01525932061846807,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.1717673725960323,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.18889877526193294,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.0178314076274967,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
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
        "date": 1782123355144,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2442219686697303,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02460780487686468,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0024462562532097804,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12321805858627352,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013651925070152319,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.00134279597671764,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6579777118964671,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16516193229325832,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015601699824885629,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.658126002797255,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.14815958233418242,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015410209218662835,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.5704557979632037,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.15900732220901856,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015004444860350043,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.13478075842290657,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014515880084932587,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014197752715817422,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6576864563427243,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16576632400667043,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015637236945647094,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.2391775299307612,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.2151716619870391,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.019285825624146883,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2670169740568654,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.027048556098360533,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0026756697892859394,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2484482704545854,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02499534858465711,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002414807672533435,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12160424316752397,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013440251106088461,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.001233110518234873,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.7250281261297178,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.1685654242328181,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015292800972527883,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.5900511531974968,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.1657704378549304,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015248319369934668,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.850772816934866,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.18108511666434862,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.016979517593090285,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.1271840027189807,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.01398359668609873,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014198145231349775,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.675102444099938,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16655564203002865,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015110853470385367,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.78391283217409,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.18569461435960977,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015994236951249845,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12742251235549695,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013584964332461313,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.001376537026480071,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6006237264843552,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15473435064837276,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015335610312492943,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.620853545229377,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15354538025122938,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015531343850970469,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.5454595152252377,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.1583489039894374,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015695535253092315,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.131147103740632,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014600955478770775,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014272120143144068,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6045130488046973,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1621651010358592,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015023656353000018,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.096270716719515,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1854429590980388,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01585993112534738,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
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
        "date": 1782724866669,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.23382412499820723,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.023122819910657184,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002352362369656709,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.1186313863701243,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013295542566674876,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013081031687880759,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.5086843613266274,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15258922220439539,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015255602971355623,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.567828577207027,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15265738612801802,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015301876345594434,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.5045456212901431,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.14855629214017246,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.014546380197830921,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.12576692029986195,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.01368734077973631,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0013736777600227674,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.5593079959773894,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.15167568453719868,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015352176708629553,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.2969503276157646,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.2160384764827082,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.020477030428053277,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2563706135989512,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.026537226639618577,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0025818189974388126,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.23447980468189203,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02391076341167043,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0023599454355664696,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12051001735633245,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013105148570078076,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.001312614745742571,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.5983986852329373,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15312911328108358,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01563670867946525,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.5994453684189294,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15282211594207548,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015197291680995668,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6951837689113483,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.17195863524846028,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01733307558742319,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.12586415621938923,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.01220084755499733,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0013599268305163358,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.576859699806288,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.14712553466751296,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015438966137644405,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.689174486469435,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1939771895304872,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015506240496073803,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12076219526776719,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013294853940674398,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013227925675725176,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.4605836093921205,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.14151661172342736,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015322980609746111,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.4724110630826768,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.14363104651733027,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015400379243394038,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.4634913004046166,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.14363678162471688,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.014670088594753938,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.12947998988856477,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.01392370928147572,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0013893959884662068,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.4587211260757564,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.14817290817532558,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015365776737041204,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.0891754800198656,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.18159286186181645,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.016906019345597746,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
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
        "date": 1783330282203,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.23374204651222424,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.024553344241300266,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002428800504833454,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12261959204262071,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013651970077834452,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.001389468347461958,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6403170361103032,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16455275883601542,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015355443423193527,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6467352817006151,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16554418563056697,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015379507345042124,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.55097626281905,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.15560743460787307,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01480393483299675,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.13055773247104802,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.01430926855672785,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014043660610776405,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6490607273458173,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16817298940400685,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015411498295588008,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.2257747726864694,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.21461280681135925,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.019419273149542525,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.26291034857550616,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02679565431130356,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0026455822353970014,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2451765485028084,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.024958831223617008,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.00242865446346327,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12237829039865086,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013546793615206231,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013491586625322283,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6410651881819214,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16627476518294873,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015300833006610365,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.721968121216403,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16425181001946776,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01553659544559328,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.7209762465459453,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.18011776443005975,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01669992752979184,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.13096168956588314,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.01412731013232428,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014001761712693407,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6976393789221025,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1656951617637202,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015165300888356584,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7872918669634525,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1853554955177164,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01522707115806412,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12667150387862475,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.01335946886879191,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013766400232124657,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6181968870678862,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.1510711856522799,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015577251766907002,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6196684178787126,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.1502410122024474,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015238747774204534,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.5577826757850042,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.1582101615928335,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015789712853724398,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.13644046660448095,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014592212949784642,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014749800891995547,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.5805230812216928,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16222354376527404,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015363279097657098,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.098251352265612,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.18510273933900845,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.0166356708581559,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
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
        "date": 1783932478918,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.23942170149740902,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02395082850880474,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0023782665558044957,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12189378412012868,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013658342072166893,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013626751305427574,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6329594372006393,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16329227100079174,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01545519500879675,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6359776615404165,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16313070528105172,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01535856463320795,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.5546450740256015,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.1581468440151407,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.014755741014587834,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.13079083967158392,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014281651644352198,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0013956668334422213,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6516213495324006,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16465043540483762,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015235950660477827,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.231883964013043,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.2117085979639491,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.019359743776151142,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2614823677163143,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.026221536903048674,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0025871944745776794,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.23423499645293938,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.024521656473893955,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002383495933460659,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12315152341593327,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013257459118036899,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013410222062987046,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.720480703955302,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16686275430226083,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015467326679401053,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.7144643459328157,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.1651884538159676,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015864555146621183,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.7263396771657191,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.18038988920211785,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01686846720595564,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.1277864092756378,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.013747339260706842,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014199906260844342,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.674788064375973,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16585718073833342,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015383935343650434,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7502225183438012,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1909437296832598,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01550010805316537,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12445456976778528,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.012976878582798595,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013136263839508987,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.5936552898756762,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.1509596983724134,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01518237469183871,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6200871649632496,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15101218389238952,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015614440955084541,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.541891226856609,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.10879722679287865,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015369334436184967,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.1321956742641504,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014108901894774855,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014435593860302961,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.5662188426743486,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16010598964429668,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015460431246768514,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.0253425654394195,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.18479740703998976,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.016896618696171588,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
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
        "date": 1784537025335,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.24510925892901173,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.026139279193163258,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002534476667900421,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.09822053954813222,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.010280830294049985,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0010229994218973488,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.5655969236918612,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15146395500981674,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015338429275608339,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.5724286245945347,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15224174324751544,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01529433656352835,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.4644608890245085,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.14967532788993232,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.013964771648070671,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.1041565883804608,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.010801807432024305,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.001081386002768373,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.558820652088976,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.15345355610652595,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015200767892139708,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.387899650800928,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.2036420579205584,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.018688315180561126,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.38615625840767737,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.040282508438785794,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.003948605831693401,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2521080253290503,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.025419855153897643,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0024896828148869687,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.09809585750951266,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.010291704772796575,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.001033364199979759,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6057667336209565,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15820578627237297,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.014959883444762298,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.5924945652302325,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.153980369421978,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015093810370032485,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6932210822424971,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.1730226858352116,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01683057789332907,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.1041210850281589,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.010836527023375741,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0010756571916549524,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.5725439877969163,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.14756078681035184,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015184644495641556,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.665700742733517,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.18485560195763903,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015388853942643516,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.10030374710357022,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.010657375539420966,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0010608341743031393,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.4837290597202917,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.1416811659294238,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01504245169480145,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.4702969803035233,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.1415360077191575,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015421289721035352,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.4963845202859514,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.14533340084180923,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01524756505419178,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.10521078248072324,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.01105700639004802,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0010907781507788396,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.4539374416325113,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.14930246474361947,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015072772974530704,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.980165492086766,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.17912724415653827,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015566150676479349,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          }
        ]
      }
    ]
  }
}