package controller;

import dao.DiaryDAO;
import api.MindAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "WriteServlet", urlPatterns = "/write")
public class WriteServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(WriteServlet.class);
    
    private DiaryDAO diaryDAO;
    private MindAnalyzer mindAnalyzer;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            this.diaryDAO = new DiaryDAO();
            this.mindAnalyzer = new MindAnalyzer();
            logger.info("WriteServlet initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize WriteServlet", e);
            throw new ServletException("Failed to initialize WriteServlet", e);
        }
    }    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // GET 요청 시 일기 작성 페이지로 리다이렉트
        response.sendRedirect("write.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 한글 인코딩 설정
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        
        try {
            // 요청 파라미터 추출
            String content = request.getParameter("content");
            String userEmotion = request.getParameter("emotion");
            
            logger.info("Received diary write request - content length: {}, user emotion: {}", 
                       content != null ? content.length() : 0, userEmotion);
            
            // 입력값 검증
            ValidationResult validation = validateInput(content);
            if (!validation.isValid()) {
                handleValidationError(request, response, validation.getErrorMessage());
                return;
            }
            
            // 감정 분석 수행
            String analyzedEmotion = performEmotionAnalysis(content, userEmotion);
            
            // 데이터베이스에 저장
            boolean saveResult = saveDiaryToDatabase(content, analyzedEmotion);
            
            if (saveResult) {
                handleSuccess(request, response, analyzedEmotion);
            } else {
                handleError(request, response, "일기 저장 중 오류가 발생했습니다. 다시 시도해주세요.");
            }
            
        } catch (Exception e) {
            logger.error("Error processing diary write request", e);
            handleError(request, response, "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
    
    /**
     * 입력값 검증
     */
    private ValidationResult validateInput(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new ValidationResult(false, "일기 내용을 입력해주세요.");
        }
        
        if (content.trim().length() < 10) {
            return new ValidationResult(false, "일기 내용을 10자 이상 입력해주세요.");
        }
        
        if (content.length() > 5000) {
            return new ValidationResult(false, "일기 내용은 5000자를 초과할 수 없습니다.");
        }
        
        // 스팸 또는 부적절한 내용 검사 (간단한 예시)
        if (isInappropriateContent(content)) {
            return new ValidationResult(false, "부적절한 내용이 포함되어 있습니다.");
        }
        
        return new ValidationResult(true, null);
    }    
    /**
     * 부적절한 내용 검사 (간단한 구현)
     */
    private boolean isInappropriateContent(String content) {
        String[] inappropriateWords = {"스팸", "광고", "홍보"};
        String lowerContent = content.toLowerCase();
        
        for (String word : inappropriateWords) {
            if (lowerContent.contains(word)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 감정 분석 수행
     */
    private String performEmotionAnalysis(String content, String userEmotion) {
        try {
            // 사용자가 직접 감정을 선택한 경우
            if (userEmotion != null && !userEmotion.trim().isEmpty() && !userEmotion.equals("")) {
                logger.info("User provided emotion: {}", userEmotion);
                return userEmotion;
            }
            
            // AI 감정 분석 수행
            String analyzedEmotion = mindAnalyzer.analyzeEmotion(content);
            logger.info("AI analyzed emotion: {}", analyzedEmotion);
            
            return analyzedEmotion;
            
        } catch (Exception e) {
            logger.error("Error during emotion analysis", e);
            return "중립"; // 분석 실패 시 기본값
        }
    }    
    /**
     * 데이터베이스에 일기 저장
     */
    private boolean saveDiaryToDatabase(String content, String emotion) {
        try {
            boolean result = diaryDAO.insertDiary(content, emotion);
            if (result) {
                logger.info("Diary saved successfully with emotion: {}", emotion);
            } else {
                logger.warn("Failed to save diary to database");
            }
            return result;
        } catch (Exception e) {
            logger.error("Database error while saving diary", e);
            return false;
        }
    }
    
    /**
     * 성공 처리
     */
    private void handleSuccess(HttpServletRequest request, HttpServletResponse response, 
                              String emotion) throws ServletException, IOException {
        
        // 감정에 따른 응답 메시지 생성
        String responseMessage = mindAnalyzer.generateEmotionResponse(emotion);
        
        // 성공 정보를 request에 설정
        request.setAttribute("success", true);
        request.setAttribute("emotion", emotion);
        request.setAttribute("responseMessage", responseMessage);
        request.setAttribute("message", "일기가 성공적으로 저장되었습니다!");
        
        logger.info("Diary write completed successfully with emotion: {}", emotion);
        
        // 결과 페이지로 포워드
        request.getRequestDispatcher("write_result.jsp").forward(request, response);
    }    
    /**
     * 검증 오류 처리
     */
    private void handleValidationError(HttpServletRequest request, HttpServletResponse response, 
                                     String errorMessage) throws ServletException, IOException {
        request.setAttribute("error", true);
        request.setAttribute("errorMessage", errorMessage);
        request.setAttribute("content", request.getParameter("content")); // 입력한 내용 보존
        
        logger.warn("Validation error: {}", errorMessage);
        
        // 다시 작성 페이지로 포워드
        request.getRequestDispatcher("write.jsp").forward(request, response);
    }
    
    /**
     * 일반 오류 처리
     */
    private void handleError(HttpServletRequest request, HttpServletResponse response, 
                           String errorMessage) throws ServletException, IOException {
        request.setAttribute("error", true);
        request.setAttribute("errorMessage", errorMessage);
        request.setAttribute("content", request.getParameter("content")); // 입력한 내용 보존
        
        logger.error("Error occurred: {}", errorMessage);
        
        // 다시 작성 페이지로 포워드
        request.getRequestDispatcher("write.jsp").forward(request, response);
    }
    
    /**
     * 검증 결과를 담는 내부 클래스
     */
    private static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;        
        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}