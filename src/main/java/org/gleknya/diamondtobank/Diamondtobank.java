package org.gleknya.diamondtobank;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Diamondtobank extends JavaPlugin implements CommandExecutor {

    private Economy economy;
    private ConfigurationSection config;
    private ConfigurationSection messages;
    private MiniMessage miniMessage;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault не найден! Плагин отключен.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        miniMessage = MiniMessage.miniMessage();

        loadPluginFiles();

        getCommand("deposit").setExecutor(this);
        getCommand("take").setExecutor(this);
        getCommand("diamondtobankreload").setExecutor(this);
    }

    private void loadPluginFiles() {
        saveDefaultConfig();
        saveResource("messages.yml", false);
        reloadConfig();
        config = getConfig();
        messages = loadMessages();
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    private ConfigurationSection loadMessages() {
        File file = new File(getDataFolder(), "messages.yml");
        try {
            return org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            getLogger().severe("Ошибка загрузки messages.yml: " + e.getMessage());
            return null;
        }
    }


    private void sendMessage(Player player, String path, Map<String, String> placeholders) {
        if (messages == null) {
            player.sendMessage("§cОшибка: messages.yml не загружен.");
            return;
        }
        String raw = messages.getString(path, "");
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                raw = raw.replace(e.getKey(), e.getValue());
            }
        }
        player.sendMessage(
                LegacyComponentSerializer.legacySection()
                        .serialize(miniMessage.deserialize(raw))
        );
    }

    private void sendMessage(Player player, String path) {
        sendMessage(player, path, null);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("diamondtobankreload")) {
            if (!player.hasPermission("diamondtobank.reload")) {
                sendMessage(player, "no_permission_reload");
                return true;
            }

            loadPluginFiles();
            sendMessage(player, "reload_success");
            return true;
        }


        if (!(sender instanceof Player)) {
            sender.sendMessage("Только игроки могут использовать эту команду.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("deposit")) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            Material depositItem = Material.valueOf(config.getString("deposit_item"));
            if (itemInHand.getType() != depositItem) {
                Map<String, String> ph = new HashMap<>();
                ph.put("{item}", config.getString("deposit_item"));
                sendMessage(player, "deposit_no_item", ph);
                return true;
            }

            int grossAmount = itemInHand.getAmount();
            double commission = config.getDouble("deposit_commission");
            int netAmount = (int) (grossAmount * (1 - commission));
            player.getInventory().setItemInMainHand(null);
            economy.depositPlayer(player, netAmount);

            Map<String, String> ph = new HashMap<>();
            ph.put("{net_amount}", String.valueOf(netAmount));
            ph.put("{item}", config.getString("deposit_item"));
            sendMessage(player, "deposit_success", ph);
            return true;
        }

        if (command.getName().equalsIgnoreCase("take")) {
            if (args.length != 1) {
                sendMessage(player, "take_usage");
                return true;
            }
            try {
                int amount = Integer.parseInt(args[0]);
                if (amount <= 0) {
                    sendMessage(player, "take_amount_zero");
                    return true;
                }
                Material takeItem = Material.valueOf(config.getString("take_item"));
                if (economy.getBalance(player) < amount) {
                    sendMessage(player, "take_insufficient_funds");
                    return true;
                }

                economy.withdrawPlayer(player, amount);

                int remaining = amount;
                while (remaining > 0) {
                    int giveNow = Math.min(64, remaining);
                    ItemStack stack = new ItemStack(takeItem, giveNow);
                    HashMap<Integer, ItemStack> notStored = player.getInventory().addItem(stack);
                    if (!notStored.isEmpty()) {
                        // Если инвентарь полон — выбрасываем остаток под ноги
                        for (ItemStack is : notStored.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), is);
                        }
                        break;
                    }
                    remaining -= giveNow;
                }

                Map<String, String> ph = new HashMap<>();
                ph.put("{amount}", String.valueOf(amount));
                ph.put("{item}", config.getString("take_item"));
                sendMessage(player, "take_success", ph);
            } catch (NumberFormatException e) {
                sendMessage(player, "take_invalid_number");
            }
            return true;
        }

        return false;
    }
}
