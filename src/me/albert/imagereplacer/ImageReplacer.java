package me.albert.imagereplacer;

import me.albert.amazingbot.bot.Bot;
import me.albert.amazingbot.events.ABEvent;
import net.mamoe.mirai.event.events.GroupMessagePreSendEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URLDecoder;
import java.util.List;

public class ImageReplacer extends JavaPlugin implements Listener {
    private static int length = 100000;
    private static ImageReplacer instance;

    private static Message getMessageToReplace(GroupMessagePreSendEvent event) throws Exception {
        Message message = event.getMessage();
        if (message instanceof PlainText) {
            String text = message.contentToString();
            for (String section : instance.getConfig().getConfigurationSection("custom").getKeys(false)) {
                List<String> contains = instance.getConfig().getStringList("custom." + section + ".contains");
                List<String> equals = instance.getConfig().getStringList("custom." + section + ".equals");
                File file = new File(URLDecoder.decode(instance.getConfig().getString("custom." + section + ".file")));
                String messageString = instance.getConfig().getString("custom." + section + ".message");
                if (contains != null)
                    for (String key : contains) {
                        if (text.contains(key)) {
                            if (messageString!= null) return new PlainText(messageString);
                            BufferedImage bufferedImage = ImageIO.read(file);
                            return event.getTarget().uploadImageAsync(bufferedImage).get();
                        }
                    }
                if (equals != null)
                    for (String key : equals) {
                        if (text.equalsIgnoreCase(key)) {
                            if (messageString!= null) return new PlainText(messageString);
                            BufferedImage bufferedImage = ImageIO.read(file);
                            return event.getTarget().uploadImageAsync(bufferedImage).get();
                        }

                    }

            }

        }
        return null;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        length = getConfig().getInt("replace-length");
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Loaded");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        reloadConfig();
        length = getConfig().getInt("replace-length");
        sender.sendMessage("§c已重新载入配置文件");
        return true;
    }

    @EventHandler
    public void onMsg(ABEvent event) {
        if (event.getEvent() instanceof GroupMessagePreSendEvent) {
            GroupMessagePreSendEvent preSendEvent = (GroupMessagePreSendEvent) event.getEvent();
            if (preSendEvent.getMessage() instanceof PlainText) {
                try {
                    Message message1 = getMessageToReplace(preSendEvent);
                    if (message1 != null) {
                        preSendEvent.setMessage(message1);
                    }
                    String text = preSendEvent.getMessage().contentToString();
                    if (text.length() > length) {
                        Bot.getApi().getBot().getLogger().info("[ImageReplacer] 开始替换↑消息为图片");
                        List<BufferedImage> images = ImageUtil.createImage(text, 720, 480, 24, 20);
                        Message message = null;
                        for (BufferedImage image : images) {
                            Image image1 = preSendEvent.getTarget().uploadImageAsync(image).get();
                            if (message == null) {
                                message = image1;
                            }
                            message.plus(image1);
                        }
                        if (message != null)
                            preSendEvent.setMessage(message);

                    }
                } catch (Exception ignored) {

                }
            }
        }

    }
}
