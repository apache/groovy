window.BENCHMARK_DATA = {
  "lastUpdate": 1778797815067,
  "repoUrl": "https://github.com/apache/groovy",
  "entries": {
    "Compiler Performance": [
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
          "id": "0fb707bea1d9460979380ea7a716b9e40a8a058c",
          "message": "add compiler performance to dashboard",
          "timestamp": "2026-05-14T21:27:23Z",
          "url": "https://github.com/apache/groovy/commit/0fb707bea1d9460979380ea7a716b9e40a8a058c"
        },
        "date": 1778795377781,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "compile@current",
            "value": 631.67,
            "range": "±46.25",
            "unit": "ms",
            "extra": "current"
          },
          {
            "name": "compile@groovy-3",
            "value": 728.5433333333334,
            "range": "±236.85",
            "unit": "ms",
            "extra": "3.0.25"
          },
          {
            "name": "compile@groovy-4",
            "value": 660.8766666666667,
            "range": "±168.32",
            "unit": "ms",
            "extra": "4.0.32"
          },
          {
            "name": "compile@groovy-5",
            "value": 559.27,
            "range": "±51.98",
            "unit": "ms",
            "extra": "5.0.6"
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
          "id": "6b109a62a7987c2a9ab7a6c1456693f0a3f3bf58",
          "message": "GROOVY-12008: Sealed types: graduate from incubating status",
          "timestamp": "2026-05-14T12:58:56Z",
          "url": "https://github.com/apache/groovy/commit/6b109a62a7987c2a9ab7a6c1456693f0a3f3bf58"
        },
        "date": 1778797814465,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "compile@current",
            "value": 539.5699999999999,
            "range": "±23.05",
            "unit": "ms",
            "extra": "current"
          },
          {
            "name": "compile@groovy-3",
            "value": 618.5433333333333,
            "range": "±210.98",
            "unit": "ms",
            "extra": "3.0.25"
          },
          {
            "name": "compile@groovy-4",
            "value": 570.23,
            "range": "±149.77",
            "unit": "ms",
            "extra": "4.0.32"
          },
          {
            "name": "compile@groovy-5",
            "value": 476.57666666666665,
            "range": "±23.87",
            "unit": "ms",
            "extra": "5.0.6"
          }
        ]
      }
    ]
  }
}