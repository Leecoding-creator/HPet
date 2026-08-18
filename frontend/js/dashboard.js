/**
 * HPet - Dashboard Controller & Mission Checklist Management (Stage 4)
 */

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
      btnAddSupp.addEventListener('click', async () => {
        const modal = document.getElementById('modal-custom-supp');
        if (modal) {
          modal.dataset.context = 'dashboard';
          document.getElementById('custom-supp-user-id').value = '';
          document.getElementById('modal-supp-title').textContent = '영양제 추가';
          document.getElementById('btn-delete-supp').classList.add('hidden');
          
          const btnSave = document.getElementById('btn-save-custom-supp');
          if (btnSave) btnSave.classList.remove('hidden');
          
          document.getElementById('custom-supp-name').value = '';
          document.getElementById('custom-supp-time').value = '09:00';
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
      btnOpenAddSupp.addEventListener('click', async () => {
        document.getElementById('modal-manage-supplements').classList.add('hidden');
        const modal = document.getElementById('modal-custom-supp');
        if (modal) {
          modal.dataset.context = 'dashboard';
          document.getElementById('custom-supp-user-id').value = '';
          document.getElementById('modal-supp-title').textContent = '새 영양제 등록';
          document.getElementById('btn-delete-supp').classList.add('hidden');
          
          const btnSave = document.getElementById('btn-save-custom-supp');
          if (btnSave) btnSave.classList.remove('hidden');
          
          document.getElementById('custom-supp-name').value = '';
          document.getElementById('custom-supp-time').value = '09:00';
          modal.classList.remove('hidden');
        }
      });
    }

    const btnDeleteSupp = document.getElementById('btn-delete-supp');
    if (btnDeleteSupp) {
      btnDeleteSupp.addEventListener('click', async () => {
        const userIdInput = document.getElementById('custom-supp-user-id');
        const userSupplementId = userIdInput ? userIdInput.value : null;
        
        if (userSupplementId && confirm('이 영양제를 삭제하시겠습니까?')) {
          try {
            await window.hpetApi.removeUserSupplement(userSupplementId);
            window.hpetSound.playSuccess();
            document.getElementById('modal-custom-supp').classList.add('hidden');
            this.renderManageSupplements();
            document.getElementById('modal-manage-supplements').classList.remove('hidden');
            this.render(); // 메인 홈 요약 갱신
          } catch (err) {
            alert(err.message || '영양제 삭제에 실패했습니다.');
          }
        }
      });
    }

    const btnSaveCustomSupp = document.getElementById('btn-save-custom-supp');
    if (btnSaveCustomSupp) {
      btnSaveCustomSupp.addEventListener('click', async () => {
        const modal = document.getElementById('modal-custom-supp');
        if (modal && modal.dataset.context !== 'dashboard') return;
        
        const nameEl = document.getElementById('custom-supp-name');
        const timeEl = document.getElementById('custom-supp-time');
        const userIdEl = document.getElementById('custom-supp-user-id');
        
        const customName = nameEl ? nameEl.value.trim() : '';
        const doseTime = timeEl ? timeEl.value.trim() : '09:00';
        const userSupplementId = userIdEl ? userIdEl.value : '';

        if (!customName) {
          alert('영양제 이름을 입력해주세요.');
          return;
        }
        try {
          if (userSupplementId) {
            // 수정 모드
            await window.hpetApi.updateUserSupplement(userSupplementId, customName, doseTime);
          } else {
            // 생성 모드
            await window.hpetApi.addUserSupplement(customName, doseTime);
          }
          window.hpetSound.playSuccess();
          modal.classList.add('hidden');
          this.renderManageSupplements();
          document.getElementById('modal-manage-supplements').classList.remove('hidden');
          this.render(); // 메인 홈 요약 갱신
        } catch (err) {
          alert(err.message || '영양제 저장에 실패했습니다.');
        }
      });
    }

    const btnCancelCustomSupp = document.getElementById('btn-cancel-custom-supp');
    if (btnCancelCustomSupp) {
      btnCancelCustomSupp.addEventListener('click', () => {
        const modal = document.getElementById('modal-custom-supp');
        if (modal && modal.dataset.context !== 'dashboard') return;
        
        modal.classList.add('hidden');
        document.getElementById('modal-manage-supplements').classList.remove('hidden');
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
      // 캐릭터 미배정 상태의 기본 표시 이미지 (임의로 CHICK 1단계 사용)
      const placeholder = getCharacterImagePath('CHICK', 0);
      if (mainImg) { mainImg.src = placeholder; mainImg.alt = 'HPet 캐릭터: 준비 중'; }
      if (postureImg) postureImg.src = placeholder;
      return;
    }

    const currentMotion = window.hpetStore.state.pet.currentMotion;
    const charFile = currentMotion 
      ? getCharacterMotionPath(characterSummary.characterCode, characterSummary.growthPoints, currentMotion)
      : getCharacterImagePath(characterSummary.characterCode, characterSummary.growthPoints);
      
    const stageLabel = STAGE_LABELS[characterSummary.stage] || characterSummary.stage;
    const stageIdx = STAGE_INDEX[characterSummary.stage] || 1;
    const maxPoints = characterSummary.maxGrowthPoints || 300;
    const growthPercent = Math.max(0, Math.min(100, Math.round((characterSummary.growthPoints / maxPoints) * 100)));

    if (nameEl) nameEl.textContent = characterSummary.characterName;
    if (levelEl) levelEl.textContent = `Lv.${stageIdx} ${stageLabel}`;
    if (growthBar) growthBar.style.width = `${growthPercent}%`;
    if (growthText) growthText.innerHTML = `<strong>${characterSummary.growthPoints}</strong> / ${maxPoints} (${growthPercent}%)`;
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

  async renderManageSupplements() {
    const listContainer = document.getElementById('manage-supps-list');
    if (!listContainer) return;

    try {
      const supps = await window.hpetApi.getUserSupplements();
      
      if (!supps || supps.length === 0) {
        listContainer.innerHTML = `<div style="text-align:center; padding: 20px; color:var(--text-mid);">등록된 영양제가 없습니다.</div>`;
        return;
      }

      listContainer.innerHTML = supps.map(supp => `
        <div class="manage-supp-item">
          <div class="manage-supp-info">
            <span class="manage-supp-name">${supp.customName}</span>
            <span class="manage-supp-time">${supp.doseTime}</span>
          </div>
          <div style="position: relative;">
            <button class="btn-more-options" data-id="${supp.userSupplementId}" data-name="${supp.customName}" data-time="${supp.doseTime}">
              <i class="fa-solid fa-ellipsis"></i>
            </button>
          </div>
        </div>
      `).join('');

      // ... 버튼(수정/삭제 모드)
      listContainer.querySelectorAll('.btn-more-options').forEach(btn => {
        btn.addEventListener('click', (e) => {
          const suppId = e.currentTarget.dataset.id;
          const suppName = e.currentTarget.dataset.name;
          const suppTime = e.currentTarget.dataset.time;
          
          document.getElementById('modal-manage-supplements').classList.add('hidden');
          const modal = document.getElementById('modal-custom-supp');
          
          if (modal) {
            modal.dataset.context = 'dashboard';
            document.getElementById('custom-supp-user-id').value = suppId;
            document.getElementById('modal-supp-title').textContent = '영양제 관리';
            
            document.getElementById('custom-supp-name').value = suppName;
            document.getElementById('custom-supp-time').value = suppTime;
            
            const btnSave = document.getElementById('btn-save-custom-supp');
            if (btnSave) btnSave.classList.remove('hidden'); // 수정 지원
            
            document.getElementById('btn-delete-supp').classList.remove('hidden');
            modal.classList.remove('hidden');
          }
        });
      });
    } catch (e) {
      console.error('Failed to render manage supplements', e);
    }
  }
}

window.hpetDashboard = new HPetDashboardManager();

document.addEventListener('DOMContentLoaded', () => {
  window.hpetDashboard.init();
});
