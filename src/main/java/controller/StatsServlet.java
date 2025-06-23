package controller;

import dao.DiaryDAO;
import model.Diary;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name = "StatsServlet", urlPatterns = "/stats")
public class StatsServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(StatsServlet.class);
    
    private DiaryDAO diaryDAO;
    private Gson gson;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] EMOTION_ORDER = {"행복", "평온", "복잡", "피로", "불안", "슬픔", "분노"};

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            this.diaryDAO = new DiaryDAO();
            this.gson = new Gson();
            logger.info("StatsServlet initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize StatsServlet", e);
            throw new ServletException("Failed to initialize StatsServlet", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // JSON 응답 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String statsType = request.getParameter("type");
            JsonObject statsData = new JsonObject();
            
            if (statsType == null || statsType.equals("all")) {
                // 전체 통계 데이터 반환
                statsData = getAllStats();
            } else if (statsType.equals("emotion")) {
                // 감정별 통계
                statsData = getEmotionStats();
            } else if (statsType.equals("monthly")) {
                // 월별 통계
                statsData = getMonthlyStats();
            } else if (statsType.equals("recent")) {
                // 최근 7일 통계
                statsData = getRecentStats();
            } else {
                // 기본값: 전체 통계
                statsData = getAllStats();
            }
            
            // JSON 응답 전송
            PrintWriter out = response.getWriter();
            out.print(gson.toJson(statsData));
            out.flush();
            
            logger.info("Stats data sent successfully for type: {}", statsType);
            
        } catch (Exception e) {
            logger.error("Error generating stats", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "통계 데이터를 불러오는 중 오류가 발생했습니다.");
            
            PrintWriter out = response.getWriter();
            out.print(gson.toJson(errorResponse));
            out.flush();
        }
    }    
    /**
     * 전체 통계 데이터 생성
     */
    private JsonObject getAllStats() {
        JsonObject allStats = new JsonObject();
        
        try {
            // 전체 일기 수
            List<Diary> allDiaries = diaryDAO.getAllDiaries();
            allStats.addProperty("totalDiaries", allDiaries.size());
            
            // 감정별 통계 추가
            JsonObject emotionStats = getEmotionStatsData(allDiaries);
            allStats.add("emotions", emotionStats);
            
            // 최근 활동 통계
            JsonObject recentActivity = getRecentActivityData(allDiaries);
            allStats.add("recentActivity", recentActivity);
            
            // 월별 작성 횟수
            JsonArray monthlyData = getMonthlyStatsData(allDiaries);
            allStats.add("monthly", monthlyData);
            
        } catch (Exception e) {
            logger.error("Error generating all stats", e);
        }
        
        return allStats;
    }
    
    /**
     * 감정별 통계 데이터 생성
     */
    private JsonObject getEmotionStats() {
        JsonObject emotionStats = new JsonObject();
        
        try {
            List<Diary> allDiaries = diaryDAO.getAllDiaries();
            JsonObject emotionData = getEmotionStatsData(allDiaries);
            emotionStats.add("data", emotionData);
            emotionStats.addProperty("total", allDiaries.size());
            
        } catch (Exception e) {
            logger.error("Error generating emotion stats", e);
        }
        
        return emotionStats;
    }    
    /**
     * 월별 통계 데이터 생성
     */
    private JsonObject getMonthlyStats() {
        JsonObject monthlyStats = new JsonObject();
        
        try {
            List<Diary> allDiaries = diaryDAO.getAllDiaries();
            JsonArray monthlyData = getMonthlyStatsData(allDiaries);
            monthlyStats.add("data", monthlyData);
            
        } catch (Exception e) {
            logger.error("Error generating monthly stats", e);
        }
        
        return monthlyStats;
    }
    
    /**
     * 최근 7일 통계 데이터 생성
     */
    private JsonObject getRecentStats() {
        JsonObject recentStats = new JsonObject();
        
        try {
            // 최근 7일 데이터 필터링
            LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
            List<Diary> allDiaries = diaryDAO.getAllDiaries();
            
            List<Diary> recentDiaries = allDiaries.stream()
                .filter(diary -> {
                    try {
                        LocalDate diaryDate = LocalDate.parse(diary.getCreatedAt().substring(0, 10));
                        return !diaryDate.isBefore(sevenDaysAgo);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
            
            recentStats.addProperty("totalRecentDiaries", recentDiaries.size());
            
            JsonObject emotionData = getEmotionStatsData(recentDiaries);
            recentStats.add("emotions", emotionData);
            
        } catch (Exception e) {
            logger.error("Error generating recent stats", e);
        }
        
        return recentStats;
    }    
    /**
     * 감정별 통계 데이터 헬퍼 메소드
     */
    private JsonObject getEmotionStatsData(List<Diary> diaries) {
        JsonObject emotionData = new JsonObject();
        
        // 감정별 카운트
        Map<String, Integer> emotionCounts = new HashMap<>();
        for (String emotion : EMOTION_ORDER) {
            emotionCounts.put(emotion, 0);
        }
        
        for (Diary diary : diaries) {
            String emotion = diary.getEmotionSummary();
            if (emotion != null && emotionCounts.containsKey(emotion)) {
                emotionCounts.put(emotion, emotionCounts.get(emotion) + 1);
            }
        }
        
        // JSON으로 변환
        for (String emotion : EMOTION_ORDER) {
            emotionData.addProperty(emotion, emotionCounts.get(emotion));
        }
        
        return emotionData;
    }
    
    /**
     * 최근 활동 데이터 헬퍼 메소드
     */
    private JsonObject getRecentActivityData(List<Diary> diaries) {
        JsonObject activityData = new JsonObject();
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate lastWeek = today.minusDays(7);
            LocalDate lastMonth = today.minusDays(30);
            
            long thisWeekCount = diaries.stream()
                .filter(diary -> {
                    try {
                        LocalDate diaryDate = LocalDate.parse(diary.getCreatedAt().substring(0, 10));
                        return !diaryDate.isBefore(lastWeek);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();
            
            long thisMonthCount = diaries.stream()
                .filter(diary -> {
                    try {
                        LocalDate diaryDate = LocalDate.parse(diary.getCreatedAt().substring(0, 10));
                        return !diaryDate.isBefore(lastMonth);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();
            
            activityData.addProperty("thisWeek", thisWeekCount);
            activityData.addProperty("thisMonth", thisMonthCount);
            
        } catch (Exception e) {
            logger.error("Error calculating recent activity", e);
        }
        
        return activityData;
    }    
    /**
     * 월별 통계 데이터 헬퍼 메소드
     */
    private JsonArray getMonthlyStatsData(List<Diary> diaries) {
        JsonArray monthlyArray = new JsonArray();
        
        try {
            // 최근 12개월 데이터 생성
            LocalDate currentDate = LocalDate.now();
            
            for (int i = 11; i >= 0; i--) {
                LocalDate monthDate = currentDate.minusMonths(i);
                String monthKey = monthDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                
                long monthCount = diaries.stream()
                    .filter(diary -> {
                        try {
                            String diaryDateStr = diary.getCreatedAt().substring(0, 7); // yyyy-MM
                            return diaryDateStr.equals(monthKey);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .count();
                
                JsonObject monthData = new JsonObject();
                monthData.addProperty("month", monthKey);
                monthData.addProperty("count", monthCount);
                monthlyArray.add(monthData);
            }
            
        } catch (Exception e) {
            logger.error("Error calculating monthly stats", e);
        }
        
        return monthlyArray;
    }
}