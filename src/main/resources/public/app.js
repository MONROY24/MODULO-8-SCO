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

    let chartInstance = null;
    let selectedEmpresaId = null;

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
        const ctx = document.getElementById('predictionChart').getContext('2d');
        
        if (chartInstance) {
            chartInstance.destroy();
        }

        const labels = historial.map(h => `${h.anio}-${String(h.mesNumero).padStart(2, '0')}`);
        labels.push('PROY.');

        const dataVentas = historial.map(h => h.totalVentas);
        dataVentas.push(ventasPred);

        const dataCompras = historial.map(h => h.totalCompras);
        dataCompras.push(comprasPred);

        // Reset chart colors to light theme
        Chart.defaults.color = '#64748B';

        chartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Ventas ($)',
                        data: dataVentas,
                        borderColor: '#16A34A',
                        backgroundColor: '#16A34A',
                        borderWidth: 2,
                        pointRadius: 4,
                        fill: false,
                        tension: 0.1
                    },
                    {
                        label: 'Compras ($)',
                        data: dataCompras,
                        borderColor: '#DC2626',
                        backgroundColor: '#DC2626',
                        borderWidth: 2,
                        pointRadius: 4,
                        fill: false,
                        tension: 0.1
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'top', labels: { boxWidth: 12, font: { size: 10 } } }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: { font: { size: 10 }, callback: value => '$' + value.toLocaleString() }
                    },
                    x: {
                        ticks: { font: { size: 10 } }
                    }
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
