package cn.com.reformer.web.bean;

public class Msg {
	private String content;
	private String title;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Msg(String content, String title) {
		super();
		this.content = content;
		this.title = title;
	}

	public Msg() {
	}

}
