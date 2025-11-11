/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2021)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2021)
 *
 * This software is a computer program whose purpose is to allow insertion of
 * custom images in a Minecraft world.
 *
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */

package fr.moribus.imageonmap.economy;

import fr.moribus.imageonmap.ImageOnMap;
import fr.moribus.imageonmap.PluginConfiguration;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Level;

/**
 * Manages economy integration with Vault.
 * Handles charging players for map creation and updates.
 */
public class VaultEconomyManager {

    private static Economy economy = null;
    private static boolean economyEnabled = false;

    /**
     * Initialize the Vault economy integration
     *
     * @return true if successfully initialized
     */
    public static boolean init() {
        if (!PluginConfiguration.ECONOMY_ENABLED.get()) {
            ImageOnMap.getPlugin().getLogger().info("Economy integration is disabled in config.");
            economyEnabled = false;
            return false;
        }

        if (ImageOnMap.getPlugin().getServer().getPluginManager().getPlugin("Vault") == null) {
            ImageOnMap.getPlugin().getLogger().warning("Vault plugin not found! Economy integration disabled.");
            economyEnabled = false;
            return false;
        }

        RegisteredServiceProvider<Economy> rsp =
            ImageOnMap.getPlugin().getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            ImageOnMap.getPlugin().getLogger().warning("No economy provider found! Economy integration disabled.");
            economyEnabled = false;
            return false;
        }

        economy = rsp.getProvider();
        economyEnabled = true;
        ImageOnMap.getPlugin().getLogger().info("Economy integration enabled with " + economy.getName());
        return true;
    }

    /**
     * Check if economy is enabled and available
     *
     * @return true if economy is enabled
     */
    public static boolean isEnabled() {
        return economyEnabled && economy != null;
    }

    /**
     * Calculate the cost for creating a map based on the number of blocks (map parts)
     *
     * @param mapCount The number of map blocks (e.g., 2x2 = 4 blocks)
     * @return The cost in currency
     */
    public static double calculateMapCost(int mapCount) {
        return mapCount * PluginConfiguration.ECONOMY_COST_PER_MAP.get();
    }

    /**
     * Check if a player has enough money to create a map
     *
     * @param player   The player to check
     * @param mapCount The number of map blocks
     * @return true if the player has enough money
     */
    public static boolean hasEnoughMoney(OfflinePlayer player, int mapCount) {
        if (!isEnabled()) {
            return true; // If economy is disabled, always allow
        }

        double cost = calculateMapCost(mapCount);
        return economy.has(player, cost);
    }

    /**
     * Get the balance of a player
     *
     * @param player The player to check
     * @return The player's balance
     */
    public static double getBalance(OfflinePlayer player) {
        if (!isEnabled()) {
            return 0;
        }
        return economy.getBalance(player);
    }

    /**
     * Charge a player for creating a map
     *
     * @param player   The player to charge
     * @param mapCount The number of map blocks
     * @return true if the charge was successful
     */
    public static boolean chargePlayer(OfflinePlayer player, int mapCount) {
        if (!isEnabled()) {
            return true; // If economy is disabled, always succeed
        }

        double cost = calculateMapCost(mapCount);

        if (!hasEnoughMoney(player, mapCount)) {
            return false;
        }

        try {
            var response = economy.withdrawPlayer(player, cost);
            if (response.transactionSuccess()) {
                ImageOnMap.getPlugin().getLogger().log(Level.INFO,
                    String.format("Charged %s %.2f %s for %d map blocks",
                        player.getName(), cost, economy.currencyNamePlural(), mapCount));
                return true;
            } else {
                ImageOnMap.getPlugin().getLogger().log(Level.WARNING,
                    String.format("Failed to charge %s: %s", player.getName(), response.errorMessage));
                return false;
            }
        } catch (Exception e) {
            ImageOnMap.getPlugin().getLogger().log(Level.SEVERE,
                "Error charging player " + player.getName(), e);
            return false;
        }
    }

    /**
     * Refund a player for a deleted map
     *
     * @param player   The player to refund
     * @param mapCount The number of map blocks
     * @return true if the refund was successful
     */
    public static boolean refundPlayer(OfflinePlayer player, int mapCount) {
        if (!isEnabled() || !PluginConfiguration.ECONOMY_REFUND_ON_DELETE.get()) {
            return true;
        }

        double refund = calculateMapCost(mapCount) * PluginConfiguration.ECONOMY_REFUND_PERCENTAGE.get();

        try {
            var response = economy.depositPlayer(player, refund);
            if (response.transactionSuccess()) {
                ImageOnMap.getPlugin().getLogger().log(Level.INFO,
                    String.format("Refunded %s %.2f %s for %d map blocks",
                        player.getName(), refund, economy.currencyNamePlural(), mapCount));
                return true;
            } else {
                ImageOnMap.getPlugin().getLogger().log(Level.WARNING,
                    String.format("Failed to refund %s: %s", player.getName(), response.errorMessage));
                return false;
            }
        } catch (Exception e) {
            ImageOnMap.getPlugin().getLogger().log(Level.SEVERE,
                "Error refunding player " + player.getName(), e);
            return false;
        }
    }

    /**
     * Format currency amount for display
     *
     * @param amount The amount to format
     * @return Formatted currency string
     */
    public static String formatCurrency(double amount) {
        if (!isEnabled()) {
            return String.format("%.2f", amount);
        }
        return economy.format(amount);
    }

    /**
     * Get the currency name (plural)
     *
     * @return The currency name
     */
    public static String getCurrencyName() {
        if (!isEnabled()) {
            return "currency";
        }
        return economy.currencyNamePlural();
    }

    /**
     * Cleanup economy resources
     */
    public static void exit() {
        economy = null;
        economyEnabled = false;
    }
}
