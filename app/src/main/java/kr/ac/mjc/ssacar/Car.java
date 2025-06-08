package kr.ac.mjc.ssacar;

import android.os.Parcel;
import android.os.Parcelable;

public class Car implements Parcelable {
    private String name;
    private String price;
    private int imageResId;
    private String imageUrl;
    private String engineType;
    private String fuelEfficiency;
    private String carCode;

    public Car(String name, String price, int imageResId) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
        this.imageUrl = "";
        this.engineType = "정보없음";
        this.fuelEfficiency = "정보없음";
        this.carCode = "";
    }

    public Car(String name, String price, String imageUrl, String engineType, String fuelEfficiency, String carCode) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.engineType = engineType;
        this.fuelEfficiency = fuelEfficiency;
        this.carCode = carCode;
        this.imageResId = 0;
    }

    public Car(String name, String price, int imageResId, String imageUrl, String engineType, String fuelEfficiency, String carCode) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
        this.imageUrl = imageUrl;
        this.engineType = engineType;
        this.fuelEfficiency = fuelEfficiency;
        this.carCode = carCode;
    }

    protected Car(Parcel in) {
        name = in.readString();
        price = in.readString();
        imageResId = in.readInt();
        imageUrl = in.readString();
        engineType = in.readString();
        fuelEfficiency = in.readString();
        carCode = in.readString();
    }

    public static final Creator<Car> CREATOR = new Creator<Car>() {
        @Override
        public Car createFromParcel(Parcel in) {
            return new Car(in);
        }

        @Override
        public Car[] newArray(int size) {
            return new Car[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeString(price);
        parcel.writeInt(imageResId);
        parcel.writeString(imageUrl);
        parcel.writeString(engineType);
        parcel.writeString(fuelEfficiency);
        parcel.writeString(carCode);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getName() { return name; }
    public String getPrice() { return price; }
    public int getImageResId() { return imageResId; }
    public String getImageUrl() { return imageUrl; }
    public String getEngineType() { return engineType; }
    public String getFuelEfficiency() { return fuelEfficiency; }
    public String getCarCode() { return carCode; }

    public void setName(String name) { this.name = name; }
    public void setPrice(String price) { this.price = price; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setEngineType(String engineType) { this.engineType = engineType; }
    public void setFuelEfficiency(String fuelEfficiency) { this.fuelEfficiency = fuelEfficiency; }
    public void setCarCode(String carCode) { this.carCode = carCode; }

    public boolean hasOnlineImage() {
        return imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("http");
    }
}
