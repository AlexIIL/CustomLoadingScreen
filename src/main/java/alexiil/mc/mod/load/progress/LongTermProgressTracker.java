package alexiil.mc.mod.load.progress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Loader;

import alexiil.mc.mod.load.ModLoadingListener;
import alexiil.mc.mod.load.ModLoadingListener.LoaderStage;
import alexiil.mc.mod.load.progress.SingleProgressBarTracker.ReloadPart;

public class LongTermProgressTracker {

    public final String[] modIds;
    public final ProgressSectionInfo[] infos;

    @Nullable
    public static LongTermProgressTracker load() {
        File file = Loader.instance().getConfigDir();
        file = new File(file, "customloadingscreen_timings.nbt");
        if (file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                NBTTagCompound nbt = CompressedStreamTools.readCompressed(fis);
                return new LongTermProgressTracker(nbt);
            } catch (IOException io) {
                io.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public static void save(List<ProgressSectionInfo> infos) {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList modList = new NBTTagList();
        for (String modId : ModLoadingListener.modIds) {
            modList.appendTag(new NBTTagString(modId));
        }
        nbt.setTag("mods", modList);
        int[] ids = new int[infos.size()];
        int[] timings = new int[ids.length];
        for (int i = 0; i < ids.length; i++) {
            ProgressSectionInfo info = infos.get(i);
            int id = 0;
            if (info.modState != null) {
                id |= 1;
                id |= info.modState.ordinal() << 1;
                id |= ModLoadingListener.modIds.indexOf(info.modId) << 5;
            } else {
                id |= info.reloadPart.ordinal() << 1;
            }
            ids[i] = id;
            timings[i] = (int) info.time;
        }
        nbt.setIntArray("ids", ids);
        nbt.setIntArray("timings", timings);

        File file = Loader.instance().getConfigDir();
        file = new File(file, "customloadingscreen_timings.nbt");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            CompressedStreamTools.writeCompressed(nbt, fos);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    private LongTermProgressTracker(NBTTagCompound nbt) throws IOException {
        NBTTagList list = nbt.getTagList("mods", Constants.NBT.TAG_STRING);
        modIds = new String[list.tagCount()];

        if (modIds.length == 0) {
            throw new IOException("0 mods isn't right!");
        }

        for (int i = 0; i < modIds.length; i++) {
            modIds[i] = list.getStringTagAt(i);
        }

        int[] ids = nbt.getIntArray("ids");
        int[] timings = nbt.getIntArray("timings");

        if (ids.length != timings.length || ids.length == 0) {
            throw new IOException("wrong lengths!");
        }

        infos = new ProgressSectionInfo[ids.length];

        for (int i = 0; i < ids.length; i++) {
            int id = ids[i];
            int time = timings[i];
            if ((id & 1) == 1) {
                // mod
                int stage = (id >> 1) & 0xF;
                if (stage >= LoaderStage.values().length) {
                    throw new IOException("Index out of bounds");
                }
                LoaderStage loaderStage = LoaderStage.values()[stage];
                int modIndex = id >>> 5;
                if (modIndex < 0 || modIndex >= modIds.length) {
                    throw new IOException("Index out of bounds");
                }
                String modId = modIds[modIndex];
                infos[i] = new ProgressSectionInfo(loaderStage, modId, time);
            } else {
                // reload section
                int stage = id >>> 1;
                if (stage >= ReloadPart.values().length) {
                    throw new IOException("Index out of bounds");
                }
                ReloadPart reloadPart = ReloadPart.values()[stage];
                infos[i] = new ProgressSectionInfo(reloadPart, time);
            }
        }
    }
}
