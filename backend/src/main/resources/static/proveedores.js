let proveedoresData = [], proveedoresFull = [], usuarioActual = null, proveedorModal, confirmDeleteModalInstance = null;
let pendingDeleteCif = null, pendingDeleteName = null;
let searchTimeout = null; let currentPage = 1; const ITEMS_PER_PAGE = 10;

const $ = id => document.getElementById(id);

const msg = (m, t) => {
    let a = document.createElement('div');
    a.className = 'alert alert-' + t + ' position-fixed top-0 end-0 m-3';
    a.style.zIndex = '9999';
    a.style.maxWidth = '400px';
    a.innerHTML = m + '<button type="button" class="btn-close" data-bs-dismiss="alert"></button>';
    document.body.appendChild(a);
    setTimeout(() => a.remove(), 3000);
};

const validarCIF_NIF = (doc) => {
    if (!doc || doc.length < 8 || doc.length > 10) return false;
    doc = doc.toUpperCase().replace(/[\s\-.,;_]/g, '');
    // NIF: 8 digits + letter (control)
    if (/^\d{8}[A-Z]$/.test(doc)) {
        const letras = "TRWAGMYFPDXBNJZSQVHLCKE";
        return letras.charAt(parseInt(doc.substring(0, 8)) % 23) === doc.charAt(8);
    }
    // NIE: X/Y/Z + 7 digits + letter
    if (/^[XYZ]\d{7}[A-Z]$/.test(doc)) {
        const nif = (doc[0] === 'X' ? '0' : doc[0] === 'Y' ? '1' : '2') + doc.substring(1, 8);
        const letras = "TRWAGMYFPDXBNJZSQVHLCKE";
        return letras.charAt(parseInt(nif) % 23) === doc.charAt(8);
    }
    // CIF: letter + 7 digits + control (digit or letter)
    if (/^[ABCDEFGHJKLMNPQRSUVW]\d{7}[0-9A-J]$/.test(doc)) {
        const digits = doc.substring(1, 8);
        let sum = 0;
        for (let i = 0; i < 7; i++) {
            const d = parseInt(digits[i]);
            if (i % 2 === 0) {
                const x2 = d * 2;
                sum += x2 > 9 ? x2 - 9 : x2;
            } else {
                sum += d;
            }
        }
        const control = (10 - (sum % 10)) % 10;
        const tipus = doc[0];
        if ("ABCDEFGHJKLMNPQRSUVW".indexOf(tipus) >= 0) {
            return parseInt(doc[8]) === control || "ABCDEFGHJKLMNPQRSUVW".indexOf(doc[8]) >= 0;
        }
        return doc[8] === "TRWAGMYFPDXBNJZSQVHLCKE".charAt(control);
    }
    return false;
};

const escapeHtml = text => {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
};

const render = () => {
    const tbody = $('tablaProveedores');
    if (!tbody) return;
    if (!proveedoresData.length) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">No hay proveedores</td></tr>';
        renderPagination(0);
        return;
    }
    const start = (currentPage - 1) * ITEMS_PER_PAGE;
    const pageItems = proveedoresData.slice(start, start + ITEMS_PER_PAGE);
    tbody.innerHTML = pageItems.map(p => `
        <tr>
            <td><strong>${escapeHtml(p.cif)}</strong></td>
            <td>${escapeHtml(p.nombre)}</td>
            <td>${p.telefono || '-'}</td>
            <td>${escapeHtml(p.direccion) || '-'}</td>
            <td>${escapeHtml(p.observaciones) || '-'}</td>
            <td class="action-icons">
                ${usuarioActual?.rol !== 'TRABAJADOR' ? `<button class="btn-warning" onclick="editarProveedor('${p.cif}')" title="Editar"><i class="fas fa-edit"></i></button>
                <button class="btn-danger" onclick="mostrarConfirmDelete('${p.cif}', '${escapeHtml(p.nombre)}')" title="Eliminar"><i class="fas fa-trash"></i></button>` : ''}
            </td>
        </tr>
    `).join('');
    renderPagination(proveedoresData.length);
};

const cargarProveedores = async () => {
    try {
        const r = await fetch('/proveedores/api/listar');
        proveedoresData = await r.json();
        proveedoresFull = [...proveedoresData];
        render();
    } catch (e) {
        msg('Error al cargar', 'danger');
    }
};

const buscarProveedores = () => {
    if (searchTimeout) clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        currentPage = 1;
        const t = $('searchInput')?.value.trim().toLowerCase();
        if (t) {
            proveedoresData = proveedoresFull.filter(p =>
                (p.cif || p.nif || '').toLowerCase().includes(t) ||
                (p.nombre || '').toLowerCase().includes(t)
            );
            render();
        } else cargarProveedores();
    }, 300);
};

const cargarDatos = async () => {
    try {
        await cargarProveedores();
        const r = await fetch('/usuarios/api/actual');
        usuarioActual = await r.json();
        if (usuarioActual?.nombre) {
            const av = document.querySelector('.avatar');
            const un = document.querySelector('.user-name');
            const ur = document.querySelector('.user-role');
            if (av) {
                if (usuarioActual.fotoUrl) {
                    av.innerHTML = '<img src="' + usuarioActual.fotoUrl + '" alt="Foto" style="width:100%;height:100%;border-radius:50%;object-fit:cover;">';
                } else {
                    av.textContent = usuarioActual.iniciales || '??';
                }
            }
            if (un) un.textContent = usuarioActual.nombre;
            if (ur) ur.textContent = usuarioActual.rol === 'SUPER_ADMIN' ? 'Super Admin' : usuarioActual.rol === 'ADMIN' ? 'Administrador' : 'Trabajador';
        }
    } catch (e) { msg('Error', 'danger'); }
};

const mostrarFormularioNuevo = () => {
    $('modalTitle').textContent = 'Nuevo Proveedor';
    $('proveedorForm').reset();
    $('proveedorId').value = '';
    $('cif').readOnly = false;
    proveedorModal = new bootstrap.Modal($('proveedorModal'));
    proveedorModal.show();
};

const editarProveedor = async cif => {
    try {
        const r = await fetch('/proveedores/api/' + cif);
        const p = await r.json();
        $('modalTitle').textContent = 'Editar Proveedor';
        $('proveedorId').value = p.cif;
        $('nombre').value = p.nombre || '';
        $('telefono').value = p.telefono || '';
        $('cif').value = p.cif || '';
        $('direccion').value = p.direccion || '';
        $('observaciones').value = p.observaciones || '';
        $('cif').readOnly = true;
        proveedorModal = new bootstrap.Modal($('proveedorModal'));
        proveedorModal.show();
    } catch (e) {
        msg('Error al cargar', 'danger');
    }
};

const guardarProveedor = async () => {
    const nombre = $('nombre').value.trim();
    const cif = $('cif').value.trim().toUpperCase();
    const isEdit = $('cif').readOnly;
    
    if (!nombre) { msg('Nombre obligatorio', 'warning'); return; }
    if (!cif) { msg('CIF obligatorio', 'warning'); return; }
    if (!validarCIF_NIF(cif)) { msg('CIF inválido', 'warning'); return; }
    
    const data = {
        cif: cif,
        nombre: nombre,
        telefono: $('telefono').value.trim(),
        direccion: $('direccion').value.trim(),
        observaciones: $('observaciones').value.trim()
    };
    
    try {
        const url = isEdit ? '/proveedores/api/actualizar/' + cif : '/proveedores/api/guardar';
        const method = isEdit ? 'PUT' : 'POST';
        const r = await fetch(url, { method, headers: {'Content-Type': 'application/json'}, body: JSON.stringify(data) });
        const res = await r.json();
        if (r.ok && res.success) {
            proveedorModal.hide();
            cargarProveedores();
            msg(isEdit ? 'Actualizado' : 'Creado', 'success');
        } else {
            msg(res.error || 'Error', 'danger');
        }
    } catch (e) { msg('Error', 'danger'); }
};

const mostrarConfirmDelete = (cif, nombre) => {
    pendingDeleteCif = cif;
    pendingDeleteName = nombre;
    $('deleteMessage').innerHTML = '¿Eliminar <strong>' + escapeHtml(nombre) + '</strong>?';
    if (!confirmDeleteModalInstance) confirmDeleteModalInstance = new bootstrap.Modal($('confirmDeleteModal'));
    confirmDeleteModalInstance.show();
};

const ejecutarEliminacion = async () => {
    if (!pendingDeleteCif) return;
    const btn = $('confirmDeleteBtn');
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
    try {
        const r = await fetch('/proveedores/api/eliminar/' + pendingDeleteCif, { method: 'DELETE' });
        const res = await r.json();
        if (r.ok && res.success) {
            confirmDeleteModalInstance.hide();
            cargarProveedores();
            msg('Eliminado', 'success');
        } else {
            confirmDeleteModalInstance.hide();
            $('errorDeleteMsg').textContent = res.error || 'Error';
            new bootstrap.Modal($('errorDeleteModal')).show();
        }
    } catch (e) {
        confirmDeleteModalInstance.hide();
        $('errorDeleteMsg').textContent = 'Error de connexió';
        new bootstrap.Modal($('errorDeleteModal')).show();
    }
    btn.disabled = false;
    btn.innerHTML = '<i class="fas fa-trash"></i> Eliminar';
    pendingDeleteCif = null;
};

const toggleUserMenu = () => {
    const m = $('userMenu');
    if (m) m.style.display = m.style.display === 'none' ? 'block' : 'none';
};

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
    render();
}

document.addEventListener('DOMContentLoaded', () => {
    cargarDatos();
    $('confirmDeleteBtn').addEventListener('click', ejecutarEliminacion);
});

document.addEventListener('click', e => {
    const m = $('userMenu'), p = document.querySelector('.user-profile');
    if (m && p && !p.contains(e.target) && !m.contains(e.target)) m.style.display = 'none';
});