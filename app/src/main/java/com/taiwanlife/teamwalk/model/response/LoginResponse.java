package com.taiwanlife.teamwalk.model.response;

public class LoginResponse {

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
        private String token;

        public String getToken() {
            return token;
        }
    }
}

