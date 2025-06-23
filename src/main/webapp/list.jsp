<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, model.Diary" %>
<!DOCTYPE html>
<html>
<head>
    <title>일기 목록 - Mindiary</title>
    <%@ include file="include.jsp" %>
    <style>
        .filter-section {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 10px;
            margin-bottom: 20px;
        }
        .filter-form {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
            align-items: center;
        }
        .diary-entry {
            background: #fefefe;
            padding: 20px;
            margin: 15px 0;
            border-left: 5px solid #4CAF50;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .diary-meta {
            color: #666;
            font-size: 14px;
            margin-bottom: 10px;
        }
        .emotion-tag {
            display: inline-block;
            padding: 4px 12px;
            background: #e3f2fd;
            color: #1976d2;
            border-radius: 15px;
            font-size: 12px;
            font-weight: bold;
        }
        .pagination {
            text-align: center;
            margin: 30px 0;
        }
        .pagination a, .pagination span {
            display: inline-block;
            padding: 8px 12px;
            margin: 0 2px;
            border: 1px solid #ddd;
            text-decoration: none;
            border-radius: 4px;
        }
        .pagination .current {
            background: #4CAF50;
            color: white;
            border-color: #4CAF50;
        }
        .stats-summary {
            background: #e8f5e8;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            text-align: center;
        }
        .no-results {
            text-align: center;
            padding: 40px;
            color: #666;
        }
    </style>
</head>
<body>
<div class="container">
    <h2>📝 일기 목록</h2>

    <!-- 통계 요약 -->
    <% if (request.getAttribute("totalDiaryCount") != null) { %>
    <div class="stats-summary">
        <strong>전체 일기:</strong> <%= request.getAttribute("totalDiaryCount") %>개 | 
        <strong>최근 7일:</strong> <%= request.getAttribute("recentDiaryCount") %>개 | 
        <strong>현재 표시:</strong> <%= request.getAttribute("totalItems") %>개
    </div>
    <% } %>

    <!-- 필터 및 검색 섹션 -->
    <div class="filter-section">
        <form method="GET" action="list" class="filter-form">
            <label for="emotion">감정 필터:</label>
            <select id="emotion" name="emotion">
                <option value="">전체</option>
                <%
                    String selectedEmotion = (String) request.getAttribute("selectedEmotion");
                    String[] emotions = {"행복", "슬픔", "분노", "불안", "피로", "평온", "복잡"};
                    for (String emotion : emotions) {
                        String selected = emotion.equals(selectedEmotion) ? "selected" : "";
                %>
                <option value="<%= emotion %>" <%= selected %>><%= emotion %></option>
                <% } %>
            </select>

            <label for="date">날짜:</label>
            <input type="date" id="date" name="date" value="<%= request.getAttribute("selectedDate") != null ? request.getAttribute("selectedDate") : "" %>">

            <label for="search">검색:</label>
            <input type="text" id="search" name="search" 
                   placeholder="일기 내용 검색..." value="<%= request.getAttribute("searchKeyword") != null ? request.getAttribute("searchKeyword") : "" %>">

            <label for="sortBy">정렬:</label>
            <select id="sortBy" name="sortBy">
                <%
                    String sortBy = (String) request.getAttribute("sortBy");
                    String descSelected = "date_desc".equals(sortBy) ? "selected" : "";
                    String ascSelected = "date_asc".equals(sortBy) ? "selected" : "";
                %>
                <option value="date_desc" <%= descSelected %>>최신순</option>
                <option value="date_asc" <%= ascSelected %>>오래된순</option>
            </select>

            <label for="pageSize">표시 개수:</label>
            <select id="pageSize" name="pageSize">
                <%
                    Integer pageSize = (Integer) request.getAttribute("pageSize");
                    if (pageSize == null) pageSize = 10;
                %>
                <option value="5" <%= pageSize == 5 ? "selected" : "" %>>5개</option>
                <option value="10" <%= pageSize == 10 ? "selected" : "" %>>10개</option>
                <option value="20" <%= pageSize == 20 ? "selected" : "" %>>20개</option>
            </select>

            <button type="submit">🔍 검색</button>
            <a href="list"><button type="button" style="background-color: #6c757d;">초기화</button></a>
        </form>
    </div>

    <!-- 오류 메시지 -->
    <% if (request.getAttribute("error") != null && (Boolean) request.getAttribute("error")) { %>
        <div style="background: #ffe6e6; padding: 15px; border-radius: 5px; margin: 20px 0; color: #d32f2f;">
            ❌ <%= request.getAttribute("errorMessage") %>
        </div>
    <% } %>

    <!-- 일기 목록 -->
    <%
        List<Diary> diaryList = (List<Diary>) request.getAttribute("diaryList");
        if (diaryList != null && !diaryList.isEmpty()) {
            for (Diary diary : diaryList) {
                String content = diary.getContent();
                String searchKeyword = (String) request.getAttribute("searchKeyword");
                
                // 검색어 하이라이트
                if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                    content = content.replaceAll("(?i)(" + searchKeyword + ")", 
                        "<span style='background-color: yellow; font-weight: bold;'>$1</span>");
                }
                
                // 미리보기 텍스트 (첫 200자)
                String preview = content.length() > 200 ? content.substring(0, 200) + "..." : content;
    %>
        <div class="diary-entry">
            <div class="diary-meta">
                <span>📅 <%= diary.getCreatedAt() %></span>
                <span class="emotion-tag"><%= diary.getEmotionSummary() %></span>
            </div>
            <div class="diary-content">
                <p><%= preview %></p>
            </div>
        </div>
    <%
            }
            
            // 페이징 처리
            Integer totalPages = (Integer) request.getAttribute("totalPages");
            Integer currentPage = (Integer) request.getAttribute("currentPage");
            Boolean hasPrevious = (Boolean) request.getAttribute("hasPrevious");
            Boolean hasNext = (Boolean) request.getAttribute("hasNext");
            Integer totalItems = (Integer) request.getAttribute("totalItems");
            
            if (totalPages != null && totalPages > 1) {
                String baseUrl = "list?emotion=" + (selectedEmotion != null ? selectedEmotion : "") +
                               "&date=" + (request.getAttribute("selectedDate") != null ? request.getAttribute("selectedDate") : "") +
                               "&search=" + (request.getAttribute("searchKeyword") != null ? request.getAttribute("searchKeyword") : "") +
                               "&sortBy=" + (sortBy != null ? sortBy : "date_desc") +
                               "&pageSize=" + pageSize;
    %>
        <div class="pagination">
            <% if (hasPrevious != null && hasPrevious) { %>
                <a href="<%= baseUrl %>&page=1">&laquo; 처음</a>
                <a href="<%= baseUrl %>&page=<%= currentPage - 1 %>">&lsaquo; 이전</a>
            <% } %>
            
            <%
                int startPage = Math.max(1, currentPage - 2);
                int endPage = Math.min(totalPages, currentPage + 2);
                for (int i = startPage; i <= endPage; i++) {
                    if (i == currentPage) {
            %>
                <span class="current"><%= i %></span>
            <% } else { %>
                <a href="<%= baseUrl %>&page=<%= i %>"><%= i %></a>
            <% } } %>
            
            <% if (hasNext != null && hasNext) { %>
                <a href="<%= baseUrl %>&page=<%= currentPage + 1 %>">다음 &rsaquo;</a>
                <a href="<%= baseUrl %>&page=<%= totalPages %>">마지막 &raquo;</a>
            <% } %>
        </div>
        
        <div style="text-align: center; color: #666; font-size: 14px;">
            <%= currentPage %> / <%= totalPages %> 페이지 (총 <%= totalItems %>개)
        </div>
    <% } %>

    <% } else { %>
        <div class="no-results">
            <h3>📋 일기가 없습니다</h3>
            <p>
                <% if (selectedEmotion != null || request.getAttribute("selectedDate") != null || request.getAttribute("searchKeyword") != null) { %>
                    검색 조건에 맞는 일기를 찾을 수 없습니다.<br>
                    다른 조건으로 검색해보시거나 새로운 일기를 작성해보세요.
                <% } else { %>
                    아직 작성된 일기가 없습니다.<br>
                    첫 번째 일기를 작성해보세요!
                <% } %>
            </p>
            <a href="write.jsp"><button style="margin-top: 15px;">✍️ 새 일기 작성</button></a>
        </div>
    <% } %>

    <!-- 액션 버튼 -->
    <div style="text-align: center; margin-top: 30px;">
        <a href="write.jsp"><button>✍️ 새 일기 작성</button></a>
        <a href="stats.jsp"><button style="background-color: #17a2b8;">📊 감정 통계</button></a>
        <a href="index.jsp"><button style="background-color: #6c757d;">🏠 홈으로</button></a>
    </div>
</div>
</body>
</html>
