package kr.ac.mjc.ssacar;

import android.os.Parcel;
import android.os.Parcelable;

public class Vehicle implements Parcelable {
    private String name;
    private String price;
    private String fuelEfficiency;
    private String engineType;
    private String imageUrl;
    private String carCode;
    private int localImageResource; // 로컬 이미지 리소스 ID 추가

    // 온라인 이미지용 생성자 (기존)
    public Vehicle(String name, String price, String fuelEfficiency, String engineType, String imageUrl, String carCode) {
        this.name = name;
        this.price = price;
        this.fuelEfficiency = fuelEfficiency;
        this.engineType = engineType;
        this.imageUrl = imageUrl;
        this.carCode = carCode;
        this.localImageResource = 0; // 로컬 이미지 없음
    }

    // 로컬 이미지용 생성자 (새로 추가)
    public Vehicle(String name, String price, String fuelEfficiency, String engineType, int localImageResource, String carCode) {
        this.name = name;
        this.price = price;
        this.fuelEfficiency = fuelEfficiency;
        this.engineType = engineType;
        this.imageUrl = ""; // 온라인 이미지 없음
        this.carCode = carCode;
        this.localImageResource = localImageResource;
    }

    // Parcelable 구현
    protected Vehicle(Parcel in) {
        name = in.readString();
        price = in.readString();
        fuelEfficiency = in.readString();
        engineType = in.readString();
        imageUrl = in.readString();
        carCode = in.readString();
        localImageResource = in.readInt(); // 로컬 이미지 리소스 ID 읽기
    }

    public static final Creator<Vehicle> CREATOR = new Creator<Vehicle>() {
        @Override
        public Vehicle createFromParcel(Parcel in) {
            return new Vehicle(in);
        }

        @Override
        public Vehicle[] newArray(int size) {
            return new Vehicle[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(price);
        dest.writeString(fuelEfficiency);
        dest.writeString(engineType);
        dest.writeString(imageUrl);
        dest.writeString(carCode);
        dest.writeInt(localImageResource); // 로컬 이미지 리소스 ID 쓰기
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getFuelEfficiency() {
        return fuelEfficiency;
    }

    public void setFuelEfficiency(String fuelEfficiency) {
        this.fuelEfficiency = fuelEfficiency;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCarCode() {
        return carCode;
    }

    public void setCarCode(String carCode) {
        this.carCode = carCode;
    }

    // 로컬 이미지 관련 메서드들 추가
    public int getLocalImageResource() {
        return localImageResource;
    }

    public void setLocalImageResource(int localImageResource) {
        this.localImageResource = localImageResource;
    }

    public boolean hasLocalImage() {
        return localImageResource != 0;
    }
}