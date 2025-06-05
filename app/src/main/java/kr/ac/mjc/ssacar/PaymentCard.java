package kr.ac.mjc.ssacar;

public class PaymentCard {
    private long id;
    private String cardType;
    private String maskedCardNumber;
    private String cardholderName;
    private String expiryDate;
    private boolean isDefault;
    private long registrationTime;

    // 생성자
    public PaymentCard() {
        this.registrationTime = System.currentTimeMillis();
    }

    public PaymentCard(long id, String cardType, String maskedCardNumber,
                       String cardholderName, String expiryDate, boolean isDefault) {
        this.id = id;
        this.cardType = cardType;
        this.maskedCardNumber = maskedCardNumber;
        this.cardholderName = cardholderName;
        this.expiryDate = expiryDate;
        this.isDefault = isDefault;
        this.registrationTime = System.currentTimeMillis();
    }

    // Getter 메서드들
    public long getId() {
        return id;
    }

    public String getCardType() {
        return cardType;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public long getRegistrationTime() {
        return registrationTime;
    }

    // Setter 메서드들
    public void setId(long id) {
        this.id = id;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setRegistrationTime(long registrationTime) {
        this.registrationTime = registrationTime;
    }

    // 카드 타입에 따른 아이콘 리소스 ID 반환
    public int getCardIconResource() {
        switch (cardType.toLowerCase()) {
            case "visa":
                return kr.ac.mjc.ssacar.R.drawable.ic_visa;
            case "mastercard":
                return kr.ac.mjc.ssacar.R.drawable.ic_mastercard;
            case "american express":
            case "amex":
                return kr.ac.mjc.ssacar.R.drawable.ic_amex;
            default:
                return kr.ac.mjc.ssacar.R.drawable.ic_card_default;
        }
    }

    // 카드의 마지막 4자리만 반환
    public String getLastFourDigits() {
        if (maskedCardNumber != null && maskedCardNumber.length() >= 4) {
            return maskedCardNumber.substring(maskedCardNumber.length() - 4);
        }
        return "****";
    }

    @Override
    public String toString() {
        return "PaymentCard{" +
                "id=" + id +
                ", cardType='" + cardType + '\'' +
                ", maskedCardNumber='" + maskedCardNumber + '\'' +
                ", cardholderName='" + cardholderName + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", isDefault=" + isDefault +
                ", registrationTime=" + registrationTime +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PaymentCard that = (PaymentCard) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}