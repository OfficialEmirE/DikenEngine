package me.ramazanenescik04.diken.studio.dialog;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.FutureTask;

import javax.swing.*;
import javax.swing.event.ChangeListener;

import me.ramazanenescik04.diken.language.Lang;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;

public class CropDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private final Bitmap originalImage;
    private final JLabel imageLabel;

    private final JSpinner xField;
    private final JSpinner yField;
    
    private final FutureTask<ArrayBitmap> theFuture;

    public CropDialog(Frame owner, Bitmap original) {
        super(owner, Lang.get("resources.cutImage"), true);

        this.originalImage = original;
        this.theFuture = new FutureTask<ArrayBitmap>(() -> {
        	return new ArrayBitmap(originalImage.cutImage(getXValue(), getYValue()));
        });

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        xField = new JSpinner(
        	    new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));

        yField = new JSpinner(
        	    new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));

        xField.setValue(16);
        yField.setValue(16);

        ChangeListener listener = _ -> updatePreview();

        xField.addChangeListener(listener);
        yField.addChangeListener(listener);

        JPanel valuePanel = new JPanel(new FlowLayout());
        valuePanel.add(new JLabel("X:"));
        valuePanel.add(xField);
        valuePanel.add(Box.createHorizontalStrut(15));
        valuePanel.add(new JLabel("Y:"));
        valuePanel.add(yField);

        JButton cancelButton = new JButton(Lang.get("cancel"));
        JButton cropButton = new JButton(Lang.get("explorer.cut"));

        cancelButton.addActionListener(_ -> {
        	theFuture.cancel(true);
        	
        	dispose();
        });

        cropButton.addActionListener(_ -> {
            theFuture.run();
        	
            dispose();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(cancelButton);
        buttonPanel.add(cropButton);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.add(valuePanel);
        southPanel.add(buttonPanel);

        setLayout(new BorderLayout(5, 5));
        add(new JScrollPane(imageLabel), BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        updatePreview();

        setPreferredSize(new Dimension(600, 500));
        pack();
        setLocationRelativeTo(owner);
    }
    
    public ArrayBitmap get() throws Exception {
    	if (this.theFuture.isDone() && !this.theFuture.isCancelled()) {
    		return this.theFuture.get();
    	}
    	
    	return null;
    }

    private int getXValue() {
        Number n = (Number) xField.getValue();
        return n == null ? 0 : n.intValue();
    }

    private int getYValue() {
        Number n = (Number) yField.getValue();
        return n == null ? 0 : n.intValue();
    }

    private void updatePreview() {
        int x = getXValue();
        int y = getYValue();

        BufferedImage preview = new BufferedImage(
                originalImage.w,
                originalImage.h,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = preview.createGraphics();

        g.drawImage(originalImage.toImage(), 0, 0, null);

        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(2));
        
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < preview.getWidth(); i += x)
            g.drawLine(i, 0, i, preview.getHeight());

        for (int i = 0; i < preview.getHeight(); i += y)
            g.drawLine(0, i, preview.getWidth(), i);

        g.dispose();

        imageLabel.setIcon(new ImageIcon(preview));
    }
}