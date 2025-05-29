package kr.ac.mjc.ssacar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CarInfo implements Serializable {
    private String carName;
    private String carType;
    private String price;
    private String rating;
    private int imageResource;
    private List<ReservationInfo> reservations; // 예약 정보 리스트

    public CarInfo(String carName, String carType, String price, String rating, int imageResource) {
        this.carName = carName;
        this.carType = carType;
        this.price = price;
        this.rating = rating;
        this.imageResource = imageResource;
        this.reservations = new ArrayList<>();
    }

    // 예약 정보 클래스
    public static class ReservationInfo {
        private String startTime;
        private String endTime;

        public ReservationInfo(String startTime, String endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
    }

    // 특정 시간대에 예약 가능한지 확인
    public boolean isAvailable(String requestStartTime, String requestEndTime) {
        // 간단한 문자열 비교로 시간 충돌 확인
        // 실제로는 더 정교한 시간 비교가 필요
        for (ReservationInfo reservation : reservations) {
            if (isTimeOverlap(requestStartTime, requestEndTime,
                    reservation.getStartTime(), reservation.getEndTime())) {
                return false;
            }
        }
        return true;
    }

    // 시간 겹침 확인 (간단한 문자열 비교)
    private boolean isTimeOverlap(String start1, String end1, String start2, String end2) {
        // 문자열 비교로 간단히 구현 (실제로는 Date 객체 사용 권장)
        return !(end1.compareTo(start2) <= 0 || start1.compareTo(end2) >= 0);
    }

    // 예약 추가
    public void addReservation(String startTime, String endTime) {
        reservations.add(new ReservationInfo(startTime, endTime));
    }

    // Getters
    public String getCarName() {
        return carName;
    }

    public String getCarType() {
        return carType;
    }

    public String getPrice() {
        return price;
    }

    public String getRating() {
        return rating;
    }

    public int getImageResource() {
        return imageResource;
    }

    public List<ReservationInfo> getReservations() {
        return reservations;
    }

    // Setters
    public void setCarName(String carName) {
        this.carName = carName;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }
}