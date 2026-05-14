package dev.isxander.zoomify.config;

import net.minecraft.src.EnumOptions;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSlider;
import net.minecraft.src.Minecraft;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ZoomifyConfigScreen extends GuiScreen {
    private static final int DONE = 0;
    private static final int PREV = 1;
    private static final int NEXT = 2;
    private static final int FIRST_OPTION = 100;

    private final GuiScreen parent;
    private final List<Page> pages = new ArrayList<>();
    private int pageIndex;

    public ZoomifyConfigScreen(GuiScreen parent) {
        this.parent = parent;
        createPages();
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        int optionWidth = Math.min(300, this.width - 40);
        this.buttonList.add(new GuiButton(PREV, this.width / 2 - 140, this.height - 27, 50, 20, "<"));
        this.buttonList.add(new GuiButton(DONE, this.width / 2 - 80, this.height - 27, 160, 20, "Done"));
        this.buttonList.add(new GuiButton(NEXT, this.width / 2 + 90, this.height - 27, 50, 20, ">"));

        Page page = pages.get(pageIndex);
        int y = 42;
        int id = FIRST_OPTION;
        for (OptionRow row : page.rows) {
            this.buttonList.add(row.createButton(id++, this.width / 2 - optionWidth / 2, y, optionWidth, 20));
            y += 24;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == DONE) {
            ZoomifySettings.INSTANCE.save();
            this.mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == PREV) {
            pageIndex = (pageIndex + pages.size() - 1) % pages.size();
            initGui();
            return;
        }
        if (button.id == NEXT) {
            pageIndex = (pageIndex + 1) % pages.size();
            initGui();
            return;
        }

        int rowIndex = button.id - FIRST_OPTION;
        Page page = pages.get(pageIndex);
        if (rowIndex >= 0 && rowIndex < page.rows.size()) {
            page.rows.get(rowIndex).click(1);
            ZoomifySettings.INSTANCE.save();
            button.displayString = page.rows.get(rowIndex).buttonText();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 1) {
            Page page = pages.get(pageIndex);
            for (Object object : this.buttonList) {
                GuiButton guiButton = (GuiButton) object;
                int rowIndex = guiButton.id - FIRST_OPTION;
                if (rowIndex >= 0 && rowIndex < page.rows.size()
                        && page.rows.get(rowIndex).supportsBackwardClick()
                        && guiButton.mousePressed(this.mc, mouseX, mouseY)) {
                    this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
                    page.rows.get(rowIndex).click(-1);
                    ZoomifySettings.INSTANCE.save();
                    guiButton.displayString = page.rows.get(rowIndex).buttonText();
                    return;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float delta) {
        this.drawDefaultBackground();
        Page page = pages.get(pageIndex);
        this.drawCenteredString(this.fontRenderer, "Zoomify", this.width / 2, 8, 0xffffff);
        this.drawCenteredString(this.fontRenderer, page.title + " (" + (pageIndex + 1) + "/" + pages.size() + ")", this.width / 2, 22, 0xa0a0a0);
        super.drawScreen(mouseX, mouseY, delta);
    }

    @Override
    public void onGuiClosed() {
        ZoomifySettings.INSTANCE.save();
    }

    private void createPages() {
        Page behaviour = new Page("Behavior");
        behaviour.rows.add(new IntOption("Initial Zoom", () -> ZoomifySettings.INSTANCE.initialZoom, v -> ZoomifySettings.INSTANCE.initialZoom = v, 1, 10, 1, v -> v + "x"));
        behaviour.rows.add(new DoubleOption("Zoom In Time", () -> ZoomifySettings.INSTANCE.zoomInTime, v -> ZoomifySettings.INSTANCE.zoomInTime = v, 0.1D, 5.0D, 0.1D, v -> seconds(v)));
        behaviour.rows.add(new DoubleOption("Zoom Out Time", () -> ZoomifySettings.INSTANCE.zoomOutTime, v -> ZoomifySettings.INSTANCE.zoomOutTime = v, 0.1D, 5.0D, 0.1D, v -> seconds(v)));
        behaviour.rows.add(new EnumOption<>("Zoom In Transition", () -> ZoomifySettings.INSTANCE.zoomInTransition, v -> ZoomifySettings.INSTANCE.zoomInTransition = v, TransitionType.values()));
        behaviour.rows.add(new EnumOption<>("Zoom Out Transition", () -> ZoomifySettings.INSTANCE.zoomOutTransition, v -> ZoomifySettings.INSTANCE.zoomOutTransition = v, TransitionType.values()));
        behaviour.rows.add(new BooleanOption("Affect Hand FOV", () -> ZoomifySettings.INSTANCE.affectHandFov, v -> ZoomifySettings.INSTANCE.affectHandFov = v));
        pages.add(behaviour);

        Page scrolling = new Page("Scrolling");
        scrolling.rows.add(new BooleanOption("Enable Scroll Zoom", () -> ZoomifySettings.INSTANCE.scrollZoom, v -> ZoomifySettings.INSTANCE.scrollZoom = v));
        scrolling.rows.add(new IntOption("Scroll Step Count", () -> ZoomifySettings.INSTANCE.scrollStepCount, v -> ZoomifySettings.INSTANCE.scrollStepCount = v, 3, 20, 1, String::valueOf));
        scrolling.rows.add(new IntOption("Zoom Per Step", () -> ZoomifySettings.INSTANCE.zoomPerStep, v -> ZoomifySettings.INSTANCE.zoomPerStep = v, 110, 200, 10, v -> String.format("%.1fx", v / 100.0D)));
        scrolling.rows.add(new IntOption("Scroll Zoom Smoothness", () -> ZoomifySettings.INSTANCE.scrollZoomSmoothness, v -> ZoomifySettings.INSTANCE.scrollZoomSmoothness = v, 0, 100, 10, v -> v == 0 ? "Instant" : v + "%"));
        scrolling.rows.add(new BooleanOption("Remember Zoom Steps", () -> ZoomifySettings.INSTANCE.retainZoomSteps, v -> ZoomifySettings.INSTANCE.retainZoomSteps = v));
        pages.add(scrolling);

        Page controls = new Page("Controls");
        controls.rows.add(new EnumOption<>("Zoom Key Behavior", () -> ZoomifySettings.INSTANCE.zoomKeyBehaviour, v -> ZoomifySettings.INSTANCE.zoomKeyBehaviour = v, ZoomKeyBehaviour.values()));
        controls.rows.add(new BooleanOption("Keybind Scrolling (Restart)", () -> ZoomifySettings.INSTANCE.keybindScrolling, v -> ZoomifySettings.INSTANCE.keybindScrolling = v));
        controls.rows.add(new IntOption("Relative Sensitivity", () -> ZoomifySettings.INSTANCE.relativeSensitivity, v -> ZoomifySettings.INSTANCE.relativeSensitivity = v, 0, 150, 10, v -> v == 0 ? "Off" : v + "%"));
        controls.rows.add(new BooleanOption("Relative View Bobbing", () -> ZoomifySettings.INSTANCE.relativeViewBobbing, v -> ZoomifySettings.INSTANCE.relativeViewBobbing = v));
        controls.rows.add(new IntOption("Cinematic Camera", () -> ZoomifySettings.INSTANCE.cinematicCamera, v -> ZoomifySettings.INSTANCE.cinematicCamera = v, 0, 250, 10, v -> v == 0 ? "Off" : v + "%"));
        pages.add(controls);

        Page secondary = new Page("Secondary Zoom");
        secondary.rows.add(new IntOption("Zoom Amount", () -> ZoomifySettings.INSTANCE.secondaryZoomAmount, v -> ZoomifySettings.INSTANCE.secondaryZoomAmount = v, 2, 10, 1, v -> v + "x"));
        secondary.rows.add(new DoubleOption("Zoom In Time", () -> ZoomifySettings.INSTANCE.secondaryZoomInTime, v -> ZoomifySettings.INSTANCE.secondaryZoomInTime = v, 6.0D, 30.0D, 2.0D, v -> seconds(v)));
        secondary.rows.add(new DoubleOption("Zoom Out Time", () -> ZoomifySettings.INSTANCE.secondaryZoomOutTime, v -> ZoomifySettings.INSTANCE.secondaryZoomOutTime = v, 0.0D, 5.0D, 0.25D, v -> v == 0.0D ? "Instant" : seconds(v)));
        secondary.rows.add(new BooleanOption("Hide HUD On Zoom", () -> ZoomifySettings.INSTANCE.secondaryHideHUDOnZoom, v -> ZoomifySettings.INSTANCE.secondaryHideHUDOnZoom = v));
        pages.add(secondary);

        Page presets = new Page("Presets");
        presets.rows.add(new ActionOption("Default", () -> ZoomifySettings.INSTANCE.resetDefaults()));
        presets.rows.add(new ActionOption("OptiFine", () -> ZoomifySettings.INSTANCE.applyOptifinePreset()));
        presets.rows.add(new ActionOption("Ok Zoomer", () -> ZoomifySettings.INSTANCE.applyOkZoomerPreset()));
        pages.add(presets);
    }

    private static String seconds(double value) {
        return String.format("%.2fs", value);
    }

    private static String enumName(Enum<?> value) {
        String text = value.name().toLowerCase().replace('_', ' ');
        StringBuilder builder = new StringBuilder(text.length());
        boolean upper = true;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (upper && c >= 'a' && c <= 'z') {
                builder.append((char) (c - 32));
            } else {
                builder.append(c);
            }
            upper = c == ' ';
        }
        return builder.toString();
    }

    private static final class Page {
        private final String title;
        private final List<OptionRow> rows = new ArrayList<>();

        private Page(String title) {
            this.title = title;
        }
    }

    private interface OptionRow {
        String buttonText();

        default GuiButton createButton(int id, int x, int y, int width, int height) {
            return new GuiButton(id, x, y, width, height, buttonText());
        }

        default boolean supportsBackwardClick() {
            return false;
        }

        void click(int direction);
    }

    private interface IntGetter {
        int get();
    }

    private interface IntSetter {
        void set(int value);
    }

    private interface IntFormatter {
        String format(int value);
    }

    private static final class IntOption implements OptionRow {
        private final String name;
        private final IntGetter getter;
        private final IntSetter setter;
        private final int min;
        private final int max;
        private final int step;
        private final IntFormatter formatter;

        private IntOption(String name, IntGetter getter, IntSetter setter, int min, int max, int step, IntFormatter formatter) {
            this.name = name;
            this.getter = getter;
            this.setter = setter;
            this.min = min;
            this.max = max;
            this.step = step;
            this.formatter = formatter;
        }

        @Override
        public String buttonText() {
            return name + ": " + formatter.format(getter.get());
        }

        @Override
        public GuiButton createButton(int id, int x, int y, int width, int height) {
            return new SliderButton(id, x, y, width, height, this::buttonText, () -> (getter.get() - min) / (double) (max - min), value -> {
                int steps = (int) Math.round((min + value * (max - min)) / (double) step);
                int next = steps * step;
                setter.set(Math.max(min, Math.min(max, next)));
            });
        }

        @Override
        public void click(int direction) {
        }
    }

    private interface DoubleGetter {
        double get();
    }

    private interface DoubleSetter {
        void set(double value);
    }

    private interface DoubleFormatter {
        String format(double value);
    }

    private static final class DoubleOption implements OptionRow {
        private final String name;
        private final DoubleGetter getter;
        private final DoubleSetter setter;
        private final double min;
        private final double max;
        private final double step;
        private final DoubleFormatter formatter;

        private DoubleOption(String name, DoubleGetter getter, DoubleSetter setter, double min, double max, double step, DoubleFormatter formatter) {
            this.name = name;
            this.getter = getter;
            this.setter = setter;
            this.min = min;
            this.max = max;
            this.step = step;
            this.formatter = formatter;
        }

        @Override
        public String buttonText() {
            return name + ": " + formatter.format(getter.get());
        }

        @Override
        public GuiButton createButton(int id, int x, int y, int width, int height) {
            return new SliderButton(id, x, y, width, height, this::buttonText, () -> (getter.get() - min) / (max - min), value -> {
                double raw = min + value * (max - min);
                double next = Math.round(raw / step) * step;
                setter.set(Math.max(min, Math.min(max, next)));
            });
        }

        @Override
        public void click(int direction) {
        }
    }

    private interface BooleanGetter {
        boolean get();
    }

    private interface BooleanSetter {
        void set(boolean value);
    }

    private static final class BooleanOption implements OptionRow {
        private final String name;
        private final BooleanGetter getter;
        private final BooleanSetter setter;

        private BooleanOption(String name, BooleanGetter getter, BooleanSetter setter) {
            this.name = name;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public String buttonText() {
            return name + ": " + (getter.get() ? "On" : "Off");
        }

        @Override
        public void click(int direction) {
            setter.set(!getter.get());
        }
    }

    private interface EnumGetter<E extends Enum<E>> {
        E get();
    }

    private interface EnumSetter<E extends Enum<E>> {
        void set(E value);
    }

    private static final class EnumOption<E extends Enum<E>> implements OptionRow {
        private final String name;
        private final EnumGetter<E> getter;
        private final EnumSetter<E> setter;
        private final E[] values;

        private EnumOption(String name, EnumGetter<E> getter, EnumSetter<E> setter, E[] values) {
            this.name = name;
            this.getter = getter;
            this.setter = setter;
            this.values = values;
        }

        @Override
        public String buttonText() {
            String text = name + ": " + enumName(getter.get());
            if (values.length > 3) {
                text += " (" + (selectedIndex() + 1) + "/" + values.length + ")";
            }
            return text;
        }

        @Override
        public void click(int direction) {
            setter.set(values[(selectedIndex() + values.length + direction) % values.length]);
        }

        @Override
        public boolean supportsBackwardClick() {
            return true;
        }

        private int selectedIndex() {
            E current = getter.get();
            for (int i = 0; i < values.length; i++) {
                if (values[i] == current) {
                    return i;
                }
            }
            return 0;
        }
    }

    private static final class ActionOption implements OptionRow {
        private final String name;
        private final Runnable action;

        private ActionOption(String name, Runnable action) {
            this.name = name;
            this.action = action;
        }

        @Override
        public String buttonText() {
            return "Apply " + name;
        }

        @Override
        public void click(int direction) {
            if (direction > 0) {
                action.run();
            }
        }
    }

    private interface StringSupplier {
        String get();
    }

    private interface DoubleValueGetter {
        double get();
    }

    private interface DoubleValueSetter {
        void set(double value);
    }

    private static final class SliderButton extends GuiSlider {
        private final StringSupplier label;
        private final DoubleValueGetter getter;
        private final DoubleValueSetter setter;

        private SliderButton(int id, int x, int y, int width, int height, StringSupplier label, DoubleValueGetter getter, DoubleValueSetter setter) {
            super(id, x, y, EnumOptions.FOV, label.get(), (float) Math.max(0.0D, Math.min(1.0D, getter.get())));
            this.width = width;
            this.height = height;
            this.label = label;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
            if (this.enabled && this.drawButton
                    && mouseX >= this.xPosition
                    && mouseY >= this.yPosition
                    && mouseX < this.xPosition + this.width
                    && mouseY < this.yPosition + this.height) {
                this.dragging = true;
                setFromMouse(mouseX);
                return true;
            }
            return false;
        }

        @Override
        protected void mouseDragged(Minecraft minecraft, int mouseX, int mouseY) {
            if (this.dragging) {
                setFromMouse(mouseX);
            }
            this.sliderValue = (float) Math.max(0.0D, Math.min(1.0D, getter.get()));
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            int knobX = this.xPosition + (int) (this.sliderValue * (this.width - 8));
            this.drawTexturedModalRect(knobX, this.yPosition, 0, 66, 4, 20);
            this.drawTexturedModalRect(knobX + 4, this.yPosition, 196, 66, 4, 20);
        }

        @Override
        public void mouseReleased(int mouseX, int mouseY) {
            this.dragging = false;
        }

        private void setFromMouse(int mouseX) {
            double value = (mouseX - (this.xPosition + 4)) / (double) (this.width - 8);
            setter.set(Math.max(0.0D, Math.min(1.0D, value)));
            this.displayString = label.get();
        }
    }
}
