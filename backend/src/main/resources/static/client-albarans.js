let deleteId = null;
let productes = [];
let clients = [];
let isEditing = false;
let allAlbarans = [];
let ordreData = 'desc';let currentPage = 1;const ITEMS_PER_PAGE = 10;

const msg = (m, t) => {
    let a = document.createElement('div');
    a.className = `alert alert-${t} position-fixed top-0 end-0 m-3`;
    a.style.zIndex = '9999';
    a.innerHTML = `${m}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>`;
    a.setAttribute('role', 'alert');
    document.body.appendChild(a);
    setTimeout(() => a.remove(), 5000);
};

function $(id) { return document.getElementById(id); }

function formatDate(d) {
    if (!d) return '-';
    if (d.includes('T')) {
        const [p] = d.split('T');
        const [y, m, dd] = p.split('-');
        return dd + '/' + m + '/' + y;
    }
    if (d.includes('-')) {
        const [y, m, dd] = d.split('-');
        return dd + '/' + m + '/' + y;
    }
    return d;
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/[&<>"']/g, function(c) {
        return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c];
    });
}

document.addEventListener('DOMContentLoaded', function() {
    carregarProductes();
    carregarClients();
    carregarAlbarans();
    const dataAvui = new Date().toISOString().split('T')[0];
    $('albaraData').value = dataAvui;
});

function carregarProductes() {
    fetch('/catalogo/api/productos')
        .then(r => r.json())
        .then(data => { productes = data; })
        .catch(e => { console.error(e); msg('Error al carregar', 'danger'); });
}

function carregarClients() {
    fetch('/api/clients')
        .then(r => r.json())
        .then(data => {
            clients = data; // Esto guarda los objetos completos
            const sel = $('clientId');
            sel.innerHTML = '<option value="">Selecciona...</option>';
            clients.forEach(c => {
                if (c.activo !== false) {
                    sel.innerHTML += `<option value="${c.nif}">${c.nif} - ${c.nom} ${c.cognoms}</option>`;
                }
            });
        })
        .catch(e => { console.error(e); msg('Error al carregar clients', 'danger'); });
}

function carregarAlbarans() {
    const tbody = $('albaransBody');
    tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4"><div class="spinner-border text-primary"></div><p class="mt-2">Carregant...</p></td></tr>';

    fetch('/api/client-albarans')
        .then(r => r.json())
        .then(data => {
            allAlbarans = data || [];
            currentPage = 1;
            aplicarFiltres();
        })
        .catch(() => {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-danger">Error al carregar albarans</td></tr>';
        });
}

function renderAlbarans(data) {
    const tbody = $('albaransBody');
    tbody.innerHTML = '';
    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7"><div class="empty-state"><i class="fas fa-file-invoice"></i><p>No hi ha albarans de client</p></div></td></tr>';
        return;
    }
    const start=(currentPage-1)*ITEMS_PER_PAGE,end=start+ITEMS_PER_PAGE;const paginated=data.slice(start,end);
    paginated.forEach(a => {
        const badge = a.estat === 'LLIURAT' ? 'badge-lliurat' : 'badge-no-lliuurat';
        const numLinies = a.linies ? a.linies.length : 0;
        const dataStr = a.dataProduccio ? (a.dataProduccio.split('T')[0] || a.dataProduccio) : '-';
        const key = a.nifClient + '___' + (a.dataProduccio ? a.dataProduccio.split('T')[0] || a.dataProduccio : '');
        let btns = '<button class="btn-info" onclick="visualitzar(\'' + key + '\')" title="Visualitzar"><i class="fas fa-eye"></i></button>';
        if (a.estat === 'NO_LLIURAT') {
            btns += ' <button class="btn-warning" onclick="editar(\'' + key + '\')" title="Editar"><i class="fas fa-edit"></i></button>';
            btns += ' <button class="btn-danger" onclick="preguntarEliminar(\'' + key + '\')" title="Eliminar"><i class="fas fa-trash"></i></button>';
        }
        tbody.innerHTML += '<tr><td>' + escapeHtml(a.nifClient || '-') + '</td><td>' + escapeHtml(a.nomClient || '-') + '</td><td>' + dataStr + '</td><td>' + escapeHtml(a.operari ? a.operari.nombre : '-') + '</td><td>' + numLinies + '</td><td><span class="' + badge + '">' + a.estat + '</span></td><td style="white-space:nowrap;">' + btns + '</td></tr>';
    });
    renderPagination(data.length);
}

function buscarAlbarans() { currentPage = 1; aplicarFiltres(); }
function aplicarFiltres() {
    let data = [...allAlbarans];
    const s = ($('searchInput')?.value || '').toLowerCase().trim();
    if (s) {
        data = data.filter(a => {
            const idMatch = String(a.id).includes(s);
            const clientMatch = (a.nomClient || '').toLowerCase().includes(s);
            return idMatch || clientMatch;
        });
    }
    data.sort((a, b) => {
        const da = a.data || '';
        const db = b.data || '';
        return ordreData === 'desc' ? db.localeCompare(da) : da.localeCompare(db);
    });
    renderAlbarans(data);
}

function toggleOrdreData() {
    ordreData = ordreData === 'desc' ? 'asc' : 'desc';
    const icon = $('ordreDataIcon');
    if (icon) icon.className = 'fas fa-sort-' + (ordreData === 'desc' ? 'down' : 'up');
    aplicarFiltres();
}

function mostrarModalCrear() {
    isEditing = false;
    $('modalTitle').innerHTML = '<i class="fas fa-plus"></i> Nou Albarà Client';
    $('btnGuardar').textContent = 'Guardar Albarà';
    $('btnGuardar').className = 'btn btn-primary';
    $('editId').value = '';
    $('clientId').value = '';
    const dataAvui = new Date().toISOString().split('T')[0];
    $('albaraData').value = dataAvui;
    $('liniesContainer').innerHTML = '';
    afegirLinia();
    bootstrap.Modal.getOrCreateInstance($('modalAlbara')).show();
}

function producteJaExisteix(producteId, excludeId) {
    const liniaCards = document.querySelectorAll('#liniesContainer .linia-card');
    for (const card of liniaCards) {
        const select = card.querySelector('select');
        const cardId = parseInt(card.id.replace('linia-', ''));
        if (excludeId && cardId === excludeId) continue;
        if (parseInt(select.value) === producteId) return true;
    }
    return false;
}

function afegirLinia(producteId, quantitat) {
    const container = $('liniesContainer');
    const id = Date.now();
    let options = '<option value="">Selecciona...</option>';
    productes.forEach(p => {
        const sel = (p.id == producteId) ? 'selected' : '';
        options += '<option value="' + p.id + '" ' + sel + '>' + escapeHtml(p.nombre) + '</option>';
    });
    const q = quantitat || '';
    const card = document.createElement('div');
    card.className = 'linia-card';
    card.id = 'linia-' + id;
    card.innerHTML = '<select class="form-select" id="liniaProducte-' + id + '" required>' + options + '</select>' +
        '<input type="number" class="form-control" id="liniaQuantitat-' + id + '" placeholder="Quantitat" min="0.01" step="0.01" value="' + q + '" required>' +
        '<button type="button" class="btn btn-danger btn-sm" onclick="eliminarLinia(' + id + ')"><i class="fas fa-times"></i></button>';
    
    const select = card.querySelector('select');
    select.addEventListener('change', function() {
        const prodId = parseInt(this.value);
        if (prodId && producteJaExisteix(prodId, id)) {
            msg('Aquest producte ja existeix a l\'albarà', 'warning');
            this.value = '';
        }
    });
    
    container.appendChild(card);
}

function eliminarLinia(id) {
    const el = $('linia-' + id);
    if (el) el.remove();
}

function guardarAlbara() {
    const clientNif = $('clientId').value;
    const data = $('albaraData').value;
    const editId = $('editId').value;

    if (!clientNif) { msg('Selecciona un client', 'warning'); return; }
    if (!data) { msg('Selecciona una data', 'warning'); return; }

    // Buscar el cliente completo
    const clientSeleccionat = clients.find(c => c.nif === clientNif);
    if (!clientSeleccionat) {
        msg('Client no trobat', 'danger');
        return;
    }

    const liniaCards = document.querySelectorAll('#liniesContainer .linia-card');
    const linies = [];
    let valid = true;
    
    liniaCards.forEach(card => {
        const sel = card.querySelector('select');
        const prodId = parseInt(sel.value);
        const input = card.querySelector('input[type="number"]');
        const q = parseFloat(input.value);
        if (prodId && q > 0) {
            linies.push({ producteId: prodId, quantitat: q });
        } else if (prodId && (!q || q <= 0)) {
            valid = false;
            msg('La quantitat ha de ser superior a zero', 'warning');
        }
    });

    if (!valid) return;
    if (linies.length === 0) { msg('Afegeix almenys un producte', 'warning'); return; }
    
    const productesSet = new Set();
    for (const l of linies) {
        if (productesSet.has(l.producteId)) {
            msg('No es poden afegir productes duplicats', 'warning');
            return;
        }
        productesSet.add(l.producteId);
    }

    const body = {
        albara: {
            client: clientSeleccionat,
            dataProduccio: data
        },
        linies: linies
    };

    let url = '/api/client-albarans';
    let method = 'POST';
    
    if (editId) {
        const [urlNif, urlData] = editId.split('___');
        const cleanData = urlData ? urlData.split('T')[0] : urlData;
        url = `/api/client-albarans/${encodeURIComponent(urlNif)}/${cleanData}`;
        method = 'PUT';
    }

    console.log('Enviando petición:', JSON.stringify(body, null, 2));

    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
    .then(async response => {
        if (!response.ok) {
            const errorText = await response.text();
            console.error('Error response:', errorText);
            let errorMessage = `HTTP ${response.status}`;
            try {
                const errorJson = JSON.parse(errorText);
                errorMessage = errorJson.message || errorJson.error || errorMessage;
            } catch(e) {}
            throw new Error(errorMessage);
        }
        return response.json();
    })
    .then(resp => {
        console.log('Respuesta:', resp);
        if (resp.success || resp.albara) {
            msg(editId ? 'Albarà actualitzat correctament' : 'Albarà creat correctament', 'success');
            const m = bootstrap.Modal.getInstance($('modalAlbara'));
            if (m) m.hide();
            carregarAlbarans();
            // Limpiar formulario
            $('editId').value = '';
            $('liniesContainer').innerHTML = '';
            $('clientId').value = '';
            $('albaraData').value = new Date().toISOString().split('T')[0];
        } else {
            const errMsg = resp.error || resp.mensaje || 'Error desconegut';
            msg('Error: ' + errMsg, 'danger');
        }
    })
    .catch(e => {
        console.error('Error en la petición:', e);
        msg('Error: ' + e.message, 'danger');
    });
}

let editLineaAlbaraKey = null;

function trobarAlbaraPerClau(clau) {
    const [nif, data] = clau.split('___');
    const cleanData = data ? data.split('T')[0] || data : '';
    return allAlbarans.find(a => a.nifClient === nif && (a.dataProduccio ? a.dataProduccio.split('T')[0] || a.dataProduccio : '') === cleanData);
}

function visualitzar(clau) {
    editLineaAlbaraKey = clau;
    const [nifClient, dataProduccio] = clau.split('___');
    const cleanData = dataProduccio ? dataProduccio.split('T')[0] || dataProduccio : '';
    
    fetch(`/api/client-albarans/${encodeURIComponent(nifClient)}/${cleanData}`)
        .then(r => {
            if (!r.ok) throw new Error(`HTTP ${r.status}`);
            return r.json();
        })
        .then(a => {
const dataStr = a.dataProduccio ? a.dataProduccio.split('T')[0] : (a.data ? a.data.split('T')[0] : '-');            const esEditable = a.estat === 'NO_LLIURAT';
            let html = '<div class="row mb-3"><div class="col-md-6"><strong>Client:</strong> ' + escapeHtml(a.nomClient || '-') + '</div>' +
                '<div class="col-md-6"><strong>Data:</strong> ' + dataStr + '</div></div>' +
                '<div class="row mb-3"><div class="col-md-6"><strong>Operari:</strong> ' + escapeHtml(a.operari ? a.operari.nombre : '-') + '</div>' +
                '<div class="col-md-6"><strong>Estat:</strong> <span class="' + (a.estat === 'LLIURAT' ? 'badge-lliurat' : 'badge-no-lliuurat') + '">' + a.estat + '</span></div></div>';
            html += '<hr><h6><i class="fas fa-boxes"></i> Línies</h6><table class="table"><thead><tr><th>Producte</th><th>Quantitat</th>' +
                (esEditable ? '<th>Accions</th>' : '') + '</tr></thead><tbody id="viewLiniesBody">';
            
            if (a.linies && a.linies.length > 0) {
                const perProducte = {};
                a.linies.forEach(l => {
                    const prodId = l.producte ? l.producte.id : 0;
                    if (!perProducte[prodId]) perProducte[prodId] = { producte: l.producte, quantitat: l.quantitat };
                });
                Object.values(perProducte).forEach(g => {
                    html += '<tr>' +
                        '<td>' + escapeHtml(g.producte ? g.producte.nombre : '-') + '</td>' +
                        '<td>' + g.quantitat + '</td>' +
                        (esEditable ? '<td style="white-space:nowrap;">-</td>' : '') +
                        '</tr>';
                });
            } else {
                html += '<tr><td colspan="' + (esEditable ? 2 : 1) + '" class="text-center">Sense línies</td></tr>';
            }
            html += '</tbody></table>';
            
            const lotsAssociatsList = a.lotsAssociats || [];
            const lotsDetails = lotsAssociatsList.map(la => la.lot);
            html += '<hr><h6><i class="fas fa-layer-group"></i> Lots oberts durant el periode <span style="font-size:0.85rem;color:#888;">(' + lotsDetails.length + ')</span></h6>';
            if (lotsDetails.length > 0) {
                html += '<table class="table table-sm"><thead><tr><th>Nº Lot</th><th>Matèria Primera</th><th>Estat</th><th>Inici</th><th>Final</th></tr></thead><tbody>';
                lotsDetails.forEach(lot => {
                    const badge = lot.estat === 'OBERT' ? 'badge-obert' : (lot.estat === 'EN_ESTOC' ? 'badge-en-estoc' : 'badge-acabat');
                    html += '<tr>' +
                        '<td>' + escapeHtml(lot.numLot || lot.idLot || '-') + '</td>' +
                        '<td>' + escapeHtml(lot.catalogo ? lot.catalogo.nombre : '-') + '</td>' +
                        '<td><span class="badge-lot ' + badge + '">' + (lot.estat || '-') + '</span></td>' +
                        '<td>' + formatDate(lot.dataObertura) + '</td>' +
                        '<td>' + formatDate(lot.dataAcabament) + '</td>' +
                        '</tr>';
                });
                html += '</tbody></table>';
            } else {
                html += '<p class="text-muted">No hi ha lots oberts en aquest periode.</p>';
            }
            if (esEditable) {
                html += '<div id="editLiniaForm" style="display:none;" class="card card-body mt-2 p-3 border-warning"></div>';
                html += '<div class="text-center mt-3"><button class="btn btn-success" onclick="marcarLliurat(\'' + clau + '\')"><i class="fas fa-check-circle"></i> Marcar com a LLIURAT</button></div>';
            }
            $('detallBody').innerHTML = html;
            bootstrap.Modal.getOrCreateInstance($('modalVisualitzar')).show();
        })
        .catch(e => msg('Error: ' + e.message, 'danger'));
}

let lliuratId = null;

function marcarLliurat(clau) {
    lliuratId = clau;
    $('confirmLliuratModal').style.display = 'flex';
}

function tancarLliuratModal() {
    $('confirmLliuratModal').style.display = 'none';
    lliuratId = null;
}

function confirmarLliurat() {
    if (!lliuratId) return;
    const [nif, data] = lliuratId.split('___');
    fetch('/api/client-albarans/' + encodeURIComponent(nif) + '/' + (data.split('T')[0] || data) + '/marcar-lliurat', { method: 'PUT' })
        .then(r => r.json())
        .then(resp => {
            if (resp.success) {
                tancarLliuratModal();
                bootstrap.Modal.getInstance($('modalVisualitzar')).hide();
                carregarAlbarans();
            } else {
                msg('Error: ' + (resp.mensaje || resp.error || 'Error desconegut'));
                tancarLliuratModal();
            }
        })
        .catch(e => { msg('Error de connexió: ' + e.message); tancarLliuratModal(); });
}

function editarLiniaView(lineaId) {
    const row = $('viewLinia-' + lineaId);
    if (!row) return;
    const producteNom = row.cells[0].textContent.trim();
    const quantitat = row.cells[1].textContent.trim();

    let options = '<option value="">Selecciona...</option>';
    productes.forEach(p => {
        const sel = (p.nombre === producteNom) ? 'selected' : '';
        options += '<option value="' + p.id + '" ' + sel + '>' + escapeHtml(p.nombre) + '</option>';
    });

    row.style.display = 'none';
    const formDiv = $('editLiniaForm');
    formDiv.style.display = 'block';
    formDiv.innerHTML = '<div class="d-flex align-items-center gap-2 flex-wrap">' +
        '<label class="fw-bold me-1">Producte:</label>' +
        '<select id="editLiniaProducte" class="form-select" style="width:auto;min-width:200px;">' + options + '</select>' +
        '<label class="fw-bold me-1">Quantitat:</label>' +
        '<input type="number" id="editLiniaQuantitat" class="form-control" style="width:100px;" value="' + quantitat + '" min="0.01" step="0.01">' +
        '<button class="btn btn-success btn-sm" onclick="guardarEdicioLinia(' + lineaId + ')"><i class="fas fa-check"></i> Guardar</button>' +
        '<button class="btn btn-secondary btn-sm" onclick="cancelarEdicioLinia(' + lineaId + ')"><i class="fas fa-times"></i> Cancel·lar</button>' +
        '</div>';
}

function guardarEdicioLinia(lineaId) {
    const producteId = parseInt($('editLiniaProducte').value);
    const quantitat = parseFloat($('editLiniaQuantitat').value);
    if (!producteId) { msg('Selecciona un producte', 'warning'); return; }
    if (!quantitat || quantitat <= 0) { msg('La quantitat ha de ser superior a zero', 'warning'); return; }

    const [encNif, encData] = editLineaAlbaraKey.split('___');
    const cleanData = encData ? encData.split('T')[0] || encData : '';
    fetch('/api/client-albarans/' + encodeURIComponent(encNif) + '/' + cleanData + '/linies/' + lineaId, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ producteId: producteId, quantitat: quantitat })
    })
    .then(r => r.json())
    .then(resp => {
        if (resp.success) {
            visualitzar(editLineaAlbaraKey);
        } else {
            msg('Error: ' + (resp.mensaje || resp.error || 'Error desconegut'));
        }
    })
    .catch(e => msg('Error de connexió: ' + e.message));
}

function cancelarEdicioLinia(lineaId) {
    $('editLiniaForm').style.display = 'none';
    const row = $('viewLinia-' + lineaId);
    if (row) row.style.display = '';
}

function eliminarLiniaView(lineaId) {
    if (!confirm('Eliminar aquesta línia?')) return;
    const [encNif, encData] = editLineaAlbaraKey.split('___');
    const cleanData = encData ? encData.split('T')[0] || encData : '';
    fetch('/api/client-albarans/' + encodeURIComponent(encNif) + '/' + cleanData + '/linies/' + lineaId, { method: 'DELETE' })
        .then(r => r.json())
        .then(resp => {
            if (resp.success) {
                visualitzar(editLineaAlbaraKey);
            } else {
                msg('Error: ' + (resp.mensaje || resp.error || 'Error desconegut'));
            }
        })
        .catch(e => msg('Error de connexió: ' + e.message));
}

function editar(clau) {
    const [editNif, editData] = clau.split('___');
    const cleanData = editData ? editData.split('T')[0] || editData : '';
    
    fetch(`/api/client-albarans/${encodeURIComponent(editNif)}/${cleanData}`)
        .then(r => {
            if (!r.ok) throw new Error(`HTTP ${r.status}`);
            return r.json();
        })
        .then(a => {
            isEditing = true;
            $('modalTitle').innerHTML = '<i class="fas fa-edit"></i> Editar Albarà Client';
            $('btnGuardar').textContent = 'Actualitzar Albarà';
            $('btnGuardar').className = 'btn btn-warning';
            
            // Guardar la clave original para la URL
            $('editId').value = `${a.nifClient}___${a.dataProduccio}`;
            
            // Seleccionar el cliente
            $('clientId').value = a.nifClient;
            
            // Establecer la fecha
            const dataValue = a.dataProduccio ? a.dataProduccio.split('T')[0] : (a.data ? a.data.split('T')[0] : '');
            $('albaraData').value = dataValue;
            
            // Cargar líneas
            $('liniesContainer').innerHTML = '';
            if (a.linies && a.linies.length > 0) {
                // Agrupar líneas por producto (evitar duplicados en la UI)
                const uniqueProducts = new Map();
                a.linies.forEach(l => {
                    if (l.producte && !uniqueProducts.has(l.producte.id)) {
                        uniqueProducts.set(l.producte.id, {
                            id: l.producte.id,
                            quantitat: l.quantitat
                        });
                    }
                });
                uniqueProducts.forEach((prod, prodId) => {
                    afegirLinia(prodId, prod.quantitat);
                });
            } else {
                afegirLinia();
            }
            
            bootstrap.Modal.getOrCreateInstance($('modalAlbara')).show();
        })
        .catch(e => {
            console.error('Error cargando albarán:', e);
            msg('Error al carregar l\'albarà: ' + e.message, 'danger');
        });
}

function preguntarEliminar(clau) {
    deleteId = clau;
    $('confirmDeleteMsg').textContent = 'Esborraràs l\'albarà de client. Aquesta acció no es pot desfer.';
    $('confirmDeleteModal').style.display = 'flex';
}

function tancarConfirmModal() {
    $('confirmDeleteModal').style.display = 'none';
    deleteId = null;
}

function confirmarEliminar() {
    if (!deleteId) return;
    const [delNif, delData] = deleteId.split('___');
    fetch('/api/client-albarans/' + encodeURIComponent(delNif) + '/' + (delData.split('T')[0] || delData), { method: 'DELETE' })
        .then(r => r.json())
        .then(resp => {
            if (resp.success) {
                carregarAlbarans();
            } else {
                msg('Error: ' + (resp.error || resp.mensaje || 'Error desconegut'));
            }
            tancarConfirmModal();
        })
        .catch(e => { msg('Error de connexió: ' + e.message); tancarConfirmModal(); });
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
    aplicarFiltres();
}
