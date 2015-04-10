package alexiil.mods.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Translation {
    private static Map<String, Translation> translators = new HashMap<String, Translation>();
    private static Translation currentTranslation = null;
    private Map<String, String> translations = new HashMap<String, String>();

    public static String translate(String toTranslate) {
        return translate(toTranslate, toTranslate);
    }

    public static String translate(String toTranslate, String failure) {
        if (currentTranslation != null)
            return currentTranslation.translateInternal(toTranslate, failure);
        return failure;
    }

    public static void addTranslations(File modLocation) {
        String lookingFor = "assets/betterloadingscreen/lang/";
        if (modLocation == null)
            return;
        if (modLocation.isDirectory()) {
            File langFolder = new File(modLocation, lookingFor);
            System.out.println(langFolder.getAbsolutePath() + ", " + langFolder.isDirectory());
            for (File f : langFolder.listFiles()) {
                if (f != null)
                    System.out.println(f.getAbsolutePath());
                else
                    System.out.println("null");
            }
        }
        else if (modLocation.isFile()) {
            JarFile modJar = null;
            try {
                modJar = new JarFile(modLocation);
                Enumeration<JarEntry> entries = modJar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry je = entries.nextElement();
                    String name = je.getName();
                    if (name.startsWith(lookingFor) && !name.equals(lookingFor)) {
                        try {
                            addTranslation(name.replace(lookingFor, "").replace(".lang", ""),
                                    new BufferedReader(new InputStreamReader(modJar.getInputStream(je), "UTF-8")));
                        }
                        catch (IOException e) {
                            System.out.println("Had trouble opening " + name);
                        }
                    }
                }
            }
            catch (IOException e) {
                System.out.println("Could not open file");
            }
            finally {
                if (modJar != null)
                    try {
                        modJar.close();
                    }
                    catch (IOException e) {}
            }
        }

        // Lastly, set the current locale
        File options = new File("./options.txt");
        String language = "en_US";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(options));
            String line = "";
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts[0].equals("lang")) {
                    language = parts[1];
                }
            }
        }
        catch (IOException e) {

        }
        finally {
            if (reader != null)
                try {
                    reader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
        }
        if (translators.containsKey(language))
            currentTranslation = translators.get(language);
        else if (translators.containsKey("en_US")) {
            System.out.println("Failed to load " + language + ", loading en_US insted");
            currentTranslation = translators.get("en_US");
        }
        else if (!translators.isEmpty()) {
            String name = translators.keySet().iterator().next();
            System.out.println("Failed to load " + language + ", AND FAILED TO LOAD en_US! One available however is " + name
                    + ", using that and keeping quiet...");
            currentTranslation = translators.values().iterator().next();
        }
        else {
            System.out.println("Failed to load ANY languages! all strings fail now!");
        }
    }

    public static boolean addTranslation(String locale, BufferedReader from) {
        try {
            translators.put(locale, new Translation(from));
        }
        catch (IOException e) {
            System.out.println("Failed to add" + locale);
        }
        return true;
    }

    private Translation(BufferedReader loadFrom) throws IOException {
        BufferedReader reader = loadFrom;
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] splitter = line.split("=");
                if (splitter.length != 2) {
                    System.out.println("Found an invalid line (" + line + ")");
                }
                else {
                    translations.put(splitter[0], splitter[1]);
                }
            }
        }
        finally {
            if (reader != null)
                reader.close();
        }
    }

    private String translateInternal(String toTranslate, String failure) {
        if (translations.containsKey(toTranslate))
            return translations.get(toTranslate);
        return failure;
    }
}
