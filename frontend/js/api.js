/**
 * HPet - Backend API Client
 * 백엔드는 { "success": true, "data": {...} } / { "success": false, "error": {...} } 포맷으로 응답한다.
 */

const HPET_API_BASE = 'http://localhost:8080';
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

  getHomeSummary() {
    return this.request('GET', '/api/home/summary');
  }
}

window.HPetApiError = HPetApiError;
window.hpetApi = new HPetApiClient();
