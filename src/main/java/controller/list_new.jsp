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
        .search-highlight {
            background-color: yellow;
            font-weight: bold;
        }
    </style>
</head>
<body>
<div class="container">
    <h2>📝 일기 목록</h2>

    <!-- 통계 요약 -->
    <div class="stats-summary">
        <strong>전체 일기:</strong> ${totalDiaryCount}개 | 
        <strong>최근 7일:</strong> ${recentDiaryCount}개 | 
        <strong>현재 표시:</strong> ${totalItems}개
    </div>

    <!-- 필터 및 검색 섹션 -->
    <div class="filter-section">
        <form method="GET" action="list" class="filter-form">
            <label for="emotion">감정 필터:</label>
            <select id="emotion" name="emotion">
                <option value="">전체</option>
                <c:forEach var="emotionOption" items="${availableEmotions}">
                    <option value="${emotionOption}" 
                            ${selectedEmotion == emotionOption ? 'selected' : ''}>
                        ${emotionOption}
                    </option>
                </c:forEach>
            </select>

            <label for="date">날짜:</label>
            <input type="date" id="date" name="date" value="${selectedDate}">

            <label for="search">검색:</label>
            <input type="text" id="search" name="search" 
                   placeholder="일기 내용 검색..." value="${searchKeyword}">

            <label for="sortBy">정렬:</label>
            <select id="sortBy" name="sortBy">
                <option value="date_desc" ${sortBy == 'date_desc' ? 'selected' : ''}>최신순</option>
                <option value="date_asc" ${sortBy == 'date_asc' ? 'selected' : ''}>오래된순</option>
            </select>

            <label for="pageSize">표시 개수:</label>
            <select id="pageSize" name="pageSize">
                <option value="5" ${pageSize == 5 ? 'selected' : ''}>5개</option>
                <option value="10" ${pageSize == 10 ? 'selected' : ''}>10개</option>
                <option value="20" ${pageSize == 20 ? 'selected' : ''}>20개</option>
            </select>

            <button type="submit">🔍 검색</button>
            <a href="list"><button type="button" style="background-color: #6c757d;">초기화</button></a>
        </form>
    </div>

    <!-- 오류 메시지 -->
    <c:if test="${error}">
        <div style="background: #ffe6e6; padding: 15px; border-radius: 5px; margin: 20px 0; color: #d32f2f;">
            ❌ ${errorMessage}
        </div>
    </c:if>

    <!-- 일기 목록 -->
    <c:choose>
        <c:when test="${diaryList != null && !diaryList.isEmpty()}">
            <c:forEach var="diary" items="${diaryList}">
                <div class="diary-entry">
                    <div class="diary-meta">
                        <span>📅 ${diary.createdAt}</span>
                        <span class="emotion-tag">${diary.emotionSummary}</span>
                    </div>
                    <div class="diary-content">
                        <%
                            Diary currentDiary = (Diary) pageContext.getAttribute("diary");
                            String content = currentDiary.getContent();
                            String searchKeyword = (String) request.getAttribute("searchKeyword");
                            
                            // 검색어 하이라이트
                            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                                content = content.replaceAll("(?i)(" + searchKeyword + ")", 
                                    "<span class='search-highlight'>$1</span>");
                            }
                            
                            // 미리보기 텍스트 (첫 200자)
                            String preview = content.length() > 200 ? content.substring(0, 200) + "..." : content;
                        %>
                        <p><%= preview %></p>
                    </div>
                </div>
            </c:forEach>

            <!-- 페이징 -->
            <c:if test="${totalPages > 1}">
                <div class="pagination">
                    <c:if test="${hasPrevious}">
                        <a href="list?page=1&emotion=${selectedEmotion}&date=${selectedDate}&search=${searchKeyword}&sortBy=${sortBy}&pageSize=${pageSize}">
                            &laquo; 처음
                        </a>
                        <a href="list?page=${previousPage}&emotion=${selectedEmotion}&date=${selectedDate}&search=${searchKeyword}&sortBy=${sortBy}&pageSize=${pageSize}">
                            &lsaquo; 이전
                        </a>
                    </c:if>

                    <c:forEach var="i" begin="${currentPage - 2 > 0 ? currentPage - 2 : 1}" 
                               end="${currentPage + 2 < totalPages ? currentPage + 2 : totalPages}">
                        <c:choose>
                            <c:when test="${i == currentPage}">
                                <span class="current">${i}</span>
                            </c:when>
                            <c:otherwise>
                                <a href="list?page=${i}&emotion=${selectedEmotion}&date=${selectedDate}&search=${searchKeyword}&sortBy=${sortBy}&pageSize=${pageSize}">
                                    ${i}
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>

                    <c:if test="${hasNext}">
                        <a href="list?page=${nextPage}&emotion=${selectedEmotion}&date=${selectedDate}&search=${searchKeyword}&sortBy=${sortBy}&pageSize=${pageSize}">
                            다음 &rsaquo;
                        </a>
                        <a href="list?page=${totalPages}&emotion=${selectedEmotion}&date=${selectedDate}&search=${searchKeyword}&sortBy=${sortBy}&pageSize=${pageSize}">
                            마지막 &raquo;
                        </a>
                    </c:if>
                </div>

                <div style="text-align: center; color: #666; font-size: 14px;">
                    ${currentPage} / ${totalPages} 페이지 (총 ${totalItems}개)
                </div>
            </c:if>

        </c:when>
        <c:otherwise>
            <div class="no-results">
                <h3>📋 일기가 없습니다</h3>
                <p>
                    <c:choose>
                        <c:when test="${selectedEmotion != null || selectedDate != null || searchKeyword != null}">
                            검색 조건에 맞는 일기를 찾을 수 없습니다.<br>
                            다른 조건으로 검색해보시거나 새로운 일기를 작성해보세요.
                        </c:when>
                        <c:otherwise>
                            아직 작성된 일기가 없습니다.<br>
                            첫 번째 일기를 작성해보세요!
                        </c:otherwise>
                    </c:choose>
                </p>
                <a href="write.jsp"><button style="margin-top: 15px;">✍️ 새 일기 작성</button></a>
            </div>
        </c:otherwise>
    </c:choose>

    <!-- 액션 버튼 -->
    <div style="text-align: center; margin-top: 30px;">
        <a href="write.jsp"><button>✍️ 새 일기 작성</button></a>
        <a href="stats.jsp"><button style="background-color: #17a2b8;">📊 감정 통계</button></a>
        <a href="index.jsp"><button style="background-color: #6c757d;">🏠 홈으로</button></a>
    </div>
</div>

<%-- JSTL 라이브러리 추가 --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

</body>
</html>
