package cn.com.reformer.web.bean;

public class Notice {
	private String[] platform;
	private Audience audience;
	private Notification notification;
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
	public Notification getNotification() {
		return notification;
	}
	public void setNotification(Notification notification) {
		this.notification = notification;
	}
	public Notice(String[] platform, Audience audience, Notification notification) {
		super();
		this.platform = platform;
		this.audience = audience;
		this.notification = notification;
	}
	public Notice() {
		
	}
	
	
}
