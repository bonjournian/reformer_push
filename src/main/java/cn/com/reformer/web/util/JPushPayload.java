package cn.com.reformer.web.util;

import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.audience.AudienceTarget;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;

/**
 * JPush推送的数据结构 参照jpush-api-java-client-3.2.9中的PushExample.java
 * 
 * @author cent
 *
 */
public class JPushPayload {

	/**
	 * 生成推送通知的数据结构, 目标用户: 全部, 目标平台: 全部
	 * 
	 * @param alert
	 *            通知内容
	 * @return 推送的数据结构
	 */
	public static PushPayload buildPushObject_all_all_alert(String alert) {
		return PushPayload.alertAll(alert);
	}

	/**
	 * 生成推送通知的数据结构, 目标用户: 指定的一组别名, 目标平台: 全部
	 * 
	 * @param alert
	 *            通知内容
	 * @param alias
	 *            指定的一组别名
	 * @return 推送的数据结构
	 */
	public static PushPayload buildPushObject_all_alias_alert(String alert, String... alias) {
		return PushPayload.newBuilder().setPlatform(Platform.all()).setAudience(Audience.alias(alias))
				.setNotification(Notification.alert(alert)).build();
	}

	/**
	 * 生成推送通知的数据结构, 目标用户: 指定的一组标签, 目标平台: android
	 * 
	 * @param alert
	 *            通知内容
	 * @param title
	 *            通知标题
	 * @param tags
	 *            指定的一组标签
	 * @return 推送的数据结构
	 */
	public static PushPayload buildPushObject_android_tag_alertWithTitle(String alert, String title, String... tags) {
		return PushPayload.newBuilder().setPlatform(Platform.android()).setAudience(Audience.tag(tags))
				.setNotification(Notification.android(alert, title, null)).build();
	}

	/**
	 * 生成推送通知的数据结构, 目标用户: 指定的一组标签, 目标平台: android和ios
	 * 
	 * @param alert
	 *            通知内容
	 * @param adTitle
	 *            android的通知标题
	 * @param iosExtraKey
	 *            ios的通知扩展key
	 * @param iosExtraValue
	 *            ios的通知扩展value
	 * @param tags
	 *            指定的一组标签
	 * @return 推送的数据结构
	 */
	public static PushPayload buildPushObject_android_and_ios(String alert, String adTitle, String iosExtraKey,
			String iosExtraValue, String... tags) {
		return PushPayload.newBuilder().setPlatform(Platform.android_ios()).setAudience(Audience.tag(tags))
				.setNotification(Notification.newBuilder().setAlert(alert)
						.addPlatformNotification(AndroidNotification.newBuilder().setTitle(adTitle).build())
						.addPlatformNotification(
								IosNotification.newBuilder().incrBadge(1).addExtra(iosExtraKey, iosExtraValue).build())
						.build())
				.build();
	}

	/**
	 * 生成推送通知和消息的数据结构, 目标用户: 指定的一组别名, 目标平台: 所有平台
	 * 
	 * @param message
	 *            消息内容
	 * @param alias
	 *            一组别名
	 * @return 推送的数据结构
	 */
	public static PushPayload buildPushObject_ios_audienceMore_messageWithExtras(String message, String... alias) {
		return PushPayload.newBuilder().setPlatform(Platform.all())
				.setAudience(Audience.newBuilder().addAudienceTarget(AudienceTarget.alias(alias)).build())
				.setMessage(Message.newBuilder().setMsgContent(message).build()).build();
	}

	/**
	 * 生成推送通知和消息的数据结构, 目标用户: 一组标签的交集(属于多个标签的用户), 目标平台: ios
	 * 
	 * @param alert
	 *            通知内容
	 * @param sound
	 *            通知提示声
	 * @param iosExtraKey
	 *            ios的通知扩展key
	 * @param iosExtraValue
	 *            ios的通知扩展value
	 * @param message
	 *            消息内容
	 * @param tagAnd
	 *            多组标签, 实际取交集
	 * @return 推送的数据结构
	 */
	public static PushPayload buildPushObject_ios_tagAnd_alertWithExtrasAndMessage(String alert, String sound,
			String iosExtraKey, String iosExtraValue, String message, String... tagAnd) {
		return PushPayload.newBuilder().setPlatform(Platform.ios()).setAudience(Audience.tag_and(tagAnd))
				.setNotification(Notification.newBuilder()
						.addPlatformNotification(IosNotification.newBuilder().setAlert(alert).incrBadge(1)
								.setSound(sound).addExtra(iosExtraKey, iosExtraValue).build())
						.build())
				.setMessage(Message.content(message)).setOptions(Options.newBuilder().setApnsProduction(true).build())
				.build();
	}

	/**
	 * 生成推送通知和消息的数据结构, 目标用户: 一组标签和一组别名的交集(别名中且属于多个标签的用户), 目标平台: ios
	 * 
	 * @param message
	 *            消息内容
	 * @param iosExtraKey
	 *            ios的消息扩展key
	 * @param iosExtraValue
	 *            ios的消息扩展value
	 * @param tags
	 *            指定的一组标签
	 * @param alias
	 *            指定的一组用户
	 * @return 推送的数据结构
	 */
	public static PushPayload buildPushObject_ios_audienceMore_messageWithExtras(String message, String iosExtraKey,
			String iosExtraValue, String[] tags, String[] alias) {
		return PushPayload.newBuilder().setPlatform(Platform.android_ios())
				.setAudience(Audience.newBuilder().addAudienceTarget(AudienceTarget.tag(tags))
						.addAudienceTarget(AudienceTarget.alias(alias)).build())
				.setMessage(Message.newBuilder().setMsgContent(message).addExtra(iosExtraKey, iosExtraValue).build())
				.build();
	}
	
	public static PushPayload buildNotificationPushload(String content, String title, String... alias) {
		return PushPayload.newBuilder().setPlatform(Platform.android_ios()).setAudience(Audience.alias(alias))
				.setNotification(Notification.newBuilder().setAlert(content).build()).build();
	}

	public static PushPayload buildMessagePushload(String content, String title, String... alias) {
		return PushPayload.newBuilder().setPlatform(Platform.android_ios()).setAudience(Audience.alias(alias))
				.setMessage(Message.newBuilder().setTitle(title).setMsgContent(content).build()).build();
	}
}
