package kr.ac.mjc.ssacar;

import android.os.Parcel;
import android.os.Parcelable;

public class PaymentCard implements Parcelable {
    private String cardId;
    private String cardType;
    private String cardNumber;
    private String maskedCardNumber;
    private String cardholderName;
    private String expiryDate;
    private boolean isDefault;
    private long registrationTime;

    public PaymentCard() {
        this.registrationTime = System.currentTimeMillis();
        this.isDefault = false;
    }

    public PaymentCard(String cardType, String cardNumber, String cardholderName, String expiryDate) {
        this();
        this.cardType = cardType;
        this.cardNumber = cardNumber;
        this.cardholderName = cardholderName;
        this.expiryDate = expiryDate;
        this.maskedCardNumber = maskCardNumber(cardNumber);
        this.cardId = generateCardId();
    }

    protected PaymentCard(Parcel in) {
        cardId = in.readString();
        cardType = in.readString();
        cardNumber = in.readString();
        maskedCardNumber = in.readString();
        cardholderName = in.readString();
        expiryDate = in.readString();
        isDefault = in.readByte() != 0;
        registrationTime = in.readLong();
    }

    public static final Creator<PaymentCard> CREATOR = new Creator<PaymentCard>() {
        @Override
        public PaymentCard createFromParcel(Parcel in) {
            return new PaymentCard(in);
        }

        @Override
        public PaymentCard[] newArray(int size) {
            return new PaymentCard[size];
        }
    };

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "**** **** **** ****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    private String generateCardId() {
        return "CARD_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    public int getCardIconResource() {
        switch (cardType.toUpperCase()) {
            case "VISA":
                return R.drawable.ic_visa;
            case "MASTERCARD":
                return R.drawable.ic_mastercard;
            case "AMERICAN EXPRESS":
            case "AMEX":
                return R.drawable.ic_amex;
            default:
                return R.drawable.ic_card_default;
        }
    }

    public boolean isExpired() {
        try {
            if (expiryDate == null || expiryDate.length() != 5) return true;
            String[] parts = expiryDate.split("/");
            if (parts.length != 2) return true;

            int expMonth = Integer.parseInt(parts[0]);
            int expYear = 2000 + Integer.parseInt(parts[1]);

            java.util.Calendar now = java.util.Calendar.getInstance();
            java.util.Calendar expiry = java.util.Calendar.getInstance();
            expiry.set(expYear, expMonth - 1, 1);
            expiry.set(java.util.Calendar.DAY_OF_MONTH, expiry.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));

            return now.after(expiry);
        } catch (Exception e) {
            return true;
        }
    }

    // Getters and Setters
    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }
    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        this.maskedCardNumber = maskCardNumber(cardNumber);
    }
    public String getMaskedCardNumber() { return maskedCardNumber; }
    public String getCardholderName() { return cardholderName; }
    public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
    public long getRegistrationTime() { return registrationTime; }
    public void setRegistrationTime(long registrationTime) { this.registrationTime = registrationTime; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cardId);
        dest.writeString(cardType);
        dest.writeString(cardNumber);
        dest.writeString(maskedCardNumber);
        dest.writeString(cardholderName);
        dest.writeString(expiryDate);
        dest.writeByte((byte) (isDefault ? 1 : 0));
        dest.writeLong(registrationTime);
    }

    @Override
    public String toString() {
        return "PaymentCard{" +
                "cardId='" + cardId + '\'' +
                ", cardType='" + cardType + '\'' +
                ", maskedCardNumber='" + maskedCardNumber + '\'' +
                ", cardholderName='" + cardholderName + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", isDefault=" + isDefault +
                ", isExpired=" + isExpired() +
                '}';
    }
}
