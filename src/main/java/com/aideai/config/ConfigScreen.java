package com.aideai.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private EditBox apiKeyField;
    private EditBox modelField;
    private EditBox temperatureField;
    private EditBox maxTokensField;
    
    private String apiKey;
    private String model;
    private String temperature;
    private String maxTokens;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("config.aideai.title"));
        this.parent = parent;
        this.apiKey = ModConfig.API_KEY.get();
        this.model = ModConfig.MODEL.get();
        this.temperature = String.valueOf(ModConfig.TEMPERATURE.get());
        this.maxTokens = String.valueOf(ModConfig.MAX_TOKENS.get());
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        
        addRenderableWidget(Button.builder(
            Component.translatable("config.aideai.api_key"),
            btn -> {}
        ).bounds(centerX - 150, 30, 120, 20).build());
        
        apiKeyField = new EditBox(this.font, centerX - 20, 30, 170, 20, 
            Component.translatable("config.aideai.api_key"));
        apiKeyField.setValue(apiKey);
        apiKeyField.setMaxLength(256);
        addRenderableWidget(apiKeyField);
        
        addRenderableWidget(Button.builder(
            Component.translatable("config.aideai.model"),
            btn -> {}
        ).bounds(centerX - 150, 60, 120, 20).build());
        
        modelField = new EditBox(this.font, centerX - 20, 60, 170, 20,
            Component.translatable("config.aideai.model"));
        modelField.setValue(model);
        modelField.setMaxLength(64);
        addRenderableWidget(modelField);
        
        addRenderableWidget(Button.builder(
            Component.translatable("config.aideai.temperature"),
            btn -> {}
        ).bounds(centerX - 150, 90, 120, 20).build());
        
        temperatureField = new EditBox(this.font, centerX - 20, 90, 170, 20,
            Component.translatable("config.aideai.temperature"));
        temperatureField.setValue(temperature);
        addRenderableWidget(temperatureField);
        
        addRenderableWidget(Button.builder(
            Component.translatable("config.aideai.max_tokens"),
            btn -> {}
        ).bounds(centerX - 150, 120, 120, 20).build());
        
        maxTokensField = new EditBox(this.font, centerX - 20, 120, 170, 20,
            Component.translatable("config.aideai.max_tokens"));
        maxTokensField.setValue(maxTokens);
        addRenderableWidget(maxTokensField);
        
        addRenderableWidget(Button.builder(
            Component.literal("保存"),
            btn -> saveConfig()
        ).bounds(centerX - 50, 170, 100, 20).build());
        
        addRenderableWidget(Button.builder(
            Component.literal("返回"),
            btn -> onClose()
        ).bounds(centerX - 50, 200, 100, 20).build());
    }

    private void saveConfig() {
        ModConfig.API_KEY.set(apiKeyField.getValue());
        ModConfig.MODEL.set(modelField.getValue());
        try {
            double temp = Double.parseDouble(temperatureField.getValue());
            ModConfig.TEMPERATURE.set(Math.max(0.0, Math.min(2.0, temp)));
        } catch (NumberFormatException ignored) {}
        try {
            int tokens = Integer.parseInt(maxTokensField.getValue());
            ModConfig.MAX_TOKENS.set(Math.max(64, Math.min(8192, tokens)));
        } catch (NumberFormatException ignored) {}
        
        ModConfig.SPEC.save();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
