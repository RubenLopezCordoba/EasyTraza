function $(id) { return document.getElementById(id); }

document.addEventListener('DOMContentLoaded', function() {
    carregarStats();
});
window.addEventListener('pageshow', function(e) {
    if (e.persisted) carregarStats();
});

function carregarStats() {
    fetch('/api/panel/stats?_=' + Date.now())
        .then(r => r.json())
        .then(stats => {
            $('statActiveClients').textContent = stats.activeClients || 0;
            $('statInactiveClients').textContent = stats.inactiveClients || 0;
            $('statSuppliers').textContent = stats.totalSuppliers || 0;
            $('statProducts').textContent = stats.totalProducts || 0;
            $('statIngredients').textContent = stats.totalIngredients || 0;
            $('statAlbProv').textContent = stats.totalAlbaransProveidor || 0;
            $('statAlbClient').textContent = stats.totalAlbaransClient || 0;
            $('statTotalLots').textContent = stats.totalLots || 0;
            $('statLotsEnEstoc').textContent = stats.lotsEnEstoc || 0;
            $('statPh').textContent = stats.latestPh != null ? stats.latestPh : '-';
            $('statUsers').textContent = stats.totalUsers || 0;

            $('phReminder').style.display = stats.phWeekCompleted ? 'none' : 'flex';
        })
        .catch(e => { console.error('Error carregant estadístiques:', e); });
}