package cn.com.reformer.web.bean;

public class Message {
	private String[] platform;
	private Audience audience;
	private Msg message;

	public String[] getPlatform() {
		return platform;
	}

	public void setPlatform(String[] platform) {
		this.platform = platform;
	}

	public Audience getAudience() {
		return audience;
	}

	public void setAudience(Audience audience) {
		this.audience = audience;
	}

	public Msg getMessage() {
		return message;
	}

	public void setMessage(Msg message) {
		this.message = message;
	}

	public Message(String[] platform, Audience audience, Msg message) {
		super();
		this.platform = platform;
		this.audience = audience;
		this.message = message;
	}

	public Message() {

	}

}
