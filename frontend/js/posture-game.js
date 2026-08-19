/**
 * HPet - 자세 분석 & 거북목 교정 미니게임 (Stage 6)
 * 
 * 웹캠 기반 자세 측정 시뮬레이션, 거북목 경고 모달, 교정 타이머 게임
 */

// 백엔드는 LocalDateTime(타임존 없는 "YYYY-MM-DDTHH:mm:ss")을 기대하므로,
// Date.toISOString()의 UTC/밀리초/Z 표기를 쓰지 않고 로컬 시각 기준으로 직접 포맷한다.
function toLocalIsoString(date) {
  const pad = n => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

class HPetPostureGame {
  constructor() {
    this.stream = null;
    this.videoEl = null;
    this.timerInterval = null;
    this.remainSeconds = 30;
    this.isPlaying = false;
    this.goodPostureStreak = 0;     // 바른 자세 연속 유지 카운트
    this.turtleDetectCount = 0;     // 미니게임 중 거북목 감지 횟수
  }

  init() {
    this.videoEl = document.getElementById('posture-webcam');
    this.bindEvents();
  }

  bindEvents() {
    // 미니게임 시작 버튼
    document.getElementById('btn-start-posture-game')?.addEventListener('click', () => {
      if (!this.isPlaying) this.startGame();
    });
    // 미니게임 중지 버튼
    document.getElementById('btn-stop-posture-game')?.addEventListener('click', () => {
      if (this.isPlaying) this.resetGame();
    });

    // 카메라 뷰 진입/퇴장 시 스트림 관리
    window.addEventListener('hpet_view_enter_postureGame', () => this.startPostureCamera());
    window.addEventListener('hpet_view_leave_postureGame', () => this.resetGame());
  }

  // ── 자세 게임용 웹캠 시작 ──
  async startPostureCamera() {
    try {
      this.stopGame();

      this.stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'user', width: 640, height: 480 },
        audio: false
      });

      if (this.videoEl) {
        this.videoEl.srcObject = this.stream;
        this.videoEl.play();
      }
    } catch (err) {
      console.warn('카메라 접근 실패, 데모 모드로 전환:', err.message);
      this.showPostureDemoPlaceholder();
    }
  }

  showPostureDemoPlaceholder() {
    const box = document.querySelector('.game-canvas-area');
    if (!box || box.querySelector('.demo-placeholder')) return;

    const placeholder = document.createElement('div');
    placeholder.className = 'demo-placeholder';
    placeholder.innerHTML = `
      <i class="fa-solid fa-child-reaching" style="font-size:36px; color:#94a3b8;"></i>
      <p style="color:#94a3b8; font-size:13px; margin-top:8px;">카메라 미지원 환경</p>
      <p style="color:#64748b; font-size:11px;">시작 버튼을 누르면 데모 미니게임이 진행됩니다</p>
    `;
    placeholder.style.cssText = `
      position:absolute; inset:0; display:flex; flex-direction:column;
      justify-content:center; align-items:center; background:#1e293b; z-index:1;
    `;
    box.appendChild(placeholder);
  }

  // ── 미니게임 시작 ──
  startGame() {
    this.isPlaying = true;
    this.remainSeconds = 30;
    this.goodPostureStreak = 0;
    this.turtleDetectCount = 0;

    window.hpetSound.playBeep(523.25, 0.1);
    setTimeout(() => window.hpetSound.playBeep(659.25, 0.1), 150);
    setTimeout(() => window.hpetSound.playBeep(783.99, 0.15), 300);

    // 시작 버튼 비활성화 및 중지 버튼 표시
    const startBtn = document.getElementById('btn-start-posture-game');
    const stopBtn = document.getElementById('btn-stop-posture-game');
    if (startBtn) {
      startBtn.classList.add('hidden');
    }
    if (stopBtn) {
      stopBtn.classList.remove('hidden');
    }

    // 타이머 카운트다운
    this.updateTimerDisplay();
    this.timerInterval = setInterval(() => {
      this.remainSeconds--;
      this.updateTimerDisplay();

      // 2초마다 자세 판정 시뮬레이션 (랜덤 기반)
      if (this.remainSeconds % 2 === 0) {
        this.simulatePostureCheck();
      }

      // 타이머 종료
      if (this.remainSeconds <= 0) {
        this.endGame();
      }
    }, 1000);
  }

  // ── 자세 판정 시뮬레이션 ──
  simulatePostureCheck() {
    // 80% 확률로 바른 자세, 20% 확률로 거북목 감지 (데모)
    const isGoodPosture = Math.random() < 0.80;
    const indicator = document.getElementById('posture-indicator');
    const statusText = document.getElementById('posture-status-text');
    const turtleBanner = document.getElementById('turtle-warning');

    if (isGoodPosture) {
      this.goodPostureStreak++;
      
      if (indicator) {
        indicator.className = 'status-indicator good';
        indicator.querySelector('i').className = 'fa-solid fa-face-smile';
      }
      if (statusText) statusText.textContent = '바른 자세 유지 중! 👍';
      if (turtleBanner) turtleBanner.classList.add('hidden');

      // 캐릭터 정상 복귀
      if (window.hpetCharacter) {
        window.hpetCharacter.setTurtleNeckAlert(false);
      }
    } else {
      this.turtleDetectCount++;
      this.goodPostureStreak = 0;
      
      if (indicator) {
        indicator.className = 'status-indicator bad';
        indicator.querySelector('i').className = 'fa-solid fa-face-frown';
      }
      if (statusText) statusText.textContent = '거북목 감지! 턱을 당기세요! ⚠️';
      if (turtleBanner) turtleBanner.classList.remove('hidden');

      window.hpetSound.playBeep(220, 0.15, 'square');

      // 캐릭터 거북목 경고 모드
      if (window.hpetCharacter) {
        window.hpetCharacter.setTurtleNeckAlert(true);
      }

      // 판정된 순간을 실제 백엔드에 기록 (게임 템포를 막지 않도록 결과를 기다리지 않음).
      // 사진 한 장 기반 판정과 동일하게 각도/지속시간은 측정 불가라 0으로 남긴다.
      window.hpetApi.createPostureEvent(toLocalIsoString(new Date()), 0, 0)
        .catch(err => console.error('자세 불량 이벤트 기록 실패', err));

      // 2초 뒤 경고 배너 자동 숨김
      setTimeout(() => {
        if (turtleBanner) turtleBanner.classList.add('hidden');
      }, 2000);
    }
  }

  // ── 타이머 디스플레이 업데이트 ──
  updateTimerDisplay() {
    const timerEl = document.getElementById('game-timer');
    if (!timerEl) return;

    const mins = String(Math.floor(this.remainSeconds / 60)).padStart(2, '0');
    const secs = String(this.remainSeconds % 60).padStart(2, '0');
    timerEl.textContent = `${mins}:${secs}`;

    // 10초 미만이면 강조 색상
    if (this.remainSeconds <= 10) {
      timerEl.style.color = '#f43f5e';
    } else {
      timerEl.style.color = '';
    }
  }

  // ── 미니게임 종료 & 보상 처리 ──
  endGame() {
    clearInterval(this.timerInterval);
    this.timerInterval = null;
    this.isPlaying = false;

    // 시작 버튼 복원
    const startBtn = document.getElementById('btn-start-posture-game');
    const stopBtn = document.getElementById('btn-stop-posture-game');
    if (startBtn) {
      startBtn.innerHTML = '미니게임 다시 시작';
      startBtn.disabled = false;
      startBtn.style.opacity = '1';
      startBtn.classList.remove('hidden');
    }
    if (stopBtn) {
      stopBtn.classList.add('hidden');
    }

    // 거북목 감지가 3회 미만이면 성공 판정
    const isSuccess = this.turtleDetectCount < 3;

    if (isSuccess) {
      // 보상 지급: 자세 건강도 +15, 경험치 +50
      window.hpetStore.updatePostureHealth(15);
      window.hpetStore.updatePetExp(50);
      window.hpetStore.state.stats.postureGamesCleared++;
      window.hpetStore.saveState();
      window.hpetSound.playSuccess();

      // 캐릭터 정상 복귀 및 기뻐하는 애니메이션 재생
      if (window.hpetCharacter) {
        window.hpetCharacter.setTurtleNeckAlert(false);
        window.hpetCharacter.playMotion('Happiness', 4000);
      }

      this.showResultModal(
        '미니게임 클리어! 🎉',
        `바른 자세를 훌륭하게 유지했습니다!\n자세 건강도 +15, 성장 포션 +50 획득!`,
        '🏆'
      );
    } else {
      // 실패 시에도 소량 보상
      window.hpetStore.updatePostureHealth(5);
      window.hpetStore.updatePetExp(10);
      window.hpetStore.state.stats.turtleNeckDetectionsThisWeek += this.turtleDetectCount;
      window.hpetStore.saveState();

      window.hpetSound.playBeep(330, 0.2, 'triangle');

      if (window.hpetCharacter) {
        window.hpetCharacter.setTurtleNeckAlert(false);
      }

      this.showResultModal(
        '아쉬워요! 다시 도전해봐요 💪',
        `거북목 ${this.turtleDetectCount}회 감지됐어요.\n다음엔 더 좋은 자세를 유지해보세요!\n자세 건강도 +5, 성장 포션 +10 획득`,
        '🐢'
      );
    }
  }

  // ── 결과 모달 표시 ──
  showResultModal(title, desc, icon) {
    const rewardTitle = document.getElementById('reward-title');
    const rewardDesc = document.getElementById('reward-desc');
    const rewardIcon = document.getElementById('reward-icon');
    const modal = document.getElementById('modal-reward');

    if (rewardTitle) rewardTitle.textContent = title;
    if (rewardDesc) rewardDesc.textContent = desc;
    if (rewardIcon) rewardIcon.textContent = icon;
    if (modal) modal.classList.remove('hidden');

    const closeBtn = document.getElementById('btn-close-reward');
    if (closeBtn) {
      closeBtn.onclick = () => {
        modal.classList.add('hidden');
        this.stopGame();
        window.hpetRouter.navigateTo('dashboard');
      };
    }
  }

  // ── 게임 정지 & 카메라 해제 ──
  stopGame() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
    this.isPlaying = false;

    if (this.stream) {
      this.stream.getTracks().forEach(track => track.stop());
      this.stream = null;
    }
    if (this.videoEl) {
      this.videoEl.srcObject = null;
    }
  }

  // ── 게임 초기화 (중지 버튼 클릭 혹은 뷰 이탈 시) ──
  resetGame() {
    this.stopGame();

    this.remainSeconds = 30;
    this.updateTimerDisplay();

    // 시작/중지 버튼 토글
    const startBtn = document.getElementById('btn-start-posture-game');
    const stopBtn = document.getElementById('btn-stop-posture-game');
    if (startBtn) {
      startBtn.textContent = '미니게임 시작';
      startBtn.disabled = false;
      startBtn.style.opacity = '1';
      startBtn.classList.remove('hidden');
    }
    if (stopBtn) {
      stopBtn.classList.add('hidden');
    }

    // 상태 초기화
    const indicator = document.getElementById('posture-indicator');
    const statusText = document.getElementById('posture-status-text');
    const turtleBanner = document.getElementById('turtle-warning');

    if (indicator) {
      indicator.className = 'status-indicator good';
      indicator.querySelector('i').className = 'fa-solid fa-face-smile';
    }
    if (statusText) statusText.textContent = '바른 자세 유지 중!';
    if (turtleBanner) turtleBanner.classList.add('hidden');
  }
}

window.hpetPostureGame = new HPetPostureGame();

document.addEventListener('DOMContentLoaded', () => {
  window.hpetPostureGame.init();
});
