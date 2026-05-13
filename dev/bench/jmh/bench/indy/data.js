window.BENCHMARK_DATA = {
  "lastUpdate": 1778659165531,
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
      }
    ]
  }
}