#!/usr/bin/env python3
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Resolve the linux-x64 OpenJDK tarball Oracle currently publishes on a
# jdk.java.net release page. Used by the ea job in groovy-build-test.yml.
#
# Usage: resolve-jdk-java-net.py PAGE_URL HTML_FILE OUTPUT_ENV
#
# Writes url= / version= / sha256_url= into OUTPUT_ENV. `version` is always
# a setup-java-safe 3-field SemVer (N.0.0-ea.B or N.0.0+B).

import html as htmlmod
import re
import sys
from html.parser import HTMLParser
from urllib.parse import unquote, urljoin, urlparse

PAGE_HOSTS = {'jdk.java.net', 'www.jdk.java.net'}
ART_HOSTS = {'download.java.net'}
FN_RE = re.compile(r'^openjdk-.+_linux-x64_bin\.tar\.gz$', re.I)
SIDE_RE = re.compile(r'\.(sha256|sig|md5|sha1)$', re.I)
EA_FN = re.compile(
    r'^openjdk-([0-9]+)(?:\.[0-9]+)*-ea\+([0-9]+)_linux-x64_bin\.tar\.gz$'
)
GA_FN = re.compile(
    r'^openjdk-([0-9]+(?:\.[0-9]+)*)_linux-x64_bin\.tar\.gz$'
)
KIND_ORDER = {'ga': 0, 'ea': 1, 'other': 2}


def fail(msg, extra=''):
    sys.stderr.write('::error::%s\n' % msg)
    if extra:
        sys.stderr.write(extra if extra.endswith('\n') else extra + '\n')
    sys.exit(1)


def host_ok(url, allowed):
    parsed = urlparse(url)
    return (
        parsed.scheme == 'https'
        and parsed.hostname in allowed
        and not parsed.username
        and not parsed.query
        and not parsed.fragment
        and '..' not in parsed.path
    )


def canonical_artifact_url(url):
    """Decode percent-escapes in the path (ea%2B14 → ea+14) and drop query."""
    parsed = urlparse(url)
    path = unquote(parsed.path)
    if parsed.hostname not in ART_HOSTS or '..' in path:
        return None
    return 'https://%s%s' % (parsed.hostname, path)


def normalize_href(value):
    return re.sub(r'\s+', '', htmlmod.unescape(value.strip()))


class BuildsHrefs(HTMLParser):
    def __init__(self):
        super().__init__()
        self.all_hrefs = []
        self.table_hrefs = []
        self.title = []
        self.h1 = []
        self._table_depth = 0
        self._in_title = False
        self._in_h1 = False

    def handle_starttag(self, tag, attrs):
        tag = tag.lower()
        attrs = {k.lower(): (v or '') for k, v in attrs}
        if tag == 'table' and 'builds' in attrs.get('class', '').split():
            self._table_depth += 1
        elif tag == 'title':
            self._in_title = True
        elif tag == 'h1':
            self._in_h1 = True
        if tag != 'a':
            return
        href = attrs.get('href')
        if not href:
            return
        href = normalize_href(href)
        self.all_hrefs.append(href)
        if self._table_depth > 0:
            self.table_hrefs.append(href)

    def handle_endtag(self, tag):
        tag = tag.lower()
        if tag == 'table' and self._table_depth:
            self._table_depth -= 1
        elif tag == 'title':
            self._in_title = False
        elif tag == 'h1':
            self._in_h1 = False

    def handle_data(self, data):
        if self._in_title:
            self.title.append(data)
        if self._in_h1:
            self.h1.append(data)


def page_major(page_url):
    match = re.match(r'^https://(?:www\.)?jdk\.java\.net/(\d+)/?$', page_url)
    return match.group(1) if match else None


def matches_major(path, name, major):
    if not major:
        return True
    return (
        name.startswith('openjdk-%s-' % major)
        or name.startswith('openjdk-%s_' % major)
        or name.startswith('openjdk-%s.' % major)
        or ('/jdk%s/' % major) in path
        or ('/jdk%s.' % major) in path
    )


def classify(url, major):
    if SIDE_RE.search(url):
        return False, 'sidecar', None
    parsed = urlparse(url)
    if parsed.scheme != 'https' or parsed.hostname not in ART_HOSTS:
        return False, 'host', None
    if parsed.username or parsed.query or parsed.fragment or '..' in parsed.path:
        return False, 'url-shape', None
    path = unquote(parsed.path)
    name = path.rsplit('/', 1)[-1]
    if not FN_RE.match(name):
        return False, 'filename', None
    if not matches_major(path, name, major):
        return False, 'wrong-major', None
    lower = path.lower()
    if '/java/ga/' in lower:
        kind = 'ga'
    elif '/early_access/' in lower or '/early-access/' in lower:
        kind = 'ea'
    else:
        kind = 'other'
    return True, 'ok', kind


def java_version(url, name):
    """Always N.0.0-ea.B or N.0.0+B — setup-java rejects four-field SemVer."""
    path = unquote(urlparse(url).path)
    match = EA_FN.match(name)
    if match:
        return '%s.0.0-ea.%s' % (match.group(1), match.group(2))
    match = GA_FN.match(name)
    if not match:
        fail('Unexpected archive name: %s' % name)
    major = match.group(1).split('.')[0]
    segs = [s for s in path.split('/') if s]
    build = None
    if len(segs) >= 2 and re.fullmatch(r'[0-9]+', segs[-2]):
        build = segs[-2]
    else:
        nums = [s for s in segs[:-1] if re.fullmatch(r'[0-9]+', s)]
        if nums:
            build = nums[-1]
    if build:
        return '%s.0.0+%s' % (major, build)
    return '%s.0.0' % major


def main(argv):
    if len(argv) != 4:
        fail('Usage: resolve-jdk-java-net.py PAGE_URL HTML_FILE OUTPUT_ENV')
    page_url, html_path, out_path = argv[1], argv[2], argv[3]
    if not host_ok(page_url, PAGE_HOSTS):
        fail('Refusing page URL: %s' % page_url)

    with open(html_path, encoding='utf-8', errors='replace') as fh:
        body = fh.read()
    parser = BuildsHrefs()
    parser.feed(body)
    parser.close()

    major = page_major(page_url)
    hrefs = parser.table_hrefs or parser.all_hrefs
    used_table = bool(parser.table_hrefs)

    cands, seen, near, rejected = [], set(), [], []
    for href in hrefs:
        absu = urljoin(page_url, href)
        path = unquote(urlparse(absu).path)
        if '_linux-x64_bin.tar.gz' in path:
            near.append(absu)
        canon = canonical_artifact_url(absu) or absu
        ok, reason, kind = classify(canon, major)
        if not ok:
            if 'openjdk-' in path or '_linux-x64' in path:
                rejected.append((reason, absu))
            continue
        if canon in seen:
            continue
        seen.add(canon)
        cands.append((kind, canon))

    if not cands:
        extra = [
            'title: %s' % ''.join(parser.title).strip(),
            'h1: %s' % ''.join(parser.h1).strip(),
            'hrefs: %d, table.builds hrefs: %d' % (
                len(parser.all_hrefs), len(parser.table_hrefs)
            ),
            'page-major: %s' % (major or '(none)'),
        ]
        extra.extend('near: %s' % u for u in near)
        extra.extend('rejected (%s): %s' % pair for pair in rejected)
        fail('No linux-x64 OpenJDK tarball found on %s' % page_url,
             '\n'.join(extra))

    cands.sort(key=lambda item: KIND_ORDER.get(item[0], 9))
    chosen = cands[0][1]
    name = unquote(urlparse(chosen).path).rsplit('/', 1)[-1]
    version = java_version(chosen, name)
    sha_url = chosen + '.sha256'

    with open(out_path, 'w', encoding='ascii') as fh:
        fh.write('url=%s\n' % chosen)
        fh.write('version=%s\n' % version)
        fh.write('sha256_url=%s\n' % sha_url)

    sys.stderr.write('Resolved %s (%s) from %s%s\n' % (
        chosen, version, page_url,
        ' [table.builds]' if used_table else ' [all hrefs]',
    ))
    for kind, url in cands:
        mark = '<-' if url == chosen else '  '
        sys.stderr.write('  %s [%s] %s\n' % (mark, kind, url))


if __name__ == '__main__':
    main(sys.argv)
