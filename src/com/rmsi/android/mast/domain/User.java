package com.rmsi.android.mast.domain;

public class User 
{
//UserId INTEGER PRIMARY KEY AUTOINCREMENT, UserName TEXT, Password TEXT
	private Long userId; 

	private String userName; 

	private String password;
	
	private String newPass;
	
	private String cnfrmPass;
	
	private String roleName;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long UserId) {
		this.userId = UserId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String UserName) {
		this.userName = UserName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String Password) {
		this.password = Password;
	}

	public String getNewPass() {
		return newPass;
	}

	public void setNewPass(String newPass) {
		this.newPass = newPass;
	}

	public String getCnfrmPass() {
		return cnfrmPass;
	}

	public void setCnfrmPass(String cnfrmPass) {
		this.cnfrmPass = cnfrmPass;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}	
}
