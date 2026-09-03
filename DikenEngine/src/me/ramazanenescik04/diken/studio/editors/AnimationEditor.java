package me.ramazanenescik04.diken.studio.editors;

import me.ramazanenescik04.diken.game.Animation;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.language.Lang;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IOResource;

import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JPanel;

public class AnimationEditor extends ResourceEditor<Animation> implements Runnable {
	private static final long serialVersionUID = 1L;
	
	private static final Border PLAYING_BORDER =
		    BorderFactory.createLineBorder(new Color(0, 180, 0), 2);

	private static final Border PAUSED_BORDER =
		    BorderFactory.createLineBorder(new Color(220, 170, 0), 2);
	
	private JList<Bitmap> frameList;
	private BitmapRenderer frameRenderer;
	private Bitmap selectedFrame;

	private volatile boolean animationPlaying;
	private volatile boolean animationPaused;
	private Thread thread;

	public AnimationEditor(World world, String resourceKey) {
		super(Lang.get("studio.editor.animationEditor", resourceKey), world, resourceKey, new Animation(8));
		
		var toolbar = this.builder.create("animationToolbar");
		this.builder.addButton(toolbar, "loadImage", 9, 0, "studio.editor.animationEditor.load", this::addFrame);
		this.builder.addButton(toolbar, "deleteImage", 0, 0, "studio.editor.animationEditor.delete", this::removeFrame);
		this.builder.addButton(toolbar, "play", 3, 0, "studio.editor.animationEditor.play", this::playAnimation);
		this.builder.addButton(toolbar, "pause", 14, 0, "studio.editor.animationEditor.pause", this::pauseAnimation);
		this.builder.addButton(toolbar, "stop", 15, 0, "studio.editor.animationEditor.stop", this::stopAnimation);
		this.builder.addButton(toolbar, "setFps", 1, 3, "studio.editor.animationEditor.setFps", this::setFPS);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.25);
		add(splitPane, BorderLayout.CENTER);
		
		frameList = new JList<>();
		frameList.setSelectionMode(
			    ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
			);
		frameList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onFrameSelected();
        });
		frameList.setCellRenderer((list, _, index, isSelected, _) -> {
		    JLabel label = new JLabel("Frame " + index);

		    if (isSelected) {
		        label.setBackground(list.getSelectionBackground());
		        label.setForeground(list.getSelectionForeground());
		        label.setOpaque(true);
		    }

		    return label;
		});
		frameList.setDragEnabled(true);
		frameList.setDropMode(DropMode.INSERT);
		frameList.setTransferHandler(new FrameTransferHandler());
		splitPane.setLeftComponent(frameList);
		
		frameRenderer = new BitmapRenderer();
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(frameRenderer);
		splitPane.setRightComponent(scrollPane);
		
		refreshList();
	}
	
	private void onFrameSelected() {
		this.selectedFrame = frameList.getSelectedValue();
		
		frameRenderer.setBitmap(selectedFrame);
	}
	
	private void addFrame() {
		JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(Lang.get("resources.selectResourceFile"));
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg", "bmp", "gif"));

        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File[] selectedFiles = fileChooser.getSelectedFiles();
        
        if (selectedFiles.length == 0) return;
        
        try {
        	for (File file : selectedFiles)
        		this.resource.second.addFrame((Bitmap) IOResource.loadResource(new FileInputStream(file), EnumResource.IMAGE));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		refreshList();
	}
	
	private void removeFrame() {
		this.resource.second.removeFrame(selectedFrame);
		refreshList();
	}
	
	public void playAnimation() {
		if (animationPlaying)
	        return;
		
		thread = new Thread(this, this.resource.first + " Animation Thread");
		thread.setDaemon(true);
		thread.start();
	}
	
	public void pauseAnimation() {
	    animationPaused = !animationPaused;
	    updateBorder();
	}
	
	public void stopAnimation() {
		animationPlaying = false;
		
		if (thread != null) {
	        thread.interrupt();
	    }
		
		this.frameList.setSelectedIndex(0);
		onFrameSelected();
		updateBorder();
	}
	
	private void updateBorder() {
	    if (animationPlaying) {
	        frameRenderer.setBorder(animationPaused
	                ? PAUSED_BORDER
	                : PLAYING_BORDER);
	    } else {
	        frameRenderer.setBorder(null);
	    }
	}
	
	private void refreshList() {
		Animation anim = this.resource.second;
		frameList.setListData(anim.getFrames());
	}
	
	private void setFPS() {
        String input = JOptionPane.showInputDialog(this, Lang.get("studio.editor.animationEditor.newFps"), this.resource.second.getFPS());
        try {
            int fps = Integer.parseInt(input);
            this.resource.second.setFPS(fps);
        } catch (NumberFormatException ignored) {}
    }

	@Override
	public void run() {
		animationPlaying = true;
		updateBorder();
		
		Bitmap[] frames = resource.second.getFrames();
	    int index = 0;
		
		long nextFrame = System.nanoTime();

		while (animationPlaying) {
			if (animationPaused) {
		        try {
		            Thread.sleep(20);
		        } catch (InterruptedException e) {
		            break;
		        }
		        continue;
		    }
			
			long frameTime = 1_000_000_000L / resource.second.getFPS();
			
		    frameRenderer.setBitmap(frames[index]);
		    frameList.setSelectedIndex(index);
		    index = (index + 1) % frames.length;

		    nextFrame += frameTime;

		    long sleep = nextFrame - System.nanoTime();
		    if (sleep > 0) {
		        try {
					Thread.sleep(sleep / 1_000_000, (int) (sleep % 1_000_000));
				} catch (InterruptedException e) {}
		    }
		}
	}
	
	private class BitmapRenderer extends JPanel {
		private static final long serialVersionUID = -1240024138160262899L;
		
		private Bitmap bitmap;

		public void setBitmap(Bitmap bitmap) {
			this.bitmap = bitmap;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
		    super.paintComponent(g);
		    
		    int size = 16;

		    for (int y = 0; y < getHeight(); y += size) {
		        for (int x = 0; x < getWidth(); x += size) {
		            boolean dark = ((x / size) + (y / size)) % 2 == 0;
		            g.setColor(dark
		                    ? new Color(180, 180, 180)
		                    : new Color(220, 220, 220));
		            g.fillRect(x, y, size, size);
		        }
		    }

		    if (bitmap != null) {
		        int halfWidth = getWidth() / 2;
		        int halfHeight = getHeight() / 2;

		        g.drawImage(bitmap.toImage(),
		                halfWidth - (bitmap.w / 2),
		                halfHeight - (bitmap.h / 2),
		                null);
		    } else {
		    	String text = "No Frames";
		        String hint = "Click \"Load Image\" to add";

		        var fm = g.getFontMetrics();

		        int x = (getWidth() - fm.stringWidth(text)) / 2;
		        int y = getHeight() / 2 - fm.getHeight() / 2;

		        g.setColor(java.awt.Color.GRAY);
		        g.drawString(text, x, y);

		        x = (getWidth() - fm.stringWidth(hint)) / 2;
		        g.drawString(hint, x, y + fm.getHeight() + 4);
		    }
		    
		    drawInfoBox(
		    	    g,
		    	    "Frame " + (frameList.getSelectedIndex() + 1)
		    	            + " / " + resource.second.getFrameCount(),
		    	    5,
		    	    g.getFontMetrics().getAscent() + 5
		    	);

		    drawInfoBox(
		    	    g,
		    	    "FPS: " + resource.second.getFPS(),
		    	    5,
		    	    getHeight() - g.getFontMetrics().getDescent() - 5
		    	);
		}
		
		private void drawInfoBox(Graphics g, String text, int x, int y) {
		    var fm = g.getFontMetrics();
		    int padding = 4;

		    int w = fm.stringWidth(text) + padding * 2;
		    int h = fm.getHeight() + padding * 2;

		    g.setColor(new java.awt.Color(0, 0, 0, 170));
		    g.fillRoundRect(
		            x - padding,
		            y - fm.getAscent() - padding,
		            w,
		            h,
		            8,
		            8);

		    g.setColor(java.awt.Color.WHITE);
		    g.drawString(text, x, y);
		}
	}
	
	private class FrameTransferHandler extends TransferHandler {
		private static final long serialVersionUID = 1L;

		private final DataFlavor flavor = DataFlavor.stringFlavor;

		@Override
		protected Transferable createTransferable(JComponent c) {
			int index = frameList.getSelectedIndex();
			return new StringSelection(Integer.toString(index));
		}

		@Override
		public int getSourceActions(JComponent c) {
			return MOVE;
		}

		@Override
		public boolean canImport(TransferSupport support) {
			return support.isDrop() && support.isDataFlavorSupported(flavor);
		}

		@Override
		public boolean importData(TransferSupport support) {
			if (!canImport(support))
				return false;

			try {
				JList.DropLocation dropLocation =
					(JList.DropLocation) support.getDropLocation();

				int from = Integer.parseInt(
					(String) support.getTransferable().getTransferData(flavor));

				int to = dropLocation.getIndex();

				resource.second.moveFrame(from, to);

				refreshList();

				if (to > from)
					to--;

				frameList.setSelectedIndex(to);

				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}
}
