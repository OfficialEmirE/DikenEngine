package me.ramazanenescik04.diken.game.io;

import java.io.*;
import java.util.*;

/**
 * Minecraft NBT formatına benzer, tip güvenli, sealed bir tag sistemi.
 *
 * Java 25 özellikleri:
 *  - sealed interface + record permits listesi (kapalı tip hiyerarşisi)
 *  - switch expression + record pattern matching (id() yerine instanceof zinciri yok)
 *  - unnamed pattern/variable ("_") - kullanılmayan bileşenleri isimlendirmeye gerek yok
 *  - SequencedMap / SequencedCollection (LinkedHashMap, Java 21+ stable, 25'te de aynı)
 *
 * Kullanım senin UniFont/IResource pattern'ine benzer şekilde tasarlandı:
 *   byte[] bytes = compound.toBytes();
 *   Tag.Compound loaded = Tag.Compound.fromBytes(bytes);
 */
public sealed interface Tag {

    /** Wire formatındaki tip id'si (Minecraft NBT id'leriyle aynı sırada). */
    byte id();

    /** Tag içeriğini stream'e yazar (isim/uzunluk header'ı olmadan, sadece payload). */
    void write(DataOutputStream out) throws IOException;

    // ---------------------------------------------------------------
    // Primitive / basit tag'ler
    // ---------------------------------------------------------------

    record End() implements Tag {
        public byte id() { return 0; }
        public void write(DataOutputStream out) { /* payload yok */ }
    }

    record Byte(byte value) implements Tag {
        public byte id() { return 1; }
        public void write(DataOutputStream out) throws IOException { out.writeByte(value); }
    }

    record Short(short value) implements Tag {
        public byte id() { return 2; }
        public void write(DataOutputStream out) throws IOException { out.writeShort(value); }
    }

    record Int(int value) implements Tag {
        public byte id() { return 3; }
        public void write(DataOutputStream out) throws IOException { out.writeInt(value); }
    }

    record Long(long value) implements Tag {
        public byte id() { return 4; }
        public void write(DataOutputStream out) throws IOException { out.writeLong(value); }
    }

    record Float(float value) implements Tag {
        public byte id() { return 5; }
        public void write(DataOutputStream out) throws IOException { out.writeFloat(value); }
    }

    record Double(double value) implements Tag {
        public byte id() { return 6; }
        public void write(DataOutputStream out) throws IOException { out.writeDouble(value); }
    }

    record ByteArray(byte[] values) implements Tag {
        public byte id() { return 7; }
        public void write(DataOutputStream out) throws IOException {
            out.writeInt(values.length);
            out.write(values);
        }
    }

    record Str(String value) implements Tag {
        public byte id() { return 8; }
        public void write(DataOutputStream out) throws IOException { out.writeUTF(value); }
    }
    
    record Boolean(boolean value) implements Tag {
        public byte id() { return 13; }
        public void write(DataOutputStream out) throws IOException { out.writeBoolean(value); }
    }

    // ---------------------------------------------------------------
    // Bileşik tag'ler (List / Compound)
    // ---------------------------------------------------------------

    /**
     * Homojen liste: tüm elemanlar aynı tag türünde olmalı (gerçek NBT gibi).
     * elementId, listenin boş olması durumunda bile hangi türde olduğunu belirtir.
     */
    final class ListTag implements Tag {
        private byte elementId;
        private final List<Tag> items = new ArrayList<>();

        public ListTag() { this.elementId = 0; }
        public ListTag(byte elementId) { this.elementId = elementId; }

        public byte id() { return 9; }
        public byte elementId() { return elementId; }
        public List<Tag> items() { return items; } // SequencedCollection - ekleme sırası korunur

        public ListTag add(Tag tag) {
            if (items.isEmpty() && elementId == 0) {
                elementId = tag.id();
            } else if (tag.id() != elementId) {
                throw new IllegalArgumentException(
                        "Liste tag id=%d bekliyor, gelen id=%d".formatted(elementId, tag.id()));
            }
            items.add(tag);
            return this;
        }

        public int size() { return items.size(); }
        public Tag get(int index) { return items.get(index); }

        public void write(DataOutputStream out) throws IOException {
            out.writeByte(elementId);
            out.writeInt(items.size());
            for (var item : items) item.write(out);
        }

        @Override
        public String toString() { return "ListTag" + items; }
    }

    /**
     * İsim -> Tag eşlemesi tutan bileşik yapı. LinkedHashMap kullanıldığı için
     * eklenme sırası korunur (SequencedMap, Java 21+).
     */
    final class Compound implements Tag {
        private final LinkedHashMap<String, Tag> entries = new LinkedHashMap<>();

        public byte id() { return 10; }
        public Map<String, Tag> entries() { return entries; }

        // --- fluent put helper'ları ---
        public Compound putByte(String key, byte v) { entries.put(key, new Byte(v)); return this; }
        public Compound putShort(String key, short v) { entries.put(key, new Short(v)); return this; }
        public Compound putInt(String key, int v) { entries.put(key, new Int(v)); return this; }
        public Compound putLong(String key, long v) { entries.put(key, new Long(v)); return this; }
        public Compound putFloat(String key, float v) { entries.put(key, new Float(v)); return this; }
        public Compound putDouble(String key, double v) { entries.put(key, new Double(v)); return this; }
        public Compound putString(String key, String v) { entries.put(key, new Str(v)); return this; }
        public Compound putByteArray(String key, byte[] v) { entries.put(key, new ByteArray(v)); return this; }
        public Compound putIntArray(String key, int[] v) { entries.put(key, new IntArray(v)); return this; }
        public Compound putLongArray(String key, long[] v) { entries.put(key, new LongArray(v)); return this; }
        public Compound putBoolean(String key, boolean v) { entries.put(key, new Boolean(v)); return this; }
        public Compound put(String key, Tag tag) { entries.put(key, tag); return this; }

        public boolean has(String key) { return entries.containsKey(key); }
        public Tag getRaw(String key) { return entries.get(key); }

        // --- tip güvenli getter'lar: pattern matching switch ile ---
        public byte getByte(String key, byte def) {
            return switch (entries.get(key)) {
                case Byte(var v) -> v;
                case null, default -> def;
            };
        }

        public int getInt(String key, int def) {
            return switch (entries.get(key)) {
                case Int(var v) -> v;
                case null, default -> def;
            };
        }

        public long getLong(String key, long def) {
            return switch (entries.get(key)) {
                case Long(var v) -> v;
                case null, default -> def;
            };
        }

        public float getFloat(String key, float def) {
            return switch (entries.get(key)) {
                case Float(var v) -> v;
                case null, default -> def;
            };
        }

        public double getDouble(String key, double def) {
            return switch (entries.get(key)) {
                case Double(var v) -> v;
                case null, default -> def;
            };
        }
        
        public boolean getBoolean(String key, boolean def) {
        	return switch (entries.get(key)) {
            	case Boolean(var v) -> v;
            	case null, default -> def;
        	};
        }

        public String getString(String key, String def) {
            return switch (entries.get(key)) {
                case Str(var v) -> v;
                case null, default -> def;
            };
        }

        public Compound getCompound(String key) {
            return switch (entries.get(key)) {
                case Compound c -> c;
                case null, default -> new Compound();
            };
        }

        public ListTag getList(String key) {
            return switch (entries.get(key)) {
                case ListTag l -> l;
                case null, default -> new ListTag();
            };
        }

        public byte[] getByteArray(String key) {
            return switch (entries.get(key)) {
                case ByteArray(var arr) -> arr;
                case null, default -> new byte[0];
            };
        }

        public int[] getIntArray(String key) {
            return switch (entries.get(key)) {
                case IntArray(var arr) -> arr;
                case null, default -> new int[0];
            };
        }

        public long[] getLongArray(String key) {
            return switch (entries.get(key)) {
                case LongArray(var arr) -> arr;
                case null, default -> new long[0];
            };
        }

        public void write(DataOutputStream out) throws IOException {
            for (var entry : entries.entrySet()) {
                var value = entry.getValue();
                out.writeByte(value.id());
                out.writeUTF(entry.getKey());
                value.write(out);
            }
            out.writeByte(0); // End tag ile kapat
        }

        /** Kök compound'u byte dizisine serialize eder (senin toBytes() pattern'in). */
        public byte[] toBytes() {
            try (var baos = new ByteArrayOutputStream();
                 var out = new DataOutputStream(baos)) {
                write(out);
                return baos.toByteArray();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        /** Byte dizisinden kök compound'u geri okur. */
        public static Compound fromBytes(byte[] bytes) {
            try (var in = new DataInputStream(new ByteArrayInputStream(bytes))) {
                return TagIO.readCompoundBody(in);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public String toString() { return "Compound" + entries; }
    }

    record IntArray(int[] values) implements Tag {
        public byte id() { return 11; }
        public void write(DataOutputStream out) throws IOException {
            out.writeInt(values.length);
            for (int v : values) out.writeInt(v);
        }
    }

    record LongArray(long[] values) implements Tag {
        public byte id() { return 12; }
        public void write(DataOutputStream out) throws IOException {
            out.writeInt(values.length);
            for (long v : values) out.writeLong(v);
        }
    }
}
