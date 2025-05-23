package kr.ac.mjc.ssacar;

public class Car {
    private String name;
    private String price;
    private int imageResId;

    public Car(String name, String price, int imageResId) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
    }

    public String getName() { return name; }
    public String getPrice() { return price; }
    public int getImageResId() { return imageResId; }
}