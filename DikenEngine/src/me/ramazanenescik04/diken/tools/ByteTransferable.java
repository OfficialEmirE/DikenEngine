package me.ramazanenescik04.diken.tools;
import java.awt.datatransfer.*;

public class ByteTransferable implements Transferable {
    // Kendi MIME tipimizi tanımlıyoruz (Bayt dizisi için)
    public static final DataFlavor BYTE_ARRAY_FLAVOR = new DataFlavor(byte[].class, "Byte Array");

    private byte[] data;

    public ByteTransferable(byte[] data) {
        this.data = data;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{BYTE_ARRAY_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return BYTE_ARRAY_FLAVOR.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
        return data;
    }
}