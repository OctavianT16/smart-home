package com.smartHome.backend.dehumidifier;

import java.util.List;

public class TuyaDeviceStatusResponse {
    private Boolean success;
    private Long t;
    private List<StatusItem> result;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Long getT() {
        return t;
    }

    public void setT(Long t) {
        this.t = t;
    }

    public List<StatusItem> getResult() {
        return result;
    }

    public void setResult(List<StatusItem> result) {
        this.result = result;
    }

    public static class StatusItem {
        private String code;
        private Object value;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}
