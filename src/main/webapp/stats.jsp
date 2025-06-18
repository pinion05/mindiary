<%--
  Created by IntelliJ IDEA.
  User: tlsth
  Date: 25. 6. 9.
  Time: 오후 8:32
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html>
<head>
    <title>감정 통계 - Mindiary Emotion</title>
    <%@ include file ="include.jsp" %>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <div class="container">
        <h2>감정 통계 (이번 주)</h2>
        <canvas id="emotionChart" width="600" height="400"></canvas>

        <%
            Map<String, Float> emotionStats = (Map<String, Float>) request.getAttribute("emotionStats");
            if (emotionStats == null) {
                emotionStats = new java.util.LinkedHashMap<>();
                emotionStats.put("happy", 0.7f);
                emotionStats.put("sad", 0.2f);
                emotionStats.put("angry", 0.1f);
            }

            StringBuilder labels = new StringBuilder();
            StringBuilder data = new StringBuilder();

            for (Map.Entry<String, Float> entry : emotionStats.entrySet()) {
                labels.append("\"").append(entry.getKey()).append("\",");
                data.append(entry.getValue()).append(",");
            }

            if (labels.length() > 0) labels.setLength(labels.length() - 1);
            if (data.length() > 0) data.setLength(data.length() - 1);
        %>

        <script>
            const labels = [<%= labels.toString() %>];
            const data = {
                labels: labels,
                datasets: [{
                    label: '감정 점수',
                    data: [<%= data.toString() %>],
                    borderWidth: 1
                }]
            };

            const config = {
                type: 'bar',
                data: data,
                options: {
                    scales: {
                        y: {
                            beginAtZero: true,
                            max: 1
                        }
                    }
                }
            };

            new Chart(document.getElementById('emotionChart'), config);
        </script>
    </div>
</body>
</html>