package net.bytemc.minestom.server.utils.text;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.color.DyeColor;

import java.awt.Color;
import java.util.Arrays;

public enum ByteColor {
    BLACK("§0", "<black>", NamedTextColor.BLACK, DyeColor.BLACK, true),
    DARK_BLUE("§1", "<dark_blue>", NamedTextColor.DARK_BLUE, DyeColor.BLUE, true),
    DARK_GREEN("§2", "<dark_green>", NamedTextColor.DARK_GREEN, DyeColor.GREEN, true),
    CYAN("§3", "<dark_aqua>", NamedTextColor.DARK_AQUA, DyeColor.CYAN, true),
    DARK_RED("§4", "<dark_red>", NamedTextColor.DARK_RED, DyeColor.RED, true),
    DARK_PURPLE("§5", "<dark_purple>", NamedTextColor.DARK_PURPLE, DyeColor.PURPLE, true),
    GOLD("§6", "<gold>", NamedTextColor.GOLD, DyeColor.ORANGE, true),
    GRAY("§7", "<gray>", NamedTextColor.GRAY, DyeColor.LIGHT_GRAY, true),
    DARK_GRAY("§8", "<dark_gray>", NamedTextColor.DARK_GRAY, DyeColor.GRAY, true),
    BLUE("§9", "<blue>", NamedTextColor.BLUE, DyeColor.BLUE, true),
    GREEN("§a", "<green>", NamedTextColor.GREEN, DyeColor.LIME, true),
    AQUA("§b", "<aqua>", NamedTextColor.AQUA, DyeColor.LIGHT_BLUE, true),
    RED("§c", "<red>", NamedTextColor.RED, DyeColor.RED, true),
    PINK("§d", "<purple>", NamedTextColor.LIGHT_PURPLE, DyeColor.PINK, true),
    YELLOW("§e", "<yellow>", NamedTextColor.YELLOW, DyeColor.YELLOW, true),
    WHITE("§f", "<white>", NamedTextColor.WHITE, DyeColor.WHITE, true),
    BOLD("§l", "<b>", null, null, false),
    ITALIC("§o", "<i>", null, null, false),
    UNDERLINED("§n", "<u>", null, null, false),
    OBFUSCATED("§k", "<obf>", null, null, false),
    STRIKETHROUGH("§m", "<st>", null, null, false),
    RESET("§r", "<reset>", null, null, false);

    private final String colorCode;
    private final String miniMessageTag;
    private final NamedTextColor textColor;
    private final DyeColor dyeColor;

    private final boolean color;

    ByteColor(String colorCode, String miniMessageTag, NamedTextColor textColor, DyeColor dyeColor, boolean color) {
        this.colorCode = colorCode;
        this.miniMessageTag = miniMessageTag;
        this.textColor = textColor;
        this.color = color;
        this.dyeColor = dyeColor;
    }

    public static ByteColor[] getAllColors() {
        return Arrays.stream(values())
                .filter(ByteColor::isColor)
                .toArray(ByteColor[]::new);
    }

    public static ByteColor[] getAllDecorations() {
        return Arrays.stream(values())
                .filter(byteColor -> !byteColor.isColor())
                .toArray(ByteColor[]::new);
    }

    public static ByteColor fromColorCode(String colorCode) {
        return Arrays.stream(values())
                .filter(byteColor -> byteColor.getColorCode().equals(colorCode.replace("&", "§")))
                .findFirst()
                .orElse(null);
    }

    public static ByteColor fromMiniMessageTag(String miniMessageTag) {
        return Arrays.stream(values())
                .filter(byteColor -> byteColor.getMiniMessageTag().equals(miniMessageTag))
                .findFirst()
                .orElse(null);
    }

    public static ByteColor fromTextColor(NamedTextColor textColor) {
        return Arrays.stream(values())
                .filter(byteColor -> byteColor.getTextColor().equals(textColor))
                .findFirst()
                .orElse(null);
    }

    public static String translateToMiniMessage(String input) {
        for (ByteColor value : values()) {
            input = input.replace(value.getColorCode(), value.getMiniMessageTag());
        }

        return input;
    }

    public boolean isColor() {
        return color;
    }

    public NamedTextColor getTextColor() {
        return textColor;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getMiniMessageTag() {
        return miniMessageTag;
    }

    public String getMiniMessageClosingTag() {
        return "</" + this.miniMessageTag.substring(1);
    }

    public Color toColor() {
        NamedTextColor textColor = this.textColor;
        return new Color(textColor.red(), textColor.green(), textColor.blue());
    }
}

