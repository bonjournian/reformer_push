package cn.com.reformer.web.bean;

public class Audience {
	private String[] alias;

	public String[] getAlias() {
		return alias;
	}

	public void setAlias(String... alias) {
		this.alias = alias;
	}

	public Audience(String[] alias) {
		super();
		this.alias = alias;
	}

	public Audience() {

	}
}
