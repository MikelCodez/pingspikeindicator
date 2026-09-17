document.addEventListener('DOMContentLoaded', () => {
  // Theme Switching Logic
  const themes = {
    ember: {
      accent: '#FF6B1A',
      glow: 'rgba(255, 107, 26, 0.45)',
      badge: '⚠️ PING SPIKE'
    },
    tactical: {
      accent: '#00D4FF',
      glow: 'rgba(0, 212, 255, 0.45)',
      badge: '📡 NETWORK DELTA'
    },
    toxic: {
      accent: '#39FF14',
      glow: 'rgba(57, 255, 20, 0.45)',
      badge: '⚡ LATENCY SURGE'
    },
    void: {
      accent: '#B026FF',
      glow: 'rgba(176, 38, 255, 0.45)',
      badge: '🔮 VOID SPIKE'
    },
    white: {
      accent: '#FFFFFF',
      glow: 'rgba(255, 255, 255, 0.65)',
      badge: '⚡ PING SPIKE'
    }
  };

  const themeBtns = document.querySelectorAll('.theme-btn');
  const mockBanner = document.getElementById('mockBanner');
  const mockPing = document.getElementById('mockPing');
  const bannerBadge = mockBanner.querySelector('.banner-badge');
  const triggerSpikeBtn = document.getElementById('triggerSpike');
  const hudWrapper = document.querySelector('.hud-mock-wrapper');

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
      const isActive = btn.dataset.theme === name;
      btn.classList.toggle('active', isActive);
      if (isActive) {
        btn.style.setProperty('--accent-glow', t.glow);
      }
    });

    if (currentTheme === 'white') {
      mockPing.style.borderColor = '#FFFFFF';
    } else {
      mockPing.style.borderColor = 'var(--border-light)';
    }
  }

  themeBtns.forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      applyTheme(btn.dataset.theme);
    });
  });

  // Interactive Spike Simulation
  let isSpiking = false;
  function simulateSpike() {
    if (isSpiking) return;
    isSpiking = true;

    const initialPing = 28;
    const spikeDelta = Math.floor(Math.random() * 120) + 90; // +90 to +210ms
    const totalPing = initialPing + spikeDelta;

    const theme = themes[currentTheme];
    mockPing.style.color = theme.accent;
    mockPing.style.borderColor = theme.accent;
    mockPing.style.boxShadow = `0 0 16px ${theme.glow}`;
    mockPing.innerHTML = `<span class="ping-icon">📡</span> <span style="color:${theme.accent}">${totalPing} ms</span>`;

    const bannerText = mockBanner.querySelector('.banner-text');
    bannerText.textContent = `+${spikeDelta} ms (${totalPing} ms)`;
    bannerText.style.color = theme.accent === '#FFFFFF' ? '#FFFFFF' : theme.accent;

    mockBanner.style.transform = 'scale(1.08)';
    mockBanner.style.boxShadow = `0 0 36px ${theme.glow}`;

    setTimeout(() => {
      mockBanner.style.transform = 'scale(1)';
    }, 200);

    setTimeout(() => {
      mockPing.style.color = '#FFFFFF';
      mockPing.style.borderColor = currentTheme === 'white' ? '#FFFFFF' : 'var(--border-light)';
      mockPing.style.boxShadow = 'none';
      mockPing.innerHTML = `<span class="ping-icon">📡</span> 28 ms`;
      bannerText.textContent = `+145 ms (173 ms)`;
      bannerText.style.color = '#FFFFFF';
      mockBanner.style.boxShadow = `0 0 24px ${theme.glow}`;
      isSpiking = false;
    }, 2800);
  }

  if (triggerSpikeBtn) {
    triggerSpikeBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      simulateSpike();
    });
  }

  // Make mock banner and mock ping directly clickable to trigger spike
  if (mockBanner) {
    mockBanner.style.cursor = 'grab';
    mockBanner.setAttribute('title', 'Click to simulate spike, or drag to reposition');
    mockBanner.addEventListener('click', (e) => {
      if (!wasDragging) {
        simulateSpike();
      }
    });
  }

  if (mockPing) {
    mockPing.style.cursor = 'grab';
    mockPing.setAttribute('title', 'Click to simulate spike, or drag to reposition');
    mockPing.addEventListener('click', (e) => {
      if (!wasDragging) {
        simulateSpike();
      }
    });
  }

  // Interactive Drag & Drop Positioning inside HUD canvas
  let draggedEl = null;
  let dragOffsetX = 0;
  let dragOffsetY = 0;
  let wasDragging = false;

  function initDraggable(el) {
    el.addEventListener('mousedown', (e) => {
      if (e.button !== 0) return;
      draggedEl = el;
      wasDragging = false;
      el.style.cursor = 'grabbing';
      const rect = el.getBoundingClientRect();
      const parentRect = hudWrapper.getBoundingClientRect();
      dragOffsetX = e.clientX - rect.left;
      dragOffsetY = e.clientY - rect.top;
      e.preventDefault();
    });
  }

  if (mockBanner && hudWrapper) initDraggable(mockBanner);
  if (mockPing && hudWrapper) initDraggable(mockPing);

  window.addEventListener('mousemove', (e) => {
    if (!draggedEl || !hudWrapper) return;
    wasDragging = true;
    const parentRect = hudWrapper.getBoundingClientRect();
    let newX = e.clientX - parentRect.left - dragOffsetX;
    let newY = e.clientY - parentRect.top - dragOffsetY;

    // Clamp inside wrapper
    const maxX = parentRect.width - draggedEl.offsetWidth;
    const maxY = parentRect.height - draggedEl.offsetHeight;
    newX = Math.max(4, Math.min(newX, maxX - 4));
    newY = Math.max(4, Math.min(newY, maxY - 4));

    draggedEl.style.position = 'absolute';
    draggedEl.style.left = `${newX}px`;
    draggedEl.style.top = `${newY}px`;
    draggedEl.style.margin = '0';
  });

  window.addEventListener('mouseup', () => {
    if (draggedEl) {
      draggedEl.style.cursor = 'grab';
      draggedEl = null;
    }
  });

  // Version Control & Minecraft Target Filter Tabs
  const filterTabs = document.querySelectorAll('.filter-tab');
  const downloadCards = document.querySelectorAll('.download-card');
  const buildBoxes = document.querySelectorAll('.build-download-box');
  const versionSelect = document.getElementById('versionSelect');
  const releaseCards = document.querySelectorAll('.release-card');

  filterTabs.forEach(tab => {
    tab.addEventListener('click', () => {
      filterTabs.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');

      const target = tab.dataset.filter;

      // Filter download section cards
      downloadCards.forEach(card => {
        if (target === 'all' || card.dataset.mc === target) {
          card.style.display = 'flex';
        } else {
          card.style.display = 'none';
        }
      });

      // Filter changelog build download boxes
      buildBoxes.forEach(box => {
        if (target === 'all' || box.dataset.mcTarget === target) {
          box.style.display = 'flex';
        } else {
          box.style.display = 'none';
        }
      });
    });
  });

  // Version Dropdown Selector (e.g. v1.0.0, v1.0.1, all)
  if (versionSelect) {
    versionSelect.addEventListener('change', (e) => {
      const selectedVersion = e.target.value;
      releaseCards.forEach(card => {
        if (selectedVersion === 'all' || card.dataset.release === selectedVersion) {
          card.style.display = 'block';
        } else {
          card.style.display = 'none';
        }
      });
    });
  }
});
