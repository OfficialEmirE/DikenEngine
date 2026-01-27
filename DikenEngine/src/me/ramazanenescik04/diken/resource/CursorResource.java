package me.ramazanenescik04.diken.resource;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;

public class CursorResource implements IResource {

    private static final long serialVersionUID = 1L;

    public Bitmap cursorBitmap;
    private transient long cursorHandle = 0;

    public CursorResource() {
        cursorBitmap = IOResource.missingTexture;
    }

    private long generateCursor() {
        if (cursorBitmap == null)
            cursorBitmap = IOResource.missingTexture;

        int width = cursorBitmap.w;
        int height = cursorBitmap.h;

        // RGBA ByteBuffer
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

        // OpenGL uyumlu Y flip
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = cursorBitmap.pixels.get(y * width + x);

                buffer.put((byte) ((pixel >> 16) & 0xFF)); // R
                buffer.put((byte) ((pixel >> 8) & 0xFF));  // G
                buffer.put((byte) (pixel & 0xFF));         // B
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // A
            }
        }

        buffer.flip();

        GLFWImage image = GLFWImage.malloc();
        image.set(width, height, buffer);

        // hotspot (eski kodundaki mantık)
        long cursor = GLFW.glfwCreateCursor(
                image,
                2,
                height - 2
        );

        image.free();

        return cursor;
    }

    public long getCursor() {
        if (cursorHandle == 0) {
            cursorHandle = generateCursor();
        }
        return cursorHandle;
    }

    @Override
    public void saveResource(DataOutputStream out) throws IOException {
        byte[] array = cursorBitmap.toBytes("png");
        out.writeInt(array.length);
        out.write(array);
    }

    @Override
    public void loadResource(DataInputStream in) throws IOException {
        int length = in.readInt();
        byte[] image = new byte[length];
        in.readFully(image);
        cursorBitmap = Bitmap.fromBytes(image);
    }

    public EnumResource getResourceType() {
        return EnumResource.CURSOR;
    }

    @Override
    public IResource clone() {
        CursorResource cloned = new CursorResource();
        cloned.cursorBitmap = this.cursorBitmap;
        return cloned;
    }
}
