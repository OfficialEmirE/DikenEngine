package me.ramazanenescik04.diken.game.io;

import java.io.*;

/**
 * Tag okuma mantığı. id byte'ına göre switch ile ilgili record'u üretir.
 * Java 25: exhaustive switch (sealed interface sayesinde default'a gerek yok
 * ama wire formatındaki bilinmeyen id'ler için yine de bir güvenlik ağı var).
 */
public final class TagIO {

    private TagIO() { }

    /** Tek bir tag'in payload'ını, verilen id'ye göre okur. */
    public static Tag readPayload(byte id, DataInputStream in) throws IOException {
        return switch (id) {
            case 0 -> new Tag.End();
            case 1 -> new Tag.Byte(in.readByte());
            case 2 -> new Tag.Short(in.readShort());
            case 3 -> new Tag.Int(in.readInt());
            case 4 -> new Tag.Long(in.readLong());
            case 5 -> new Tag.Float(in.readFloat());
            case 6 -> new Tag.Double(in.readDouble());
            case 7 -> readByteArray(in);
            case 8 -> new Tag.Str(in.readUTF());
            case 9 -> readList(in);
            case 10 -> readCompoundBody(in);
            case 11 -> readIntArray(in);
            case 12 -> readLongArray(in);
            case 13 -> new Tag.Boolean(in.readBoolean());
            default -> throw new IOException("Bilinmeyen tag id: " + id);
        };
    }

    private static Tag.ByteArray readByteArray(DataInputStream in) throws IOException {
        int len = in.readInt();
        byte[] values = new byte[len];
        in.readFully(values);
        return new Tag.ByteArray(values);
    }

    private static Tag.IntArray readIntArray(DataInputStream in) throws IOException {
        int len = in.readInt();
        int[] values = new int[len];
        for (int i = 0; i < len; i++) values[i] = in.readInt();
        return new Tag.IntArray(values);
    }

    private static Tag.LongArray readLongArray(DataInputStream in) throws IOException {
        int len = in.readInt();
        long[] values = new long[len];
        for (int i = 0; i < len; i++) values[i] = in.readLong();
        return new Tag.LongArray(values);
    }

    private static Tag.ListTag readList(DataInputStream in) throws IOException {
        byte elementId = in.readByte();
        int count = in.readInt();
        var list = new Tag.ListTag(elementId);
        for (int i = 0; i < count; i++) {
            list.add(readPayload(elementId, in));
        }
        return list;
    }

    /** Compound gövdesini (End tag'e kadar) okur; kök seviyede de, iç içe de kullanılır. */
    static Tag.Compound readCompoundBody(DataInputStream in) throws IOException {
        var compound = new Tag.Compound();
        while (true) {
            byte id = in.readByte();
            if (id == 0) break; // End tag
            String name = in.readUTF();
            compound.put(name, readPayload(id, in));
        }
        return compound;
    }

    /**
     * Herhangi bir tag'i güzel/okunabilir şekilde yazdırır.
     * Java 25 record pattern matching + unnamed variable ("_") kullanımına örnek:
     * iç değeri kullanmadığımız durumlarda değişkeni isimlendirmiyoruz.
     */
    public static String describe(Tag tag) {
        return switch (tag) {
            case Tag.End _ -> "<end>";
            case Tag.Byte(var v) -> "byte(" + v + ")";
            case Tag.Short(var v) -> "short(" + v + ")";
            case Tag.Int(var v) -> "int(" + v + ")";
            case Tag.Long(var v) -> "long(" + v + ")";
            case Tag.Float(var v) -> "float(" + v + ")";
            case Tag.Double(var v) -> "double(" + v + ")";
            case Tag.ByteArray(var arr) -> "byte[" + arr.length + "]";
            case Tag.Str(var s) -> "\"" + s + "\"";
            case Tag.IntArray(var arr) -> "int[" + arr.length + "]";
            case Tag.LongArray(var arr) -> "long[" + arr.length + "]";
            case Tag.ListTag list -> "list<" + list.size() + " öğe>";
            case Tag.Boolean(var v) -> "boolean(" + v +  ")";
            case Tag.Compound c -> "compound{" + c.entries().size() + " alan}";
        };
    }
}
