document.addEventListener('DOMContentLoaded', () => {
    const empresaSelect = document.getElementById('empresaSelect');
    const btnAnalizar = document.getElementById('btnAnalizar');
    const btnExportar = document.getElementById('btnExportar');
    const resultsSection = document.getElementById('resultsSection');
    const errorToast = document.getElementById('errorToast');
    
    // KPI Elements
    const valVentas = document.getElementById('valVentas');
    const trendVentas = document.getElementById('trendVentas');
    const valCompras = document.getElementById('valCompras');
    const valUtilidad = document.getElementById('valUtilidad');
    const valConfianza = document.getElementById('valConfianza');
    
    let chartInstance = null;
    let selectedEmpresaId = null;

    // Load Companies
    fetch('/api/empresas')
        .then(response => {
            if (!response.ok) throw new Error('Error al cargar empresas');
            return response.json();
        })
        .then(empresas => {
            empresaSelect.innerHTML = '<option value="">-- Seleccione una Empresa --</option>';
            empresas.forEach(emp => {
                const option = document.createElement('option');
                option.value = emp.id;
                option.textContent = emp.nombre;
                empresaSelect.appendChild(option);
            });
        })
        .catch(err => showError("No se pudo conectar al servidor de BD. " + err.message));

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

    // Format Currency
    const formatMoney = (amount) => {
        return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
    };

    // Execute Analysis
    btnAnalizar.addEventListener('click', async () => {
        if (!selectedEmpresaId) return;

        setLoading(true);
        resultsSection.classList.add('hidden');

        try {
            const response = await fetch(`/api/analisis/${selectedEmpresaId}`);
            if (!response.ok) {
                const errData = await response.json();
                throw new Error(errData.error || 'Error desconocido');
            }
            
            const data = await response.json();
            renderResults(data);
            btnExportar.disabled = false;
            resultsSection.classList.remove('hidden');
        } catch (error) {
            showError(error.message);
        } finally {
            setLoading(false);
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
        
        // Utilidad Color
        valUtilidad.style.color = data.utilidadProyectada >= 0 ? 'var(--success)' : 'var(--danger)';

        // Trend
        const varPct = data.tendenciaPorcentaje.toFixed(2);
        trendVentas.textContent = `${varPct > 0 ? '+' : ''}${varPct}% vs mes anterior`;
        trendVentas.className = 'kpi-trend ' + (varPct > 0 ? 'trend-up' : (varPct < 0 ? 'trend-down' : 'trend-stable'));

        // Confidence
        valConfianza.textContent = data.nivelConfianza;
        let confClass = 'conf-badge ';
        if (data.nivelConfianza === 'Alto') confClass += 'conf-high';
        else if (data.nivelConfianza === 'Medio') confClass += 'conf-medium';
        else confClass += 'conf-low';
        valConfianza.className = confClass;

        // Render Chart
        renderChart(data.historial, data.ventasPredichas, data.comprasPredichas);
    }

    function renderChart(historial, ventasPred, comprasPred) {
        const ctx = document.getElementById('predictionChart').getContext('2d');
        
        if (chartInstance) {
            chartInstance.destroy();
        }

        const labels = historial.map(h => `Mes ${h.mesAnio}`);
        labels.push('PROYECCIÓN');

        const dataVentas = historial.map(h => h.totalVentas);
        dataVentas.push(ventasPred);

        const dataCompras = historial.map(h => h.totalCompras);
        dataCompras.push(comprasPred);

        // Chart.js defaults for dark mode
        Chart.defaults.color = '#94a3b8';
        Chart.defaults.borderColor = 'rgba(255,255,255,0.1)';

        chartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Ventas ($)',
                        data: dataVentas,
                        borderColor: '#10b981',
                        backgroundColor: 'rgba(16, 185, 129, 0.1)',
                        borderWidth: 3,
                        pointBackgroundColor: '#10b981',
                        pointBorderColor: '#fff',
                        pointRadius: 5,
                        fill: true,
                        tension: 0.4
                    },
                    {
                        label: 'Compras ($)',
                        data: dataCompras,
                        borderColor: '#ec4899',
                        backgroundColor: 'rgba(236, 72, 153, 0.1)',
                        borderWidth: 3,
                        pointBackgroundColor: '#ec4899',
                        pointBorderColor: '#fff',
                        pointRadius: 5,
                        fill: true,
                        tension: 0.4
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                        labels: { font: { family: 'Inter', size: 14 } }
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false,
                        backgroundColor: 'rgba(15, 23, 42, 0.9)',
                        titleFont: { size: 14 },
                        bodyFont: { size: 13 },
                        padding: 12,
                        cornerRadius: 8
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: { drawBorder: false },
                        ticks: {
                            callback: function(value) {
                                return '$' + value.toLocaleString();
                            }
                        }
                    },
                    x: {
                        grid: { display: false }
                    }
                }
            }
        });
    }

    function setLoading(isLoading) {
        const textSpan = btnAnalizar.querySelector('.btn-text');
        const loader = btnAnalizar.querySelector('.loader');
        
        if (isLoading) {
            btnAnalizar.disabled = true;
            textSpan.classList.add('hidden');
            loader.classList.remove('hidden');
        } else {
            btnAnalizar.disabled = false;
            textSpan.classList.remove('hidden');
            loader.classList.add('hidden');
        }
    }

    function showError(msg) {
        errorToast.textContent = msg;
        errorToast.classList.remove('hidden');
        setTimeout(() => {
            errorToast.classList.add('hidden');
        }, 5000);
    }
});
