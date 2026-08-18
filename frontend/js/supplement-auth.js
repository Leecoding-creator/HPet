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
    window.addEventListener('hpet_view_enter_cameraAuth', () => {
      this.resetAuthView();
      this.loadCurrentTarget();
    });
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

  // 카메라 화면에는 인증할 영양제를 고르는 UI가 따로 없어서, 대시보드와 동일한 기준(오늘 아직
  // 인증 안 된 첫 번째 등록 영양제)으로 인증 대상을 정한다 (GET /api/home/summary 재사용).
  async loadCurrentTarget() {
    this.currentSuppId = null;
    this.currentSuppName = null;
    try {
      const summary = await window.hpetApi.getHomeSummary();
      const doseList = (summary.doseSummary && summary.doseSummary.doseList) || [];
      const target = doseList.find(d => !d.completed);
      if (target) {
        this.currentSuppId = target.userSupplementId;
        this.currentSuppName = target.supplementName;
      }
    } catch (err) {
      console.error('인증 대상 영양제 조회 실패', err);
    }
  }

  // ── AI 분석 (실제 백엔드 호출) ──
  async captureAndAnalyze() {
    if (!this.currentSuppId) {
      alert('오늘 인증할 영양제가 없습니다. 대시보드에서 영양제를 등록했는지 확인해주세요.');
      return;
    }

    const file = this.fileInput?.files[0];
    if (!file) return;

    this.isScanning = true;
    window.hpetSound.playBeep(1000, 0.06);

    const scanOverlay = document.getElementById('ai-scanning-overlay');
    if (scanOverlay) scanOverlay.classList.remove('hidden');

    try {
      const result = await window.hpetApi.verifyDosePhoto(this.currentSuppId, file);
      if (scanOverlay) scanOverlay.classList.add('hidden');
      this.isScanning = false;

      if (result.verified) {
        this.onAuthSuccess(result);
      } else {
        this.onAuthFail(result.reason);
      }
    } catch (err) {
      if (scanOverlay) scanOverlay.classList.add('hidden');
      this.isScanning = false;
      this.onAuthFail(err.message || 'AI 판정 요청에 실패했습니다.');
    }
  }

  // ── 인증 성공 처리 ──
  onAuthSuccess(result) {
    window.hpetSound.playSuccess();

    // 포션 애니메이션 재생 (4초 후 원래대로)
    if (window.hpetCharacter) {
      window.hpetCharacter.playMotion('Potion', 4000);
    }

    // 보상 모달 표시 (실제 응답의 획득 포인트를 그대로 사용)
    const rewardTitle = document.getElementById('reward-title');
    const rewardDesc = document.getElementById('reward-desc');
    const rewardIcon = document.getElementById('reward-icon');
    const modal = document.getElementById('modal-reward');

    if (rewardTitle) rewardTitle.textContent = '복용 인증 성공!';
    if (rewardDesc) {
      const suppName = this.currentSuppName || '영양제';
      rewardDesc.textContent = `${suppName} 복용이 확인되었습니다! 성장치 +${result.pointsGainedThisTime} (오늘 ${result.todayEarnedPoints}/${result.dailyMaxPoints}) 🧪`;
    }
    if (rewardIcon) rewardIcon.textContent = '🧪';
    if (modal) modal.classList.remove('hidden');

    // 보상 모달 닫힘 시 대시보드로 복귀
    const closeBtn = document.getElementById('btn-close-reward');
    if (closeBtn) {
      closeBtn.onclick = () => {
        modal.classList.add('hidden');
        this.resetAuthView();
        window.hpetRouter.navigateTo('dashboard');
      };
    }
  }

  // ── 인증 실패 처리 ──
  onAuthFail(reason) {
    window.hpetSound.playBeep(220, 0.2, 'sawtooth');

    const rewardTitle = document.getElementById('reward-title');
    const rewardDesc = document.getElementById('reward-desc');
    const rewardIcon = document.getElementById('reward-icon');
    const modal = document.getElementById('modal-reward');

    if (rewardTitle) rewardTitle.textContent = '인증 실패 😢';
    if (rewardDesc) rewardDesc.textContent = reason || '영양제를 정확히 인식하지 못했습니다. 라벨이 보이게 다시 촬영해주세요!';
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
