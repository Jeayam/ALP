// ---- DATABASE ENGINE ----
const DB = {
  getRecords() {
    return JSON.parse(localStorage.getItem('energiku_records')) || this.getSeeds();
  },
  saveRecords(records) {
    localStorage.setItem('energiku_records', JSON.stringify(records));
  },
  getSettings() {
    return JSON.parse(localStorage.getItem('energiku_settings')) || { tarifListrik: 1444, hargaBBM: 10000, tarifAir: 7500 };
  },
  saveSettings(settings) {
    localStorage.setItem('energiku_settings', JSON.stringify(settings));
  },
  getTargets() {
    return JSON.parse(localStorage.getItem('energiku_targets')) || { listrik: 200, bbm: 50, air: 20 };
  },
  saveTargets(targets) {
    localStorage.setItem('energiku_targets', JSON.stringify(targets));
  },
  getSeeds() {
    return [
      { tanggal: '2026-05-28', listrik: 8.5, bbm: 2.5, air: 0.6, biaya: 41774, co2: 12.3 },
      { tanggal: '2026-05-29', listrik: 9.2, bbm: 3.0, air: 0.8, biaya: 49284, co2: 14.1 },
      { tanggal: '2026-05-30', listrik: 7.8, bbm: 1.5, air: 0.5, biaya: 30013, co2: 9.8 },
      { tanggal: '2026-05-31', listrik: 10.5, bbm: 4.0, air: 1.1, biaya: 63412, co2: 17.5 },
      { tanggal: '2026-06-01', listrik: 8.0, bbm: 2.0, air: 0.7, biaya: 36802, co2: 11.4 }
    ];
  }
};

// ---- GLOBAL ALERTS & TOASTS ----
function showToast(message, type = 'success') {
  const container = document.getElementById('toastContainer');
  if(!container) return;
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `<i class="fas fa-circle-check"></i> <span>${message}</span>`;
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 3000);
}