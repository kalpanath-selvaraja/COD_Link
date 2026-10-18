function escapeHtml(value) {
    return $('<span>').text(value == null ? '' : String(value)).html();
}
