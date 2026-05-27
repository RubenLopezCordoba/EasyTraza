console.log('=== LOTS JS ===');
let lotsData=[],acciPendent=null,lotSel=null;let currentPage=1;const ITEMS_PER_PAGE=10;
let ordreLots={col:'',asc:true};
const $=id=>document.getElementById(id);

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

function ordenarLots(col){
    if(ordreLots.col===col)ordreLots.asc=!ordreLots.asc;
    else{ordreLots.col=col;ordreLots.asc=true;}
    document.querySelectorAll('.table-custom thead th i.fa-sort,.table-custom thead th i.fa-sort-up,.table-custom thead th i.fa-sort-down').forEach(i=>i.className='fas fa-sort');
    const icon=document.getElementById('sort-'+col);
    if(icon)icon.className='fas fa-sort-'+(ordreLots.asc?'down':'up');
    lotsData.sort((a,b)=>{
        let va=a[col],vb=b[col];
        if(col==='producte'){va=a.catalogo?.nombre||'';vb=b.catalogo?.nombre||'';}
        if(col==='albara'){va=a.albara?.numAlbara||'';vb=b.albara?.numAlbara||'';}
        if(col==='dataAlbara'){va=a.albara?.data||'';vb=b.albara?.data||'';}
        if(col==='quantitat')return ordreLots.asc?(va||0)-(vb||0):(vb||0)-(va||0);
        va=(va||'').toString().toLowerCase();vb=(vb||'').toString().toLowerCase();
        return ordreLots.asc?va.localeCompare(vb):vb.localeCompare(va);
    });
    const filtre=document.querySelector('.filter-tab.active')?.getAttribute('data-filtre')||'tots';
    filtrarLots(filtre);
}

const msg=(m,t)=>{
    let a=document.createElement('div');
    a.className='alert alert-'+t+' position-fixed top-0 end-0 m-3';
    a.style.zIndex='99999';
    a.innerHTML=m+'<button type="button" class="btn-close" data-bs-dismiss="alert"></button>';
    document.body.appendChild(a);
    setTimeout(()=>a.remove(),3000);
};

async function cargarLots(){
    try{const r=await fetch('/api/lots');lotsData=await r.json();currentPage=1;carregarMateriesPrime();filtrarLots('tots');}
    catch(e){$('tablaLots').innerHTML='<tr><td colspan="9">Error</td></tr>';}
}

async function carregarMateriesPrime() {
    try {
        const r = await fetch('/api/materies-prime');
        const d = await r.json();
        const sel = $('filterMateria');
        if (!sel) return;
        sel.innerHTML = '<option value="">Tots</option>';
        d.forEach(m => {
            const opt = document.createElement('option');
            opt.value = m.id;
            opt.textContent = m.nombre;
            sel.appendChild(opt);
        });
    } catch (e) { console.error('Error carregant matèries primeres', e); }
}

function netejarFiltres() {
    $('filterIdLot').value = '';
    $('filterEstat').value = '';
    $('filterMateria').value = '';
    $('filterData').value = '';
    currentPage = 1;
    filtrarLots('tots');
}

function aplicarFiltres() {
    currentPage = 1;
    filtrarLots('tots');
}

function filtrarPerEstat(estat) {
    $('filterEstat').value = estat === 'tots' ? '' : estat;
    currentPage = 1;
    filtrarLots();
}
function filtrarLots(filtre){
    document.querySelectorAll('.filter-tab').forEach(t=>t.classList.remove('active'));
    const tab=document.querySelector('[data-filtre="'+(filtre||'tots')+'"]');
    if(tab)tab.classList.add('active');
    let f=[...lotsData];
    if(filtre&&filtre!=='tots')f=f.filter(l=>l.estat===filtre);

    const idLotFilter = ($('filterIdLot')?.value||'').toLowerCase().trim();
    const estatFilter = $('filterEstat')?.value || '';
    const materiaFilter = $('filterMateria')?.value || '';
    const dataFilter = $('filterData')?.value || '';

    if(idLotFilter) {
        f=f.filter(l=>l.idLot && l.idLot.toString().toLowerCase().includes(idLotFilter));
    }
    if(estatFilter) {
        f=f.filter(l=>l.estat === estatFilter);
    }
    if(materiaFilter) {
        f=f.filter(l=>l.catalogo?.id && l.catalogo.id.toString() === materiaFilter);
    }
    if(dataFilter) {
        f=f.filter(l=>{
            if(!l.dataObertura) return false;
            return l.dataObertura.startsWith(dataFilter);
        });
    }

    mostrarLots(f);
}
function mostrarLots(lots){
    const tbody=$('tablaLots');
    if(!lots||!lots.length){tbody.innerHTML='<tr><td colspan="9">No hi ha lots</td></tr>';renderPagination(0);return;}
    const start=(currentPage-1)*ITEMS_PER_PAGE,end=start+ITEMS_PER_PAGE;const paginated=lots.slice(start,end);
    if(!paginated.length){currentPage=1;filtrarLots(document.querySelector('.filter-tab.active')?.getAttribute('data-filtre')||'tots');return;}
    let html='';
    paginated.forEach(l=>{
        let badge='badge-en-estoc';
        if(l.estat==='OBERT')badge='badge-obert';
        else if(l.estat==='ACABAT')badge='badge-acabat';
        else if(l.estat==='CONSUMIT')badge='badge-consumit';
        else if(l.estat==='CADUCAT')badge='badge-caducat';
        else if(l.estat==='REBUTJAT')badge='badge-rebutjat';
        let a='';
        const lotKey=l.nifProveidor+'___'+l.idLot;
        if(l.estat==='EN_ESTOC'){
            a='<button class="btn-success btn-action" onclick="iniciarLot(\''+lotKey+'\')" title="Iniciar"><i class="fas fa-play"></i> Iniciar</button>';
            a+='<button class="btn-danger btn-action" onclick="eliminarLot(\''+lotKey+'\')" title="Eliminar"><i class="fas fa-trash"></i> Eliminar</button>';
        }else if(l.estat==='OBERT'){
            a='<button class="btn-danger btn-action" onclick="finalitzarLot(\''+lotKey+'\')" title="Finalitzar"><i class="fas fa-stop"></i> Finalitzar</button>';
        }
        if(l.teProduccio)a+='<button class="btn-info btn-action" onclick="veureProduccio(\''+lotKey+'\')" title="Producció"><i class="fas fa-industry"></i> Producció</button>';
        html+='<tr><td>'+l.idLot+'</td><td><strong>'+l.numLot+'</strong></td><td>'+(l.catalogo?.nombre||'-')+'</td><td>'+l.quantitat+' '+(l.unitat||'kg')+'</td><td><span class="badge-lot '+badge+'">'+l.estat+'</span></td><td>'+formatDate(l.dataObertura)+'</td><td>'+formatDate(l.dataAcabament)+'</td><td>'+(l.albara?.numAlbara||'-')+'</td><td style="white-space:nowrap;text-align:center;">'+a+'</td></tr>';
    });
    tbody.innerHTML=html;renderPagination(lots.length);
}
function trobarLotPerClau(clau){
    const [nif,idLot]=clau.split('___');
    return lotsData.find(l=>l.nifProveidor===nif&&l.idLot===idLot);
}
function iniciarLot(clau){
    const l=trobarLotPerClau(clau);if(!l)return;lotSel=l;acciPendent='iniciar';
    let h='<i class="fas fa-play-circle" style="font-size:48px;color:#28a745;"></i><h3>Iniciar Lot</h3><p><strong>'+l.numLot+'</strong> - '+(l.catalogo?.nombre||'')+'</p>';
    const o=lotsData.filter(x=>x.catalogo?.id===l.catalogo?.id&&x.estat==='OBERT');
    if(o.length)h+='<p style="color:#dc3545;">⚠️ '+o.length+' lot(s) obert(s). Es tancaran.</p>';
    $('confirmContent').innerHTML=h;
    $('btnConfirmarAccio').style.background='#28a745';
    $('btnConfirmarAccio').textContent='Iniciar Lot';
    $('confirmModal').style.display='flex';
}
function finalitzarLot(clau){
    const l=trobarLotPerClau(clau);if(!l)return;lotSel=l;acciPendent='finalitzar';
    $('confirmContent').innerHTML='<i class="fas fa-stop-circle" style="font-size:48px;color:#dc3545;"></i><h3>Finalitzar Lot</h3><p><strong>'+l.numLot+'</strong> - '+(l.catalogo?.nombre||'')+'</p><p>Quedara <strong>ACABAT</strong>.</p>';
    $('btnConfirmarAccio').style.background='#dc3545';
    $('btnConfirmarAccio').textContent='Finalitzar Lot';
    $('confirmModal').style.display='flex';
}
async function confirmarAccio(){
    if(!lotSel||!acciPendent)return;
    try{
        let r;
        const nif = encodeURIComponent(lotSel.nifProveidor);
        const idLot = encodeURIComponent(lotSel.idLot);
        if(acciPendent==='eliminar'){
            r=await fetch('/api/lots/'+nif+'?idLot='+idLot,{method:'DELETE'});
        }else{
            r=await fetch('/api/lots/'+nif+'/'+acciPendent+'?idLot='+idLot,{method:'PUT'});
        }
        const d=await r.json();
        if(r.ok){msg(d.message,'success');cargarLots();}
        else{msg(d.error||'Error','danger');}
    }catch(e){msg('Error de connexió','danger');}
    $('confirmModal').style.display='none';acciPendent=null;lotSel=null;
}
function eliminarLot(clau){
    const l=trobarLotPerClau(clau);if(!l)return;lotSel=l;acciPendent='eliminar';
    $('confirmContent').innerHTML='<i class="fas fa-trash-alt" style="font-size:48px;color:#dc3545;"></i><h3>Eliminar Lot</h3><p><strong>'+l.numLot+'</strong> - '+(l.catalogo?.nombre||'')+'</p><p style="color:#ef4444;font-size:13px;margin-top:10px;">Aquesta acció no es pot desfer.</p>';
    $('btnConfirmarAccio').style.background='#dc3545';
    $('btnConfirmarAccio').textContent='Eliminar';
    $('confirmModal').style.display='flex';
}
function tancarModal(){
    $('confirmModal').style.display='none';acciPendent=null;lotSel=null;
}
function toggleUserMenu(){const m=$('userMenu');if(m)m.style.display=m.style.display==='none'?'block':'none';}
document.addEventListener('click',function(e){const m=$('userMenu'),d=document.querySelector('.user-profile');if(m&&d&&!d.contains(e.target)&&!m.contains(e.target))m.style.display='none';});
document.addEventListener('DOMContentLoaded',cargarLots);

async function veureProduccio(clau){
    const [nif,idLot]=clau.split('___');
    const body=$('produccioBody');
    body.innerHTML='<tr><td colspan="5" class="text-center">Carregant...</td></tr>';
    const modal=new bootstrap.Modal($('produccioModal'));
    modal.show();
    try{
        const r=await fetch('/api/lots/'+encodeURIComponent(nif)+'/'+encodeURIComponent(idLot)+'/produccio');
        const data=await r.json();
        const produccioBody=$('produccioBody');
        if(!data||data.length===0){
            produccioBody.innerHTML='<tr><td colspan="5" class="text-center">Cap producció associada</td></tr>';
            return;
        }
        produccioBody.innerHTML=data.map(p=>'<tr><td>'+p.producte+'</td><td>'+p.quantitat+'</td><td>'+p.client+'</td><td>'+p.nifClient+'</td><td>'+p.dataAlbara+'</td></tr>').join('');
    }catch(e){
        body.innerHTML='<tr><td colspan="5" class="text-center text-danger">Error en carregar la producció</td></tr>';
    }
}

function resetPage() { currentPage = 1; }
// ========== PAGINACIÓ ==========
function renderPagination(totalItems) {
    const totalPages = Math.ceil(totalItems / ITEMS_PER_PAGE);
    let tableWrapper = document.querySelector('.table-container');
    if (!tableWrapper) {
        const table = document.getElementById('tablaLots');
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
    const filtre = document.querySelector('.filter-tab.active')?.getAttribute('data-filtre') || 'tots';
    filtrarLots(filtre);
}
