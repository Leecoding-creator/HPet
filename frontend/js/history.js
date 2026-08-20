/**
 * HPet - 복용 기록 캘린더 & 자세 불량 이력 히스토리 (Stage 7)
 * 
 * 월별 캘린더 뷰, 복용 도장, 자세 통계 차트 시각화
 */

class HPetHistoryManager {
  constructor() {
    this.currentYear = new Date().getFullYear();
    this.currentMonth = new Date().getMonth(); // 0-indexed
    // 현재 보고 있는 달의 실제 백엔드 데이터 캐시
    this.monthDoseDays = new Set();       // 복용 기록이 있는 날짜(YYYY-MM-DD) 집합
    this.monthTurtleCounts = {};          // { 'YYYY-MM-DD': count }
  }

  init() {
    this.bindEvents();
  }

  monthDateRange() {
    const startDate = `${this.currentYear}-${String(this.currentMonth + 1).padStart(2, '0')}-01`;
    const daysInMonth = new Date(this.currentYear, this.currentMonth + 1, 0).getDate();
    const endDate = `${this.currentYear}-${String(this.currentMonth + 1).padStart(2, '0')}-${String(daysInMonth).padStart(2, '0')}`;
    return { startDate, endDate };
  }

  async loadMonthData() {
    const { startDate, endDate } = this.monthDateRange();
    try {
      const [doseRecords, postureSummary] = await Promise.all([
        window.hpetApi.getDoseRecords({ startDate, endDate }),
        window.hpetApi.getPostureSummary(startDate, endDate)
      ]);

      this.monthDoseDays = new Set((doseRecords || []).map(r => r.doseDate));
      this.monthTurtleCounts = {};
      (postureSummary || []).forEach(row => {
        this.monthTurtleCounts[row.date] = row.count;
      });
    } catch (err) {
      console.error('히스토리 데이터 조회 실패', err);
      this.monthDoseDays = new Set();
      this.monthTurtleCounts = {};
    }
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

    document.getElementById('btn-go-today')?.addEventListener('click', () => {
      const now = new Date();
      this.currentYear = now.getFullYear();
      this.currentMonth = now.getMonth();
      this.render();
    });

    const monthPicker = document.getElementById('cal-month-picker');
    if (monthPicker) {
      // 초기 밸류 세팅 (YYYY-MM)
      const initValue = `${this.currentYear}-${String(this.currentMonth + 1).padStart(2, '0')}`;
      monthPicker.value = initValue;

      monthPicker.addEventListener('change', (e) => {
        if (!e.target.value) return;
        const parts = e.target.value.split('-');
        this.currentYear = parseInt(parts[0], 10);
        this.currentMonth = parseInt(parts[1], 10) - 1;
        this.render();
      });
    }
  }

  async render() {
    await this.loadMonthData();
    this.renderCalendar();
    this.renderStats();
  }

  renderCalendar() {
    const titleEl = document.getElementById('cal-month-title');
    const gridEl = document.getElementById('calendar-grid');
    if (!titleEl || !gridEl) return;

    titleEl.textContent = `${this.currentYear}년 ${this.currentMonth + 1}월`;

    const monthPicker = document.getElementById('cal-month-picker');
    if (monthPicker) {
      monthPicker.value = `${this.currentYear}-${String(this.currentMonth + 1).padStart(2, '0')}`;
    }

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

      // 오늘 날짜 여부
      const now = new Date();
      const isToday = (this.currentYear === now.getFullYear()
        && this.currentMonth === now.getMonth()
        && day === now.getDate());

      // 복용 완료 여부 (해당 날짜에 복용 기록이 하나라도 있으면 완료로 표시)
      const isChecked = this.monthDoseDays.has(dateKey);

      // 거북목 감지 여부
      const hasTurtle = (this.monthTurtleCounts[dateKey] || 0) > 0;

      let classes = 'cal-day';
      if (isToday) classes += ' today';
      if (isChecked) classes += ' checked';
      if (hasTurtle) classes += ' turtle';

      html += `
        <div class="${classes}" data-date="${dateKey}">
          <span class="day-num">${day}</span>
          <div class="cal-dots">
            ${isChecked ? '<div class="dot dot-supp"></div>' : ''}
            ${hasTurtle ? '<div class="dot dot-turtle"></div>' : ''}
          </div>
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

  async showDayDetail(dateKey) {
    const parts = dateKey.split('-');
    const label = `${parseInt(parts[1])}월 ${parseInt(parts[2])}일`;

    const titleEl = document.getElementById('daily-detail-date');
    const timelineEl = document.getElementById('daily-timeline');
    const modal = document.getElementById('modal-daily-detail');

    if (titleEl) titleEl.textContent = `${label} 기록`;

    // 닫기 버튼 이벤트
    const closeBtn = document.getElementById('btn-close-daily');
    if (closeBtn) {
      closeBtn.onclick = () => modal.classList.add('hidden');
    }

    let doseRecords = [];
    let postureEvents = [];
    try {
      [doseRecords, postureEvents] = await Promise.all([
        window.hpetApi.getDoseRecords({ date: dateKey }),
        window.hpetApi.getPostureHistory(dateKey, dateKey)
      ]);
    } catch (err) {
      console.error('일별 기록 조회 실패', err);
    }

    if ((!doseRecords || doseRecords.length === 0) && (!postureEvents || postureEvents.length === 0)) {
      timelineEl.innerHTML = `
        <div style="text-align:center; padding: 40px 20px; color: var(--text-light);">
          <i class="fa-regular fa-calendar-xmark" style="font-size: 40px; margin-bottom: 12px; color: #d0d0d0;"></i>
          <p>해당 날짜에 기록된 데이터가 없습니다.</p>
        </div>
      `;
      if (modal) modal.classList.remove('hidden');
      return;
    }

    const formatTime = (isoDateTime) => {
      if (!isoDateTime) return '';
      const d = new Date(isoDateTime);
      let hours = d.getHours();
      const ampm = hours >= 12 ? 'PM' : 'AM';
      hours = hours % 12 || 12;
      return `${String(hours).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')} ${ampm}`;
    };

    let timelineHtml = '';

    // 영양제 복용 타임라인 (실제 인증/등록 시각)
    (doseRecords || []).forEach(record => {
      timelineHtml += `
        <div class="timeline-item">
          <div class="timeline-time">${formatTime(record.verifiedAt)}</div>
          <div class="timeline-icon supp"><i class="fa-solid fa-capsules"></i></div>
          <div class="timeline-content" style="display: flex; justify-content: space-between; align-items: flex-start;">
            <div>
              <div class="timeline-title">영양제 복용 완료</div>
              <div class="timeline-desc">${record.supplementName} 복용 확인</div>
            </div>
            <button class="btn-more-options" onclick="alert('복용 기록을 수정하거나 삭제하시겠습니까? (Mock)')" title="기록 수정/삭제"><i class="fa-solid fa-ellipsis-vertical"></i></button>
          </div>
        </div>
      `;
    });

    // 거북목 타임라인 (실제 감지 시각)
    (postureEvents || []).forEach(event => {
      timelineHtml += `
        <div class="timeline-item">
          <div class="timeline-time">${formatTime(event.detectedAt)}</div>
          <div class="timeline-icon turtle"><i class="fa-solid fa-triangle-exclamation"></i></div>
          <div class="timeline-content" style="display: flex; justify-content: space-between; align-items: flex-start;">
            <div>
              <div class="timeline-title">자세 불량 감지</div>
              <div class="timeline-desc">거북목 자세 경고 발생</div>
            </div>
            <button class="btn-more-options" onclick="alert('자세 경고 기록을 삭제하시겠습니까? (Mock)')" title="기록 삭제"><i class="fa-solid fa-ellipsis-vertical"></i></button>
          </div>
        </div>
      `;
    });

    timelineEl.innerHTML = timelineHtml;
    if (modal) modal.classList.remove('hidden');
  }

  renderStats() {
    const daysInMonth = new Date(this.currentYear, this.currentMonth + 1, 0).getDate();
    let currentDayInMonth = daysInMonth;

    // 만약 현재 연/월이 오늘이 속한 연/월이라면, 오늘까지만 분모로 계산
    const now = new Date();
    if (this.currentYear === now.getFullYear() && this.currentMonth === now.getMonth()) {
      currentDayInMonth = now.getDate();
    } else if (this.currentYear > now.getFullYear() || (this.currentYear === now.getFullYear() && this.currentMonth > now.getMonth())) {
      // 미래의 달
      currentDayInMonth = 0;
    }

    const supplementTakenDays = this.monthDoseDays.size;
    const turtleCount = Object.values(this.monthTurtleCounts).reduce((sum, c) => sum + c, 0);

    const suppPercent = currentDayInMonth === 0 ? 0 : Math.round((supplementTakenDays / currentDayInMonth) * 100);

    const suppPercentEl = document.getElementById('stat-supp-percent');
    const suppBarEl = document.getElementById('stat-supp-bar');
    if (suppPercentEl) suppPercentEl.textContent = `${suppPercent}%`;
    if (suppBarEl) suppBarEl.style.width = `${suppPercent}%`;

    const posturePercentEl = document.getElementById('stat-posture-percent');
    const postureBarEl = document.getElementById('stat-posture-bar');
    
    if (posturePercentEl) posturePercentEl.textContent = `${turtleCount}회 경고`;
    if (postureBarEl) {
      // 10회 경고를 100%로 가정
      const pct = Math.min(100, (turtleCount / 10) * 100);
      postureBarEl.style.width = `${pct}%`;
    }
  }
}

window.hpetHistory = new HPetHistoryManager();

document.addEventListener('DOMContentLoaded', () => {
  window.hpetHistory.init();
});
