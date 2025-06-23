<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, model.Diary" %>
<!DOCTYPE html>
<html>
<head>
    <title>일기 목록 - Mindiary</title>
    <%@ include file="include.jsp" %>
</head>
<body>
<div class="container">
    <h2>작성한 일기 목록</h2>

    <form method="get" action="list.jsp">
        <select name="emotion">
            <option value="">전체</option>
            <option value="행복">행복</option>
            <option value="슬픔">슬픔</option>
            <option value="분노">분노</option>
            <option value="불안">불안</option>
        </select>
        <button type="submit">필터 적용</button>
    </form>

    <%
        List<Diary> diaryList = (List<Diary>) request.getAttribute("diaryList");
        if (diaryList != null && !diaryList.isEmpty()) {
            for (Diary d : diaryList) {
                String content = d.getContent();
                String preview = content.length() > 50 ? content.substring(0, 50) + "..." : content;
    %>
    <div class="diary-entry">
        <p><strong>작성일 :</strong> <%= d.getCreatedAt() %></p>
        <p><strong>대표감정 :</strong> <%= d.getEmotionSummary() %></p>
        <p><strong>내용 :</strong> <%= preview %></p>
        <hr>
    </div>
    <%
        }
    } else {
    %>
    <p>작성된 일기가 없습니다.</p>
    <%
        }
    %>

</div>
</body>
</html>
