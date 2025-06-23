package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    
    // 데이터베이스 설정
    private static final String DEFAULT_DB_URL = "jdbc:sqlite:mindiary.db";
    private static final String BACKUP_DB_PATH = "mindiary_backup.db";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // 데이터베이스 연결 풀 설정
    private static final int MAX_CONNECTIONS = 10;
    private static final int CONNECTION_TIMEOUT = 30; // seconds
    
    // 싱글톤 인스턴스
    private static DatabaseUtil instance;
    private static final Object LOCK = new Object();
    
    // 설정 프로퍼티
    private Properties dbProperties;
    private String dbUrl;
    
    /**
     * 싱글톤 인스턴스 반환
     */
    public static DatabaseUtil getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new DatabaseUtil();
                }
            }
        }
        return instance;
    }
    
    /**
     * 생성자 (private)
     */
    private DatabaseUtil() {
        initialize();
    }
    
    /**
     * 데이터베이스 유틸리티 초기화
     */
    private void initialize() {
        try {
            loadDatabaseProperties();
            setupDatabase();
            logger.info("DatabaseUtil initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize DatabaseUtil", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * 데이터베이스 설정 로드
     */
    private void loadDatabaseProperties() {
        dbProperties = new Properties();
        
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (is != null) {
                dbProperties.load(is);
                dbUrl = dbProperties.getProperty("db.url", DEFAULT_DB_URL);
                logger.info("Database properties loaded from file");
            } else {
                // 기본 설정 사용
                dbUrl = DEFAULT_DB_URL;
                setupDefaultProperties();
                logger.info("Using default database configuration");
            }
        } catch (IOException e) {
            logger.warn("Could not load database.properties, using defaults", e);
            dbUrl = DEFAULT_DB_URL;
            setupDefaultProperties();
        }
    }
    
    /**
     * 기본 데이터베이스 설정
     */
    private void setupDefaultProperties() {
        dbProperties.setProperty("db.url", DEFAULT_DB_URL);
        dbProperties.setProperty("db.driver", "org.sqlite.JDBC");
        dbProperties.setProperty("db.maxConnections", String.valueOf(MAX_CONNECTIONS));
        dbProperties.setProperty("db.connectionTimeout", String.valueOf(CONNECTION_TIMEOUT));
    }
    
    /**
     * 데이터베이스 및 테이블 설정
     */
    private void setupDatabase() {
        try {
            createDatabaseIfNotExists();
            createTablesIfNotExist();
            insertDefaultDataIfNeeded();
        } catch (SQLException e) {
            logger.error("Error setting up database", e);
            throw new RuntimeException("Failed to setup database", e);
        }
    }
    
    /**
     * 데이터베이스 파일 생성 (존재하지 않는 경우)
     */
    private void createDatabaseIfNotExists() throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            logger.info("Database file verified/created: {}", dbUrl);
        }
    }
    
    /**
     * 필요한 테이블들 생성
     */
    private void createTablesIfNotExist() throws SQLException {
        String[] createTableSQLs = {
            // 일기 테이블
            """
            CREATE TABLE IF NOT EXISTS diary (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                content TEXT NOT NULL,
                emotion_summary TEXT,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            )
            """,
            
            // 사용자 설정 테이블 (향후 확장용)
            """
            CREATE TABLE IF NOT EXISTS user_settings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                setting_key TEXT UNIQUE NOT NULL,
                setting_value TEXT,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            )
            """,
            
            // 백업 로그 테이블
            """
            CREATE TABLE IF NOT EXISTS backup_log (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                backup_path TEXT NOT NULL,
                backup_size INTEGER,
                created_at TEXT NOT NULL,
                status TEXT DEFAULT 'SUCCESS'
            )
            """
        };
        
        try (Connection conn = getConnection()) {
            for (String sql : createTableSQLs) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                }
            }
            logger.info("All required tables created/verified");
        }
    }
    
    /**
     * 기본 데이터 삽입 (필요한 경우)
     */
    private void insertDefaultDataIfNeeded() throws SQLException {
        String checkSQL = "SELECT COUNT(*) FROM user_settings WHERE setting_key = ?";
        String insertSQL = "INSERT INTO user_settings (setting_key, setting_value, created_at, updated_at) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = getConnection()) {
            String now = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            
            // 기본 설정값들
            String[][] defaultSettings = {
                {"app_version", "1.0.0"},
                {"emotion_analysis_enabled", "true"},
                {"backup_enabled", "true"},
                {"max_diary_length", "5000"}
            };
            
            for (String[] setting : defaultSettings) {
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
                    checkStmt.setString(1, setting[0]);
                    ResultSet rs = checkStmt.executeQuery();
                    
                    if (rs.next() && rs.getInt(1) == 0) {
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                            insertStmt.setString(1, setting[0]);
                            insertStmt.setString(2, setting[1]);
                            insertStmt.setString(3, now);
                            insertStmt.setString(4, now);
                            insertStmt.executeUpdate();
                        }
                    }
                }
            }
            
            logger.info("Default settings verified/inserted");
        }
    }
    
    /**
     * 데이터베이스 연결 반환
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(dbUrl);
            
            // SQLite 최적화 설정
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA journal_mode = WAL");
                stmt.execute("PRAGMA synchronous = NORMAL");
                stmt.execute("PRAGMA cache_size = 1000");
                stmt.execute("PRAGMA temp_store = MEMORY");
            }
            
            return conn;
        } catch (SQLException e) {
            logger.error("Failed to get database connection", e);
            throw e;
        }
    }
    
    /**
     * 연결 테스트
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(CONNECTION_TIMEOUT);
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }
    
    /**
     * 데이터베이스 백업
     */
    public boolean backupDatabase() {
        try {
            String backupPath = generateBackupPath();
            String backupSQL = "BACKUP TO '" + backupPath + "'";
            
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // SQLite의 BACKUP 명령 사용 (SQLite 3.27.0+)
                // 대안: 파일 복사 방식
                return createFileBasedBackup(backupPath);
            }
        } catch (Exception e) {
            logger.error("Database backup failed", e);
            return false;
        }
    }
    
    /**
     * 파일 기반 백업 생성
     */
    private boolean createFileBasedBackup(String backupPath) {
        try {
            // 현재 데이터베이스의 모든 데이터를 새 파일로 복사
            String attachSQL = "ATTACH DATABASE ? AS backup_db";
            String copyDataSQL = """
                CREATE TABLE backup_db.diary AS SELECT * FROM main.diary;
                CREATE TABLE backup_db.user_settings AS SELECT * FROM main.user_settings;
                CREATE TABLE backup_db.backup_log AS SELECT * FROM main.backup_log;
                """;
            
            try (Connection conn = getConnection()) {
                // 백업 데이터베이스 연결
                try (PreparedStatement attachStmt = conn.prepareStatement(attachSQL)) {
                    attachStmt.setString(1, backupPath);
                    attachStmt.execute();
                }
                
                // 데이터 복사
                try (Statement stmt = conn.createStatement()) {
                    for (String sql : copyDataSQL.split(";")) {
                        if (!sql.trim().isEmpty()) {
                            stmt.execute(sql.trim());
                        }
                    }
                }
                
                // 백업 로그 기록
                logBackup(backupPath, true);
                
                logger.info("Database backup created successfully: {}", backupPath);
                return true;
            }
        } catch (SQLException e) {
            logger.error("File-based backup failed", e);
            logBackup(backupPath, false);
            return false;
        }
    }
    
    /**
     * 백업 파일 경로 생성
     */
    private String generateBackupPath() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "mindiary_backup_" + timestamp + ".db";
    }
    
    /**
     * 백업 로그 기록
     */
    private void logBackup(String backupPath, boolean success) {
        String insertSQL = "INSERT INTO backup_log (backup_path, backup_size, created_at, status) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
            
            stmt.setString(1, backupPath);
            stmt.setLong(2, 0); // 파일 크기는 나중에 계산 가능
            stmt.setString(3, LocalDateTime.now().format(TIMESTAMP_FORMAT));
            stmt.setString(4, success ? "SUCCESS" : "FAILED");
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to log backup operation", e);
        }
    }
    
    /**
     * 데이터베이스 정리 (오래된 데이터 삭제)
     */
    public int cleanupOldData(int daysToKeep) {
        String deleteSQL = "DELETE FROM diary WHERE DATE(created_at) < DATE('now', '-' || ? || ' days')";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {
            
            stmt.setInt(1, daysToKeep);
            int deletedCount = stmt.executeUpdate();
            
            logger.info("Cleaned up {} old diary entries (older than {} days)", deletedCount, daysToKeep);
            return deletedCount;
            
        } catch (SQLException e) {
            logger.error("Failed to cleanup old data", e);
            return -1;
        }
    }
    
    /**
     * 데이터베이스 통계 조회
     */
    public DatabaseStats getDatabaseStats() {
        DatabaseStats stats = new DatabaseStats();
        
        try (Connection conn = getConnection()) {
            // 테이블별 레코드 수 조회
            stats.diaryCount = getTableRowCount(conn, "diary");
            stats.settingsCount = getTableRowCount(conn, "user_settings");
            stats.backupLogCount = getTableRowCount(conn, "backup_log");
            
            // 데이터베이스 크기 (근사값)
            stats.databaseSize = getDatabaseSize(conn);
            
            logger.debug("Database stats: diaries={}, settings={}, backups={}, size={}KB", 
                        stats.diaryCount, stats.settingsCount, stats.backupLogCount, stats.databaseSize);
                        
        } catch (SQLException e) {
            logger.error("Failed to get database stats", e);
        }
        
        return stats;
    }
    
    /**
     * 테이블 행 수 조회
     */
    private int getTableRowCount(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    /**
     * 데이터베이스 크기 조회 (KB)
     */
    private long getDatabaseSize(Connection conn) throws SQLException {
        String sql = "PRAGMA page_count; PRAGMA page_size;";
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs1 = stmt.executeQuery("PRAGMA page_count");
            int pageCount = rs1.next() ? rs1.getInt(1) : 0;
            
            ResultSet rs2 = stmt.executeQuery("PRAGMA page_size");
            int pageSize = rs2.next() ? rs2.getInt(1) : 0;
            
            return (long) pageCount * pageSize / 1024; // KB 단위
        }
    }
    
    /**
     * 설정값 조회
     */
    public String getSetting(String key, String defaultValue) {
        String sql = "SELECT setting_value FROM user_settings WHERE setting_key = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, key);
            ResultSet rs = stmt.executeQuery();
            
            return rs.next() ? rs.getString("setting_value") : defaultValue;
            
        } catch (SQLException e) {
            logger.error("Failed to get setting: {}", key, e);
            return defaultValue;
        }
    }
    
    /**
     * 설정값 저장
     */
    public boolean setSetting(String key, String value) {
        String upsertSQL = """
            INSERT OR REPLACE INTO user_settings (setting_key, setting_value, created_at, updated_at)
            VALUES (?, ?, COALESCE((SELECT created_at FROM user_settings WHERE setting_key = ?), ?), ?)
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(upsertSQL)) {
            
            String now = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.setString(3, key);
            stmt.setString(4, now);
            stmt.setString(5, now);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            logger.error("Failed to set setting: {} = {}", key, value, e);
            return false;
        }
    }
    
    /**
     * 데이터베이스 종료
     */
    public void shutdown() {
        try {
            // SQLite는 별도 종료 작업이 필요하지 않음
            logger.info("DatabaseUtil shutdown completed");
        } catch (Exception e) {
            logger.error("Error during database shutdown", e);
        }
    }
    
    /**
     * 데이터베이스 통계 정보 클래스
     */
    public static class DatabaseStats {
        public int diaryCount;
        public int settingsCount;
        public int backupLogCount;
        public long databaseSize; // KB
        
        @Override
        public String toString() {
            return String.format("DatabaseStats{diaries=%d, settings=%d, backups=%d, size=%dKB}", 
                               diaryCount, settingsCount, backupLogCount, databaseSize);
        }
    }
}

