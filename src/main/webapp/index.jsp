<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Mindiary - Main</title>
    <%@ include file ="include.jsp" %>
</head>
<body>
<div class="container">
    <h1>Mindiary</h1>
    <p>AI가 당신의 감정을 분석해주는 감정 해석 일기장입니다.</p>

    <div class="menu">
        <a href="write.jsp"><button>일기 작성하기</button></a>
        <a href="list.jsp"><button>일기 목록 보기</button></a>
        <a href="stats.jsp"><button>감정 통계 보기</button></a>
    </div>
</div>
</body>
</html>