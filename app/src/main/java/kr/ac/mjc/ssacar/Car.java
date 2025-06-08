    package kr.ac.mjc.ssacar;

    public class Car {
        private String name;
        private String price;
        private int imageResId;
        private String imageUrl;  // 온라인 이미지 URL
        private String engineType;
        private String fuelEfficiency;
        private String carCode;

        // 기존 생성자 (로컬 이미지용)
        public Car(String name, String price, int imageResId) {
            this.name = name;
            this.price = price;
            this.imageResId = imageResId;
            this.imageUrl = "";
            this.engineType = "정보없음";
            this.fuelEfficiency = "정보없음";
            this.carCode = "";
        }

        // 새로운 생성자 (API 데이터용)
        public Car(String name, String price, String imageUrl, String engineType, String fuelEfficiency, String carCode) {
            this.name = name;
            this.price = price;
            this.imageUrl = imageUrl;
            this.engineType = engineType;
            this.fuelEfficiency = fuelEfficiency;
            this.carCode = carCode;
            this.imageResId = 0; // 온라인 이미지 사용 시 0
        }

        // 완전한 생성자
        public Car(String name, String price, int imageResId, String imageUrl, String engineType, String fuelEfficiency, String carCode) {
            this.name = name;
            this.price = price;
            this.imageResId = imageResId;
            this.imageUrl = imageUrl;
            this.engineType = engineType;
            this.fuelEfficiency = fuelEfficiency;
            this.carCode = carCode;
        }

        // Getter 메서드들
        public String getName() { return name; }
        public String getPrice() { return price; }
        public int getImageResId() { return imageResId; }
        public String getImageUrl() { return imageUrl; }
        public String getEngineType() { return engineType; }
        public String getFuelEfficiency() { return fuelEfficiency; }
        public String getCarCode() { return carCode; }

        // Setter 메서드들
        public void setName(String name) { this.name = name; }
        public void setPrice(String price) { this.price = price; }
        public void setImageResId(int imageResId) { this.imageResId = imageResId; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public void setEngineType(String engineType) { this.engineType = engineType; }
        public void setFuelEfficiency(String fuelEfficiency) { this.fuelEfficiency = fuelEfficiency; }
        public void setCarCode(String carCode) { this.carCode = carCode; }

        // 온라인 이미지 사용 여부 확인
        public boolean hasOnlineImage() {
            return imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("http");
        }
    }