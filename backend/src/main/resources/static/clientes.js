let editId = null; let currentPage = 1; const ITEMS_PER_PAGE = 10;

function $(id) { return document.getElementById(id); }

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'})[c]);
}

function validarNIF(nif) {
    if (!nif) return false;
    nif = nif.trim().toUpperCase();

    // DNI: 8 digits + letter
    const dniRegex = /^(\d{8})([A-Z])$/;
    // NIE: letter (XYZ) + 7 digits + letter
    const nieRegex = /^([XYZ])(\d{7})([A-Z])$/;
    // CIF: letter + 7 digits + letter/number
    const cifRegex = /^([ABCDEFGHJNPQRSUVW])(\d{7})([A-Z0-9])$/;

    const letras = "TRWAGMYFPDXBNJZSQVHLCKE";
    let numero, letraCalculada;

    if (dniRegex.test(nif)) {
        const match = nif.match(dniRegex);
        numero = parseInt(match[1], 10);
        letraCalculada = letras[numero % 23];
        return match[2] === letraCalculada;
    }

    if (nieRegex.test(nif)) {
        let match = nif.match(nieRegex);
        const prefijo = match[1];
        let numeroStr = match[2];
        let letra = match[3];
        // Reemplazar X→0, Y→1, Z→2
        const mapa = { 'X': '0', 'Y': '1', 'Z': '2' };
        numero = parseInt(mapa[prefijo] + numeroStr, 10);
        letraCalculada = letras[numero % 23];
        return letra === letraCalculada;
    }

    if (cifRegex.test(nif)) {
        // Validación CIF simplificada
        return true;
    }

    return false;
}

function soloLetras(texto) {
    return /^[a-zA-ZàáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçčšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆČŠŽ'\-\. ]+$/.test(texto);
}

document.addEventListener('DOMContentLoaded', function() {
    carregarClients();
    // Validar nom i cognoms en temps real
    $('clientNom').addEventListener('input', function() {
        if (this.value && !soloLetras(this.value)) {
            this.value = this.value.replace(/[^a-zA-ZàáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçčšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆČŠŽ'\-\. ]/g, '');
        }
    });
    $('clientCognoms').addEventListener('input', function() {
        if (this.value && !soloLetras(this.value)) {
            this.value = this.value.replace(/[^a-zA-ZàáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçčšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆČŠŽ'\-\. ]/g, '');
        }
    });
});

function carregarClients(filtro) {
    const tbody = $('clientsBody');
    tbody.innerHTML = '<tr><td colspan="9" class="text-center py-4"><div class="spinner-border text-primary"></div><p class="mt-2">Carregant...</p></td></tr>';

    fetch('/api/clients')
        .then(r => r.json())
        .then(data => {
            tbody.innerHTML = '';
            if (!data || data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="9"><div class="empty-state"><i class="fas fa-users"></i><p>No hi ha clients</p></div></td></tr>';
                renderPagination(0);
                return;
            }
            const filtered = filtro ? data.filter(c =>
                (c.nif || '').toLowerCase().includes(filtro) ||
                (c.nom || '').toLowerCase().includes(filtro) ||
                (c.cognoms || '').toLowerCase().includes(filtro)
            ) : data;

            if (filtered.length === 0) {
                tbody.innerHTML = '<tr><td colspan="9"><div class="empty-state"><i class="fas fa-users"></i><p>Cap client coincideix</p></div></td></tr>';
                renderPagination(0);
                return;
            }
            const start = (currentPage - 1) * ITEMS_PER_PAGE;
            const pageItems = filtered.slice(start, start + ITEMS_PER_PAGE);
            pageItems.forEach(c => {
                const activa = c.activo !== false;
                tbody.innerHTML += '<tr>' +
                    '<td>' + escapeHtml(c.nif) + '</td>' +
                    '<td>' + escapeHtml(c.nom) + '</td>' +
                    '<td>' + escapeHtml(c.cognoms) + '</td>' +
                    '<td>' + escapeHtml(c.telefon || '-') + '</td>' +
                    '<td>' + escapeHtml(c.email || '-') + '</td>' +
                    '<td>' + escapeHtml(c.adreca || '-') + '</td>' +
                    '<td>' + escapeHtml(c.observacions || '-') + '</td>' +
                    '<td><span class="badge ' + (activa ? 'badge-lliurat' : 'badge-no-lliuurat') + '">' + (activa ? 'Actiu' : 'Inactiu') + '</span></td>' +
                    '<td>' +
                    (!(typeof ES_TRABAJADOR !== 'undefined' && ES_TRABAJADOR) ?
                    '<button class="btn-warning" onclick="editar(\'' + c.nif + '\')" title="Editar"><i class="fas fa-edit"></i></button> ' +
                    '<button class="btn-danger" onclick="eliminar(\'' + c.nif + '\')" title="Eliminar"><i class="fas fa-trash"></i></button>' : '') +
                    '</td></tr>';
            });
            renderPagination(filtered.length);
        })
        .catch(() => {
            tbody.innerHTML = '<tr><td colspan="9" class="text-center py-4 text-danger">Error al carregar clients</td></tr>';
        });
}

function filtrarClients() {
    currentPage = 1;
    const filtro = $('searchInput').value.toLowerCase().trim();
    carregarClients(filtro);
}

function mostrarFormulariNou() {
    editId = null;
    $('modalTitle').innerHTML = '<i class="fas fa-plus"></i> Nou Client';
    $('editId').value = '';
    $('clientNif').value = '';
    $('clientNif').readOnly = false;
    $('clientNif').style.background = 'white';
    $('clientNom').value = '';
    $('clientCognoms').value = '';
    $('clientTelefon').value = '';
    $('clientEmail').value = '';
    $('clientAdreca').value = '';
    $('clientObservacions').value = '';
    $('clientActivo').checked = true;
    bootstrap.Modal.getOrCreateInstance($('clientModal')).show();
}

function guardarClient() {
    const nif = $('clientNif').value.trim().toUpperCase();
    const nom = $('clientNom').value.trim();
    const cognoms = $('clientCognoms').value.trim();

    if (!nif) { mostrarErrorClient('El NIF és obligatori'); return; }
    if (!validarNIF(nif)) { mostrarErrorClient('El NIF no és vàlid. Format: 12345678Z (DNI), X1234567L (NIE) o CIF'); return; }
    if (!nom) { mostrarErrorClient('El Nom és obligatori'); return; }
    if (!soloLetras(nom)) { mostrarErrorClient('El Nom només pot contenir lletres'); return; }
    if (cognoms && !soloLetras(cognoms)) { mostrarErrorClient('Els Cognoms només poden contenir lletres'); return; }

    const telefon = $('clientTelefon').value.trim();
    if (!telefon) { mostrarErrorClient('El Telèfon és obligatori'); return; }
    if (!/^\d{9}$/.test(telefon)) { mostrarErrorClient('El Telèfon ha de tenir exactament 9 dígits'); return; }

    const btn = document.querySelector('#clientModal .btn-primary');
    const originalText = btn.textContent;
    btn.textContent = 'Guardant...';
    btn.disabled = true;

    const body = {
        nif, nom, cognoms,
        telefon: telefon,
        email: $('clientEmail').value.trim(),
        adreca: $('clientAdreca').value.trim(),
        observacions: $('clientObservacions').value.trim(),
        activo: $('clientActivo').checked
    };

    const url = editId ? '/api/clients/' + editId : '/api/clients';
    const method = editId ? 'PUT' : 'POST';

    fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
    .then(r => r.json())
    .then(resp => {
        if (resp.nif) {
            bootstrap.Modal.getInstance($('clientModal')).hide();
            carregarClients();
        } else {
            mostrarErrorClient(resp.mensaje || resp.error || 'Error desconegut');
        }
        btn.textContent = originalText;
        btn.disabled = false;
    })
    .catch(e => { mostrarErrorClient('Error de connexió: ' + e.message); btn.textContent = originalText; btn.disabled = false; });
}

function editar(nif) {
    fetch('/api/clients/' + nif)
        .then(r => r.json())
        .then(c => {
            editId = c.nif;
            $('modalTitle').innerHTML = '<i class="fas fa-edit"></i> Editar Client';
            $('editId').value = c.nif;
            $('clientNif').value = c.nif || '';
            $('clientNif').readOnly = true;
            $('clientNif').style.background = '#e9ecef';
            $('clientNom').value = c.nom || '';
            $('clientCognoms').value = c.cognoms || '';
            $('clientTelefon').value = c.telefon || '';
            $('clientEmail').value = c.email || '';
            $('clientAdreca').value = c.adreca || '';
            $('clientObservacions').value = c.observacions || '';
            $('clientActivo').checked = c.activo !== false;
            bootstrap.Modal.getOrCreateInstance($('clientModal')).show();
        })
        .catch(e => mostrarErrorClient('Error: ' + e.message));
}

let deleteClientId = null;

function eliminar(nif) {
    deleteClientId = nif;
    $('confirmDeleteClientModal').style.display = 'flex';
}

function tancarConfirmClientModal() {
    $('confirmDeleteClientModal').style.display = 'none';
    deleteClientId = null;
}

function confirmarEliminarClient() {
    if (!deleteClientId) return;
    fetch('/api/clients/' + deleteClientId, { method: 'DELETE' })
        .then(r => {
            if (r.ok) {
                tancarConfirmClientModal();
                carregarClients();
            } else {
                r.json().then(resp => {
                    tancarConfirmClientModal();
                    mostrarErrorClient(resp.mensaje || resp.error || 'Error desconegut');
                });
            }
        })
        .catch(e => { mostrarErrorClient('Error de connexió: ' + e.message); tancarConfirmClientModal(); });
}

function mostrarErrorClient(msg) {
    $('errorClientMsg').textContent = msg;
    $('errorClientModal').style.display = 'flex';
}

function tancarErrorClientModal() {
    $('errorClientModal').style.display = 'none';
}

function renderPagination(totalItems) {
    const totalPages = Math.ceil(totalItems / ITEMS_PER_PAGE);
    const table = document.querySelector('.table-wrapper, .table-container, .content-area');
    if (!table) return;
    let container = document.getElementById('pagination');
    if (!container) {
        container = document.createElement('div');
        container.id = 'pagination';
        container.className = 'pagination-container';
        table.after(container);
    }
    if (totalPages <= 1) { container.innerHTML = ''; return; }
    let html = '';
    if (currentPage > 1) html += `<button class="page-btn" onclick="changePage(${currentPage - 1})">«</button>`;
    for (let i = 1; i <= totalPages; i++) {
        if (i === currentPage) html += `<span class="page-btn active">${i}</span>`;
        else html += `<button class="page-btn" onclick="changePage(${i})">${i}</button>`;
    }
    if (currentPage < totalPages) html += `<button class="page-btn" onclick="changePage(${currentPage + 1})">»</button>`;
    container.innerHTML = html;
}

function changePage(p) {
    currentPage = p;
    carregarClients(document.getElementById('searchInput')?.value?.toLowerCase().trim());
}
