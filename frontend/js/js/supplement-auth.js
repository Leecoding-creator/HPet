/**
 * HPet - 영양제 AI 카메라 촬영 인증 & 포션 보상 시스템 (Stage 5)
 * 
 * 웹캠 바인딩, 사진 캡처, AI 스캔 시뮬레이션, 포션 보상 지급 처리
 */

class HPetSupplementAuth {
  constructor() {
    this.stream = null;           // 웹캠 MediaStream
    this.videoEl = null;          // <video> 요소
    this.canvasEl = null;         // 스냅샷용 <canvas>
    this.isScanning = false;      // AI 분석 진행 중 여부
    this.currentSuppId = null;    // 현재 인증 대상 영양제 ID
  }

  init() {
    this.videoEl = document.getElementById('webcam-preview');
    this.canvasEl = document.getElementById('snapshot-canvas');
    this.bindEvents();
  }

  bindEvents() {
    // 셔터 버튼 클릭 → 촬영 & AI 분석 시작
    document.getElementById('btn-shutter')?.addEventListener('click', () => {
      if (!this.isScanning) {
        this.captureAndAnalyze();
      }
    });

    // 카메라 뷰 진입/퇴장 시 스트림 관리
    // navigateTo에서 호출되도록 전역 이벤트 사용
    window.addEventListener('hpet_view_enter_cameraAuth', () => this.startCamera());
    window.addEventListener('hpet_view_leave_cameraAuth', () => this.stopCamera());
  }

  // ── 웹캠 스트림 시작 ──
  async startCamera() {
    try {
      // 이전 스트림이 남아있으면 정리
      this.stopCamera();

      this.stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment', width: 640, height: 480 },
        audio: false
      });

      if (this.videoEl) {
        this.videoEl.srcObject = this.stream;
        this.videoEl.play();
      }
    } catch (err) {
      console.warn('카메라 접근 실패, 데모 모드로 전환:', err.message);
      // 카메라 없는 환경(데스크톱 등)에서도 인증 시뮬레이션 가능하도록 처리
      this.showDemoPlaceholder();
    }
  }

  // ── 웹캠 스트림 정지 ──
  stopCamera() {
    if (this.stream) {
      this.stream.getTracks().forEach(track => track.stop());
      this.stream = null;
    }
    if (this.videoEl) {
      this.videoEl.srcObject = null;
    }
  }

  // ── 카메라 없을 때 데모 플레이스홀더 표시 ──
  showDemoPlaceholder() {
    const box = document.querySelector('.viewfinder-box');
    if (!box) return;

    // 이미 플레이스홀더가 있으면 중복 생성 방지
    if (box.querySelector('.demo-placeholder')) return;

    const placeholder = document.createElement('div');
    placeholder.className = 'demo-placeholder';
    placeholder.innerHTML = `
      <i class="fa-solid fa-camera-rotate" style="font-size:36px; color:#94a3b8;"></i>
      <p style="color:#94a3b8; font-size:13px; margin-top:8px;">카메라 미지원 환경</p>
      <p style="color:#64748b; font-size:11px;">셔터 버튼을 누르면 데모 인증이 진행됩니다</p>
    `;
    placeholder.style.cssText = `
      position:absolute; inset:0; display:flex; flex-direction:column;
      justify-content:center; align-items:center; background:#1e293b; z-index:1;
    `;
    box.appendChild(placeholder);
  }

  // ── 사진 캡처 & AI 분석 시뮬레이션 ──
  captureAndAnalyze() {
    this.isScanning = true;
    window.hpetSound.playBeep(1000, 0.06);

    // 1) 캔버스에 현재 프레임 캡처 (실제 카메라가 있을 때)
    if (this.canvasEl && this.videoEl && this.stream) {
      this.canvasEl.width = this.videoEl.videoWidth || 640;
      this.canvasEl.height = this.videoEl.videoHeight || 480;
      const ctx = this.canvasEl.getContext('2d');
      ctx.drawImage(this.videoEl, 0, 0);
    }

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
        this.stopCamera();
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
