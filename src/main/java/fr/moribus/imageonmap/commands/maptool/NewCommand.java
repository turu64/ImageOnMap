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
import fr.moribus.imageonmap.image.ImageRendererExecutor;
import fr.moribus.imageonmap.image.ImageUtils;
import fr.moribus.imageonmap.image.PosterImage;
import fr.moribus.imageonmap.map.MapManager;
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
        final boolean confirm = hasFlag("confirm");

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

        // Calculate estimated map count for confirmation/charging
        int estimatedMapCount = calculateEstimatedMapCount(url, scaling, width, height);
        ImageOnMap.getPlugin().getLogger().info("[NewCommand] Estimated map count: " + estimatedMapCount);

        // Show confirmation for players without bypass
        if (!Permissions.BYPASS_COST.grantedTo(player) && !confirm) {
            double cost = VaultEconomyManager.isEnabled()
                ? VaultEconomyManager.calculateMapCost(estimatedMapCount)
                : 0;

            // Build command for confirmation
            String confirmCommand = Commands.getCommandInfo(NewCommand.class).build(
                "\"" + args[0] + "\"",
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
                .append(Component.text(I.t("Map Creation Confirmation")).color(NamedTextColor.YELLOW))
                .build());

            player.sendMessage(Component.text()
                .append(Component.text("════════════════════════════════════").color(NamedTextColor.GOLD))
                .build());

            player.sendMessage(Component.text()
                .append(Component.text(I.t("Map Size: ")).color(NamedTextColor.AQUA))
                .append(Component.text(estimatedMapCount + " blocks").color(NamedTextColor.WHITE))
                .build());

            if (VaultEconomyManager.isEnabled()) {
                player.sendMessage(Component.text()
                    .append(Component.text(I.t("Cost: ")).color(NamedTextColor.AQUA))
                    .append(Component.text(VaultEconomyManager.formatCurrency(cost)).color(NamedTextColor.GOLD))
                    .build());

                player.sendMessage(Component.text()
                    .append(Component.text(I.t("Your Balance: ")).color(NamedTextColor.AQUA))
                    .append(Component.text(VaultEconomyManager.formatCurrency(VaultEconomyManager.getBalance(player))).color(NamedTextColor.WHITE))
                    .build());
            }

            player.sendMessage(Component.text()
                .append(Component.text("════════════════════════════════════").color(NamedTextColor.GOLD))
                .build());

            player.sendMessage(Component.text()
                .append(Component.text(I.t("Click to confirm: ")).color(NamedTextColor.YELLOW))
                .append(Component.text("[CONFIRM]")
                    .color(NamedTextColor.GREEN)
                    .hoverEvent(HoverEvent.showText(Component.text(I.t("Click to create the map"))))
                    .clickEvent(ClickEvent.runCommand(confirmCommand)))
                .build());

            ImageOnMap.getPlugin().getLogger().info("[NewCommand] Confirmation message sent to " + player.getName());
            return;
        }

        // Economy check and charge
        if (VaultEconomyManager.isEnabled() && !Permissions.BYPASS_COST.grantedTo(player)) {
            if (estimatedMapCount > 0) {
                double cost = VaultEconomyManager.calculateMapCost(estimatedMapCount);
                ImageOnMap.getPlugin().getLogger().info("[NewCommand] Checking balance for " + player.getName() + ": cost=" + cost + ", balance=" + VaultEconomyManager.getBalance(player));

                if (!VaultEconomyManager.hasEnoughMoney(player, estimatedMapCount)) {
                    player.sendMessage(I.t("{ce}You don't have enough money to create this map!"));
                    player.sendMessage(I.t("{ce}Cost: {0}, Your balance: {1}",
                        VaultEconomyManager.formatCurrency(cost),
                        VaultEconomyManager.formatCurrency(VaultEconomyManager.getBalance(player))));
                    ImageOnMap.getPlugin().getLogger().warning("[NewCommand] " + player.getName() + " has insufficient funds");
                    return;
                }

                if (!VaultEconomyManager.chargePlayer(player, estimatedMapCount)) {
                    player.sendMessage(I.t("{ce}Failed to charge your account. Map creation cancelled."));
                    ImageOnMap.getPlugin().getLogger().severe("[NewCommand] Failed to charge " + player.getName());
                    return;
                }

                player.sendMessage(I.t("{cs}Charged {0} for {1} map blocks.",
                    VaultEconomyManager.formatCurrency(cost), estimatedMapCount));
                ImageOnMap.getPlugin().getLogger().info("[NewCommand] Successfully charged " + player.getName() + " " + cost);
            }
        } else if (Permissions.BYPASS_COST.grantedTo(player)) {
            ImageOnMap.getPlugin().getLogger().info("[NewCommand] " + player.getName() + " has bypass cost permission");
        }

        try {
            ImageOnMap.getPlugin().getLogger().info("[NewCommand] Starting render for " + player.getName());
            ActionBar.sendPermanentMessage(player, ChatColor.DARK_GREEN + I.t("Rendering..."));
            ImageRendererExecutor.render(url, scaling, player.getUniqueId(), width, height)
                    .exceptionallyAsync((exception) -> {
                        ImageOnMap.getPlugin().getLogger().log(Level.SEVERE, "[NewCommand] Rendering failed for " + player.getName(), exception);
                        player.sendMessage(I.t("{ce}Map rendering failed: {0}", exception.getMessage()));
                        return null;
                    })
                    .thenAccept(result -> {
                        ImageOnMap.getPlugin().getLogger().info("[NewCommand] Rendering completed for " + player.getName() + ", result=" + (result != null ? result.getId() : "null"));

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
                            player.sendMessage(I.t("{cs}Map created successfully!"));
                        } else if (inventoryFull && (result instanceof PosterMap && !((PosterMap) result).hasColumnData())) {
                            // Poster map was too big, parts need to be retrieved
                            ImageOnMap.getPlugin().getLogger().info("[NewCommand] Poster map too big for inventory: " + player.getName());
                            info(I.t("The rendered map was too big to fit in your inventory."));
                            info(I.t("Use '/maptool getremaining' to get the remaining maps."));
                        } else {
                            // Inventory was full
                            ImageOnMap.getPlugin().getLogger().warning("[NewCommand] Inventory full for " + player.getName());
                            player.sendMessage(I.t("{ce}Your inventory is full! Use '/maptool getremaining' to get your map."));
                        }
                    });
        } finally {
            ActionBar.removeMessage(player);
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
        try {
            // If width and height are specified, use them directly
            if (width > 0 && height > 0) {
                return width * height;
            }

            // Otherwise, download and check the image
            BufferedImage image;
            try (var stream = url.openStream()) {
                image = ImageIO.read(stream);
            }

            if (image == null) {
                return 0;
            }

            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            image.flush();

            // If resize mode is used and dimensions are 0, it becomes a single map
            if (scaling != ImageUtils.ScalingType.NONE && height <= 1 && width <= 1) {
                return 1;
            }

            // Calculate the number of 128x128 blocks needed
            int columns = (int) Math.ceil((double) imageWidth / 128.0);
            int rows = (int) Math.ceil((double) imageHeight / 128.0);

            return columns * rows;
        } catch (IOException e) {
            ImageOnMap.getPlugin().getLogger().warning("Failed to calculate map count for " + url + ": " + e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return Permissions.NEW.grantedTo(sender);
    }
}
