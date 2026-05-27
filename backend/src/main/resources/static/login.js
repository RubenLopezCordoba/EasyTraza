let usuarioSeleccionado = null;

function cargarUsuarios() {
    console.log('Cargando usuarios...');
    fetch('/login/api/usuarios')
        .then(response => response.json())
        .then(usuarios => {
            const grid = document.getElementById('usuariosGrid');
            if (!grid) return;
            
            grid.innerHTML = '';
            
            if (usuarios.length === 0) {
                grid.innerHTML = '<div class="loading">No hay usuarios disponibles</div>';
                return;
            }
            
            usuarios.forEach(usuario => {
                const card = document.createElement('div');
                card.className = 'usuario-card';
                card.onclick = () => abrirPasswordModal(usuario);
                
                card.innerHTML = usuario.fotoUrl
                    ? `<div class="usuario-avatar" style="overflow:hidden;"><img src="${usuario.fotoUrl}" style="width:100%;height:100%;object-fit:cover;"></div><div class="usuario-nombre">${usuario.nombre}</div><div class="usuario-rol">${getRolTexto(usuario.rol)}</div>`
                    : `<div class="usuario-avatar">${usuario.iniciales || getIniciales(usuario.nombre)}</div><div class="usuario-nombre">${usuario.nombre}</div><div class="usuario-rol">${getRolTexto(usuario.rol)}</div>`;
                
                grid.appendChild(card);
            });
        })
        .catch(error => {
            console.error('Error:', error);
            const grid = document.getElementById('usuariosGrid');
            if (grid) grid.innerHTML = '<div class="loading">Error al cargar usuarios</div>';
        });
}

function getIniciales(nombre) {
    if (!nombre) return '??';
    const partes = nombre.split(' ');
    if (partes.length === 1) return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
    return (partes[0].charAt(0) + partes[partes.length - 1].charAt(0)).toUpperCase();
}

function getRolTexto(rol) {
    if (rol === 'SUPER_ADMIN') return 'Super Administrador';
    if (rol === 'ADMIN') return 'Administrador';
    return 'Trabajador';
}

function abrirPasswordModal(usuario) {
    try {
        usuarioSeleccionado = usuario;
        
        const modalAvatar = document.getElementById('modalAvatar');
        const modalUserName = document.getElementById('modalUserName');
        const passwordInput = document.getElementById('passwordInput');
        const passwordError = document.getElementById('passwordError');
        
        if (modalAvatar) {
            if (usuario.fotoUrl) {
                modalAvatar.innerHTML = '<img src="' + usuario.fotoUrl + '" style="width:60px;height:60px;border-radius:50%;object-fit:cover;">';
            } else {
                modalAvatar.textContent = usuario.iniciales || getIniciales(usuario.nombre);
            }
        }
        if (modalUserName) modalUserName.textContent = usuario.nombre;
        if (passwordInput) {
            passwordInput.value = '';
            passwordInput.classList.remove('input-error');
        }
        if (passwordError) {
            passwordError.style.display = 'none';
            passwordError.textContent = '';
        }
        
        const modal = document.getElementById('passwordModal');
        if (modal) modal.style.display = 'flex';
        
    } catch (error) {
        console.error('Error en abrirPasswordModal:', error);
    }
}

function cerrarPasswordModal() {
    try {
        const modal = document.getElementById('passwordModal');
        if (modal) modal.style.display = 'none';
        usuarioSeleccionado = null;
        
        const passwordInput = document.getElementById('passwordInput');
        if (passwordInput) passwordInput.classList.remove('input-error');
    } catch (error) {
        console.error('Error en cerrarPasswordModal:', error);
    }
}

function mostrarError(mensaje) {
    try {
        const errorDiv = document.getElementById('passwordError');
        const passwordInput = document.getElementById('passwordInput');
        
        if (errorDiv) {
            errorDiv.textContent = mensaje;
            errorDiv.style.display = 'block';
            errorDiv.style.color = '#dc3545';
            errorDiv.style.backgroundColor = '#fff8f8';
            errorDiv.style.padding = '8px 12px';
            errorDiv.style.borderRadius = '8px';
            errorDiv.style.borderLeft = '3px solid #dc3545';
            errorDiv.style.marginTop = '8px';
            errorDiv.style.fontSize = '13px';
        }
        
        if (passwordInput) {
            passwordInput.classList.add('input-error');
            passwordInput.style.border = '2px solid #dc3545';
            passwordInput.value = '';
            passwordInput.focus();
        }
    } catch (error) {
        console.error('Error en mostrarError:', error);
        alert(mensaje); // Fallback
    }
}

async function confirmarPassword() {
    try {
        // Prevenir cualquier comportamiento por defecto
        if (window.event) {
            window.event.preventDefault();
            window.event.stopPropagation();
        }
        
        const password = document.getElementById('passwordInput').value;
        
        if (!usuarioSeleccionado) {
            mostrarError('⚠️ No se ha seleccionado ningún usuario');
            return false;
        }
        
        if (!password) {
            mostrarError('⚠️ Por favor, introduce la contraseña');
            return false;
        }
        
        // Mostrar indicador de carga
        const btnConfirm = document.querySelector('#passwordModal .btn-confirm');
        const originalText = btnConfirm ? btnConfirm.textContent : 'Entrar';
        if (btnConfirm) {
            btnConfirm.textContent = 'Verificando...';
            btnConfirm.disabled = true;
        }
        
        console.log('Enviando petición...');
        
        const response = await fetch('/login/authenticate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id: usuarioSeleccionado.id, password: password })
        });
        
        console.log('Respuesta status:', response.status);
        
        const data = await response.json();
        console.log('Respuesta data:', data);
        
        if (data.success) {
            if (data.necesitaCambiarPassword) {
                cerrarPasswordModal();
                mostrarModalCambioPassword(data.usuario.id, data.usuario.nombre);
            } else {
                window.location.href = '/panel';
            }
        } else {
            let mensajeError = '❌ Contraseña incorrecta';
            if (data.error) {
                mensajeError = `❌ ${data.error}`;
            }
            mostrarError(mensajeError);
        }
        
    } catch (error) {
        console.error('Error en confirmarPassword:', error);
        mostrarError('❌ Error de conexión. Inténtalo de nuevo.');
    } finally {
        const btnConfirm = document.querySelector('#passwordModal .btn-confirm');
        if (btnConfirm) {
            btnConfirm.textContent = 'Entrar';
            btnConfirm.disabled = false;
        }
    }
    
    return false;
}

function mostrarModalCambioPassword(usuarioId, usuarioNombre) {
    try {
        let modal = document.getElementById('cambioPasswordModal');
        if (!modal) {
            modal = document.createElement('div');
            modal.id = 'cambioPasswordModal';
            modal.className = 'password-modal';
            modal.innerHTML = `
                <div class="password-modal-content">
                    <div class="password-modal-header" style="background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);">
                        <div class="user-avatar-large" id="cambioModalAvatar">👤</div>
                        <h3 id="cambioModalUserName">Cambiar Contraseña</h3>
                        <p style="color: rgba(255,255,255,0.9); font-size: 12px; margin-top: 10px;">⚠️ Por seguridad, debes cambiar tu contraseña</p>
                    </div>
                    <div class="password-modal-body">
                        <label>Nueva Contraseña (mínimo 6 caracteres)</label>
                        <input type="password" id="nuevaPasswordInput" placeholder="Nueva contraseña" autocomplete="off">
                        <label style="margin-top: 15px;">Confirmar Contraseña</label>
                        <input type="password" id="confirmarPasswordInput" placeholder="Confirmar contraseña" autocomplete="off">
                        <div class="error-message" id="cambioPasswordError"></div>
                    </div>
                    <div class="password-modal-footer">
                        <button class="btn-confirm" type="button" onclick="confirmarCambioPassword(${usuarioId})">Cambiar Contraseña</button>
                    </div>
                </div>
            `;
            document.body.appendChild(modal);
        }
        
        const avatarDiv = document.getElementById('cambioModalAvatar');
        if (avatarDiv) avatarDiv.textContent = getIniciales(usuarioNombre);
        
        const userNameSpan = document.getElementById('cambioModalUserName');
        if (userNameSpan) userNameSpan.textContent = usuarioNombre;
        
        const nuevaPass = document.getElementById('nuevaPasswordInput');
        const confirmaPass = document.getElementById('confirmarPasswordInput');
        if (nuevaPass) nuevaPass.value = '';
        if (confirmaPass) confirmaPass.value = '';
        
        const errorDiv = document.getElementById('cambioPasswordError');
        if (errorDiv) errorDiv.style.display = 'none';
        
        modal.style.display = 'flex';
        
    } catch (error) {
        console.error('Error en mostrarModalCambioPassword:', error);
    }
}

async function confirmarCambioPassword(usuarioId) {
    try {
        if (window.event) {
            window.event.preventDefault();
            window.event.stopPropagation();
        }
        
        const nuevaPassword = document.getElementById('nuevaPasswordInput').value;
        const confirmarPassword = document.getElementById('confirmarPasswordInput').value;
        const errorDiv = document.getElementById('cambioPasswordError');
        const nuevaPassInput = document.getElementById('nuevaPasswordInput');
        const confirmaPassInput = document.getElementById('confirmarPasswordInput');
        
        if (!nuevaPassword) {
            if (errorDiv) {
                errorDiv.textContent = '⚠️ Por favor, introduce una nueva contraseña';
                errorDiv.style.display = 'block';
            }
            if (nuevaPassInput) nuevaPassInput.classList.add('input-error');
            return false;
        }
        
        if (!confirmarPassword) {
            if (errorDiv) {
                errorDiv.textContent = '⚠️ Por favor, confirma la contraseña';
                errorDiv.style.display = 'block';
            }
            if (confirmaPassInput) confirmaPassInput.classList.add('input-error');
            return false;
        }
        
        if (nuevaPassword.length < 6) {
            if (errorDiv) {
                errorDiv.textContent = '⚠️ La contraseña debe tener al menos 6 caracteres';
                errorDiv.style.display = 'block';
            }
            if (nuevaPassInput) nuevaPassInput.classList.add('input-error');
            return false;
        }
        
        if (nuevaPassword !== confirmarPassword) {
            if (errorDiv) {
                errorDiv.textContent = '❌ Las contraseñas no coinciden';
                errorDiv.style.display = 'block';
            }
            if (nuevaPassInput) nuevaPassInput.classList.add('input-error');
            if (confirmaPassInput) confirmaPassInput.classList.add('input-error');
            return false;
        }
        
        if (errorDiv) errorDiv.style.display = 'none';
        if (nuevaPassInput) nuevaPassInput.classList.remove('input-error');
        if (confirmaPassInput) confirmaPassInput.classList.remove('input-error');
        
        const btnConfirm = document.querySelector('#cambioPasswordModal .btn-confirm');
        const originalText = btnConfirm ? btnConfirm.textContent : 'Cambiar Contraseña';
        if (btnConfirm) {
            btnConfirm.textContent = 'Cambiando...';
            btnConfirm.disabled = true;
        }
        
        const response = await fetch('/login/api/cambiar-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                usuarioId: usuarioId,
                nuevaPassword: nuevaPassword,
                confirmarPassword: confirmarPassword
            })
        });
        
        const data = await response.json();
        
        if (data.success) {
            const modal = document.getElementById('cambioPasswordModal');
            if (modal) modal.style.display = 'none';
            window.location.href = '/panel';
        } else {
            if (errorDiv) {
                errorDiv.textContent = `❌ ${data.error || 'Error al cambiar la contraseña'}`;
                errorDiv.style.display = 'block';
            }
        }
        
    } catch (error) {
        console.error('Error en confirmarCambioPassword:', error);
        const errorDiv = document.getElementById('cambioPasswordError');
        if (errorDiv) {
            errorDiv.textContent = '❌ Error de conexión. Inténtalo de nuevo.';
            errorDiv.style.display = 'block';
        }
    } finally {
        const btnConfirm = document.querySelector('#cambioPasswordModal .btn-confirm');
        if (btnConfirm) {
            btnConfirm.textContent = 'Cambiar Contraseña';
            btnConfirm.disabled = false;
        }
    }
    
    return false;
}

// Cerrar modales al hacer clic fuera
window.onclick = function(event) {
    const modalPassword = document.getElementById('passwordModal');
    if (event.target === modalPassword) cerrarPasswordModal();
    
    const modalCambio = document.getElementById('cambioPasswordModal');
    if (event.target === modalCambio && modalCambio) {
        modalCambio.style.display = 'none';
    }
};

// Inicializar
document.addEventListener('DOMContentLoaded', cargarUsuarios);

function toggleLoginPassword() {
    const input = document.getElementById('passwordInput');
    const icon = document.getElementById('loginPasswordIcon');
    if (input.type === 'password') {
        input.type = 'text';
        icon.className = 'fas fa-eye-slash';
    } else {
        input.type = 'password';
        icon.className = 'fas fa-eye';
    }
}