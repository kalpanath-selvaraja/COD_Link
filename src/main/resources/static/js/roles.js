document.addEventListener('DOMContentLoaded', function () {
    if (localStorage.getItem('Role') !== 'RECEPTIONIST') return;
    document.querySelectorAll('.sidebar a').forEach(function (link) {
        if (!link.getAttribute('href').endsWith('shipments.html')) link.closest('li')?.remove();
    });
    if (location.pathname.startsWith('/admin/') && !location.pathname.endsWith('/shipments.html')) {
        location.replace('/admin/shipments.html');
    }
});
