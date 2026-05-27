console.log('=== INICIO ===');

let albaransData = [], proveedoresList = [], usuariosList = [], catalogoList = [];
let albaranAEliminar = null, lotsCounter = 0, editLotsCounter = 0, ocrDatosAnalizados = null;let currentPage = 1;const ITEMS_PER_PAGE = 10;

const $ = id => document.getElementById(id);
const unitatsSelect = (sel) => '<option value="kg" '+(sel==='kg'?'selected':'')+'>kg</option><option value="g" '+(sel==='g'?'selected':'')+'>g</option><option value="L" '+(sel==='L'?'selected':'')+'>L</option><option value="TONELADAS" '+(sel==='TONELADAS'?'selected':'')+'>TONELADAS</option><option value="SACOS" '+(sel==='SACOS'?'selected':'')+'>SACOS</option><option value="UDS" '+(sel==='UDS'?'selected':'')+'>UDS</option>';
const validarCIF = (doc) => {
    if (!doc || doc.length !== 9) return false;
    doc = doc.toUpperCase();
    if (/^\d{8}[A-Z]$/.test(doc)) {
        const letras = "TRWAGMYFPDXBNJZSQVHLCKE";
        return letras.charAt(parseInt(doc.substring(0, 8)) % 23) === doc.charAt(8);
    }
    if (/^[XYZ]\d{7}[A-Z]$/.test(doc)) {
        const nif = (doc[0] === 'X' ? '0' : doc[0] === 'Y' ? '1' : '2') + doc.substring(1, 8);
        const letras = "TRWAGMYFPDXBNJZSQVHLCKE";
        return letras.charAt(parseInt(nif) % 23) === doc.charAt(8);
    }
    if (/^[ABCDEFGHJNPQRSUVW]\d{7}[0-9A-J]$/.test(doc)) {
        const digits = doc.substring(1, 8);
        let sum = 0;
        for (let i = 0; i < 7; i++) {
            const d = parseInt(digits[i]);
            // 🔧 CORREGIDO: pares multiplican, impares suman
            if (i % 2 === 0) {  // Posiciones pares (0,2,4,6) - multiplicar
                const x2 = d * 2;
                sum += x2 > 9 ? x2 - 9 : x2;
            } else {            // Posiciones impares (1,3,5) - sumar directo
                sum += d;
            }
        }
        const control = (10 - (sum % 10)) % 10;
        return "ABCDEFGHJUV".indexOf(doc[0]) >= 0
            ? parseInt(doc[8]) === control
            : doc[8] === "TRWAGMYFPDXBNJZSQVHLCKE"[control];
    }
    return false;
};
const msg = (m, t) => {
    let a = document.createElement('div');
    a.className = 'alert alert-' + t + ' position-fixed top-0 end-0 m-3';
    a.style.zIndex = '9999';
    a.innerHTML = m + '<button type="button" class="btn-close" data-bs-dismiss="alert"></button>';
    document.body.appendChild(a);
    setTimeout(() => a.remove(), 3000);
};
const escapeHtml = t => {
    if (!t)
        return '';
    const d = document.createElement('div');
    d.textContent = t;
    return d.innerHTML;
};

function formatDate(d) {
    if (!d)
        return '-';
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
function formatDateForInput(d) {
    if (!d)
        return '';
    if (d.includes('T'))
        return d.split('T')[0];
    if (d.includes('-'))
        return d;
    return '';
}

// ========== CARGAR DATOS ==========
async function cargarProveedores() {
    const r = await fetch('/proveedores/api/listar');
    proveedoresList = await r.json();
    ['proveedorId', 'editProveedorId'].forEach(id => {
        const s = $(id);
        if (s) {
            s.innerHTML = '<option value="">Selecciona...</option>';
            proveedoresList.forEach(p => s.innerHTML += '<option value="' + p.cif + '">' + p.nombre + '</option>');
        }
    });
}
async function cargarUsuarios() {
    const r = await fetch('/usuarios/api/listar');
    usuariosList = await r.json();
    const s = $('usuarioId');
    if (s) {
        s.innerHTML = '<option value="">Selecciona...</option>';
        usuariosList.forEach(u => s.innerHTML += '<option value="' + u.id + '">' + u.nombre + '</option>');
        try {
            const res = await fetch('/usuarios/api/actual');
            const user = await res.json();
            if (user && user.id) {
                s.value = user.id;
                if ($('editUsuarioId')) $('editUsuarioId').value = user.id;
            }
        } catch(e) {}
    }
}
async function cargarCatalogo() {
    const r = await fetch('/catalogo/api/listar');
    catalogoList = await r.json();
    console.log('Catálogo cargado:', catalogoList.length);
}
async function cargarAlbarans() {
    const r = await fetch('/api/albarans');
    albaransData = await r.json();
    filtrarPerEstat();
}
async function cargarDades() {
    await cargarProveedores();
    await cargarUsuarios();
    await cargarCatalogo();
    await cargarAlbarans();
}

// ========== MOSTRAR TABLA ==========
function tieneLotsActius(a) {
    return a.lots && a.lots.some(l => l.estat === 'OBERT' || l.estat === 'ACABAT');
}
function clauAlbara(a) { return (a.proveedor?.cif || a.nifProveidor) + '___' + a.numAlbara; }
function trobarAlbaraPerClau(clau) {
    const [nif, num] = clau.split('___');
    return albaransData.find(a => (a.proveedor?.cif || a.nifProveidor) === nif && a.numAlbara === num);
}

function mostrarAlbarans(albs) {
    const t = $('tablaAlbarans');
    if (!t)
        return;
    if (!albs || !albs.length) {
        t.innerHTML = '<tr><td colspan="7">No hay albaranes</td></tr>';
        renderPagination(0);
        return;
    }
    let h = '';
    const start=(currentPage-1)*ITEMS_PER_PAGE,end=start+ITEMS_PER_PAGE;const paginated=albs.slice(start,end);
    if(!paginated.length){currentPage=1;filtrarPerEstat();return;}
    paginated.forEach(a => {
        const actiu = tieneLotsActius(a);
        const key = clauAlbara(a);
        let btns = '';
        if (actiu) {
            btns = '<button class="btn-info" onclick="mostrarDetall(\'' + key + '\')" title="Visualitzar"><i class="fas fa-eye"></i> ' + ($('i18n-visualitzar')?.textContent || 'Visualitzar') + '</button>';
        } else {
            btns = '<button class="btn-warning" onclick="editarAlbaran(\'' + key + '\')" title="Editar"><i class="fas fa-edit"></i></button><button class="btn-danger" onclick="eliminarAlbaran(\'' + key + '\')" title="Eliminar"><i class="fas fa-trash"></i></button>';
        }
        h += '<tr><td>' + escapeHtml(a.numAlbara) + '</td><td><strong>' + escapeHtml(a.numAlbara) + '</strong></td><td>' + formatDate(a.dataRecepcio) + '</td><td>' + escapeHtml((a.proveedor || {nombre: '-'}).nombre) + '</td><td>' + escapeHtml((a.usuario || {nombre: '-'}).nombre) + '</td><td>' + (a.lots ? a.lots.length : 0) + ' lots</td><td style="white-space:nowrap;text-align:center;">' + btns + '</td></tr>';
    });
    t.innerHTML = h;renderPagination(albs.length);
}

// ========== VISUALITZAR DETALL ==========
function mostrarDetall(clau) {
    const a = trobarAlbaraPerClau(clau);
    if (!a) return;
    $('detallNumAlbara').textContent = a.numAlbara || '-';
    $('detallData').textContent = formatDate(a.dataRecepcio);
    $('detallProveedor').textContent = (a.proveedor || {}).nombre || a.proveedorNombre || '-';
    $('detallUsuario').textContent = (a.usuario || {}).nombre || a.operarioNombre || '-';
    $('detallObservacions').textContent = a.observacions || '-';
    const tl = $('detallLots');
    if (a.lots && a.lots.length) {
        let lh = '';
        a.lots.forEach(l => {
            let badge = 'badge-en-estoc';
            if (l.estat === 'OBERT') badge = 'badge-obert';
            else if (l.estat === 'ACABAT') badge = 'badge-acabat';
            else if (l.estat === 'CONSUMIT') badge = 'badge-consumit';
            else if (l.estat === 'CADUCAT') badge = 'badge-caducat';
            else if (l.estat === 'REBUTJAT') badge = 'badge-rebutjat';
            lh += '<tr><td>' + escapeHtml(l.numLot) + '</td><td>' + escapeHtml((l.catalogo || {}).nombre || '-') + '</td><td>' + (l.quantitat || 0) + ' ' + (l.unitat || 'kg') + '</td><td>' + (l.dataCaducitat ? formatDate(l.dataCaducitat) : '-') + '</td><td><span class="badge-lot ' + badge + '">' + l.estat + '</span></td></tr>';
        });
        tl.innerHTML = lh;
    } else {
        tl.innerHTML = '<tr><td colspan="4" class="text-center">Sense lots</td></tr>';
    }
    new bootstrap.Modal($('modalVisualitzarAlbaran')).show();
}
function tancarDetall() {
    const m = bootstrap.Modal.getInstance($('modalVisualitzarAlbaran'));
    if (m) m.hide();
}

let ordreData = 'desc'; // 'asc' o 'desc'

function formatDateSearch(d) {
    if (!d) return '';
    if (d.includes('T')) return d.split('T')[0];
    if (d.includes('-')) return d;
    return d;
}

function buscarAlbarans() { currentPage = 1; filtrarPerEstat(); }
function filtrarPerEstat() {
    let f = [...albaransData];
    const s = ($('searchInput')?.value || '').toLowerCase().trim();
    if (s) {
        f = f.filter(a => {
            const fechaStr = formatDate(a.dataRecepcio).toLowerCase();
            return a.numAlbara.toLowerCase().includes(s)
                || (a.proveedor?.nombre || '').toLowerCase().includes(s)
                || (a.proveedorNombre || '').toLowerCase().includes(s)
                || fechaStr.includes(s);
        });
    }
    // Ordenar per data
    f.sort((a, b) => {
        const da = a.dataRecepcio ? new Date(a.dataRecepcio).getTime() : 0;
        const db = b.dataRecepcio ? new Date(b.dataRecepcio).getTime() : 0;
        return ordreData === 'desc' ? db - da : da - db;
    });
    mostrarAlbarans(f);
}

function toggleOrdreData() {
    ordreData = ordreData === 'desc' ? 'asc' : 'desc';
    const icon = $('ordreDataIcon');
    if (icon) icon.className = 'fas fa-sort-' + (ordreData === 'desc' ? 'down' : 'up');
    filtrarPerEstat();
}

// ========== FORMULARIO NUEVO ==========
function mostrarFormulariNou() {
    lotsCounter = 0;
    $('lotsContainer').innerHTML = '';
    afegirLot();
    const s = $('usuarioId');
    if (s && s.options.length > 0 && !s.value) {
        const firstOpt = s.options[0];
        if (firstOpt.value === '') s.remove(0);
    }
    new bootstrap.Modal($('modalCrearAlbaran')).show();
}

function generarOpcionsMateries() {
    let opts = '<option value="">Selecciona matèria...</option>';
    if (catalogoList && catalogoList.length) {
        catalogoList.filter(c => c.tipo === 'INGREDIENTE').forEach(c => {
            opts += '<option value="' + c.id + '">' + escapeHtml(c.nombre) + '</option>';
        });
    }
    return opts;
}

function afegirLot() {
    const c = $('lotsContainer');
    if (!c) return;
    const id = lotsCounter++;
    c.insertAdjacentHTML('beforeend', '<div class="lot-item p-3 mb-2 border rounded" id="lot-' + id + '"><div class="row g-2 align-items-center">' +
        '<div class="col-md-2"><input class="form-control" id="lotNum-' + id + '" placeholder="Nº Lot" required></div>' +
        '<div class="col-md-3"><select class="form-select lot-producte" id="lotProducte-' + id + '" required>' + generarOpcionsMateries() + '</select></div>' +
        '<div class="col-md-2"><input type="number" class="form-control" id="lotQuantitat-' + id + '" placeholder="Quantitat" required></div>' +
        '<div class="col-md-2"><select class="form-select" id="lotUnitat-' + id + '">' + unitatsSelect('kg') + '</select></div>' +
        '<div class="col-md-2"><input type="date" class="form-control" id="lotCaducitat-' + id + '" placeholder="Caducitat"></div>' +
        '<div class="col-md-1"><button class="btn btn-outline-danger btn-sm" onclick="$(\'lot-' + id + '\').remove()"><i class="fas fa-times"></i></button></div>' +
        '</div></div>');
}
function recollirLots() {
    const lots = [];
    document.querySelectorAll('#lotsContainer .lot-item').forEach(item => {
        const id = item.id.split('-')[1];
        const n = $('lotNum-' + id)?.value;
        const matSelect = $('lotProducte-' + id);
        const matId = parseInt(matSelect?.value);
        const q = $('lotQuantitat-' + id)?.value;
        const u = $('lotUnitat-' + id)?.value || 'kg';
        const c = $('lotCaducitat-' + id)?.value || '';
        if (n && matId && q) {
            const lot = {numLot: n, quantitat: parseInt(q), unitat: u};
            if (c) lot.dataCaducitat = c;
            lot.materiaPrimeraId = matId;
            lots.push(lot);
        }
    });
    return lots;
}
async function guardarAlbaran() {
    const provSelect = $('proveedorId');
    const provName = provSelect?.selectedOptions?.[0]?.text || '';
    const d = {numAlbara: $('numAlbara')?.value, proveedorCif: provSelect?.value, proveedorNombre: provName, dataRecepcio: $('dataRecepcio')?.value || null, observacions: $('observacions')?.value || '', lots: recollirLots()};
    if (!d.numAlbara || !d.proveedorCif) {
        msg('Completa los campos', 'warning');
        return;
    }
    if (!validarCIF(d.proveedorCif)) {
        msg('CIF del proveïdor no vàlid', 'warning');
        return;
    }
    const r = await fetch('/api/albarans/with-lots', {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(d)});
    if (r.ok) {
        msg('Creat correctament', 'success');
        bootstrap.Modal.getInstance($('modalCrearAlbaran')).hide();
        $('formCrearAlbaran').reset();
        $('lotsContainer').innerHTML = '';
        lotsCounter = 0;
        cargarAlbarans();
    } else {
        const e = await r.json();
        msg(e.error || 'Error', 'danger');
    }
}

// ========== EDITAR ==========
async function editarAlbaran(clau) {
    const a = trobarAlbaraPerClau(clau);
    if (!a)
        return;
    if (a.lots && a.lots.length) {
        const act = a.lots.filter(l => l.estat === 'OBERT' || l.estat === 'ACABAT');
        if (act.length) {
            msg('No editable: lots iniciats', 'warning');
            return;
        }
    }
    editLotsCounter = 0;
    const ps = $('editProveedorId'), us = $('editUsuarioId');
    if (ps) {
        ps.innerHTML = '<option value="">Selecciona...</option>';
        proveedoresList.forEach(p => ps.innerHTML += '<option value="' + p.cif + '" ' + (p.cif === (a.proveedor?.cif || a.proveedorCif) ? 'selected' : '') + '>' + p.nombre + '</option>');
    }
    if (us) {
        us.innerHTML = '<option value="">Selecciona...</option>';
        usuariosList.forEach(u => us.innerHTML += '<option value="' + u.id + '" ' + (u.id === a.usuario?.id ? 'selected' : '') + '>' + u.nombre + '</option>');
        us.disabled = true;
    }
    $('editId').value = (a.proveedor?.cif || a.nifProveidor) + '/' + a.numAlbara;
    $('editNumAlbara').value = a.numAlbara;
    $('editObservacions').value = a.observacions || '';
    $('editDataRecepcio').value = formatDateForInput(a.dataRecepcio);
    $('editLotsContainer').innerHTML = '';
    if (a.lots && a.lots.length)
        a.lots.forEach(l => afegirLotEditarConDatos(l));
    else
        afegirLotEditar();
    new bootstrap.Modal($('modalEditarAlbaran')).show();
}
function afegirLotEditar() {
    const c = $('editLotsContainer');
    if (!c) return;
    const id = editLotsCounter++;
    c.insertAdjacentHTML('beforeend', '<div class="lot-item p-3 mb-2 border rounded" id="editLot-' + id + '"><div class="row g-2 align-items-center">' +
        '<div class="col-md-2"><input class="form-control" id="editLotNum-' + id + '" placeholder="Nº Lot" required></div>' +
        '<div class="col-md-3"><select class="form-select edit-lot-producte" id="editLotProducte-' + id + '" required>' + generarOpcionsMateries() + '</select></div>' +
        '<div class="col-md-2"><input type="number" class="form-control" id="editLotQuantitat-' + id + '" placeholder="Quantitat" required></div>' +
        '<div class="col-md-2"><select class="form-select" id="editLotUnitat-' + id + '">' + unitatsSelect('kg') + '</select></div>' +
        '<div class="col-md-2"><input type="date" class="form-control" id="editLotCaducitat-' + id + '" placeholder="Caducitat"></div>' +
        '<div class="col-md-1"><button class="btn btn-outline-danger btn-sm" onclick="$(\'editLot-' + id + '\').remove()"><i class="fas fa-times"></i></button></div>' +
        '</div></div>');
}
function afegirLotEditarConDatos(lot) {
    const c = $('editLotsContainer');
    if (!c) return;
    const id = editLotsCounter++;
    const catId = lot.catalogo?.id || '';
    const prodName = (lot.catalogo?.nombre || lot.descripcion || '');
    const unit = lot.unitat || 'kg';
    const cad = lot.dataCaducitat ? lot.dataCaducitat.split('T')[0] : '';
    let options = '<option value="">Selecciona matèria...</option>';
    if (catalogoList && catalogoList.length) {
        catalogoList.filter(m => m.tipo === 'INGREDIENTE').forEach(m => {
            const sel = (m.id == catId) ? 'selected' : '';
            options += '<option value="' + m.id + '" ' + sel + '>' + escapeHtml(m.nombre) + '</option>';
        });
    }
    c.insertAdjacentHTML('beforeend', '<div class="lot-item p-3 mb-2 border rounded" id="editLot-' + id + '"><div class="row g-2 align-items-center">' +
        '<div class="col-md-2"><input class="form-control" id="editLotNum-' + id + '" value="' + escapeHtml(lot.numLot) + '" required></div>' +
        '<div class="col-md-3"><select class="form-select edit-lot-producte" id="editLotProducte-' + id + '" required>' + options + '</select></div>' +
        '<div class="col-md-2"><input type="number" class="form-control" id="editLotQuantitat-' + id + '" value="' + lot.quantitat + '" required></div>' +
        '<div class="col-md-2"><select class="form-select" id="editLotUnitat-' + id + '">' + unitatsSelect(unit) + '</select></div>' +
        '<div class="col-md-2"><input type="date" class="form-control" id="editLotCaducitat-' + id + '" placeholder="Caducitat" value="' + cad + '"></div>' +
        '<div class="col-md-1"><button class="btn btn-outline-danger btn-sm" onclick="$(\'editLot-' + id + '\').remove()"><i class="fas fa-times"></i></button></div>' +
        '</div></div>');
}
function recollirLotsEditar() {
    const lots = [];
    document.querySelectorAll('#editLotsContainer .lot-item').forEach(item => {
        const id = item.id.split('-')[1];
        const n = $('editLotNum-' + id)?.value;
        const matSelect = $('editLotProducte-' + id);
        const matId = parseInt(matSelect?.value);
        const q = $('editLotQuantitat-' + id)?.value;
        const u = $('editLotUnitat-' + id)?.value || 'kg';
        const c = $('editLotCaducitat-' + id)?.value || '';
        if (n && matId && q) {
            const lot = {numLot: n, quantitat: parseInt(q), unitat: u};
            if (c) lot.dataCaducitat = c;
            lot.materiaPrimeraId = matId;
            lots.push(lot);
        }
    });
    return lots;
}
async function actualizarAlbaran() {
    const d = {numAlbara: $('editNumAlbara').value, proveedorCif: $('editProveedorId').value, usuarioId: parseInt($('editUsuarioId').value), observacions: $('editObservacions').value || '', dataRecepcio: $('editDataRecepcio').value || null, lots: recollirLotsEditar()};
    if (!d.numAlbara || !d.proveedorCif || !d.usuarioId) {
        msg('Completa los campos', 'warning');
        return;
    }
    if (!validarCIF(d.proveedorCif)) {
        msg('CIF del proveïdor no vàlid', 'warning');
        return;
    }
    const r = await fetch('/api/albarans/' + encodeURIComponent(d.proveedorCif) + '?idAlbarra=' + encodeURIComponent(d.numAlbara), {method: 'PUT', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(d)});
    if (r.ok) {
        msg('Actualizado', 'success');
        bootstrap.Modal.getInstance($('modalEditarAlbaran')).hide();
        cargarAlbarans();
    } else {
        const e = await r.json();
        msg(e.error || 'Error', 'danger');
    }
}

// ========== ELIMINAR ==========
function eliminarAlbaran(clau) {
    const a = trobarAlbaraPerClau(clau);
    if (!a) {
        msg('No encontrado', 'danger');
        return;
    }
    if (a.lots && a.lots.length) {
        msg('No es pot eliminar l\'albarà perquè té lots associats', 'warning');
        return;
    }
    albaranAEliminar = (a.proveedor?.cif || a.nifProveidor) + '/' + a.numAlbara;
    document.getElementById('confirmDeleteModal').style.display = 'flex';
}
async function confirmarEliminar() {
    const parts = albaranAEliminar.split('/');
    const cif = parts[0];
    const idAlbara = parts.slice(1).join('/');
    const r = await fetch('/api/albarans/' + encodeURIComponent(cif) + '?idAlbarra=' + encodeURIComponent(idAlbara), {method: 'DELETE'});
    if (r.ok) {
        msg('Eliminado', 'success');
        cargarAlbarans();
    } else {
        const e = await r.json();
        msg(e.error || 'Error al eliminar', 'danger');
    }
    document.getElementById('confirmDeleteModal').style.display = 'none';
    albaranAEliminar = null;
}

// ========== OCR WEB ==========
function mostrarModalOCR() {
    resetOCR();
    new bootstrap.Modal($('modalOCR')).show();
}

function previsualizarOCR() {
    const f = $('ocrImagen').files[0];
    if (f) {
        const r = new FileReader();
        r.onload = e => {
            $('ocrPreview').src = e.target.result;
            $('ocrPreview').style.display = 'block';
        };
        r.readAsDataURL(f);
        $('btnAnalizar').disabled = false;
    } else {
        $('ocrPreview').style.display = 'none';
        $('btnAnalizar').disabled = true;
    }
}

async function analizarOCR() {
    const f = $('ocrImagen').files[0];
    if (!f)
        return;
    $('ocrLoading').style.display = 'block';
    $('btnAnalizar').disabled = true;
    const fd = new FormData();
    fd.append('imagen', f);
    try {
        const r = await fetch('/api/ocr/analizar', {method: 'POST', body: fd});
        const d = await r.json();
        if (d.success && d.datos) {
            ocrDatosAnalizados = d.datos;
            mostrarResultadoOCR(d.datos);
        } else {
            msg(d.mensaje || 'Error', 'danger');
            $('ocrLoading').style.display = 'none';
            $('btnAnalizar').disabled = false;
        }
    } catch (e) {
        msg('Error: ' + e.message, 'danger');
        $('ocrLoading').style.display = 'none';
        $('btnAnalizar').disabled = false;
    }
}

function cercarProducte(input) {
    const nom = (input.value || '').toLowerCase().trim();
    if (!nom) { msg('Escriu un nom de producte', 'warning'); return; }
    const found = catalogoList.find(c => c.nombre.toLowerCase().trim() === nom);
    if (found) {
        input.dataset.catalogoId = found.id;
        input.style.borderColor = '#28a745';
        msg('✓ Trobat: ' + found.nombre, 'success');
    } else {
        input.dataset.catalogoId = '';
        input.style.borderColor = '#ffc107';
        msg('No trobat. Es crearà un producte nou.', 'warning');
    }
}
function mostrarResultadoOCR(datos) {
    $('ocrPaso1').style.display = 'none';
    $('ocrPaso2').style.display = 'block';
    $('ocrNumAlbara').value = datos.numAlbara || '';
    $('ocrProveedorNombre').value = datos.proveedor?.nombre || '';
    $('ocrProveedorCif').value = datos.proveedor?.cif || '';
    if (datos.fecha) {
        const p = datos.fecha.split('/');
        if (p.length === 3)
            $('ocrFecha').value = p[2] + '-' + p[1] + '-' + p[0];
    } else {
        const avui = new Date();
        $('ocrFecha').value = avui.getFullYear() + '-' + String(avui.getMonth() + 1).padStart(2, '0') + '-' + String(avui.getDate()).padStart(2, '0');
    }

    const c = $('ocrLotsContainer');
    c.innerHTML = '';

    if (datos.lots && datos.lots.length) {
        datos.lots.forEach((l, i) => {
            const nomProd = l.descripcion || '';
            const numLot = l.lote || 'L' + Date.now().toString(36).toUpperCase() + i;
            const descOCR = nomProd.toLowerCase().trim();
            const found = catalogoList.find(cat => {
                const cn = cat.nombre.toLowerCase().trim();
                return cn === descOCR || cn.includes(descOCR) || descOCR.includes(cn);
            }) || (descOCR ? catalogoList.find(cat => {
                const pals = descOCR.split(/\s+/);
                return pals.some(p => cat.nombre.toLowerCase().includes(p));
            }) : null);

            const catId = found ? found.id : '';
            const unitOCR = l.unidad || l.unitat || 'kg';
            c.innerHTML += '<div class="lot-item p-3 mb-2 border rounded" id="ocrLot-' + i + '">' +
                '<div class="row g-2 align-items-end">' +
                '<div class="col-md-3"><label class="form-label small mb-0">Nº Lot</label><input class="form-control ocr-lot-num form-control-sm" value="' + escapeHtml(numLot) + '"></div>' +
                '<div class="col-md-3"><label class="form-label small mb-0">Producte</label><div class="input-group input-group-sm"><input type="text" class="form-control ocr-lot-producte" value="' + escapeHtml(nomProd) + '" data-catalogo-id="' + catId + '" style="' + (found ? 'border-color:#28a745' : 'border-color:#ffc107') + '"><button class="btn btn-outline-secondary" type="button" onclick="cercarProducte(this.previousElementSibling)" title="Cercar al catàleg">🔍</button></div></div>' +
                '<div class="col-md-2"><label class="form-label small mb-0">Quantitat</label><input type="number" class="form-control ocr-lot-cant form-control-sm" value="' + (l.cantidad || 0) + '"></div>' +
                '<div class="col-md-2"><label class="form-label small mb-0">Unitat</label><select class="form-select ocr-lot-unitat form-select-sm">' + unitatsSelect(unitOCR) + '</select></div>' +
                '<div class="col-md-2"><label class="form-label small mb-0"> </label><button class="btn btn-outline-danger btn-sm w-100" onclick="this.closest(\'.lot-item\').remove()"><i class="fas fa-trash"></i> Eliminar</button></div>' +
                '</div></div>';
        });
    } else {
        c.innerHTML = '<p class="text-muted">No s\'han detectat lots</p>';
    }
}

async function confirmarOCR() {
    if (!ocrDatosAnalizados)
        return;
    const cifOcr = $('ocrProveedorCif').value;
    if (!validarCIF(cifOcr)) {
        msg('CIF del proveïdor no vàlid', 'warning');
        return;
    }
    const datos = {numAlbara: $('ocrNumAlbara').value, proveedor: {cif: cifOcr, nombre: $('ocrProveedorNombre').value, direccion: ''}, fecha: (() => {
            const v = $('ocrFecha').value;
            if (!v)
                return'';
            const p = v.split('-');
            return p[2] + '/' + p[1] + '/' + p[0];
        })(), imagenTemporal: ocrDatosAnalizados.imagenTemporal || '', lots: []};
    document.querySelectorAll('#ocrLotsContainer .lot-item').forEach((item) => {
        const n = item.querySelector('.ocr-lot-num')?.value;
        const prodInput = item.querySelector('.ocr-lot-producte');
        const prodName = prodInput?.value?.trim();
        const catId = prodInput?.dataset?.catalogoId;
        const q = item.querySelector('.ocr-lot-cant')?.value;
        const u = item.querySelector('.ocr-lot-unitat')?.value || 'kg';
        if (n) {
            const lot = {
                numLot: n,
                quantitat: parseInt(q || 0),
                unitat: u,
                descripcion: prodName || '',
                lote: n,
                cantidad: parseFloat(q || 0),
                unidad: u,
                codigoArticulo: '',
                fechaConsumo: ''
            };
            if (catId) {
                lot.catalogo = {id: parseInt(catId)};
            }
            datos.lots.push(lot);
        }
    });
    if (datos.lots.length === 0) {
        msg('Afegeix almenys un lot', 'warning');
        return;
    }
    $('ocrGuardando').style.display = 'block';
    try {
        const r = await fetch('/api/ocr/confirmar?usuarioId=1', {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(datos)});
        const d = await r.json();
        if (d.success) {
            $('ocrPaso2').style.display = 'none';
            $('ocrPaso3').style.display = 'block';
            cargarAlbarans();
        } else {
            msg(d.mensaje || 'Error', 'danger');
            $('ocrGuardando').style.display = 'none';
        }
    } catch (e) {
        msg('Error: ' + e.message, 'danger');
        $('ocrGuardando').style.display = 'none';
    }
}

function resetOCR() {
    $('ocrPaso1').style.display = 'block';
    $('ocrPaso2').style.display = 'none';
    $('ocrPaso3').style.display = 'none';
    $('ocrLoading').style.display = 'none';
    $('ocrGuardando').style.display = 'none';
    $('ocrImagen').value = '';
    $('ocrPreview').style.display = 'none';
    $('btnAnalizar').disabled = true;
    ocrDatosAnalizados = null;
}
function cerrarModalOCR() {
    bootstrap.Modal.getInstance($('modalOCR')).hide();
    resetOCR();
}

// ========== MENÚ ==========
function toggleUserMenu() {
    const m = $('userMenu');
    if (m)
        m.style.display = m.style.display === 'none' ? 'block' : 'none';
}

document.addEventListener('click', function (e) {
    const m = $('userMenu'), d = document.querySelector('.user-profile');
    if (m && d && !d.contains(e.target) && !m.contains(e.target))
        m.style.display = 'none';
});
document.addEventListener('DOMContentLoaded', () => cargarDades());

// ========== PAGINACIÓ ==========
function renderPagination(totalItems) {
    const totalPages = Math.ceil(totalItems / ITEMS_PER_PAGE);
    let tableWrapper = document.querySelector('.table-wrapper');
    if (!tableWrapper) tableWrapper = document.querySelector('.table-container');
    if (!tableWrapper) {
        const table = document.getElementById('tablaAlbarans');
        if (table) tableWrapper = table.parentElement;
    }
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
    filtrarPerEstat();
}