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

    // 권한 요청 동의 버튼들
    document.getElementById('btn-grant-permissions')?.addEventListener('click', () => {
      this.grantAllPermissions();
    });
  }

  showForgotPwModal() {
    const modal = document.getElementById('modal-forgot-pw');
    if (modal) modal.classList.remove('hidden');
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
