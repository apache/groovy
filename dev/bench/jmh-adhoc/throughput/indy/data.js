window.BENCHMARK_DATA = {
  "lastUpdate": 1787559953826,
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
        "date": 1780368622840,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.25891032573148165,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.026011351001727873,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.00253095886251715,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.13860730566775653,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013977850094711405,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013879043362869904,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.5607014776120685,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15290500026590584,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015348178595372916,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.553756208711896,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15339650283216635,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.012821076082703733,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6645599088973282,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.16584098469923683,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015400401245275502,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.13864801316866845,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.013895434644658089,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.001415755030067055,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.5652576158911846,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.15479323511314227,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.0159862659742085,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.2363300238372537,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.20215358229445193,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.019288262603774222,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.282837206475172,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.028524297369958436,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002827024981426794,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2606058934147522,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.025827881390782043,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002573533250340023,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.13681328136598492,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.0136035697746127,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.001392886679044655,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6040235879020255,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15432402034604367,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01540732848156362,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.5822869129505654,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15135004135224336,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01547892705326696,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.57035840999747,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.15651150362493255,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.014997111284003795,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.1383458756135692,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.01370569099944518,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0013894161067596735,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.573927199193364,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.14803477455620903,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015326678083474607,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.688756650054146,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1931644373456613,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.016141771890646132,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.13416995415962824,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013982563399079004,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0014050875125739318,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.4674796053528298,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.14143808454561907,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015149676487677377,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.4682304788871297,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.14154738138358663,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015413480809664542,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6471354699036116,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.15555071056892014,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01529305763885,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.1377528434820402,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.01397309039189692,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0013728948439475038,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.4868195201227732,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.14632753935619983,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015070273941472164,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.9836962273522634,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.18569409795553077,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.017795391430507818,
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
        "date": 1780375381720,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2694543768394789,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02742292525379625,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002623367211498732,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.1445726915726417,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.014583074926720913,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0014482658172035837,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.652557153639052,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16479753015285734,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015104666323770827,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6432284282364242,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16641878193699466,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015225596311048204,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.7675033611828375,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.177338549149859,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015436013118569616,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.14478254869948412,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014535573235624802,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014348657080222984,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6395239298161646,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16477630943033464,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015437381465805764,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.1686689167289743,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.2208157196428461,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01931616083301117,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.303008127995985,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.03008055457865493,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0029826039076215095,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2704363866943655,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02750973642200228,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002727515679001854,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.145314748852148,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.014416503948137302,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0014094414873996378,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.7242608065180545,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16669573935239707,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01566010196955657,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6541222176847128,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16269379087265518,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015465043475050308,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6902523708535782,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.17835515751252426,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01474667929892772,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.14297489451455198,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014376230372731644,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014087383989704104,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6768964909876825,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16643153151448542,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01584411443130736,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7546726941859894,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.19710091879271593,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015293415386771153,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.14021394779758356,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.01449016348830286,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0014513918680081235,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6268660239464598,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.1507356110133618,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015231903698236529,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.625341216361831,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15140452280425365,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015691669416367342,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6990000601007549,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.17054719288126202,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01530059081680299,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.1445728010413328,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014547797485348665,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014125284354960066,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.615664992385136,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.15773589372222518,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015501236919417044,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.023265566095599,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.19236779078316196,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01687025928922176,
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
        "date": 1780395586831,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.26896734363271363,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.027903291258884227,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0026784023682165916,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.14444906417379783,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.014094826232626746,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0014791224291713673,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6465593354679218,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.1639642786881316,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01576648658342958,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.660015129784331,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16490437782910491,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015385331089530213,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.7800916415354817,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.17683550964028902,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015632679560786327,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.1481962283588074,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014551166796247553,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014608870272441758,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.673194060115571,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.165045101140789,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01586910965019076,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.2309104287803843,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.21438652503038108,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01857220484609033,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.300066404809983,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.028602678174614878,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0029986327180906163,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2457218514331286,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02752658317434158,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0027538464779992954,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.14019549144063814,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013912389984617005,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0014051467088798586,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.65470692777209,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.168910449087045,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015799909276040785,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6864864042510999,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16616992355691526,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01573159226664573,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6838058900972428,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.16626655052317654,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.014859194837518385,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.14524447229190196,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.01427082715004974,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.001428389047298992,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6770567977531132,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16662343098191482,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015707467891761445,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7565360985358904,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.18857517079024227,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01573148627813554,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.14077943679162003,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.014782626573241512,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0014577837071091744,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6247299260324155,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15105053255637482,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015640052099993845,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6276977893668612,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.151257459516614,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015662542494036893,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6976407446602604,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.17292298562285596,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01446494310479451,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.1448477315819287,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014362634540190777,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014400934771556866,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.5863498654402313,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1625370289266933,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015499759094389623,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.0331587979436727,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.18754973945196693,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.016798477800968253,
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
        "date": 1780910608474,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.24025136472073583,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.023776841197832507,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002375947083099806,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12852600883411192,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013310046368835548,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013504849651386668,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.7513058586763677,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15919417320411416,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.0161916055879427,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.7113764411760553,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.17386407545470545,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.016245137593249206,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6623297829630164,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.16103922479257565,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015746792855416067,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.13472329087687768,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.013414540967388274,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0013309504025351827,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7582077202264188,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16775184406161578,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.016460104156066864,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.451082805689449,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.17756694428979306,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.02074798281361855,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2608030008964091,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02610687489877788,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002534004813697144,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.24097148908182553,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.024210588015075196,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0023775923820677555,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12933274880247358,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013317310438721485,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013013597730018396,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.5592040439706936,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16199118163976292,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.017239828514455477,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.673503211112705,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15915743176649694,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.016467819951024504,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.5871622289906822,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.16405049740509867,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015612313844732783,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.13128011496047803,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.013157998113644088,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0012947335469302588,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.678787284986646,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1605216344848086,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015521849004824794,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6398857835470975,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.22559856518933566,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01630216731874683,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12613138162067677,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013199760584781575,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013004628032208731,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6179537315041397,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15737065292319607,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015960966528600223,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6255697724380713,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15745200925063635,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015802112260289054,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.5983066186283932,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.16564416755118977,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015945760668526807,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.12982926290043975,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.01334299409819796,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0012836561582730594,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.535882984661914,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.15208000080771392,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.016347073017950176,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.9590254717752793,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1988543784556255,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.018114304994063236,
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
        "date": 1781517240768,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2795335190486342,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.028069532404107866,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0027733163572963397,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.14399716833626938,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.014831515363449987,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0014518249308396853,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6668567671416123,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16412968406044764,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015381102981784033,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6718529534247595,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.14511815091108538,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015240928856692575,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.781576221872297,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.1776625023060565,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01488100336825115,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.14687887906681152,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014653474732395122,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014556639322933656,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6563248141124731,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16466024762957393,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015642563268483744,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.3173277174329323,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.21772650689043335,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.02019607890946639,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.29941805412771844,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.030280947148751014,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0029788694401968233,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2792294090876718,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02813056317140421,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0027813007111146667,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.14298039898607803,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.014306081005433579,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.001414592432345926,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6456749230406953,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16496749179956616,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015698753574998715,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6456458272216732,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16219906293416889,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015564067043946962,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.75658180296411,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.1583190254508104,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.014936262984352893,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.14455389404252098,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.013807318706159425,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014301001177412331,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.649832501151638,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16597864651070665,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015561531134414506,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7348073911375206,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.18565766312530177,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015755151109519982,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.1451605386654497,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013337981271071112,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0014457526822853276,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6209012068672277,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15152272552861099,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01567601429515301,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6224566680156627,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.1509998550484198,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01557758729326141,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.7869896944706003,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.16789545010692458,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015456664985948906,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.14268650999364008,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014297777856386968,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.001448717321553839,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.586295416625723,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1575123229196636,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015671844531620373,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.0982411422067893,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.18915861717281374,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01678358098317313,
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
        "date": 1782123326886,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2787744466400781,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.027716999443332606,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002638211606969267,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.14322791988564507,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.014659286194353499,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0014689142114389015,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.64961007272317,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16275484715449945,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015183824752421318,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6549625610786443,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16306586563183634,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01528365612431111,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.7778976659386359,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.17612405033313538,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015511193742601532,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.14234144279014,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014745689407885041,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014643469690793147,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.637414418820874,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16488693191404266,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015272761440342526,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.236856447403277,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.21979924413937116,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01909780972403894,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.29815090032703473,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02991785693894223,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0029753544082641087,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2752047266464559,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.027783973007032635,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002763794365441499,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.13929742051956043,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013444422508546721,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0014481227469115873,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6852022010174637,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.1674158839911138,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015278955299799443,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6798688813380451,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.1639089138395297,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01558660912018454,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.581179646423701,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.1697817403422088,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015174464865115986,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.14308043644024923,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014177714900862361,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014035334713540758,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.659151385716695,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16669595479324467,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015533828052070695,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.698345745849838,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.19102744474723918,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01598057378201563,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.14738941117524657,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.014303532689210275,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.001406642212633953,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6226793963093962,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.1508192941979305,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015618194122071005,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6220253394215327,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15100510422351157,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01552217866566801,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.7048707533468934,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.17072778374606418,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01547627998587799,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.14673486641281958,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014223103748120917,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.001465969560877579,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.5817556171870388,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1529814731169879,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01538718804929443,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.003980660278886,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.18824621464734073,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01680540174587443,
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
        "date": 1782724883492,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.27996080499732745,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.027871092808936643,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002751467890828028,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.14220644016785125,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.014612991921903348,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0014849280430685092,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.644288507418408,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.1683391770831748,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01552770209865596,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6586310501709847,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16482146900801606,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015567310216090289,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.7799542015541288,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.17718263469500256,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015621782461372455,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.1478569571554585,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014147057419936057,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014590822650009474,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6151635698510738,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16382439536554605,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015328769817422112,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.2831905149681466,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.21728700452904084,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.02047788724628632,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.29964280588335657,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.03010896500215942,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002988628307798346,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2788063930683655,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.027910715057464747,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0027702801132108734,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.14280549592967384,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.014244753563006926,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.001450150996880596,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6888757611064453,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16500550572015027,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01573260229861936,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.7237313911398413,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.167214554636894,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015200700081959784,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6802128890867525,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.17125964322114762,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.014967297586963446,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.14545868368363202,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014271273287412806,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014164728715943512,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6768809840366115,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1662265025713766,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015367081143153688,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7602771586080208,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.19128245496294805,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015887757624950795,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.1363747481129517,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.014442917669720192,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0014385341371417214,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6148953834546425,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15115378210572503,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015520711764792066,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6183944018207579,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15024791170130428,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015594072065272324,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.7108626393033497,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.17065506604754996,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015530212305751887,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.1428220828947283,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.013850870839546975,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014171723462584425,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.604604834098178,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.15734030699976073,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015273506702371088,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.0982909916047037,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.18465690563951964,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.016655175582627198,
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
        "date": 1783330221447,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2788306009959025,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.026999434581082244,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0027403995621105926,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.14134614222501357,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.014704932271025395,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0014705194940221886,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6585077053823558,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.1650380975322055,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015469785335966097,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6409149728215517,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.1657978804929811,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015543515895456519,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.7773698333757444,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.17730740453907504,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015562499711436325,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.14670515712309956,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014636214623375002,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014390521963207102,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6626871278616637,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16410756261491993,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01530651383773668,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.3227016654840456,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.21696844206650848,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.019433039826097724,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.29924304394003975,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.030295778193308965,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0029904217153080763,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2780922640412252,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02759715547286119,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002783290620938914,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.13867609259999586,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.014095775438774794,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.001434649889590588,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.687495722421999,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16371695959852509,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01576167257407136,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.686606838670274,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16707391952813283,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015597967737383723,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6199352229622381,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.1710826133289558,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015049498246569653,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.14159524481940794,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.014317242147934488,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0014435403583486013,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.656141364867746,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16593410146132592,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01574860011129069,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7389154092883872,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1951343724353998,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01566054982057733,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.14179110953557233,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.014472072849618428,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.001447567468177089,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6195281229643839,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15077200566071253,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015666885472300128,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6240547041164266,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15134618858515747,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015330114578592199,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.7480829666135356,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.17425875188988083,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015419499996856083,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.14939647443848814,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.01415983558665145,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0013807287153631501,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6131589525224235,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1527088865888721,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015483918581690303,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.0979305027137203,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.18875796973838818,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.016895572525160583,
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
        "date": 1783932498677,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2398048531924101,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.02380076502764341,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002369170068529548,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12803538592841301,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.01290535971743668,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013127623506086713,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6364412468959295,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.17326855473112152,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01627694977363848,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6247590519205706,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16784029547312626,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015942055318946977,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6435920640222261,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.15863209094928016,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01656812496401596,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.1336391964914924,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.012735919239205622,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0013377417409231433,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7281401969766512,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.17180738342962498,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.016151771072898224,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.3212784097763057,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.2089370185720711,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.021394453759799703,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.24573294256988548,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.024699783142829133,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002486750007640317,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2344213531062218,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.023236624224138348,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.00236671851412124,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12586662579364138,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.01288550562078245,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013199833443115886,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6740278746652812,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15867941494499244,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.016601582515772297,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.683447872232509,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.1580952779458375,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.0164515800415082,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.5944853363070766,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.1484655250772502,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015371621332916741,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.1274006697839518,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.012925128407124556,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0013349955069609865,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6605717050420654,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16111495574568063,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015860539001977515,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6565066630521095,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.22044507474955632,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01617774676913454,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.12594369900546282,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.013009654488883837,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0013018657585641877,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.564654305667957,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15670503826371754,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015808456174570967,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.492505534182256,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15480345499204712,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01586680319271612,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.639876309234498,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.16280257462387474,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015997786042065025,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.13319060434452507,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.01323133435042898,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0013436929894694537,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6064315373990994,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1537287305730049,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01661512778112402,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.044386963095889,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1974120546205574,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.016938607408016695,
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
        "date": 1784537015673,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.33249248115440605,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.03443721954677374,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0033531181530506208,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.105812458025956,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.010902945799668893,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0010302388955183415,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.7263531888299066,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.17371516892426248,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01599965504632374,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6894514632238966,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.1742541924717979,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01628964156069808,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6610305017272768,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.16279485490221343,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.016402655791839853,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.10913483408841365,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.010898400710930449,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0010065807356581537,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7101721242181243,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.173668417833004,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.017052803911085636,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.325201973307576,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1971200513673553,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.02024748598652146,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.4905599734881306,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.049866735820545946,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.005224288337799811,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.33566122550268623,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.03238773114850724,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0032005168875402674,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.10468107707486865,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.010497802982374769,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.001042788359997108,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.5774296360534479,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.1600231743180813,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015567238968883578,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6870386730311775,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15875876298962638,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.016186280228702096,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.7076033315259422,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.15347407725820708,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.014927554653813452,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.10704631685100738,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.010251067605596468,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0010193290715457405,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.5158455983115116,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16025928283599394,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01583517235607548,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7816559895006783,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.21569515248127372,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01792363715205673,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.10684229951748743,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.010534470100809294,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0010490288024938127,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6100355690204704,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.1567391716857804,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01629756544995965,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.5946570839863716,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15281244329663896,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01633954478585652,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6248632288461486,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.16058761397761265,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01585883475992044,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.10857117638241479,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.010890743745997723,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.001040327609156012,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.5986456873411399,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.14936093756146054,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01626595649387231,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.084576448093059,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.18876084921334996,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.017040853988096683,
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
          "id": "532d38577bb93e6f13bf1ec9ecba84aad02f463f",
          "message": "Detect CI via env vars so Windows/macOS GHA get maxParallelForks=1\n\nPath-only detectCi matched Linux GHA (/home/runner/work/) but not\nWindows (D:\\a\\...) or macOS (/Users/runner/work/), so those runners\nran test workers with local maxParallelForks and saw intermittent\nMessageIOException connection resets during worker teardown.",
          "timestamp": "2026-07-26T11:50:11Z",
          "url": "https://github.com/apache/groovy/commit/532d38577bb93e6f13bf1ec9ecba84aad02f463f"
        },
        "date": 1785142558272,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2978342099058457,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.030062622084984485,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0029937062801856104,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.10856885887979646,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.011196994137141774,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0010940670352616879,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6403877844239596,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15975888827076787,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01551029964421873,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6405947235940368,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.1647980056124953,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015615479972467747,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.778926931790488,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.17602490804374032,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015397860153999141,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.11500153376020024,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.011354732837350104,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.001051638374165007,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.638801329713944,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16612484600285388,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015635641732376505,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.1669529409315476,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.21321913544624332,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.019162434314335737,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.4663669253142779,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.048767765969840546,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.004814218924906572,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.28683320443336335,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.028963321346351684,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0028946462504607596,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.11195752972224575,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.010682039195850252,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0010937682333393367,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.7250131834708136,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16153723804370926,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01580513424453515,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6857949987610834,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.1617357447333911,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01601279423818337,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6206624928738353,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.1562380138157709,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015004654004542603,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.11700949979211264,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.011005443603488336,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.001179000772962281,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6969453402046895,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16587777666818063,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01558227391037921,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7788981922172735,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.18481563373058957,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015372574246470285,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.11013138306312946,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.011086518511399118,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0011530677924193409,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6141835012719103,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15076649484467342,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015765545954617415,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6161226368281363,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15113894869513997,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015697384095014942,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.7032705182995032,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.17079536920906255,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015506138234612471,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.1143982247408816,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.011526109382932664,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0011515163518122614,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.5861900382999399,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16182759228907878,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015601439544112273,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.110470015440622,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1946156326251583,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.016115381770512933,
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
          "id": "8c02dbd62392a10a246fcb280150efd2b0f54e7d",
          "message": "GROOVY-12227: GeneratedDispatcher: avoid runtime class definition so\npacked closures work in native images\n\nTwo layered changes to the GROOVY-12151 packed-closure machinery (GEP-27):\n\n1. ClosureWriter now emits into each packed hosting class a private\nstatic $packedDispatchersFactory$ whose body adapts the class's three\nprivate static dispatch tables to their functional interfaces through\nordinary bytecode-level LambdaMetafactory invokedynamic sites and\nreturns them as one Bundle. The dispatcher accessor's bootstrap becomes\nthe new four-argument IndyInterface.packedDispatchers, which receives\nthat factory as a CONSTANT_MethodHandle bootstrap argument.\n\n2. GeneratedDispatcher.bootstrap (four-argument form) just invokes the\nfactory into a ConstantCallSite: linking needs neither a runtime\nLookup.findStatic nor a programmatic LambdaMetafactory call — an\nundeclared reflective lookup and a run-time class definition, the two\noperations ahead-of-time runtimes restrict, so the linkage suits any\nsuch environment. Under GraalVM native image, the verified case, the\nfactory's sites are pre-processed at image build time and its method\nreferences reach the class's own private tables without reflection\nmetadata.\n\nOn a regular JVM the linkage is equivalent: the VM spins the same three\nhidden classes when it links the factory's sites, with the cost moving\nfrom bootstrap-time programmatic LMF to first-invocation indy linkage\n(per packed class: one extra synthetic method, three indy sites, one\nbootstrap argument; the accessor shape is unchanged). The Bundle\nconstructor is publicized because the compiler-emitted factory in the\nhosting class's own package constructs it directly.\n\nThe previous three-argument bootstrap is retained verbatim for class\nfiles emitted by earlier 6.0 pre-releases, which keep linking through\nfindStatic plus programmatic LambdaMetafactory. Conversely, class files\nin the new format need this runtime to link; both directions only\nconcern unreleased 6.0 snapshots.\n\nVerified on GraalVM 25.2.4 (native-image 25.0.4): the packed repro that\npreviously failed with 'Classes cannot be defined at runtime ...\nM$$Lambda...' now runs correctly (single emitted class, 30MB image,\n~12ms total run time), and the tracing agent records zero\npackedDispatch entries for the new bytecode.\n\nPackedDispatcherFactoryTest covers every dispatch shape and the\npropagation of undeclared checked exceptions through both class-file\nformats -- the legacy format recreated by downgrading a freshly\ncompiled accessor's bootstrap reference from the four-argument to the\nthree-argument form, the only difference between the two formats. The\ntest serializes its system-property mutation against other\nproperty-touching tests with @ResourceLock(SYSTEM_PROPERTIES).",
          "timestamp": "2026-08-03T21:12:19Z",
          "url": "https://github.com/apache/groovy/commit/8c02dbd62392a10a246fcb280150efd2b0f54e7d"
        },
        "date": 1786351183675,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.491472045374789,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.04737689579999135,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.004722034963939245,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectC_functionLambda ( {\"size\":\"100\"} )",
            "value": 1.581918301243548,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectC_functionLambda ( {\"size\":\"1000\"} )",
            "value": 0.21474421908790356,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectC_functionLambda ( {\"size\":\"10000\"} )",
            "value": 0.02353146151077919,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectD_functionMethodRef ( {\"size\":\"100\"} )",
            "value": 1.5329969917774862,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectD_functionMethodRef ( {\"size\":\"1000\"} )",
            "value": 0.2158403714719399,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectD_functionMethodRef ( {\"size\":\"10000\"} )",
            "value": 0.02380650895278746,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectE_baseline ( {\"size\":\"100\"} )",
            "value": 3.609019875843927,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.328640639390435,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.029916409796582742,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.47772023346958575,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.046956562401371534,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.004632563804419285,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectC_functionLambda ( {\"size\":\"100\"} )",
            "value": 2.0445554390529685,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectC_functionLambda ( {\"size\":\"1000\"} )",
            "value": 0.18515005118104252,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectC_functionLambda ( {\"size\":\"10000\"} )",
            "value": 0.017813648943992948,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectD_functionMethodRef ( {\"size\":\"100\"} )",
            "value": 2.0464190852938557,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectD_functionMethodRef ( {\"size\":\"1000\"} )",
            "value": 0.1859157820146359,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectD_functionMethodRef ( {\"size\":\"10000\"} )",
            "value": 0.017832753082674763,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectE_baseline ( {\"size\":\"100\"} )",
            "value": 2.209568498926166,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.2146238789165716,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.019004887900443614,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2997676181787616,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.027325723251741263,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.003014568915559562,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.10702683745487689,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.011412842380314347,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0011316163897342182,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6476817578488336,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.1646114649717499,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015487398354439217,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6381453707905862,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16507508301898613,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015351265788246382,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.7826052820271407,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.17741557091754923,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01568881924299203,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.1137276351525384,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.011572461555490608,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0011142760053559054,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6554525073330513,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16435492512484154,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015636524370569515,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.1620974249777847,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.21479106620984995,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01926553072919807,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.4595801500669138,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.04831121616683051,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.004828785912952189,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.2895283504502865,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.029563182615159955,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.002962611371129788,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.11344034354738089,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.011224821387289516,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0011276803181643053,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6525288906495157,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.1647066042257328,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015411691448891488,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6883047358254202,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.1624158382418459,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015803295890095714,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6835572591854349,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.16515817831607177,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015180626589417457,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.11325749954631528,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.011363858287806424,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0011260888130514525,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6737560859127505,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16616914807228927,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015135342432566404,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7613938796125126,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.18571420128881572,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015631330897394866,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.11623562078102981,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.011477914601775871,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0010207756948655128,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6203703869068427,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15150387488768718,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01575789554685072,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6271208625754603,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15019873274904194,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015272504528287592,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.7894447088378105,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.16831533865083,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.015035677222051694,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.11449291583507153,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.011602287776967864,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0011278438487725881,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.5856539677416568,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16222508456457704,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015546062438362379,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.114163807677934,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.19299971874197752,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015769361832812905,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.45904130577616353,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.04647232490285056,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.004604271015181952,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectC_biFunctionLambda ( {\"size\":\"100\"} )",
            "value": 3.7736740055792097,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectC_biFunctionLambda ( {\"size\":\"1000\"} )",
            "value": 0.3448425324435252,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectC_biFunctionLambda ( {\"size\":\"10000\"} )",
            "value": 0.030979305641893627,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectD_biFunctionMethodRef ( {\"size\":\"100\"} )",
            "value": 3.6180332605758716,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectD_biFunctionMethodRef ( {\"size\":\"1000\"} )",
            "value": 0.3382003724100256,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectD_biFunctionMethodRef ( {\"size\":\"10000\"} )",
            "value": 0.029716572219147558,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectE_baseline ( {\"size\":\"100\"} )",
            "value": 6.719659150836371,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.6242210332393927,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.052412735281108055,
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
          "id": "3a40cf9ad60c61176de41884037cec0fef68b750",
          "message": "Bump org.sonarqube from 7.3.1.8318 to 7.4.0.8496\n\nBumps org.sonarqube from 7.3.1.8318 to 7.4.0.8496.\n\n---\nupdated-dependencies:\n- dependency-name: org.sonarqube\n  dependency-version: 7.4.0.8496\n  dependency-type: direct:production\n  update-type: version-update:semver-minor\n...\n\nSigned-off-by: dependabot[bot] <support@github.com>",
          "timestamp": "2026-08-16T01:22:51Z",
          "url": "https://github.com/apache/groovy/commit/3a40cf9ad60c61176de41884037cec0fef68b750"
        },
        "date": 1786955258761,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.6482530232200857,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.056007958923161405,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.005570366304142743,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectC_functionLambda ( {\"size\":\"100\"} )",
            "value": 2.0663004970100336,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectC_functionLambda ( {\"size\":\"1000\"} )",
            "value": 0.19657946795040387,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectC_functionLambda ( {\"size\":\"10000\"} )",
            "value": 0.021983454200538703,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectD_functionMethodRef ( {\"size\":\"100\"} )",
            "value": 2.2363509979878935,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectD_functionMethodRef ( {\"size\":\"1000\"} )",
            "value": 0.19398303601637235,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectD_functionMethodRef ( {\"size\":\"10000\"} )",
            "value": 0.021653280415108438,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectE_baseline ( {\"size\":\"100\"} )",
            "value": 3.9502823139396255,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.3473302885370265,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.035712418738873444,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.5667999118339959,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.05318986536463699,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.005267410590599908,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectC_functionLambda ( {\"size\":\"100\"} )",
            "value": 2.078268913573768,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectC_functionLambda ( {\"size\":\"1000\"} )",
            "value": 0.17029322921137666,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectC_functionLambda ( {\"size\":\"10000\"} )",
            "value": 0.017277075984752345,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectD_functionMethodRef ( {\"size\":\"100\"} )",
            "value": 2.15097307069254,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectD_functionMethodRef ( {\"size\":\"1000\"} )",
            "value": 0.1742170011594894,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectD_functionMethodRef ( {\"size\":\"10000\"} )",
            "value": 0.01755224902794135,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectE_baseline ( {\"size\":\"100\"} )",
            "value": 2.4188587133674018,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.2350781310234617,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.02291281846306523,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.4118275198528993,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.04200535018274772,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.00415196301715969,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.1276261781886138,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.012941267833120568,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.001209227243495525,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 2.4250039906092504,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.25455788245715516,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.02501831517975946,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 2.4186720758559286,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.23974800241679572,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.0250978798946336,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 2.4704246363831093,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.24606068412584808,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.023925046727556583,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.12203274608792258,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.011910924894181904,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0011523380908920115,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 2.2181212677936495,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.2214246313212608,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.02323874903043465,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.5683982780690076,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.2604147513465117,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.02500426282857889,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.5630710832649631,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.05761138875876307,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0057437065777544965,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.3916442996758807,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.0383802662277022,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.003847652903332536,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.11441709954471424,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.011111410791965445,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0011368345960517854,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 2.2905294545578396,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.22493272704834646,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.022244043569567502,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 2.3057129863344463,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.2268065342103774,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.022884847472064474,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 2.207333790952893,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.21983078695120986,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.021950138271626295,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.12040921383251817,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.011551320136254362,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0011347134374659709,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 2.2612824752457006,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.21677732917549966,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.021648749552184203,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.3853308794683796,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.2653292678993377,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.023693273739174257,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.11882466586123279,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.011361304455681695,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0011797965264019438,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 2.1086290651005397,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.22138303825489486,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.02180193447772768,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 2.137237816635133,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.21870472756491335,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.0216225290738872,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 2.2929960485705174,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.23071865884952664,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.024929745663079723,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.128838258657039,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.011386749641021496,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0011213344046062594,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 2.1425801004109353,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.2114426459163185,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.02235874223233695,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.7156788397496165,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.27542890726919433,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.024851591243353746,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.5862868395225677,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.05969370091719124,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.006108370726022936,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectC_biFunctionLambda ( {\"size\":\"100\"} )",
            "value": 4.184722138891897,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectC_biFunctionLambda ( {\"size\":\"1000\"} )",
            "value": 0.35052493606642104,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectC_biFunctionLambda ( {\"size\":\"10000\"} )",
            "value": 0.034652701817679446,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectD_biFunctionMethodRef ( {\"size\":\"100\"} )",
            "value": 3.846432030781403,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectD_biFunctionMethodRef ( {\"size\":\"1000\"} )",
            "value": 0.34085814204929143,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectD_biFunctionMethodRef ( {\"size\":\"10000\"} )",
            "value": 0.033731599089795145,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectE_baseline ( {\"size\":\"100\"} )",
            "value": 9.736767572140685,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.8725665243215406,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.08181658450879517,
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
          "id": "e797296544630578ef410594575e6eadac415ab1",
          "message": "GROOVY-12292: Honour method-level type-checking annotations over class-level SKIP\n\nA method-level @TypeChecked or @CompileStatic annotation with the default\n(non-SKIP) mode was silently ignored when the declaring class carried a\nSKIP-mode annotation: isSkipMode recursed to the declaring class without\nfirst considering that the method's own annotation had already answered\nthe question. Nested classes take a different path and already honour\nthe more specific annotation (GROOVY-10238), as does the opt-out\ndirection, making methods the lone anomaly.\n\nStop the walk up to the declaring class when the node itself carries one\nof the visitor's type-checking annotations with a non-SKIP mode: the\nmost specific annotation wins, and class-level SKIP remains the default\nfor members without their own annotation. STATIC_COMPILE_NODE metadata\nderives from the same method, so an opted-in method under a\n@CompileDynamic class is now statically compiled, not just checked.\n\nDeliberately unchanged, ratified by tests and documentation: SKIP-mode\nmethods inside checked classes are skipped as before, and method-level\n@CompileStatic(SKIP) / @CompileDynamic disables static compilation only,\nnot an enclosing class's @TypeChecked checking.\n\nBehaviour-changing: previously-inert method annotations now take effect,\nso a COMPATIBILITY.md entry is included; targets Groovy 6 only.",
          "timestamp": "2026-08-24T01:26:50Z",
          "url": "https://github.com/apache/groovy/commit/e797296544630578ef410594575e6eadac415ab1"
        },
        "date": 1787559951495,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.6280773702131425,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.06881238018276463,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.006944839296341149,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectC_functionLambda ( {\"size\":\"100\"} )",
            "value": 1.956734842956434,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectC_functionLambda ( {\"size\":\"1000\"} )",
            "value": 0.19939927356816506,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectC_functionLambda ( {\"size\":\"10000\"} )",
            "value": 0.023629741759528942,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectD_functionMethodRef ( {\"size\":\"100\"} )",
            "value": 1.5727944237774822,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectD_functionMethodRef ( {\"size\":\"1000\"} )",
            "value": 0.2014817120906396,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectD_functionMethodRef ( {\"size\":\"10000\"} )",
            "value": 0.023677874899626125,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectE_baseline ( {\"size\":\"100\"} )",
            "value": 3.5862644288444736,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.3290340033459914,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.arrayCollectE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.029782649523525807,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.6080055707627798,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.05935402346078831,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.006009537269991854,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectC_functionLambda ( {\"size\":\"100\"} )",
            "value": 1.9125255961324317,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectC_functionLambda ( {\"size\":\"1000\"} )",
            "value": 0.17751628928594618,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectC_functionLambda ( {\"size\":\"10000\"} )",
            "value": 0.018202620151698955,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectD_functionMethodRef ( {\"size\":\"100\"} )",
            "value": 2.0207439978251256,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectD_functionMethodRef ( {\"size\":\"1000\"} )",
            "value": 0.17953779706474726,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectD_functionMethodRef ( {\"size\":\"10000\"} )",
            "value": 0.01780532270510058,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectE_baseline ( {\"size\":\"100\"} )",
            "value": 2.3250157208546147,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.22867842822110251,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.collectE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01929771878517094,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.5328053313061183,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.062429338098925,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.006142855646362182,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.11634391471428947,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.011830460856962323,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0010270129907123567,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6540913904325951,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16368951963411177,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.01546201805238761,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6528340545449065,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.1661861205707924,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.01571188711831052,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6730889170691499,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.1594271645566256,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.017204793447119143,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.11850501567059042,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.011060304469603422,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0011235734555822112,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.6561467122182734,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.16662494795420152,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.01589519756388421,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.1741932651711946,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.21659938679060833,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.countH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.02116385099175103,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.6662131714622709,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.05689930936958034,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.0060847650372659115,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.5300667004707955,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.05409761381927185,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.005379799862135085,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.11882767763478883,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.011744487445672072,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0010656301773039653,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.7134895123886629,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.16659112235809226,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.016084744939943413,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.7128570530422795,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.16316211818078666,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.016182312311252965,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"100\"} )",
            "value": 1.6877910703178252,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.1735444073054907,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.01604679227212725,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.11974132096194652,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.011684118220763191,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0011684309943239698,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7014936435544228,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.1587831248139722,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015434955037690176,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 1.7194184462997633,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.19095995286022524,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findAllH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.016383231621260912,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"100\"} )",
            "value": 0.11524082011434375,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"1000\"} )",
            "value": 0.011733743724728029,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findB_rcurryClosure ( {\"size\":\"10000\"} )",
            "value": 0.0011810535767739542,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"100\"} )",
            "value": 1.6254414099857293,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"1000\"} )",
            "value": 0.15024636781833925,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findC_biPredicateParam ( {\"size\":\"10000\"} )",
            "value": 0.015631617818743417,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"100\"} )",
            "value": 1.6228782781878792,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"1000\"} )",
            "value": 0.15118641617535108,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findD_methodRefParam ( {\"size\":\"10000\"} )",
            "value": 0.015565061469736036,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"100\"} )",
            "value": 1.620622289087051,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.16423640167096912,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.016303897880715373,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"100\"} )",
            "value": 0.11831229952322653,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"1000\"} )",
            "value": 0.011489640787290006,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findF_sharedRcurry ( {\"size\":\"10000\"} )",
            "value": 0.0010707491895849154,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"100\"} )",
            "value": 1.584026773245191,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.15320880865615386,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findG_lambdasCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.015393827835707663,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"100\"} )",
            "value": 2.1527918759966242,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"1000\"} )",
            "value": 0.19329158176087582,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.findH_closuresCurryWith ( {\"size\":\"10000\"} )",
            "value": 0.016340523072851505,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectA_captureClosure ( {\"size\":\"100\"} )",
            "value": 0.6942624860592395,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectA_captureClosure ( {\"size\":\"1000\"} )",
            "value": 0.06794569483722532,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectA_captureClosure ( {\"size\":\"10000\"} )",
            "value": 0.006774218297732511,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectC_biFunctionLambda ( {\"size\":\"100\"} )",
            "value": 3.7581980352855893,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectC_biFunctionLambda ( {\"size\":\"1000\"} )",
            "value": 0.34565962056122046,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectC_biFunctionLambda ( {\"size\":\"10000\"} )",
            "value": 0.03023729949748774,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectD_biFunctionMethodRef ( {\"size\":\"100\"} )",
            "value": 3.7496964792451295,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectD_biFunctionMethodRef ( {\"size\":\"1000\"} )",
            "value": 0.3466783365385179,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectD_biFunctionMethodRef ( {\"size\":\"10000\"} )",
            "value": 0.030733742011144725,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectE_baseline ( {\"size\":\"100\"} )",
            "value": 7.970782728303563,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectE_baseline ( {\"size\":\"1000\"} )",
            "value": 0.7655112950536523,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.adhoc.FatFreeLambdaBench.injectE_baseline ( {\"size\":\"10000\"} )",
            "value": 0.05784997368650863,
            "unit": "ops/us",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          }
        ]
      }
    ]
  }
}