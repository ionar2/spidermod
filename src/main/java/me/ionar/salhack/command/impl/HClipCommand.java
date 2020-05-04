package me.ionar.salhack.command.impl;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.ionar.salhack.command.Command;
import me.ionar.salhack.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class HClipCommand extends Command
{
    public HClipCommand()
    {
        super("HClip", "Allows you to hclip x blocks");
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

        final Vec3d l_Direction = MathUtil.direction(mc.player.rotationYaw);
        
        if (l_Direction != null)
        {
            Entity l_Entity = mc.player.isRiding() ? mc.player.getRidingEntity() : mc.player;
            
            l_Entity.setPosition(mc.player.posX + l_Direction.x*l_Number, mc.player.posY, mc.player.posZ + l_Direction.z*l_Number);
            
            SendToChat(String.format("Teleported you %s blocks forward", l_Number));
        }
    }
    
    @Override
    public String GetHelp()
    {
        return "Allows you teleport forward x amount of blocks.";
    }
}
