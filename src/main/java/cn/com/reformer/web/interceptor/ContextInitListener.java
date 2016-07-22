package cn.com.reformer.web.interceptor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.junit.Test;

public class ContextInitListener implements ServletContextListener {

	private static Properties props = new Properties(); 

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
        InputStream inputStream = null; 
        try { 
        	//该地址读取正确
            inputStream = getClass().getResourceAsStream("/configs/jPush.properties"); 
            props.load(inputStream); 
            System.out.println(props.getProperty("AppKey")+ props.getProperty("Host") + props.getProperty("MasterSecret"));
        } catch (IOException ex) { 
            ex.printStackTrace(); 
        } 
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("Destroy()................");
	}
	
	public static String get(String key) {
		return props.getProperty(key);
	}
	
	@Test
	public void test() {
		System.out.println(get("AppKey"));
	}
	
	@Test
	public void printHost() {
		System.out.println(get("Host"));
	}
}
