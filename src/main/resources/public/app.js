document.addEventListener('DOMContentLoaded', () => {
    const empresaSelect = document.getElementById('empresaSelect');
    const btnAnalizar = document.getElementById('btnAnalizar');
    const btnExportar = document.getElementById('btnExportar');
    const errorToast = document.getElementById('errorToast');
    
    // KPI Elements
    const valVentas = document.getElementById('valVentas');
    const valCompras = document.getElementById('valCompras');
    const valUtilidad = document.getElementById('valUtilidad');
    const valTendencia = document.getElementById('valTendencia');
    const valConfianza = document.getElementById('valConfianza');
    
    // Table
    const historialTable = document.getElementById('historialTable').querySelector('tbody');

    let chartLine = null;
    let chartArea = null;
    let chartBar = null;
    let chartDoughnut = null;
    let selectedEmpresaId = null;

    // Splash Screen Logic
    const splashScreen = document.getElementById('splash-screen');
    const progressBar = document.getElementById('progress-bar');
    const progressText = document.getElementById('progress-text');
    let progress = 0;
    const loadingInterval = setInterval(() => {
        progress += Math.floor(Math.random() * 15) + 10; 
        if (progress > 100) progress = 100;
        if(progressBar) progressBar.style.width = `${progress}%`;
        if(progressText) progressText.textContent = `${progress}%`;
        if (progress === 100) {
            clearInterval(loadingInterval);
            setTimeout(() => {
                if(splashScreen) splashScreen.style.opacity = '0';
                setTimeout(() => { if(splashScreen) splashScreen.style.display = 'none'; }, 500);
            }, 300);
        }
    }, 150);

    // Theme Logic
    const themeBtns = document.querySelectorAll('.theme-btn');
    const rootEl = document.documentElement;
    const currentTheme = localStorage.getItem('theme') || 'dark-gray';
    
    function setTheme(themeName) {
        rootEl.setAttribute('data-theme', themeName);
        localStorage.setItem('theme', themeName);
        
        themeBtns.forEach(b => b.classList.remove('active'));
        const btn = document.querySelector(`.theme-btn[data-theme="${themeName}"]`);
        if(btn) btn.classList.add('active');

        updateChartTheme();
    }

    function updateChartTheme() {
        const isDark = rootEl.getAttribute('data-theme') && rootEl.getAttribute('data-theme').startsWith('dark');
        const color = isDark ? '#94A3B8' : '#64748B';
        Chart.defaults.color = color;
        
        const charts = [chartLine, chartArea, chartBar, chartDoughnut];
        charts.forEach(chart => {
            if (chart) {
                if (chart.options.scales && chart.options.scales.x) {
                    chart.options.scales.x.ticks.color = color;
                    chart.options.scales.y.ticks.color = color;
                    const gridColor = isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)';
                    chart.options.scales.x.grid.color = gridColor;
                    chart.options.scales.y.grid.color = gridColor;
                }
                if (chart.options.plugins && chart.options.plugins.legend) {
                    chart.options.plugins.legend.labels.color = color;
                }
                chart.update();
            }
        });
    }

    setTheme(currentTheme);

    themeBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            setTheme(btn.getAttribute('data-theme'));
        });
    });

    // Load Companies
    fetch('/api/empresas')
        .then(response => {
            if (!response.ok) throw new Error('Error al cargar empresas');
            return response.json();
        })
        .then(empresas => {
            empresaSelect.innerHTML = '<option value="">-- Seleccione Empresa --</option>';
            empresas.forEach(emp => {
                const option = document.createElement('option');
                option.value = emp.id;
                option.textContent = emp.nombre;
                empresaSelect.appendChild(option);
            });
        })
        .catch(err => showError("Error DB: Configure DB_URL en Render. " + err.message));

    // Handle Selection
    empresaSelect.addEventListener('change', (e) => {
        selectedEmpresaId = e.target.value;
        if (selectedEmpresaId) {
            btnAnalizar.disabled = false;
        } else {
            btnAnalizar.disabled = true;
            btnExportar.disabled = true;
        }
    });

    const formatMoney = (amount) => {
        return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
    };

    // Execute Analysis
    btnAnalizar.addEventListener('click', async () => {
        if (!selectedEmpresaId) return;

        btnAnalizar.disabled = true;
        const originalText = btnAnalizar.innerHTML;
        btnAnalizar.innerHTML = '<span class="btn-text">⏳ Procesando IA...</span>';
        errorToast.classList.add('hidden');

        try {
            const response = await fetch(`/api/analisis/${selectedEmpresaId}`);
            if (!response.ok) {
                const errData = await response.json();
                throw new Error(errData.error || 'Error desconocido');
            }
            
            const data = await response.json();
            renderResults(data);
            btnExportar.disabled = false;
        } catch (error) {
            showError(error.message);
        } finally {
            btnAnalizar.innerHTML = originalText;
            btnAnalizar.disabled = false;
        }
    });

    // Export PDF
    btnExportar.addEventListener('click', () => {
        if (!selectedEmpresaId) return;
        window.open(`/api/exportar-pdf/${selectedEmpresaId}`, '_blank');
    });

    function renderResults(data) {
        // Update KPIs
        valVentas.textContent = formatMoney(data.ventasPredichas);
        valCompras.textContent = formatMoney(data.comprasPredichas);
        valUtilidad.textContent = formatMoney(data.utilidadProyectada);
        valUtilidad.style.color = data.utilidadProyectada >= 0 ? 'var(--success)' : 'var(--danger)';

        const varPct = data.tendenciaPorcentaje.toFixed(2);
        valTendencia.textContent = `${varPct > 0 ? '+' : ''}${varPct}% (${data.clasificacionTendencia})`;
        valTendencia.style.color = varPct > 0 ? 'var(--success)' : (varPct < 0 ? 'var(--danger)' : 'var(--text-main)');

        valConfianza.textContent = data.nivelConfianza;

        // Render Table
        historialTable.innerHTML = '';
        data.historial.forEach(h => {
            const utilidad = h.totalVentas - h.totalCompras;
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${h.anio}</td>
                <td>${h.mesNumero}</td>
                <td>${formatMoney(h.totalVentas)}</td>
                <td>${formatMoney(h.totalCompras)}</td>
                <td style="color: ${utilidad >= 0 ? 'var(--success)' : 'var(--danger)'}; font-weight: bold;">
                    ${formatMoney(utilidad)}
                </td>
            `;
            historialTable.appendChild(tr);
        });

        // Render Chart
        renderChart(data.historial, data.ventasPredichas, data.comprasPredichas);
    }

    function renderChart(historial, ventasPred, comprasPred) {
        if (chartLine) chartLine.destroy();
        if (chartArea) chartArea.destroy();
        if (chartBar) chartBar.destroy();
        if (chartDoughnut) chartDoughnut.destroy();

        const isDark = rootEl.getAttribute('data-theme') && rootEl.getAttribute('data-theme').startsWith('dark');
        const color = isDark ? '#94A3B8' : '#64748B';
        const gridColor = isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)';
        Chart.defaults.color = color;

        const labels = historial.map(h => `${h.anio}-${String(h.mesNumero).padStart(2, '0')}`);
        labels.push('PROY.');

        const dataVentas = historial.map(h => h.totalVentas);
        dataVentas.push(ventasPred);

        const dataCompras = historial.map(h => h.totalCompras);
        dataCompras.push(comprasPred);

        const chartOptions = {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'top', labels: { boxWidth: 12, font: { size: 10 }, color: color } }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { font: { size: 10 }, callback: value => '$' + value.toLocaleString(), color: color },
                    grid: { color: gridColor }
                },
                x: {
                    ticks: { font: { size: 10 }, color: color },
                    grid: { color: gridColor }
                }
            }
        };

        // Gráfico de Líneas (Tendencia)
        const ctxLine = document.getElementById('chartLine').getContext('2d');
        chartLine = new Chart(ctxLine, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    { label: 'Ventas ($)', data: dataVentas, borderColor: '#16A34A', backgroundColor: '#16A34A', borderWidth: 2, pointRadius: 4, fill: false, tension: 0.1 },
                    { label: 'Compras ($)', data: dataCompras, borderColor: '#DC2626', backgroundColor: '#DC2626', borderWidth: 2, pointRadius: 4, fill: false, tension: 0.1 }
                ]
            },
            options: chartOptions
        });

        // Gráfico de Área (Utilidad Neta)
        const dataUtilidad = dataVentas.map((v, i) => v - dataCompras[i]);
        const ctxArea = document.getElementById('chartArea').getContext('2d');
        chartArea = new Chart(ctxArea, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Utilidad Neta ($)',
                        data: dataUtilidad,
                        borderColor: '#3B82F6',
                        backgroundColor: 'rgba(59, 130, 246, 0.3)',
                        borderWidth: 2,
                        pointRadius: 4,
                        fill: true,
                        tension: 0.2
                    }
                ]
            },
            options: chartOptions
        });

        // Calcular históricos sin predicción para barras y donas
        const histVentas = historial.map(h => h.totalVentas).reduce((a, b) => a + b, 0);
        const histCompras = historial.map(h => h.totalCompras).reduce((a, b) => a + b, 0);

        // Gráfico de Barras
        const ctxBar = document.getElementById('chartBar').getContext('2d');
        chartBar = new Chart(ctxBar, {
            type: 'bar',
            data: {
                labels: ['Ventas Totales', 'Compras Totales'],
                datasets: [{
                    label: 'Monto Acumulado ($)',
                    data: [histVentas, histCompras],
                    backgroundColor: ['#16A34A', '#DC2626'],
                    borderRadius: 4
                }]
            },
            options: chartOptions
        });

        // Gráfico de Dona
        const ctxDoughnut = document.getElementById('chartDoughnut').getContext('2d');
        chartDoughnut = new Chart(ctxDoughnut, {
            type: 'doughnut',
            data: {
                labels: ['Ingresos (Ventas)', 'Egresos (Compras)'],
                datasets: [{
                    data: [histVentas, histCompras],
                    backgroundColor: ['#16A34A', '#DC2626'],
                    hoverOffset: 4,
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'right', labels: { boxWidth: 12, font: { size: 10 }, color: color } }
                }
            }
        });
    }

    function showError(msg) {
        errorToast.textContent = msg;
        errorToast.classList.remove('hidden');
        setTimeout(() => errorToast.classList.add('hidden'), 6000);
    }
});
