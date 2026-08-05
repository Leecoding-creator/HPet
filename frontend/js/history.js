/**
 * HPet - 복용 기록 캘린더 & 자세 불량 이력 히스토리 (Stage 7)
 * 
 * 월별 캘린더 뷰, 복용 도장, 자세 통계 차트 시각화
 */

class HPetHistoryManager {
  constructor() {
    this.currentYear = new Date().getFullYear();
    this.currentMonth = new Date().getMonth(); // 0-indexed
  }

  init() {
    this.bindEvents();
  }

  bindEvents() {
    document.getElementById('btn-prev-month')?.addEventListener('click', () => {
      this.currentMonth--;
      if (this.currentMonth < 0) {
        this.currentMonth = 11;
        this.currentYear--;
      }
      this.render();
    });

    document.getElementById('btn-next-month')?.addEventListener('click', () => {
      this.currentMonth++;
      if (this.currentMonth > 11) {
        this.currentMonth = 0;
        this.currentYear++;
      }
      this.render();
    });
  }

  render() {
    this.renderCalendar();
    this.renderStats();
  }

  renderCalendar() {
    const titleEl = document.getElementById('cal-month-title');
    const gridEl = document.getElementById('calendar-grid');
    if (!titleEl || !gridEl) return;

    titleEl.textContent = `${this.currentYear}년 ${this.currentMonth + 1}월`;

    const history = window.hpetStore.state.history || {};

    // 해당 월의 1일이 무슨 요일인지, 마지막 날짜 계산
    const firstDay = new Date(this.currentYear, this.currentMonth, 1).getDay();
    const daysInMonth = new Date(this.currentYear, this.currentMonth + 1, 0).getDate();

    // 요일 헤더
    const dayHeaders = ['일', '월', '화', '수', '목', '금', '토'];
    let html = dayHeaders.map(d => `<div class="cal-header">${d}</div>`).join('');

    // 1일 앞의 빈 칸 채우기
    for (let i = 0; i < firstDay; i++) {
      html += `<div class="cal-day empty"></div>`;
    }

    // 날짜 셀 생성
    for (let day = 1; day <= daysInMonth; day++) {
      const dateKey = `${this.currentYear}-${String(this.currentMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
      const record = history[dateKey];

      // 오늘 날짜 여부
      const now = new Date();
      const isToday = (this.currentYear === now.getFullYear()
        && this.currentMonth === now.getMonth()
        && day === now.getDate());

      // 복용 완료 여부
      const isChecked = record && record.supplements === true;

      // 거북목 감지 여부
      const hasTurtle = record && record.turtleCount > 0;

      let classes = 'cal-day';
      if (isToday) classes += ' today';
      if (isChecked) classes += ' checked';
      if (hasTurtle) classes += ' turtle';

      html += `
        <div class="${classes}" data-date="${dateKey}">
          <span class="day-num">${day}</span>
          ${isChecked ? '<span class="day-badge">🧪</span>' : ''}
          ${hasTurtle ? '<span class="day-badge turtle-badge">🐢</span>' : ''}
        </div>
      `;
    }

    gridEl.innerHTML = html;

    // 날짜 클릭 시 상세 정보 표시
    gridEl.querySelectorAll('.cal-day:not(.empty)').forEach(cell => {
      cell.addEventListener('click', (e) => {
        const dateKey = e.currentTarget.dataset.date;
        this.showDayDetail(dateKey);
      });
    });
  }

  showDayDetail(dateKey) {
    const record = window.hpetStore.state.history[dateKey];
    const parts = dateKey.split('-');
    const label = `${parseInt(parts[1])}월 ${parseInt(parts[2])}일`;

    if (!record) {
      // 기록 없는 날: 간단한 안내
      const rewardTitle = document.getElementById('reward-title');
      const rewardDesc = document.getElementById('reward-desc');
      const rewardIcon = document.getElementById('reward-icon');
      const modal = document.getElementById('modal-reward');

      if (rewardTitle) rewardTitle.textContent = `${label} 기록`;
      if (rewardDesc) rewardDesc.textContent = '해당 날짜에 기록된 데이터가 없습니다.';
      if (rewardIcon) rewardIcon.textContent = '📅';
      if (modal) modal.classList.remove('hidden');

      const closeBtn = document.getElementById('btn-close-reward');
      if (closeBtn) {
        closeBtn.onclick = () => modal.classList.add('hidden');
      }
      return;
    }

    const suppStatus = record.supplements ? '✅ 영양제 복용 완료' : '❌ 영양제 미복용';
    const turtleStatus = record.turtleCount > 0
      ? `🐢 거북목 감지 ${record.turtleCount}회`
      : '👍 거북목 감지 없음';

    const rewardTitle = document.getElementById('reward-title');
    const rewardDesc = document.getElementById('reward-desc');
    const rewardIcon = document.getElementById('reward-icon');
    const modal = document.getElementById('modal-reward');

    if (rewardTitle) rewardTitle.textContent = `${label} 기록`;
    if (rewardDesc) rewardDesc.textContent = `${suppStatus}\n${turtleStatus}`;
    if (rewardIcon) rewardIcon.textContent = '📋';
    if (modal) modal.classList.remove('hidden');

    const closeBtn = document.getElementById('btn-close-reward');
    if (closeBtn) {
      closeBtn.onclick = () => modal.classList.add('hidden');
    }
  }

  renderStats() {
    const stats = window.hpetStore.state.stats;
    
    const turtleCountEl = document.getElementById('stat-turtle-count');
    const gameClearEl = document.getElementById('stat-game-clear');

    if (turtleCountEl) turtleCountEl.textContent = `${stats.turtleNeckDetectionsThisWeek}회`;
    if (gameClearEl) gameClearEl.textContent = `${stats.postureGamesCleared}회`;
  }
}

window.hpetHistory = new HPetHistoryManager();

document.addEventListener('DOMContentLoaded', () => {
  window.hpetHistory.init();
});
