package com.rlms.constants;

public enum RlmsErrorType {

	UNNKOWN_EXCEPTION_OCCHURS(1,"unknown_exception_occur"),
	ACCOUNT_CREATION_SUCCESS(2,"account_creation_success"),
	USER_ALREADY_REGISTERED(3,"user_already_registered"),
	CUSTOMER_ALREADY_ADDED(3,"customer_already_added"),
	PLEASE_PROVIDE_EMAILID(4,"please_provide_emailid"),
	BRANCH_CREATION_SUCCESSFUL(5,"branch_creation_successful"),
	BRANCH_ALREADY_EXISTS(6,"branch_already_exists"),
	ROLE_SUCCESSFULLY_ASSIGNED(7,"role_successfully_assigned"),
	ROLE_ALREADY_GIVEN(8,"role_already_given"),
	USER_REG_SUCCESFUL(9,"user_reg_succesful"),
	CUSTOMER_REG_SUCCESFUL(9,"customer_reg_succesful"),
	REGISTRATION_ID_INCORRECT(10,"registration_id_incorrect"),
	PUSH_NOTIFICATION_FAILED(11,"push_notification_failed"),
	INVALID_COMPANY_NAME(12,"invalid_company_name"),
	INVALID_BRANCH_NAME(12,"invalid_branch_name");
	
	private Integer code;
	private String message;
	private RlmsErrorType(int code, String message){
		this.code = code;
		this.message = message;
	}
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
