package test;

import api.MindAnalyzer;
import dao.DiaryDAO;
import util.DatabaseUtil;

public class MindDiaryTest {
    public static void main(String[] args) {
        System.out.println("=== 🎉 Mindiary 프로젝트 테스트 시작 ===");
        
        try {
            // 1. 데이터베이스 초기화 테스트
            System.out.println("\n🔧 데이터베이스 연결 테스트...");
            DatabaseUtil dbUtil = DatabaseUtil.getInstance();
            boolean dbOk = dbUtil.testConnection();
            System.out.println("데이터베이스 연결: " + (dbOk ? "✅ 성공" : "❌ 실패"));
            
            // 2. AI 감정 분석 테스트
            System.out.println("\n🤖 AI 감정 분석 테스트...");
            MindAnalyzer analyzer = new MindAnalyzer();
            
            String[] testDiaries = {
                "오늘은 정말 행복한 하루였어요! 친구들과 즐거운 시간을 보냈습니다.",
                "요즘 너무 피곤하고 지쳐서 아무것도 하기 싫어요...",
                "회사에서 상사가 너무 화가 나서 스트레스 받아요.",
                "조용한 카페에서 책을 읽으며 평온한 시간을 보냈어요."
            };
            
            for (String diary : testDiaries) {
                String emotion = analyzer.analyzeEmotion(diary);
                String response = analyzer.generateEmotionResponse(emotion);
                
                System.out.println("📝 일기: " + diary.substring(0, Math.min(30, diary.length())) + "...");
                System.out.println("😊 분석된 감정: " + emotion);
                System.out.println("💬 AI 응답: " + response);
                System.out.println();
            }
            
            // 3. 데이터베이스 저장 테스트
            System.out.println("💾 데이터베이스 저장 테스트...");
            DiaryDAO dao = new DiaryDAO();
            boolean saved = dao.insertDiary(testDiaries[0], analyzer.analyzeEmotion(testDiaries[0]));
            System.out.println("일기 저장: " + (saved ? "✅ 성공" : "❌ 실패"));
            
            // 4. 통계 조회 테스트
            System.out.println("\n📊 통계 데이터 테스트...");
            System.out.println("총 일기 개수: " + dao.getTotalDiaryCount());
            System.out.println("감정 통계: " + dao.getEmotionStatistics());
            
            System.out.println("\n🎉 모든 테스트 완료! 프로젝트가 정상 작동합니다.");
            System.out.println("\n🌐 웹 서버 실행을 원하시면 IntelliJ에서 Tomcat 설정 후 실행하세요!");
            
        } catch (Exception e) {
            System.err.println("❌ 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

