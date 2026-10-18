$(function () {
    const token = localStorage.getItem('JWT');
    if (!token) return;
    const section = $('<section class="section-box">').append($('<h3>').text('Notifications'));
    const list = $('<ul>');
    const refresh = $('<button type="button" class="btn-action btn-ghost">').text('Refresh');
    $('main').append(section.append(refresh, list));
    function load() {
        $.ajax({ url: '/api/notification/me', headers: { Authorization: 'Bearer ' + token } }).done(function (res) {
            list.empty();
            (res.body || []).forEach(function (notification) { list.append($('<li>').text(notification.message)); });
            if (!res.body?.length) list.append($('<li>').text('No notifications yet.'));
        }).fail(function () { list.empty().append($('<li>').text('Could not load notifications. Try refreshing.')); });
    }
    refresh.on('click', load);
    load();
});
