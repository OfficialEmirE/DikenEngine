package me.ramazanenescik04.diken.studio.editors;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.ramazanenescik04.diken.game.InstanceList;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.event.Event;
import me.ramazanenescik04.diken.game.nodes.Camera;
import me.ramazanenescik04.diken.game.services.*;
import me.ramazanenescik04.diken.scripting.LuaDoc;
import me.ramazanenescik04.diken.scripting.LuaInit;

/**
 * DikenEngine Lua API'si için otomatik tamamlama verilerini toplar.
 * Ctrl+Space'e basıldığında ScriptEditor bu sınıfı kullanarak öneri listesi alır.
 */
public final class LuaCompleter {

    private static final String[] LUA_KEYWORDS = {
        "and", "break", "do", "else", "elseif", "end",
        "false", "for", "function", "if", "in", "local",
        "nil", "not", "or", "repeat", "return", "then",
        "true", "until", "while"
    };

    private static final String[] SNIPPETS = {
        "for i = 1, N do\n\t\nend",
        "for _, v in ipairs(T) do\n\t\nend",
        "for k, v in pairs(T) do\n\t\nend",
        "function ()\n\t\nend",
        "if  then\n\t\nend",
        "if  then\n\t\nelse\n\t\nend",
        "print()",
        "warn()",
    };

    private static final String[] SNIPPET_TRIGGERS = {
        "fori", "forv", "fork", "fun", "if", "ife", "print", "warn"
    };

    private static final List<Class<?>> SCANNED_CLASSES = new ArrayList<>();
    private static final List<String> ALL_COMPLETIONS = new ArrayList<>();

    static {
        // 1. Lua keyword'leri
        for (String kw : LUA_KEYWORDS) {
            ALL_COMPLETIONS.add(kw);
        }

        // 2. Snippet'ler
        for (String s : SNIPPETS) {
            ALL_COMPLETIONS.add(s);
        }
        for (String t : SNIPPET_TRIGGERS) {
            ALL_COMPLETIONS.add(t);
        }

        // 3. self
        ALL_COMPLETIONS.add("self");

        // 4. Node sınıfları
        for (Class<?> clazz : InstanceList.getNodeClassList()) {
            scanClass(clazz);
        }

        // 5. Servis sınıfları
        scanClass(Camera.class);
        scanClass(Game.class);
        scanClass(Workspace.class);
        scanClass(PlayerService.class);
        scanClass(Lighting.class);
        scanClass(UIService.class);
        scanClass(InputService.class);
        scanClass(RunService.class);

        // 6. LuaInit yardımcı sınıflar
        for (Class<?> clazz : LuaInit.initClasses().values()) {
            scanClass(clazz);
        }

        // 7. Enum değerleri
        for (Map.Entry<String, Class<? extends Enum<?>>> entry : LuaInit.initEnums().entrySet()) {
            String enumName = entry.getKey();
            for (Object constant : entry.getValue().getEnumConstants()) {
                Enum<?> e = (Enum<?>) constant;
                ALL_COMPLETIONS.add("Enum." + enumName + "." + e.name());
                ALL_COMPLETIONS.add(enumName + "." + e.name());
            }
        }
    }

    private LuaCompleter() {}

    /**
     * Verilen kelimeye göre eşleşen completion'ları döndürür.
     * Boş kelime verilirse tüm completion'ları döndürür.
     */
    public static List<String> complete(String word) {
        if (word == null || word.isEmpty()) {
            return new ArrayList<>(ALL_COMPLETIONS);
        }

        String lower = word.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String c : ALL_COMPLETIONS) {
            if (c.toLowerCase().startsWith(lower)) {
                result.add(c);
            }
        }

        // Noktadan sonra: eğer "Part." yazıyorsa, Part'a ait üyeleri göster
        int dotIndex = word.lastIndexOf('.');
        if (dotIndex >= 0 && result.isEmpty()) {
            String prefix = word.substring(0, dotIndex);
            String suffix = word.substring(dotIndex + 1);
            // prefix'e göre class bulup field/metodlarını göster
            for (Class<?> clazz : getAllScannedClasses()) {
                if (clazz.getSimpleName().equalsIgnoreCase(prefix)
                        || clazz.getName().equalsIgnoreCase(prefix)) {
                    for (String member : getClassMembers(clazz)) {
                        if (suffix.isEmpty() || member.toLowerCase().startsWith(suffix.toLowerCase())) {
                            result.add(prefix + "." + member);
                        }
                    }
                }
            }
        }

        return result;
    }

    // -------------------------------------------------------------------------
    //  Class tarama
    // -------------------------------------------------------------------------

    private static void scanClass(Class<?> clazz) {
        SCANNED_CLASSES.add(clazz);
        ALL_COMPLETIONS.add(clazz.getSimpleName());

        for (Field f : clazz.getFields()) {
            if (isApiMember(f)) {
                ALL_COMPLETIONS.add(f.getName());
                ALL_COMPLETIONS.add(clazz.getSimpleName() + "." + f.getName());
            }
        }

        for (Method m : clazz.getMethods()) {
            if (isApiMember(m)) {
                ALL_COMPLETIONS.add(m.getName());
                ALL_COMPLETIONS.add(clazz.getSimpleName() + "." + m.getName());

                // Parametre imzalı versiyon
                StringBuilder sig = new StringBuilder(m.getName()).append("(");
                Class<?>[] params = m.getParameterTypes();
                for (int i = 0; i < params.length; i++) {
                    if (i > 0) sig.append(", ");
                    sig.append(params[i].getSimpleName());
                }
                sig.append(")");
                ALL_COMPLETIONS.add(clazz.getSimpleName() + "." + sig.toString());
            }
        }
    }

    private static List<String> getClassMembers(Class<?> clazz) {
        List<String> members = new ArrayList<>();
        for (Field f : clazz.getFields()) {
            if (isApiMember(f)) members.add(f.getName());
        }
        for (Method m : clazz.getMethods()) {
            if (isApiMember(m)) members.add(m.getName());
        }
        return members;
    }

    private static List<Class<?>> getAllScannedClasses() {
        return new ArrayList<>(SCANNED_CLASSES);
    }

    private static boolean isApiMember(Field f) {
        return f.isAnnotationPresent(LuaDoc.class)
                || f.getType() == Event.class
                || f.getName().startsWith("get")
                || f.getName().startsWith("set")
                || f.getName().startsWith("is");
    }

    private static boolean isApiMember(Method m) {
        if (m.getDeclaringClass() == Object.class) return false;
        if (m.isAnnotationPresent(LuaDoc.class)) return true;
        if (m.getName().startsWith("get") || m.getName().startsWith("set")) return true;
        if (m.getName().startsWith("is") && m.getParameterCount() == 0) return true;
        return false;
    }
}
