/**
 * HPet - 영양제 AI 카메라 촬영 인증 & 포션 보상 시스템 (Stage 5)
 * 
 * 웹캠 바인딩, 사진 캡처, AI 스캔 시뮬레이션, 포션 보상 지급 처리
 */

class HPetSupplementAuth {
  constructor() {
    this.fileInput = null;        // <input type="file">
    this.previewImg = null;       // <img>
    this.placeholder = null;      // 업로드 안내 영역
    this.btnAnalyze = null;       // 분석 버튼
    this.isScanning = false;      // AI 분석 진행 중 여부
    this.currentSuppId = null;    // 현재 인증 대상 영양제 ID
  }

  init() {
    this.fileInput = document.getElementById('supp-image-input');
    this.previewImg = document.getElementById('supp-image-preview');
    this.placeholder = document.getElementById('upload-placeholder');
    this.btnAnalyze = document.getElementById('btn-analyze');
    this.bindEvents();
  }

  bindEvents() {
    // 플레이스홀더 클릭 시 파일 입력 창 띄우기
    this.placeholder?.addEventListener('click', () => {
      this.fileInput?.click();
    });

    // 미리보기 이미지 클릭 시 다시 파일 선택
    this.previewImg?.addEventListener('click', () => {
      if (!this.isScanning) {
        this.fileInput?.click();
      }
    });

    // 파일 선택 시 미리보기 적용
    this.fileInput?.addEventListener('change', (e) => {
      const file = e.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
          if (this.previewImg) {
            this.previewImg.src = e.target.result;
            this.previewImg.style.display = 'block';
          }
          if (this.placeholder) {
            this.placeholder.style.display = 'none';
          }
          if (this.btnAnalyze) {
            this.btnAnalyze.style.display = 'block';
          }
        };
        reader.readAsDataURL(file);
      }
    });

    // 분석 버튼 클릭 → AI 분석 시작
    this.btnAnalyze?.addEventListener('click', () => {
      if (!this.isScanning && this.previewImg?.src) {
        this.captureAndAnalyze();
      }
    });

    // 뷰 진입/퇴장 시 초기화
    window.addEventListener('hpet_view_enter_cameraAuth', () => this.resetAuthView());
    window.addEventListener('hpet_view_leave_cameraAuth', () => this.resetAuthView());
  }

  // ── 뷰 상태 초기화 ──
  resetAuthView() {
    if (this.fileInput) this.fileInput.value = '';
    if (this.previewImg) {
      this.previewImg.src = '';
      this.previewImg.style.display = 'none';
    }
    if (this.placeholder) this.placeholder.style.display = 'block';
    if (this.btnAnalyze) this.btnAnalyze.style.display = 'none';
    this.isScanning = false;
  }

  // ── AI 분석 시뮬레이션 ──
  captureAndAnalyze() {
    this.isScanning = true;
    window.hpetSound.playBeep(1000, 0.06);



    // 2) AI 스캔 오버레이 표시
    const scanOverlay = document.getElementById('ai-scanning-overlay');
    if (scanOverlay) scanOverlay.classList.remove('hidden');

    // 3) 2초 뒤 분석 완료 시뮬레이션 (성공률 85%)
    setTimeout(() => {
      const isSuccess = Math.random() < 0.85;

      if (scanOverlay) scanOverlay.classList.add('hidden');
      this.isScanning = false;

      if (isSuccess) {
        this.onAuthSuccess();
      } else {
        this.onAuthFail();
      }
    }, 2200);
  }

  // ── 인증 성공 처리 ──
  onAuthSuccess() {
    window.hpetSound.playSuccess();

    // 아직 인증 안 된 첫 번째 영양제를 자동 인증 처리
    const state = window.hpetStore.state;
    const unverified = state.supplements.find(s => !s.takenToday);
    if (unverified) {
      window.hpetStore.markSupplementTaken(unverified.id);
    }

    // 포션 애니메이션 재생 (4초 후 원래대로)
    if (window.hpetCharacter) {
      window.hpetCharacter.playMotion('Potion', 4000);
    }

    // 보상 모달 표시
    const rewardTitle = document.getElementById('reward-title');
    const rewardDesc = document.getElementById('reward-desc');
    const rewardIcon = document.getElementById('reward-icon');
    const modal = document.getElementById('modal-reward');

    if (rewardTitle) rewardTitle.textContent = '복용 인증 성공!';
    if (rewardDesc) {
      const suppName = unverified ? unverified.name : '영양제';
      rewardDesc.textContent = `${suppName} 복용이 확인되었습니다! 포션 +20 획득 🧪`;
    }
    if (rewardIcon) rewardIcon.textContent = '🧪';
    if (modal) modal.classList.remove('hidden');

    // 보상 모달 닫힘 시 대시보드로 복귀
    const closeBtn = document.getElementById('btn-close-reward');
    const originalHandler = closeBtn?.onclick;

    if (closeBtn) {
      closeBtn.onclick = () => {
        modal.classList.add('hidden');
        this.resetAuthView();
        window.hpetRouter.navigateTo('dashboard');
      };
    }
  }

  // ── 인증 실패 처리 ──
  onAuthFail() {
    window.hpetSound.playBeep(220, 0.2, 'sawtooth');

    const rewardTitle = document.getElementById('reward-title');
    const rewardDesc = document.getElementById('reward-desc');
    const rewardIcon = document.getElementById('reward-icon');
    const modal = document.getElementById('modal-reward');

    if (rewardTitle) rewardTitle.textContent = '인증 실패 😢';
    if (rewardDesc) rewardDesc.textContent = '영양제를 정확히 인식하지 못했습니다. 라벨이 보이게 다시 촬영해주세요!';
    if (rewardIcon) rewardIcon.textContent = '📷';
    if (modal) modal.classList.remove('hidden');

    // 실패 시 모달 닫으면 카메라 화면 유지
    const closeBtn = document.getElementById('btn-close-reward');
    if (closeBtn) {
      closeBtn.onclick = () => {
        modal.classList.add('hidden');
      };
    }
  }
}

window.hpetSuppAuth = new HPetSupplementAuth();

document.addEventListener('DOMContentLoaded', () => {
  window.hpetSuppAuth.init();
});
