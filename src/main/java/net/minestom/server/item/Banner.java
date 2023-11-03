package net.minestom.server.item;

import net.minestom.server.color.DyeColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public final class Banner {

    private final Instance instance;
    private final Pos position;

    private DyeColor color = DyeColor.WHITE;
    private final List<Pattern> patterns = new ArrayList<>();

    public Banner(Instance instance, Pos position) {
        this.instance = instance;
        this.position = position;
    }

    public Banner resetPatterns() {
        this.patterns.clear();
        return this;
    }

    public Banner addPattern(Pattern pattern) {
        this.patterns.add(pattern);
        return this;
    }

    public void update() {
        try {
            instance.setBlock(position, instance.getBlock(position).withNbt((NBTCompound) new SNBTParser(new StringReader("{\"Patterns\": [" + this.getPattern() + "]}")).parse()));
        } catch (NBTException e) {
            throw new RuntimeException(e);
        }
    }

    public void setColor(DyeColor color, boolean update) {
        this.color = color;

        if(update) {
            this.update();
        }
    }

    private String getPattern() {
        return String.join(", ", patterns.stream().map(it -> "{\"Pattern\": \"" + it.getCode() + "\", \"Color\": " + color.mapColorId() + "}").toList());
    }

    public enum Pattern {
        BASE("b"),
        BOTTOM_STRIPE("bs"),
        TOP_STRIPE("ts"),
        LEFT_STRIPE("ls"),
        RIGHT_STRIPE("rs"),
        CENTER_STRIPE_VERTOCAL("cs"),
        MIDDLE_STRIPE_HORIZONTAL("ms"),
        DOWN_RIGHT_STRIPE("drs"),
        DOWN_LEFT_STRIPE("dls"),
        SMALL_VERTICAL_STRIPES("ss"),
        DIAGONAL_CROSS("cr"),
        SQUARE_CROSS("sc"),
        LEFT_DIAGONAL("ld"),
        RIGHT_UPSIDE_DOWN_DIAGONAL("rud"),
        LEFT_UPSIDE_DOWN_DIAGONAL("lud"),
        RIGHT_DIAGONAL("rd"),
        VERTICAL_HALF_LEFT("vh"),
        VERTICAL_HALF_RIGHT("vhr"),
        HORIZONTAL_HALF_TOP("hh"),
        HORIZONTAL_HALF_BOTTOM("hhb"),
        BOTTOM_LEFT_CORNER("bl"),
        BOTTOM_RIGHT_CORNER("br"),
        TOP_LEFT_CORNER("tl"),
        TOP_RIGHT_CORNER("tr"),
        BOTTOM_TRIANGLE("bt"),
        TOP_TRIANGLE("tt"),
        BOTTOM_TRIANGLE_SAWTOOTH("bts"),
        TOP_TRIANGLE_SAWTOOTH("tts"),
        MIDDLE_CIRCLE("mc"),
        MIDDLE_RHOMBUS("mr"),
        BORDER("bo"),
        CURLY_BORDER("cbo"),
        BRICK("bri"),
        GRADIENT("gra"),
        GRADIENT_UPSIDE_DOWN("gru"),
        CREEPER("cre"),
        SKULL("sku"),
        FLOWER("flo"),
        MOJANG("moj"),
        GLOBE("glb"),
        PIGLIN("pig");

        private final String code;

        Pattern(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}
