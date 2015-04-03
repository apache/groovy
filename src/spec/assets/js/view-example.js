
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
