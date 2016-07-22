package cn.com.reformer.web.controller;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import cn.com.reformer.web.interceptor.ContextInitListener;
import cn.com.reformer.web.util.JPush;
import cn.com.reformer.web.util.JPushPayload;
import cn.jpush.api.push.model.PushPayload;
import redis.clients.jedis.Jedis;

@Controller
@RequestMapping("/push")
public class TestController {

	private static final Logger logger = Logger.getLogger(TestController.class);
	private static final String APPKEY = ContextInitListener.get("AppKey");
	private static final String MASTERSECRET = ContextInitListener.get("MasterSecret");
	private static final String HOST = ContextInitListener.get("Host");
	private static final int PORT = Integer.parseInt(ContextInitListener.get("Port"));
	// private static final int TIMEOUT =
	// Integer.parseInt(ContextInitListener.get("TimeOut"));
	// the start time
	private long sTime = System.currentTimeMillis();
	private Jedis jedis = null;
	private String jedisKey = null;
	private Lock cacheLock = null;
	// private TestController testController;

	@RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json;charset=UTF-8")
	@ResponseBody
	public String test(@RequestBody String reqBody, HttpServletRequest request, HttpServletResponse response) {
		// ------------Test if this method executes----------------
		System.out.println("==========/push/save===========");
		// --------------------------------------------------------
		// save information to redis cache and return the result to judge
		// whether save successfully
		String saveRes = saveToCache(reqBody);
		if (!("saveSuccess".equalsIgnoreCase(saveRes))) {
			// save again if the return value if failure at first time
			saveRes = saveToCache(reqBody);
		}
		return saveRes;
	}

	@RequestMapping(value = "/send", method = RequestMethod.POST)
	@ResponseBody
	public int pushInformation(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("==========/push/send===========");
		System.out.println(jedis.zcard(jedisKey));
		// result = 1 represent the failure
		int result = 1;
		// the time when someone visit this url
		long visitTime = System.currentTimeMillis();
		int timeInterval = (int) (visitTime - sTime);
		if (timeInterval >= 100) {
			// meeting the demand (when the timeInterval is more than 100ms),
			// send information
			// if the number of information less than 10
			if (jedis.zcard(jedisKey) < 10) {// 10
				result = sendNInfo(jedis.zcard(jedisKey).intValue());
			} else {
				// send 10 pieces of information
				result = sendNInfo(10);
			}
			// initialize the start time
			sTime = System.currentTimeMillis();
		}
		return result;
	}

	/**
	 * send n pieces of information and delete them
	 * 
	 * @param n
	 * @return
	 */
	private int sendNInfo(int n) {
		int result = 1;
		PushPayload payload = null;
		// judge if there are more than 10 pieces of information in this cache
		// jedis.zrem(key, members)
		// jedis.zscore(key, member) return a double value or null
		// send 10 pieces of information
		try {
			cacheLock.tryLock(100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.error("current thread inteerupted, exception: " + e.getMessage());
		}
		try {
			Set<String> infoJsonSet = jedis.zrange(jedisKey, 0, n);
			Iterator<String> jsonIterator = infoJsonSet.iterator();
			while (jsonIterator.hasNext()) {
				String singleInfo = jsonIterator.next();
				JSONObject singleJson = JSON.parseObject((singleInfo));
				String[] alias = null;
				// get the send object
				if (singleJson.containsKey("audience")) {
					String audienceStr = singleJson.getString("audience");
					JSONObject audienceJson = JSON.parseObject((audienceStr));
					if (audienceJson.containsKey("alias")) {
						// get all the alias
						String aliasArrStr = audienceJson.getString("alias");
						alias = aliasArrStr.split("\",\"");

						// ----------------------------------
						for (int i = 0; i < alias.length; i++) {
							System.out.print(alias[i] + ",");
						}
						// ----------------------------------
						System.out.print("----------------");

						alias[0] = alias[0].substring(2);
						alias[alias.length - 1] = alias[alias.length - 1].substring(0,
								alias[alias.length - 1].length() - 2);

						// ----------------------------------
						for (int i = 0; i < alias.length; i++) {
							if (i < alias.length - 1) {
								System.out.print(alias[i] + ",");
							} else {
								System.out.print(alias[i]);
							}
						}
						System.out.println();
						// ----------------------------------

					} else {
						logger.error("alias doesnot exist, delete corresponding infomation: " + singleInfo);
						// delete the corresponding record
						jedis.zrem(jedisKey, singleInfo);
						continue;
					}
					// judge the type of information: "message" or
					// "notification"
					if (singleJson.containsKey("notification")) {
						String notification = singleJson.getString("notification");
						String alert = null;
						JSONObject noticeJson = JSON.parseObject((notification));
						if (noticeJson.containsKey("android")) {
							String android = noticeJson.getString("android");
							JSONObject androidJson = JSON.parseObject((android));
							alert = androidJson.getString("alert");
						} else if (noticeJson.containsKey("ios")) {
							String ios = noticeJson.getString("ios");
							JSONObject androidJson = JSON.parseObject((ios));
							alert = androidJson.getString("alert");
						} else {
							logger.error("notification format error, delete corresponding infomation: " + singleInfo);
							jedis.zrem(jedisKey, singleInfo);
							continue;
						}
						payload = JPushPayload.buildPushObject_all_alias_alert(alert, alias);
						jedis.zrem(jedisKey, singleInfo);
						result = JPush.sendPush(MASTERSECRET, APPKEY, payload);
					} else if (singleJson.containsKey("message")) {
						String msg_content = null;
						String message = singleJson.getString("message");
						JSONObject messageJson = JSON.parseObject((message));
						if (messageJson.containsKey("msg_content")) {
							msg_content = messageJson.getString("msg_content");
						} else {
							logger.error("message format error, delete corresponding infomation: " + singleInfo);
							jedis.zrem(jedisKey, singleInfo);
							continue;
						}
						payload = JPushPayload.buildPushObject_ios_audienceMore_messageWithExtras(msg_content, alias);
						jedis.zrem(jedisKey, singleInfo);
						result = JPush.sendPush(MASTERSECRET, APPKEY, payload);
					}
				} else {
					logger.error("audience doesnot exist, delete corresponding infomation: " + singleInfo);
					jedis.zrem(jedisKey, singleInfo);
					continue;
				}
			}
		} finally {
			try {
				cacheLock.unlock();
			} catch (IllegalMonitorStateException e) {
				logger.warn("the lock is in use by " + Thread.currentThread() + "warn message: " + e.getMessage());
			}
		}
		return result;
	}

	/**
	 * 保存信息到缓存中
	 * 
	 * @param infoStr
	 *            消息内容
	 * @return 保存成功失败结果
	 */
	public String saveToCache(String infoStr) {
		String saveRes = null;
		JSONObject infoObj = null;
		// judge the request is json
		try {
			infoObj = JSON.parseObject((infoStr));
		} catch (JSONException e) {
			logger.warn("format of the request is not suitable, json format is required!");
			saveRes = "saveFailure";
			return saveRes;
		}
		// before saving, judge the format of the coming information, by
		// following three fields
		if ((infoObj.containsKey("notification") || infoObj.containsKey("message"))
				&& infoObj.containsKey("audience")) {
			if (infoObj.containsKey("priority")) {
				if ("0".equals(infoObj.getString("priority"))) {
					// 紧急Information，插队
					logger.info("an piece of emergency information comes, please be careful!");
					try {
						jedis.zadd(this.jedisKey, 0, infoObj.toJSONString());
						saveRes = "saveSuccess";
					} catch (Exception e) {
						saveRes = "saveFailure";
					}
				} else {
					try {
						saveRes = jedis.zadd(this.jedisKey, System.currentTimeMillis(), infoObj.toJSONString()) + "";
						saveRes = "saveSuccess";
					} catch (Exception e) {
						saveRes = "saveFailure";
					}
				}
				/*
				 * else if ("1".equals(obj.getString("priority"))) { //
				 * 非紧急Information，排队 }
				 */
			} else {
				// 有非0的标记或者没有标记，表示非紧急Information，按照消息来到的时间戳排队即可
				try {
					saveRes = jedis.zadd(this.jedisKey, System.currentTimeMillis(), infoObj.toJSONString()) + "";
					saveRes = "saveSuccess";
				} catch (Exception e) {
					saveRes = "saveFailure";
				}
			}
		} else {
			logger.error("format of this information has some problems, check again!");
			saveRes = "saveFailure";
		}
		return saveRes;
	}

	/**
	 * 初始化控制器工作
	 */
	public TestController() {
		// base on the given host/port/timeout, the time is 1min
		jedis = new Jedis(HOST, PORT);
		jedisKey = "information";
		cacheLock = new ReentrantLock();
		// ------------Test if this method executes----------------
		System.out.println("-----Constructor111()-----");
		// --------------------------------------------------------
	}
}
