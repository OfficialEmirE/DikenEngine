package me.ramazanenescik04.diken.studio;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.world.World;
import me.ramazanenescik04.diken.gui.component.Button;
import me.ramazanenescik04.diken.gui.component.GuiComponent;
import me.ramazanenescik04.diken.gui.component.Panel;
import me.ramazanenescik04.diken.gui.screen.GameScreen;
import me.ramazanenescik04.diken.gui.window.OptionWindow;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.FrameBitmapPool;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class StudioToolbarPanel extends Panel {
	private static final long serialVersionUID = 1L;
	
	private StudioScreen screen;
	
	public StudioToolbarPanel(StudioScreen screen) {
		this.screen = screen;
	}

	@Override
	public void init(DikenEngine engine) {
		ArrayBitmap icons = (ArrayBitmap) ResourceLocator.getResource("editor_icons");
		Button backButton = new StudioToolbarButton(icons.getBitmap(7, 0), 6, 4, 20, 20, "Back to Main Menu").setRunnable(() -> {
			if (screen.parent != null) {
				engine.setCurrentScreen(screen.parent);
			}
		});
		add(backButton);
		
		Button newButton = new StudioToolbarButton(icons.getBitmap(10, 0), 30, 4, 20, 20, "New Game").setRunnable(() -> {
			OptionWindow.showMessageNoWait("Are you sure you want to start a new game project?", "Are you sure?", OptionWindow.PLAIN_MESSAGE, OptionWindow.YES_NO_OPTION, (i) -> {
				if (i == OptionWindow.YES_BUTTON) {
					var oldWorld = screen.world;
					screen.world = screen.createDefaultWorld();
					screen.rebuildAnything(oldWorld);
					screen.resized();
				}
			});
		});
		add(newButton);
		
		Button saveButton = new StudioToolbarButton(icons.getBitmap(8, 0), 51, 4, 20, 20, "Save Game").setRunnable(() -> {
			var file = openFilePicker(false);
			
			if (file == null)
				return;
			
			try {
				var savePath = file.getAbsolutePath();
				
				if (!(savePath.endsWith(".cglf") || savePath.endsWith(".dew"))) {
					savePath = new File(savePath + ".cglf").getAbsolutePath();
				}
				
				World.saveWorld(screen.world, new File(savePath));
				
				OptionWindow.showMessageNoWait("Game Project Successfully Saved!", "Successfully Saved", OptionWindow.INFO_MESSAGE, OptionWindow.OK_BUTTON, _ -> {});
			} catch (IOException e) {
				e.printStackTrace();
				OptionWindow.showMessageNoWait("A problem occurred while saving the game project!\n" + e.getMessage(), "Error", OptionWindow.ERROR_MESSAGE, OptionWindow.OK_BUTTON, _ -> {});
			}
		});
		add(saveButton);
		
		Button focusRootButton = new StudioToolbarButton(icons.getBitmap(9, 0), 72, 4, 20, 20, "Load Game").setRunnable(() -> {
			var file = openFilePicker(true);

			if (file == null)
				return;
			
			try {
				var oldWorld = screen.world;
				screen.world = World.loadWorld(file);
				screen.rebuildAnything(oldWorld);
				screen.resized();
			} catch (IOException | ReflectiveOperationException e) {
				e.printStackTrace();
				OptionWindow.showMessageNoWait("A problem occurred while loading the game project!\n" + e.getMessage(), "Error", OptionWindow.ERROR_MESSAGE, OptionWindow.OK_BUTTON, _ -> {});
			}
		});
		add(focusRootButton);

		screen.selectToolButton = new StudioToolbarButton(icons.getBitmap(4, 0), 96, 4, 20, 20, "Select a Node").setRunnable(() -> {
			screen.activeTool = StudioScreen.TOOL_SELECT;
			screen.resetGameManipulation();
			screen.updateToolbarToolButtons();
		});
		add(screen.selectToolButton);
		
		screen.moveToolButton = new StudioToolbarButton(icons.getBitmap(5, 0), 117, 4, 20, 20, "Move the Node").setRunnable(() -> {
			screen.activeTool = StudioScreen.TOOL_MOVE;
			screen.resetGameManipulation();
			screen.updateToolbarToolButtons();
		});
		add(screen.moveToolButton);
		
		screen.resizeToolButton = new StudioToolbarButton(icons.getBitmap(6, 0), 138, 4, 20, 20, "Resize the Mode").setRunnable(() -> {
			screen.activeTool = StudioScreen.TOOL_RESIZE;
			screen.resetGameManipulation();
			screen.updateToolbarToolButtons();
		});
		add(screen.resizeToolButton);
		
		Button playButton = new StudioToolbarButton(icons.getBitmap(3, 0), 162, 4, 20, 20, "Test the game").setRunnable(() -> {
			engine.setCurrentScreen(new GameScreen(screen, screen.world, true));
		});
		add(playButton);
		
		Button addPartButton = new StudioToolbarButton(icons.getBitmap(2, 0), 183, 4, 20, 20, "Add Instance").setRunnable(() -> {
			screen.addInstanceToNode(screen.selectedNode);
		});
		add(addPartButton);
		
		ArrayBitmap winIcons = (ArrayBitmap) ResourceLocator.getResource("win-icons");
		Button resourcesButton = new StudioToolbarButton(winIcons.getBitmap(8, 0), 204, 4, 20, 20, "Edit Resources").setRunnable(() -> {
			if (engine.wManager.isWindowVaild(StudioResourceWindow.class)) {
				return;
			}
			
			engine.wManager.addWindow(new StudioResourceWindow(screen.world), true);
		});
		add(resourcesButton);
	}
	
	public void resized() {
	}

	@Override
	public Bitmap render() {
		Bitmap drawToolInfo = null;
		Bitmap bitmap = FrameBitmapPool.newBitmap(width, height);
		bitmap.fill(0, 0, width, height, 0xff2b313a);
		bitmap.box(0, 0, width - 1, height - 1, 0xff5e6a7d);
		bitmap.fill(0, height - 2, width, height, 0xff1d222a);
		for (GuiComponent component : getCompoments()) {
			if (component != null && component.isVisible()) {
				bitmap.draw(component.render(), component.x, component.y);
				
				if (component instanceof StudioToolbarButton button && button.isTouchingMouse()) {
					drawToolInfo = button.renderToolInfo();
				}
			}
		}
		
		if (drawToolInfo != null) {
			bitmap.draw(drawToolInfo, width - drawToolInfo.w - 10, 10);
		}
		return bitmap;
	}
	
	private File openFilePicker(boolean type) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(type ? "Load" : "Save" + " World");
		
		chooser.setFileFilter(new FileNameExtensionFilter("Capsule Game Local File", "cglf", "dew"));
		
		int result;
		if (type) {
			result = chooser.showOpenDialog(null);
		} else {
			result = chooser.showSaveDialog(null);	
		}
		
		if (result == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}
}

