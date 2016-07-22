package cn.com.reformer.web.interceptor;

import java.util.Timer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import cn.com.reformer.web.service.PushTask;



public class ThreadListener implements ServletContextListener {
	private Timer timer = new Timer();
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		if (timer!=null) {
			timer.cancel();
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		timer.scheduleAtFixedRate(new PushTask(), 1, 100);
	}

}
