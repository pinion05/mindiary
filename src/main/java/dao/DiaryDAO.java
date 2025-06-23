package dao;

import model.Diary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiaryDAO {
    private static final Logger logger = LoggerFactory.getLogger(DiaryDAO.class);
    private static final String DB_URL = "jdbc:sqlite:mindiary.db";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DiaryDAO() {
        initializeDatabase();
    }

    /**
     * 데이터베이스 초기화 - 테이블 생성
     */
    private void initializeDatabase() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS diary (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                content TEXT NOT NULL,
                emotion_summary TEXT,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            )
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createTableSQL);
            logger.info("Database initialized successfully");
            
        } catch (SQLException e) {
            logger.error("Database initialization failed", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    /**
     * 새로운 일기 저장
     */
    public boolean insertDiary(String content, String emotionSummary) {
        String insertSQL = """
            INSERT INTO diary (content, emotion_summary, created_at, updated_at)
            VALUES (?, ?, ?, ?)
            """;

        String now = LocalDateTime.now().format(formatter);
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            
            pstmt.setString(1, content);
            pstmt.setString(2, emotionSummary);
            pstmt.setString(3, now);
            pstmt.setString(4, now);
            
            int result = pstmt.executeUpdate();
            logger.info("Diary inserted successfully. Rows affected: {}", result);
            return result > 0;            
        } catch (SQLException e) {
            logger.error("Failed to insert diary", e);
            return false;
        }
    }

    /**
     * 모든 일기 조회 (최신순)
     */
    public List<Diary> getAllDiaries() {
        String selectSQL = """
            SELECT content, emotion_summary, created_at
            FROM diary
            ORDER BY created_at DESC
            """;
        
        List<Diary> diaries = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            
            while (rs.next()) {
                Diary diary = new Diary(
                    rs.getString("content"),
                    rs.getString("emotion_summary"),
                    rs.getString("created_at")
                );
                diaries.add(diary);
            }
            
            logger.info("Retrieved {} diaries", diaries.size());            
        } catch (SQLException e) {
            logger.error("Failed to retrieve diaries", e);
        }
        
        return diaries;
    }

    /**
     * 감정별 일기 필터링 조회
     */
    public List<Diary> getDiariesByEmotion(String emotion) {
        String selectSQL = """
            SELECT content, emotion_summary, created_at
            FROM diary
            WHERE emotion_summary LIKE ?
            ORDER BY created_at DESC
            """;
        
        List<Diary> diaries = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            
            pstmt.setString(1, "%" + emotion + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Diary diary = new Diary(
                        rs.getString("content"),
                        rs.getString("emotion_summary"),
                        rs.getString("created_at")
                    );
                    diaries.add(diary);
                }
            }
            
            logger.info("Retrieved {} diaries for emotion: {}", diaries.size(), emotion);
            
        } catch (SQLException e) {
            logger.error("Failed to retrieve diaries by emotion: {}", emotion, e);
        }
        
        return diaries;
    }

    /**
     * 감정 통계 데이터 조회
     */
    public Map<String, Integer> getEmotionStatistics() {
        String statsSQL = """
            SELECT emotion_summary, COUNT(*) as count
            FROM diary
            WHERE emotion_summary IS NOT NULL AND emotion_summary != ''
            GROUP BY emotion_summary
            ORDER BY count DESC
            """;
        
        Map<String, Integer> stats = new HashMap<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(statsSQL)) {
            
            while (rs.next()) {
                stats.put(rs.getString("emotion_summary"), rs.getInt("count"));
            }
            
            logger.info("Retrieved emotion statistics: {}", stats);
            
        } catch (SQLException e) {
            logger.error("Failed to retrieve emotion statistics", e);
        }
        
        return stats;
    }

    /**
     * 최근 N일간의 일기 개수 조회
     */
    public int getDiaryCountForLastDays(int days) {
        String countSQL = """
            SELECT COUNT(*) as count
            FROM diary
            WHERE datetime(created_at) >= datetime('now', '-' || ? || ' days')
            """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(countSQL)) {
            
            pstmt.setInt(1, days);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    logger.info("Found {} diaries in last {} days", count, days);
                    return count;
                }
            }            
        } catch (SQLException e) {
            logger.error("Failed to get diary count for last {} days", days, e);
        }
        
        return 0;
    }

    /**
     * 특정 날짜의 일기 조회
     */
    public List<Diary> getDiariesByDate(String date) {
        String selectSQL = """
            SELECT content, emotion_summary, created_at
            FROM diary
            WHERE DATE(created_at) = ?
            ORDER BY created_at DESC
            """;
        
        List<Diary> diaries = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            
            pstmt.setString(1, date);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Diary diary = new Diary(
                        rs.getString("content"),
                        rs.getString("emotion_summary"),
                        rs.getString("created_at")
                    );
                    diaries.add(diary);
                }
            }
            
            logger.info("Retrieved {} diaries for date: {}", diaries.size(), date);
            
        } catch (SQLException e) {
            logger.error("Failed to retrieve diaries for date: {}", date, e);
        }
        
        return diaries;
    }

    /**
     * 데이터베이스 연결 테스트
     */
    public boolean testConnection() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            logger.info("Database connection test successful");
            return true;
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }

    /**
     * 전체 일기 개수 조회
     */
    public int getTotalDiaryCount() {
        String countSQL = "SELECT COUNT(*) as count FROM diary";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSQL)) {
            
            if (rs.next()) {
                int count = rs.getInt("count");
                logger.info("Total diary count: {}", count);
                return count;
            }
            
        } catch (SQLException e) {
            logger.error("Failed to get total diary count", e);
        }
        
        return 0;
    }
}