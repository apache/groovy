window.BENCHMARK_DATA = {
  "lastUpdate": 1779437313696,
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
          "id": "160c9d57d19dd70b97fb7d3f286e285cac100496",
          "message": "tweak performance test which has stack overflow in some environments",
          "timestamp": "2026-05-10T23:27:04Z",
          "url": "https://github.com/apache/groovy/commit/160c9d57d19dd70b97fb7d3f286e285cac100496"
        },
        "date": 1778456798148,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107138.91450232032,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 60505.450067129896,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2667456.62375528,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 10988.145146695597,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18132.310957765985,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.11580318152525,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.053055898839023,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 118.15546952425416,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 529.5017395713107,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2528.454209049771,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2528.626580721183,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.158760145839106,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.519391925912816,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.5107461525067434,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 631.6445134882595,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3389.3569386378394,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3397.2381949942983,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3268.954361600168,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15498.751041228938,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36170.97178533593,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 354.0398082403172,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15436.360536891068,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2045.7194986723414,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 314.17364339835933,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15383.369214382585,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1507.357266862487,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.2746936891935826,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1928181595006317,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.025472453621834,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.050491486873130285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.22039681810750528,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 0.9711894322123529,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"8\"} )",
            "value": 5.0563844720363225,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20873748544368437,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.0062797234081886,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 21.626182078854704,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22453.881020499997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004555997842286601,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012307227761621103,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.059522502159888545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 102.04057699,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.007706148461867598,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.01556903161757911,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.059361610291538316,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 101.62960032,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.815128745157713,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.877658953959687,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.673347515042547,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.46918241428571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.08297942333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.3770127327833395,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.392516567811348,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.835002652336417,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.32513900111103,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.12749211848224,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 340829.1341625002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 355582.09288749995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 370542.8609749999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 324558.632375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 333529.1478625,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 345902.8881250002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1826.0368750000011,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1970.709912500001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2915.7543124999993,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.351475,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 52.06943750000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 430.1686125,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
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
          "id": "160c9d57d19dd70b97fb7d3f286e285cac100496",
          "message": "tweak performance test which has stack overflow in some environments",
          "timestamp": "2026-05-10T23:27:04Z",
          "url": "https://github.com/apache/groovy/commit/160c9d57d19dd70b97fb7d3f286e285cac100496"
        },
        "date": 1778459745019,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107467.33569911993,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 62322.00018325202,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2642128.7604438183,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 10769.544963307402,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18226.34956997708,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.47758851166563,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0481783630078945,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 118.66888344802037,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 528.072441371685,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2523.21882946917,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2523.118418131583,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.1534226351128636,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.505866254363501,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.4926903850958633,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 629.9207648461568,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3385.5426226146665,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3393.04662311739,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3271.738041572863,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15543.208095255564,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36137.844760372805,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 343.6969685152586,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15435.132932025424,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1827.632913753338,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 317.68707458806676,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15416.306030276959,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1528.1981856427326,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.27272896281816805,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1985943451946652,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.0789244082250224,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05094654046791269,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.2139590037756843,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.0470877896612103,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20417586147212186,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.0110372278903967,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 21.634388540860215,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 25340.630741499997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004864270910119562,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012294495034743823,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.05991453074646186,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 101.826149835,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.004392616038895336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.020314086261984693,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.08963274475715445,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 101.7999228,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.783447379411765,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.836712153893265,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.676505670512825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.575581322278914,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.21145846666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.3830062399546166,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.476569300952344,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.8551106490874,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.339129949549251,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.201822311146756,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 342599.87692500005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 347990.5758374999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 363294.4590875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 315185.21778750006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 326383.268425,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 337104.81588749995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1770.3968499999996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1912.2871000000007,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2869.2593375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 17.423137499999996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 50.215774999999994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 435.66050000000024,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
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
          "id": "06645cd674262ec1b8e4ab691bac13835ffcecb3",
          "message": "test additional user agent workarounds",
          "timestamp": "2026-05-11T08:00:01Z",
          "url": "https://github.com/apache/groovy/commit/06645cd674262ec1b8e4ab691bac13835ffcecb3"
        },
        "date": 1778486673791,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 102050.2438293186,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 30918.506870076362,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2599226.344327209,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 10671.149473874688,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 16402.5979449446,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 88.02864262911096,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 0.9558027844620774,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 112.33697130884643,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 558.6515951716481,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2249.7011170398778,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2244.347457700979,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.022805357877373,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.387716511652937,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.3898107113278733,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 560.4096429423446,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3003.0024179955044,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 2997.3506205919048,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3206.842447773316,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15697.775633736845,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 35563.378285192084,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 380.17505811447563,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 13631.205360705882,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1975.6039603153326,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 323.3015981953744,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 13599.455850808552,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1803.4765351644037,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.3035901957633536,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.3189201041816052,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.576940044241427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.06161858899646404,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.2525418328546515,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.2605663064886037,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20559897770608143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.0646255216729283,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.594848998402124,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22969.833163099996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004909208844086607,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012654455697852032,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.06437294249932835,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 121.67023154117646,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.004893745199615018,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.017201440393442544,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.06365206690939457,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 121.65517015294117,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 10.767883564261586,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 17.37931507804348,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 28.143038807844288,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 45.48714583686869,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 73.72880057857142,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.423027186717345,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.528427959288498,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.939145575123124,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.51302214254859,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.370157332558136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 328162.30112499994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 337504.3401625,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 354397.12717500003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 305346.22712500015,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 323850.47090000013,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 337413.4234124999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1914.2253749999993,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 2005.931037500001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 3114.6711875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.543337500000007,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 51.436237500000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 434.7805125,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
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
          "id": "03ec3f0ea3ee4858566320d3ae561445c9e62cda",
          "message": "AI readiness: add draft meta skill about creating skills",
          "timestamp": "2026-05-12T06:50:20Z",
          "url": "https://github.com/apache/groovy/commit/03ec3f0ea3ee4858566320d3ae561445c9e62cda"
        },
        "date": 1778572162887,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 102422.72316862029,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 58207.94755341188,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2597559.503688851,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 10786.755501165888,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 16471.660826016298,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.58479499821135,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 0.9464564209674128,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 112.01288849889227,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 559.4014985889056,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2245.629737925929,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2250.7617454586666,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.0172415325079696,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.3913660291420316,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.3900209408621658,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 560.8227681243516,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3005.5440078939037,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 2997.9936729752467,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3170.2081790757743,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15704.697921544339,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 35585.136815998645,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 380.8858744407709,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 13546.39342870998,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2032.803114616264,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 329.91532132594233,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 13603.929095313204,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1876.2066301869222,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.3015469820896811,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.3165700614469207,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.593421731951937,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.06783088785418875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.2888582027434958,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.1553338933777726,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20458732299602983,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.060746488079788,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.455883202084898,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22870.701522299998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.00474990947450914,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012647517340392178,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.06461830592473541,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 121.39654865882353,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.004964883468495877,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.011318217703467203,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.11748957170697238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 121.61826990000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 10.737172385614672,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 17.513337007484512,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 28.09462468583725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 45.20671969111111,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 73.91048726626985,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.4135040365359,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.523468902294,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.942648009046271,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.45702947529276,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.429104081285907,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 325376.8500749999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 337965.74213749997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 357669.7997624999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 309222.8799875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 317382.57050000003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 328292.3265875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1932.8429500000007,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1951.3000000000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 3096.8805375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 19.0263375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 51.875637499999996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 420.0334250000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
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
          "id": "cfa37afd58bb7fdaf5042e7ed47e2f9060688f51",
          "message": "bump dependency metadata",
          "timestamp": "2026-05-13T02:19:00Z",
          "url": "https://github.com/apache/groovy/commit/cfa37afd58bb7fdaf5042e7ed47e2f9060688f51"
        },
        "date": 1778659164358,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 106724.79977820879,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 59667.1180920214,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2641190.4070660793,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 10777.668809948385,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 17921.45022827634,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 86.45332224587735,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.049378018397871,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 119.96423312035795,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 525.8035783301668,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2523.6945279039305,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2523.789901976549,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.1606178314657525,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.5010894790281535,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.5056871684913196,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 629.2867164001266,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3382.0041462942854,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3394.3291193663827,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3255.8637835166455,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15462.664433026273,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 35980.15238206792,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 355.03850301562915,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15340.798577054444,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1867.1157497295512,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 310.36197094946147,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15332.7846772456,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1723.0646989459165,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.276747386839124,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1950541582690202,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 4.990952848940201,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05108320603733265,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.23377634022782337,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.1055353445490161,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20834484455318938,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.0112093130453936,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.287669277630847,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22619.605276000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.0045616342903004575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.01231427095632397,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.05960084762922062,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 121.5381579677451,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.0060182837809971525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.010962430039225975,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.11943626999572561,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 115.2565517127171,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.802354012740315,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.854006371472314,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.73700727177738,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.49036832142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.37360724586208,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.381395417689249,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.409297180655236,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.861225446281521,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.35355688548304,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 22.803209305919758,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 339832.96443749993,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 347836.11013750004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 367587.5492875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 322962.48998749995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 357393.71070000005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 357146.10915000003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1870.598,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 2015.7063124999993,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2915.9159500000005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.231162500000003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 53.844875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 437.04561250000006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
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
          "id": "0a04376328dcec0a3442f3bb3b28723b52c1daf4",
          "message": "try to make JMX tests more resilient",
          "timestamp": "2026-05-13T13:20:24Z",
          "url": "https://github.com/apache/groovy/commit/0a04376328dcec0a3442f3bb3b28723b52c1daf4"
        },
        "date": 1778745135272,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107221.75553353834,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 62176.66046780291,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2641844.481672612,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11265.51899282834,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18220.95201174736,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.07173316716847,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0498199324331168,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 117.51112956913008,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 528.9521651452138,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2523.110615704389,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2525.0515359477836,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.149875771328355,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.5052414708103674,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.502436761042536,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 630.9188559603973,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3385.196913633379,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3399.586883428126,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3273.588330738487,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15520.161416483334,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36064.4497022816,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 353.07923894293566,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15387.20789026059,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1876.247197912629,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 316.1153530774885,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15416.347983274598,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1710.1045482689374,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.27500567627893563,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1949415478822139,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.087873263221673,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.050415271938405795,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.21754267993631501,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.104851132295761,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20475478185624207,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.0066520913992716,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 21.959752730066125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22409.686825300003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004706954861831985,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012258312909223006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.05941261587080637,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 128.60758027381578,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.004135250036661493,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.010958982750171905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.08965332983305295,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 129.71459321875003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.801918348433764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.870814707733782,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.582389384112304,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.47186242491497,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.11625061333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.396135069460391,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.386108358095435,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.84971034385599,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.36877506955465,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 22.81027858881226,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 340163.57594999985,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 346286.7427500001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 370520.1906375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 326560.3193125,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 334543.29051250004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 350176.3244375002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1871.4328,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1986.3488250000014,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2884.803649999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.482300000000002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 53.100637500000005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 448.2991874999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
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
          "id": "9a37d12c790ce574a993d50d5cf2c9d377b2cb4c",
          "message": "minor refactor: use SHA version",
          "timestamp": "2026-05-15T07:54:28Z",
          "url": "https://github.com/apache/groovy/commit/9a37d12c790ce574a993d50d5cf2c9d377b2cb4c"
        },
        "date": 1778831918582,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 106893.02653018109,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 62284.64550128674,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2654381.4706017394,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 10840.84846380035,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18198.70998145834,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.57856236234116,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0472974107925577,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 120.43983718314448,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 529.2992837330388,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2522.684727387613,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2526.997066082846,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.151878589206735,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.5016066931148715,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.49687319578846,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 629.8159848499361,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3386.4099763973304,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3400.9266603010183,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3163.6288006024934,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15531.048229616872,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36085.20414172932,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 344.92700034524046,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15442.431791396088,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1976.795978856755,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 318.343486353319,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15397.977518395555,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1622.162024838001,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.2737620968269015,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1928053167136112,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.169003855897295,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05056183609331756,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.2133442350993608,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.032755226481249,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20768937932635656,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.009200555089411,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.380210395186484,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22253.289789600003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.0047296867806582545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012352654853258836,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.05952934545482917,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 117.99376146986928,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.007794899625038353,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.010936507105857583,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.11960062802983537,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 116.10113408925653,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.801357752079914,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.8724119786819,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.66975361131126,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.43122509387755,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.04508019666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.3945117477506166,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.467749673033293,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 9.15010396933577,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.114123834981802,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 24.350168415787085,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 331209.50017500005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 340444.211475,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 358928.38148750004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 307359.48995000013,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 316544.74473750003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 330508.5498124998,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1763.1530749999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1862.9481499999995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2808.8255000000013,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.273974999999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 54.69646250000002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 443.1726375000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
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
          "id": "60141623ee09a06b5d354ca922120fefd03d75ed",
          "message": "add branch protection",
          "timestamp": "2026-05-15T11:24:08Z",
          "url": "https://github.com/apache/groovy/commit/60141623ee09a06b5d354ca922120fefd03d75ed"
        },
        "date": 1778844683763,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 101975.96354166047,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 57995.77974670309,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2673077.670394617,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 10807.87341756938,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 16535.649188644595,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.79796846997216,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 0.9582052725703164,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 111.88947765133756,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 560.3642388329432,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2249.3303283581504,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2248.3660723794424,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.0233816149391446,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.392546070802916,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.390830335089956,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 562.708720308361,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3003.0020051886795,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3000.317555100769,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3226.0971751218326,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15653.125326738958,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 35658.170006664106,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 370.61478861165415,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 13579.4491640836,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2185.829968515504,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 320.8535044456846,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 13592.91842565561,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1806.0147475064684,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.3031711149937728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.3244314459365627,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.58244419429191,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.06809519016130314,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.2534462864620903,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.3667068780116225,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.2044272320756114,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.0681144282711075,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 23.19979304828193,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22755.1893089,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004987390788019199,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.01261218514269064,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.0651017545717501,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 121.97801394117646,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.004909597975419791,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.017221141261425148,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.09084967410939633,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 121.97710315294118,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 10.782210439208542,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 17.418286368871783,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 28.182871265708137,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 45.59261247954545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 73.885832575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.4113376460477234,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.526237810427885,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 9.072717042311831,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.458930047482017,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.479267183898767,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 307026.3396374999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 315801.3824124999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 336649.9055750001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 295652.8279125,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 306790.1022249999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 317720.8135,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1756.9741124999994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1876.7804,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 3001.525649999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.39778749999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 52.37985,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 427.424175,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
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
          "id": "e8aed4f0379ad063b027a56fab14119c54be03aa",
          "message": "GROOVY-12013: New optional type checking extension: CombinerChecker to verify associative combiners in injectParallel/sumParallel/Stream.reduce",
          "timestamp": "2026-05-16T06:19:01Z",
          "url": "https://github.com/apache/groovy/commit/e8aed4f0379ad063b027a56fab14119c54be03aa"
        },
        "date": 1778917013843,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107223.26310442024,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 62264.78572249389,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2651490.087887716,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11007.653108704344,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18097.349146232726,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.36209961690915,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0489131782832377,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 122.08944941274842,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 529.2310365424169,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2524.171583001197,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2518.3425131470312,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.154996948793483,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.489246485721316,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.502897995639055,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 629.1591176884738,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3384.9710239940687,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3397.0449063396272,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3195.2636410072723,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15537.6100898796,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 35990.28569191119,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 344.19615425689983,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15389.121544919817,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1945.332669389413,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 315.4817933232181,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15406.800565959042,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1449.970827403788,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.27505773138984024,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1945315078789578,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.122722618211783,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05153210516631655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.20946098064347546,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.0966495758301609,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20380229771696526,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.008238100641544,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 21.766973403838577,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22452.6412433,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004854467791707679,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012296455598288372,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.06017282837639935,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 133.25148986416667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.00483486480138895,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.015644805522031825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.08958535349823424,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 134.93595430541666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.804007178003348,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.868013916635423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.737197519235615,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.51037558635204,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.39726117000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.386175625687329,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.473036503600257,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.860082585071398,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.330368233130699,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.13316077916272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 339884.8948375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 350929.287875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 370864.7209749999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 327056.02052500023,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 334196.5468374999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 349808.0667874999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1844.5801750000003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1998.9259000000006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2904.6766124999995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.5531125,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 53.99084999999998,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 444.18829999999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
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
          "id": "7f4fb8afd4c72bb5da84bb74fc9181c607f4dce8",
          "message": "AI readiness: sandboxed reproducers",
          "timestamp": "2026-05-17T05:26:09Z",
          "url": "https://github.com/apache/groovy/commit/7f4fb8afd4c72bb5da84bb74fc9181c607f4dce8"
        },
        "date": 1779004029651,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 102957.20288878311,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 48274.29143020207,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2663030.028065963,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11010.844310177132,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18212.597666729187,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.34597649865141,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0516477522417214,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 122.06760918694434,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 529.8205539413179,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2525.7395126715046,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2527.679324706656,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.1621057194848587,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.5137861802662043,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.5184718511332185,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 630.3492050195002,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3386.5027438185525,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3399.460935288669,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3359.8045175556063,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15541.821974089931,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36104.02820745458,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 361.1957606071004,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15427.009768649907,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1792.607958434899,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 312.616079113698,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15217.581421371797,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1517.2137137394875,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.27490164743009393,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1944365305510105,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.0755214614601325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.050769612801344025,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.2128757318812815,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.0564361974510603,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20899226728076906,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.00505622792868,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.398630953661485,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22302.3653731,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004834331533973511,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012295544050115398,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.059426286042930154,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 135.15932672666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.007559644324784821,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.015600637736894124,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.08964517746691854,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 134.99846282375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.78088272875133,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.816733942238468,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.691620921579688,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.416933369430275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.09272915666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.37813648771725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.463118629713369,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.845242161580567,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.321601310714282,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.17938136325849,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 318548.499225,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 326751.2235375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 348172.401,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 300978.87674999994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 311153.60742499994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 326393.24036249996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1722.6461124999998,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1808.2195374999988,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2804.661762499999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 17.750424999999996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 54.572000000000024,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 452.6806500000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
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
          "id": "167034dab7ed20d43d763520a40d916cac3fe648",
          "message": "minor refactor: improved consistency with other recent method signatures",
          "timestamp": "2026-05-18T05:56:43Z",
          "url": "https://github.com/apache/groovy/commit/167034dab7ed20d43d763520a40d916cac3fe648"
        },
        "date": 1779093124352,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107068.32202577882,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 60054.46106643034,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2656589.001240817,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 10704.824115129233,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18205.944412706034,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 86.98428535306323,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0479707230733217,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 121.83265632177988,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 528.8386379825472,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2520.138356467267,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2524.470441504431,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.158593197517459,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.499035907768525,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.468090194474427,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 631.3210280431915,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3369.097604536518,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3395.1954428587583,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3186.057136640995,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15526.34695744288,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36106.78975408083,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 342.6831842597305,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15408.108225361726,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1950.6740088108886,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 315.5693921718322,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15422.728338946088,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1448.7970350536332,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.2764413697060514,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.2038341121141474,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.0356576758339475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05089735452269619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.21883972338067714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.0982892460359408,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20754980066512774,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.01209557437436,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 49.71777986878394,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22422.9426253,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004703824669112124,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012300847042384315,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.060261047789075504,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 121.20529841814326,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.004235061677440744,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.01564863865267677,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.059325768712906436,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 106.9693807084675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.819988126540252,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.965283759822494,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.693663680662166,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 50.668616442294535,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.27636207333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.3836590223042675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.475892261884436,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.870338499835052,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.339338933198997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.196904746271052,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 327994.67072500003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 340220.6308875001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 363109.0355874998,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 321703.294775,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 337125.34815,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 350653.78902499995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1882.9293375000002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1960.8092375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2886.4978625000003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 20.0057875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 52.134775000000005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 443.4974374999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
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
          "id": "6f24829c9038c9539426fc998420f481cde68ec9",
          "message": "minor refactor: mention spec vs mainline tests in skills file",
          "timestamp": "2026-05-19T05:31:24Z",
          "url": "https://github.com/apache/groovy/commit/6f24829c9038c9539426fc998420f481cde68ec9"
        },
        "date": 1779178884005,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107824.4598465501,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 62350.15831412722,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2662446.3063497706,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11056.295897752505,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 17896.4682106451,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.36420004727611,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0525609504428686,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 118.38173675015707,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 529.0294651554781,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2525.0990501999518,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2527.697001253592,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.164740754882921,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.506949829724924,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.507931431224079,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 631.8543053693821,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3389.1599266088174,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3403.582291618642,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3237.6287311162096,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15544.01266907987,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36174.59753384841,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 360.0842373267409,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15435.808881210212,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1970.9103381226566,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 323.1340306590681,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15172.7754288767,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1697.4726481885805,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.2748358644415682,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1974989324700793,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 4.990188429077171,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.050390621026175954,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.20913767802927086,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.1384037814550259,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20809883544398722,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.006383354637058,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.395188035555556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22400.2419594,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004701325530806969,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012262577229741733,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.05936420719404967,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 102.12357155000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.004375198519922039,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.010870603560040718,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.08940302264976471,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 101.88167658500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.801487641672628,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.825408818328958,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.601480847792924,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.45631731611395,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.15751145666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.3753345518937907,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.47146591671571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.846273934100035,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.084781698235684,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.176849930112272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 325843.84660000005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 337275.0384625001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 356259.49702500005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 308804.0321125,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 317260.70574999985,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 330173.24665,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1785.1715625000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1901.8426875000005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2833.6806874999993,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.70595,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 51.425587499999985,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 445.8665249999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
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
          "id": "6f24829c9038c9539426fc998420f481cde68ec9",
          "message": "minor refactor: mention spec vs mainline tests in skills file",
          "timestamp": "2026-05-19T05:31:24Z",
          "url": "https://github.com/apache/groovy/commit/6f24829c9038c9539426fc998420f481cde68ec9"
        },
        "date": 1779264681503,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 102308.6873291685,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 58345.85615279665,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2662328.4315395844,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 10889.035913375683,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 16532.907472972165,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.80437358976545,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 0.9569522566305281,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 112.41129576566684,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 560.0891732464966,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2249.025560375724,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2248.6847230833487,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.0203089540181898,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.3908223288541803,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.3860431094862165,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 561.8396234824406,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3005.0527251273807,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3000.06640071742,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3153.87173769671,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15677.62405465731,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 35667.9660843367,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 378.72287609317425,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 13622.209841710719,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2190.279128037492,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 320.71399455396806,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 13607.582574056632,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 2067.8028304386976,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.30346810516834716,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.3181579492953408,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.567216197624998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.06780299377187778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.27157493642943337,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.2442336090911794,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.2048614499229569,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.0680949017382866,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.64882009775281,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22915.8613184,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.0048598227054633325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012645759106649229,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.06478391195553629,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 121.53748860588237,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.004873846617384658,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.023119883003725018,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.09089322231076465,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 121.62358491176471,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 10.786215305634991,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 17.44126162475262,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 28.18763443812598,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 45.70629673863637,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 73.87469303822752,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.413889450924226,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.529190074578713,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.93945666138815,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.439237740287771,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.44510596904241,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 309333.59916249994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 319237.3569125,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 341366.4129500001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 302147.44138750003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 309316.30845,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 322094.4835499999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1783.731174999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1878.1637999999998,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2983.0549,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 19.267049999999998,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 54.50908750000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 431.61487500000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
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
          "id": "a2ce6f02fea7c439a9eb3feefe7d34f45e34282b",
          "message": "minor refactor: remove javadoc warning",
          "timestamp": "2026-05-20T15:04:10Z",
          "url": "https://github.com/apache/groovy/commit/a2ce6f02fea7c439a9eb3feefe7d34f45e34282b"
        },
        "date": 1779350894593,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 102062.03831359898,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 58117.922879727346,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2669687.4041675213,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 10636.564932896898,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 16518.184302851227,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.17193929509689,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 0.9555173689407912,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 110.98818254402502,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 549.334298536018,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2242.467410858114,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2240.6446655209947,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.0126489580130014,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.3854939451914476,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.2679154513975264,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 559.6660083510582,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3001.7279046635194,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 2994.8029443539185,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3161.930303610569,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15720.113757904328,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 35551.14621659716,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 381.9565718620938,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 13602.71908263609,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2309.0006962011926,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 312.34906021810804,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 13540.124432458228,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1851.8618144049383,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.3034142572069435,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.3278100779924222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.621965130639421,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05952603017486994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.2685697263572432,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.1557528071073768,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.2058433655684142,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.0750891001751404,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.56643443054194,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22948.193215999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.00509929203679315,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012649667882203775,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.06495730438800743,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 121.36511585294117,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.0048811557816024875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.017219294146924986,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.09155470546961096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 121.35991206470585,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 10.821951084131687,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 17.44092848697151,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 28.136471942116593,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 45.88453993366808,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 73.52766767288358,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.4172992980561063,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.533977396601914,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.966969350591768,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 15.010155460780107,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.45338396357664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 313534.3033875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 321125.4692625,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 340740.4012874999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 297564.3958125001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 306738.0629625,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 321386.6426625001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1784.5518499999998,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1905.5507249999996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 3049.958112500001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.1706125,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 53.39386250000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 443.1129749999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
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
          "id": "e9b9cf1ec53ceb86f6b1e3cd88d630ab3234f3ca",
          "message": "GROOVY-12027: Align CompilerConfiguration with JDK17 minimum (fix some tests and docs)",
          "timestamp": "2026-05-22T05:22:33Z",
          "url": "https://github.com/apache/groovy/commit/e9b9cf1ec53ceb86f6b1e3cd88d630ab3234f3ca"
        },
        "date": 1779437312796,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107134.47049607578,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 62195.13722146023,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2645520.9407771085,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 10728.235101272416,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18212.65998378967,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.00288306134289,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0491318220030483,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 118.5958930144474,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 528.062072972902,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2524.5533855841904,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2526.8928200375944,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.1569584558014983,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.5074084148993974,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.5070377516428493,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 630.8932218998094,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3386.555669012227,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3393.951067845478,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3228.137361281785,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15534.028556383322,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36122.40008641714,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 351.6180776339679,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15399.165390184713,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1745.1027835991376,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 317.82784358170886,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15379.42946282652,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1622.6883443910604,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.27556020413699445,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1917542845052453,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.016925870299036,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05071154734418433,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.21371309537499156,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.1311801513669837,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20981598350410136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 1.9992295453259572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.1100891018315,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 25084.4410665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004655423716458879,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012312358755936411,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.05953794106984284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 107.77845229480393,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.006161244724696738,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.020346819848063278,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.08908907820528005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 124.57105280002062,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.825028788764095,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.923015303507663,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.68144070666991,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.496650214285715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.80023301482758,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.3945757572095245,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.4790432027793745,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.85882001056166,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.36470783114594,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.166118085739107,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 332436.096175,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 342674.320775,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 353405.2447624999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 307824.11983750004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 320281.3551499999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 341240.92302500026,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1807.5053875000008,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1886.9423125,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2873.833962500001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.3068625,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 52.11216249999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 438.7576249999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          }
        ]
      }
    ]
  }
}