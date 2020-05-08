package me.ionar.salhack.managers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;

import com.google.gson.internal.LinkedTreeMap;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.main.Wrapper;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Module.ModuleType;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.bot.*;
import me.ionar.salhack.module.combat.*;
import me.ionar.salhack.module.exploit.*;
import me.ionar.salhack.module.misc.*;
import me.ionar.salhack.module.movement.*;
import me.ionar.salhack.module.render.*;
import me.ionar.salhack.module.schematica.*;
import me.ionar.salhack.module.ui.*;
import me.ionar.salhack.module.world.*;
import me.ionar.salhack.util.Pair;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.render.CustomTessellator;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Post;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public class ModuleManager
{
    public static ModuleManager Get()
    {
        return SalHack.GetModuleManager();
    }

    public ModuleManager()
    {
    }

    public ArrayList<Module> Mods = new ArrayList<Module>();
    private ArrayList<Module> ArrayListAnimations = new ArrayList<Module>();
    
    public void Init()
    {
        /// Bot
        Add(new DupeBot());
        
        /// Combat
        Add(new AimbotModule());
        Add(new AutoArmorModule());
        Add(new AutoCrystalModule());
        Add(new AutoTotemModule());
        Add(new AutoTrap());
        Add(new AutoTrapFeet());
        Add(new BowSpamModule());
        Add(new CriticalsModule());
        Add(new KillAuraModule());
        Add(new ReachModule());
        Add(new SurroundModule());
        Add(new VelocityModule());
        
        /// Exploit
        Add(new AntiHungerModule());
        Add(new CoordTPExploitModule());
        Add(new CrashExploitModule());
        Add(new EntityDesyncModule());
        Add(new LiquidInteractModule());
        Add(new ModifiedFreecam());
        Add(new MountBypassModule());
        Add(new NoMiningTrace());
        Add(new NewChunksModule());
        Add(new PacketCancellerModule());
        Add(new PacketFlyModule());
        Add(new PortalGodModeModule());
        Add(new SwingModule());

        /// Misc
        Add(new AntiAFKModule());
        Add(new AutoBonemealModule());
        Add(new AutoEatModule());
        Add(new AutoDyeModule());
        Add(new AutoFarmlandModule());
        Add(new AutoMendArmorModule());
        Add(new AutoShearModule());
        Add(new AutoShovelPathModule());
        Add(new AutoTameModule());
        Add(new AutoTendModule());
        Add(new BuildHeightModule());
        Add(new ChatModificationsModule());
        Add(new ChatNotifierModule());
        Add(new ChestStealerModule());
        Add(new FriendsModule());
        Add(new GlobalLocationModule());
        Add(new HotbarCacheModule());
        Add(new MiddleClickFriendsModule());
        Add(new RetardChatModule());
        Add(new StopWatchModule());
        Add(new TotemPopNotifierModule());
        Add(new XCarryModule());
        
        /// Movement
        Add(new AntiLevitationModule());
        Add(new ElytraFlyModule());
        Add(new EntityControlModule());
        Add(new FlightModule());
        Add(new NoFallModule());
        Add(new NoRotateModule());
        Add(new NoSlowModule());
        Add(new JesusModule());
        Add(new SafeWalkModule());
        Add(new SneakModule());
        Add(new SpeedModule());
        Add(new SprintModule());
        Add(new YawModule());
        
        /// Render
        Add(new AntiFog());
        Add(new BrightnessModule());
    //    Add(new ContainerPreviewModule());
        Add(new ESPModule());
        Add(new FreecamModule());
        Add(new HandProgressModule());
        Add(new NametagsModule());
        Add(new NoBobModule());
        Add(new NoRenderModule());
        Add(new ShulkerPreviewModule());
        Add(new TrajectoriesModule());

        /// UI
        Add(new ColorsModule());
        Add(new ConsoleModule());
        Add(new ClickGuiModule());
        Add(new HudEditorModule());
        Add(new HudModule());
        
        /// World
        Add(new AutoToolModule());
        Add(new AutoBuilderModule());
        Add(new AutoWitherModule());
        Add(new FastPlaceModule());
        Add(new LawnmowerModule());
        Add(new NoWeatherModule());
        Add(new ScaffoldModule());
        Add(new SpeedyGonzales());
        Add(new TimerModule());
        Add(new TorchAnnihilatorModule());
        
        /// Schematica
        Add(new PrinterBypassModule());

        Mods.sort((p_Mod1, p_Mod2) -> p_Mod1.getDisplayName().compareTo(p_Mod2.getDisplayName()));

        Mods.forEach(p_Mod ->
        {
            p_Mod.LoadSettings();
        });
    }

    public void Add(Module mod)
    {
        try
        {
            for (Field field : mod.getClass().getDeclaredFields())
            {
                if (Value.class.isAssignableFrom(field.getType()))
                {
                    if (!field.isAccessible())
                    {
                        field.setAccessible(true);
                    }
                    final Value val = (Value) field.get(mod);
                    val.InitalizeMod(mod);
                    mod.getValueList().add(val);
                }
            }
            Mods.add(mod);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public final List<Module> GetModuleList(ModuleType p_Type)
    {
        List<Module> list = new ArrayList<>();
        for (Module module : Mods)
        {
            if (module.getType().equals(p_Type))
            {
                list.add(module);
            }
        }
        // Organize alphabetically
        list.sort(Comparator.comparing(Module::getDisplayName));

        return list;
    }

    public final List<Module> GetModuleList()
    {
        return Mods;
    }

    public void OnBind(String string)
    {
        if (string == null || string.isEmpty() || string.equalsIgnoreCase("NONE"))
            return;
        
        Mods.forEach(p_Mod ->
        {
            if (p_Mod.getKey().equals(string))
            {
                p_Mod.toggle();
            }
        });
    }

    public Module GetMod(Class p_Class)
    {
        /*Mods.forEach(p_Mod ->
        {
           if (p_Mod.getClass() == p_Class)
               return p_Mod;
        });*/
        
        for (Module l_Mod : Mods)
        {
            if (l_Mod.getClass() == p_Class)
                return l_Mod;
        }
        
        SalHackMod.log.error("Could not find the class " + p_Class.getName() + " in Mods list");
        return null;
    }

    public Module GetModLike(String p_String)
    {
        for (Module l_Mod : Mods)
        {
            if (l_Mod.GetArrayListDisplayName().toLowerCase().startsWith(p_String.toLowerCase()))
                return l_Mod;
        }
        
        return null;
    }
    
    public void OnModEnable(Module p_Mod)
    {
        ArrayListAnimations.remove(p_Mod);
        ArrayListAnimations.add(p_Mod);

        final Comparator<Module> comparator = (first, second) ->
        {
            final String firstName = first.GetFullArrayListDisplayName();
            final String secondName = second.GetFullArrayListDisplayName();
            final float dif = RenderUtil.getStringWidth(secondName) - RenderUtil.getStringWidth(firstName);
            return dif != 0 ? (int) dif : secondName.compareTo(firstName);
        };
        
        ArrayListAnimations = (ArrayList<Module>) ArrayListAnimations.stream()
        .sorted(comparator)
        .collect(Collectors.toList());
    }

    public void Update()
    {
        if (ArrayListAnimations.isEmpty())
            return;
        
        Module l_Mod = ArrayListAnimations.get(0);
        
        if ((l_Mod.RemainingXAnimation -= (RenderUtil.getStringWidth(l_Mod.GetFullArrayListDisplayName()) / 10)) <= 0)
        {
            ArrayListAnimations.remove(l_Mod);
            l_Mod.RemainingXAnimation = 0;
        }
    }
}
