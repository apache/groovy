<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# Vendored dashboard dependencies

These JavaScript files are vendored verbatim (no modification) so the
performance dashboard published to the `gh-pages` branch has no
third-party CDN runtime dependency. This removes a supply-chain /
availability surface from the published page.

When bumping a version: re-download from the exact pinned URL below,
replace the file, update the version and SHA-256 here, and verify the
dashboard still renders.

| File | Project | Version | Upstream | License |
|------|---------|---------|----------|---------|
| `chart.umd.min.js` | Chart.js | 4.4.4 | https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js | MIT |
| `chartjs-adapter-date-fns.bundle.min.js` | chartjs-adapter-date-fns (bundles date-fns) | 3.0.0 | https://cdn.jsdelivr.net/npm/chartjs-adapter-date-fns@3.0.0/dist/chartjs-adapter-date-fns.bundle.min.js | MIT |

SHA-256 (as vendored):

```
b38076762f7363bc9e912b68b8e034826798db5df26bb61f000ec2e7a3137bc7  chart.umd.min.js
ea7ab30d26c38dcf1f2d26bb43e73a94537b58f1906f55e1a546dd09321b5615  chartjs-adapter-date-fns.bundle.min.js
```

Both projects (and the `date-fns` library bundled inside the adapter)
are distributed under the MIT License. Bundling MIT-licensed works in
an Apache-licensed project is permitted; the MIT license text is
reproduced below as required by its terms.

## The MIT License

```
MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

Upstream license sources:
- Chart.js: https://github.com/chartjs/Chart.js/blob/master/LICENSE.md
- chartjs-adapter-date-fns: https://github.com/chartjs/chartjs-adapter-date-fns/blob/master/LICENSE.md
- date-fns: https://github.com/date-fns/date-fns/blob/main/LICENSE.md
