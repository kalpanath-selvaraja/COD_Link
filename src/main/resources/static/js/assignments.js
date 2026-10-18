$(function () {
    const role = localStorage.getItem('Role');
    const headers = { Authorization: 'Bearer ' + localStorage.getItem('JWT') };
    const box = $('<section class="card">').append($('<h2>').text(role === 'COURIER' ? 'Parcel assignments' : 'Assign a courier'));
    $('main').append(box);
    function failure(xhr) { alert(xhr.responseJSON?.message || 'Request failed. Please refresh and try again.'); }
    if (role === 'ADMIN') {
        const parcel = $('<input type="number" min="1" placeholder="Parcel ID">');
        const courier = $('<input type="number" min="1" placeholder="Courier ID">');
        box.append(parcel, courier, $('<button type="button">').text('Assign courier').on('click', function () {
            if (!parcel.val() || !courier.val()) return;
            $.ajax({ url: '/api/parcels/' + Number(parcel.val()) + '/courier/' + Number(courier.val()), method: 'PATCH', headers })
                .done(function () { alert('Courier assigned.'); }).fail(failure);
        }));
        return;
    }
    if (role !== 'COURIER') { box.remove(); return; }
    const list = $('<div>');
    function load() {
        list.empty();
        ['assigned', 'available'].forEach(function (kind) {
            $.ajax({ url: '/api/parcels/' + kind, headers }).done(function (res) {
                const group = $('<div>').append($('<h3>').text(kind === 'assigned' ? 'My parcels' : 'Available in my branch'));
                (res.body || []).forEach(function (parcel) {
                    const row = $('<p>').text(parcel.trackingNumber + ' | ' + parcel.status + ' | Parcel #' + parcel.parcelId + ' ');
                    if (kind === 'available') row.append($('<button type="button">').text('Claim').on('click', function () {
                        $.ajax({ url: '/api/parcels/' + parcel.parcelId + '/claim', method: 'PATCH', headers }).done(load).fail(failure);
                    }));
                    group.append(row);
                });
                list.append(group);
            }).fail(failure);
        });
    }
    box.append($('<button type="button">').text('Refresh assignments').on('click', load), list);
    load();
});
