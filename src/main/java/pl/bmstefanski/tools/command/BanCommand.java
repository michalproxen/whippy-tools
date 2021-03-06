/*
 MIT License

 Copyright (c) 2018 Whippy Tools

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package pl.bmstefanski.tools.command;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.bmstefanski.tools.Tools;
import pl.bmstefanski.tools.api.basic.Ban;
import pl.bmstefanski.tools.api.basic.User;
import pl.bmstefanski.tools.basic.BanImpl;
import pl.bmstefanski.tools.basic.manager.UserManager;
import pl.bmstefanski.tools.command.basic.CommandContext;
import pl.bmstefanski.tools.command.basic.CommandInfo;
import pl.bmstefanski.tools.storage.configuration.Messages;
import pl.bmstefanski.tools.util.MessageUtils;
import pl.bmstefanski.tools.util.TabCompleterUtils;

import java.util.List;

public class BanCommand implements MessageUtils {

    private final Tools plugin;
    private final Messages messages;

    public BanCommand(Tools plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessages();
    }

    @CommandInfo(
            name = "ban",
            description = "ban command",
            usage = "[player] [reason]",
            permission = "ban",
            min = 1,
            completer = "banCompleter"
    )
    private void ban(CommandSender commandSender, CommandContext context) {

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(context.getParam(0));

        User punisher = UserManager.getUser(commandSender.getName());
        User punished = UserManager.getUser(offlinePlayer.getUniqueId());

        if (!offlinePlayer.hasPlayedBefore()) {
            sendMessage(commandSender, StringUtils.replace(messages.getPlayerNotFound(), "%player%", context.getParam(0)));
            return;
        }

        if (punished.getName().equals(punisher.getName())) {
            sendMessage(commandSender, messages.getCannotBanYourself());
            return;
        }

        if (punished.isBanned()) {
            sendMessage(commandSender, StringUtils.replace(messages.getAlreadyBanned(), "%player%", offlinePlayer.getName()));
            return;
        }

        String reason = "";

        if (context.getArgs().length == 1) {
            reason = fixColor(messages.getDefaultReason());
        } else if (context.getArgs().length > 1) reason = fixColor(StringUtils.join(context.getArgs(), " ", 1, context.getArgs().length));

        Ban ban = new BanImpl(punished.getUUID(), punisher.getName());
        ban.setReason(reason);
        ban.setTime(-1);

        plugin.getBanResource().add(ban);

        if (offlinePlayer.isOnline()) {
            String banFormat = listToString(messages.getBanFormat());
            String untilFormat = fixColor(messages.getPermanentBan());

            Player target = Bukkit.getPlayer(offlinePlayer.getUniqueId());

            target.kickPlayer(StringUtils.replaceEach(banFormat,
                    new String[]{"%punisher%", "%until%", "%reason%"},
                    new String[]{ban.getPunisher(), untilFormat, reason}));
        }

        sendMessage(commandSender, StringUtils.replace(messages.getSuccessfullyBanned(), "%player%", offlinePlayer.getName()));
    }

    public List<String> banCompleter(CommandSender commandSender, CommandContext context) {
        List<String> availableList = TabCompleterUtils.getAvailableList(context);
        if (availableList != null) return availableList;

        return null;
    }
}
