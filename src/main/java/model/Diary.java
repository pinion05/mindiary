package model;

public class Diary {
    private String content;
    private String emotionSummary;
    private String createdAt;

    public Diary(String content, String emotionSummary, String createdAt) {
        this.content = content;
        this.emotionSummary = emotionSummary;
        this.createdAt = createdAt;
    }

    public String getContent() {
        return content;
    }

    public String getEmotionSummary() {
        return emotionSummary;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
