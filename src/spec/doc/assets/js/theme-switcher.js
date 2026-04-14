/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

(function () {
    'use strict';

    var MODES = ['system', 'light', 'dark'];
    var STORAGE_KEY = 'groovy-theme';

    var ICONS = {
        system: '\uD83D\uDCBB',
        light:  '\u2600\uFE0F',
        dark:   '\uD83C\uDF19'
    };

    var TITLES = {
        system: 'Theme: System preference',
        light:  'Theme: Light',
        dark:   'Theme: Dark'
    };

    function getStoredMode() {
        try { return localStorage.getItem(STORAGE_KEY); } catch (e) { return null; }
    }

    function storeMode(mode) {
        try {
            if (mode === 'system') { localStorage.removeItem(STORAGE_KEY); }
            else { localStorage.setItem(STORAGE_KEY, mode); }
        } catch (e) {}
    }

    function getCurrentMode() {
        var stored = getStoredMode();
        if (stored === 'light' || stored === 'dark') return stored;
        return 'system';
    }

    function applyMode(mode) {
        var html = document.documentElement;
        if (mode === 'light' || mode === 'dark') {
            html.setAttribute('data-theme', mode);
        } else {
            html.removeAttribute('data-theme');
        }
    }

    function updateButton(btn, mode) {
        var icon = btn.querySelector('.theme-icon');
        if (icon) icon.textContent = ICONS[mode];
        btn.setAttribute('title', TITLES[mode]);
        btn.setAttribute('aria-label', TITLES[mode]);
    }

    function nextMode(current) {
        var idx = MODES.indexOf(current);
        return MODES[(idx + 1) % MODES.length];
    }

    // Apply immediately to prevent flash
    applyMode(getCurrentMode());

    function init() {
        // Create the button dynamically
        var btn = document.createElement('button');
        btn.id = 'theme-switcher';
        btn.type = 'button';
        btn.setAttribute('aria-label', 'Toggle theme');
        btn.innerHTML = '<span class="theme-icon"></span>';
        document.body.appendChild(btn);

        var mode = getCurrentMode();
        updateButton(btn, mode);

        btn.addEventListener('click', function () {
            mode = nextMode(mode);
            applyMode(mode);
            storeMode(mode);
            updateButton(btn, mode);
        });

        if (window.matchMedia) {
            window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', function () {
                if (getCurrentMode() === 'system') {
                    applyMode('system');
                }
            });
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
