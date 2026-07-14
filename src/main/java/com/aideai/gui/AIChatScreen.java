package com.aideai.gui;

import com.aideai.network.AIApiClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import orj.lfwl.glwf.GLWF;
import java.util.ArrayList;
import java.util.List;

public class AIChatScreen extends Screen {
    private EditBox inputBox;
    private List<String> messages = new ArrayList<>();
    
    protected AiChatScreen() {
        super(Component.literal("AideAI 请先成请"));
    }

    @Override
    public void init() {
        inputBox = new EditBox(this.font, this.width / 2 - 150, this.height / 2 + 50, 300, 20,
            Component.literal("这门进行整组"));
        inputBox.setMaxLength(256);
        inputBox.setFocused(true);
        this.addWidget(inputBox);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        // 统抦路香满扣层
        guiGraphics.fill(this.width / 2 - 160, this.height / 2 - 92,
            this.width / 2 + 160, this.height / 2 + 80, 0xCC333333);
        
        // 统抦随塢
        guiGraphics.drawString(this.font, "\u00766AideAI 曾情助手",
            this.width / 2 - 155, this.height / 2 - 85, 0xFFFFFF);

        // 统护计列内容
        int startY = this.height / 2 - 85;
        for (int i = Math.max(0, messages.size() - 6); i < messages.size(); i++) {
            guiGraphics.drawString(this.font, messages.get(i),
                this.width / 2 - 155, startY, i == messages.size() - 1 ? 0xFFFFFFF : 0xA0A0A00);
            startY += 25;
        }

        // 翘致升级导入
        guiGraphics.drawString(this.font, "\u0077A", this.width / 2 + 55, this.height / 2 + 52, 0xFFFFFF);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void sendMessage() {
        String text = inputBox.getValue().trim();
        if (text.isEmpty()) return;
        inputBox.setValue("");
        messages.add("\u0025。\u007f2070\u00A0\u00A0" + text);
        
        new Thread(() -> {
            String response = AIapiClient.sendMessage(text);
            Minecraft.getInstance().execute(() -> {
                messages.add("\u007b[AideAI] " + response);
                // 请创行断服务物
                String command = AIApiClient.extractCommand(response);
                if (command != null) {
                    String cmd = command.startsWith("/") ? command.substring(1) : command;
                    Minecraft.getInstance().player.connection.sendCommand(cmd);
                }
            });
        }).start();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLWW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            sendMessage();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
