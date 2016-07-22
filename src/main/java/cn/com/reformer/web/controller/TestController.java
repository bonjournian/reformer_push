package cn.com.reformer.web.controller;


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
import redis.clients.jedis.Jedis;

@Controller 
@RequestMapping("/push")
public class TestController {

	private static final Logger logger = Logger.getLogger(TestController.class);
	private static final String HOST = ContextInitListener.get("Host");
	private static final int PORT = Integer.parseInt(ContextInitListener.get("Port"));
	// the start time
	private Jedis jedis = null;
	private String jedisKey = null;

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
		// ------------Test if this method executes----------------
		System.out.println("-----Constructor111()-----");
		// --------------------------------------------------------
	}
}
