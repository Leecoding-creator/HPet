/**
 * HPet - Auth & Onboarding Permission Management (Stage 2)
 */

class HPetAuthManager {
  constructor() {
    this.permissions = {
      camera: false,
      notification: false,
      healthData: false
    };
  }

  init() {
    this.bindEvents();
  }

  bindEvents() {
    // 비밀번호 재설정 버튼
    document.getElementById('btn-forgot-pw')?.addEventListener('click', () => {
      this.showForgotPwModal();
    });

    // 1단계: 이메일로 재설정 토큰 요청
    document.getElementById('btn-request-reset')?.addEventListener('click', async (e) => {
      const btn = e.currentTarget;
      const emailInput = document.getElementById('forgot-pw-email');
      const errEl = document.getElementById('forgot-pw-request-error');
      const email = emailInput.value.trim();

      errEl.classList.add('hidden');
      btn.disabled = true;
      try {
        await window.hpetApi.requestPasswordReset(email);
        window.hpetSound.playSuccess();
        document.getElementById('forgot-pw-step1').classList.add('hidden');
        document.getElementById('forgot-pw-step2').classList.remove('hidden');
      } catch (err) {
        errEl.textContent = err.message || '재설정 요청에 실패했습니다.';
        errEl.classList.remove('hidden');
      } finally {
        btn.disabled = false;
      }
    });

    // 2단계: 토큰 + 새 비밀번호로 실제 재설정
    document.getElementById('btn-confirm-reset')?.addEventListener('click', async (e) => {
      const btn = e.currentTarget;
      const token = document.getElementById('forgot-pw-token').value.trim();
      const newPassword = document.getElementById('forgot-pw-new-password').value;
      const errEl = document.getElementById('forgot-pw-confirm-error');

      errEl.classList.add('hidden');
      btn.disabled = true;
      try {
        await window.hpetApi.confirmPasswordReset(token, newPassword);
        window.hpetSound.playSuccess();
        alert('비밀번호가 변경되었습니다. 새 비밀번호로 다시 로그인해주세요.');
        document.getElementById('modal-forgot-pw').classList.add('hidden');
      } catch (err) {
        errEl.textContent = err.message || '비밀번호 재설정에 실패했습니다.';
        errEl.classList.remove('hidden');
      } finally {
        btn.disabled = false;
      }
    });

    // 취소 버튼 (1단계/2단계 공통 - 모달 닫기)
    document.getElementById('btn-cancel-forgot-pw')?.addEventListener('click', () => {
      document.getElementById('modal-forgot-pw').classList.add('hidden');
    });
    document.getElementById('btn-cancel-forgot-pw-2')?.addEventListener('click', () => {
      document.getElementById('modal-forgot-pw').classList.add('hidden');
    });

    // 권한 요청 동의 버튼들
    document.getElementById('btn-grant-permissions')?.addEventListener('click', () => {
      this.grantAllPermissions();
    });
  }

  showForgotPwModal() {
    const modal = document.getElementById('modal-forgot-pw');
    if (!modal) return;

    // 모달을 열 때마다 1단계로 초기화 (이전 시도의 입력값/에러 잔상 제거)
    document.getElementById('forgot-pw-step1').classList.remove('hidden');
    document.getElementById('forgot-pw-step2').classList.add('hidden');
    document.getElementById('forgot-pw-email').value = '';
    document.getElementById('forgot-pw-token').value = '';
    document.getElementById('forgot-pw-new-password').value = '';
    document.getElementById('forgot-pw-request-error').classList.add('hidden');
    document.getElementById('forgot-pw-confirm-error').classList.add('hidden');

    modal.classList.remove('hidden');
  }

  showPermissionModal(onComplete) {
    const modal = document.getElementById('modal-permissions');
    if (!modal) {
      if (onComplete) onComplete();
      return;
    }

    modal.classList.remove('hidden');
    
    // 권한 체크박스/스위치 핸들링
    const btnGrant = document.getElementById('btn-grant-permissions');
    btnGrant.onclick = () => {
      this.permissions.camera = document.getElementById('perm-camera')?.checked || true;
      this.permissions.notification = document.getElementById('perm-notification')?.checked || true;
      this.permissions.healthData = document.getElementById('perm-health')?.checked || true;

      window.hpetStore.state.permissions = this.permissions;
      window.hpetStore.saveState();
      window.hpetSound.playSuccess();

      modal.classList.add('hidden');
      if (onComplete) onComplete();
    };
  }

  grantAllPermissions() {
    this.permissions = { camera: true, notification: true, healthData: true };
    window.hpetStore.state.permissions = this.permissions;
    window.hpetStore.saveState();
  }
}

window.hpetAuth = new HPetAuthManager();

document.addEventListener('DOMContentLoaded', () => {
  window.hpetAuth.init();
});
