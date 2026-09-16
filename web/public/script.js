document.addEventListener('DOMContentLoaded', () => {
  const themes = {
    ember: {
      accent: '#FF6B1A',
      glow: 'rgba(255, 107, 26, 0.35)',
      badge: '⚠️ PING SPIKE'
    },
    tactical: {
      accent: '#00D4FF',
      glow: 'rgba(0, 212, 255, 0.35)',
      badge: '📡 NETWORK DELTA'
    },
    toxic: {
      accent: '#39FF14',
      glow: 'rgba(57, 255, 20, 0.35)',
      badge: '⚡ LATENCY SURGE'
    },
    void: {
      accent: '#B026FF',
      glow: 'rgba(176, 38, 255, 0.35)',
      badge: '🔮 VOID SPIKE'
    },
    white: {
      accent: '#FFFFFF',
      glow: 'rgba(255, 255, 255, 0.45)',
      badge: '⚡ PING SPIKE'
    }
  };

  const themeBtns = document.querySelectorAll('.theme-btn');
  const mockBanner = document.getElementById('mockBanner');
  const mockPing = document.getElementById('mockPing');
  const bannerBadge = mockBanner.querySelector('.banner-badge');
  const triggerSpikeBtn = document.getElementById('triggerSpike');

  let currentTheme = 'ember';

  function applyTheme(name) {
    const t = themes[name];
    if (!t) return;
    currentTheme = name;
    
    mockBanner.style.borderColor = t.accent;
    mockBanner.style.boxShadow = `0 0 24px ${t.glow}`;
    bannerBadge.style.color = t.accent;
    bannerBadge.textContent = t.badge;

    themeBtns.forEach(btn => {
      btn.classList.toggle('active', btn.dataset.theme === name);
    });
  }

  themeBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      applyTheme(btn.dataset.theme);
    });
  });

  // Interactive Spike Simulation
  let isSpiking = false;
  function simulateSpike() {
    if (isSpiking) return;
    isSpiking = true;

    // Spike current ping
    const initialPing = 28;
    const spikeDelta = Math.floor(Math.random() * 120) + 90; // +90 to +210ms
    const totalPing = initialPing + spikeDelta;

    mockPing.style.color = themes[currentTheme].accent;
    mockPing.style.borderColor = themes[currentTheme].accent;
    mockPing.innerHTML = `📡 <span style="color:${themes[currentTheme].accent}">${totalPing} ms</span>`;

    const bannerText = mockBanner.querySelector('.banner-text');
    bannerText.textContent = `+${spikeDelta} ms (${totalPing} ms)`;
    mockBanner.style.transform = 'scale(1.08)';

    setTimeout(() => {
      mockBanner.style.transform = 'scale(1)';
    }, 200);

    setTimeout(() => {
      mockPing.style.color = '#FFFFFF';
      mockPing.style.borderColor = 'var(--border-light)';
      mockPing.innerHTML = `📡 28 ms`;
      bannerText.textContent = `+145 ms (173 ms)`;
      isSpiking = false;
    }, 2800);
  }

  if (triggerSpikeBtn) {
    triggerSpikeBtn.addEventListener('click', simulateSpike);
  }
});
