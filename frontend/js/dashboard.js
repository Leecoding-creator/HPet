/**
 * HPet - Dashboard Controller & Mission Checklist Management (Stage 4)
 */

// 캐릭터 코드(백엔드) → 로컬 이미지/이름 자산 매핑
const CHAR_CODE_TO_IMAGE = {
  TURTLE: 'char_turtle.png',
  CHICK: 'char_chick.png',
  OTTER: 'char_otter.png',
  HEDGEHOG: 'char_hedgehog.png'
};

// 성장 단계(백엔드 6단계 enum) → 한글 라벨 / 레벨 숫자
// 팀 결정 필요(작업지시서 3번): 정식 EXP/레벨 게이지가 정해지기 전까지의 임시 근사치
const STAGE_LABELS = {
  BABY: '아기', TODDLER: '유아기', CHILD: '어린이',
  TEEN: '청소년', ADULT: '성체', GROWN: '완전성장'
};
const STAGE_INDEX = { BABY: 1, TODDLER: 2, CHILD: 3, TEEN: 4, ADULT: 5, GROWN: 6 };

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
      if (document.getElementById('modal-manage-supplements') && !document.getElementById('modal-manage-supplements').classList.contains('hidden')) {
        this.renderManageSupplements();
      }
    });

    // 대시보드 영양제 추가 버튼
    const btnAddSupp = document.getElementById('btn-add-supp-dashboard');
    if (btnAddSupp) {
      btnAddSupp.addEventListener('click', () => {
        const modal = document.getElementById('modal-custom-supp');
        if (modal) {
          document.getElementById('custom-supp-name').value = ''; // 초기화
          delete modal.dataset.editId; // 생성 모드
          document.getElementById('modal-supp-title').textContent = '영양제 추가';
          document.getElementById('btn-delete-supp').classList.add('hidden');
          modal.classList.remove('hidden');
        }
      });
    }

    // 전체 보기 버튼 (영양제 관리 모달 열기)
    const btnViewAll = document.getElementById('btn-view-all');
    if (btnViewAll) {
      btnViewAll.addEventListener('click', () => {
        this.renderManageSupplements();
        document.getElementById('modal-manage-supplements').classList.remove('hidden');
      });
    }

    // 영양제 관리 모달 닫기
    const btnCloseManage = document.getElementById('btn-close-manage-supps');
    if (btnCloseManage) {
      btnCloseManage.addEventListener('click', () => {
        document.getElementById('modal-manage-supplements').classList.add('hidden');
      });
    }

    // 새 영양제 추가 버튼 (관리 모달 내)
    const btnOpenAddSupp = document.getElementById('btn-open-add-supp');
    if (btnOpenAddSupp) {
      btnOpenAddSupp.addEventListener('click', () => {
        document.getElementById('modal-manage-supplements').classList.add('hidden');
        const modal = document.getElementById('modal-custom-supp');
        if (modal) {
          document.getElementById('custom-supp-name').value = '';
          document.getElementById('custom-supp-time').value = '09:00 AM';
          delete modal.dataset.editId;
          document.getElementById('modal-supp-title').textContent = '새 영양제 등록';
          document.getElementById('btn-delete-supp').classList.add('hidden');
          modal.classList.remove('hidden');
        }
      });
    }

    const btnDeleteSupp = document.getElementById('btn-delete-supp');
    if (btnDeleteSupp) {
      btnDeleteSupp.addEventListener('click', () => {
        const modal = document.getElementById('modal-custom-supp');
        const idInput = modal.dataset.editId;
        if (idInput && confirm('이 영양제를 삭제하시겠습니까?')) {
          const state = window.hpetStore.state;
          window.hpetStore.state.supplements = state.supplements.filter(s => s.id !== idInput);
          window.hpetStore.saveState();
          
          modal.classList.add('hidden');
          this.renderManageSupplements();
          document.getElementById('modal-manage-supplements').classList.remove('hidden');
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

  async render() {
    if (!window.hpetApi || !window.hpetApi.isLoggedIn()) return;

    try {
      const [summary, profile] = await Promise.all([
        window.hpetApi.getHomeSummary(),
        window.hpetApi.getMyProfile().catch(() => null)
      ]);

      if (profile) {
        window.hpetStore.state.user.name = profile.nickname || profile.email;
        const headerName = document.getElementById('header-username');
        if (headerName) headerName.textContent = `${window.hpetStore.state.user.name}님`;
      }

      this.renderCharacter(summary.characterSummary);
      this.renderMissions(summary.doseSummary);
      this.renderPosture(summary.postureSummary);
    } catch (err) {
      console.error('홈 요약 조회 실패', err);
      if (err.status === 401) {
        window.hpetApi.clearTokens();
        window.hpetRouter.navigateTo('auth');
      }
    }
  }

  renderCharacter(characterSummary) {
    const nameEl = document.getElementById('pet-name-text');
    const levelEl = document.getElementById('pet-level-text');
    const growthBar = document.getElementById('pet-growth-bar');
    const growthText = document.getElementById('pet-growth-text');
    const mainImg = document.getElementById('main-pet-img');
    const postureImg = document.getElementById('posture-char-img');

    // characterSummary는 아직 캐릭터가 배정되지 않은 경우 null (최초 영양제 등록 전)
    if (!characterSummary) {
      if (nameEl) nameEl.textContent = '준비 중';
      if (levelEl) levelEl.textContent = '';
      if (growthBar) growthBar.style.width = '0%';
      if (growthText) growthText.innerHTML = '영양제를 등록하면 캐릭터가 배정돼요';
      return;
    }

    const charFile = CHAR_CODE_TO_IMAGE[characterSummary.characterCode] || 'char_chick.png';
    const stageLabel = STAGE_LABELS[characterSummary.stage] || characterSummary.stage;
    const stageIdx = STAGE_INDEX[characterSummary.stage] || 1;
    // 백엔드는 EXP/레벨 대신 growthDays(누적 성장일수)+stage만 제공.
    // 팀 결정 전까지 "완전 성장 = 31일" 가정으로 근사한 게이지(작업지시서 3번 항목 참고).
    const growthPercent = Math.max(0, Math.min(100, Math.round((characterSummary.growthDays / 31) * 100)));

    if (nameEl) nameEl.textContent = characterSummary.characterName;
    if (levelEl) levelEl.textContent = `Lv.${stageIdx} ${stageLabel}`;
    if (growthBar) growthBar.style.width = `${growthPercent}%`;
    if (growthText) growthText.innerHTML = `<strong>${characterSummary.growthDays}</strong>일 성장 (${growthPercent}%)`;
    if (mainImg) {
      mainImg.src = charFile;
      mainImg.alt = `HPet 캐릭터: ${characterSummary.characterName}`;
    }
    if (postureImg) postureImg.src = charFile;
  }

  renderMissions(doseSummary) {
    if (!this.missionContainer) return;
    const doseList = (doseSummary && doseSummary.doseList) || [];

    if (doseList.length === 0) {
      this.missionContainer.innerHTML = `
        <div class="empty-mission">
          <p>등록된 영양제가 없습니다. 프로필 설정에서 추가해보세요!</p>
        </div>
      `;
    } else {
      const colors = ['yellow', 'blue', 'red', 'green', 'purple'];
      this.missionContainer.innerHTML = doseList.map((dose, idx) => {
        const colorCls = colors[idx % colors.length];
        return `
          <div class="pill-item ${dose.completed ? 'taken' : ''}" data-user-supplement-id="${dose.userSupplementId}">
            <i class="fa-solid fa-capsules pill-icon ${colorCls}"></i>
            <span class="pill-name">${dose.supplementName}</span>
            ${dose.completed
              ? '<i class="fa-solid fa-circle-check check-icon text-green"></i>'
              : '<i class="fa-regular fa-circle check-icon text-gray"></i>'
            }
          </div>
        `;
      }).join('');
    }

    const totalCount = (doseSummary && doseSummary.totalSupplementCount) || 0;
    const completedCount = (doseSummary && doseSummary.completedCount) || 0;
    const fractionText = document.querySelector('.home-card.pill-card .fraction-text');
    if (fractionText) {
      fractionText.innerHTML = `<strong class="text-yellow">${completedCount}</strong> / ${totalCount}`;
    }
  }

  renderPosture(postureSummary) {
    const textEl = document.getElementById('posture-summary-text');
    if (!textEl) return;
    const todayCount = (postureSummary && postureSummary.todayCount) || 0;
    // 백엔드는 "오늘 감지 횟수"만 제공, 0~100 점수 개념은 없음 (작업지시서 3번 항목 참고)
    textEl.textContent = todayCount === 0
      ? '오늘은 거북목이 감지되지 않았어요!'
      : `오늘 거북목이 ${todayCount}회 감지됐어요`;
  }

  renderManageSupplements() {
    const listContainer = document.getElementById('manage-supps-list');
    if (!listContainer) return;

    const state = window.hpetStore.state;
    if (!state.supplements || state.supplements.length === 0) {
      listContainer.innerHTML = `<div style="text-align:center; padding: 20px; color:var(--text-mid);">등록된 영양제가 없습니다.</div>`;
      return;
    }

    listContainer.innerHTML = state.supplements.map(supp => `
      <div class="manage-supp-item">
        <div class="manage-supp-info">
          <span class="manage-supp-name">${supp.name}</span>
          <span class="manage-supp-time">${supp.time || ''}</span>
        </div>
        <div style="position: relative;">
          <button class="btn-more-options" data-id="${supp.id}">
            <i class="fa-solid fa-ellipsis"></i>
          </button>
        </div>
      </div>
    `).join('');

    // ... 버튼(수정 모달 띄우기)
    listContainer.querySelectorAll('.btn-more-options').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const suppId = e.currentTarget.dataset.id;
        const targetSupp = state.supplements.find(s => s.id === suppId);
        
        document.getElementById('modal-manage-supplements').classList.add('hidden');
        const modal = document.getElementById('modal-custom-supp');
        
        if (targetSupp && modal) {
          modal.dataset.editId = suppId;
          document.getElementById('custom-supp-name').value = targetSupp.name;
          document.getElementById('custom-supp-time').value = targetSupp.time || '09:00 AM';
          document.getElementById('modal-supp-title').textContent = '영양제 수정';
          document.getElementById('btn-delete-supp').classList.remove('hidden');
          modal.classList.remove('hidden');
        }
      });
    });

  }
}

window.hpetDashboard = new HPetDashboardManager();

document.addEventListener('DOMContentLoaded', () => {
  window.hpetDashboard.init();
});
