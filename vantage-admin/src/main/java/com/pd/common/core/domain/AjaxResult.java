package com.pd.common.core.domain;

import java.util.HashMap;

public class AjaxResult extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public static final String CODE_TAG = "code";
    public static final String MSG_TAG = "msg";
    public static final String DATA_TAG = "data";

    public AjaxResult() {
    }

    public AjaxResult(int code, String msg) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
    }

    public AjaxResult(int code, String msg, Object data) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        if (data != null) {
            super.put(DATA_TAG, data);
        }
    }

    public static AjaxResult success() {
        return AjaxResult.success("Success");
    }

    public static AjaxResult success(Object data) {
        return AjaxResult.success("Success", data);
    }

    public static AjaxResult success(String msg) {
        return AjaxResult.success(msg, null);
    }

    public static AjaxResult success(String msg, Object data) {
        return new AjaxResult(200, msg, data);
    }

    public static AjaxResult error() {
        return AjaxResult.error("Error");
    }

    public static AjaxResult error(String msg) {
        return AjaxResult.error(msg, null);
    }

    public static AjaxResult error(String msg, Object data) {
        return new AjaxResult(500, msg, data);
    }

    public static AjaxResult error(int code, String msg) {
        return new AjaxResult(code, msg, null);
    }
}
