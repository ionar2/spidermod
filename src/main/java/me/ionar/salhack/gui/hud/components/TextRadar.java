package me.ionar.salhack.gui.hud.components;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.main.Wrapper;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.render.ChatColor;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

// Made by Jumpinqq on 9/12/20

public class TextRadar extends HudComponentItem {
    private enum DisplayMode {
        Everyone,
        Friends,
        NonFriends
    }
    public final Value<DisplayMode> display = new Value<>("Display", new String[]{ "" }, "NoFall Module to use", DisplayMode.Everyone);
    public final Value<Integer> max = new Value<>("MaxLines", new String[] {""}, "Max number of players listed.", 18, 0, 50, 1);
    public final Value<Integer> spacing = new Value<>("Spacing", new String[] {""}, "Amount of space between each line.", 0, 0, 5, 1);

    public TextRadar() {
        super("TextRadar", 3, 175);
    }

    List<EntityPlayer> pList = new ArrayList<>();

    @Override
    public void render(int p_MouseX, int p_MouseY, float p_PartialTicks) {
        super.render(p_MouseX, p_MouseY, p_PartialTicks);

        for (EntityPlayer p : mc.world.playerEntities) {
            if (p instanceof EntityPlayerSP) continue;

            if (pList.size() < max.getValue() && !pList.contains(p) && !invalidType(p)) {
                pList.add(p);
            }
        }
        if (pList.size() > max.getValue() && pList.size() != 0) {
            pList.remove(pList.size() - 1);
        }

        ArrayList<String> text = new ArrayList<>();
        if (pList != null) {
            for (int i = 0; i < pList.size(); i++) {
                EntityPlayer p = pList.get(i);

                // Makes sure that the player in question should still be listed.
                if (!p.isInRangeToRender3d(mc.player.posX, mc.player.posY, mc.player.posZ) || p.isDead || invalidType(p)) {
                    pList.remove(p);
                    continue;
                }
                // Builds the line for each player listed.
                ChatColor color = FriendManager.Get().IsFriend(p) ? ChatColor.AQUA : ChatColor.GRAY;
                StringBuilder line = new StringBuilder();
                line.append(ChatColor.DARK_GRAY).append("- ").append(color).append(p.getName()).append(ChatColor.DARK_GRAY).append(" [");

                double pHealth = Math.floor(p.getHealth() + p.getAbsorptionAmount());
                color = healthToColor(pHealth);
                line.append(color).append(pHealth).append(ChatColor.DARK_GRAY).append("]\n");

                text.add(line.toString());
            }
        }
        // Doing it this way, rather than rendering one big string, gives the user the option to change spacing between each line.
        for (int i = 0; i < text.size(); i++) {
            Wrapper.GetMC().fontRenderer.drawStringWithShadow(text.get(i).replace("\n", ""), GetX(), (GetY()) + (i * (10 + spacing.getValue())), -1);
        }

        SetWidth(RenderUtil.getStringWidth(text.toString() + 80));

        int num = 12 - (10 + spacing.getValue());
        SetHeight(RenderUtil.getStringHeight(text.toString()) - (num * pList.size()) - 20);
    }

    private ChatColor healthToColor(double health) {
        // java pls support ranges with switch statements :^(
        if (health <= 6) return ChatColor.RED;
        else if (health < 18) return ChatColor.YELLOW;
        else if (health >= 18) return ChatColor.GREEN;

        return ChatColor.RESET;
    }

    private boolean invalidType(EntityPlayer p) {
        if (display.getValue() == DisplayMode.Everyone) {
            return false;
        }
        return (display.getValue() == DisplayMode.Friends && !FriendManager.Get().IsFriend(p)) || ((display.getValue() == DisplayMode.NonFriends && FriendManager.Get().IsFriend(p)));
    }
}