/**
 * HPet - Dashboard Controller & Mission Checklist Management (Stage 4)
 */

class HPetDashboardManager {
  constructor() {
    this.dateTag = null;
    this.missionContainer = null;
  }

  init() {
    this.dateTag = document.getElementById('current-date-tag');
    this.missionContainer = document.getElementById('today-mission-list');
    this.setCurrentDate();
    this.render();

    // 상태 변경 이벤트 수신 시 자동으로 화면 갱신
    window.addEventListener('hpet_state_changed', () => {
      this.render();
    });

    // 대시보드 영양제 추가 버튼
    const btnAddSupp = document.getElementById('btn-add-supp-dashboard');
    if (btnAddSupp) {
      btnAddSupp.addEventListener('click', () => {
        const modal = document.getElementById('modal-custom-supp');
        if (modal) {
          document.getElementById('custom-supp-name').value = ''; // 초기화
          modal.classList.remove('hidden');
        }
      });
    }
  }

  setCurrentDate() {
    if (!this.dateTag) return;
    const now = new Date();
    const month = now.getMonth() + 1;
    const date = now.getDate();
    const days = ['일', '월', '화', '수', '목', '금', '토'];
    const dayName = days[now.getDay()];

    this.dateTag.textContent = `${month}월 ${date}일 (${dayName})`;
  }

  render() {
    this.updateGauges();
    this.renderMissions();
  }

  updateGauges() {
    const state = window.hpetStore.state;
    
    // 포션/경험치 바
    const barExp = document.getElementById('bar-exp');
    const valExp = document.getElementById('val-exp');
    if (barExp && valExp) {
      barExp.style.width = `${state.pet.exp}%`;
      valExp.textContent = `Lv.${state.pet.level} (${state.pet.exp}/${state.pet.maxExp})`;
    }

    // 자세 건강도 바
    const barPosture = document.getElementById('bar-posture');
    const valPosture = document.getElementById('val-posture');
    if (barPosture && valPosture) {
      barPosture.style.width = `${state.pet.postureHealth}%`;
      valPosture.textContent = `${state.pet.postureHealth} / 100`;
    }
  }

  renderMissions() {
    if (!this.missionContainer) return;
    const state = window.hpetStore.state;

    if (!state.supplements || state.supplements.length === 0) {
      this.missionContainer.innerHTML = `
        <div class="empty-mission">
          <p>등록된 영양제가 없습니다. 프로필 설정에서 추가해보세요!</p>
        </div>
      `;
      return;
    }

    // 영양제 약어 매핑 (기획서 스타일: C, D, Zn, Pro 등)
    const abbrMap = {
      '비타민 C': 'C', '비타민 D': 'D', '아연': 'Zn', '프로바이오틱스': 'Pro',
      '오메가3': 'Ω3', '마그네슘': 'Mg', '비타민 B군': 'B', '철분': 'Fe',
      '루테인': 'Lu', '칼슘': 'Ca', '콜라겐': 'Co'
    };

    // 아이콘 색상 순환
    const colors = ['#66bb6a','#42a5f5','#ff7043','#ab47bc','#26a69a','#ec407a'];

    this.missionContainer.innerHTML = state.supplements.map((supp, idx) => {
      const abbr = abbrMap[supp.name] || supp.name.substring(0, 2);
      const bgColor = colors[idx % colors.length];
      return `
        <div class="mission-item ${supp.takenToday ? 'completed' : ''}">
          <div class="mission-info">
            <span class="mission-time" style="background:${bgColor}; color:white;">${abbr}</span>
            <div>
              <span class="mission-name">${supp.name}</span>
              <span style="font-size:10px; color:#8fa08f; display:block;">${supp.time || ''}</span>
            </div>
          </div>
          <div style="display: flex; gap: 8px; align-items: center; position: relative;">
            <button class="btn-check-auth ${supp.takenToday ? 'done' : ''}" data-id="${supp.id}">
              ${supp.takenToday
                ? '<i class="fa-solid fa-check"></i>'
                : '<i class="fa-solid fa-camera"></i>'}
            </button>
            <button class="btn-supp-options" style="background:none; border:none; color:var(--text-mid); cursor:pointer; font-size: 16px; padding: 4px 8px;">
              <i class="fa-solid fa-ellipsis-vertical"></i>
            </button>
            <div class="dropdown-menu hidden">
              <button class="dropdown-item btn-edit-supp" data-id="${supp.id}">수정</button>
              <button class="dropdown-item danger btn-delete-supp" data-id="${supp.id}">삭제</button>
            </div>
          </div>
        </div>
      `;
    }).join('');

    // 카메라 인증 이동
    this.missionContainer.querySelectorAll('.btn-check-auth:not(.done)').forEach(btn => {
      btn.addEventListener('click', (e) => {
        window.hpetRouter.navigateTo('cameraAuth');
      });
    });

    // 더보기 메뉴 토글
    this.missionContainer.querySelectorAll('.btn-supp-options').forEach(btn => {
      btn.addEventListener('click', (e) => {
        // 다른 열려있는 메뉴 닫기
        document.querySelectorAll('.dropdown-menu').forEach(menu => menu.classList.add('hidden'));
        
        const dropdown = e.currentTarget.nextElementSibling;
        dropdown.classList.remove('hidden');
        e.stopPropagation();
      });
    });

    // 화면 아무 곳이나 누르면 드롭다운 닫기
    document.addEventListener('click', () => {
      document.querySelectorAll('.dropdown-menu').forEach(menu => menu.classList.add('hidden'));
    }, { once: true });

    // 수정 버튼
    this.missionContainer.querySelectorAll('.btn-edit-supp').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const suppId = e.currentTarget.dataset.id;
        const targetSupp = window.hpetStore.state.supplements.find(s => s.id === suppId);
        const modal = document.getElementById('modal-custom-supp');
        
        if (targetSupp && modal) {
          modal.dataset.editId = suppId;
          document.getElementById('custom-supp-name').value = targetSupp.name;
          document.getElementById('custom-supp-time').value = targetSupp.time || '09:00 AM';
          modal.classList.remove('hidden');
        }
      });
    });

    // 삭제 버튼
    this.missionContainer.querySelectorAll('.btn-delete-supp').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const suppId = e.currentTarget.dataset.id;
        if (confirm('영양제를 삭제하시겠습니까?')) {
          window.hpetStore.state.supplements = window.hpetStore.state.supplements.filter(s => s.id !== suppId);
          window.hpetStore.saveState();
        }
      });
    });
  }
}

window.hpetDashboard = new HPetDashboardManager();

document.addEventListener('DOMContentLoaded', () => {
  window.hpetDashboard.init();
});
