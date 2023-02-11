package com.libanto.net.bankaccounts.resp;

import java.time.LocalDate;

import lombok.ToString;

@ToString
public class AccountResp {
	private int customerId;
	public int getCustomerId() {
		return customerId;
	}
	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}
	public long getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(long accountNumber) {
		this.accountNumber = accountNumber;
	}
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public String getBranchAddress() {
		return branchAddress;
	}
	public void setBranchAddress(String branchAddress) {
		this.branchAddress = branchAddress;
	}
	public LocalDate getCreateDt() {
		return createDt;
	}
	public void setCreateDt(LocalDate createDt) {
		this.createDt = createDt;
	}
	private long accountNumber;
	private String accountType;
	private String branchAddress;
	private LocalDate createDt;
}
