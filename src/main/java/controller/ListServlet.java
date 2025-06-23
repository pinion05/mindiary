package controller;

import dao.DiaryDAO;
import model.Diary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "ListServlet", urlPatterns = "/list")
public class ListServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ListServlet.class);
    
    private DiaryDAO diaryDAO;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            this.diaryDAO = new DiaryDAO();
            logger.info("ListServlet initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize ListServlet", e);
            throw new ServletException("Failed to initialize ListServlet", e);
        }
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 한글 인코딩 설정
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        
        try {
            // 요청 파라미터 추출
            FilterParams filterParams = extractFilterParams(request);
            
            logger.info("List request - emotion: {}, date: {}, search: {}, page: {}", 
                       filterParams.emotion, filterParams.date, filterParams.searchKeyword, filterParams.page);
            
            // 일기 목록 조회
            List<Diary> diaries = fetchDiaries(filterParams);
            
            // 페이징 처리
            PaginationResult paginationResult = applyPagination(diaries, filterParams.page, filterParams.pageSize);
            
            // 요청 속성 설정
            setRequestAttributes(request, paginationResult, filterParams);
            
            // JSP로 포워드
            request.getRequestDispatcher("list.jsp").forward(request, response);
            
        } catch (Exception e) {
            logger.error("Error processing list request", e);
            handleError(request, response, "일기 목록을 불러오는 중 오류가 발생했습니다.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // POST 요청도 GET과 동일하게 처리
        doGet(request, response);
    }    
    /**
     * 필터링 파라미터 추출
     */
    private FilterParams extractFilterParams(HttpServletRequest request) {
        FilterParams params = new FilterParams();
        
        // 감정 필터
        params.emotion = request.getParameter("emotion");
        if (params.emotion != null && params.emotion.trim().isEmpty()) {
            params.emotion = null;
        }
        
        // 날짜 필터
        params.date = request.getParameter("date");
        if (params.date != null && params.date.trim().isEmpty()) {
            params.date = null;
        }
        
        // 검색 키워드
        params.searchKeyword = request.getParameter("search");
        if (params.searchKeyword != null && params.searchKeyword.trim().isEmpty()) {
            params.searchKeyword = null;
        }
        
        // 페이지 번호
        try {
            String pageParam = request.getParameter("page");
            params.page = (pageParam != null) ? Integer.parseInt(pageParam) : 1;
            if (params.page < 1) params.page = 1;
        } catch (NumberFormatException e) {
            params.page = 1;
        }        
        // 페이지 크기
        try {
            String pageSizeParam = request.getParameter("pageSize");
            params.pageSize = (pageSizeParam != null) ? Integer.parseInt(pageSizeParam) : DEFAULT_PAGE_SIZE;
            if (params.pageSize < 1 || params.pageSize > 100) params.pageSize = DEFAULT_PAGE_SIZE;
        } catch (NumberFormatException e) {
            params.pageSize = DEFAULT_PAGE_SIZE;
        }
        
        // 정렬 방식
        params.sortBy = request.getParameter("sortBy");
        if (params.sortBy == null || (!params.sortBy.equals("date_asc") && !params.sortBy.equals("date_desc"))) {
            params.sortBy = "date_desc"; // 기본값: 최신순
        }
        
        return params;
    }
    
    /**
     * 필터링 조건에 따라 일기 목록 조회
     */
    private List<Diary> fetchDiaries(FilterParams params) {
        List<Diary> diaries;
        
        try {
            // 날짜 필터가 있는 경우
            if (params.date != null) {
                if (isValidDate(params.date)) {
                    diaries = diaryDAO.getDiariesByDate(params.date);
                    logger.info("Fetched {} diaries for date: {}", diaries.size(), params.date);
                } else {
                    logger.warn("Invalid date format: {}", params.date);
                    diaries = diaryDAO.getAllDiaries();
                }
            }            // 감정 필터가 있는 경우
            else if (params.emotion != null) {
                diaries = diaryDAO.getDiariesByEmotion(params.emotion);
                logger.info("Fetched {} diaries for emotion: {}", diaries.size(), params.emotion);
            }
            // 전체 조회
            else {
                diaries = diaryDAO.getAllDiaries();
                logger.info("Fetched {} total diaries", diaries.size());
            }
            
            // 검색 키워드 필터링
            if (params.searchKeyword != null) {
                diaries = filterByKeyword(diaries, params.searchKeyword);
                logger.info("After keyword filtering: {} diaries", diaries.size());
            }
            
            // 정렬 처리
            diaries = applySorting(diaries, params.sortBy);
            
            return diaries;
            
        } catch (Exception e) {
            logger.error("Error fetching diaries", e);
            return diaryDAO.getAllDiaries(); // 오류 시 전체 목록 반환
        }
    }
    
    /**
     * 날짜 형식 검증
     */
    private boolean isValidDate(String dateString) {
        try {
            LocalDate.parse(dateString, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }    
    /**
     * 키워드로 일기 내용 필터링
     */
    private List<Diary> filterByKeyword(List<Diary> diaries, String keyword) {
        String lowerKeyword = keyword.toLowerCase().trim();
        
        return diaries.stream()
                .filter(diary -> diary.getContent().toLowerCase().contains(lowerKeyword) ||
                               (diary.getEmotionSummary() != null && 
                                diary.getEmotionSummary().toLowerCase().contains(lowerKeyword)))
                .collect(Collectors.toList());
    }
    
    /**
     * 정렬 적용
     */
    private List<Diary> applySorting(List<Diary> diaries, String sortBy) {
        if ("date_asc".equals(sortBy)) {
            return diaries.stream()
                    .sorted((d1, d2) -> d1.getCreatedAt().compareTo(d2.getCreatedAt()))
                    .collect(Collectors.toList());
        } else {
            // date_desc (기본값)
            return diaries.stream()
                    .sorted((d1, d2) -> d2.getCreatedAt().compareTo(d1.getCreatedAt()))
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * 페이징 처리
     */
    private PaginationResult applyPagination(List<Diary> allDiaries, int currentPage, int pageSize) {
        PaginationResult result = new PaginationResult();        
        result.totalItems = allDiaries.size();
        result.totalPages = (int) Math.ceil((double) result.totalItems / pageSize);
        result.currentPage = Math.min(currentPage, Math.max(1, result.totalPages));
        result.pageSize = pageSize;
        
        // 현재 페이지의 일기 목록 계산
        int startIndex = (result.currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, result.totalItems);
        
        if (startIndex < result.totalItems && startIndex >= 0) {
            result.diaries = allDiaries.subList(startIndex, endIndex);
        } else {
            result.diaries = List.of();
        }
        
        // 페이징 네비게이션 정보
        result.hasPrevious = result.currentPage > 1;
        result.hasNext = result.currentPage < result.totalPages;
        result.previousPage = result.hasPrevious ? result.currentPage - 1 : 1;
        result.nextPage = result.hasNext ? result.currentPage + 1 : result.totalPages;
        
        logger.info("Pagination applied - page {}/{}, showing {} items", 
                   result.currentPage, result.totalPages, result.diaries.size());
        
        return result;
    }
    
    /**
     * 요청 속성 설정
     */
    private void setRequestAttributes(HttpServletRequest request, PaginationResult pagination, FilterParams params) {
        // 일기 목록과 페이징 정보
        request.setAttribute("diaryList", pagination.diaries);
        request.setAttribute("totalItems", pagination.totalItems);        request.setAttribute("totalPages", pagination.totalPages);
        request.setAttribute("currentPage", pagination.currentPage);
        request.setAttribute("pageSize", pagination.pageSize);
        request.setAttribute("hasPrevious", pagination.hasPrevious);
        request.setAttribute("hasNext", pagination.hasNext);
        request.setAttribute("previousPage", pagination.previousPage);
        request.setAttribute("nextPage", pagination.nextPage);
        
        // 필터 정보
        request.setAttribute("selectedEmotion", params.emotion);
        request.setAttribute("selectedDate", params.date);
        request.setAttribute("searchKeyword", params.searchKeyword);
        request.setAttribute("sortBy", params.sortBy);
        
        // 사용 가능한 감정 목록
        request.setAttribute("availableEmotions", 
            List.of("행복", "슬픔", "분노", "불안", "피로", "평온", "복잡"));
        
        // 통계 정보
        request.setAttribute("totalDiaryCount", diaryDAO.getTotalDiaryCount());
        request.setAttribute("recentDiaryCount", diaryDAO.getDiaryCountForLastDays(7));
    }
    
    /**
     * 오류 처리
     */
    private void handleError(HttpServletRequest request, HttpServletResponse response, 
                           String errorMessage) throws ServletException, IOException {
        request.setAttribute("error", true);
        request.setAttribute("errorMessage", errorMessage);
        request.setAttribute("diaryList", List.of()); // 빈 목록
        request.getRequestDispatcher("list.jsp").forward(request, response);
    }    
    @Override
    public void destroy() {
        logger.info("ListServlet destroyed");
        super.destroy();
    }
    
    /**
     * 필터링 파라미터를 담는 내부 클래스
     */
    private static class FilterParams {
        String emotion;
        String date;
        String searchKeyword;
        int page = 1;
        int pageSize = DEFAULT_PAGE_SIZE;
        String sortBy = "date_desc";
    }
    
    /**
     * 페이징 결과를 담는 내부 클래스
     */
    private static class PaginationResult {
        List<Diary> diaries;
        int totalItems;
        int totalPages;
        int currentPage;
        int pageSize;
        boolean hasPrevious;
        boolean hasNext;
        int previousPage;
        int nextPage;
    }
}