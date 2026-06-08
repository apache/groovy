window.BENCHMARK_DATA = {
  "lastUpdate": 1780910596674,
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
      }
    ]
  }
}