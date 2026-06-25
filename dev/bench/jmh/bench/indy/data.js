window.BENCHMARK_DATA = {
  "lastUpdate": 1782374592346,
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
          "id": "650ff80d2ef62d55ae100c4716176d76f2da25c2",
          "message": "Add missing javadoc",
          "timestamp": "2026-05-23T07:13:01Z",
          "url": "https://github.com/apache/groovy/commit/650ff80d2ef62d55ae100c4716176d76f2da25c2"
        },
        "date": 1779522284891,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 102123.90873375737,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 58221.98224409202,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2670868.8515717955,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 10726.121817305582,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 16543.68650109133,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.41418501748777,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 0.9565737379225654,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 112.48357177252653,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 560.4115214249038,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2249.9300716714442,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2250.7348949890256,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.0218143559075648,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.3932127340356417,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.4047387590639544,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 560.2995220949089,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3004.288547170917,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3001.147550242309,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3162.271247396514,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15671.327915691169,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 35648.56854729027,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 376.453696958955,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 13628.06804499943,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2007.491026013445,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 321.7458061689672,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 13601.765928235729,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1818.1905369614772,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.30246554313126905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.3257983749150843,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.561653264770621,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.06858192789981088,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.2706259630115333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.1598571733483944,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.2049140370096035,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.0682894383307655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.56400756067416,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22842.9243729,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004922962178985842,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.01260397470537086,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.06475195204159939,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 121.16089528235293,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.006515580863554885,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.017231857377344468,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.0908407645312678,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 121.14270799411763,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 10.736536861259736,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 17.36176067765367,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 28.1216566206964,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 45.44043905474748,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 73.85840218214287,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.417902614155742,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.5370680350833785,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.933447596847222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.465441598290061,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.37127251976744,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 303410.97935000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 313318.53522499994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 332285.39307499997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 289420.6968874999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 297971.6974875001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 312500.84673750005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1740.8797374999995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1891.2914125,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2981.3029625000013,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 17.5305875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 52.62693750000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 447.5473375,
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
            "name": "Daniel Sun",
            "username": "daniellansun",
            "email": "realbluesun@hotmail.com"
          },
          "id": "121c8605f7994fcefb4f13f21b5a4a46b95ee84a",
          "message": "GROOVY-12030: Graduate PropertyHandler from incubating to stable",
          "timestamp": "2026-05-22T23:45:44Z",
          "url": "https://github.com/apache/groovy/commit/121c8605f7994fcefb4f13f21b5a4a46b95ee84a"
        },
        "date": 1779609372020,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107300.29601667728,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 62099.26805827642,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2673312.3320474895,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 10496.648544382228,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18276.320506524546,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.33707688867688,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0511429752394454,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 118.59433476851704,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 528.5323553095409,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2527.8147703726513,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2522.7051404525587,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.1597055237436473,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.5145688982020085,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.512904400417861,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 630.8167323597937,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3388.16744701448,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3401.0262388703945,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3254.934654097764,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15532.539951548499,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36099.46676381857,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 376.255753153025,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15428.564608549339,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1784.1111402127476,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 311.04824507976684,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15451.729224433366,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1490.8908483369844,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.2744892236240869,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.196703656836751,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 4.960577345749506,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.050471098442177495,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.20932595070771837,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.016404539750242,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20771405268393087,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 1.991810334661532,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.114261361568076,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22242.6378624,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.00474158027141149,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012256613691655063,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.059458329613169214,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 108.25310975484649,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.005384888767690461,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.020315457542910566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.11866878058514005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 118.71209048617645,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.77463558575678,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.812371262729659,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.570960877215192,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.45819827210885,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.00791525999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.385043027374466,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.461867832015419,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.84016264580913,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.351468748230833,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.139241579310344,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 321770.22035,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 333346.2006875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 351835.32555,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 306205.1541125,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 314395.7735,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 330481.1320624999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1742.5431125000002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1868.237775,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2793.5767625000003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 17.307300000000005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 51.98293749999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 437.9685875,
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
          "id": "42ce7e09611dd02d85d6d1e80e3c079a90100460",
          "message": "GROOVY-12038: Graduate groovy-contracts from incubating to stable",
          "timestamp": "2026-05-24T10:28:42Z",
          "url": "https://github.com/apache/groovy/commit/42ce7e09611dd02d85d6d1e80e3c079a90100460"
        },
        "date": 1779697848115,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107799.3027401801,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 61908.92167859682,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2647907.4127066014,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 10993.802269118136,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18261.91510730492,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.48689888601113,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0498021665018027,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 119.22755175717766,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 528.7444581025287,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2523.555799670406,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2518.55592205857,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.1574953216790886,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.5091023783327686,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.5107595944618892,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 630.9589256026418,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3386.957548293272,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3399.634788621558,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3265.8246407579013,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15515.87133529302,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36061.187347873034,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 357.1217622184587,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15458.514789020515,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2003.7897664428317,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 323.64680999295814,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15421.146422290863,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1642.8432818042834,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.2734463485806772,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1934111586610225,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 4.970605943056717,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05134328221015405,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.21711193233158976,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.0533101422137734,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.2077016428269931,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 1.9852308288654126,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.327567445958486,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 25124.6626524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004667802956476843,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012310426550701279,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.06047371894588742,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 125.29721815903508,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.0042054128806583,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.015617743687152904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.11874003162790647,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 130.2962223370588,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.779674007585559,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.843426681214847,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.667510219246992,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.447153434693874,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 66.94419951666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.375486212319455,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.460328602771973,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 9.269897531990122,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.327754865155327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.14555959169674,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 318441.17902500014,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 328980.1886375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 357617.1327499999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 323537.12272499996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 325753.0088875001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 330009.99660000013,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1784.6036124999998,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1873.3305375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2838.1881375000003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 17.364087499999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 52.92836250000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 428.37725,
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
          "id": "42ce7e09611dd02d85d6d1e80e3c079a90100460",
          "message": "GROOVY-12038: Graduate groovy-contracts from incubating to stable",
          "timestamp": "2026-05-24T10:28:42Z",
          "url": "https://github.com/apache/groovy/commit/42ce7e09611dd02d85d6d1e80e3c079a90100460"
        },
        "date": 1779783115049,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107227.4153572139,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 61462.13568115609,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2655300.7534414,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11037.782868799339,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 17939.336034635773,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.59787801348445,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0491682496528962,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 120.11712492100071,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 529.4203905454071,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2525.280525766336,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2528.1294943729035,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.1610469990737737,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.5061197340597774,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.4957333742032404,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 630.8186971881655,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3385.7381115007265,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3397.7738772584344,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3224.482409146399,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15504.34775800842,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36063.56371797934,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 354.35796556878347,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15423.834287598667,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1875.9305450226252,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 315.55555189517264,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15432.211978428997,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1444.032019435096,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.27480412954307176,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1901937153793791,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 4.984390129012709,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.0508831658042244,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.21138111721071748,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.1291254167715803,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.2037979244267584,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.002809146435508,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 21.96857716339202,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 25950.205668200004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004729036293056846,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012284581102788345,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.0593995204552971,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 109.4491141866228,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.0042868870054741885,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.020281471113577455,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.05913619783159668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 115.66154666042397,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.805703758529528,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.825705690132484,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.649828425332686,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.51280364948979,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.17238789000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.380071125290911,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.469679944573896,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.851811372103935,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.306562131200607,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.174703329885055,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 321040.67002499994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 339232.8344,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 355323.8641500001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 307586.3672875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 326903.09111249994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 339908.61104999995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1887.9506500000002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1910.4951500000002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2861.1616875000013,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.281699999999994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 54.03717500000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 448.4468499999998,
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
          "id": "f1a4483fe4bab0f6cd9511fad0abb7016059e557",
          "message": "GROOVY-12040: restore @Builder retention to RUNTIME in 5.0.x",
          "timestamp": "2026-05-26T10:09:43Z",
          "url": "https://github.com/apache/groovy/commit/f1a4483fe4bab0f6cd9511fad0abb7016059e557"
        },
        "date": 1779869936814,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 89326.45859321837,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 71184.33765234168,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2293366.7611420574,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11301.812903964528,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 19251.592258880253,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 69.81766982441395,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 0.9371982848752612,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 78.26915865741692,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 473.1192684303154,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2125.016270787382,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2123.8493068249118,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.0425267657465893,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.0284107080026152,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.02390039510452,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 604.6227383010142,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3640.607324141133,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3642.7254112042565,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3038.3716175353943,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 14097.106575187117,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 23120.647629008003,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 320.56017924565117,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 10698.266725004692,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 6459.9032760331165,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 298.37964352726493,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 10695.067928292056,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 5961.575492639267,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.3196846468778372,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.4120432306343953,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.94190624960836,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.10079560739765481,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.453784601165244,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.9249398600213887,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.21538168397412522,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.1681002843049417,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 24.791257616534022,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 24994.246426700003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.005317565401730899,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.016660771542019445,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.13832114610790322,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 286.6263011767857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.006677398073928006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.015675678533334313,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.13760463090705213,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 281.1041369428571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 10.5465877360557,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 16.994828430035838,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 27.896378859741247,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 44.34181056188405,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 72.32424060357141,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.77755080469544,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 6.122878013066388,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 9.921468472492531,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 16.169881888048668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 26.29936499894736,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 312051.74601249996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 320555.91645,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 337189.27717499994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 296406.33351250004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 305716.28733749996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 320318.1725375001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1511.75095,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1553.432749999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2315.6045375000003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 13.165250000000006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 33.07520000000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 262.498375,
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
          "id": "f220aa2fe7a30fafb7ca02bb454577866174949b",
          "message": "re-enable jmh stats comparison",
          "timestamp": "2026-05-27T10:46:41Z",
          "url": "https://github.com/apache/groovy/commit/f220aa2fe7a30fafb7ca02bb454577866174949b"
        },
        "date": 1779955911416,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 101402.78759050863,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 57991.57107653628,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2670178.0063264933,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 10810.674538441588,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 16425.436388354363,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.77680435499063,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 0.9514531927427703,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 111.40597359354342,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 557.8068698130652,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2249.6144272203505,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2246.461211874289,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.0194209067692315,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.3756499740739967,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.3826839955527097,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 561.8361724265662,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3002.8779617504188,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 2999.471155811746,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3196.115441388708,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15663.334440267334,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 35650.22098191979,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 351.093302644696,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 13601.784385312803,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2641.2854533571085,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 333.18439311476914,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 13589.347806575086,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1680.0870305635017,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.30338847606562785,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.3209607104650187,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.583518332280244,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.06635390411044301,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.27030377827618024,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.2590668455333067,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20519067050309353,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.068183386114268,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.678072677311036,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 23006.2693125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004782453494894204,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.01260291892848942,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.064494177715462,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 121.5773254882353,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.008678786543349772,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.017404580044842296,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.0637651108656509,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 121.49101154705882,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 10.774207019070209,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 17.425574646191905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 28.22306973597418,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 45.727986711363634,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 73.93547454589947,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.419064350941794,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.536101389102245,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.953632890493033,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.475782799900946,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.42245965232558,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 321768.90422500006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 327201.05059999996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 358567.0508750001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 313572.0698749999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 317484.596925,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 330142.84318749997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1845.8050500000002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1995.6835249999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 3030.9205999999995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 17.631587500000002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 54.124575000000014,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 449.7832874999998,
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
          "id": "f220aa2fe7a30fafb7ca02bb454577866174949b",
          "message": "re-enable jmh stats comparison",
          "timestamp": "2026-05-27T10:46:41Z",
          "url": "https://github.com/apache/groovy/commit/f220aa2fe7a30fafb7ca02bb454577866174949b"
        },
        "date": 1780042307351,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 89237.4600004979,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 70444.14612117568,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2292384.2748515126,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11555.505423035172,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18969.096252773827,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 70.391320315907,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 0.9317441959388993,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 79.04540174553365,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 472.7773719311537,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2126.60500305823,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2125.0591799181616,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.040084330132779,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.0230822441548595,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.0201705146647995,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 582.691089313583,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3641.586405483933,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3640.5899565202462,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3123.5904518343623,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 14435.806255539608,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 23041.625030260122,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 326.1272828296345,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 10699.014916523105,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 6324.5148800944935,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 317.5694213941516,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 10698.221558842932,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 6028.125751836061,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.32495866785264166,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.4041469972370835,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 6.0295844605257845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.10182816751155839,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.4593919809364043,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.9609693309782137,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.21106629317249875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.1875970104944535,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 24.684065999036438,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 25179.372050899998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.005571213556481607,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.016716506972711976,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.13875487505188225,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 274.43410610000006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.006916021187803466,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.015711871262745848,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.1374258053850508,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 272.1219785375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 10.544115526036501,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 16.90908686004817,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 27.499966672213993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 44.544374212463765,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 72.47935473214287,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.820982415980835,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.99316275773785,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 9.770600768155287,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 16.057195998411366,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 26.16105500914086,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 319145.93256249995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 323750.981225,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 346590.7223875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 304818.7811375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 314877.2593250001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 319538.9805,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1477.3352500000003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1596.6784750000002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2301.321262499999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 14.0735,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 32.237075000000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 259.1506500000001,
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
          "id": "f6e2248d1262e2d0cb0f9f835f1aee40f0d162b2",
          "message": "GROOVY-12036/GROOVY-12037: GDK: cache Collectors instances in StreamGroovyMethods and ParallelCollectionExtensions",
          "timestamp": "2026-05-24T00:57:15Z",
          "url": "https://github.com/apache/groovy/commit/f6e2248d1262e2d0cb0f9f835f1aee40f0d162b2"
        },
        "date": 1780127485141,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107114.31192842421,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 48086.71797974083,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2650630.1185185607,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11869.212773572115,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18205.99574370352,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.01371364475683,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.048872186221875,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 118.22100585708549,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 529.1587828639998,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2520.963852438252,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2516.7038858326273,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.156482822348112,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.4933223510498537,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.504156667710191,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 629.9804263570044,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3384.012594124545,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3399.0985148781897,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3192.6354551230643,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15472.0903618668,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36057.5840277837,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 352.6588398073105,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15449.24724421997,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1831.8161593345746,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 318.1633472854317,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15434.390165686922,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1585.0684939617454,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.2753021766968871,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1980103647285731,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.015952714504903,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05119898592119011,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.21330827459412327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.0458916153664943,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20794536071068465,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 1.9864776232929189,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.233242826300366,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 25557.4306652,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004774087496353644,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012269732613361844,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.05974688040338324,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 123.06295760333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.007582157535605738,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.015576467744403919,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.05911905959651491,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 110.986990365,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.818820395762923,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.872988812458994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.667667059866925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.817942703713456,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.13249761,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.6784854474791224,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.472941475229115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.868987104199663,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.313016402168188,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.214898577332264,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 320041.2258125,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 331086.20195,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 354962.46708749986,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 308659.8114875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 319066.59885000007,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 332777.8101,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1791.6744250000006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1939.5429124999991,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2793.0019000000016,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 17.898412499999992,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 53.431887499999995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 444.3382375000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
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
          "id": "7bfdeea2361c3b02488d381b7b5dd867db0787bf",
          "message": "Trivial refactor: use pattern variable instead",
          "timestamp": "2026-05-31T06:53:43Z",
          "url": "https://github.com/apache/groovy/commit/7bfdeea2361c3b02488d381b7b5dd867db0787bf"
        },
        "date": 1780214872842,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 102414.87622315245,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 43137.50092340237,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2650618.156315428,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 12185.992730930255,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 16509.697562991543,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.83641027796786,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 0.9563677160254175,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 112.31509615072316,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 557.7012939345137,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2250.236770112115,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2247.9559905745205,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.0650555298537663,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.378699101235302,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.388392349761299,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 561.8726287263887,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3004.189895217153,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 2998.445394477457,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3152.5452595875336,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15663.139582568178,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 35700.49835794603,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 367.2764037431534,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 13623.283376338359,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2413.104982711734,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 331.359845955384,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 13616.742269468257,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 2020.3042509506886,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.30336240510756773,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.315203329937251,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.535534289230432,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.06780520420179632,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.28804410454810947,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.328450899958267,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20430383481501035,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.060319393872282,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 23.133293392952794,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22800.856007799994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004938259071175955,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012617637296212916,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.06445702157936052,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 121.08539994705882,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.0068156971212046695,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.01133056597668317,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.11813928775131403,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 121.1846774882353,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 10.721950630209193,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 17.34260104595202,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 28.209216829049296,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 45.37618611070707,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 73.91419767817459,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.414574905892903,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.771678203292495,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.93809379227579,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.445329028776976,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.41837648488372,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 303564.9453000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 314394.32121249987,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 332908.87136249995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 290760.0316750001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 299600.0048875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 313633.54860000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1747.1386124999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1875.3625499999998,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2971.5837125000007,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 17.712787499999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 52.708725,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 437.7985,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "netliomax25-code",
            "username": "netliomax25-code",
            "email": "netliomax25@gmail.com"
          },
          "committer": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "id": "1776f97521c3521723e181d466db72e007085a5c",
          "message": "use Locale.ROOT in DataSet table name folding",
          "timestamp": "2026-06-01T06:31:38Z",
          "url": "https://github.com/apache/groovy/commit/1776f97521c3521723e181d466db72e007085a5c"
        },
        "date": 1780303142373,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 106715.63224401698,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 62286.47648785725,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2649424.523690513,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11851.252716381725,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18242.117277127472,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.26169380569709,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0492030404943025,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 118.80580872865475,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 529.0148336556974,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2520.4660425226934,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2524.4103570476063,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.1572207342006364,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.5105301547086563,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.50789621766929,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 629.3590723253171,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3383.885462802939,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3398.9403310694556,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3208.294336416585,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15537.311831889096,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36053.49904951725,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 349.262568671392,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15407.236256274806,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1828.4546555107117,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 314.5245131880807,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15353.232285977405,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1551.3621145944478,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.2761921378188307,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1971765285079725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.101749112469161,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05028213514389317,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.21668405495779325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 0.9759181614448066,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20870680021347457,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.0125295993459087,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 21.960209064249952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22292.9735134,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.0047455839153863685,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012311231295537982,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.057815593822101406,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 112.74206952544117,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.004431484516504253,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.010932909652132606,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.11943799687427355,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 128.63024954831013,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.8024175856718,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.869382940438694,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.670471218671832,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.44010144179421,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.22108190333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.337489320534625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.4774398771596715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.853550628490119,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.326643732852077,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 22.791461289425282,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 326550.54416249995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 341247.92145000014,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 349750.0542624998,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 304304.15905000013,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 311859.567025,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 325702.1861124999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1751.7732999999996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1873.5097875000006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2860.7610375000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 17.201012499999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 50.141000000000005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 446.1615750000001,
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
          "id": "653195f4057c7f77032b10b766562502db2705b4",
          "message": "Use log y-axis for JMH adhoc throughput charts\n\nLarger workload sizes span ~2 decades of throughput, so a linear axis\nflattened the size=10000 group into indistinguishable slivers despite a\nreal ~12x best-vs-worst spread within each size. Matches the allocation\ncharts, which were already log.\n\nCo-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>",
          "timestamp": "2026-06-02T06:58:05Z",
          "url": "https://github.com/apache/groovy/commit/653195f4057c7f77032b10b766562502db2705b4"
        },
        "date": 1780388975328,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 100775.1460091056,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 58256.061817174406,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2676389.5636551757,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 12046.637399953514,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 16460.481602355787,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.93451350301002,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 0.9546971381285825,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 111.39306223631665,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 557.509495448807,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2247.7673225683056,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2250.32592565729,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.017114472345682,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.274875725544068,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.384765656133834,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 560.9198192796237,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3002.3258878980255,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 2998.6283448006575,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3115.7960933675254,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15692.984865770977,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 35665.52720613898,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 372.6450066397174,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 13466.337959525015,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1949.1600908622545,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 333.35431073416396,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 13595.155651946994,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1817.1836844475754,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.30465941210589303,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.3328887691280173,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.580335741232715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.06809810233193643,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.2699931372754046,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.2093494369630597,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20587230762109776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.0707337287201435,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.646463047191013,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22868.451448300002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.005048347872466631,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012633593506695271,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.06459861622884991,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 121.82383039411764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.004882046423856468,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.011448799793930318,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.11809777323968378,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 122.0159154,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 10.752397018313777,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 17.416050480442273,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 28.2681234022239,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 45.276691086666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 73.8483231064815,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.4213192730420823,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.557255063354982,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.935851472932539,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.450774731034304,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 24.434124643126285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 315926.46091249987,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 327928.86993750004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 348740.7928375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 304083.35938750004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 310392.3433625001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 325166.5926750001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1781.7118749999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1950.0866999999994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 3064.0523375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.679162500000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 52.3612875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 435.67197500000003,
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
          "id": "c71d51560dc2211aea298ad2360e8a7449d1e182",
          "message": "GROOVY-12060: groovy-contracts could support @Decreases at the method level (sibling loop test)",
          "timestamp": "2026-06-03T02:29:10Z",
          "url": "https://github.com/apache/groovy/commit/c71d51560dc2211aea298ad2360e8a7449d1e182"
        },
        "date": 1780476397922,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107923.50540962804,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 62356.30556918993,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2644908.4386677183,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11860.485140936145,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 17421.79665424542,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.98414762057581,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0518923778457308,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 119.45363850399015,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 529.865879588147,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2511.279917626936,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2505.0260193345407,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.1368258030196023,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.5066573866281843,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.506135656882858,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 629.9855238964482,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3384.427368576602,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3402.259489363005,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3284.5766197667326,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15536.265477922385,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36136.96372181972,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 343.5540411496155,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15439.533762732706,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2084.3945267611116,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 311.9817209010017,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15432.21217715145,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1683.5658659110438,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.27443461651960543,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1882739789326937,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 4.972337166791553,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05050003873020901,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.21030668369705924,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.0357991935444415,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.2075625807678084,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 1.9779582874592516,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 21.949832850659654,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 25410.6486587,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004467179795076932,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.01233887417707857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.05789329501219229,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 109.32729213241484,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.004111258409749345,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.01085942236693219,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.08909569444943229,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 112.11085223447542,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.800194336819645,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.877284239065668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.659770470723792,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.50249930204082,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.16669883,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.3838089783413623,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.487420531423018,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.711046130484153,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.337076231778004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 22.856584383970898,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 348034.78921250004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 361837.4758,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 371230.8516375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 322549.5946749999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 329872.83066250006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 346214.44547500013,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 2001.533549999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 2198.966712499999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 3135.0992250000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 17.57699999999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 53.2028625,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 439.02212500000013,
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
          "id": "13b009a3243716c000d3ceb7db681883d98bf76b",
          "message": "GROOVY-12059: groovy-contracts: test nested closures in postconditions\n\nThe fix already covered @Ensures (the method/constructor path in\nAnnotationClosureVisitor recomputes variable scopes for both pre- and\npostconditions), but the original commit only added nested-closure tests\nfor @Invariant and @Requires. This adds the matching @Ensures coverage:\nnested closures with an explicit parameter, the implicit 'it', and\nreferences to both the method parameter and 'result'.\n\nAssisted-by: Claude Opus 4.8 (1M context) <noreply@anthropic.com>",
          "timestamp": "2026-06-03T22:09:51Z",
          "url": "https://github.com/apache/groovy/commit/13b009a3243716c000d3ceb7db681883d98bf76b"
        },
        "date": 1780562130899,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 102088.52216778776,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 44793.394532949846,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2670033.398992032,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 12192.083082147543,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 16498.05962711502,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 88.09467783777093,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 0.9535394602539693,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 111.83212646947047,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 559.0672589997096,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2250.487769602221,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2248.8290021146286,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.021480043906755,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.3809782995508852,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.391718430043499,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 559.146089326405,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3004.0948094599485,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3000.3649607562884,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3123.422980212375,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15697.387970086615,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 35627.36297842876,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 393.2055538471196,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 13563.23541753791,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2133.972300894504,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 319.02298345985963,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 13607.47828920511,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 2530.244575399069,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.3041482644936302,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.3141271129036833,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.569649106602578,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.06776773496692282,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.28887612442197025,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.2610691437971988,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20511170814063004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.064945704544998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.530714100387016,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22832.7793265,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.005031366182718072,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012620665435559375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.06460709133690537,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 121.40515387058824,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.005048681376706564,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.017206553668567402,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.06363672423945925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 121.28108438823531,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 10.772424080489907,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 17.444179053770615,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 28.137178810700313,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 45.70026445681819,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 73.65593760714285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.4107953779761386,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.5301732757103945,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 9.369744834499533,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.46085072691586,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.38325619534884,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 307054.5519249999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 316399.0074,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 336665.79896250006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 292198.7215625001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 299858.09033749986,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 314769.65043750004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1721.9261250000006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1857.6960625000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2989.5295374999996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.039174999999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 52.2440875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 453.7181124999999,
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
          "id": "13b009a3243716c000d3ceb7db681883d98bf76b",
          "message": "GROOVY-12059: groovy-contracts: test nested closures in postconditions\n\nThe fix already covered @Ensures (the method/constructor path in\nAnnotationClosureVisitor recomputes variable scopes for both pre- and\npostconditions), but the original commit only added nested-closure tests\nfor @Invariant and @Requires. This adds the matching @Ensures coverage:\nnested closures with an explicit parameter, the implicit 'it', and\nreferences to both the method parameter and 'result'.\n\nAssisted-by: Claude Opus 4.8 (1M context) <noreply@anthropic.com>",
          "timestamp": "2026-06-03T22:09:51Z",
          "url": "https://github.com/apache/groovy/commit/13b009a3243716c000d3ceb7db681883d98bf76b"
        },
        "date": 1780647678174,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 106774.08383498045,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 62192.72641366253,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2640525.511038753,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11872.710719367498,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 17918.90663259122,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.26110001073093,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0486131842225581,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 121.32691796266882,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 528.4340388976851,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2524.9239694198063,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2521.0972124493055,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.1606393234376426,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.5073823791330776,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.5146854188481447,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 626.7722173549612,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3387.7599975434873,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3399.6599963611247,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3224.2546255202433,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15550.146128684788,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36106.332987171045,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 369.9464299716521,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15391.87780339427,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1925.2555731193243,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 315.5877908535284,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15415.98164070058,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1509.5644879958131,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.27534165852769676,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1920231112808979,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.015407582195925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05093171838134718,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.2378581946392877,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.166053908473866,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20349465584848506,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.009510626419369,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.009567564587815,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 25161.5387553,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004486932760346346,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012289129445640066,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.05776511552442305,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 113.40780477190015,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.005951314672969098,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.015620436708017249,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.08891998560738687,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 120.77426399877193,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.829401774840244,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.831474159755032,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.64106627161636,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.444738724489795,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 66.94368264333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.3845343502581215,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.482440363209155,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.728697745356538,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.321880988571428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.21441153072708,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 328946.04916249996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 338542.1859875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 359647.1360124999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 311172.8365250001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 319927.32071249996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 333891.650425,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1732.5854375000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1894.8325750000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2798.4656125000006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.88989999999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 51.55297500000002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 442.1086875000001,
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
          "id": "5499e6c359c403bad892929c34f583170d930d50",
          "message": "GROOVY-12061: Develop a threat model for Groovy",
          "timestamp": "2026-06-03T06:46:04Z",
          "url": "https://github.com/apache/groovy/commit/5499e6c359c403bad892929c34f583170d930d50"
        },
        "date": 1780732440728,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 106702.68740052155,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 48108.64933104279,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2647928.8707910264,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11929.595232718455,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18165.821606610203,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 86.96265567663878,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.04977568562795,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 120.20955005891696,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 529.2242443339038,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2527.122976492764,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2518.076389949424,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.1608963246482107,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.5141429038704395,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.5100132705036335,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 629.8585791553427,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3387.1218600667444,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3400.0461446131544,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3237.28330639275,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15521.115340602362,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36222.605686009374,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 354.45857209847935,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15414.515041504517,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2061.672284547263,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 311.9979318137346,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15417.390471568102,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1557.261659873728,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.2778185534812849,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1919939381589162,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.086676324212315,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05051886991481147,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.2130785099853715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 0.9758806863895909,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20878112828504372,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.010292857134608,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.11805121207729,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22136.84175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004751152259512228,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012320006105073125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.057643790485718605,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 114.49380287669935,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.005772651509412379,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.015581689789324157,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.08895810367001782,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 112.86152071045751,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.786158233074929,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.828542129890014,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.661751122314183,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.4191947244898,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 66.94055453666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.3786205424344926,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.386450964180663,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.862508701516406,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.328206310344186,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.13902663228172,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 328545.47767500003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 344309.0034125001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 353662.12883749994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 308497.705725,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 316550.528,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 327086.8507249999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1733.5029500000005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1834.9162500000002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2807.8489624999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 17.1706625,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 53.9017375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 447.35620000000006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
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
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "e53f643e0a88d7cc81afc91f4e3234f2c351e149",
          "message": "Bump actions/checkout from 6 to 6.0.2 (#2593)\n\nBumps [actions/checkout](https://github.com/actions/checkout) from 6 to 6.0.2.\n- [Release notes](https://github.com/actions/checkout/releases)\n- [Changelog](https://github.com/actions/checkout/blob/main/CHANGELOG.md)\n- [Commits](https://github.com/actions/checkout/compare/v6...v6.0.2)\n\n---\nupdated-dependencies:\n- dependency-name: actions/checkout\n  dependency-version: 6.0.2\n  dependency-type: direct:production\n  update-type: version-update:semver-patch\n...\n\nSigned-off-by: dependabot[bot] <support@github.com>\nCo-authored-by: dependabot[bot] <49699333+dependabot[bot]@users.noreply.github.com>",
          "timestamp": "2026-06-07T01:55:18Z",
          "url": "https://github.com/apache/groovy/commit/e53f643e0a88d7cc81afc91f4e3234f2c351e149"
        },
        "date": 1780820084892,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 87076.4941639781,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 71100.76226769539,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2292667.8980347025,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 12608.888041467515,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 19130.10139780308,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 67.78574217774425,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 0.9379618282792069,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 75.07089667451896,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 447.43911038544746,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2123.9911126578327,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2123.155524047994,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.060808350672235,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.0218171680270407,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.0222917244678347,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 589.9054556881031,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3635.7730616961853,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3638.081084265477,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3093.241155334819,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 14032.706018731715,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 23034.69938830914,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 333.56382991049156,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 10690.601415393206,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 6309.168655909694,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 306.0649896335541,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 10698.392697022407,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 6050.064909155685,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.3268086207505785,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.4144011420508407,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 6.008711863197159,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.10184809649125492,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.4593813957710323,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.8999523334907966,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.21531119561689419,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.180849184847486,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 25.228536530427213,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 25704.009304,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.005199987596031604,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.01662673219535664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.13884422646588732,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 262.3133011611111,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.006896309847520288,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.015796057804896495,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.13770086451677602,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 255.6535878208333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 10.452255102900027,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 17.116664541240052,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 27.64783585450913,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 44.90728477637681,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 72.62397013571429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.811351105710082,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 6.2054399976459,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 9.977575975335133,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 16.018623668292065,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 26.30348378550923,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 328566.25573750003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 335400.7326875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 352871.1010000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 311118.5798874999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 321678.03112500004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 331221.06781249994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1575.9988500000002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1665.1570749999998,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2379.425999999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 14.678600000000003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 34.2435,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 262.58677499999993,
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
          "id": "9f8ebfc8972504541da1c5ec85f80159d1489f8a",
          "message": "GROOVY-12067: IntRange.containsWithinBounds delegates to contains, breaking the continuous-bounds contract",
          "timestamp": "2026-06-08T04:07:38Z",
          "url": "https://github.com/apache/groovy/commit/9f8ebfc8972504541da1c5ec85f80159d1489f8a"
        },
        "date": 1780907811253,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107719.94724585136,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 61926.50058399498,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2667419.682512553,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11938.4127066982,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18199.71596121069,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.43877821371633,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0510240289044859,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 122.68716945606866,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 529.2938792318538,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2524.0162440371646,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2526.274766170047,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.1572723131000253,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.5079907045960836,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.510637343402292,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 630.260939808705,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3389.7451884338516,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3397.6657898288563,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3203.640076299391,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15522.073842694701,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36168.03113750159,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 361.16999785746344,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15439.28842809234,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1950.5202455553833,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 314.3420785270845,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15431.712756721501,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1593.970491630286,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.27575385763881366,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.2034210545681496,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.021712284994441,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.050213995298047175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.2056771345906699,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 0.9804652092470375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20749368909222327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.0056697585738927,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 21.97978268438912,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22131.3281157,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004879750826211488,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.01223987536019837,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.05859124800588007,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 119.26654616971491,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.005519961601272327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.010868337854673785,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.059270394954117345,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 113.07021644784315,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.783596470877571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.83570243016498,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.665111061905186,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.504199885076524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.09586623,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.382575456507624,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.463753364973528,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.837509881938326,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.307143971626138,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 22.79441063749451,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 323349.2675625001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 337527.4132749999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 353112.3534,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 310681.547325,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 320104.82438749995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 331112.1869,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1751.67135,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1880.3207625000002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2858.0630749999996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 16.550024999999998,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 52.2418,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 445.9861000000001,
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
          "id": "61740ec67af12e1f9fa111aabfe538ebc11be20c",
          "message": "GROOVY-12071: groovy-contracts: ContractClosureWriter strips generics from closure parameters",
          "timestamp": "2026-06-09T07:20:26Z",
          "url": "https://github.com/apache/groovy/commit/61740ec67af12e1f9fa111aabfe538ebc11be20c"
        },
        "date": 1780992513805,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107839.00519708304,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 62218.82624990047,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2650932.572859208,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11959.195766756297,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18216.414449101117,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 86.97062702581299,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0487350262211543,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 121.62051947506389,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 528.6873608689302,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2521.0650113219285,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2525.9855390838234,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.1500226982667585,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.502808065264496,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.503182091255995,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 629.6593188367664,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3388.6504060595007,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3400.636879294151,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3235.7958215729077,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15510.5107359393,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36050.62154219459,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 355.5831418059334,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15434.24562696445,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1909.9953081441581,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 308.070944763009,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15409.97764549919,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1459.117722388182,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.27386842082718466,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1942175984608885,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.003459654839096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05045082834748622,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.21331768589495184,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.0994591296517255,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20736907139936317,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.0101823013024336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.02936822677244,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22354.576922900003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004490775558485178,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012290007521052044,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.05861626800899837,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 136.06480661999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.005973931358480661,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.015604315434571478,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.0888687502206415,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 135.24658785625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.817153040359692,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.832546438292479,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.626773350097373,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.482612642559516,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 66.98691877,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.3876050316609136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.478777433322417,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.861289307745377,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.302046721013173,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.156070006816357,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 330983.31235,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 342212.57021250005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 360093.0416375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 313421.9397125001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 323043.1797125001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 336203.1009125002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1778.3314124999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1881.1054374999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2839.07125,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 17.95864999999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 52.191487500000015,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 441.3000750000001,
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
          "id": "4ce518dc181ce1d5d368af2b7af71f053578044e",
          "message": "some old missing @since versions",
          "timestamp": "2026-06-10T07:51:29Z",
          "url": "https://github.com/apache/groovy/commit/4ce518dc181ce1d5d368af2b7af71f053578044e"
        },
        "date": 1781080240579,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 101392.34109753477,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 58274.263270234704,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2671707.7545794705,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 12238.372654179815,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 16419.96264183884,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.3095117933997,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 0.9552081992414088,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 111.53421919898229,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 554.3353503789332,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2243.475461645283,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2240.9093863592707,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.0176662362802293,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.3842838564550837,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.3651586306310968,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 558.4174140437706,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 2997.7096129760557,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 2993.6782917658065,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3179.8070072748924,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15632.704408972117,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 35604.38778755138,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 370.84081958320655,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 13573.45434551753,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2143.9530311437024,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 325.2514253304604,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 13548.659946807054,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1805.9701008717743,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.3041930807919517,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.3377612477146228,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.6346685835228545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.06809153968583502,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.2897898012206962,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.2602246533650514,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20502967748554268,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.0758868386637777,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.75963680563609,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22909.514814799997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004918855478972105,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012659786159052796,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.06473811825173051,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 121.90676319411764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.007025198102915568,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.017208177593757226,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.11844293241981374,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 122.26812325882352,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 10.770848572770277,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 17.44186871284976,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 28.34619654443942,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 45.665091947222216,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 73.67940604933862,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.4348323977160846,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.522023262121212,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 9.372674550102143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.496568814281677,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.54634003970002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 316936.44837500004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 322858.5427,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 341587.91278750007,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 295523.0211125001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 305523.07193750015,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 322848.2771625002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1765.4576874999996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1874.2542374999994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 3022.7953749999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.857425,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 51.308649999999986,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 441.3842374999999,
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
          "id": "5d84d5ff6f06cb735d0fb30b4fa52568e933c2f2",
          "message": "minor refactor: reduce javadoc warnings",
          "timestamp": "2026-06-11T05:24:44Z",
          "url": "https://github.com/apache/groovy/commit/5d84d5ff6f06cb735d0fb30b4fa52568e933c2f2"
        },
        "date": 1781166971095,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107514.13881889249,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 61989.245521738194,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2648203.405000628,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11895.589050255421,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 17930.293385787307,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 86.86441134996258,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.049373267580774,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 120.23758843828088,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 528.1274100274596,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2520.5298014585524,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2515.453613737821,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.158469801276513,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.502522740193103,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.50220233668185,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 630.3077617094184,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3384.2411709824664,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3395.696784758931,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3229.7776183963238,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15522.461114671718,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36030.91378920978,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 350.41743834083456,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15403.295871859806,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2007.2736484145648,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 312.991193876362,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15383.63733535579,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1507.4896736900102,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.2756309150106867,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1999395728266578,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.009353890968275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05092472668972982,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.21384240587980993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 0.9816472999826933,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.2082831781493371,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 1.9919566038826642,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 21.640976752197286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 25435.9039635,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004732795783788445,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012321844548715178,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.058683990820486566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 133.6603297124265,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.004405694928886398,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.010938723233673859,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.05924323729480757,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 135.90079718,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.770129958113879,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.832621704461943,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.674470091609862,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.49306650786565,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.19147155,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.376747930555532,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.484019797059215,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.872129355300915,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.326266531999627,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.185449047126433,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 333535.63505000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 341463.73034999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 361976.040625,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 315473.3100500001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 325814.5160625,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 338396.2498750001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1762.8097125000008,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1910.1817749999998,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2834.4489875000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 19.573137499999998,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 51.55942500000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 442.0432125000001,
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
          "id": "f8f2f31effe9e0249efd93bf73b310275fced318",
          "message": "GROOVY-10985: additional test for doco purposes",
          "timestamp": "2026-06-12T06:37:05Z",
          "url": "https://github.com/apache/groovy/commit/f8f2f31effe9e0249efd93bf73b310275fced318"
        },
        "date": 1781252889820,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 101840.74697204135,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 44620.431973385676,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2668687.379398908,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 12123.13648723477,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 16509.768008447627,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.42586495041567,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 0.9564849326928957,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 111.88925616670076,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 559.2838333943221,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2248.600229769348,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2249.2519398993213,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.0212060654905293,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.3895341902552687,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.340108918547368,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 561.1815213328084,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3004.7526358913824,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 2999.3668304792373,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3163.501136838853,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15696.071054310456,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 35683.714199586015,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 361.7216211791706,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 13560.694872589907,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2045.1198364697393,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 342.5327258840201,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 13575.433789055136,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 2061.6946044750994,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.30235473405188534,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.32705032872371,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.527177667681312,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.06899113996790165,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.2885221900641929,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.161057027371239,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20534283201480732,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.0859104344639534,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.8034760933927,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22899.663913499997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.00512123581223871,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012620194400792187,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.0643594607510575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 121.59040214705882,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.0066172341925819405,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.011397821257746788,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.11866978238412518,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 121.6182912117647,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 10.716503603122435,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 17.343198651832544,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 28.143032992507823,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 45.34157907742424,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 73.89029939219577,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.415280011757585,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.526528751990777,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.93201398323782,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.473000459451185,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.44624026437757,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 309105.337125,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 319746.5617624999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 337906.4186625002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 295162.3211249999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 306305.94831249997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 321194.61382499995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1763.6606500000003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1951.4038249999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2993.967775000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.48300000000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 54.56200000000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 438.85609999999997,
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
          "id": "7c3fe7067f8501a7fa368a5dea2273384c13be40",
          "message": "GROOVY-12085 follow-on, align vertical bars for terms containing emojis (best effort works well for modern terminals - may still be out for some typically older terminals)",
          "timestamp": "2026-06-13T02:13:17Z",
          "url": "https://github.com/apache/groovy/commit/7c3fe7067f8501a7fa368a5dea2273384c13be40"
        },
        "date": 1781338116773,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107083.67583204256,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 62286.816366403444,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2669657.587065671,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11899.920821565162,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18172.566718177775,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 86.82302821496245,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0482232097819681,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 120.36649417784392,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 528.2283280112495,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2521.4773381913665,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2521.94938268611,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.156244433337702,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.476326631026616,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.5077180500710385,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 629.507445520248,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3384.970283025227,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3399.273688637832,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3219.091240212364,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15508.660899613114,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36131.191811334524,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 351.15737468298903,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15388.44265911658,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1706.4357997981817,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 316.6995223431581,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15414.119104477086,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1432.559089946975,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.274844473845793,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.204043109398712,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.000443355804375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05109136634410862,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.20608054968135944,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.0452793392963424,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20979100718322669,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.012399097707315,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.47896519076155,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22393.843784000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.005092513793975591,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012353617844738003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.05870888396243599,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 101.89786695500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.005785315188402863,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.010892849797676418,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.11869506863042498,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 101.88947157,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.772884869934046,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.907998302895589,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.632381374229148,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.568905331249994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.24548621000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.3829510006209893,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.471869507467135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.861044298299412,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.330154538064843,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 22.841446210321248,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 327370.7258875001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 337580.7685875001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 358079.90520000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 307775.74694999994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 316159.646675,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 332992.878025,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1806.939475,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1846.9603625,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2835.692437499999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.389600000000005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 52.925475000000006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 430.1712375000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
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
            "name": "Daniel Sun",
            "username": "daniellansun",
            "email": "realbluesun@hotmail.com"
          },
          "id": "5db6dec99f90b73b29126332635c699255abc254",
          "message": "Bump actions/checkout from 6.0.2 to 6.0.3\n\nBumps [actions/checkout](https://github.com/actions/checkout) from 6.0.2 to 6.0.3.\n- [Release notes](https://github.com/actions/checkout/releases)\n- [Changelog](https://github.com/actions/checkout/blob/main/CHANGELOG.md)\n- [Commits](https://github.com/actions/checkout/compare/v6.0.2...v6.0.3)\n\n---\nupdated-dependencies:\n- dependency-name: actions/checkout\n  dependency-version: 6.0.3\n  dependency-type: direct:production\n  update-type: version-update:semver-patch\n...\n\nSigned-off-by: dependabot[bot] <support@github.com>",
          "timestamp": "2026-06-14T01:24:25Z",
          "url": "https://github.com/apache/groovy/commit/5db6dec99f90b73b29126332635c699255abc254"
        },
        "date": 1781425590795,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 106898.21209702858,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 62270.991461589874,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2647008.4332709047,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11886.457696257372,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18116.64139866872,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.28715909248442,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0474704004537556,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 119.76928077857038,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 528.1213125109563,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2517.2849818412724,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2517.786116817443,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.1612083077559356,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.5034471797139504,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.512921118075588,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 630.7946815364419,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3384.738938790909,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3396.533414785657,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3240.2180444817814,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15494.350166288621,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36082.8521171382,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 360.32313287098907,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15397.063930153643,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2025.5229213133443,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 318.1526618384954,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15408.451194829144,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1423.9012565496755,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.27667424117907624,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.2010503154712542,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.014995024809833,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.0510783855798882,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.21275212637040436,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.0629412917279475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20374515822024675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.0173047987521953,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.36680604680084,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 25366.4628745,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.00483767855947548,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.01231468622303351,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.05875039356717787,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 121.84717042916665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.005635652759305642,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.020300713440235506,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.08946505020146268,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 109.8204073723465,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.79044620820451,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.865837852477815,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.686952296170073,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.57440512865646,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.16918458333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.3805765519746807,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.46828507691796,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.824550402272545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.342522316051728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.21819580990377,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 320810.31750000006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 331765.7953499999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 350204.2240625,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 304745.25956250005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 313171.90495,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 328728.37594999996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1745.5591874999996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1901.6237125,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2800.0323000000003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 16.949024999999995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 51.734674999999996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 431.6561499999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Jochen Theodorou",
            "username": "blackdrag",
            "email": "blackdrag@gmx.org"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "72002909d7079f9b60b12459b203eeac234541f2",
          "message": "Merge pull request #2597 from apache/feature/GROOVY-12068/short_paths\n\nGROOVY-12068: add fast paths on DTT for better inlining",
          "timestamp": "2026-06-14T22:47:19Z",
          "url": "https://github.com/apache/groovy/commit/72002909d7079f9b60b12459b203eeac234541f2"
        },
        "date": 1781513830082,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 108057.34380858012,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 62236.548000522984,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2647945.8998587,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11991.056212822386,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18238.76380000925,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.76828378324049,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0504146189099384,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 119.82598923892199,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 529.4939238099329,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2524.066615858471,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2520.0517820412874,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.1587261609909225,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.5061647940830722,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.510249026008174,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 631.4862088362072,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3387.541923645599,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3402.147385908696,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3819.6751933108108,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15534.552610474235,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36069.80834186035,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 390.10696720511737,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15424.769269319731,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1769.4040274072897,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 330.83718570936855,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15432.840796104498,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1430.938309736999,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.27725598321431144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1896430771538613,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 4.974301609285791,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.051451841516800635,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.2126808954616574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.1072930782575443,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.2087505488001992,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 1.9717505912791045,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 21.584184670807595,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 25107.7732429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004992078656859682,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.01227733434091284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.057871597592011034,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 124.59691428977939,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.004219747249783241,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.010855622553985114,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.08883446906485326,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 111.45794818734478,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.781759261888746,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.875126734431344,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.577375454803637,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.44416947551021,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.12874925999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.5581350578025686,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.464781261823804,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.861783772652261,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.126688128709603,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.147181772413795,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 327522.62871249986,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 337672.31939999986,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 356761.63696249994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 313257.11437500006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 319009.50641249993,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 337083.70158750005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1808.222925,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1885.7459625,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2835.9716000000003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 16.87995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 49.72715,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 446.6859625,
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
          "id": "141150926387f5f1739369638d614a74275f2821",
          "message": "GROOVY-12089: Groovy 5 ClassNode.getGetterMethod() can clone a getter with a null exceptions array when @Entity and @Sortable are combined",
          "timestamp": "2026-06-15T08:05:24Z",
          "url": "https://github.com/apache/groovy/commit/141150926387f5f1739369638d614a74275f2821"
        },
        "date": 1781601187393,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 106892.89280048358,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 46147.03813930299,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2648507.5303100734,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11870.38344570909,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18204.13024711197,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.425334026763,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0489309545482717,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 120.26464788672288,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 529.0292424478481,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2525.8095947537745,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2512.582280193942,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.1577785186275262,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.5013277198242365,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.5047817153285115,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 628.6470027394554,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3375.334329261702,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3398.3724833424153,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3810.189177704438,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15484.333255521991,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36103.64131297555,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 379.90387455410007,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15384.848146774782,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2306.2832659477303,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 334.19241259334933,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15375.085251229952,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1806.544047739076,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.27333959417714415,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1910452386702173,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 4.951111902387066,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05104159835841563,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.20986592574156476,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.0208069637963149,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20805719232725917,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 1.9885282585136383,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.000476865666492,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 28156.536045499997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.0050110043661569246,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012287199534835166,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.057876928815794336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 101.61069449,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.005865378082070618,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.015612949660144024,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.11882208615235657,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 101.92622250999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.793822761235948,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.868601103705288,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.633672110921783,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.569960335459186,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.3147852,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.3822285884277967,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.477419421134837,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.861322513208062,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.122646649713264,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.12898310056955,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 336464.57418750005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 345067.38795000006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 365169.50182500004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 319231.43945000006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 335598.60121250007,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 350059.80263750005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1845.7373750000006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1957.685987499999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2929.8608624999993,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 19.297774999999994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 51.23461249999998,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 435.6618000000002,
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
          "id": "72f671532a630f4b09b28e46d8dbcb517bcf5f4d",
          "message": "GROOVY-12092: Allow a \"groovy.indy.callsite.cache.cleaner.thread\" flag to turn off the PIC-Cleaner thread",
          "timestamp": "2026-06-16T13:20:08Z",
          "url": "https://github.com/apache/groovy/commit/72f671532a630f4b09b28e46d8dbcb517bcf5f4d"
        },
        "date": 1781685803155,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107217.44805202328,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 62228.55801616759,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2640094.3702602303,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11902.492827565318,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18217.395979967532,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.3191436088867,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.043955386222909,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 122.49260825265507,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 527.9471610459498,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2523.248781505216,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2516.4604132504355,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.159519827880349,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.50194752602494,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.686030260295174,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 632.0674559628802,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3381.4317244306776,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3400.1718467088626,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3813.0399738195,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15521.853640450912,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36020.00947683142,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 382.20697122384274,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15370.232887498642,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1819.5680076937545,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 320.0847865321306,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15392.59261957184,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1500.419218825415,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.27450441883388366,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1844022525488813,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 4.968590128836855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05018087948747093,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.2129610444787374,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.1098752673089067,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20802515474249636,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 1.9764844219146256,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.12319814437524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 25142.9120282,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.005108198047649109,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012282714781169416,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.058648540154819594,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 136.00158892000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.004451060981905415,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.0203082716969951,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.08889501856048804,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 136.21893439333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.79772106111063,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.868603359261346,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.62881582632262,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.47164367346939,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.20598471,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.3896969817125693,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.473820664468525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.856014032429535,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.11348885954796,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.140904180526597,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 324431.1802875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 335746.0439250001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 354171.9327125001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 307987.7842624998,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 321090.1462749999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 336819.57663749997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1776.4812125,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1884.9117875000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2822.4620375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.507624999999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 52.559275000000014,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 448.9133,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Eric Milles",
            "username": "eric-milles",
            "email": "eric.milles@thomsonreuters.com"
          },
          "committer": {
            "name": "Eric Milles",
            "username": "eric-milles",
            "email": "eric.milles@thomsonreuters.com"
          },
          "id": "8f560e7046b39ce677e5eaa5f7613c1ed5c65b6b",
          "message": "GROOVY-12091: fix type of trait property placeholder in setter method",
          "timestamp": "2026-06-17T18:24:38Z",
          "url": "https://github.com/apache/groovy/commit/8f560e7046b39ce677e5eaa5f7613c1ed5c65b6b"
        },
        "date": 1781771571382,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107819.65558671796,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 48292.395861141486,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2651884.5596933,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11927.082644907287,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 17956.190897254062,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.90754828268906,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0514564809487459,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 121.07561795130115,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 529.8716035632032,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2517.2431488427565,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2524.537956655877,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.1594691633321266,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.5172163695329983,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.497887194811439,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 629.2443743625454,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3387.7149796100543,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3401.8218388399387,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3824.133378560786,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15578.91684153939,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36121.56578563091,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 376.84491530033995,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15430.419765034201,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2008.8766049997168,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 337.7805276081856,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15433.98110979226,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1463.0580668960342,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.2738123280106103,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1800208878584688,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 4.99445572837082,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05094620064006696,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.21399121843206076,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.026536561669535,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.2085217094032458,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 1.9687200531036582,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.158439943174606,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 25100.438234199995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004809255420647267,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.01228656796105776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.057779750444347064,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 105.88594424982458,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.004213785764707238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.015547929784807558,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.05923940697952157,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 116.38881396011695,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.767445741236088,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.849708775952905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.596917853407984,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.396200695918374,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 66.96077869333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.3812409298188824,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.469601081435724,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 9.2789069117259,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.319518747183384,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.15101015402299,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 317836.4375625,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 328753.3179749999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 346901.0146000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 302546.8897250001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 311281.61634999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 325016.8445499999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1701.7093374999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1820.6930750000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2786.4180000000006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 17.530525000000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 53.25261250000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 442.106625,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
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
          "id": "1bb22751a4d4027eb53f42f524502e001c98561e",
          "message": "GROOVY-12094: Bump jline to 4.2.1",
          "timestamp": "2026-06-19T08:16:39Z",
          "url": "https://github.com/apache/groovy/commit/1bb22751a4d4027eb53f42f524502e001c98561e"
        },
        "date": 1781858636792,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107520.1927286678,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 62310.41733474385,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2661133.9616178134,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11905.047550024452,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18158.115663109988,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.43281410891765,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0505648920547697,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 122.29907333180233,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 528.9776103386997,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2519.290394905198,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2523.893630662291,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.153997397278384,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.4950222952358168,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.504436791798427,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 631.1403631504493,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3381.6111202655316,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3397.771415246021,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3812.255585610442,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15501.55110818317,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36059.24045536857,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 378.4012447034475,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15381.414975584636,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2011.729366669794,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 336.0947615865076,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15399.069783462084,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1576.5253322255453,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.27389266381635224,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1981062177205097,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 4.993111949119273,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.050943828026600156,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.210135064799861,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.0367516691896301,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20844532196697868,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 1.9864361665821104,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.280001126506704,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22241.5539497,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004950353265668171,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012344893036311134,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.058571850638877376,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 136.87677809291665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.005730096450159589,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.02033985750281445,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.08893137700411288,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 136.21651614625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.779881066341463,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.861177986526684,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.63124164647841,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.579035189413275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.16709345666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.387553617098855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.479701112998525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.75626446010581,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.334834223571429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.215476658433573,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 325756.8130375001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 340760.185375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 359323.0052624999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 320512.6642875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 327612.60283750005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 344941.48447500006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1877.7354749999995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1958.1028250000006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2953.632249999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.9455875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 52.59411250000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 437.48902499999974,
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
          "id": "9488b37ca09b89db266a82453f34e1e3f7b75a72",
          "message": "use basic cache-provider",
          "timestamp": "2026-06-21T04:04:22Z",
          "url": "https://github.com/apache/groovy/commit/9488b37ca09b89db266a82453f34e1e3f7b75a72"
        },
        "date": 1782030563459,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 106927.60054463598,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 62163.61118544708,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2652292.995894014,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11945.473484044833,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 17781.381102086816,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.03783372033901,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0467451710474442,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 118.19719342170572,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 527.8394667816148,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2519.672120079789,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2519.373494392624,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.1567399126253735,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.5074449879844893,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.4932704845443503,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 631.0424007320973,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3386.2941619442113,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3399.2363898877243,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3816.286033056122,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15514.29669402614,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36044.04214000715,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 383.38091427477025,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15419.866936130386,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1915.359327441185,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 328.9458977427579,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15417.482302031372,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1704.1335260657695,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.27323747379151686,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1837434987350137,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 4.974691968235043,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05096397764383598,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.23936543794369838,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.1544813245266885,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.2096155409343196,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 1.9783929389659618,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.287283909080546,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 25139.4218583,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004702998245538376,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012291489481577059,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.05937332283236577,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 113.70286346084364,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.006324102328205984,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.020279793808151404,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.11945366962473206,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 108.47914512314242,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.787729654945002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.833615853318335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.592961876971764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.534873894472796,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.4465121124138,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.3841416041117554,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.477573868758463,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.713821705607739,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.314147044290781,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.199254355399624,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 334566.36579999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 350294.66150000005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 367461.4445125001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 312913.7835625,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 320526.7671374999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 334429.62955,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1759.4678875000002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1877.0364375000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2838.562500000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 17.4176375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 53.20932500000002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 447.54241250000007,
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
          "id": "9488b37ca09b89db266a82453f34e1e3f7b75a72",
          "message": "use basic cache-provider",
          "timestamp": "2026-06-21T04:04:22Z",
          "url": "https://github.com/apache/groovy/commit/9488b37ca09b89db266a82453f34e1e3f7b75a72"
        },
        "date": 1782118897075,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 131497.90875471663,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 57689.644031385986,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 3460654.0093761995,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 15963.545120353801,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18503.62395464184,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 112.93576130915032,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.2332744711414276,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 142.6669912426616,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 721.0469938878963,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2899.2380327451333,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2902.084894358481,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.6103770339917323,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 4.376620133974628,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 4.362068607116409,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 722.0991258635484,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3873.0394716350747,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3866.355896016278,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 4585.338416203158,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 20261.78029517527,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 46018.84352806083,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 487.29438867329566,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 17505.64321139342,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2754.471320704066,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 419.5036189325016,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 17563.076952943353,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 2335.072372473747,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.2355192257626948,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.0227245873468243,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 4.338979332175732,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05283629500774828,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.21020124281392144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 0.899276597158611,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.1586090014898694,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 1.6097468255458829,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 17.47061847356677,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 17811.5322801,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.003857750559238993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.011601255940020481,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.062013190005344845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 94.64163726818182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.006751569070901927,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.01006463511410267,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.05954401475557736,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 95.18793103138529,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 8.373983096802098,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 13.479230247900414,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 21.75824175701609,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 35.28611964957653,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 57.266060357222216,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 2.651367861790672,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 4.283703028313852,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 6.944891120469495,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 11.189769240487276,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 18.118814115315317,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 245312.0068875001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 253826.68745000014,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 269560.4481125001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 236403.2488750001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 240957.96929999994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 255725.29571250002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1473.1946624999998,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1493.2070374999996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2345.9407125000002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 13.775187499999996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 39.75075,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 341.79730000000006,
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
          "id": "4542f412672781a05bfc8742dbbdc925b4da5352",
          "message": "scrub failed resolution - seems to be getting cached",
          "timestamp": "2026-06-23T05:19:11Z",
          "url": "https://github.com/apache/groovy/commit/4542f412672781a05bfc8742dbbdc925b4da5352"
        },
        "date": 1782202576470,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 100730.26738236743,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 58214.02036787114,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2663130.202590797,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11997.937531100008,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 16442.838074895797,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.49047290763824,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 0.9568338300829566,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 111.06488486984897,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 559.3371413009834,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2240.9275090281103,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2246.534150295026,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.014825662305527,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.3873680439554095,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.3878745531422707,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 559.3461408833937,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3001.556865921227,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 2994.0414278591406,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3526.9018926803387,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15642.032523869604,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 35562.70057060443,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 374.9020987133538,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 13571.900782737135,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2173.0341671279366,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 327.2387671518503,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 13593.93568023187,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1573.585295366906,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.30385362104255165,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.3366231468755891,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 5.582604486116272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.06799734858166151,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.2716680670537495,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.2597755708909257,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20672498006134,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.079327916984397,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.624530629769318,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 23133.401819000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.0048812395700526595,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012663531173954084,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.06468912356027376,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 122.04393895882353,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.0067373321620298994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.023129113722467787,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.06376152527081132,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 121.88048630588237,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 10.797265555267039,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 17.378669071323664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 28.19619006874022,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 45.39788194732323,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 73.92595646388888,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.417874351243397,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.54033665683686,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.972457897149898,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.474618879856115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.383225489534887,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 313215.2835875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 325148.06585,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 342959.85667499993,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 298018.81746250007,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 318721.1566,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 343782.5897125,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1876.9874125000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 2000.4457125000008,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 3204.5383624999995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 20.057012500000003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 55.4295375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 441.85106250000007,
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
          "id": "5252125f426e93c538b6724cfd086ba73c2cda4f",
          "message": "GROOVY-12100: provide Closure variants for AGM primitive each",
          "timestamp": "2026-06-23T11:17:47Z",
          "url": "https://github.com/apache/groovy/commit/5252125f426e93c538b6724cfd086ba73c2cda4f"
        },
        "date": 1782288121349,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 107393.54379763683,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 59701.98028395241,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2647457.6425495427,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11941.305419701268,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 17897.079985026947,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 87.56638551746474,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0500617312221894,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 119.13179485410012,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 528.8733975264792,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2530.763953284433,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2522.6616846606457,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.1611681847114554,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.505738143447909,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.4992702583084894,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 629.7629479313239,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3387.5761550926263,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3401.0744357326876,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3820.2453134225557,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15478.128583651323,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36104.12000356705,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 419.93347129921494,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15446.937971048674,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1914.1633401835675,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 327.5005801406321,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15417.960814083082,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1610.5214054039006,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.2737643659275158,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.178741519695022,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 4.955600629408275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05050388838745483,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.21365161444637795,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.0462571226030348,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20324176290082044,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 1.9695272675966558,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.127246062203906,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22419.855328800004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.0047698438182446345,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.01229638805208056,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.05778066389690475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 110.3789695491228,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.007688531484250499,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.020282869438403853,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.11866436244193321,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 126.83579096524511,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.785829414131992,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.815210596187976,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.6679957371308,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.53693924081633,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.12560497,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.375611371744474,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.472345265154329,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.843623325022415,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.330464357521032,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 22.76406282650179,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 325503.1898374999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 335315.4887875001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 353930.33182500006,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 309074.11493749986,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 318092.08867499995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 330794.04296250007,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1770.2063624999998,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1855.6057875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2789.1437375000005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 17.613537499999996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 51.75741250000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 431.05273749999986,
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
          "id": "49565020a078f78e514ad1fb0a3650af5761dd15",
          "message": "Traits: add two @NYI rows in the trait matrix test for discussion",
          "timestamp": "2026-06-25T04:34:40Z",
          "url": "https://github.com/apache/groovy/commit/49565020a078f78e514ad1fb0a3650af5761dd15"
        },
        "date": 1782374590456,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 105961.96074371494,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 48097.829960940384,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2641630.6048108884,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11832.877085826278,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18148.1324846649,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 86.53915415996225,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.0488296696404433,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 117.18502574048155,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 526.950649896761,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2518.7336318917523,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2522.4677319885227,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 2.152947168007989,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.4958804461169075,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.511355180261546,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 630.4939454287785,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3383.674642246288,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3395.2993823178485,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 3796.8842900091818,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15532.760675193716,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36046.11274404378,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 377.8150904991952,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15390.53536819398,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1854.311185522033,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 342.45616592325274,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15434.64305429199,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1592.9160275961772,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.276442867152069,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 1.1835150854845278,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 4.985138668604781,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05100805559672358,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.2125017883337795,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.1088688775189222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.20658824263494563,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 2.0374741297517507,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 22.42296355379526,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 22717.0067082,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.005115100973519237,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.01231410000808829,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.05971668668293743,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 132.83054386882355,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.0043693461012691125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.015603312216716425,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.059092764108804985,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 135.84410655750003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 9.80448404818019,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 15.866722381408474,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 25.65215960048685,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 41.535083483673475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 67.162572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.3793404974157033,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.496582522110863,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.711254109907784,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.343587750125627,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.19643142952419,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 336095.25332499994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 347546.1119749999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 364589.46338749997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 321406.3752624999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 331428.28521249996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 347462.46256250003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1772.8499625,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1905.6375500000008,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2839.2359125,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 18.4317125,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 52.5706,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 437.5052625000002,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          }
        ]
      }
    ]
  }
}