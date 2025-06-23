package api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.*;
import java.util.stream.Collectors;

public class MindAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(MindAnalyzer.class);
    
    // 감정별 키워드 사전
    private static final Map<String, List<String>> EMOTION_KEYWORDS = new HashMap<>();
    
    // 감정 강도 가중치
    private static final Map<String, Integer> EMOTION_WEIGHTS = new HashMap<>();
    
    static {
        // 긍정적 감정 키워드
        EMOTION_KEYWORDS.put("행복", Arrays.asList(
            "기쁘다", "즐겁다", "좋다", "신나다", "만족", "흐뭇", "뿌듯", "감사", "사랑", 
            "웃음", "행복", "기분좋", "최고", "완벽", "환상적", "멋지다", "훌륭", "성공",
            "축하", "기뻐", "희망", "꿈", "이루다", "달성", "승리", "성과", "보람"
        ));
        
        EMOTION_KEYWORDS.put("평온", Arrays.asList(
            "편안", "고요", "차분", "안정", "평화", "조용", "여유", "휴식", "쉬다",
            "편하다", "마음의평화", "힐링", "안락", "느긋", "여유롭", "담담", "평범",
            "무난", "그저그런", "보통", "괜찮", "나쁘지않", "적당"
        ));        
        // 부정적 감정 키워드
        EMOTION_KEYWORDS.put("슬픔", Arrays.asList(
            "슬프다", "눈물", "우울", "외롭다", "그립다", "아쉽다", "허무", "공허",
            "울다", "비참", "절망", "좌절", "실망", "상처", "아프다", "힘들다",
            "괴롭다", "답답", "막막", "암울", "처참", "비극", "불행"
        ));
        
        EMOTION_KEYWORDS.put("분노", Arrays.asList(
            "화나다", "짜증", "분노", "열받다", "빡치다", "미치겠다", "싫다", "증오",
            "역겹다", "무시", "배신", "화가나", "약오르다", "속상", "격분", "분개",
            "원망", "한탄", "억울", "뜯어말", "복수", "적대감", "불만"
        ));
        
        EMOTION_KEYWORDS.put("불안", Arrays.asList(
            "걱정", "불안", "두렵다", "무섭다", "염려", "조마조마", "떨리다", "긴장",
            "스트레스", "압박", "부담", "고민", "망설", "의심", "확신없", "미래걱정",
            "시험", "면접", "발표", "실패", "실수", "위험", "공포", "당황"
        ));
        
        EMOTION_KEYWORDS.put("피로", Arrays.asList(
            "피곤", "지치다", "힘들다", "번아웃", "탈진", "지겹다", "귀찮다", "무기력",
            "의욕없", "에너지없", "포기", "그만", "쉬고싶", "자고싶", "놓고싶",
            "벗어나고싶", "도망", "회피", "미루다", "늦잠"
        ));
        
        // 복합 감정
        EMOTION_KEYWORDS.put("복잡", Arrays.asList(
            "복잡", "애매", "모호", "헷갈", "갈등", "딜레마", "혼란", "양가감정",
            "미묘", "어정쩡", "어중간", "이상", "묘한", "뭔가", "말로표현안됨"
        ));        
        // 감정 가중치 설정 (강한 감정일수록 높은 점수)
        EMOTION_WEIGHTS.put("행복", 85);
        EMOTION_WEIGHTS.put("분노", 80);
        EMOTION_WEIGHTS.put("슬픔", 75);
        EMOTION_WEIGHTS.put("불안", 70);
        EMOTION_WEIGHTS.put("피로", 60);
        EMOTION_WEIGHTS.put("평온", 50);
        EMOTION_WEIGHTS.put("복잡", 40);
    }
    
    /**
     * 일기 텍스트를 분석하여 감정을 판단합니다.
     * @param content 일기 내용
     * @return 감정 분석 결과 문자열
     */
    public String analyzeEmotion(String content) {
        if (content == null || content.trim().isEmpty()) {
            logger.warn("Empty content provided for emotion analysis");
            return "중립";
        }
        
        String cleanContent = preprocessText(content);
        Map<String, Double> emotionScores = calculateEmotionScores(cleanContent);
        
        logger.info("Emotion analysis completed for content length: {}", content.length());
        logger.debug("Emotion scores: {}", emotionScores);
        
        return determineMainEmotion(emotionScores);
    }
    
    /**
     * 텍스트 전처리 (소문자 변환, 특수문자 제거 등)
     */
    private String preprocessText(String text) {
        return text.toLowerCase()
                  .replaceAll("[^가-힣a-zA-Z0-9\\s]", " ")
                  .replaceAll("\\s+", " ")
                  .trim();
    }    
    /**
     * 각 감정에 대한 점수를 계산합니다.
     */
    private Map<String, Double> calculateEmotionScores(String content) {
        Map<String, Double> scores = new HashMap<>();
        
        for (Map.Entry<String, List<String>> entry : EMOTION_KEYWORDS.entrySet()) {
            String emotion = entry.getKey();
            List<String> keywords = entry.getValue();
            
            double score = calculateEmotionScore(content, keywords, emotion);
            scores.put(emotion, score);
        }
        
        return scores;
    }
    
    /**
     * 특정 감정에 대한 점수를 계산합니다.
     */
    private double calculateEmotionScore(String content, List<String> keywords, String emotion) {
        double totalScore = 0;
        int matchCount = 0;
        
        for (String keyword : keywords) {
            // 정확한 매칭
            if (content.contains(keyword)) {
                totalScore += 10;
                matchCount++;
                continue;
            }
            
            // 유사도 기반 매칭 (레벤스타인 거리 사용)
            String[] words = content.split("\\s+");
            for (String word : words) {
                if (word.length() >= 2) {
                    double similarity = calculateSimilarity(keyword, word);
                    if (similarity > 0.7) { // 70% 이상 유사하면 부분 점수 부여
                        totalScore += 5 * similarity;
                        matchCount++;
                    }
                }
            }
        }
        
        // 기본 가중치 적용
        Integer weight = EMOTION_WEIGHTS.get(emotion);
        if (weight != null && matchCount > 0) {
            totalScore = totalScore * (weight / 100.0);
        }
        
        // 텍스트 길이 대비 정규화
        return (totalScore / Math.max(content.length() / 10.0, 1)) + (matchCount * 2);
    }    
    /**
     * 두 단어의 유사도를 계산합니다.
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        
        LevenshteinDistance ld = new LevenshteinDistance();
        int distance = ld.apply(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        
        return 1.0 - ((double) distance / maxLength);
    }
    
    /**
     * 감정 점수를 기반으로 주요 감정을 결정합니다.
     */
    private String determineMainEmotion(Map<String, Double> emotionScores) {
        // 가장 높은 점수의 감정 찾기
        String mainEmotion = emotionScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("중립");
        
        double maxScore = emotionScores.get(mainEmotion);
        
        // 점수가 너무 낮으면 중립으로 판단
        if (maxScore < 1.0) {
            return "중립";
        }
        
        // 복합 감정 판단 (두 감정의 점수가 비슷한 경우)
        List<Map.Entry<String, Double>> sortedEmotions = emotionScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        if (sortedEmotions.size() >= 2) {
            double firstScore = sortedEmotions.get(0).getValue();
            double secondScore = sortedEmotions.get(1).getValue();
            
            // 1위와 2위 점수 차이가 20% 이하이면 복합 감정으로 판단
            if (firstScore > 0 && (firstScore - secondScore) / firstScore < 0.2) {
                String first = sortedEmotions.get(0).getKey();
                String second = sortedEmotions.get(1).getKey();
                return first + "+" + second;
            }
        }
        
        return mainEmotion;
    }    
    /**
     * 감정 분석 결과에 대한 상세 정보를 제공합니다.
     */
    public Map<String, Object> analyzeEmotionDetailed(String content) {
        Map<String, Object> result = new HashMap<>();
        
        if (content == null || content.trim().isEmpty()) {
            result.put("emotion", "중립");
            result.put("confidence", 0.0);
            result.put("scores", new HashMap<>());
            return result;
        }
        
        String cleanContent = preprocessText(content);
        Map<String, Double> emotionScores = calculateEmotionScores(cleanContent);
        String mainEmotion = determineMainEmotion(emotionScores);
        
        // 신뢰도 계산 (최고 점수를 기준으로)
        double maxScore = emotionScores.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double confidence = Math.min(maxScore / 10.0, 1.0); // 0~1 사이로 정규화
        
        result.put("emotion", mainEmotion);
        result.put("confidence", confidence);
        result.put("scores", emotionScores);
        result.put("wordCount", content.split("\\s+").length);
        result.put("analyzedAt", new Date());
        
        logger.info("Detailed emotion analysis: emotion={}, confidence={}", mainEmotion, confidence);
        
        return result;
    }    
    /**
     * 감정에 따른 응답 메시지를 생성합니다.
     */
    public String generateEmotionResponse(String emotion) {
        Map<String, List<String>> responses = new HashMap<>();
        
        responses.put("행복", Arrays.asList(
            "정말 좋은 하루를 보내셨네요! 이런 순간들이 더 많았으면 좋겠어요.",
            "긍정적인 에너지가 느껴져요. 이 기분을 오래 간직하세요!",
            "행복한 마음이 전해져 와요. 주변 사람들과도 이 기쁨을 나누시길!"
        ));
        
        responses.put("슬픔", Arrays.asList(
            "힘든 시간을 보내고 계시는군요. 괜찮으니까 충분히 슬퍼하세요.",
            "모든 감정에는 의미가 있어요. 지금의 슬픔도 언젠가는 소중한 경험이 될 거예요.",
            "슬플 때는 혼자 견디지 마시고 주변 사람들에게 도움을 요청하세요."
        ));
        
        responses.put("분노", Arrays.asList(
            "화가 나는 상황이었군요. 깊게 숨을 들이마시고 차분히 정리해보세요.",
            "분노는 자연스러운 감정이에요. 건설적인 방향으로 그 에너지를 사용해보세요.",
            "때로는 분노가 변화의 시작점이 되기도 해요. 무엇을 바꾸고 싶으신가요?"
        ));
        
        responses.put("불안", Arrays.asList(
            "걱정이 많으신 것 같네요. 호흡을 천천히 하며 현재에 집중해보세요.",
            "불안할 때는 구체적으로 무엇이 걱정되는지 적어보시면 도움이 돼요.",
            "모든 일이 계획대로 되지 않아도 괜찮아요. 하나씩 차근차근 해결해 나가세요."
        ));
        
        responses.put("피로", Arrays.asList(
            "많이 지치셨군요. 충분한 휴식을 취하시고 자신을 돌봐주세요.",
            "때로는 멈춤도 필요해요. 무리하지 마시고 재충전 시간을 가지세요.",
            "피로는 몸과 마음이 보내는 신호예요. 페이스를 조절해보시는 건 어떨까요?"
        ));
        
        responses.put("평온", Arrays.asList(
            "마음이 평온하신 것 같아 다행이에요. 이런 안정감을 유지하시길!",
            "고요한 마음 상태가 느껴져요. 내면의 평화가 소중해요.",
            "균형 잡힌 하루를 보내신 것 같네요. 꾸준히 이런 일상을 만들어가세요."
        ));
        
        List<String> emotionResponses = responses.getOrDefault(emotion.split("\\+")[0], 
            Arrays.asList("오늘도 일기를 써주셨네요. 당신의 감정을 들여다보는 시간이 되었기를 바라요."));
        
        Random random = new Random();
        return emotionResponses.get(random.nextInt(emotionResponses.size()));
    }
    
    /**
     * 시스템 상태를 확인합니다.
     */
    public boolean isHealthy() {
        try {
            String testContent = "오늘은 정말 좋은 하루였어요.";
            String result = analyzeEmotion(testContent);
            return result != null && !result.isEmpty();
        } catch (Exception e) {
            logger.error("Health check failed", e);
            return false;
        }
    }
}