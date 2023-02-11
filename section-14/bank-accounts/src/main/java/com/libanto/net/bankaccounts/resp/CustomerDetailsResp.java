package com.libanto.net.bankaccounts.resp;

import java.util.List;

import com.libanto.net.bankaccounts.entity.Account;

public class CustomerDetailsResp {
	private Account account;
	private List<LoanResp> loans;
	private List<CardResp> cards;
	
	public Account getAccount() {
		return account;
	}
	public void setAccount(Account account) {
		this.account = account;
	}
	public List<LoanResp> getLoans() {
		return loans;
	}
	public void setLoans(List<LoanResp> loans) {
		this.loans = loans;
	}
	public List<CardResp> getCards() {
		return cards;
	}
	public void setCards(List<CardResp> cards) {
		this.cards = cards;
	}
}
