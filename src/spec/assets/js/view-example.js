/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function toggle_result_block(e) {
        this.prev().toggleClass('stacked');
        this.toggle();
        return false;
        }

function insert_result_links() {
        $('.result').each(function (idx, node) {
            znode = $(node);
            p = $(znode.parent().children()[0]);
            p.append(' <a class="view-result" href="#">[ view example ]</a>');
            view_result_link = p.children().last();
            view_result_link.on('click', $.proxy(toggle_result_block, znode));
        });
        }

$('document').ready(insert_result_links);
