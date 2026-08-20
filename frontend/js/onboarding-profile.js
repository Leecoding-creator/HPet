/**
 * HPet - Onboarding Profile Setup & AI Supplement Recommendation (Stage 2/3)
 */

// 연령대 chip 값 → 대략적인 생년월일 (백엔드 추천 로직의 나이 규칙은 정확한 나이가 아니라
// "40세 이상인지"만 보므로, 각 구간의 중간 나이로 근사해도 규칙 판정에는 문제없다)
const AGE_GROUP_TO_APPROX_AGE = { '10s': 15, '20s': 25, '30s': 35, '40s+': 45 };

// 성별 chip 값 → 백엔드 Gender enum
const GENDER_VALUE_TO_ENUM = { female: 'FEMALE', male: 'MALE', other: 'OTHER' };

// 건강 고민 chip 값 → 백엔드 추천 로직이 메모에서 찾는 키워드로 변환
// (RecommendationService가 자유 텍스트 memo에서 키워드를 매칭하는 방식이라, 선택한 고민을
//  한글 문구로 이어붙여 memo로 보낸다. eye/immunity는 대응되는 규칙이 없어 그대로 전달만 한다)
const CONCERN_VALUE_TO_LABEL = {
  fatigue: '피로 회복',
  'turtle-neck': '거북목 통증',
  eye: '눈 피로',
  gut: '장 건강 소화',
  immunity: '면역력'
};

class HPetProfileSetupManager {
  constructor() {
    this.selectedAge = '20s';
    this.selectedGender = 'female';
    this.selectedConcerns = ['fatigue', 'turtle-neck'];
    this.selectedSupplements = [];
    this.selectedPetId = 'pet_cat';
    // 온보딩 중 "직접 등록"으로 추가한 영양제 (실제 등록은 2→3단계 전환 시 일괄 처리)
    this.customAddedSupplements = [];
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

    // 커스텀 영양제 폼 제출 (온보딩 전용 - 대시보드에서 연 모달은 dashboard.js가 처리하므로 여기선 무시)
    document.getElementById('form-add-supp')?.addEventListener('click', async (e) => {
      if (e.target.id !== 'btn-save-custom-supp') return;
      const modal = document.getElementById('modal-custom-supp');
      if (modal && modal.dataset.context === 'dashboard') return;

      const nameInput = document.getElementById('custom-supp-name');
      const timeInput = document.getElementById('custom-supp-time');
      const name = nameInput ? nameInput.value.trim() : '';
      if (!name) return;

      this.customAddedSupplements.push({ name, time: timeInput ? timeInput.value : '09:00' });
      this.renderRecommendationCards();
      if (modal) modal.classList.add('hidden');
      window.hpetSound.playSuccess();
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

  // 1단계 → 2단계: 건강 프로필을 실제로 저장하고, 그 프로필 기반 추천을 받아온다.
  async loadRecommendations() {
    const ageYears = AGE_GROUP_TO_APPROX_AGE[this.selectedAge] || 25;
    const birthYear = new Date().getFullYear() - ageYears;

    const profile = {
      birthDate: `${birthYear}-01-01`,
      gender: GENDER_VALUE_TO_ENUM[this.selectedGender] || 'OTHER',
      memo: this.selectedConcerns.map(c => CONCERN_VALUE_TO_LABEL[c]).filter(Boolean).join(', ')
    };

    await window.hpetApi.saveHealthProfile(profile);
    this.lastRecommendations = await window.hpetApi.getRecommendations();
    this.renderRecommendationCards();
  }

  renderRecommendationCards() {
    const container = document.getElementById('recommend-list');
    if (!container) return;

    const apiRecs = this.lastRecommendations || [];

    container.innerHTML = apiRecs.map(item => `
      <div class="recommend-card selected" data-name="${item.supplementName}">
        <div class="supp-icon">💊</div>
        <div class="supp-info">
          <div class="supp-name">${item.supplementName}</div>
          <div class="supp-reason">${item.reason}</div>
        </div>
        <i class="fa-solid fa-circle-check text-primary"></i>
      </div>
    `).join('');

    // 온보딩 중 직접 등록한 영양제도 함께 표시
    this.customAddedSupplements.forEach(supp => {
      if (apiRecs.some(r => r.supplementName === supp.name)) return;
      const div = document.createElement('div');
      div.className = 'recommend-card selected';
      div.dataset.name = supp.name;
      div.innerHTML = `
        <div class="supp-icon">💊</div>
        <div class="supp-info">
          <div class="supp-name">${supp.name}</div>
          <div class="supp-reason">사용자 직접 등록 영양제 (${supp.time})</div>
        </div>
        <i class="fa-solid fa-circle-check text-primary"></i>
      `;
      container.appendChild(div);
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

  // 2단계 → 3단계: 선택된 영양제를 실제로 등록하고, 그 결과로 배정된 실제 캐릭터를 가져온다.
  async registerSelectedSupplementsAndAssignCharacter() {
    const selectedCards = document.querySelectorAll('#recommend-list .recommend-card.selected');
    const names = Array.from(selectedCards).map(c => c.dataset.name).filter(Boolean);

    for (const name of names) {
      const custom = this.customAddedSupplements.find(s => s.name === name);
      const doseTime = custom ? custom.time : '09:00';
      await window.hpetApi.addUserSupplement(name, doseTime);
    }

    try {
      return await window.hpetApi.getMyCharacter();
    } catch (err) {
      // 등록한 영양제가 매핑표(철분/칼슘/오메가3/비타민/마그네슘/멜라토닌/비오틴)에
      // 하나도 안 걸리면(예: 유산균만 선택) 캐릭터가 아직 배정되지 않는다 - 정상 흐름이다.
      return null;
    }
  }
}

window.hpetProfileSetup = new HPetProfileSetupManager();

document.addEventListener('DOMContentLoaded', () => {
  window.hpetProfileSetup.init();
});
