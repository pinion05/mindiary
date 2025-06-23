<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html>
<head>
    <title>감정 통계 - Mindiary</title>
    <%@ include file ="include.jsp" %>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        .stats-container {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-bottom: 30px;
        }
        .stats-card {
            background: #ffffff;
            padding: 25px;
            border-radius: 15px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            text-align: center;
        }
        .stats-number {
            font-size: 2.5em;
            font-weight: bold;
            color: #4CAF50;
            margin: 10px 0;
        }
        .stats-label {
            color: #666;
            font-size: 14px;
        }
        .chart-container {
            position: relative;
            height: 400px;
            margin: 20px 0;
            background: white;
            padding: 20px;
            border-radius: 15px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        .emotion-summary {
            background: #e8f5e8;
            padding: 20px;
            border-radius: 10px;
            margin: 20px 0;
        }
        .activity-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 15px;
            margin: 20px 0;
        }
        .activity-item {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 8px;
            text-align: center;
            border-left: 4px solid #4CAF50;
        }
        .controls {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 10px;
            margin-bottom: 20px;
            text-align: center;
        }
        .error-message {
            background: #ffe6e6;
            color: #d32f2f;
            padding: 15px;
            border-radius: 5px;
            margin: 20px 0;
        }
        @media (max-width: 768px) {
            .stats-container {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
<div class="container">
    <h2>📊 감정 통계 대시보드</h2>

    <!-- 오류 메시지 -->
    <% if (request.getAttribute("error") != null && (Boolean) request.getAttribute("error")) { %>
        <div class="error-message">
            ❌ <%= request.getAttribute("errorMessage") %>
        </div>
    <% } %>

    <!-- 주요 통계 카드 -->
    <div class="stats-container">
        <div class="stats-card">
            <div class="stats-number"><%= request.getAttribute("totalDiaries") != null ? request.getAttribute("totalDiaries") : 0 %></div>
            <div class="stats-label">총 작성 일기</div>
        </div>
        <div class="stats-card">
            <div class="stats-number"><%= request.getAttribute("recentDiaries") != null ? request.getAttribute("recentDiaries") : 0 %></div>
            <div class="stats-label">최근 7일</div>
        </div>
        <div class="stats-card">
            <div class="stats-number"><%= request.getAttribute("monthlyDiaries") != null ? request.getAttribute("monthlyDiaries") : 0 %></div>
            <div class="stats-label">최근 30일</div>
        </div>
        <div class="stats-card">
            <div class="stats-number"><%= request.getAttribute("writingStreak") != null ? request.getAttribute("writingStreak") : 0 %></div>
            <div class="stats-label">연속 작성일</div>
        </div>
    </div>

    <!-- 감정 요약 -->
    <% if (request.getAttribute("mostFrequentEmotion") != null) { %>
    <div class="emotion-summary">
        <h3>🎯 감정 분석 요약</h3>
        <p><strong>가장 자주 느끼는 감정:</strong> <span style="color: #4CAF50; font-size: 18px;"><%= request.getAttribute("mostFrequentEmotion") %></span></p>
        <p>당신의 감정 패턴을 파악하여 더 나은 멘탈 케어를 도와드릴게요.</p>
    </div>
    <% } %>

    <!-- 차트 컨트롤 -->
    <div class="controls">
        <button onclick="showEmotionChart()" id="emotionBtn" class="active">감정 분포</button>
        <button onclick="showTimelineChart()" id="timelineBtn">시간별 추이</button>
        <select id="timeRange" onchange="updateTimelineChart()" style="margin-left: 10px;">
            <option value="7">최근 7일</option>
            <option value="30" selected>최근 30일</option>
            <option value="90">최근 90일</option>
        </select>
    </div>

    <!-- 차트 컨테이너 -->
    <div class="chart-container">
        <canvas id="mainChart"></canvas>
    </div>

    <!-- 활동 통계 -->
    <h3>📈 작성 활동</h3>
    <div class="activity-grid">
        <%
            Map<String, Integer> recentActivity = (Map<String, Integer>) request.getAttribute("recentActivity");
            if (recentActivity != null) {
        %>
        <div class="activity-item">
            <strong><%= recentActivity.getOrDefault("today", 0) %></strong><br>
            <small>오늘</small>
        </div>
        <div class="activity-item">
            <strong><%= recentActivity.getOrDefault("thisWeek", 0) %></strong><br>
            <small>이번 주</small>
        </div>
        <div class="activity-item">
            <strong><%= recentActivity.getOrDefault("thisMonth", 0) %></strong><br>
            <small>이번 달</small>
        </div>
        <div class="activity-item">
            <strong><%= recentActivity.getOrDefault("last3Months", 0) %></strong><br>
            <small>최근 3개월</small>
        </div>
        <% } %>
    </div>

    <!-- 액션 버튼 -->
    <div style="text-align: center; margin-top: 30px;">
        <a href="write.jsp"><button>✍️ 새 일기 작성</button></a>
        <a href="list.jsp"><button style="background-color: #17a2b8;">📝 일기 목록</button></a>
        <a href="index.jsp"><button style="background-color: #6c757d;">🏠 홈으로</button></a>
    </div>
</div>

<script>
let mainChart = null;
let currentChartType = 'emotion';

// 페이지 로드 시 감정 차트 표시
document.addEventListener('DOMContentLoaded', function() {
    showEmotionChart();
});

// 감정 분포 차트 표시
function showEmotionChart() {
    currentChartType = 'emotion';
    updateButtonStyles('emotionBtn');
    
    fetch('stats?action=json&type=emotion')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                createEmotionChart(data);
            } else {
                showError('감정 통계 데이터를 불러올 수 없습니다.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showError('네트워크 오류가 발생했습니다.');
        });
}

// 시간별 추이 차트 표시
function showTimelineChart() {
    currentChartType = 'timeline';
    updateButtonStyles('timelineBtn');
    updateTimelineChart();
}

// 타임라인 차트 업데이트
function updateTimelineChart() {
    if (currentChartType !== 'timeline') return;
    
    const days = document.getElementById('timeRange').value;
    
    fetch(`stats?action=json&type=timeline&days=${days}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                createTimelineChart(data);
            } else {
                showError('타임라인 데이터를 불러올 수 없습니다.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showError('네트워크 오류가 발생했습니다.');
        });
}

// 감정 차트 생성
function createEmotionChart(data) {
    const ctx = document.getElementById('mainChart').getContext('2d');
    
    if (mainChart) {
        mainChart.destroy();
    }
    
    mainChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: data.labels,
            datasets: [{
                data: data.data,
                backgroundColor: data.colors,
                borderWidth: 2,
                borderColor: '#ffffff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 20,
                        font: {
                            size: 14
                        }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = total > 0 ? ((context.raw / total) * 100).toFixed(1) : 0;
                            return `${context.label}: ${context.raw}개 (${percentage}%)`;
                        }
                    }
                }
            }
        }
    });
}

// 타임라인 차트 생성
function createTimelineChart(data) {
    const ctx = document.getElementById('mainChart').getContext('2d');
    
    if (mainChart) {
        mainChart.destroy();
    }
    
    mainChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: data.dates,
            datasets: [{
                label: '일기 작성 수',
                data: data.counts,
                borderColor: '#4CAF50',
                backgroundColor: 'rgba(76, 175, 80, 0.1)',
                borderWidth: 3,
                fill: true,
                tension: 0.3,
                pointBackgroundColor: '#4CAF50',
                pointBorderColor: '#ffffff',
                pointBorderWidth: 2,
                pointRadius: 5
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'top'
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1
                    },
                    title: {
                        display: true,
                        text: '일기 개수'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: '날짜'
                    }
                }
            }
        }
    });
}

// 버튼 스타일 업데이트
function updateButtonStyles(activeId) {
    document.getElementById('emotionBtn').classList.remove('active');
    document.getElementById('timelineBtn').classList.remove('active');
    document.getElementById(activeId).classList.add('active');
    
    // 타임라인이 아닐 때는 시간 범위 선택 숨기기
    const timeRange = document.getElementById('timeRange');
    timeRange.style.display = activeId === 'timelineBtn' ? 'inline' : 'none';
}

// 오류 메시지 표시
function showError(message) {
    const ctx = document.getElementById('mainChart').getContext('2d');
    if (mainChart) {
        mainChart.destroy();
    }
    
    ctx.fillStyle = '#666';
    ctx.font = '16px Arial';
    ctx.textAlign = 'center';
    ctx.fillText(message, ctx.canvas.width / 2, ctx.canvas.height / 2);
}

// 버튼 활성화 스타일
.button.active, button.active {
    background-color: #4CAF50 !important;
    color: white !important;
}
</script>

<style>
button.active {
    background-color: #4CAF50 !important;
    color: white !important;
}
</style>

</body>
</html>
