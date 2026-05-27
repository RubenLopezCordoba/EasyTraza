let catalogoData = [], usuarioActual = null, catalogoModal, confirmDeleteModal, currentFilter = 'todos';
let pendingDeleteId = null, pendingDeleteName = null; let currentPage = 1; const ITEMS_PER_PAGE = 10;

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

const render = () => {
    const tbody = $('tablaCatalogo');
    let items = catalogoData;
    if (currentFilter !== 'todos') items = items.filter(i => i.tipo === currentFilter);
    const searchTerm = $('searchInput')?.value.toLowerCase();
    if (searchTerm) items = items.filter(i => i.nombre.toLowerCase().includes(searchTerm) || (i.descripcion || '').toLowerCase().includes(searchTerm));
    
    if (!items.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center">No hay items registrados</td></tr>';
        renderPagination(0);
        return;
    }
    
    const start = (currentPage - 1) * ITEMS_PER_PAGE;
    const pageItems = items.slice(start, start + ITEMS_PER_PAGE);
    tbody.innerHTML = pageItems.map(i => `
        <tr>
            <td>${i.id}</td>
            <td><strong>${escapeHtml(i.nombre)}</strong></td>
            <td>${escapeHtml(i.descripcion) || '-'}</td>
            <td><span class="badge-tipo badge-${i.tipo === 'INGREDIENTE' ? 'ingrediente' : 'producto'}">${i.tipo === 'INGREDIENTE' ? 'Materia Prima' : 'Producto'}</span></td>
            <td class="action-icons">
                ${usuarioActual?.rol !== 'TRABAJADOR' ? `<button class="btn-warning" onclick="editarItem(${i.id})" title="Editar"><i class="fas fa-edit"></i></button>
                <button class="btn-danger" onclick="mostrarConfirmDelete(${i.id}, '${escapeHtml(i.nombre)}')" title="Eliminar"><i class="fas fa-trash"></i></button>` : ''}
            </td>
        </tr>
    `).join('');
    renderPagination(items.length);
};

const cargarDatos = async () => {
    catalogoData = await api('/catalogo/api/listar');
    usuarioActual = await api('/usuarios/api/actual');
    if (usuarioActual?.nombre) {
        const av = document.querySelector('.avatar');
        if (av) {
            if (usuarioActual.fotoUrl) {
                av.innerHTML = '<img src="' + usuarioActual.fotoUrl + '" alt="Foto" style="width:100%;height:100%;border-radius:50%;object-fit:cover;">';
            } else {
                av.textContent = usuarioActual.iniciales || '??';
            }
        }
        document.querySelector('.user-name').textContent = usuarioActual.nombre;
        document.querySelector('.user-role').textContent = usuarioActual.rol === 'SUPER_ADMIN' ? 'Super Admin' : usuarioActual.rol === 'ADMIN' ? 'Administrador' : 'Trabajador';
    }
    render();
};

const filtrarPorTipo = tipo => {
    currentPage = 1;
    currentFilter = tipo;
    document.querySelectorAll('.tab-btn').forEach(t => t.classList.remove('active'));
    const tabId = tipo === 'todos' ? 'tab-tots' : 'tab-' + tipo;
    const activeTab = document.getElementById(tabId);
    if (activeTab) activeTab.classList.add('active');
    render();
};

const buscarItems = () => { currentPage = 1; render(); };

const mostrarFormularioNuevo = () => {
    $('modalTitle').textContent = 'Nuevo Ítem';
    $('catalogoForm').reset();
    $('itemId').value = '';
    catalogoModal = new bootstrap.Modal($('catalogoModal'));
    catalogoModal.show();
};

const editarItem = async id => {
    const i = await api(`/catalogo/api/${id}`);
    $('modalTitle').textContent = 'Editar Ítem';
    $('itemId').value = i.id;
    $('tipo').value = i.tipo;
    $('nombre').value = i.nombre;
    $('descripcion').value = i.descripcion || '';
    catalogoModal = new bootstrap.Modal($('catalogoModal'));
    catalogoModal.show();
};

const guardarItem = async () => {
    const id = $('itemId').value;
    const item = {
        tipo: $('tipo').value,
        nombre: $('nombre').value.trim(),
        descripcion: $('descripcion').value.trim()
    };
    if (!item.tipo || !item.nombre) return msg('Complete los campos obligatorios', 'warning');
    
    const res = await api(`/catalogo/api/${id ? `actualizar/${id}` : 'guardar'}`, {
        method: id ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(item)
    });
    
    if (res.success) {
        catalogoModal.hide();
        cargarDatos();
        msg(id ? 'Actualizado' : 'Guardado', 'success');
    } else msg(res.error, 'danger');
};

// Reemplaza todo el código relacionado con el modal de confirmación

// Variables globales
let confirmDeleteModalInstance = null;

// Mostrar modal de confirmación
const mostrarConfirmDelete = (id, nombre) => {
    pendingDeleteId = id;
    pendingDeleteName = nombre;
    
    const deleteMessage = document.getElementById('deleteMessage');
    if (deleteMessage) {
        deleteMessage.innerHTML = `¿Estás seguro de que quieres eliminar <strong>${escapeHtml(nombre)}</strong>?`;
    }
    
    // Crear o reutilizar instancia del modal
    const modalElement = document.getElementById('confirmDeleteModal');
    if (!confirmDeleteModalInstance) {
        confirmDeleteModalInstance = new bootstrap.Modal(modalElement);
    }
    confirmDeleteModalInstance.show();
};

// Ejecutar eliminación
const ejecutarEliminacion = async () => {
    if (!pendingDeleteId) {
        if (confirmDeleteModalInstance) confirmDeleteModalInstance.hide();
        return;
    }
    
    // Cambiar estado del botón para evitar doble clic
    const confirmBtn = document.getElementById('confirmDeleteBtn');
    const originalText = confirmBtn.innerHTML;
    confirmBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Eliminando...';
    confirmBtn.disabled = true;
    
    try {
        const res = await api(`/catalogo/api/eliminar/${pendingDeleteId}`, { 
            method: 'DELETE' 
        });
        
        if (res.success) {
            confirmDeleteModalInstance.hide();
            await cargarDatos();
            msg(`"${pendingDeleteName}" eliminado correctamente`, 'success');
            pendingDeleteId = null;
            pendingDeleteName = null;
        } else {
            confirmDeleteModalInstance.hide();
            document.getElementById('errorDeleteMsg').textContent = res.error || 'Error al eliminar';
            new bootstrap.Modal(document.getElementById('errorDeleteModal')).show();
        }
    } catch (error) {
        console.error('Error:', error);
        confirmDeleteModalInstance.hide();
        document.getElementById('errorDeleteMsg').textContent = 'Error de connexió';
        new bootstrap.Modal(document.getElementById('errorDeleteModal')).show();
    } finally {
        confirmBtn.innerHTML = originalText;
        confirmBtn.disabled = false;
    }
};

// Mantener la función eliminarItem por compatibilidad (opcional)
const eliminarItem = async (id, nombre) => {
    mostrarConfirmDelete(id, nombre);
};

const toggleUserMenu = () => {
    const m = document.getElementById('userMenu');
    m.style.display = m.style.display === 'none' ? 'block' : 'none';
};

const escapeHtml = text => {
    const div = document.createElement('div');
    div.textContent = text || '';
    return div.innerHTML;
};

// Cerrar menú al hacer clic fuera
document.addEventListener('click', e => {
    const menu = document.getElementById('userMenu');
    const profile = document.querySelector('.user-profile');
    if (menu && profile && !profile.contains(e.target) && !menu.contains(e.target)) menu.style.display = 'none';
});

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

// Configurar evento del botón confirmar
document.addEventListener('DOMContentLoaded', () => {
    cargarDatos();
    const confirmBtn = document.getElementById('confirmDeleteBtn');
    if (confirmBtn) {
        confirmBtn.addEventListener('click', ejecutarEliminacion);
    }
});