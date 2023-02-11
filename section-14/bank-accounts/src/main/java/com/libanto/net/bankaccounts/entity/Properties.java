package com.libanto.net.bankaccounts.entity;

import java.util.List;
import java.util.Map;

public class Properties {
	private String msg;
	private String buildVersion;
	private Map<String, String> mailDetails;
	private List<String> activeBranches;

	public Properties(String msg, String buildVersion, Map<String, String> mailDetails, List<String> activeBranches) {
		this.msg = msg;
		this.buildVersion = buildVersion;
		this.mailDetails = mailDetails;
		this.activeBranches = activeBranches;
	}

	public String getMsg() {
		return msg;
	}

	public String getBuildVersion() {
		return buildVersion;
	}

	public Map<String, String> getMailDetails() {
		return mailDetails;
	}

	public List<String> getActiveBranches() {
		return activeBranches;
	}

}
