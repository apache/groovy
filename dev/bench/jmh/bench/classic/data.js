window.BENCHMARK_DATA = {
  "lastUpdate": 1778571701170,
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
        "date": 1778456340310,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 49861.44611067549,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 59035.362071638185,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2605184.6001982475,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 10959.720471449707,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 16484.780113438996,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 133.66091468418023,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.3483047242888913,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 467.85677591762425,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 140.6647388329701,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2246.7093105635786,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2248.743760204985,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 1.355349734095333,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.3909008505950182,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.389483212613611,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 467.96767668915044,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3004.374157475869,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3000.339783302235,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 1785.4013354881008,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15683.75956152529,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 35686.384510716554,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 297.4949483072312,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 13635.963256138732,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 2401.5128716204863,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 218.99301097255594,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 13613.027242629061,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1785.586350377725,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.13811444427178135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 0.5705224280674099,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 2.3704140337232262,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"8\"} )",
            "value": 10.159474286190184,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.06949685382009677,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.30836709188919265,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.3621353716162552,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"8\"} )",
            "value": 5.392666499310111,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.004521428064216903,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 0.013177986165879488,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 0.06332675649635329,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 120.8127678,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004680582625393338,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012626392409437187,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.06455503546839894,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 121.5551854352941,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.0050577914472735444,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.023126142306273058,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.06368671787126366,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 121.55986864705883,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 7.895535323972176,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 12.773928592984637,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 20.665749017263657,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 33.453050290683066,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 54.073766031507816,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.418324970385014,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.530951538546575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.964903472445306,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.462734642628508,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 23.423184267606022,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 218833.55751250003,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 218476.20996249994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 221273.88958750005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 5151.356175,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 5563.2162499999995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 7580.218737500005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1905.9405250000007,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1968.9741500000007,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 3135.638974999999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 16.738162499999998,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 50.7942375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 427.6457375000001,
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
        "date": 1778459211438,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 49659.21180294111,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 62818.961349966354,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2616262.1475334475,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11390.696954705329,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18207.389230705317,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 146.23653482715534,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.4764066581705924,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 527.4461352081362,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 158.96734951881226,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2522.778413377249,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2523.4425607842095,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 1.5165549184997782,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.508857879526454,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.5105807841512906,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 526.4097710337597,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3388.1078882887277,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3395.545214720808,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 2105.7987941316624,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15542.746612346778,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36130.50768097399,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 297.8439867013382,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15447.416484734811,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1919.0512444871433,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 238.05110071638828,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15428.950984522966,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1721.384072634762,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.12080984230761778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 0.49994157608213297,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 2.0875794050241154,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05063705454629549,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.22676894927094202,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 0.9944807473296999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.0041397883784699135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 0.012060516993437577,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 0.05907208443683086,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 120.67546550400327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004463386312979977,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012443401107094802,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.05792743251294928,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 119.24420412172515,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.006639183216432766,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.01076852195602421,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.08940503927636162,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 119.45615537290851,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 7.135395700447347,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 11.549599000392002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 18.71987637443752,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 30.277577874536405,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 49.28141324390244,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.3712380004675033,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.461941494699023,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.838666707432505,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.278489896465024,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 22.75780808874173,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 225257.07721249992,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 225648.970575,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 228013.38148749992,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 5175.954312499999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 5513.364324999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 7437.6806750000005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1821.8999999999996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1952.2197125000007,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2883.7172624999994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 17.512187499999992,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 50.092337499999985,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 432.1203625,
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
          "id": "c18dd839c712cddcaab67e4631ebe96e09b407fc",
          "message": "test additional user agent workarounds",
          "timestamp": "2026-05-11T06:34:33Z",
          "url": "https://github.com/apache/groovy/commit/c18dd839c712cddcaab67e4631ebe96e09b407fc"
        },
        "date": 1778486224009,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 49895.36192175592,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 58831.72193393187,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2641771.6867944514,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 10717.345809490202,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 16425.988627632483,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 134.08213476473645,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.3491363481295355,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 466.5483385004194,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 140.81704215161662,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2249.101422302859,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2250.5483076001374,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 1.3565391153107476,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.386135615766168,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.3884587821939327,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 467.7450400006154,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3004.150278950406,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3000.587606174959,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 2101.094930709948,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15656.858018395145,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 35590.17037848293,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 287.81441968401015,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 13627.348605071471,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1938.045958305418,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 214.91912483721026,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 13597.919365569813,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1794.758850693231,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.13756604236187467,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 0.5736915057666047,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 2.4018918230126323,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.07008932907174961,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.270548986439785,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.152874827972806,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.004241128372708533,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 0.01321660838975353,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 0.06331867478923967,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 120.36492107058822,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.005113216741489547,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012636913605180253,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.06437038019495905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 121.56642857058823,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.006448311386916332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.023111383685119728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.11819539457395856,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 121.71822617647058,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 7.896619159060043,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 12.751633240244296,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 20.651886286692616,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 33.416086362213115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 54.08893171379801,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.536747127042274,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.5398480244524695,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.931723961860255,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.436230060089368,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 24.246722904418604,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 231664.76008749995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 236677.468425,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 238163.04812500012,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 5562.426600000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 5900.092212499999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 8099.7541375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1959.5381499999996,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 2106.6747000000005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 3248.6803375,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 19.984412500000005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 53.38250000000001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 416.4194124999999,
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
        "date": 1778571700300,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.bench.GeneratedHashCodeBench.generated_hashcode_on_instance_with_null_properties",
            "value": 49815.39143441936,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.capturingLambdaApply",
            "value": 62519.9403315464,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.nonCapturingLambdaApply",
            "value": 2628266.679718568,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamMapNonCapturing",
            "value": 11148.761342400368,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.NonCapturingLambdaBench.streamReduceNonCapturing",
            "value": 18137.15503241616,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceChain_groovy",
            "value": 146.20506722634818,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceFib_groovy",
            "value": 1.4708645003043934,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.instanceSum_groovy",
            "value": 524.6495941824644,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovy",
            "value": 158.7687857584828,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_groovyCS",
            "value": 2520.006664095476,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticChain_java",
            "value": 2519.543168262364,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovy",
            "value": 1.5133041865658265,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_groovyCS",
            "value": 3.505317375208585,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticFib_java",
            "value": 3.4986732302440315,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovy",
            "value": 526.8967606867013,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_groovyCS",
            "value": 3381.856964212563,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyBench.staticSum_java",
            "value": 3399.542883713914,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovy",
            "value": 2088.1072344223876,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_groovyCS",
            "value": 15556.445831289016,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_1_monomorphic_java",
            "value": 36161.51313127492,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovy",
            "value": 307.20254666147093,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_groovyCS",
            "value": 15425.21095740998,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_3_polymorphic_java",
            "value": 1926.890571707816,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovy",
            "value": 237.26462395079028,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_groovyCS",
            "value": 15413.990265593453,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.dispatch.CallsiteBench.dispatch_8_megamorphic_java",
            "value": 1683.926737699648,
            "unit": "ops/ms",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"5\"} )",
            "value": 0.12111345140491007,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"6\"} )",
            "value": 0.5035711436975807,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.groovy ( {\"n\":\"7\"} )",
            "value": 2.0854500035188708,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"5\"} )",
            "value": 0.05047291884178838,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"6\"} )",
            "value": 0.20948096438440675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AckermannBench.java ( {\"n\":\"7\"} )",
            "value": 1.0422649783346891,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"10\"} )",
            "value": 0.004259555627406838,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"100\"} )",
            "value": 0.012111540208280053,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000\"} )",
            "value": 0.05903203651972577,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovy ( {\"n\":\"1000000\"} )",
            "value": 136.70216008000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"10\"} )",
            "value": 0.004691854969950661,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"100\"} )",
            "value": 0.012297877055470963,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000\"} )",
            "value": 0.057937469598044244,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.groovyCS ( {\"n\":\"1000000\"} )",
            "value": 137.12552528,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"10\"} )",
            "value": 0.007728442526954492,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"100\"} )",
            "value": 0.020295332776038455,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000\"} )",
            "value": 0.08964749618879442,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.AryBench.java ( {\"n\":\"1000000\"} )",
            "value": 137.23345704666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"30\"} )",
            "value": 7.185683305121725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"31\"} )",
            "value": 11.577759914269313,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"32\"} )",
            "value": 18.74364794251471,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"33\"} )",
            "value": 30.476046486883764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.groovy ( {\"n\":\"34\"} )",
            "value": 48.978816855052266,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"30\"} )",
            "value": 3.37631614895112,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"31\"} )",
            "value": 5.7525188317987475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"32\"} )",
            "value": 8.874353068649933,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"33\"} )",
            "value": 14.349306382550235,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.FiboBench.java ( {\"n\":\"34\"} )",
            "value": 22.767087810153257,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"500\"} )",
            "value": 229088.2154874999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"2000\"} )",
            "value": 229091.0952875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.instanceSum_groovy ( {\"n\":\"20000\"} )",
            "value": 232104.60498749994,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"500\"} )",
            "value": 5299.974937500001,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"2000\"} )",
            "value": 5734.387262499999,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovy ( {\"n\":\"20000\"} )",
            "value": 7531.8997875000005,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"500\"} )",
            "value": 1825.7394999999997,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"2000\"} )",
            "value": 1945.2620624999993,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_groovyCS ( {\"n\":\"20000\"} )",
            "value": 2879.298925,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"500\"} )",
            "value": 17.2890875,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"2000\"} )",
            "value": 49.536837500000004,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.bench.StaticMethodCallIndyColdBench.staticSum_java ( {\"n\":\"20000\"} )",
            "value": 423.52647499999995,
            "unit": "us/op",
            "extra": "iterations: 1\nforks: 80\nthreads: 1"
          }
        ]
      }
    ]
  }
}