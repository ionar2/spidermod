package me.ionar.salhack.util.entity;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ItemUtil
{
    public static boolean Is32k(ItemStack p_Stack)
    {
        if (p_Stack.getEnchantmentTagList() != null)
        {
            final NBTTagList tags = p_Stack.getEnchantmentTagList();
            for (int i = 0; i < tags.tagCount(); i++)
            {
                final NBTTagCompound tagCompound = tags.getCompoundTagAt(i);
                if (tagCompound != null && Enchantment.getEnchantmentByID(tagCompound.getByte("id")) != null)
                {
                    final Enchantment enchantment = Enchantment.getEnchantmentByID(tagCompound.getShort("id"));
                    final short lvl = tagCompound.getShort("lvl");
                    if (enchantment != null)
                    {
                        if (enchantment.isCurse())
                            continue;
                        
                        if (lvl >= 1000)
                            return true;
                    }
                }
            }
        }
        return false;
    }
}
