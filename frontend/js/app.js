/**
 * HPet - Client Core Application & State Management Engine (Stage 1)
 */

// ==========================================================================
// 1. Storage & State Management System
// ==========================================================================
class HPetStore {
  // 캐릭터 이미지 목록 (백엔드 연동 전 프론트 임시 배정용)
  static CHAR_IMAGES = [
    { id: 'chick', name: '병아리', file: 'char_chick.png' },
    { id: 'hedgehog', name: '고슴도치', file: 'char_hedgehog.png' },
    { id: 'otter', name: '수달이', file: 'char_otter.png' },
    { id: 'turtle', name: '거북이', file: 'char_turtle.png' }
  ];

  constructor() {
    this.STORAGE_KEY = 'HPET_APP_STATE_V1';
    this.state = this.loadState();
  }

  // 랜덤 캐릭터 배정 (백엔드 연동 전 임시 로직 — 추후 서버에서 받은 값으로 교체)
  assignRandomChar() {
    const chars = HPetStore.CHAR_IMAGES;
    const chosen = chars[Math.floor(Math.random() * chars.length)];
    this.state.pet.charId = chosen.id;
    this.state.pet.charImage = chosen.file;
    this.state.pet.name = chosen.name;
    this.saveState();
    return chosen;
  }

  getInitialState() {
    return {
      user: {
        isLoggedIn: false,
        name: '김하늘',
        email: 'sky@hpet.io',
        ageGroup: '20s',
        gender: 'female',
        concerns: ['fatigue', 'turtle-neck'],
        streak: 3
      },
      pet: {
        id: 'pet_default',
        name: '병아리',
        charId: 'chick',
        charImage: 'char_chick.png',  // 기본값, 로그인 시 랜덤 배정됨
        type: 'character',
        level: 2,
        exp: 68,
        maxExp: 100,
        postureHealth: 85,
        happiness: 80,
        stage: 'teen' // baby, teen, adult
      },
      supplements: [
        { id: 'supp_1', name: '비타민 C', time: '08:00 AM', takenToday: true, icon: 'C' },
        { id: 'supp_2', name: '비타민 D', time: '12:00 PM', takenToday: false, icon: 'D' },
        { id: 'supp_3', name: '아연 (Zinc)', time: '06:00 PM', takenToday: false, icon: 'Zn' },
        { id: 'supp_4', name: '프로바이오틱스', time: '09:00 PM', takenToday: false, icon: 'Pro' }
      ],
      history: {
        '2026-07-29': { supplements: true, turtleCount: 1 },
        '2026-07-30': { supplements: true, turtleCount: 0 },
        '2026-07-31': { supplements: false, turtleCount: 2 }
      },
      stats: {
        turtleNeckDetectionsThisWeek: 4,
        postureGamesCleared: 12
      }
    };
  }

  loadState() {
    const saved = localStorage.getItem(this.STORAGE_KEY);
    let state;
    if (!saved) {
      state = this.getInitialState();
    } else {
      try {
        state = JSON.parse(saved);
      } catch (e) {
        console.error('State parse error, fallback to initial state', e);
        state = this.getInitialState();
      }
    }

    // 테스트를 위한 8월 가상 데이터 강제 주입
    if (!state.history['2026-08-01']) {
      state.history['2026-08-01'] = { supplements: true, turtleCount: 1 };
      state.history['2026-08-05'] = { supplements: true, turtleCount: 0 };
      
      const now = new Date();
      const todayStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
      state.history[todayStr] = { supplements: true, turtleCount: 3 }; // 오늘 날짜에도 데이터 주입
      
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(state));
    }
    
    return state;
  }

  saveState() {
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(this.state));
    window.dispatchEvent(new CustomEvent('hpet_state_changed', { detail: this.state }));
  }

  updatePetExp(amount) {
    this.state.pet.exp += amount;
    if (this.state.pet.exp >= this.state.pet.maxExp) {
      this.state.pet.exp -= this.state.pet.maxExp;
      this.state.pet.level += 1;
    }
    this.saveState();
  }

  updatePostureHealth(amount) {
    this.state.pet.postureHealth = Math.min(100, Math.max(0, this.state.pet.postureHealth + amount));
    this.saveState();
  }

  markSupplementTaken(suppId) {
    const supp = this.state.supplements.find(s => s.id === suppId);
    if (supp) {
      supp.takenToday = true;
      this.updatePetExp(20);
    }
  }
}

// Global App Store instance
window.hpetStore = new HPetStore();

// ==========================================================================
// 2. Retro Sound Engine (Web Audio API Beeps)
// ==========================================================================
class HPetSound {
  constructor() {
    this.ctx = null;
  }

  init() {
    if (!this.ctx) {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      this.ctx = new AudioCtx();
    }
  }

  playBeep(freq = 440, duration = 0.08, type = 'sine') {
    try {
      this.init();
      if (this.ctx.state === 'suspended') this.ctx.resume();
      
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      
      osc.type = type;
      osc.frequency.setValueAtTime(freq, this.ctx.currentTime);
      gain.gain.setValueAtTime(0.1, this.ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + duration);

      osc.connect(gain);
      gain.connect(this.ctx.destination);

      osc.start();
      osc.stop(this.ctx.currentTime + duration);
    } catch (e) {
      // Silently ignore audio block
    }
  }

  playSuccess() {
    this.playBeep(523.25, 0.08); // C5
    setTimeout(() => this.playBeep(659.25, 0.08), 80); // E5
    setTimeout(() => this.playBeep(783.99, 0.15), 160); // G5
  }
}

window.hpetSound = new HPetSound();

// ==========================================================================
// 3. Router & View Management
// ==========================================================================
class HPetRouter {
  constructor() {
    this.views = {
      auth: document.getElementById('view-auth'),
      profileSetup: document.getElementById('view-profile-setup'),
      dashboard: document.getElementById('view-dashboard'),
      cameraAuth: document.getElementById('view-camera-auth'),
      postureGame: document.getElementById('view-posture-game'),
      history: document.getElementById('view-history'),
      profile: document.getElementById('view-profile')
    };

    this.nav = document.getElementById('app-nav');
    this.header = document.getElementById('main-header');
    this.currentView = 'auth';
  }

  navigateTo(viewName) {
    if (!this.views[viewName]) return;
    
    window.hpetSound.playBeep(600, 0.05);

    // 이전 뷰의 leave 이벤트 발행 (카메라 스트림 정리 등)
    const prevView = this.currentView;
    if (prevView) {
      window.dispatchEvent(new CustomEvent(`hpet_view_leave_${prevView}`));
    }

    Object.keys(this.views).forEach(key => {
      this.views[key].classList.add('hidden');
      this.views[key].classList.remove('active');
    });

    this.views[viewName].classList.remove('hidden');
    this.views[viewName].classList.add('active');
    this.currentView = viewName;

    // Header & Bottom Nav visibility logic
    if (viewName === 'auth' || viewName === 'cameraAuth' || viewName === 'postureGame') {
      this.header.classList.add('hidden');
      this.nav.classList.add('hidden');
    } else if (viewName === 'profileSetup') {
      this.header.classList.add('hidden');
      this.nav.classList.add('hidden');
    } else {
      this.header.classList.remove('hidden');
      this.nav.classList.remove('hidden');
    }

    // Update bottom nav icons
    document.querySelectorAll('.nav-item').forEach(item => {
      if (item.dataset.view === viewName) {
        item.classList.add('active');
      } else {
        item.classList.remove('active');
      }
    });

    // 새 뷰의 enter 이벤트 발행 (카메라 시작 등)
    window.dispatchEvent(new CustomEvent(`hpet_view_enter_${viewName}`));

    // View specific init triggers
    if (viewName === 'dashboard') {
      if (window.hpetDashboard) window.hpetDashboard.render();
    } else if (viewName === 'history') {
      if (window.hpetHistory) window.hpetHistory.render();
    }
  }
}

window.hpetRouter = new HPetRouter();

// ==========================================================================
// 4. UI Controller & Renderer
// ==========================================================================
const HPetUI = {
  init() {
    this.bindEvents();
    this.checkInitialSession();
  },

  checkInitialSession() {
    const state = window.hpetStore.state;
    if (state.user.isLoggedIn) {
      window.hpetRouter.navigateTo('dashboard');
    } else {
      window.hpetRouter.navigateTo('auth');
    }
  },

  bindEvents() {
    // Auth Tab Switch
    document.querySelectorAll('.auth-tabs .tab-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const tab = e.target.dataset.tab;
        document.querySelectorAll('.auth-tabs .tab-btn').forEach(b => b.classList.remove('active'));
        e.target.classList.add('active');

        if (tab === 'login') {
          document.getElementById('form-login').classList.remove('hidden');
          document.getElementById('form-signup').classList.add('hidden');
        } else {
          document.getElementById('form-login').classList.add('hidden');
          document.getElementById('form-signup').classList.remove('hidden');
        }
      });
    });

    // Form Submissions
    document.getElementById('form-login').addEventListener('submit', (e) => {
      e.preventDefault();
      window.hpetStore.state.user.isLoggedIn = true;
      // 캐릭터가 아직 배정되지 않았으면 랜덤 배정 (백엔드 연동 전 임시)
      if (!window.hpetStore.state.pet.charImage) {
        window.hpetStore.assignRandomChar();
      }
      window.hpetStore.saveState();
      window.hpetSound.playSuccess();
      window.hpetRouter.navigateTo('dashboard');
    });

    document.getElementById('form-signup').addEventListener('submit', (e) => {
      e.preventDefault();
      window.hpetStore.state.user.isLoggedIn = true;
      // 회원가입 시 랜덤 캐릭터 배정 (백엔드 연동 전 임시)
      window.hpetStore.assignRandomChar();
      window.hpetStore.saveState();
      window.hpetSound.playSuccess();
      window.hpetRouter.navigateTo('profileSetup');
    });

    // Profile Setup Chips
    document.querySelectorAll('.chip-group:not(.multi) .chip').forEach(chip => {
      chip.addEventListener('click', (e) => {
        const parent = e.target.parentElement;
        parent.querySelectorAll('.chip').forEach(c => c.classList.remove('active'));
        e.target.classList.add('active');
      });
    });

    document.querySelectorAll('.chip-group.multi .chip').forEach(chip => {
      chip.addEventListener('click', (e) => {
        e.target.classList.toggle('active');
      });
    });

    // Profile Setup Steps Navigation
    document.getElementById('btn-next-step-1')?.addEventListener('click', () => {
      document.getElementById('setup-step-1').classList.add('hidden');
      document.getElementById('setup-step-2').classList.remove('hidden');
      this.renderRecommendations();
    });

    document.getElementById('btn-next-step-2')?.addEventListener('click', () => {
      document.getElementById('setup-step-2').classList.add('hidden');
      document.getElementById('setup-step-3').classList.remove('hidden');
      this.renderPetOptions();
    });

    document.getElementById('btn-finish-setup')?.addEventListener('click', () => {
      window.hpetStore.saveState();
      window.hpetSound.playSuccess();
      window.hpetRouter.navigateTo('dashboard');
    });

    // Navigation Bar
    document.querySelectorAll('.nav-item').forEach(item => {
      item.addEventListener('click', (e) => {
        const targetView = e.currentTarget.dataset.view;
        window.hpetRouter.navigateTo(targetView);
      });
    });

    // Quick Action Card Links
    document.getElementById('btn-action-auth')?.addEventListener('click', () => {
      window.hpetRouter.navigateTo('cameraAuth');
    });

    document.getElementById('btn-action-game')?.addEventListener('click', () => {
      window.hpetRouter.navigateTo('postureGame');
    });

    document.getElementById('btn-close-cam')?.addEventListener('click', () => {
      window.hpetRouter.navigateTo('dashboard');
    });

    document.getElementById('btn-close-game')?.addEventListener('click', () => {
      window.hpetRouter.navigateTo('dashboard');
    });

    // Pet Interactions
    document.getElementById('tamagotchi-pet-wrapper')?.addEventListener('click', () => {
      window.hpetSound.playBeep(880, 0.1);
      const bubble = document.getElementById('pet-speech');
      const messages = ["기분이 너무 좋아요! 💕", "오늘 영양제 먹으셨나요?", "바른 자세를 유지해봐요!"];
      bubble.textContent = messages[Math.floor(Math.random() * messages.length)];
    });

    // Physical Decorator Buttons
    document.getElementById('tamagotchi-phy-a')?.addEventListener('click', () => {
      window.hpetSound.playBeep(400, 0.06);
    });
    document.getElementById('tamagotchi-phy-b')?.addEventListener('click', () => {
      window.hpetSound.playBeep(600, 0.06);
    });
    document.getElementById('tamagotchi-phy-c')?.addEventListener('click', () => {
      window.hpetSound.playBeep(800, 0.06);
    });

    // 비밀번호 표시/숨김 토글 (Pill & Pose 눈 아이콘)
    document.querySelectorAll('.password-toggle').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const input = e.currentTarget.parentElement.querySelector('input');
        const icon = e.currentTarget.querySelector('i');
        if (input.type === 'password') {
          input.type = 'text';
          icon.className = 'fa-solid fa-eye';
        } else {
          input.type = 'password';
          icon.className = 'fa-solid fa-eye-slash';
        }
      });
    });

    // 하단 전환 링크 (Pill & Pose: "이미 계정이 있으신가요? 로그인")
    document.querySelectorAll('.tab-btn-switch').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.preventDefault();
        const tab = e.target.dataset.tab;
        // 기존 탭 버튼도 동기화
        document.querySelectorAll('.auth-tabs .tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelector(`.auth-tabs .tab-btn[data-tab="${tab}"]`)?.classList.add('active');

        if (tab === 'login') {
          document.getElementById('form-login').classList.remove('hidden');
          document.getElementById('form-signup').classList.add('hidden');
        } else {
          document.getElementById('form-login').classList.add('hidden');
          document.getElementById('form-signup').classList.remove('hidden');
        }
      });
    });

    // Reward Modal Close
    document.getElementById('btn-close-reward')?.addEventListener('click', () => {
      document.getElementById('modal-reward').classList.add('hidden');
    });
  },

  renderRecommendations() {
    const container = document.getElementById('recommend-list');
    if (!container) return;
    const items = [
      { name: '비타민 C', reason: '피로 회복 & 항산화 케어 추천', icon: '🍋' },
      { name: '오메가 3', reason: '눈 피로 및 혈행 개선 추천', icon: '🐟' },
      { name: '마그네슘', reason: '근육 이완 및 거북목 통증 완화', icon: '⚡' }
    ];

    container.innerHTML = items.map(item => `
      <div class="recommend-card selected">
        <div class="supp-icon">${item.icon}</div>
        <div class="supp-info">
          <div class="supp-name">${item.name}</div>
          <div class="supp-reason">${item.reason}</div>
        </div>
        <i class="fa-solid fa-circle-check text-primary"></i>
      </div>
    `).join('');
  },

  renderPetOptions() {
    const container = document.getElementById('pet-selection-grid');
    if (!container) return;
    // 실제 char_*.png 이미지 사용 (백엔드 연동 전 프론트 임시)
    const pets = HPetStore.CHAR_IMAGES;

    container.innerHTML = pets.map((pet, idx) => `
      <div class="pet-option-card ${idx === 0 ? 'selected' : ''}" data-id="${pet.id}">
        <img src="${pet.file}" alt="${pet.name}" style="width:64px;height:64px;object-fit:contain;">
        <strong>${pet.name}</strong>
      </div>
    `).join('');

    // 캐릭터 선택 클릭 이벤트
    container.querySelectorAll('.pet-option-card').forEach(card => {
      card.addEventListener('click', () => {
        container.querySelectorAll('.pet-option-card').forEach(c => c.classList.remove('selected'));
        card.classList.add('selected');
        const charId = card.dataset.id;
        const chosen = HPetStore.CHAR_IMAGES.find(c => c.id === charId);
        if (chosen) {
          window.hpetStore.state.pet.charId = chosen.id;
          window.hpetStore.state.pet.charImage = chosen.file;
          window.hpetStore.state.pet.name = chosen.name;
          window.hpetStore.saveState();
        }
      });
    });
  },

  renderDashboard() {
    const state = window.hpetStore.state;
    
    // Header
    document.getElementById('header-username').textContent = `${state.user.name}님`;
    document.getElementById('header-streak').textContent = state.user.streak;

    // Gauges
    document.getElementById('bar-exp').style.width = `${state.pet.exp}%`;
    document.getElementById('val-exp').textContent = `${state.pet.exp} / ${state.pet.maxExp}`;

    document.getElementById('bar-posture').style.width = `${state.pet.postureHealth}%`;
    document.getElementById('val-posture').textContent = `${state.pet.postureHealth} / 100`;

    // 캐릭터 이미지 업데이트 (char_*.png)
    const charImg = document.getElementById('pet-char-img');
    if (charImg && state.pet.charImage) {
      charImg.src = state.pet.charImage;
      charImg.alt = `HPet 캐릭터: ${state.pet.name}`;
    }

    // Mission List
    const missionList = document.getElementById('today-mission-list');
    if (missionList) {
      missionList.innerHTML = state.supplements.map(supp => `
        <div class="mission-item ${supp.takenToday ? 'completed' : ''}">
          <div class="mission-info">
            <span class="mission-time">${supp.time}</span>
            <span class="mission-name">${supp.name}</span>
          </div>
          <button class="btn-check-auth ${supp.takenToday ? 'done' : ''}" data-id="${supp.id}">
            ${supp.takenToday ? '<i class="fa-solid fa-check"></i> 완료' : '인증하기'}
          </button>
        </div>
      `).join('');

      missionList.querySelectorAll('.btn-check-auth:not(.done)').forEach(btn => {
        btn.addEventListener('click', (e) => {
          const suppId = e.currentTarget.dataset.id;
          window.hpetStore.markSupplementTaken(suppId);
          window.hpetSound.playSuccess();
          this.renderDashboard();
          
          // Show Reward Modal
          document.getElementById('reward-title').textContent = "복용 인증 성공!";
          document.getElementById('reward-desc').textContent = "포션을 획득하여 HPet 성장에 기여했습니다 (+20 EXP)";
          document.getElementById('modal-reward').classList.remove('hidden');
        });
      });
    }
  },

  renderHistory() {
    const calGrid = document.getElementById('calendar-grid');
    if (!calGrid) return;
    
    let html = '';
    for (let i = 1; i <= 31; i++) {
      const isChecked = i <= 3; // Demo checked status
      html += `<div class="cal-day ${isChecked ? 'checked' : ''}">${i}</div>`;
    }
    calGrid.innerHTML = html;
  }
};

// Initialize app when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
  HPetUI.init();
});
