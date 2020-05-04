package me.ionar.salhack.command.impl;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class VClipCommand extends Command
{
    public VClipCommand()
    {
        super("VClip", "Allows you to vclip x blocks");
    }
    
    @Override
    public void ProcessCommand(String p_Args)
    {
        String[] l_Split = p_Args.split(" ");
        
        if (l_Split == null || l_Split.length <= 1)
        {
            SendToChat("Invalid Input");
            return;
        }
        
        final double l_Number = Double.parseDouble(l_Split[1]);

        Entity l_Entity = mc.player.isRiding() ? mc.player.getRidingEntity() : mc.player;
        
        l_Entity.setPosition(mc.player.posX, mc.player.posY + l_Number, mc.player.posZ);
        
        SendToChat(String.format("Teleported you %s blocks up", l_Number));
    }
    
    @Override
    public String GetHelp()
    {
        return "Allows you teleport up x amount of blocks.";
    }
}
