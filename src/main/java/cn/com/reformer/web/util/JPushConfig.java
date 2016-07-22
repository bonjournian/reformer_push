package cn.com.reformer.web.util;

import org.apache.log4j.Logger;

/**
 * JPush配置信息类
 * @author cent
 *
 */
public class JPushConfig {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(JPushConfig.class);
	private static JPushConfig jPushConfig = new JPushConfig();
	
	private JPushConfig() {
		
	}
	
	public static JPushConfig getInstance() {
		return jPushConfig;
	}
}
