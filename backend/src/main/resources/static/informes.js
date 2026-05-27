let chartInstance = null;

const $ = id => document.getElementById(id);

function getLang() {
    return document.documentElement.lang || 'ca';
}

const i18n = {
    es: {
        mesos: { year: 'numeric', month: 'long' },
        productes: 'Todos los productos',
        total: 'Total unidades vendidas',
        productesDiferents: 'productos diferentes',
        error: 'Error cargando datos',
        dia: 'Día',
        unitatsVenudes: 'Unidades vendidas',
        unitats: 'Unidades',
        diaDelMes: 'Día del mes'
    },
    ca: {
        mesos: { year: 'numeric', month: 'long' },
        productes: 'Tots els productes',
        total: 'Total unitats venudes',
        productesDiferents: 'productes diferents',
        error: 'Error carregant dades',
        dia: 'Dia',
        unitatsVenudes: 'Unitats venudes',
        unitats: 'Unitats',
        diaDelMes: 'Dia del mes'
    }
};

function t(key) {
    const lang = getLang();
    return i18n[lang]?.[key] ?? i18n.ca[key];
}

function toggleUserMenu() {
    const m = $('userMenu');
    if (m) m.style.display = m.style.display === 'none' ? 'block' : 'none';
}

document.addEventListener('click', e => {
    const m = $('userMenu'), d = document.querySelector('.user-profile');
    if (m && d && !d.contains(e.target) && !m.contains(e.target)) m.style.display = 'none';
});

function omplirSelectors() {
    const selMes = $('selMes');
    const ara = new Date();
    const lang = getLang();
    const locale = lang === 'es' ? 'es-ES' : 'ca-ES';
    for (let i = 0; i < 12; i++) {
        const d = new Date(ara.getFullYear(), ara.getMonth() - i, 1);
        const val = d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0');
        const txt = d.toLocaleDateString(locale, t('mesos'));
        const opt = document.createElement('option');
        opt.value = val;
        opt.textContent = txt;
        if (i === 0) opt.selected = true;
        selMes.appendChild(opt);
    }

    fetch('/api/informes/productes')
        .then(r => r.json())
        .then(data => {
            const sel = $('selProducte');
            data.forEach(p => {
                const opt = document.createElement('option');
                opt.value = p.id;
                opt.textContent = p.nombre;
                sel.appendChild(opt);
            });
        })
        .catch(() => {});
}

async function carregarGrafic() {
    const [year, month] = $('selMes').value.split('-');
    const producteId = $('selProducte').value;

    let url = `/api/informes/venuts?year=${year}&month=${parseInt(month)}`;
    if (producteId) url += `&producteId=${producteId}`;

    try {
        const r = await fetch(url);
        const data = await r.json();

        $('totalResum').textContent =
            t('total') + ': ' + data.totalMes.toFixed(2) +
            ' (' + data.productesTrobats + ' ' + t('productesDiferents') + ')';

        dibuixarGrafic(data.dies);
    } catch (e) {
        $('totalResum').textContent = t('error');
    }
}

function dibuixarGrafic(dies) {
    const ctx = $('graficVendes').getContext('2d');

    if (chartInstance) chartInstance.destroy();

    chartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: dies.map(d => t('dia') + ' ' + d.dia),
            datasets: [{
                label: t('unitatsVenudes'),
                data: dies.map(d => d.total),
                borderColor: '#A0522D',
                backgroundColor: 'rgba(160,82,45,0.1)',
                fill: true,
                tension: 0.3,
                pointRadius: 4,
                pointBackgroundColor: '#A0522D'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: true, position: 'top' }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: { display: true, text: t('unitats') }
                },
                x: {
                    title: { display: true, text: t('diaDelMes') }
                }
            }
        }
    });
}

document.addEventListener('DOMContentLoaded', () => {
    omplirSelectors();
    carregarGrafic();
});
