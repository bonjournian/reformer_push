package cn.com.reformer.web.bean;

public class Notification {
	private String alert;
	private String title;

	public String getAlert() {
		return alert;
	}

	public void setAlert(String alert) {
		this.alert = alert;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Notification(String alert, String title) {
		super();
		this.alert = alert;
		this.title = title;
	}

	public Notification() {

	}
}
