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

package fr.moribus.imageonmap.locale;

import fr.moribus.imageonmap.ImageOnMap;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.logging.Level;

/**
 * Manages locale files for user-translatable messages
 */
public class LocaleManager {
    private static FileConfiguration messages = null;
    private static String currentLocale = "en-US";

    /**
     * Initializes the locale system with the specified locale
     * @param locale The locale to use (e.g., "en-US", "ja-JP")
     */
    public static void init(String locale) {
        currentLocale = locale;

        // Create locale directory if it doesn't exist
        File localeDir = new File(ImageOnMap.getPlugin().getDataFolder(), "locale");
        if (!localeDir.exists()) {
            localeDir.mkdirs();
        }

        // Copy default locale file if it doesn't exist
        File defaultLocaleFile = new File(localeDir, "message_en-US.yml");
        if (!defaultLocaleFile.exists()) {
            saveDefaultLocaleFile("message_en-US.yml");
        }

        // Load the requested locale file
        File localeFile = new File(localeDir, "message_" + locale + ".yml");
        if (!localeFile.exists()) {
            ImageOnMap.getPlugin().getLogger().warning(
                "Locale file message_" + locale + ".yml not found. Using default en-US."
            );
            localeFile = defaultLocaleFile;
            currentLocale = "en-US";
        }

        messages = YamlConfiguration.loadConfiguration(localeFile);
        ImageOnMap.getPlugin().getLogger().info("Loaded locale: " + currentLocale);
    }

    /**
     * Saves the default locale file from the plugin jar
     * @param fileName The locale file name
     */
    private static void saveDefaultLocaleFile(String fileName) {
        File localeDir = new File(ImageOnMap.getPlugin().getDataFolder(), "locale");
        File localeFile = new File(localeDir, fileName);

        if (!localeFile.exists()) {
            try (InputStream in = ImageOnMap.getPlugin().getResource("locale/" + fileName)) {
                if (in != null) {
                    Files.copy(in, localeFile.toPath());
                    ImageOnMap.getPlugin().getLogger().info("Created default locale file: " + fileName);
                } else {
                    ImageOnMap.getPlugin().getLogger().warning(
                        "Could not find default locale file in jar: " + fileName
                    );
                }
            } catch (IOException e) {
                ImageOnMap.getPlugin().getLogger().log(Level.SEVERE,
                    "Failed to save default locale file: " + fileName, e);
            }
        }
    }

    /**
     * Gets a message from the locale file
     * @param key The message key (e.g., "economy.invalid-url")
     * @return The message, or the key if not found
     */
    public static String getMessage(String key) {
        if (messages == null) {
            return key;
        }

        String message = messages.getString(key);
        if (message == null) {
            ImageOnMap.getPlugin().getLogger().warning(
                "Missing locale key '" + key + "' in locale " + currentLocale
            );
            return key;
        }

        // Replace formatting codes
        return replaceFormattingCodes(message);
    }

    /**
     * Gets a message with placeholder replacements
     * @param key The message key
     * @param args Arguments to replace {0}, {1}, etc.
     * @return The formatted message
     */
    public static String getMessage(String key, Object... args) {
        if (messages == null) {
            return key;
        }

        String message = messages.getString(key);
        if (message == null) {
            ImageOnMap.getPlugin().getLogger().warning(
                "Missing locale key '" + key + "' in locale " + currentLocale
            );
            return key;
        }

        // First, replace formatting codes like {cs}, {ce}, etc.
        message = replaceFormattingCodes(message);

        // Then, replace placeholders like {0}, {1}, etc.
        if (args.length > 0) {
            try {
                // Escape single quotes for MessageFormat
                message = message.replace("'", "''");
                return MessageFormat.format(message, args);
            } catch (IllegalArgumentException e) {
                ImageOnMap.getPlugin().getLogger().warning(
                    "Failed to format message '" + key + "': " + e.getMessage()
                );
                return message;
            }
        }
        return message;
    }

    /**
     * Replaces formatting codes like {cs}, {ce} with actual color codes
     * Based on I18n.replaceFormattingCodes()
     * @param text The text to process
     * @return The text with color codes replaced
     */
    private static String replaceFormattingCodes(String text) {
        return text.replace("{black}", ChatColor.BLACK.toString())
                .replace("{darkblue}", ChatColor.DARK_BLUE.toString())
                .replace("{darkgreen}", ChatColor.DARK_GREEN.toString())
                .replace("{darkaqua}", ChatColor.DARK_AQUA.toString())
                .replace("{darkred}", ChatColor.DARK_RED.toString())
                .replace("{darkpurple}", ChatColor.DARK_PURPLE.toString())
                .replace("{gold}", ChatColor.GOLD.toString())
                .replace("{gray}", ChatColor.GRAY.toString())
                .replace("{darkgray}", ChatColor.DARK_GRAY.toString())
                .replace("{blue}", ChatColor.BLUE.toString())
                .replace("{green}", ChatColor.GREEN.toString())
                .replace("{aqua}", ChatColor.AQUA.toString())
                .replace("{red}", ChatColor.RED.toString())
                .replace("{lightpurple}", ChatColor.LIGHT_PURPLE.toString())
                .replace("{yellow}", ChatColor.YELLOW.toString())
                .replace("{white}", ChatColor.WHITE.toString())
                .replace("{bold}", ChatColor.BOLD.toString())
                .replace("{strikethrough}", ChatColor.STRIKETHROUGH.toString())
                .replace("{underline}", ChatColor.UNDERLINE.toString())
                .replace("{italic}", ChatColor.ITALIC.toString())
                .replace("{obfuscated}", ChatColor.MAGIC.toString())
                .replace("{reset}", ChatColor.RESET.toString())
                // Shorthand color codes
                .replace("{ce}", ChatColor.RED.toString())        // error
                .replace("{cc}", ChatColor.GOLD.toString())       // command
                .replace("{ci}", ChatColor.WHITE.toString())      // info/notice
                .replace("{cs}", ChatColor.GREEN.toString())      // success
                .replace("{cst}", ChatColor.GRAY.toString());     // status
    }

    /**
     * Reloads the locale file (useful for /reload command)
     */
    public static void reload() {
        if (currentLocale != null) {
            init(currentLocale);
        }
    }

    /**
     * Gets the current locale
     * @return The current locale (e.g., "en-US")
     */
    public static String getCurrentLocale() {
        return currentLocale;
    }
}
