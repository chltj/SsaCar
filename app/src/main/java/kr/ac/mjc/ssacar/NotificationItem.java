package kr.ac.mjc.ssacar;

public class NotificationItem {
    private String title;
    private String message;
    private long timestamp;
    private String userId; // 알림 수신 대상 사용자

    public NotificationItem(String title, String message, long timestamp) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }

    public String getUserId() { return  userId;}
}
