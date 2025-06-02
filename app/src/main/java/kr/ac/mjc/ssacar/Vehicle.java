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

    public Vehicle(String name, String price, String fuelEfficiency, String engineType, String imageUrl, String carCode) {
        this.name = name;
        this.price = price;
        this.fuelEfficiency = fuelEfficiency;
        this.engineType = engineType;
        this.imageUrl = imageUrl;
        this.carCode = carCode;
    }

    // Parcelable 구현
    protected Vehicle(Parcel in) {
        name = in.readString();
        price = in.readString();
        fuelEfficiency = in.readString();
        engineType = in.readString();
        imageUrl = in.readString();
        carCode = in.readString();
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
}