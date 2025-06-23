<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>페이지를 찾을 수 없습니다 - Mindiary</title>
    <%@ include file="include.jsp" %>
</head>
<body>
<div class="container">
    <h2>🔍 페이지를 찾을 수 없습니다 (404)</h2>
    
    <div style="background: #fff3cd; padding: 20px; border-radius: 10px; margin: 20px 0; border-left: 5px solid #ffc107;">
        <h3>📍 요청하신 페이지가 존재하지 않습니다</h3>
        <p>죄송합니다. 찾으시는 페이지가 삭제되었거나 주소가 변경되었을 수 있습니다.</p>
    </div>
    
    <div style="background: #e3f2fd; padding: 15px; border-radius: 5px; margin: 20px 0;">
        <h4>💡 다음을 확인해보세요</h4>
        <ul style="margin-left: 20px;">
            <li>주소를 정확히 입력했는지 확인해보세요</li>
            <li>북마크나 링크가 최신 정보인지 확인해보세요</li>
            <li>브라우저의 새로고침 버튼을 눌러보세요</li>
        </ul>
    </div>
    
    <div style="text-align: center; margin-top: 30px;">
        <a href="index.jsp"><button style="margin: 5px;">🏠 홈으로</button></a>
        <a href="write.jsp"><button style="margin: 5px;">✍️ 일기 작성</button></a>
        <a href="list.jsp"><button style="margin: 5px;">📝 일기 목록</button></a>
        <a href="stats.jsp"><button style="margin: 5px;">📊 감정 통계</button></a>
    </div>
    
    <div style="text-align: center; margin-top: 20px; font-size: 12px; color: #888;">
        <p>문제가 계속되면 새로고침 후 다시 시도해주세요.</p>
    </div>
</div>
</body>
</html>
