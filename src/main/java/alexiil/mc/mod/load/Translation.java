package alexiil.mc.mod.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Translation {
    private static Map<String, Translation> translators = new HashMap<>();
    private static Translation currentTranslation = null;
    private Map<String, String> translations = new HashMap<>();
    private Set<String> failedTranslations = new HashSet<>();

    public static String translate(String toTranslate) {
        if (currentTranslation != null) return currentTranslation.translateInternal(toTranslate);
        CLSLog.log().warn("We don't have a translator!");
        return toTranslate;
    }

    public static void addTranslations(File modLocation) {
        String lookingFor = "assets/customloadingscreen/lang/";
        if (modLocation == null) return;
        if (modLocation.isDirectory()) {
            File langFolder = new File(modLocation, lookingFor);
            System.out.println(langFolder.getAbsolutePath() + ", " + langFolder.isDirectory());
            for (File f : langFolder.listFiles()) {
                if (f.getName().endsWith(".lang")) {
                    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                        addTranslation(f.getName().replace(".lang", ""), br);
                    } catch (IOException e) {
                        System.out.println(e);
                    }
                }
            }
        } else if (modLocation.isFile()) {
            try (JarFile modJar = new JarFile(modLocation)) {
                Enumeration<JarEntry> entries = modJar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry je = entries.nextElement();
                    String name = je.getName();
                    if (name.startsWith(lookingFor) && !name.equals(lookingFor)) {
                        try {
                            addTranslation(
                                name.replace(lookingFor, "").replace(".lang", ""), new BufferedReader(
                                    new InputStreamReader(modJar.getInputStream(je), "UTF-8")
                                )
                            );
                        } catch (IOException e) {
                            System.out.println("Had trouble opening " + name);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Could not open file " + e.getMessage());
            }
        }

        // Lastly, set the current locale
        File options = new File("./options.txt");
        String language = "en_us";
        try (BufferedReader reader = new BufferedReader(new FileReader(options))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts[0].equals("lang")) {
                    language = parts[1].toLowerCase(Locale.ROOT);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (translators.containsKey(language)) {
            currentTranslation = translators.get(language);
        } else if (translators.containsKey("en_us")) {
            System.out.println("Failed to load " + language + ", loading en_us insted");
            currentTranslation = translators.get("en_us");
        } else if (!translators.isEmpty()) {
            String name = translators.keySet().iterator().next();
            System.out.println(
                "Failed to load " + language + ", AND FAILED TO LOAD en_us! One available however is " + name
                    + ", using that and keeping quiet..."
            );
            currentTranslation = translators.values().iterator().next();
        } else {
            System.out.println("Failed to load ANY languages! All strings fail now!");
        }
    }

    public static boolean addTranslation(String locale, BufferedReader from) {
        try {
            translators.put(locale, new Translation(from));
        } catch (IOException e) {
            System.out.println("Failed to add" + locale + " because " + e.getMessage());
        }
        return true;
    }

    private Translation(BufferedReader loadFrom) throws IOException {
        try {
            String line;
            while ((line = loadFrom.readLine()) != null) {
                String[] splitter = line.split("=");
                if (splitter.length != 2) {
                    System.out.println("Found an invalid line (" + line + ")");
                } else {
                    translations.put(splitter[0], splitter[1]);
                }
            }
        } finally {
            if (loadFrom != null) {
                loadFrom.close();
            }
        }
    }

    private String translateInternal(String toTranslate) {
        if (translations.containsKey(toTranslate)) return translations.get(toTranslate);
        if (failedTranslations.add(toTranslate)) {
            CLSLog.log().warn("Failed to translate " + toTranslate);
        }
        return toTranslate;
    }
}
