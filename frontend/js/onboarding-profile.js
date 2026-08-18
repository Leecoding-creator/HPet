/**
 * HPet - Onboarding Profile Setup & AI Supplement Recommendation (Stage 2/3)
 */

class HPetProfileSetupManager {
  constructor() {
    this.selectedAge = '20s';
    this.selectedGender = 'female';
    this.selectedConcerns = ['fatigue', 'turtle-neck'];
    this.selectedSupplements = [];
    this.selectedPetId = 'pet_cat';
  }

  init() {
    this.bindStepEvents();
  }

  bindStepEvents() {
    // Step 1: 건강 고민 데이터 수집
    const ageChips = document.querySelectorAll('#chip-age .chip');
    ageChips.forEach(chip => {
      chip.addEventListener('click', (e) => {
        this.selectedAge = e.target.dataset.value;
      });
    });

    const genderChips = document.querySelectorAll('#chip-gender .chip');
    genderChips.forEach(chip => {
      chip.addEventListener('click', (e) => {
        this.selectedGender = e.target.dataset.value;
      });
    });

    const concernChips = document.querySelectorAll('#chip-concerns .chip');
    concernChips.forEach(chip => {
      chip.addEventListener('click', () => {
        this.selectedConcerns = Array.from(document.querySelectorAll('#chip-concerns .chip.active'))
          .map(c => c.dataset.value);
      });
    });

    // 영양제 직접 등록 모달 오픈
    document.getElementById('btn-add-custom-supp')?.addEventListener('click', () => {
      this.showCustomSupplementModal();
    });

    // 커스텀 영양제 폼 제출 (온보딩 모달 전용)
    document.getElementById('form-add-supp')?.addEventListener('click', async (e) => {
      if (e.target.id === 'btn-save-custom-supp') {
        const nameInput = document.getElementById('custom-supp-name');
        const timeInput = document.getElementById('custom-supp-time');
        if (nameInput && nameInput.value.trim()) {
           const newSupp = {
              id: 'supp_' + Date.now(),
              name: nameInput.value.trim(),
              time: timeInput ? timeInput.value : '09:00',
              takenToday: false,
              icon: nameInput.value.substring(0, 2)
           };
           window.hpetStore.state.supplements.push(newSupp);
           window.hpetStore.saveState();
           
           this.renderRecommendations();
           const modal = document.getElementById('modal-custom-supp');
           if (modal) modal.classList.add('hidden');
           window.hpetSound.playSuccess();
        }
      }
    });

    // 펫 선택 카드의 클릭 처리
    document.getElementById('pet-selection-grid')?.addEventListener('click', (e) => {
      const card = e.target.closest('.pet-option-card');
      if (card) {
        document.querySelectorAll('.pet-option-card').forEach(c => c.classList.remove('selected'));
        card.classList.add('selected');
        this.selectedPetId = card.dataset.id;
        
        const petName = card.querySelector('strong')?.textContent || 'HPet';
        window.hpetStore.state.pet.id = this.selectedPetId;
        window.hpetStore.state.pet.name = petName;
        window.hpetSound.playBeep(700, 0.08);
      }
    });
  }

  showCustomSupplementModal() {
    const modal = document.getElementById('modal-custom-supp');
    if (modal) modal.classList.remove('hidden');
  }

  renderRecommendations() {
    const container = document.getElementById('recommend-list');
    if (!container) return;

    // 프로필 고민 기반 동적 AI 추천 알고리즘 (Mock)
    const recommendations = [
      { name: '비타민 C', reason: '선택하신 [피로 회복]을 위해 필수', icon: '🍋', selected: true },
      { name: '마그네슘', reason: '선택하신 [거북목/목통증] 근육 이완에 도움', icon: '⚡', selected: true },
      { name: '루테인', reason: '[눈 피로] 완화 및 영양 공급', icon: '👁️', selected: false }
    ];

    // 기존 스토어 영양제 목록과 합성
    const existingSupps = window.hpetStore.state.supplements;
    
    container.innerHTML = recommendations.map((item, idx) => `
      <div class="recommend-card ${item.selected ? 'selected' : ''}" data-idx="${idx}">
        <div class="supp-icon">${item.icon}</div>
        <div class="supp-info">
          <div class="supp-name">${item.name}</div>
          <div class="supp-reason">${item.reason}</div>
        </div>
        <i class="fa-solid ${item.selected ? 'fa-circle-check text-primary' : 'fa-circle-plus'}"></i>
      </div>
    `).join('');

    // 추가된 사용자 설정 영양제도 함께 표시
    existingSupps.forEach(supp => {
      if (!recommendations.some(r => r.name === supp.name)) {
        const div = document.createElement('div');
        div.className = 'recommend-card selected';
        div.innerHTML = `
          <div class="supp-icon">💊</div>
          <div class="supp-info">
            <div class="supp-name">${supp.name}</div>
            <div class="supp-reason">사용자 직접 등록 영양제 (${supp.time})</div>
          </div>
          <i class="fa-solid fa-circle-check text-primary"></i>
        `;
        container.appendChild(div);
      }
    });

    // 선택 토글 인터랙션
    container.querySelectorAll('.recommend-card').forEach(card => {
      card.addEventListener('click', () => {
        card.classList.toggle('selected');
        const icon = card.querySelector('i');
        if (card.classList.contains('selected')) {
          icon.className = 'fa-solid fa-circle-check text-primary';
        } else {
          icon.className = 'fa-solid fa-circle-plus';
        }
      });
    });
  }
}

window.hpetProfileSetup = new HPetProfileSetupManager();

document.addEventListener('DOMContentLoaded', () => {
  window.hpetProfileSetup.init();
});
