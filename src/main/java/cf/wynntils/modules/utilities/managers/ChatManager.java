/*
 *  * Copyright © Wynntils - 2018.
 */

package cf.wynntils.modules.utilities.managers;

import cf.wynntils.ModCore;
import cf.wynntils.core.utils.Pair;
import cf.wynntils.modules.utilities.configs.UtilitiesConfig;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ChatManager {

    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private static String lastMessage = "";
    private  static int lastAmount = 2;

    private static final SoundEvent popOffSound = new SoundEvent(new ResourceLocation("minecraft", "entity.blaze.hurt"));

    public static Pair<String, Boolean> applyUpdates(String message) {
        String after = message;

        boolean cancel = false;

        if(UtilitiesConfig.Chat.INSTANCE.allowChatMentions && message.contains("/") && message.contains(":") && message.contains(ModCore.mc().player.getName())) {
            String[] messageSplitted = message.split(":");
            if(!messageSplitted[0].contains(ModCore.mc().player.getName())) {
                String playerName = ModCore.mc().player.getName();
                String afterSplit = StringUtils.join(Arrays.copyOfRange(messageSplitted, 1, messageSplitted.length));
                if(afterSplit.contains(playerName)) {
                    afterSplit = afterSplit.replace(playerName, "§e" + playerName + "§" + afterSplit.charAt(4));
                    after = messageSplitted[0] + ":" + afterSplit;
                    ModCore.mc().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_NOTE_PLING, 1.0F));
                }
            }
        }

        if(UtilitiesConfig.Chat.INSTANCE.addTimestampsToChat) {
            after = "§8[§7" + dateFormat.format(new Date()) + "§8] " + after;
        }

        if(message.contains(" requires your ") && message.contains(" skill to be at least ")){
            ModCore.mc().player.playSound(popOffSound, 1f, 1f);
        }

        if (UtilitiesConfig.Chat.INSTANCE.blockChatSpamFilter && message.equals(lastMessage)) {
            GuiNewChat ch = ModCore.mc().ingameGUI.getChatGUI();

            if(ch != null) {
                List<ChatLine> oldLines = ch.chatLines;
                if(oldLines != null && oldLines.size() > 0) {
                    ChatLine line = oldLines.get(0);
                    line.lineString = new TextComponentString(after + " §7[" + lastAmount++ + "x]");
                    ch.refreshChat();

                    cancel = true;
                }
            }
        }else{
            lastAmount = 2;
        }

        lastMessage = message;

        return new Pair<>(after, cancel);
    }

}