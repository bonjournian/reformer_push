package cn.com.reformer.web.interceptor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

public class MyTest {
	private static Properties props = new Properties();

	@Test
	public void test() {
		InputStream inputStream = null;
		try {
			inputStream = getClass().getResourceAsStream("/configs/jPush.properties");
			props.load(inputStream);
			System.out.println(props.getProperty("Host"));
			System.out.println(props.getProperty("AppKey"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}
}
