package kr.ac.mjc.ssacar;

import java.io.Serializable;

public class PaymentHistory implements Serializable {
    private String carName;
    private String engineType;
    private String placeName;
    private String address;
    private String departureTime;
    private String arrivalTime;
    private int totalPrice;
    private String paymentMethod;
    private String imageUrl;

    public PaymentHistory(String carName, String engineType, String placeName, String address,
                          String departureTime, String arrivalTime, int totalPrice, String paymentMethod, String imageUrl) {
        this.carName = carName;
        this.engineType = engineType;
        this.placeName = placeName;
        this.address = address;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.imageUrl = imageUrl;
    }

    // Getter들
    public String getCarName() { return carName; }
    public String getEngineType() { return engineType; }
    public String getPlaceName() { return placeName; }
    public String getAddress() { return address; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public int getTotalPrice() { return totalPrice; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getImageUrl() { return imageUrl; }
}
