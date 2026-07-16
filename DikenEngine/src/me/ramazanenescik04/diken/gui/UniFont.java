package me.ramazanenescik04.diken.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.json.*;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.IResource;

/**
 * Represents a bitmap font within DikenEngine.
 * <p>
 * Her karakter bir {@link Bitmap}'e dönüştürülür ve harfe göre map'te tutulur.
 * Font'lar statik registry üzerinden isimle ya da index ile alınabilir.
 * </p>
 */
public class UniFont implements IResource {
    private static final long serialVersionUID = 1L;

    // -------------------------------------------------------------------------
    // Static registry
    // -------------------------------------------------------------------------

    private static final List<UniFont> registry = new ArrayList<>();

    private static final Bitmap MISSING_CHAR = buildMissingChar();

    // -------------------------------------------------------------------------
    // Instance fields
    // -------------------------------------------------------------------------

    /** Engine içindeki kısa tanımlayıcı isim (ör. "Arial.14"). */
    private String name;

    /** Okunabilir meta isim (ör. "Arial-tr_TR"). */
    private String displayName;

    /** Karakter → Bitmap haritası. */
    private final Map<Character, Bitmap> charBitmaps = new HashMap<>();

    // IResource için null olmamalı
    public UniFont() {}
    
    public UniFont(UniFont uniFont) {
		this.name = uniFont.name;
		this.displayName = uniFont.displayName;
		
		for (var entry : uniFont.charBitmaps.entrySet()) {
			this.charBitmaps.put(entry.getKey(), entry.getValue().clone());
		}
   	}

    // =========================================================================
    // Factory — dosyadan yükle
    // =========================================================================

	/**
     * Resources klasöründeki font verilerini yükler.
     *
     * @param fontName {@code /fonts/<fontName>/} altındaki klasör adı
     * @return yüklenen {@link UniFont}
     */
    public static UniFont loadFromResources(String fontName) {
        UniFont font = new UniFont();
        font.name = fontName;

        Bitmap sheet = (Bitmap) IOResource.loadResource(
                UniFont.class.getResourceAsStream("/fonts/" + fontName + "/font_bitmap.png"),
                EnumResource.IMAGE);

        JSONObject meta = readJson(UniFont.class.getResourceAsStream("/fonts/" + fontName + "/font_data.json"));
        font.displayName = meta.getString("font_name") + "-" + meta.getString("author");

        JSONArray chars = meta.getJSONArray("chars");
        for (int i = 0; i < chars.length(); i++) {
            JSONObject entry = chars.getJSONObject(i);
            char ch = entry.getString("char").charAt(0);
            int w  = entry.getInt("width");
            int h  = entry.getInt("height");
            int sx = entry.getInt("x");
            int sy = entry.getInt("y");

            Bitmap charBitmap = new Bitmap(w, h);

            if (sx >= 0 && sy >= 0) {
                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        int color = sheet.pixels.get((sx + x) + (sy + y) * sheet.w);
                        charBitmap.setPixel(x, y, color);
                    }
                }
            }

            font.charBitmaps.put(ch, charBitmap);
        }

        register(font);
        return font;
    }

    // =========================================================================
    // Factory — AWT Font'tan üret
    // =========================================================================

    /**
     * Bir AWT {@link Font}'u tüm glyph'leriyle birlikte bitmap'e dönüştürür.
     *
     * @param awtFont kaynak AWT fontu
     * @return oluşturulan {@link UniFont}
     */
    public static UniFont fromAwtFont(Font awtFont) {
        UniFont font = new UniFont();
        Locale locale = Locale.getDefault();
        font.name        = "%s.%d".formatted(awtFont.getFontName(), awtFont.getSize()).replace(" ", "_");
        font.displayName = "%s_%s-%s".formatted(awtFont.getFontName(), locale.getLanguage(), locale.getCountry());

        // Metrics için geçici grafik nesnesi
        BufferedImage scratch = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scratch.createGraphics();
        g2d.setFont(awtFont);
        FontMetrics metrics = g2d.getFontMetrics();
        g2d.dispose();

        Map<Character, Bitmap> bitmaps = new java.util.concurrent.ConcurrentHashMap<>();

        // Her geçerli glyph için bitmap üret
        for (int cp = 0; cp < awtFont.getNumGlyphs(); cp++) {
            if (!awtFont.canDisplay(cp)) continue;

            int charWidth  = metrics.charWidth(cp) + 1;
            int charHeight = metrics.getHeight();
            if (charWidth <= 0 || charHeight <= 0) continue;

            char ch = (char) cp;
            bitmaps.put(ch, renderChar(ch, awtFont, metrics, charWidth, charHeight));
        }

        font.charBitmaps.putAll(bitmaps);

        register(font);
        return font;
    }

    // =========================================================================
    // Registry yönetimi
    // =========================================================================

    private static void register(UniFont font) {
        registry.add(font);
        DikenEngine.log("Loaded Font: " + font.name);
    }

    /**
     * @param name font'un kısa adı
     * @return font ya da {@code null}
     */
    public static UniFont getFont(String name) {
        return registry.stream()
                .filter(f -> f.name.equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * @param index registry sırası
     * @return font ya da {@code null} (sınır dışı erişimde crash vermez)
     */
    public static UniFont getFont(int index) {
        if (index < 0 || index >= registry.size()) return null;
        return registry.get(index);
    }

    /**
     * @param name kaldırılacak font adı
     * @return başarılıysa {@code true}
     */
    public static boolean removeFont(String name) {
        return registry.removeIf(f -> f.name.equals(name));
    }

    /** Registry'deki toplam font sayısı. */
    public static int registrySize() {
        return registry.size();
    }

    // =========================================================================
    // Karakter & Bitmap yardımcıları
    // =========================================================================

    /**
     * Verilen metni karakter bitmap'lerine böler.
     *
     * @param text metin
     * @param font kullanılacak font (null ise hepsi missing-char döner)
     * @return bitmap dizisi
     */
    public static Bitmap[] getBitmapChars(String text, UniFont font) {
        Bitmap[] result = new Bitmap[text.length()];
        for (int i = 0; i < text.length(); i++) {
            result[i] = getBitmapChar(text.charAt(i), font);
        }
        return result;
    }

    /**
     * Tek bir karakter için bitmap döner.
     *
     * @param ch   karakter
     * @param font kullanılacak font (null ise missing-char döner)
     * @return bitmap
     */
    public static Bitmap getBitmapChar(char ch, UniFont font) {
        if (font == null) return MISSING_CHAR;
        return font.charBitmaps.getOrDefault(ch, MISSING_CHAR);
    }

    /** Font'un belirtilen karakteri içerip içermediğini döner. */
    public boolean hasChar(char ch) {
        return charBitmaps.containsKey(ch);
    }

    /** Font'taki toplam karakter sayısı. */
    public int charCount() {
        return charBitmaps.size();
    }

    // =========================================================================
    // Getters / Setters
    // =========================================================================

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    /**
     * Karakter bitmap haritasının salt okunur görünümü.
     * Direkt değişiklik yapılması önerilmez; bunun yerine {@link #putChar} kullanın.
     */
    public Map<Character, Bitmap> getCharBitmaps() {
        return Collections.unmodifiableMap(charBitmaps);
    }

    /** Haritaya manuel olarak bir karakter bitmap'i ekler. */
    public void putChar(char ch, Bitmap bitmap) {
        charBitmaps.put(ch, bitmap);
    }

    /** Haritadan bir karakter bitmap'ini kaldırır. */
    public void removeChar(char ch) {
        charBitmaps.remove(ch);
    }

    // =========================================================================
    // IResource
    // =========================================================================

    @Override
    public EnumResource getResourceType() {
        return EnumResource.FONT;
    }
    
    @Override
    public void saveResource(DataOutputStream out) throws IOException {
        out.writeUTF(name);
        out.writeUTF(displayName);
        out.writeInt(charBitmaps.size());
 
        for (Map.Entry<Character, Bitmap> entry : charBitmaps.entrySet()) {
            byte[] data = entry.getValue().toBytes("png");
            out.writeChar(entry.getKey());
            out.writeInt(data.length);
            out.write(data);
        }
    }

	@Override
	public void loadResource(DataInputStream in) throws IOException {
        name        = in.readUTF();
        displayName = in.readUTF();
 
        int charCount = in.readInt();
        charBitmaps.clear();
 
        for (int i = 0; i < charCount; i++) {
            char   ch   = in.readChar();
            byte[] data = new byte[in.readInt()];
            in.readFully(data);
            charBitmaps.put(ch, Bitmap.fromBytes(data));
        }
    }
	
	@Override
	public UniFont clone() {
		return new UniFont(this);
	}

    // =========================================================================
    // Private yardımcılar
    // =========================================================================

	private static Bitmap renderChar(char ch, Font font, FontMetrics metrics, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);
        g2d.drawString(String.valueOf(ch), 0, metrics.getAscent());
        g2d.dispose();
        return Bitmap.toBitmap(img);
    }

    private static Bitmap buildMissingChar() {
        Bitmap bmp = new Bitmap(6, 8);
        bmp.box(1, 0, 4, 7, 0xffffffff);
        return bmp;
    }

    private static JSONObject readJson(InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String json = reader.lines().collect(Collectors.joining());
            return new JSONObject(json);
        } catch (IOException e) {
            throw new UncheckedIOException("Font JSON okunamadı", e);
        }
    }
}