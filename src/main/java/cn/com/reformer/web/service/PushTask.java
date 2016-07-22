package cn.com.reformer.web.service;

import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.com.reformer.web.interceptor.ContextInitListener;
import cn.com.reformer.web.util.JPush;
import cn.com.reformer.web.util.JPushPayload;
import cn.jpush.api.push.model.PushPayload;
import redis.clients.jedis.Jedis;

public class PushTask extends TimerTask {
	private static final String APPKEY = ContextInitListener.get("AppKey");
	private static final String MASTERSECRET = ContextInitListener.get("MasterSecret");
	private static final String HOST = ContextInitListener.get("Host");
	private static final int PORT = Integer.parseInt(ContextInitListener.get("Port"));
	private static final Logger logger = Logger.getLogger(PushTask.class);
	private Jedis jedis = new Jedis(HOST, PORT);
	private String jedisKey = "information";
	private Lock cacheLock = new ReentrantLock();

	@Override
	public void run() {
		pushInformation();
	}

	public int pushInformation() {
		//System.out.println("==========/push/send===========");
		//System.out.println(jedis.zcard(jedisKey));
		// result = 1 represent the failure
		int result = 1;
		// send information
		// if the number of information less than 10
		if (jedis.zcard(jedisKey) >= 10) {// 10
			try {
				result = sendNInfo(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			try {
				result = sendNInfo(jedis.zcard(jedisKey).intValue());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * send n pieces of information and delete them
	 * 
	 * @param n
	 * @return
	 * @throws InterruptedException 
	 */
	private int sendNInfo(int n) throws InterruptedException {
		int result = 1;
		PushPayload payload = null;
		// jedis.zrem(key, members)
		// jedis.zscore(key, member) return a double value or null
		if(cacheLock.tryLock(100, TimeUnit.MILLISECONDS)) {
		try {
			Set<String> infoJsonSet = jedis.zrange(jedisKey, 0, n - 1);
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
		}
		return result;
	}
}
