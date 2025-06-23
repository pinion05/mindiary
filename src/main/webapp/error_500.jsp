<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>서버 오류 - Mindiary</title>
    <%@ include file="include.jsp" %>
</head>
<body>
<div class="container">
    <h2>⚠️ 서버에서 오류가 발생했습니다 (500)</h2>
    
    <div style="background: #ffe6e6; padding: 20px; border-radius: 10px; margin: 20px 0; border-left: 5px solid #ff4444;">
        <h3>🚨 내부 서버 오류</h3>
        <p>죄송합니다. 서버에서 예상치 못한 오류가 발생했습니다.</p>
        <p>잠시 후 다시 시도해주시기 바랍니다.</p>
    </div>
    
    <div style="background: #e8f5e8; padding: 15px; border-radius: 5px; margin: 20px 0;">
        <h4>💡 해결 방법</h4>
        <ul style="margin-left: 20px;">
            <li>페이지를 새로고침 해보세요</li>
            <li>몇 분 후 다시 시도해보세요</li>
            <li>브라우저를 다시 시작해보세요</li>
            <li>문제가 계속되면 관리자에게 문의해주세요</li>
        </ul>
    </div>
    
    <div style="background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0; font-size: 12px; color: #666;">
        <p><strong>오류 정보:</strong></p>
        <p>시간: <%= new java.util.Date() %></p>
        <p>요청 URI: <%= request.getRequestURI() %></p>
    </div>
    
    <div style="text-align: center; margin-top: 30px;">
        <a href="javascript:location.reload()"><button style="margin: 5px;">🔄 새로고침</button></a>
        <a href="index.jsp"><button style="margin: 5px;">🏠 홈으로</button></a>
        <a href="write.jsp"><button style="margin: 5px;">✍️ 일기 작성</button></a>
        <a href="list.jsp"><button style="margin: 5px;">📝 일기 목록</button></a>
    </div>
    
    <div style="text-align: center; margin-top: 20px; font-size: 12px; color: #888;">
        <p>문제가 지속될 경우 시스템 관리자에게 문의해주세요.</p>
    </div>
</div>
</body>
</html>
