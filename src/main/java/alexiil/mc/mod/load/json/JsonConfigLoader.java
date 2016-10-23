package alexiil.mc.mod.load.json;

import java.io.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonConfigLoader<T> {
    private final Class<T> clazz;
    private final File file;
    private final T defaultConfig;

    public JsonConfigLoader(File file, Class<T> clazz, T defaultConfig) {
        this.file = file;
        this.clazz = clazz;
        this.defaultConfig = defaultConfig;
    }

    /** Overwrite any existing config: Treat it as a default config */
    public void createNew() {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(defaultConfig));
            writer.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if (writer != null) try {
                writer.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public T load() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            return new Gson().fromJson(reader, clazz);
        } catch (FileNotFoundException e) {
            createNew();
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return defaultConfig;
    }
}