<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>일기 작성 - Mindiary</title>
    <%@ include file="include.jsp" %>
</head>
<body>
<div class="container">
    <h2>오늘의 감정을 기록해보세요</h2>

    <form method="post" action="write">
        <div>
            <label for="content">일기 내용</label><br>
            <textarea id="content" name="content" rows="6" cols="50" placeholder="오늘 하루는 어땠나요?" required></textarea>
        </div>

        <div>
            <label for="emotion">감정 선택 (선택 사항)</label><br>
            <select id="emotion" name="emotion">
                <option value="">AI에게 맡기기</option>
                <option value="행복">행복</option>
                <option value="슬픔">슬픔</option>
                <option value="분노">분노</option>
                <option value="불안">불안</option>
                <option value="평온">평온</option>
            </select>
        </div>

        <br>
        <button type="submit">저장하기</button>
    </form>
</div>
</body>
</html>
