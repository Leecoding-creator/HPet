/**
 * HPet - Backend API Client
 * 백엔드는 { "success": true, "data": {...} } / { "success": false, "error": {...} } 포맷으로 응답한다.
 */

const HPET_API_BASE = 'https://1.201.117.185';
const HPET_TOKEN_KEY = 'HPET_ACCESS_TOKEN';
const HPET_REFRESH_KEY = 'HPET_REFRESH_TOKEN';

class HPetApiError extends Error {
  constructor(message, code, status) {
    super(message);
    this.code = code;
    this.status = status;
  }
}

class HPetApiClient {
  getAccessToken() {
    return localStorage.getItem(HPET_TOKEN_KEY);
  }

  getRefreshToken() {
    return localStorage.getItem(HPET_REFRESH_KEY);
  }

  setTokens(tokens) {
    if (!tokens) return;
    if (tokens.accessToken) localStorage.setItem(HPET_TOKEN_KEY, tokens.accessToken);
    if (tokens.refreshToken) localStorage.setItem(HPET_REFRESH_KEY, tokens.refreshToken);
  }

  clearTokens() {
    localStorage.removeItem(HPET_TOKEN_KEY);
    localStorage.removeItem(HPET_REFRESH_KEY);
  }

  isLoggedIn() {
    return !!this.getAccessToken();
  }

  // 401 응답을 받으면 refreshToken으로 재발급을 한 번 시도하고, 성공하면 원 요청을 재시도한다.
  async request(method, path, { body, auth = true, retry = true } = {}) {
    const headers = { 'Content-Type': 'application/json' };
    if (auth) {
      const token = this.getAccessToken();
      if (token) headers['Authorization'] = `Bearer ${token}`;
    }

    let res;
    try {
      res = await fetch(`${HPET_API_BASE}${path}`, {
        method,
        headers,
        body: body !== undefined ? JSON.stringify(body) : undefined
      });
    } catch (e) {
      throw new HPetApiError('서버에 연결할 수 없습니다. 백엔드가 켜져 있는지 확인해주세요.', 'NETWORK_ERROR', 0);
    }

    if (res.status === 401 && auth && retry && this.getRefreshToken()) {
      const reissued = await this.tryReissue();
      if (reissued) {
        return this.request(method, path, { body, auth, retry: false });
      }
    }

    let payload = null;
    try {
      payload = await res.json();
    } catch (e) {
      // 본문 없음 (예: 204)
    }

    if (!res.ok || (payload && payload.success === false)) {
      const err = (payload && payload.error) || {};
      throw new HPetApiError(err.message || `요청에 실패했습니다. (${res.status})`, err.code, res.status);
    }

    return payload ? payload.data : null;
  }

  // multipart/form-data 요청 (사진 업로드용) - request()와 달리 JSON 직렬화/Content-Type을 하지 않는다.
  async requestMultipart(method, path, formData) {
    const headers = {};
    const token = this.getAccessToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;

    let res;
    try {
      res = await fetch(`${HPET_API_BASE}${path}`, { method, headers, body: formData });
    } catch (e) {
      throw new HPetApiError('서버에 연결할 수 없습니다. 백엔드가 켜져 있는지 확인해주세요.', 'NETWORK_ERROR', 0);
    }

    let payload = null;
    try {
      payload = await res.json();
    } catch (e) {
      // 본문 없음
    }

    if (!res.ok || (payload && payload.success === false)) {
      const err = (payload && payload.error) || {};
      throw new HPetApiError(err.message || `요청에 실패했습니다. (${res.status})`, err.code, res.status);
    }

    return payload ? payload.data : null;
  }

  async tryReissue() {
    try {
      const res = await fetch(`${HPET_API_BASE}/api/auth/reissue`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: this.getRefreshToken() })
      });
      const payload = await res.json().catch(() => null);
      if (res.ok && payload && payload.success) {
        this.setTokens(payload.data);
        return true;
      }
    } catch (e) {
      // ignore, fall through to clearing tokens below
    }
    this.clearTokens();
    return false;
  }

  signup(email, password) {
    return this.request('POST', '/api/auth/signup', { body: { email, password }, auth: false });
  }

  async login(email, password) {
    const data = await this.request('POST', '/api/auth/login', { body: { email, password }, auth: false });
    this.setTokens(data);
    return data;
  }

  requestPasswordReset(email) {
    return this.request('POST', '/api/auth/password-reset/request', { body: { email }, auth: false });
  }

  confirmPasswordReset(token, newPassword) {
    return this.request('POST', '/api/auth/password-reset/confirm', { body: { token, newPassword }, auth: false });
  }

  async logout() {
    try {
      await this.request('POST', '/api/auth/logout', {});
    } finally {
      this.clearTokens();
    }
  }

  getMyProfile() {
    return this.request('GET', '/api/users/me');
  }

  updateNickname(nickname) {
    return this.request('PUT', '/api/users/me/nickname', { body: { nickname } });
  }

  withdraw() {
    return this.request('DELETE', '/api/users/me');
  }

  getHomeSummary() {
    return this.request('GET', '/api/home/summary');
  }

  // 건강 프로필 & AI 추천
  saveHealthProfile(profile) {
    return this.request('POST', '/api/profile', { body: profile });
  }

  getHealthProfile() {
    return this.request('GET', '/api/profile/me');
  }

  getRecommendations() {
    return this.request('GET', '/api/profile/recommendations');
  }

  // 캐릭터
  getMyCharacter() {
    return this.request('GET', '/api/character/me');
  }

  // 영양제 API
  searchSupplements(keyword = '') {
    return this.request('GET', `/api/supplements${keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''}`);
  }

  getUserSupplements() {
    return this.request('GET', '/api/users/me/supplements');
  }

  addUserSupplement(customName, doseTime) {
    return this.request('POST', '/api/users/me/supplements', { body: { customName, doseTime } });
  }

  updateUserSupplement(userSupplementId, customName, doseTime) {
    return this.request('PUT', `/api/users/me/supplements/${userSupplementId}`, { body: { customName, doseTime } });
  }

  removeUserSupplement(userSupplementId) {
    return this.request('DELETE', `/api/users/me/supplements/${userSupplementId}`);
  }

  // 영양제 사진 인증
  // 팀 확정(2026-08-19): 영양제별로 따로 인증하는 게 아니라, 오늘 등록한 영양제를 전부
  // 한 사진에 모아서 한 번에 인증(AI가 알약 개수를 셈)하는 방식으로 변경됨.
  // 그래서 더 이상 userSupplementId를 지정하지 않는다.
  verifyDosePhoto(imageFile) {
    const formData = new FormData();
    formData.append('image', imageFile);
    return this.requestMultipart('POST', '/api/dose-verification/photo', formData);
  }

  getDoseVerificationStatus() {
    return this.request('GET', '/api/dose-verification/status');
  }

  // 복용 기록 (히스토리 캘린더용)
  getDoseRecords({ date, startDate, endDate } = {}) {
    const params = new URLSearchParams();
    if (date) params.set('date', date);
    if (startDate) params.set('startDate', startDate);
    if (endDate) params.set('endDate', endDate);
    const qs = params.toString();
    return this.request('GET', `/api/dose-records${qs ? `?${qs}` : ''}`);
  }

  // 자세 불량(거북목) 이벤트
  createPostureEvent(detectedAt, angleDeg, durationMin) {
    return this.request('POST', '/api/posture-events', { body: { detectedAt, angleDeg, durationMin } });
  }

  getPostureHistory(startDate, endDate) {
    return this.request('GET', `/api/posture-events?startDate=${startDate}&endDate=${endDate}`);
  }

  getPostureSummary(startDate, endDate) {
    return this.request('GET', `/api/posture-events/summary?startDate=${startDate}&endDate=${endDate}`);
  }
}

window.HPetApiError = HPetApiError;
window.hpetApi = new HPetApiClient();
