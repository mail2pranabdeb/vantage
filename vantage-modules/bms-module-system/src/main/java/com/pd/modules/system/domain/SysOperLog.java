package com.pd.modules.system.domain;

import java.time.LocalDateTime;

/**
 * System operation log entity - sys_oper_log
 */
public class SysOperLog {

    /** Operation ID */
    private Long operId;

    /** Module title */
    private String title;

    /** Business type (0 other, 1 insert, 2 update, 3 delete) */
    private Integer businessType;

    /** Method name */
    private String method;

    /** Request method */
    private String requestMethod;

    /** Operator type (0 other, 1 admin, 2 mobile) */
    private Integer operatorType;

    /** Operator name */
    private String operName;

    /** Department name */
    private String deptName;

    /** Request URL */
    private String operUrl;

    /** Operation IP */
    private String operIp;

    /** Operation location */
    private String operLocation;

    /** Request parameters */
    private String operParam;

    /** JSON result */
    private String jsonResult;

    /** Status (0 normal, 1 error) */
    private Integer status;

    /** Error message */
    private String errorMsg;

    /** Operation time */
    private LocalDateTime operTime;

    /** Cost time in milliseconds */
    private Long costTime;

    // Getters and Setters

    public Long getOperId() {
        return operId;
    }

    public void setOperId(Long operId) {
        this.operId = operId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getBusinessType() {
        return businessType;
    }

    public void setBusinessType(Integer businessType) {
        this.businessType = businessType;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public Integer getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(Integer operatorType) {
        this.operatorType = operatorType;
    }

    public String getOperName() {
        return operName;
    }

    public void setOperName(String operName) {
        this.operName = operName;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getOperUrl() {
        return operUrl;
    }

    public void setOperUrl(String operUrl) {
        this.operUrl = operUrl;
    }

    public String getOperIp() {
        return operIp;
    }

    public void setOperIp(String operIp) {
        this.operIp = operIp;
    }

    public String getOperLocation() {
        return operLocation;
    }

    public void setOperLocation(String operLocation) {
        this.operLocation = operLocation;
    }

    public String getOperParam() {
        return operParam;
    }

    public void setOperParam(String operParam) {
        this.operParam = operParam;
    }

    public String getJsonResult() {
        return jsonResult;
    }

    public void setJsonResult(String jsonResult) {
        this.jsonResult = jsonResult;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public LocalDateTime getOperTime() {
        return operTime;
    }

    public void setOperTime(LocalDateTime operTime) {
        this.operTime = operTime;
    }

    public Long getCostTime() {
        return costTime;
    }

    public void setCostTime(Long costTime) {
        this.costTime = costTime;
    }
}
