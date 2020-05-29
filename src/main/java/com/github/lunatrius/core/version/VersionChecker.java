package com.github.lunatrius.core.version;

import com.github.lunatrius.core.reference.Reference;
import com.google.common.base.Joiner;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.versioning.ComparableVersion;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VersionChecker {
    public static final String VER_CHECK_API_URL = "http://mc.lunatri.us/json?v=%d&mc=%s&limit=5";
    public static final int VER_CHECK_API_VER = 2;

    public static final String UPDATE_URL = "https://mods.io/mods?author=Lunatrius";

    private static final List<ModContainer> REGISTERED_MODS = new ArrayList<ModContainer>();
    private static final Joiner NEWLINE_JOINER = Joiner.on('\n');

    public static void registerMod(final ModContainer container, final String forgeVersion) {
        REGISTERED_MODS.add(container);

        final ModMetadata metadata = container.getMetadata();
        if (metadata.description != null) {
            metadata.description += "\n---\nCompiled against Forge " + forgeVersion;
        }
    }

    public static void startVersionCheck() {
        new Thread("LunatriusCore Version Check") {
            @Override
            public void run() {
                try {
                    if ("null".equals(Reference.MINECRAFT)) {
                        Reference.logger.error("Minecraft version is null! This is a bug!");
                        return;
                    }

                    final URL url = new URL(String.format(VER_CHECK_API_URL, VER_CHECK_API_VER, Reference.MINECRAFT));
                    final URLConnection connection = url.openConnection();
                    connection.addRequestProperty("User-Agent", Reference.MODID + "/" + Reference.VERSION);
                    final InputStream inputStream = connection.getInputStream();
                    final String data = new String(ByteStreams.toByteArray(inputStream));
                    inputStream.close();

                    process(new Gson().fromJson(data, VersionData.class));
                } catch (final Throwable t) {
                    Reference.logger.error("Something went wrong!", t);
                }
            }

            private void process(final VersionData versionData) {
                if (versionData.version != VER_CHECK_API_VER) {
                    return;
                }

                if (versionData.mods == null) {
                    return;
                }

                for (final ModContainer container : REGISTERED_MODS) {
                    final String modid = container.getModId();
                    if (!isAllowedToCheck(modid)) {
                        Reference.logger.info("Skipped version check for {}", modid);
                        continue;
                    }

                    final ModData modData = versionData.mods.get(modid);
                    if (modData == null || modData.latest == null) {
                        continue;
                    }

                    processMod(container, modData);
                }
            }

            private void processMod(final ModContainer container, final ModData modData) {
                final BuildData latestBuild = modData.latest;

                final ComparableVersion versionRemote = latestBuild.getVersion();
                final String version = container.getVersion();
                final ComparableVersion versionLocal = new ComparableVersion(version);

                final ForgeVersion.Status status = ForgeVersionCheck.getStatus(versionRemote, versionLocal);
                final ComparableVersion target = latestBuild.getVersion();
                final Map<ComparableVersion, String> changes = modData.getAllChanges();
                final String url = UPDATE_URL;

                ForgeVersionCheck.notify(container, status, target, changes, url);
            }
        }.start();
    }

    public static boolean isAllowedToCheck(final String scope) {
        return ForgeModContainer.getConfig().get(ForgeModContainer.VERSION_CHECK_CAT, scope, true).getBoolean();
    }

    public static class VersionData {
        public int version;
        public Map<String, ModData> mods;
    }

    public static class ModData {
        public BuildData latest;
        public List<BuildData> builds;

        public Map<ComparableVersion, String> getAllChanges() {
            final LinkedHashMap<ComparableVersion, String> changes = new LinkedHashMap<ComparableVersion, String>();

            if (this.builds != null) {
                Collections.sort(this.builds, new Comparator<BuildData>() {
                    @Override
                    public int compare(final BuildData a, final BuildData b) {
                        return b.getVersion().compareTo(a.getVersion());
                    }
                });

                for (final BuildData build : this.builds) {
                    changes.put(build.getVersion(), build.getChanges());
                }

                return changes;
            }

            if (this.latest != null) {
                changes.put(this.latest.getVersion(), this.latest.getChanges());

                return changes;
            }

            return changes;
        }
    }

    public static class BuildData {
        public String mc;
        public String version;
        public int build;
        public List<String> changes;

        public ComparableVersion getVersion() {
            return new ComparableVersion(this.version);
        }

        public String getChanges() {
            if (this.changes == null) {
                return "";
            }

            return NEWLINE_JOINER.join(this.changes);
        }
    }
}
