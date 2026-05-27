function $(id) { return document.getElementById(id); }

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/[&<>"']/g, function(c) {
        return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c];
    });
}

let controlsData=[];let currentPage=1;const ITEMS_PER_PAGE=10;

function showPopup(msg) {
    $('errorPopupMsg').textContent = msg;
    const modal = new bootstrap.Modal($('errorPopup'));
    modal.show();
}

document.addEventListener('DOMContentLoaded', function() {
    carregarControls();
});
window.addEventListener('pageshow', function(e) {
    if (e.persisted) carregarControls();
});

function carregarControls() {
    const tbody = $('controlsBody');
    tbody.innerHTML = '<tr><td colspan="4" class="text-center py-4"><div class="spinner-border text-primary"></div><p class="mt-2">Carregant...</p></td></tr>';

    fetch('/api/controls')
        .then(r => r.json())
        .then(data => {
            controlsData = data;
            renderControls();
        })
        .catch(() => {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center py-4 text-danger">Error al carregar controls</td></tr>';
        });
}

function renderControls() {
    const tbody = $('controlsBody');
    tbody.innerHTML = '';
    if (!controlsData || controlsData.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center py-4">No hi ha controls registrats</td></tr>';
        return;
    }
    const start=(currentPage-1)*ITEMS_PER_PAGE,end=start+ITEMS_PER_PAGE;const paginated=controlsData.slice(start,end);
    paginated.forEach(c => {
        const dataStr = c.data ? c.data.split('T')[0] || c.data : '-';
        tbody.innerHTML += '<tr>' +
            '<td>' + dataStr + '</td>' +
            '<td><strong>' + c.ph + '</strong></td>' +
            '<td>' + escapeHtml(c.usuari ? c.usuari.nombre : '-') + '</td>' +
            '<td>' + escapeHtml(c.observacions || '-') + '</td>' +
            '</tr>';
    });
    renderPagination(controlsData.length);
}

function mostrarFormulariPh() {
    $('phFormCard').style.display = 'block';
    $('phValue').value = '';
    $('phObservacions').value = '';
}

function cancelarPh() {
    $('phFormCard').style.display = 'none';
}

function guardarPh() {
    const btn = document.querySelector('#phFormCard .btn-primary');
    const originalText = btn.innerHTML;
    btn.innerHTML = 'Guardant...';
    btn.disabled = true;

    const ph = parseFloat($('phValue').value);
    if (isNaN(ph) || ph < 0 || ph > 14) {
        showPopup('El pH ha d\'estar entre 0 i 14');
        btn.innerHTML = originalText;
        btn.disabled = false;
        return;
    }

    fetch('/api/controls', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            ph: ph,
            observacions: $('phObservacions').value.trim()
        })
    })
    .then(r => r.json())
    .then(resp => {
        if (resp.success) {
            cancelarPh();
            carregarControls();
        } else {
            showPopup(resp.mensaje || resp.error || 'Error desconegut');
        }
        btn.innerHTML = originalText;
        btn.disabled = false;
    })
    .catch(e => { showPopup('Error de connexió: ' + e.message); btn.innerHTML = originalText; btn.disabled = false; });
}

// ========== PAGINACIÓ ==========
function renderPagination(totalItems) {
    const totalPages = Math.ceil(totalItems / ITEMS_PER_PAGE);
    const tableWrapper = document.querySelector('.table-wrapper') || document.querySelector('.table-container') || document.querySelector('.content-area');
    if (!tableWrapper) return;
    let container = document.getElementById('pagination');
    if (!container) {
        container = document.createElement('div');
        container.id = 'pagination';
        container.className = 'pagination-container';
        tableWrapper.after(container);
    }
    if (totalPages <= 1) { container.innerHTML = ''; return; }
    let html = '';
    if (currentPage > 1) html += '<button class="page-btn" onclick="changePage(' + (currentPage - 1) + ')">«</button>';
    for (let i = 1; i <= totalPages; i++) {
        if (i === currentPage) html += '<span class="page-btn active">' + i + '</span>';
        else html += '<button class="page-btn" onclick="changePage(' + i + ')">' + i + '</button>';
    }
    if (currentPage < totalPages) html += '<button class="page-btn" onclick="changePage(' + (currentPage + 1) + ')">»</button>';
    container.innerHTML = html;
}

function changePage(p) {
    currentPage = p;
    renderControls();
}