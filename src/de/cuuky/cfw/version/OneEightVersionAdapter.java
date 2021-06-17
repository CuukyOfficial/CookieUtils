package de.cuuky.cfw.version;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

class OneEightVersionAdapter extends OneSevenVersionAdapter {

	protected Class<?> chatBaseComponentInterface;
	protected Method chatSerializerMethod;
	
	//chat
	protected Class<?> packetChatClass;
	protected Constructor<?> packetChatConstructor;
	private Constructor<?> packetChatByteConstructor;
	private Constructor<?> titleConstructor;
	private Object title, subtitle;
	
	//tablist
	private Class<?> tablistPacketClass;
	private Field footerField, headerField;

	@Override
	protected void init() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException, ClassNotFoundException {
		super.init();
		this.initTablist();
		this.initChat();
		this.initTitle();
	}
	
	protected void initTablist() throws NoSuchFieldException, SecurityException, ClassNotFoundException, NoSuchMethodException {
		this.tablistPacketClass = Class.forName(VersionUtils.getNmsClass() + ".PacketPlayOutPlayerListHeaderFooter");
		
		this.headerField = this.tablistPacketClass.getDeclaredField("a");
		this.headerField.setAccessible(true);

		this.footerField = this.tablistPacketClass.getDeclaredField("b");
		this.footerField.setAccessible(true);
	}
	
	protected void initChat() throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
		this.chatBaseComponentInterface = Class.forName(VersionUtils.getNmsClass() + ".IChatBaseComponent");
		Class<?> chatSerializer = Class.forName(VersionUtils.getNmsClass() + ".IChatBaseComponent$ChatSerializer"); //.ChatSerializer //.network.chat.IChatBaseComponent$ChatSerializer
		this.chatSerializerMethod = chatSerializer.getDeclaredMethod("a", String.class);
		
		this.packetChatClass = Class.forName(VersionUtils.getNmsClass() + ".PacketPlayOutChat");
		this.packetChatConstructor = this.packetChatClass.getConstructor(this.chatBaseComponentInterface);
		this.packetChatByteConstructor = this.packetChatClass.getConstructor(this.chatBaseComponentInterface, byte.class);
	}
	
	protected void initTitle() throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, NoSuchMethodException {
		Class<?> titleClass = Class.forName(VersionUtils.getNmsClass() + ".PacketPlayOutTitle");
		Class<?> enumTitleClass = titleClass.getDeclaredClasses()[0]; //Class.forName(path + ".EnumTitleAction");
		this.titleConstructor = titleClass.getConstructor(enumTitleClass, chatBaseComponentInterface, int.class, int.class, int.class);
		this.title = enumTitleClass.getDeclaredField("TITLE").get(null);
		this.subtitle = enumTitleClass.getDeclaredField("SUBTITLE").get(null);
	}

	@Override
	public void sendActionbar(Player player, String message, int duration, Plugin instance) {
		new BukkitRunnable() {

			private int count;

			@Override
			public void run() {
				sendActionbar(player, message);

				if (this.count >= duration)
					this.cancel();

				this.count++;
			}
		}.runTaskTimerAsynchronously(instance, 0, 20);
	}

	@Override
	public void sendActionbar(Player player, String message) {
		try {
			Object barchat = this.chatSerializerMethod.invoke(null, "{\"text\": \"" + message + "\"}");
			this.sendPacket(player, this.getActionbarPacket(player, barchat));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected Object getActionbarPacket(Player player, Object text) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return this.packetChatByteConstructor.newInstance(text, (byte) 2);
	}

	@Override
	public void sendLinkedMessage(Player player, String message, String link) {
		try {
			Object text = this.chatSerializerMethod.invoke(null, "{\"text\": \"" + message + "\", \"color\": \"white\", \"clickEvent\": {\"action\": \"open_url\" , \"value\": \"" + link + "\"}}");
			this.sendPacket(player, this.getMessagePacket(player, text));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	protected Object getMessagePacket(Player player, Object text) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return this.packetChatConstructor.newInstance(text);
	}

	@Override
	public void sendTablist(Player player, String header, String footer) {
		try {
			Object tabheader = this.chatSerializerMethod.invoke(null, "{\"text\": \"" + header + "\"}");
			Object tabfooter = this.chatSerializerMethod.invoke(null, "{\"text\": \"" + footer + "\"}");
			Object packet = this.tablistPacketClass.newInstance();

			this.headerField.set(packet, tabheader);
			this.footerField.set(packet, tabfooter);

			this.sendPacket(player, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendTitle(Player player, String header, String footer) {
		try {
			Object titleHeader = this.chatSerializerMethod.invoke(null, "{\"text\": \"" + header + "\"}");
			Object titleFooter = this.chatSerializerMethod.invoke(null, "{\"text\": \"" + footer + "\"}");

			Object headerPacket = this.titleConstructor.newInstance(this.title, titleHeader, 0, 2, 0);
			Object footerPacket = this.titleConstructor.newInstance(this.subtitle, titleFooter, 0, 2, 0);

			this.sendPacket(player, headerPacket);
			this.sendPacket(player, footerPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
