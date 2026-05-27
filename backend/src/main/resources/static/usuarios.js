let usuariosData = [], usuarioActual = null, usuarioModal;
let idUsuarioAEliminar = null; let currentPage = 1; const ITEMS_PER_PAGE = 10;

const $ = id => document.getElementById(id);
const api = async (url, opt = {}) => (await fetch(url, opt)).json();
const msg = (m, t) => {
    let a = document.createElement('div');
    a.className = `alert alert-${t} position-fixed top-0 end-0 m-3`;
    a.style.zIndex = '9999';
    a.innerHTML = `${m}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>`;
    document.body.appendChild(a);
    setTimeout(() => a.remove(), 3000);
};

const mostrarTabla = (usuarios) => {
    const tbody = $('tablaUsuarios');
    if (!usuarios.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">No hay usuarios registrados</td></tr>';
        renderPagination(0);
        return;
    }
    const start = (currentPage - 1) * ITEMS_PER_PAGE;
    const pageItems = usuarios.slice(start, start + ITEMS_PER_PAGE);
    const rClass = r => ({SUPER_ADMIN:'super-admin', ADMIN:'admin', TRABAJADOR:'trabajador'}[r] || 'trabajador');
    const rName = r => ({SUPER_ADMIN:'Super Admin', ADMIN:'Administrador', TRABAJADOR:'Trabajador'}[r] || r);
    
    const puedeEditar = (usuario) => {
        // Nadie puede editar al SUPER_ADMIN
        if (usuario.rol === 'SUPER_ADMIN') return false;
        
        if (usuarioActual?.rol === 'SUPER_ADMIN') return true;
        if (usuarioActual?.rol === 'ADMIN') {
            return usuario.rol !== 'SUPER_ADMIN';
        }
        if (usuarioActual?.rol === 'TRABAJADOR') {
            return usuario.id === usuarioActual.id;
        }
        return false;
    };
    
    const puedeEliminar = (usuario) => {
        // Nadie puede eliminar al SUPER_ADMIN
        if (usuario.rol === 'SUPER_ADMIN') return false;
        if (usuarioActual?.id === usuario.id) return false;
        if (usuarioActual?.rol === 'SUPER_ADMIN') return true;
        if (usuarioActual?.rol === 'ADMIN') {
            return usuario.rol !== 'SUPER_ADMIN';
        }
        return false;
    };
    
    tbody.innerHTML = pageItems.map(u => `
        <tr>
            <td>${u.id}</td>
            <td>${u.nif || '-'}</td>
            <td><strong>${escapeHtml(u.nombre)}</strong></td>
            <td>${escapeHtml(u.email)}</td>
            <td>${u.telefono || '-'}</td>
            <td><span class="badge-role badge-${rClass(u.rol)}">${rName(u.rol)}</span></td>
            <td><span class="badge-estado badge-${u.activo ? 'activado' : 'desactivado'}">${u.activo ? 'Activo' : 'Inactivo'}</span></td>
            <td class="action-icons">
                ${puedeEditar(u) ? `<button class="btn-warning" onclick="editarUsuario(${u.id})" title="Editar"><i class="fas fa-edit"></i></button>` : ''}
                ${(usuarioActual?.rol === 'SUPER_ADMIN' || usuarioActual?.rol === 'ADMIN') && usuarioActual?.id !== u.id && u.rol !== 'SUPER_ADMIN' ? `<button class="btn-${u.activo ? 'danger' : 'success'}" onclick="toggleActivo(${u.id})" title="${u.activo ? 'Desactivar' : 'Activar'}"><i class="fas fa-${u.activo ? 'ban' : 'check'}"></i></button>` : ''}
                ${puedeEliminar(u) ? `<button class="btn-danger" onclick="eliminarUsuario(${u.id})" title="Eliminar"><i class="fas fa-trash"></i></button>` : ''}
              </td>
          </tr>
    `).join('');
    renderPagination(usuarios.length);
};

const cargarUsuarios = async () => {
    try {
        usuariosData = await api('/usuarios/api/listar');
        mostrarTabla(usuariosData);
    } catch (e) {
        msg('Error al cargar usuarios: ' + e.message, 'danger');
    }
};

const actualizarAvatar = () => {
    const avatarDiv = document.querySelector('.avatar');
    if (usuarioActual?.fotoUrl) {
        avatarDiv.innerHTML = '<img src="' + usuarioActual.fotoUrl + '" alt="Foto" style="width:100%;height:100%;border-radius:50%;object-fit:cover;">';
    } else {
        avatarDiv.textContent = usuarioActual?.iniciales || '??';
    }
};

const obtenerUsuarioActual = async () => {
    usuarioActual = await api('/usuarios/api/actual');
    if (usuarioActual?.nombre) {
        actualizarAvatar();
        document.querySelector('.user-name').textContent = usuarioActual.nombre;
        const rolText = usuarioActual.rol === 'SUPER_ADMIN' ? 'Super Admin' : 
                        usuarioActual.rol === 'ADMIN' ? 'Administrador' : 'Trabajador';
        document.querySelector('.user-role').textContent = rolText;
    }
};

const filtrarPorRol = rol => {
    currentPage = 1;
    document.querySelectorAll('.filter-tab').forEach(t => t.classList.remove('active'));
    event.target.classList.add('active');
    mostrarTabla(rol === 'todos' ? usuariosData : usuariosData.filter(u => u.rol === rol));
};

const buscarUsuarios = () => {
    currentPage = 1;
    const term = $('searchInput').value.toLowerCase();
    mostrarTabla(usuariosData.filter(u => u.nombre.toLowerCase().includes(term) || u.email.toLowerCase().includes(term)));
};

const mostrarFormularioNuevo = () => {
    // Solo SUPER_ADMIN y ADMIN pueden crear usuarios
    if (usuarioActual?.rol === 'TRABAJADOR') {
        msg('No tienes permiso para crear usuarios', 'warning');
        return;
    }
    
    $('modalTitle').textContent = 'Nuevo Usuario';
    $('usuarioForm').reset();
    $('usuarioId').value = '';
    $('nif').readOnly = false;
    $('password').required = true;
    $('passwordLabel').innerHTML = 'Contraseña *';
    $('passwordHelp').style.display = 'block';
    $('rol').disabled = false;
    $('fotoSection').style.display = 'none'; // No foto para usuarios nuevos
    
    // Solo se pueden crear ADMIN o TRABAJADOR, NUNCA SUPER_ADMIN
    if (usuarioActual?.rol === 'SUPER_ADMIN') {
        $('rol').innerHTML = `<option value="">Seleccionar...</option>
                             <option value="ADMIN">Administrador</option>
                             <option value="TRABAJADOR">Trabajador</option>`;
    } else if (usuarioActual?.rol === 'ADMIN') {
        $('rol').innerHTML = `<option value="">Seleccionar...</option>
                             <option value="ADMIN">Administrador</option>
                             <option value="TRABAJADOR">Trabajador</option>`;
    }
    
    usuarioModal = new bootstrap.Modal($('usuarioModal'));
    usuarioModal.show();
};

const editarUsuario = async (id) => {
    const u = await api(`/usuarios/api/${id}`);
    
    // No permitir editar al SUPER_ADMIN
    if (u.rol === 'SUPER_ADMIN') {
        msg('No se puede editar el usuario Super Administrador', 'warning');
        return;
    }
    
    $('modalTitle').textContent = 'Editar Usuario';
    $('usuarioId').value = u.id;
    $('nombre').value = u.nombre;
    $('nif').value = u.nif || '';
    $('nif').readOnly = true;
    $('email').value = u.email;
    $('telefono').value = u.telefono || '';
    $('password').value = '';
    $('password').required = false;
    $('passwordLabel').innerHTML = 'Contraseña (dejar en blanco para mantener)';
    
    const esPropio = usuarioActual?.id === u.id;
    $('fotoSection').style.display = esPropio ? 'block' : 'none';
    
    // Gestionar permisos para cambiar el rol
    if (usuarioActual?.rol === 'SUPER_ADMIN') {
        // SUPER_ADMIN puede cambiar el rol entre ADMIN y TRABAJADOR
        $('rol').disabled = false;
        $('rol').innerHTML = `<option value="ADMIN">Administrador</option>
                             <option value="TRABAJADOR">Trabajador</option>`;
        $('rol').value = u.rol;
    } else if (usuarioActual?.rol === 'ADMIN') {
        // ADMIN no puede cambiar el rol de nadie
        $('rol').disabled = true;
        $('rol').value = u.rol;
    } else if (usuarioActual?.rol === 'TRABAJADOR') {
        // TRABAJADOR solo puede editarse a sí mismo y no puede cambiar rol
        if (esPropio) {
            $('rol').disabled = true;
            $('rol').value = u.rol;
        }
    }
    
    usuarioModal = new bootstrap.Modal($('usuarioModal'));
    usuarioModal.show();
};

const guardarUsuario = async () => {
    const id = $('usuarioId').value;
    const rolSeleccionado = $('rol').value;
    
    // Validar que no se intente crear/editar un SUPER_ADMIN
    if (rolSeleccionado === 'SUPER_ADMIN') {
        msg('No se puede crear o editar un usuario Super Administrador', 'warning');
        return;
    }
    
    const usuario = {
        nombre: $('nombre').value,
        nif: $('nif').value,
        email: $('email').value,
        password: $('password').value,
        telefono: $('telefono').value,
        rol: rolSeleccionado,
        fotoUrl: ''
    };
    if (!usuario.nombre || !usuario.email) {
        msg('Complete los campos obligatorios', 'warning');
        return;
    }
    if (!usuario.nif) {
        msg('El NIF es obligatorio', 'warning');
        return;
    }
    if (!validarNIF(usuario.nif)) {
        msg('El NIF no es válido', 'warning');
        return;
    }
    if (!id && (!usuario.password || usuario.password.length < 4)) {
        msg('Contraseña mínimo 4 caracteres', 'warning');
        return;
    }
    
    const url = id ? `/usuarios/api/actualizar/${id}` : '/usuarios/api/guardar';
    const method = id ? 'PUT' : 'POST';
    
    const res = await api(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(usuario)
    });
    
    if (res.success) {
        usuarioModal.hide();
        cargarUsuarios();
        if (res.usuario && usuarioActual && res.usuario.id === usuarioActual.id) {
            usuarioActual = res.usuario;
            actualizarAvatar();
        }
        msg('Usuario guardado correctamente', 'success');
    } else {
        msg(res.error, 'danger');
    }
};

function eliminarUsuario(id) {
    const usuario = usuariosData.find(u => u.id === id);
    
    if (usuario?.rol === 'SUPER_ADMIN') {
        msg('No se puede eliminar al Super Administrador', 'warning');
        return;
    }
    
    if (usuarioActual?.id === id) {
        msg('No puedes eliminar tu propio usuario', 'warning');
        return;
    }
    
    idUsuarioAEliminar = id;
    const modal = document.getElementById('confirmDeleteModal');
    if (modal) {
        modal.style.display = 'flex';
    } else {
        msg('Error al mostrar la confirmación', 'danger');
    }
}

async function confirmarEliminar() {
    const id = idUsuarioAEliminar;
    if (!id) {
        tancarConfirmModal();
        return;
    }
    try {
        const res = await fetch(`/usuarios/api/eliminar/${id}`, { method: 'DELETE' });
        const data = await res.json();
        tancarConfirmModal();
        if (data.success) {
            await cargarUsuarios();
            msg('Usuario eliminado correctamente', 'success');
        } else {
            msg(data.error || 'Error desconegut', 'danger');
        }
    } catch (e) {
        tancarConfirmModal();
        msg('Error en eliminar: ' + e.message, 'danger');
    }
}

async function toggleActivo(id) {
    const res = await api(`/usuarios/api/toggle-activo/${id}`, { method: 'PUT' });
    if (res.success) {
        cargarUsuarios();
        msg(res.message, 'success');
    } else {
        msg(res.error, 'danger');
    }
}

function tancarConfirmModal() {
    const modal = document.getElementById('confirmDeleteModal');
    if (modal) modal.style.display = 'none';
    idUsuarioAEliminar = null;
}

const toggleUserMenu = () => {
    const menu = $('userMenu');
    menu.style.display = menu.style.display === 'none' ? 'block' : 'none';
};

const escapeHtml = text => {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
};

function validarNIF(doc) {
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
    return false;
}

document.addEventListener('click', (e) => {
    const menu = $('userMenu');
    const dropdown = document.querySelector('.user-profile');
    if (menu && dropdown && !dropdown.contains(e.target) && !menu.contains(e.target)) {
        menu.style.display = 'none';
    }
});

const subirFoto = async () => {
    const fileInput = document.getElementById('fotoInput');
    const file = fileInput.files[0];
    if (!file) {
        msg('Selecciona una imagen primero', 'warning');
        return;
    }
    const formData = new FormData();
    formData.append('foto', file);
    try {
        const res = await fetch('/usuarios/api/subir-foto', {
            method: 'POST',
            body: formData
        });
        const data = await res.json();
        if (data.success) {
            usuarioActual.fotoUrl = data.fotoUrl;
            actualizarAvatar();
            // Also force the user dropdown to refresh its avatar
            const allAvatars = document.querySelectorAll('.avatar');
            allAvatars.forEach(av => {
                av.innerHTML = '<img src="' + data.fotoUrl + '?t=' + Date.now() + '" alt="Foto" style="width:100%;height:100%;border-radius:50%;object-fit:cover;" onerror="this.onerror=null;this.remove()">';
            });
            msg('Foto de perfil actualizada', 'success');
        } else {
            msg(data.error, 'danger');
        }
    } catch (e) {
        msg('Error al subir la foto', 'danger');
    }
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
    mostrarTabla(usuariosData);
}

document.addEventListener('DOMContentLoaded', async () => {
    const confirmModal = document.getElementById('confirmDeleteModal');
    if (confirmModal) {
        confirmModal.addEventListener('click', function(event) {
            if (event.target === this) {
                tancarConfirmModal();
            }
        });
    }
    await obtenerUsuarioActual();
    cargarUsuarios();
});