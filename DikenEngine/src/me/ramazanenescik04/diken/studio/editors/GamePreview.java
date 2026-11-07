package me.ramazanenescik04.diken.studio.editors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.renderer.RendererPanel;
import me.ramazanenescik04.diken.studio.NotificationHelper;
import me.ramazanenescik04.diken.studio.builders.Toolbar;

public class GamePreview extends BaseEditor {

	private static final long serialVersionUID = 1L;

	private NotificationHelper notificationHelper;
	private final RendererPanel rendererPanel;
	private final JPanel canvasContainer;

	private DisplayType type = DisplayType.Native;

	private enum DisplayType {

		Native(null), Mobile(new Dimension(320, 240));

		private static int id = 0;

		public final Dimension size;

		DisplayType(Dimension size) {
			this.size = size;
		}

		public static DisplayType nextOne() {
			var values = values();

			id = (id + 1) % values.length;

			return values[id];
		}
	}

	public GamePreview(RendererPanel panel, NotificationHelper notification) {
		super("Game Preview");

		this.closeable = false;
		this.rendererPanel = panel;
		this.notificationHelper = notification;

		setLayout(new BorderLayout());

		canvasContainer = new JPanel(new BorderLayout());
		canvasContainer.setBackground(Color.DARK_GRAY);

		canvasContainer.add(rendererPanel, BorderLayout.CENTER);

		add(canvasContainer, BorderLayout.CENTER);
	}

	private void pressButton() {
	    type = DisplayType.nextOne();

	    if (type.size == null) {
	        rendererPanel.setPreferredSize(null);

	        canvasContainer.setLayout(new BorderLayout());
	        canvasContainer.add(rendererPanel, BorderLayout.CENTER);
	    } else {
	        rendererPanel.setPreferredSize(type.size);

	        canvasContainer.setLayout(new GridBagLayout());
	        canvasContainer.add(rendererPanel);
	    }
	    
	    canvasContainer.revalidate();
	    canvasContainer.repaint();
	    
	    var textSize = type.size == null ? new Dimension(0, 0) : type.size;
	    
	    notificationHelper.showNotification(
	    		"Changed Device Size", 
	    		"New Size: %s (%dx%d)".formatted(type.name(), textSize.width, textSize.height), 
	    		JOptionPane.INFORMATION_MESSAGE, 
	    		2000
	    );
	}

	@Override
	public Toolbar.Builder getToolbar() {
		var builder = new Toolbar.Builder();

		var deviceEmulatorToolbar = builder.create("DeviceEmulator-Toolbar");

		builder.addButton(deviceEmulatorToolbar, "deviceEmulator", 2, 14, "Device Emulator", this::pressButton);

		return builder;
	}
}