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

package fr.moribus.imageonmap.commands.maptool;

import fr.moribus.imageonmap.ImageOnMap;
import fr.moribus.imageonmap.Permissions;
import fr.moribus.imageonmap.commands.IoMCommand;
import fr.moribus.imageonmap.commands.Commands;
import fr.moribus.imageonmap.economy.VaultEconomyManager;
import fr.moribus.imageonmap.i18n.I;
import fr.moribus.imageonmap.locale.LocaleManager;
import fr.moribus.imageonmap.image.ImageRendererExecutor;
import fr.moribus.imageonmap.image.ImageUtils;
import fr.moribus.imageonmap.image.PosterImage;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.MapManagerException;
import fr.moribus.imageonmap.map.PosterMap;
import fr.moribus.imageonmap.commands.CommandException;
import fr.moribus.imageonmap.commands.CommandInfo;
import fr.moribus.imageonmap.commands.WithFlags;
import fr.zcraft.quartzlib.tools.text.ActionBar;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(name = "new", usageParameters = "<URL> [resize] [--confirm]")
@WithFlags({"confirm"})
public class NewCommand extends IoMCommand {

    private ImageUtils.ScalingType resizeMode() throws CommandException {
        return switch (args[1]) {
            case "resize" -> ImageUtils.ScalingType.CONTAINED;
            case "stretch", "stretched", "resize-stretched" -> ImageUtils.ScalingType.STRETCHED;
            case "cover", "covered", "resize-covered" -> ImageUtils.ScalingType.COVERED;
            default -> {
                throwInvalidArgument(I.t("Invalid Stretching mode."));
                yield  ImageUtils.ScalingType.NONE;
            }
        };
    }

    @Override
    protected void run() throws CommandException {
        final Player player = playerSender();
        ImageUtils.ScalingType scaling = ImageUtils.ScalingType.NONE;
        URL url;
        int width = 0;
        int height = 0;
        final boolean confirm = isConfirmed();

        ImageOnMap.getPlugin().getLogger().info("[NewCommand] Player " + player.getName() + " initiated map creation");

        if (args.length < 1) {
            throwInvalidArgument(I.t("You must give an URL to take the image from."));
        }

        try {
            url = new URL(args[0]);
            ImageOnMap.getPlugin().getLogger().info("[NewCommand] URL parsed: " + url);
        } catch (MalformedURLException ex) {
            throwInvalidArgument(I.t("Invalid URL."));
            return;
        }

        if (args.length >= 2) {
            if (args.length >= 4) {
                width = Integer.parseInt(args[2]);
                height = Integer.parseInt(args[3]);
                ImageOnMap.getPlugin().getLogger().info("[NewCommand] Custom size specified: " + width + "x" + height);
            }
            scaling = resizeMode();
        }

        // Calculate estimated map size for confirmation/charging
        MapSize mapSize = calculateMapSize(url, scaling, width, height);
        ImageOnMap.getPlugin().getLogger().info("[NewCommand] Estimated map size: " +
            (mapSize != null ? mapSize.width + "×" + mapSize.height + " (" + mapSize.total + " blocks)" : "null"));

        // Check if the map size calculation failed
        if (mapSize == null || mapSize.total == 0) {
            player.sendMessage(LocaleManager.getMessage("economy.invalid-url"));
            ImageOnMap.getPlugin().getLogger().warning("[NewCommand] Failed to calculate map size for URL: " + url);
            return;
        }

        int estimatedMapCount = mapSize.total;

        // Show confirmation for players without bypass
        if (!Permissions.BYPASS_COST.grantedTo(player) && !confirm) {
            double cost = VaultEconomyManager.isEnabled()
                ? VaultEconomyManager.calculateMapCost(estimatedMapCount)
                : 0;

            // Build command for confirmation
            String confirmCommand = Commands.getCommandInfo(NewCommand.class).build(
                args[0],
                args.length >= 2 ? args[1] : "",
                args.length >= 3 ? args[2] : "",
                args.length >= 4 ? args[3] : "",
                "--confirm"
            ).trim().replaceAll("\\s+", " ");

            // Send confirmation message
            player.sendMessage(Component.text()
                .append(Component.text("════════════════════════════════════").color(NamedTextColor.GOLD))
                .build());

            player.sendMessage(Component.text()
                .append(Component.text(LocaleManager.getMessage("economy.confirmation-title")).color(NamedTextColor.YELLOW))
                .build());

            player.sendMessage(Component.text()
                .append(Component.text("════════════════════════════════════").color(NamedTextColor.GOLD))
                .build());

            player.sendMessage(Component.text()
                .append(Component.text(LocaleManager.getMessage("economy.map-size")).color(NamedTextColor.AQUA))
                .append(Component.text(LocaleManager.getMessage("economy.blocks", estimatedMapCount, mapSize.width, mapSize.height)).color(NamedTextColor.WHITE))
                .build());

            if (VaultEconomyManager.isEnabled()) {
                player.sendMessage(Component.text()
                    .append(Component.text(LocaleManager.getMessage("economy.cost")).color(NamedTextColor.AQUA))
                    .append(Component.text(VaultEconomyManager.formatCurrency(cost)).color(NamedTextColor.GOLD))
                    .build());

                player.sendMessage(Component.text()
                    .append(Component.text(LocaleManager.getMessage("economy.balance")).color(NamedTextColor.AQUA))
                    .append(Component.text(VaultEconomyManager.formatCurrency(VaultEconomyManager.getBalance(player))).color(NamedTextColor.WHITE))
                    .build());
            }

            player.sendMessage(Component.text()
                .append(Component.text("════════════════════════════════════").color(NamedTextColor.GOLD))
                .build());

            player.sendMessage(Component.text()
                .append(Component.text(LocaleManager.getMessage("economy.click-to-confirm")).color(NamedTextColor.YELLOW))
                .append(Component.text(LocaleManager.getMessage("economy.confirm-button"))
                    .color(NamedTextColor.GREEN)
                    .hoverEvent(HoverEvent.showText(Component.text(LocaleManager.getMessage("economy.confirm-hover"))))
                    .clickEvent(ClickEvent.runCommand(confirmCommand)))
                .build());

            ImageOnMap.getPlugin().getLogger().info("[NewCommand] Confirmation message sent to " + player.getName());
            return;
        }

        // Check map size limit BEFORE quota and charging
        int maxMapSize = MapManager.getPlayerMaxMapSize(player.getUniqueId());
        if (maxMapSize > 0 && estimatedMapCount > maxMapSize) {
            player.sendMessage(LocaleManager.getMessage("economy.max-size-exceeded",
                estimatedMapCount, maxMapSize));
            ImageOnMap.getPlugin().getLogger().warning("[NewCommand] Map size check failed for " + player.getName() +
                ": requested=" + estimatedMapCount + ", limit=" + maxMapSize);
            return;
        }

        // Check map quota BEFORE charging (we're creating 1 new map, not multiple)
        try {
            MapManager.checkMapLimitForPlayer(player.getUniqueId(), 1);
        } catch (MapManagerException ex) {
            // Show user-friendly error message based on the reason
            if (ex.getMessage().contains("maximum")) {
                player.sendMessage(LocaleManager.getMessage("economy.quota-exceeded", ex.getMessage()));
            } else {
                player.sendMessage(LocaleManager.getMessage("economy.quota-error", ex.getMessage()));
            }
            ImageOnMap.getPlugin().getLogger().warning("[NewCommand] Map quota check failed for " + player.getName() + ": " + ex.getMessage());
            return;
        }

        // Economy check and charge
        final boolean economyCharged;
        final double chargedAmount;

        if (VaultEconomyManager.isEnabled() && !Permissions.BYPASS_COST.grantedTo(player)) {
            if (estimatedMapCount > 0) {
                double cost = VaultEconomyManager.calculateMapCost(estimatedMapCount);
                ImageOnMap.getPlugin().getLogger().info("[NewCommand] Checking balance for " + player.getName() + ": cost=" + cost + ", balance=" + VaultEconomyManager.getBalance(player));

                if (!VaultEconomyManager.hasEnoughMoney(player, estimatedMapCount)) {
                    player.sendMessage(LocaleManager.getMessage("economy.insufficient-funds"));
                    player.sendMessage(LocaleManager.getMessage("economy.insufficient-funds-details",
                        VaultEconomyManager.formatCurrency(cost),
                        VaultEconomyManager.formatCurrency(VaultEconomyManager.getBalance(player))));
                    ImageOnMap.getPlugin().getLogger().warning("[NewCommand] " + player.getName() + " has insufficient funds");
                    return;
                }

                if (!VaultEconomyManager.chargePlayer(player, estimatedMapCount)) {
                    player.sendMessage(LocaleManager.getMessage("economy.charge-failed"));
                    ImageOnMap.getPlugin().getLogger().severe("[NewCommand] Failed to charge " + player.getName());
                    return;
                }

                player.sendMessage(LocaleManager.getMessage("economy.charged",
                    VaultEconomyManager.formatCurrency(cost), estimatedMapCount));
                ImageOnMap.getPlugin().getLogger().info("[NewCommand] Successfully charged " + player.getName() + " " + cost);
                economyCharged = true;
                chargedAmount = cost;
            } else {
                economyCharged = false;
                chargedAmount = 0;
            }
        } else {
            if (Permissions.BYPASS_COST.grantedTo(player)) {
                ImageOnMap.getPlugin().getLogger().info("[NewCommand] " + player.getName() + " has bypass cost permission");
            }
            economyCharged = false;
            chargedAmount = 0;
        }

        try {
            ImageOnMap.getPlugin().getLogger().info("[NewCommand] Starting render for " + player.getName());
            ActionBar.sendPermanentMessage(player, ChatColor.DARK_GREEN + I.t("Rendering..."));
            ImageRendererExecutor.render(url, scaling, player.getUniqueId(), width, height)
                    .exceptionallyAsync((exception) -> {
                        ImageOnMap.getPlugin().getLogger().log(Level.SEVERE, "[NewCommand] Rendering failed for " + player.getName(), exception);
                        // Schedule message on main thread
                        org.bukkit.Bukkit.getScheduler().runTask(ImageOnMap.getPlugin(), () -> {
                            // Refund if money was charged
                            if (economyCharged) {
                                if (VaultEconomyManager.refundPlayer(player, estimatedMapCount)) {
                                    ImageOnMap.getPlugin().getLogger().info("[NewCommand] Refunded " + chargedAmount + " to " + player.getName() + " due to rendering failure");
                                    player.sendMessage(LocaleManager.getMessage("economy.render-failed-refunded",
                                        VaultEconomyManager.formatCurrency(chargedAmount)));
                                } else {
                                    ImageOnMap.getPlugin().getLogger().severe("[NewCommand] Failed to refund " + chargedAmount + " to " + player.getName());
                                    player.sendMessage(LocaleManager.getMessage("economy.render-failed-no-refund"));
                                }
                            } else {
                                // No refund needed, just show error
                                // Check if it's a quota exception
                                if (exception.getCause() instanceof MapManagerException) {
                                    player.sendMessage(LocaleManager.getMessage("economy.quota-exceeded", exception.getCause().getMessage()));
                                } else {
                                    // Generic user-friendly error message
                                    player.sendMessage(LocaleManager.getMessage("economy.render-failed"));
                                }
                            }
                            ActionBar.removeMessage(player);
                        });
                        return null;
                    })
                    .thenAccept(result -> {
                        ImageOnMap.getPlugin().getLogger().info("[NewCommand] Rendering completed for " + player.getName() + ", result=" + (result != null ? result.getId() : "null"));

                        // Execute on main thread for Bukkit API calls
                        org.bukkit.Bukkit.getScheduler().runTask(ImageOnMap.getPlugin(), () -> {
                            ActionBar.removeMessage(player);

                            if (result == null) {
                                ImageOnMap.getPlugin().getLogger().warning("[NewCommand] Result is null for " + player.getName());
                                player.sendMessage(I.t("{ce}Map rendering failed: result is null"));
                                return;
                            }

                            player.sendActionBar(Component.text()
                                    .color(NamedTextColor.DARK_GREEN)
                                    .append(Component.text(I.t("Rendering finished!")))
                                    .build()
                            );

                            ImageOnMap.getPlugin().getLogger().info("[NewCommand] Attempting to give map to " + player.getName());
                            // NOTE: give() returns true if inventory is FULL, false if successful
                            boolean inventoryFull = result.give(player);
                            ImageOnMap.getPlugin().getLogger().info("[NewCommand] result.give() returned: " + inventoryFull + " (inventory full=" + inventoryFull + ")");

                            if (!inventoryFull) {
                                // Successfully gave the map
                                ImageOnMap.getPlugin().getLogger().info("[NewCommand] Successfully gave map to " + player.getName());
                                player.sendMessage(LocaleManager.getMessage("economy.created"));
                            } else if (inventoryFull && (result instanceof PosterMap && !((PosterMap) result).hasColumnData())) {
                                // Poster map was too big, parts need to be retrieved
                                ImageOnMap.getPlugin().getLogger().info("[NewCommand] Poster map too big for inventory: " + player.getName());
                                info(I.t("The rendered map was too big to fit in your inventory."));
                                info(I.t("Use '/maptool getremaining' to get the remaining maps."));
                            } else {
                                // Inventory was full
                                ImageOnMap.getPlugin().getLogger().warning("[NewCommand] Inventory full for " + player.getName());
                                player.sendMessage(LocaleManager.getMessage("economy.inventory-full"));
                            }
                        });
                    });
        } finally {
            ActionBar.removeMessage(player);
        }
    }

    /**
     * Holds map size information
     */
    private static class MapSize {
        final int width;
        final int height;
        final int total;

        MapSize(int width, int height) {
            this.width = width;
            this.height = height;
            this.total = width * height;
        }
    }

    /**
     * Calculate the estimated map size needed for an image
     *
     * @param url     The image URL
     * @param scaling The scaling type
     * @param width   The width in maps (0 = auto)
     * @param height  The height in maps (0 = auto)
     * @return The MapSize with width, height, and total, or null if cannot calculate
     */
    private MapSize calculateMapSize(URL url, ImageUtils.ScalingType scaling, int width, int height) {
        try {
            // If width and height are specified, use them directly
            if (width > 0 && height > 0) {
                return new MapSize(width, height);
            }

            // Otherwise, download and check the image
            BufferedImage image;
            try (var stream = url.openStream()) {
                image = ImageIO.read(stream);
            }

            if (image == null) {
                return null;
            }

            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            image.flush();

            // If resize mode is used and dimensions are 0, it becomes a single map
            if (scaling != ImageUtils.ScalingType.NONE && height <= 1 && width <= 1) {
                return new MapSize(1, 1);
            }

            // Calculate the number of 128x128 blocks needed
            int columns = (int) Math.ceil((double) imageWidth / 128.0);
            int rows = (int) Math.ceil((double) imageHeight / 128.0);

            return new MapSize(columns, rows);
        } catch (IOException e) {
            ImageOnMap.getPlugin().getLogger().warning("Failed to calculate map size for " + url + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Calculate the estimated number of map blocks needed for an image
     *
     * @param url     The image URL
     * @param scaling The scaling type
     * @param width   The width in maps (0 = auto)
     * @param height  The height in maps (0 = auto)
     * @return The estimated number of map blocks, or 0 if cannot calculate
     */
    private int calculateEstimatedMapCount(URL url, ImageUtils.ScalingType scaling, int width, int height) {
        MapSize size = calculateMapSize(url, scaling, width, height);
        return size != null ? size.total : 0;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return Permissions.NEW.grantedTo(sender);
    }
}
