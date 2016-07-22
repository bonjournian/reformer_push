package cn.com.reformer.web.util;

import org.apache.log4j.Logger;

import cn.jpush.api.JPushClient;
import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.PushPayload;

/**
 * JPush工具类
 * @author cent
 *
 */
public class JPush {
	private static final Logger logger = Logger.getLogger(JPush.class);
	
	public static int sendPush(String masterSecret, String appKey, PushPayload payload) {
		JPushClient jpushClient = new JPushClient(masterSecret, appKey);
		try {
			logger.info("sendPush <masterSecret-appKey-payLoad>: " + masterSecret + "-" + appKey + "-" 
					+ payload.toString());
			PushResult result = jpushClient.sendPush(payload);
			logger.info("sendPush PushResult:" + result);
			if(result.isResultOK()) {
				return 0;
			} else {
				return 1;
			}
		} catch(APIConnectionException e) {
			logger.error("sendPush connection e·rror, Should retry later, exception: " + e.getMessage());
        } catch(APIRequestException e1) {
        	logger.error("sendPush error response from JPush server, Should review and fix it, exception: " 
        			+ e1.getMessage());
        	logger.error("HTTP Status: " + e1.getStatus());
        	logger.error("Error Code: " + e1.getErrorCode());
        	logger.error("Error Message: " + e1.getErrorMessage());
        	logger.error("Msg ID: " + e1.getMsgId());
        } catch(Exception e2) {
        	logger.error("sendPush <masterSecret-appKey-payLoad> exception: " + "<" + masterSecret + "-" + appKey + "-" 
					+ payload.toString() + "> " + e2.getMessage());
        }
		return 1;
	}
}
