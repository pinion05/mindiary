<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>오류 - Mindiary</title>
    <%@ include file="include.jsp" %>
</head>
<body>
<div class="container">
    <h2>❌ 오류가 발생했습니다</h2>
    
    <div style="background: #ffe6e6; padding: 20px; border-radius: 10px; margin: 20px 0; border-left: 5px solid #ff4444;">
        <h3>🚨 문제 상황</h3>
        <p style="color: #d32f2f;">
            <%= request.getAttribute("errorMessage") != null ? request.getAttribute("errorMessage") : "알 수 없는 오류가 발생했습니다." %>
        </p>
    </div>
    
    <div style="background: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0;">
        <h4>💡 해결 방법</h4>
        <ul style="margin-left: 20px;">
            <li>브라우저를 새로고침한 후 다시 시도해보세요</li>
            <li>일기 내용이 너무 길거나 짧지 않은지 확인해보세요</li>
            <li>문제가 계속되면 잠시 후 다시 이용해보세요</li>
        </ul>
    </div>
    
    <div style="text-align: center; margin-top: 30px;">
        <a href="javascript:history.back()"><button style="margin: 5px;">이전 페이지로</button></a>
        <a href="write.jsp"><button style="margin: 5px;">일기 작성하기</button></a>
        <a href="index.jsp"><button style="margin: 5px; background-color: #6c757d;">홈으로</button></a>
    </div>
    
    <div style="text-align: center; margin-top: 20px; font-size: 12px; color: #888;">
        <p>문제가 지속될 경우 관리자에게 문의해주세요.</p>
    </div>
</div>
</body>
</html>
