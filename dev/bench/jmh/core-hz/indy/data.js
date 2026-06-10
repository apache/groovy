window.BENCHMARK_DATA = {
  "lastUpdate": 1781079165290,
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
        "date": 1778455631580,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 66.03591341709678,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.654584274185415,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 130.65254381875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 68.43112964001482,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.542977315575793,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 12243.2393186,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 86.27838872499999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 92.88254194927535,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.402993030533203,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 91.07753366996046,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 64.46026987832661,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3084.0693053,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 87.00323866250001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 86.4114610625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 88.25140863913045,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.632755155433962,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.7862534193360136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.9444292653579065,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 81.60211752000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.406336436638712,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 0.9492256091780831,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 95.31567439155846,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.460162051826696,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.578539186316272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 190.9313022818182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 87.05859532047101,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 93.69817930194804,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 85.07694897466666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 102.842535655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 341.67562477857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 84.72338932916668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 303.9679299285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 126.70384523749999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 147.39169352857147,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.0422256669078624,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 5.573054245793062,
            "unit": "ns/op",
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
          "id": "160c9d57d19dd70b97fb7d3f286e285cac100496",
          "message": "tweak performance test which has stack overflow in some environments",
          "timestamp": "2026-05-10T23:27:04Z",
          "url": "https://github.com/apache/groovy/commit/160c9d57d19dd70b97fb7d3f286e285cac100496"
        },
        "date": 1778458513019,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 66.35802367139785,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.533495720098811,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 125.27542471617649,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 70.63054162068966,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.746201871627049,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 9823.6371044,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 96.41968606190477,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 96.35527127619046,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 15.732899772442057,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 97.97472805238094,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 65.99746868408602,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3106.1197896000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 96.48069370476192,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 95.42129252380951,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 99.38966721,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.267673892086266,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.886273460786571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.8894345469948725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 86.6118564365942,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.966987119775506,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 1.0681209277236385,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 103.31880865999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.6367082162757,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.871080453328608,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 199.22669007454547,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 96.8582092,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 101.01016792,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 92.48687914090911,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 111.8366669722222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 362.99260288333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 94.42924504090908,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 343.30236840000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 143.02001732952382,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 164.24503925384616,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.807613247258124,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 6.144148968143304,
            "unit": "ns/op",
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
          "id": "c18dd839c712cddcaab67e4631ebe96e09b407fc",
          "message": "test additional user agent workarounds",
          "timestamp": "2026-05-11T06:34:33Z",
          "url": "https://github.com/apache/groovy/commit/c18dd839c712cddcaab67e4631ebe96e09b407fc"
        },
        "date": 1778485561802,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 66.37890744946236,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.663399503225058,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 129.75484229409318,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 70.58649955172415,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.554405645016283,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 11894.036829,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 86.34632957626812,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 86.9786175768116,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.55778320208928,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 92.76144067272728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 64.12980609213709,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3102.415682,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 89.90942906174242,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 86.59405170724636,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 89.63885634782608,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.505887005147597,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.7871840558098058,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.9501678029083903,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 81.92724442,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.445837920470812,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 0.9470763296962016,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 95.42825799978354,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.419760045419611,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.469863245115224,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 197.09870561,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 87.19017941612321,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 96.41026693954545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 84.8534562941087,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 111.98352156735294,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 325.81614221428566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 84.38683160000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 323.2416193571429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 129.48439614375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 147.36783513571427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.038533990732196,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 5.58171277896136,
            "unit": "ns/op",
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
          "id": "03ec3f0ea3ee4858566320d3ae561445c9e62cda",
          "message": "AI readiness: add draft meta skill about creating skills",
          "timestamp": "2026-05-12T06:50:20Z",
          "url": "https://github.com/apache/groovy/commit/03ec3f0ea3ee4858566320d3ae561445c9e62cda"
        },
        "date": 1778571030719,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 64.53295352570564,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.635902314904703,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 131.94356245625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 70.74149968596059,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.551155125011803,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 11638.3567729,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 85.96373862083333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 88.54156889782607,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.314922728232556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 88.85097354347825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 65.67755964516128,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3019.4049369,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 86.62385134710146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 86.82409397010869,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 88.42779270869565,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.545638263172217,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.7875035715021512,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.9434140424384074,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 82.19851321600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.403993475432426,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 0.9450537166180553,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 95.41478191753248,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.380240243204549,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.551068469592678,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 192.924058,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 87.12321182210145,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 92.70451854545456,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 83.93383315,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 100.64221471,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 294.3932550285715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 86.27894865833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 313.02516024285717,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 125.80724009117648,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 148.59388644999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.045774916531448,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 5.547020254024898,
            "unit": "ns/op",
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
          "id": "cfa37afd58bb7fdaf5042e7ed47e2f9060688f51",
          "message": "bump dependency metadata",
          "timestamp": "2026-05-13T02:19:00Z",
          "url": "https://github.com/apache/groovy/commit/cfa37afd58bb7fdaf5042e7ed47e2f9060688f51"
        },
        "date": 1778658015252,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 66.46388558021506,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.632009304885916,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 128.75852140625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 70.92532952549261,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.538756892121029,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 11087.778505800003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 88.16043778786232,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 86.5740527875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.260345811425964,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 91.23516181976285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 64.55126097217742,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3167.9560828999993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 87.96582896394926,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 86.66919149746377,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 89.21093176086956,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.495673034090395,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.7868355982673683,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.9428137470570166,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 80.58220866399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.695331379264102,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 0.945436479846237,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 93.4406133,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.352577135319859,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.400653549570485,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 185.27818871818184,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 90.00613546699604,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 93.47533438636364,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 83.46663451500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 101.520914925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 323.7108184714285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 84.55473932083332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 318.08031867142853,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 127.95599943750001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 150.43828355,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.040416491088515,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 5.576248411597363,
            "unit": "ns/op",
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
          "id": "0a04376328dcec0a3442f3bb3b28723b52c1daf4",
          "message": "try to make JMX tests more resilient",
          "timestamp": "2026-05-13T13:20:24Z",
          "url": "https://github.com/apache/groovy/commit/0a04376328dcec0a3442f3bb3b28723b52c1daf4"
        },
        "date": 1778743942950,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 68.93579817022989,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.57879824081529,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 131.22613596875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 73.11786362010582,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.843219024403514,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 9834.047134999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 96.45229092857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 100.46381024666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 15.971817971210925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 98.11552666666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 66.82435252751945,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3083.7353686,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 96.51015799999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 97.93372847142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 97.85637530214288,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.46909338602107,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.8862809835376819,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.8907632293274244,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 85.42906994583332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 10.147315027257779,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 1.0659704098079614,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 104.099473755,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.911950186359292,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.814718863596818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 197.06846097,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 97.32857563809524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 101.932002595,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 93.93105151060607,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 112.36558774444443,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 342.7127619666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 95.00880013593073,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 336.6686356380952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 145.96251612619048,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 161.26933499230768,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.821299384820909,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 6.162779778130768,
            "unit": "ns/op",
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
          "id": "b6174a88c57fd3419d97bfeba5e4eb6938673ca5",
          "message": "GROOVY-12012: groovy-yaml: clarify when dates can retain rich types vs when handled as Strings",
          "timestamp": "2026-05-15T03:47:58Z",
          "url": "https://github.com/apache/groovy/commit/b6174a88c57fd3419d97bfeba5e4eb6938673ca5"
        },
        "date": 1778830737512,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 67.41588720638859,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.5495386472012065,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 127.18640483235295,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 72.12431156785713,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.704504537835939,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 10064.9984793,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 96.46123738571428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 96.33788376666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 15.897862348530424,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 106.46469398015874,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 67.63827352896551,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3162.0707374999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 97.18680145714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 96.56850112857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 97.44597200952383,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.474865165052746,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.8871616764904198,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.8886773406903966,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 89.75702251477273,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.991464397888706,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 1.0643420619852277,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 103.742762,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.700294133208725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.863803823288706,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 196.44791007272724,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 97.40419729380952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 101.64488402,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 92.68512390454546,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 112.37537822777779,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 326.7592519333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 95.04569068268398,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 332.642161047619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 142.8625752942857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 164.26737556153847,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.816835214439784,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 6.140182557598701,
            "unit": "ns/op",
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
          "id": "704d8652b3c5016f4b0a54c76028f6dfdb21c798",
          "message": "minor refactor: jmh summary graph",
          "timestamp": "2026-05-15T10:45:10Z",
          "url": "https://github.com/apache/groovy/commit/704d8652b3c5016f4b0a54c76028f6dfdb21c798"
        },
        "date": 1778843560582,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 64.37752448850806,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 6.026975671286938,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 126.85038388125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 70.13112162068967,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.533003570670681,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 11583.993233700001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 86.82385027916666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 88.62222278321804,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.298169304822476,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 91.5035082513834,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 64.75599688437498,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3109.1965392,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 86.78920005833332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 86.74309882065218,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 89.24720465652175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.562938063116562,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.786185689524452,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.9467070228786756,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 81.58732542800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.385439297362884,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 0.9470108993654826,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 95.12569980930736,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.37714481946689,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.264059324044018,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 191.27731703636366,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 87.17076743478262,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 92.98427290454546,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 83.29066183583333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 101.44446931,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 320.85507717142855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 84.87854499583332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 310.13463089999993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 126.81837804375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 148.38657333571425,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.041348951733846,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 5.58464657703964,
            "unit": "ns/op",
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
          "id": "e8aed4f0379ad063b027a56fab14119c54be03aa",
          "message": "GROOVY-12013: New optional type checking extension: CombinerChecker to verify associative combiners in injectParallel/sumParallel/Stream.reduce",
          "timestamp": "2026-05-16T06:19:01Z",
          "url": "https://github.com/apache/groovy/commit/e8aed4f0379ad063b027a56fab14119c54be03aa"
        },
        "date": 1778915837718,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 64.81520787459678,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.650574997100423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 128.22945259374998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 70.59550663448276,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.537729221287327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 11432.1159279,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 86.96294019275362,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 87.07112588623188,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.174910005844254,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 90.54136268458497,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 65.05632286129034,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3131.7274878999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 87.55932016231884,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 87.15046379130433,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 89.73808098695652,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.550212330592323,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.7868617824346733,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.9507851712637011,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 82.93519651433333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.442241223086926,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 0.9493344705809882,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 95.55156553484848,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.489758617683586,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.636362457212623,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 182.9664900363636,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 88.22204146956521,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 94.16881913636364,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 83.78854839649999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 104.46296627578947,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 329.81353531428573,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 92.527324405,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 329.2364200999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 127.221056525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 148.72984446428572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.036627208031447,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 5.599472441611189,
            "unit": "ns/op",
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
          "id": "7f4fb8afd4c72bb5da84bb74fc9181c607f4dce8",
          "message": "AI readiness: sandboxed reproducers",
          "timestamp": "2026-05-17T05:26:09Z",
          "url": "https://github.com/apache/groovy/commit/7f4fb8afd4c72bb5da84bb74fc9181c607f4dce8"
        },
        "date": 1779002891581,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 66.2361472188172,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.6394914524747355,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 127.68531094374998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 67.8107100919911,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.495451803269228,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 11228.873688100002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 90.28031531742425,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 86.70626932989133,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.427168198574396,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 91.70678182529643,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 64.39738151824596,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3018.5512406000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 86.86577699003622,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 87.32228131503624,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 91.1793970326087,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.632605810294166,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.7871257373930727,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.9468492425020336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 83.48112002599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.421279729757135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 0.9464906325056839,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 95.07622499047619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.461312024570233,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.37383904821328,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 188.82878973636366,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 87.12277463858696,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 101.204177095,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 84.91573611116668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 100.69761923166666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 308.52213162857146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 84.60263007500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 305.3077563428571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 126.64687966874999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 148.10510274285713,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.037241828958265,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 5.58735587703315,
            "unit": "ns/op",
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
          "id": "167034dab7ed20d43d763520a40d916cac3fe648",
          "message": "minor refactor: improved consistency with other recent method signatures",
          "timestamp": "2026-05-18T05:56:43Z",
          "url": "https://github.com/apache/groovy/commit/167034dab7ed20d43d763520a40d916cac3fe648"
        },
        "date": 1779091938535,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 66.95838388258065,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.5125029688591045,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 131.76861886416668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 71.23118855541871,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.760279798302614,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 10393.727711,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 96.79181952380954,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 96.05755562857145,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.25059517774664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 97.76199861428572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 67.32427862,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3188.4175834,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 99.73791788809524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 95.67923850064936,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 97.93496126666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.271020720576658,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.8887454807242475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.8885388336145269,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 86.62162475108697,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 10.086957212594292,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 1.0637589852139642,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 105.16752273,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.595412292902436,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.721197675753753,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 196.1343178636364,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 98.41705375119048,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 101.36203649500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 92.84004522272727,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 112.16206254444444,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 333.76106046666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 95.8718805287879,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 335.55388855238095,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 141.1162906,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 165.50537193333338,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.818665902271068,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 6.149110136390406,
            "unit": "ns/op",
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
          "id": "6f24829c9038c9539426fc998420f481cde68ec9",
          "message": "minor refactor: mention spec vs mainline tests in skills file",
          "timestamp": "2026-05-19T05:31:24Z",
          "url": "https://github.com/apache/groovy/commit/6f24829c9038c9539426fc998420f481cde68ec9"
        },
        "date": 1779177689134,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 65.67650476462366,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.6591020762504005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 126.047995975,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 69.4633418689655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.543670975144835,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 11458.706364499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 86.74970510706522,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 87.00633594909421,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.626629131582362,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 100.88034671913876,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 63.1321184,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3160.1523088999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 86.6520700588768,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 87.22413869021739,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 90.49077435217391,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.640921235144484,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.7870908605027942,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.9449947535784415,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 78.79502426538461,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.40569430513529,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 0.9470627824252368,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 93.81358340909091,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.084311284983093,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.494715400010785,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 184.58631516136364,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 89.504366479249,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 94.52653286147186,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 83.39127476583333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 101.476212,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 337.72101799999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 84.95751084166668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 322.47873614047614,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 126.60042366875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 153.9513840642857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.02892147683405,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 5.573791541943267,
            "unit": "ns/op",
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
          "id": "6f24829c9038c9539426fc998420f481cde68ec9",
          "message": "minor refactor: mention spec vs mainline tests in skills file",
          "timestamp": "2026-05-19T05:31:24Z",
          "url": "https://github.com/apache/groovy/commit/6f24829c9038c9539426fc998420f481cde68ec9"
        },
        "date": 1779263538584,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 68.30363418333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.54228224190764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 126.7234614783088,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 72.50400868928571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.6984087978344125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 10442.667514,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 96.54412051904762,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 96.27665616190475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.192093468869654,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 97.83605975238096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 67.9197365897664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3153.8811395000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 96.66739254285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 98.04267535928571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 97.40914976666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.417059342579446,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.8861068552959047,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.8894369253673503,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 83.22555006149999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 10.029010063356782,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 1.0662529030673602,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 103.8300899,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.707684170489143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.754366376180046,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 194.85350837272728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 97.7942491604762,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 101.26591976,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 93.12871497272728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 112.20671134444441,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 334.31256200952373,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 95.2534821491342,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 330.28396527619054,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 141.39247316,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 161.24076408461542,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.818456144641877,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 6.143228320009537,
            "unit": "ns/op",
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
          "id": "a2ce6f02fea7c439a9eb3feefe7d34f45e34282b",
          "message": "minor refactor: remove javadoc warning",
          "timestamp": "2026-05-20T15:04:10Z",
          "url": "https://github.com/apache/groovy/commit/a2ce6f02fea7c439a9eb3feefe7d34f45e34282b"
        },
        "date": 1779349650382,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 41.61748487052396,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 2.7496032194646594,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 77.00579047649572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 47.272176490274845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 2.3811649030452395,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 5783.5254399,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 52.833257853778676,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 52.792574486369766,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 8.161358347516162,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 54.08997761507824,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 35.139150717467615,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 1814.1579413,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 53.07957033784301,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 54.40721993995294,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 53.12818350526315,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 5.107075894247126,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.44023443969988574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.47902727796140454,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 47.93879384130675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 5.121441016882315,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 0.48401128658861153,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 60.696501738847886,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 6.551177441113348,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 6.736760686105749,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 106.57877358342105,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 53.84304927679359,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 56.02150260915916,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 50.49616394249999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 59.930936895543674,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 179.24811348787878,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 52.198605846698776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 177.87404931666669,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 76.72865969316238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 91.56002283893281,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 3.4650256432290014,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 3.522414203603385,
            "unit": "ns/op",
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
          "id": "e9b9cf1ec53ceb86f6b1e3cd88d630ab3234f3ca",
          "message": "GROOVY-12027: Align CompilerConfiguration with JDK17 minimum (fix some tests and docs)",
          "timestamp": "2026-05-22T05:22:33Z",
          "url": "https://github.com/apache/groovy/commit/e9b9cf1ec53ceb86f6b1e3cd88d630ab3234f3ca"
        },
        "date": 1779436101441,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 69.68642951724138,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 7.477788911241609,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 128.17877640625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 69.55881998965518,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 6.480870899177846,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 9898.111752,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 81.281530664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 81.333975712,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.57740909474484,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 82.394808832,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 55.39703716313814,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3064.3330021999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 81.742076456,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 81.63784769200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 85.23703488333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 11.718282453342304,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 1.1585710453121485,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 1.1578553800757994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 75.82336929697803,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 11.144365883076459,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 1.4552395213737204,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 91.25154417035573,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 13.824843267122924,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 13.337901500248233,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 184.1477428560606,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 81.656097952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 91.01077171778657,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 77.84930674615384,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 100.56885511499999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 308.8746424428572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 83.64016982813769,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 312.6942975571428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 121.77751349999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 145.99705364285717,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 4.813564198753623,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 5.005452372769986,
            "unit": "ns/op",
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
          "id": "650ff80d2ef62d55ae100c4716176d76f2da25c2",
          "message": "Add missing javadoc",
          "timestamp": "2026-05-23T07:13:01Z",
          "url": "https://github.com/apache/groovy/commit/650ff80d2ef62d55ae100c4716176d76f2da25c2"
        },
        "date": 1779521148042,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 69.68375086816091,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 7.589432842060998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 131.78030558083333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 73.98403786481482,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 6.656975921955552,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 9967.3694513,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 80.08027097061537,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 85.0633240986087,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.711023729747872,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 83.58727517083334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 55.93363791253753,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3145.0419822999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 80.392659972,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 81.8364915,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 84.51499908421015,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 11.796855971797825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 1.160256976920823,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 1.1600609338699104,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 76.41919790082417,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 11.026960228858014,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 1.543871949173915,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 90.70068159288536,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 13.995668785116333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 13.404228833202463,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 183.71168455454549,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 82.540447588,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 92.44815026482212,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 78.77840677155557,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 100.14734144071429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 306.95478064285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 83.15385243933335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 310.9555519285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 123.48760789411764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 145.69094443571427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 4.819831266952358,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 5.000494513712148,
            "unit": "ns/op",
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
            "name": "Daniel Sun",
            "username": "daniellansun",
            "email": "realbluesun@hotmail.com"
          },
          "id": "121c8605f7994fcefb4f13f21b5a4a46b95ee84a",
          "message": "GROOVY-12030: Graduate PropertyHandler from incubating to stable",
          "timestamp": "2026-05-22T23:45:44Z",
          "url": "https://github.com/apache/groovy/commit/121c8605f7994fcefb4f13f21b5a4a46b95ee84a"
        },
        "date": 1779608240863,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 65.73667220967742,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.638432656487419,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 126.86157343051471,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 69.97267792622974,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.513999871254344,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 12191.2138167,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 86.8269191905797,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 87.5125044353261,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.27756890534777,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 91.09238562252966,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 66.9227618244612,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3045.3151252999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 88.01179569619565,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 86.25552415,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 89.48519740434782,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.532943018437136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.787205290530735,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.9473885026080018,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 79.16821423461538,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.375510534294294,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 0.9443302255739692,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 93.05957629545453,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 11.991472868353096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.624709694702055,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 185.30374120757577,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 87.8919300173913,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 94.95658383116884,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 83.38033202483332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 102.24204368000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 338.6617887047619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 84.4321476375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 325.23556622857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 127.55579804999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 148.34574819285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.0350660517112376,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 5.573426595843688,
            "unit": "ns/op",
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
          "id": "42ce7e09611dd02d85d6d1e80e3c079a90100460",
          "message": "GROOVY-12038: Graduate groovy-contracts from incubating to stable",
          "timestamp": "2026-05-24T10:28:42Z",
          "url": "https://github.com/apache/groovy/commit/42ce7e09611dd02d85d6d1e80e3c079a90100460"
        },
        "date": 1779696549806,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 57.52708483021943,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 4.29840250659701,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 111.56244371111111,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 59.669668201907314,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 3.795236453778414,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 7810.5709069,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 77.49795402731054,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 75.34202491851852,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 12.431513361956327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 75.95913280982907,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 52.647349720400925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 2413.8158592,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 82.93103797127067,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 74.79549728518518,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 75.50932514814814,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 8.207709920537624,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.6871565412258063,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.6937755214078946,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 64.74607805695567,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 7.883322108576908,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 0.8248008707746054,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 80.50559724907693,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 9.910582540180794,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 10.156704553805984,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 150.91541867142854,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 75.48522407407407,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 80.80782486138462,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 74.48848746675824,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 86.47056393514492,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 257.3257725625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 74.27922979672364,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 260.8410953,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 111.10241965175437,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 126.60521936985295,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 4.506947143790219,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 4.770299303235743,
            "unit": "ns/op",
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
          "id": "42ce7e09611dd02d85d6d1e80e3c079a90100460",
          "message": "GROOVY-12038: Graduate groovy-contracts from incubating to stable",
          "timestamp": "2026-05-24T10:28:42Z",
          "url": "https://github.com/apache/groovy/commit/42ce7e09611dd02d85d6d1e80e3c079a90100460"
        },
        "date": 1779781928371,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 69.23579767850575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.539143048599648,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 127.80126590404412,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 71.44225965566957,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.743745013354081,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 10096.9029456,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 96.29026465238096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 98.53195874309525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.279023301841352,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 98.52239500761905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 70.73632129753238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3169.8652195000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 96.85039965238096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 96.00050965714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 97.61147656666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.495538839329459,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.8903228293623211,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.8913795366133638,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 87.58851414363637,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 10.12725519866365,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 1.0646186363353078,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 103.31479576500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.690644532854492,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.830612982946638,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 193.71044765454545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 97.42632415714284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 104.2512819863158,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 92.80639420909094,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 112.73735156666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 314.7403256714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 95.38624326796537,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 333.6140163666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 143.69291646619047,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 162.07377200000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.826355629102518,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 6.168636793161103,
            "unit": "ns/op",
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
          "id": "f1a4483fe4bab0f6cd9511fad0abb7016059e557",
          "message": "GROOVY-12040: restore @Builder retention to RUNTIME in 5.0.x",
          "timestamp": "2026-05-26T10:09:43Z",
          "url": "https://github.com/apache/groovy/commit/f1a4483fe4bab0f6cd9511fad0abb7016059e557"
        },
        "date": 1779868716955,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 67.03642620526881,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.61558196443291,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 127.59876011875001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 70.76982395862069,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.823568680017389,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 10082.5766198,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 97.28268803809524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 96.94420606666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 15.82143063862252,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 99.09283465809524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 67.58325059731183,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3207.0479149,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 97.69897118571427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 97.89466763642858,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 103.64190665338344,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.306787948076988,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.8883975541798061,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.8917791511963031,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 82.536499972,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.991353819479302,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 1.0665715043429314,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 104.49378492999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.78697928341712,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 13.085927863742194,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 192.26390380000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 97.90140279523808,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 102.00913414499999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 93.12627010000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 111.81876890555557,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 327.03991995952384,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 96.08023726774891,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 325.28333325714283,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 150.4648156251282,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 165.18790400128205,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.8075630197875245,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 6.174788801504264,
            "unit": "ns/op",
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
          "id": "f220aa2fe7a30fafb7ca02bb454577866174949b",
          "message": "re-enable jmh stats comparison",
          "timestamp": "2026-05-27T10:46:41Z",
          "url": "https://github.com/apache/groovy/commit/f220aa2fe7a30fafb7ca02bb454577866174949b"
        },
        "date": 1779954779520,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 71.29580069008364,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.64614271467954,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 126.47972753749998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 67.9401099703708,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.52359561448628,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 11290.4281453,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 86.85307218985508,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 86.99164089836955,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.267240234884405,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 91.33334725632412,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 64.33578566350806,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3073.5139586000005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 87.31497064565217,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 88.06475663858694,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 89.21891026086956,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.466809338674576,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.7877963081914248,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.9490621121903644,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 79.01257436538461,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.40284684202064,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 0.9468979799833726,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 95.28721035670995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.14453170385293,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.67150126777428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 183.70713852878785,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 87.19901388731884,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 93.55349667987015,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 83.52408586566668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 101.16930436499999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 309.69018047142856,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 93.8319462475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 319.5882601666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 128.65105828125002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 149.52545383571427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.031474479252888,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 5.572448952086486,
            "unit": "ns/op",
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
          "id": "f220aa2fe7a30fafb7ca02bb454577866174949b",
          "message": "re-enable jmh stats comparison",
          "timestamp": "2026-05-27T10:46:41Z",
          "url": "https://github.com/apache/groovy/commit/f220aa2fe7a30fafb7ca02bb454577866174949b"
        },
        "date": 1780041133796,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 69.2747740791954,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 7.600992635285747,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 135.35797544000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 71.46670963325124,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 6.546270528392126,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 9958.734476100002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 81.276468332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 82.9673625395,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.609814399114278,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 81.700928264,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 55.306385405480476,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3189.2085119999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 83.83580426433335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 80.57878770753847,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 84.37371456250001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 11.634691171875762,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 1.1606004811194868,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 1.1585775425318174,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 74.4708687685185,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 11.127549156548664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 1.5665978555432027,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 93.8014649031056,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 14.011650114350493,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 13.399854307731081,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 183.88488904545454,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 84.88067235234783,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 87.09375470923912,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 78.34372245384614,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 100.34784514380952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 307.39050782857146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 83.38086458000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 303.5564354714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 122.20401144117648,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 143.11201941380952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 4.859664634617347,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 4.998685017846395,
            "unit": "ns/op",
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
          "id": "f6e2248d1262e2d0cb0f9f835f1aee40f0d162b2",
          "message": "GROOVY-12036/GROOVY-12037: GDK: cache Collectors instances in StreamGroovyMethods and ParallelCollectionExtensions",
          "timestamp": "2026-05-24T00:57:15Z",
          "url": "https://github.com/apache/groovy/commit/f6e2248d1262e2d0cb0f9f835f1aee40f0d162b2"
        },
        "date": 1780126276029,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 67.38915422799036,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.567307238422792,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 127.29560444227943,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 72.71471847142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.825756315198673,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 9927.3493959,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 96.81756762380952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 96.3148116,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.26359593370705,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 98.25621248095237,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 65.53946975731182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3113.0090157000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 97.37161202380955,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 96.38457114285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 97.9120354047619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.49196792307221,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.8884826415975183,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.8892533326770329,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 82.74134763683332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.985973417982603,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 1.0645045323490314,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 104.42198955500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.806021872382791,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.988426183406329,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 193.45017123636362,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 97.1794200047619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 101.62525803000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 92.7088312090909,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 111.68599672163741,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 355.21336298333324,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 95.22891188051948,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 337.0848135071429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 140.9648369,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 161.90516449999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.810571983996107,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 6.16485892945986,
            "unit": "ns/op",
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
          "id": "7bfdeea2361c3b02488d381b7b5dd867db0787bf",
          "message": "Trivial refactor: use pattern variable instead",
          "timestamp": "2026-05-31T06:53:43Z",
          "url": "https://github.com/apache/groovy/commit/7bfdeea2361c3b02488d381b7b5dd867db0787bf"
        },
        "date": 1780213785367,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 65.71131657419355,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.67243881809617,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 130.15447086875002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 70.68142743793103,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.543443330413349,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 12542.5755685,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 86.8526230396739,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 88.86094964886362,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.45334438866413,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 89.28691963913045,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 64.15313526905243,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3119.5536380999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 87.14494320996377,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 86.68354879456521,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 96.9514874045652,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.558705262528147,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.7885560899734201,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.9438158122833687,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 78.6555753,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.415946312055251,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 0.9461731377206755,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 93.86072709090908,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.121028518710446,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.50629761178261,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 186.68985792727273,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 89.39411455652173,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 92.77326091363636,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 91.30071038940477,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 100.96514456499999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 323.42771651428563,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 84.71061869583335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 303.9626627428571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 127.33130439375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 149.5904982142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.033231238715696,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 5.582000901602806,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
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
        "date": 1780301973725,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 68.64879029356322,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.583699451251645,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 127.96631215,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 71.31949489778324,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.81279191503775,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 9885.9061724,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 97.52154576190476,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 96.33482421904765,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.04192720035301,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 98.04936774761904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 68.08584693793105,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3146.8054333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 96.9082515047619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 96.27093766666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 97.70359409523809,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.53555432174023,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.8864917598424175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.8885783415266747,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 82.9081395465,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 10.073373383384952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 1.0655661280393165,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 104.35390228000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.739818092764146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.893132918551123,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 195.31667452727274,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 97.87013223357144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 102.308193515,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 98.41393295727272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 114.32823537222222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 336.43126327619046,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 94.98118369545455,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 341.9710742166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 142.4401319504762,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 162.3984469230769,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.81710338784197,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 6.167887190378835,
            "unit": "ns/op",
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
        "date": 1780387874836,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 65.08719881604166,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.663169792353151,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 127.35081124999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 70.13342454827585,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.520002446607727,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 12127.473490200002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 89.28992927974308,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 87.51702620434783,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.499749526372575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 91.16839242391305,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 64.26043942046371,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3105.9615527,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 86.88931241105071,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 89.17463880270093,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 89.63718642608697,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.476369623175398,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.7864529836190248,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.9472054834190284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 79.34477797446154,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.42567110263655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 0.9498863403474773,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 93.53681703636363,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.211032934101807,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.289704053284598,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 182.4464133348485,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 88.67246399565218,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 93.34032890454546,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 84.53536957083334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 101.98077402499999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 346.1297265833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 84.71213081666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 324.5094768738095,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 127.25023686249999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 155.6778144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.046663191950302,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 5.60731812539691,
            "unit": "ns/op",
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
          "id": "c71d51560dc2211aea298ad2360e8a7449d1e182",
          "message": "GROOVY-12060: groovy-contracts could support @Decreases at the method level (sibling loop test)",
          "timestamp": "2026-06-03T02:29:10Z",
          "url": "https://github.com/apache/groovy/commit/c71d51560dc2211aea298ad2360e8a7449d1e182"
        },
        "date": 1780475320080,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 64.28648225967741,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.643794418237422,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 129.34772717889703,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 69.8883472862069,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.510561553301168,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 11710.095528099999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 88.62307335816865,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 86.57938989257248,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.21582794790644,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 89.38805682608697,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 64.99208272570563,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3099.5133837999992,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 88.50098156902175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 86.58480236884058,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 89.0531289,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.569789050008266,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.7874346445397243,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.9460139793680915,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 82.2296010730769,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.461064840246953,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 0.9457321829833084,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 93.26631501818181,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.17644976703143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.435855446547817,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 203.5094357609091,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 89.19187484525693,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 92.62408223181818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 83.50195109533334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 101.82655831999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 316.0432548428572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 84.98579775416667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 313.5533367,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 131.78640275874997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 155.9529640538462,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.030779515312643,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 5.585713634660253,
            "unit": "ns/op",
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
          "id": "13b009a3243716c000d3ceb7db681883d98bf76b",
          "message": "GROOVY-12059: groovy-contracts: test nested closures in postconditions\n\nThe fix already covered @Ensures (the method/constructor path in\nAnnotationClosureVisitor recomputes variable scopes for both pre- and\npostconditions), but the original commit only added nested-closure tests\nfor @Invariant and @Requires. This adds the matching @Ensures coverage:\nnested closures with an explicit parameter, the implicit 'it', and\nreferences to both the method parameter and 'result'.\n\nAssisted-by: Claude Opus 4.8 (1M context) <noreply@anthropic.com>",
          "timestamp": "2026-06-03T22:09:51Z",
          "url": "https://github.com/apache/groovy/commit/13b009a3243716c000d3ceb7db681883d98bf76b"
        },
        "date": 1780560907134,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 66.19047170580646,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.551557519179944,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 128.95633339375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 70.40777920665025,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.736345127182656,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 9979.509579500002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 96.33450985238096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 96.63038925714287,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.249630086269896,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 98.00359469047619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 70.053636915977,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3134.6044424,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 96.58318396666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 96.69406062857142,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 97.3591720142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.486974806133343,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.8882507598503144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.8943372079896352,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 84.632617088,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 10.20648510399676,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 1.066216831263802,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 104.44105034236843,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.683855706669192,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.79705467263025,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 191.02659453636366,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 98.0566428904762,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 104.67721636684212,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 92.39418032727272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 112.29727264444446,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 346.4917926,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 94.81616309545453,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 343.9284127499999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 144.09088811666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 170.17179536666663,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.810452877676656,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 6.146879569920467,
            "unit": "ns/op",
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
          "id": "13b009a3243716c000d3ceb7db681883d98bf76b",
          "message": "GROOVY-12059: groovy-contracts: test nested closures in postconditions\n\nThe fix already covered @Ensures (the method/constructor path in\nAnnotationClosureVisitor recomputes variable scopes for both pre- and\npostconditions), but the original commit only added nested-closure tests\nfor @Invariant and @Requires. This adds the matching @Ensures coverage:\nnested closures with an explicit parameter, the implicit 'it', and\nreferences to both the method parameter and 'result'.\n\nAssisted-by: Claude Opus 4.8 (1M context) <noreply@anthropic.com>",
          "timestamp": "2026-06-03T22:09:51Z",
          "url": "https://github.com/apache/groovy/commit/13b009a3243716c000d3ceb7db681883d98bf76b"
        },
        "date": 1780646464688,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 67.90708264238043,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.5495366686079795,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 131.81130907375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 72.64137539285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.70914392364695,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 10206.5564046,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 96.6058262952381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 96.44324142857144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 15.910404080864314,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 97.98425597142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 67.00511764363367,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3128.1961295,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 96.95415415238095,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 95.94387333809524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 97.69795928095238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.425695247395637,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.8862172522489355,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.8888699337186295,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 85.67797656392752,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 10.064061264824039,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 1.0636725912121856,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 103.60065897,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.752772455021788,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.677930207373983,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 195.40614025454548,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 98.36761674428571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 101.5028037,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 94.2966452,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 112.99887250555557,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 340.4930556309524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 94.91721376168832,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 340.7183453,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 141.4158188733333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 163.84374343076925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.810371202689493,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 6.1604978249754785,
            "unit": "ns/op",
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
          "id": "5499e6c359c403bad892929c34f583170d930d50",
          "message": "GROOVY-12061: Develop a threat model for Groovy",
          "timestamp": "2026-06-03T06:46:04Z",
          "url": "https://github.com/apache/groovy/commit/5499e6c359c403bad892929c34f583170d930d50"
        },
        "date": 1780731262029,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 70.23006087931034,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 7.613058326029654,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 133.42059464958334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 72.25013441083743,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 6.708664257993459,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 10178.184612699999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 80.89321844092308,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 81.750075692,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.749210704712887,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 85.36657159686956,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 56.051903752777775,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3067.1228705000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 80.41789994000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 80.854016508,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 83.93651761250001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 11.873118311940932,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 1.1613564073177367,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 1.1603699630980264,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 76.93480312649572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 11.073801958886653,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 1.53673543305404,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 93.890524410559,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 14.035669013154322,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 13.35989132168935,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 182.0226137431818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 82.53377162,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 90.92454063636364,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 77.80152662535613,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 100.411778155,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 286.4473992732143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 82.430985392,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 314.8027217714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 123.22271118235294,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 143.1403150957143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 4.825050269337622,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 5.0045682848345105,
            "unit": "ns/op",
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
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "e53f643e0a88d7cc81afc91f4e3234f2c351e149",
          "message": "Bump actions/checkout from 6 to 6.0.2 (#2593)\n\nBumps [actions/checkout](https://github.com/actions/checkout) from 6 to 6.0.2.\n- [Release notes](https://github.com/actions/checkout/releases)\n- [Changelog](https://github.com/actions/checkout/blob/main/CHANGELOG.md)\n- [Commits](https://github.com/actions/checkout/compare/v6...v6.0.2)\n\n---\nupdated-dependencies:\n- dependency-name: actions/checkout\n  dependency-version: 6.0.2\n  dependency-type: direct:production\n  update-type: version-update:semver-patch\n...\n\nSigned-off-by: dependabot[bot] <support@github.com>\nCo-authored-by: dependabot[bot] <49699333+dependabot[bot]@users.noreply.github.com>",
          "timestamp": "2026-06-07T01:55:18Z",
          "url": "https://github.com/apache/groovy/commit/e53f643e0a88d7cc81afc91f4e3234f2c351e149"
        },
        "date": 1780818872687,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 68.48679196172415,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.516783013170024,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 124.48087708235293,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 71.13074546908868,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.718389590149412,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 9890.2587925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 98.67266919404763,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 101.58818158872182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 15.874655318112115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 97.79900202380954,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 66.54290840322581,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3101.9235181,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 96.89106406666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 99.64558028833332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 97.47936502857142,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.44030758231756,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.885940865781796,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.8886009328730962,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 88.18744504454546,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.980255018195583,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 1.0645088290219964,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 103.32376275000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.607855823657323,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.90369632030072,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 191.18652616363636,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 96.69918965238097,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 101.81007369447369,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 92.28696015454544,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 115.00619897418301,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 333.52332088571427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 94.84719520000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 337.22635483333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 144.71885584571427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 160.68269052307693,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.81204433030961,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 6.150134001758303,
            "unit": "ns/op",
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
        "date": 1780906638594,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 68.61564087941788,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.5160376024144995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 128.4867578625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 72.64017489335887,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.833500101708925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 10051.360902800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 96.85910472380951,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 96.35883320952381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.104864472078606,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 98.25418222857142,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 68.03929132273637,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3160.8523591,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 99.24950236714285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 97.57405422857144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 97.28019809999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.369582053921132,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.8867711040250136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.8867930736578076,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 82.93913870866666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.965171332848792,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 1.064569718567732,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 104.05905541921052,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.648311488516082,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.888790002318922,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 197.45768336454543,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 97.05640812380952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 101.349856305,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 94.7154714417749,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 112.30170235555556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 341.0622724047619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 95.02718055909091,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 342.18177384523807,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 143.3441550642857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 167.29098064358976,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.802858548022164,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 6.162170290317453,
            "unit": "ns/op",
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
          "id": "61740ec67af12e1f9fa111aabfe538ebc11be20c",
          "message": "GROOVY-12071: groovy-contracts: ContractClosureWriter strips generics from closure parameters",
          "timestamp": "2026-06-09T07:20:26Z",
          "url": "https://github.com/apache/groovy/commit/61740ec67af12e1f9fa111aabfe538ebc11be20c"
        },
        "date": 1780991356086,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 64.43939890171372,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.659437019857149,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 129.2348385125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 68.32016047942528,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.527452754046062,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 11015.9309386,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 87.32152594583334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 87.78880901757245,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.417299296594116,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 91.7100755041502,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 64.50640568800404,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3152.3232869,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 87.11044506086957,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 86.60632102137681,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 91.49958666699605,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.593554078957398,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.7879890216180278,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.9495713593435248,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 79.3860563566154,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.48188643590883,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 0.9482350930618108,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 97.78642934285713,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 12.230800421729793,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.714788646679837,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 191.88802428181816,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 88.16408728260869,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 92.57629119545454,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 83.93377063233334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 101.01047921,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 315.3253327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 85.21513256249999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 316.8870776380952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 127.22091283125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 156.34642813296708,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.050859419615435,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 5.575416090825252,
            "unit": "ns/op",
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
          "id": "4ce518dc181ce1d5d368af2b7af71f053578044e",
          "message": "some old missing @since versions",
          "timestamp": "2026-06-10T07:51:29Z",
          "url": "https://github.com/apache/groovy/commit/4ce518dc181ce1d5d368af2b7af71f053578044e"
        },
        "date": 1781079163459,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.LoopsBench.eachIdentity",
            "value": 64.58078931694892,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.methodCallInLoop",
            "value": 5.635826025036598,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.nestedLoopsWithClosure",
            "value": 128.66832331249998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.originalEachToString",
            "value": 69.49254806206898,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.LoopsBench.reusedClosureInLoop",
            "value": 4.503987557722934,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MetaclassBench.methodCallsWithMetaclassChanges",
            "value": 11193.8782153,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkDynamicTypedCalls",
            "value": 87.00605236249999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkInterfaceMethodCalls",
            "value": 86.33786969166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithObject",
            "value": 16.129646297131252,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMethodWithParams",
            "value": 89.21280166956521,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkMonomorphicCallSite",
            "value": 62.66833069602272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkPolymorphicCallSite",
            "value": 3058.2646063,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkSimpleMethodCalls",
            "value": 86.81600107173912,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodCalls",
            "value": 86.29548701666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.MethodInvocationBench.benchmarkStaticMethodWithParams",
            "value": 89.48985667391305,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.bigDecimalArithmetic",
            "value": 10.566456144931042,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.comparisonOperators",
            "value": 0.7864222622555392,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.equalsOperator",
            "value": 0.9452212502896986,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.inOperator",
            "value": 80.92302417230769,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerMultiply",
            "value": 9.387878006862309,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.integerPlus",
            "value": 0.9444476344109622,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listGetAt",
            "value": 95.05823781558442,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listLeftShift",
            "value": 11.992265680393754,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.listPutAt",
            "value": 12.426053072862175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.mapGetAtPutAt",
            "value": 135.20333161333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.spaceshipOperator",
            "value": 87.70038332173912,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.stringMultiply",
            "value": 93.9914228112554,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.OperatorBench.unaryMinus",
            "value": 83.36142055616668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.chainedPropertyAccess",
            "value": 101.40887562,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.dynamicTypedPropertyAccess",
            "value": 343.13069756666664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.fieldReadWrite",
            "value": 86.56578418043479,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.getterSetterAccess",
            "value": 300.9112192142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapDotPropertyAccess",
            "value": 130.84694588333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.PropertyAccessBench.mapStyleAccess",
            "value": 114.73624755555556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.listIterator",
            "value": 5.04379450782446,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.RunnerRegistryBench.registryIterator",
            "value": 5.578984311134632,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          }
        ]
      }
    ]
  }
}