package me.ionar.salhack.waypoints;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.events.player.EventPlayerJoin;
import me.ionar.salhack.events.player.EventPlayerLeave;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.main.Wrapper;
import me.ionar.salhack.managers.UUIDManager;
import me.ionar.salhack.module.render.WaypointsModule;
import me.ionar.salhack.util.SalVec3d;
import me.ionar.salhack.waypoints.Waypoint.Type;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listenable;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

public class WaypointManager implements Listenable
{
    public WaypointManager()
    {
        SalHackMod.EVENT_BUS.subscribe(this);
        
        _waypoints = new CopyOnWriteArrayList<>();
        
        try 
        {
            Gson gson = new Gson();

            Reader reader = Files.newBufferedReader(Paths.get("SalHack/Waypoints/Waypoints.json"));

            _waypoints = gson.fromJson(reader, new TypeToken<CopyOnWriteArrayList<Waypoint>>() {}.getType());
            
            reader.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    private List<Waypoint> _waypoints;
    private final Map<String, EntityPlayer> playerCache = Maps.newConcurrentMap();
    private final Map<String, PlayerData> logoutCache = Maps.newConcurrentMap();
    
    public static WaypointManager Get()
    {
        return SalHack.GetWaypointManager();
    }

    public List<Waypoint> GetWaypoints()
    {
        return _waypoints;
    }
    
    public final Map<String, PlayerData> GetLogoutCache()
    {
        return logoutCache;
    }

    public void AddWaypoint(Type type, String name, SalVec3d pos, int dimension)
    {
        _waypoints.add(new Waypoint(name, pos, type, Wrapper.GetMC().getCurrentServerData() != null ? Wrapper.GetMC().getCurrentServerData().serverIP : "singleplayer", dimension));
        ProcessSave();
    }

    public boolean RemoveWaypoint(String name)
    {
        if (_waypoints.isEmpty())
            return false;
        
        if (name == null)
        {
            _waypoints.remove(_waypoints.size() - 1);
            return true;
        }
        
        Waypoint pointToRemove = null;
        
        for (Waypoint point : _waypoints)
        {
            if (point.getDisplayName().equals(name))
            {
                pointToRemove = point;
                break;
            }
        }
        
        if (pointToRemove != null)
        {
            _waypoints.remove(pointToRemove);
            ProcessSave();
        }
        
        return pointToRemove != null;
    }

    public boolean EditWaypoint(String name, SalVec3d pos)
    {
        Waypoint pointToEdit = null;
        
        for (Waypoint point : _waypoints)
        {
            if (point.getDisplayName().equals(name))
            {
                pointToEdit = point;
                break;
            }
        }
        
        if (pointToEdit != null)
        {
            pointToEdit.setPos(pos);
            ProcessSave();
            return true;
        }
        
        return false;
    }
    
    private void ProcessSave()
    {
        try
        {
            GsonBuilder builder = new GsonBuilder();
            
            Gson gson = builder.setPrettyPrinting().create();

            Writer writer = Files.newBufferedWriter(Paths.get("SalHack/Waypoints/Waypoints.json"));
            gson.toJson(_waypoints, writer);
            writer.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    // seppuku

    @EventHandler
    private Listener<EventPlayerUpdate> onPlayerUpdate = new Listener<>(event ->
    {
        final Minecraft mc = Wrapper.GetMC();

        for (EntityPlayer player : mc.world.playerEntities)
        {
            if (player == null || player.equals(mc.player))
                continue;

            updatePlayerCache(player.getGameProfile().getId().toString(), player);
        }
    });
    
    @EventHandler
    private Listener<EventPlayerLeave> onPlayerLeave = new Listener<>(event ->
    {
        for (String uuid : this.playerCache.keySet())
        {
            if (!uuid.equals(event.getId()))
                continue;

            final EntityPlayer player = this.playerCache.get(uuid);

            final PlayerData data = new PlayerData(player.getPositionVector(), player.getGameProfile(), player);

            if (!this.hasPlayerLogged(uuid))
            {
                this.logoutCache.put(uuid, data);
                this.AddWaypoint(Waypoint.Type.Logout, player.getName() + " logout spot", new SalVec3d(player.posX, player.posY, player.posZ), player.dimension);
            }
        }

        this.playerCache.clear();
    });

    @EventHandler
    private Listener<EventPlayerJoin> onPlayerJoin = new Listener<>(event ->
    {
        for (String uuid : this.logoutCache.keySet())
        {
            if (!uuid.equals(event.getId()))
                continue;

            this.logoutCache.remove(uuid);
        }

        this.playerCache.clear();
    });
    
    private void updatePlayerCache(String uuid, EntityPlayer player)
    {
        this.playerCache.put(uuid, player);
    }

    private boolean hasPlayerLogged(String uuid)
    {
        return this.logoutCache.containsKey(uuid);
    }

    public boolean isOutOfRange(PlayerData data)
    {
        Vec3d position = data.position;
        return Wrapper.GetMC().player.getDistance(position.x, position.y, position.z) > WaypointsModule.RemoveDistance.getValue();
    }

    public Map<String, EntityPlayer> getPlayerCache()
    {
        return playerCache;
    }

    public Map<String, PlayerData> getLogoutCache()
    {
        return logoutCache;
    }
    
    public void RemoveLogoutCache(String uuid)
    {
        logoutCache.remove(uuid);
        
        new Thread(() ->
        {
            final String name = UUIDManager.Get().resolveName(uuid);
            
            if (name != null)
                RemoveWaypoint(name);
        }).start();
    }

    public class PlayerData
    {
        public Vec3d position;
        public GameProfile profile;
        public EntityPlayer ghost;

        public PlayerData(Vec3d position, GameProfile profile, EntityPlayer ghost)
        {
            this.position = position;
            this.profile = profile;
            this.ghost = ghost;
        }
    }
}
