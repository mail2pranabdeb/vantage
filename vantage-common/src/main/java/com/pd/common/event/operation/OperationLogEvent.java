package com.pd.common.event.operation;

import com.pd.common.event.DomainEvent;

/**
 * Event published when a user performs an operation.
 * Listeners can use this to record operation logs in sys_oper_log.
 */
public class OperationLogEvent extends DomainEvent {

    private final String title;
    private final Integer businessType;
    private final String method;
    private final String requestMethod;
    private final Integer operatorType;
    private final String operName;
    private final String deptName;
    private final String operUrl;
    private final String operIp;
    private final String operLocation;
    private final String operParam;
    private final String jsonResult;
    private final Integer status;
    private final String errorMsg;
    private final Long costTime;

    public OperationLogEvent(String title, Integer businessType, String method, String requestMethod,
                             Integer operatorType, String operName, String deptName, String operUrl,
                             String operIp, String operLocation, String operParam, String jsonResult,
                             Integer status, String errorMsg, Long costTime) {
        super("OPERATION_LOG");
        this.title = title;
        this.businessType = businessType;
        this.method = method;
        this.requestMethod = requestMethod;
        this.operatorType = operatorType;
        this.operName = operName;
        this.deptName = deptName;
        this.operUrl = operUrl;
        this.operIp = operIp;
        this.operLocation = operLocation;
        this.operParam = operParam;
        this.jsonResult = jsonResult;
        this.status = status;
        this.errorMsg = errorMsg;
        this.costTime = costTime;
    }

    // Getters

    public String getTitle() {
        return title;
    }

    public Integer getBusinessType() {
        return businessType;
    }

    public String getMethod() {
        return method;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public Integer getOperatorType() {
        return operatorType;
    }

    public String getOperName() {
        return operName;
    }

    public String getDeptName() {
        return deptName;
    }

    public String getOperUrl() {
        return operUrl;
    }

    public String getOperIp() {
        return operIp;
    }

    public String getOperLocation() {
        return operLocation;
    }

    public String getOperParam() {
        return operParam;
    }

    public String getJsonResult() {
        return jsonResult;
    }

    public Integer getStatus() {
        return status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public Long getCostTime() {
        return costTime;
    }
}
