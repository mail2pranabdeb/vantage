package com.pd.common.core.controller;

import com.pd.common.core.domain.AjaxResult;

public class BaseController {

    protected AjaxResult toAjax(int rows) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    protected AjaxResult toAjax(boolean result) {
        return result ? success() : error();
    }

    public AjaxResult success() {
        return AjaxResult.success();
    }

    public AjaxResult error() {
        return AjaxResult.error();
    }

    public AjaxResult success(String message) {
        return AjaxResult.success(message);
    }

    public AjaxResult success(Object data) {
        return AjaxResult.success(data);
    }

    public AjaxResult success(String message, Object data) {
        return AjaxResult.success(message, data);
    }

    public AjaxResult error(String message) {
        return AjaxResult.error(message);
    }

    public AjaxResult error(int code, String message) {
        return AjaxResult.error(code, message);
    }
}
