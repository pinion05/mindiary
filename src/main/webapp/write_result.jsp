<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>일기 작성 완료 - Mindiary</title>
    <%@ include file="include.jsp" %>
</head>
<body>
<div class="container">
    <% if (request.getAttribute("success") != null && (Boolean) request.getAttribute("success")) { %>
        <h2>✨ 일기 작성이 완료되었습니다!</h2>
        
        <div style="background: #e8f5e8; padding: 20px; border-radius: 10px; margin: 20px 0;">
            <h3>감정 분석 결과</h3>
            <p><strong>분석된 감정:</strong> <span style="color: #4CAF50; font-size: 18px;"><%= request.getAttribute("emotion") %></span></p>
        </div>
        
        <div style="background: #f0f8ff; padding: 20px; border-radius: 10px; margin: 20px 0;">
            <h3>💬 AI의 한마디</h3>
            <p style="font-style: italic; color: #2c3e50;"><%= request.getAttribute("responseMessage") %></p>
        </div>
        
        <div style="text-align: center; margin-top: 30px;">
            <a href="write.jsp"><button style="margin: 5px;">새 일기 작성</button></a>
            <a href="list.jsp"><button style="margin: 5px;">일기 목록 보기</button></a>
            <a href="stats.jsp"><button style="margin: 5px;">감정 통계 보기</button></a>
            <a href="index.jsp"><button style="margin: 5px; background-color: #6c757d;">홈으로</button></a>
        </div>
        
    <% } else { %>
        <h2>❌ 오류가 발생했습니다</h2>
        <p style="color: red;"><%= request.getAttribute("errorMessage") %></p>
        <div style="text-align: center; margin-top: 20px;">
            <a href="write.jsp"><button>다시 시도</button></a>
            <a href="index.jsp"><button style="background-color: #6c757d;">홈으로</button></a>
        </div>
    <% } %>
</div>
</body>
</html>
