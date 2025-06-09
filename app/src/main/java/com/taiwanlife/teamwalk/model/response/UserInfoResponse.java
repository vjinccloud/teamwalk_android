package com.taiwanlife.teamwalk.model.response;

public class UserInfoResponse {

    private Header header;
    private Data data;

    public Header getHeader() {
        return header;
    }

    public Data getData() {
        return data;
    }

    public static class Header {
        private String code;
        private String message;

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class Data {
        private String id;
        private String pid;
        private int level;
        private int coins;
        private int exp;
        private int badge_amt;
        private String nickName;
        private String gender;
        private int height;
        private int weight;
        private String mobile;
        private String email;
        private String city;
        private String district;
        private String company_Id;
        private String company_TID;
        private String company_name;
        private String b2b;
        private String referrer_code;
        private boolean complete_onboarding;
        private boolean enable_notification;
        private String device_OS;
        private int free_game;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPid() {
            return pid;
        }

        public void setPid(String pid) {
            this.pid = pid;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public int getCoins() {
            return coins;
        }

        public void setCoins(int coins) {
            this.coins = coins;
        }

        public int getExp() {
            return exp;
        }

        public void setExp(int exp) {
            this.exp = exp;
        }

        public int getBadge_amt() {
            return badge_amt;
        }

        public void setBadge_amt(int badge_amt) {
            this.badge_amt = badge_amt;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getDistrict() {
            return district;
        }

        public void setDistrict(String district) {
            this.district = district;
        }

        public String getCompany_Id() {
            return company_Id;
        }

        public void setCompany_Id(String company_Id) {
            this.company_Id = company_Id;
        }

        public String getCompany_TID() {
            return company_TID;
        }

        public void setCompany_TID(String company_TID) {
            this.company_TID = company_TID;
        }

        public String getCompany_name() {
            return company_name;
        }

        public void setCompany_name(String company_name) {
            this.company_name = company_name;
        }

        public String getB2b() {
            return b2b;
        }

        public void setB2b(String b2b) {
            this.b2b = b2b;
        }

        public String getReferrer_code() {
            return referrer_code;
        }

        public void setReferrer_code(String referrer_code) {
            this.referrer_code = referrer_code;
        }

        public boolean isComplete_onboarding() {
            return complete_onboarding;
        }

        public void setComplete_onboarding(boolean complete_onboarding) {
            this.complete_onboarding = complete_onboarding;
        }

        public boolean isEnable_notification() {
            return enable_notification;
        }

        public void setEnable_notification(boolean enable_notification) {
            this.enable_notification = enable_notification;
        }

        public String getDevice_OS() {
            return device_OS;
        }

        public void setDevice_OS(String device_OS) {
            this.device_OS = device_OS;
        }

        public int getFree_game() {
            return free_game;
        }

        public void setFree_game(int free_game) {
            this.free_game = free_game;
        }
    }
}
