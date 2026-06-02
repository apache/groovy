window.BENCHMARK_DATA = {
  "lastUpdate": 1780368623565,
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
      }
    ]
  }
}