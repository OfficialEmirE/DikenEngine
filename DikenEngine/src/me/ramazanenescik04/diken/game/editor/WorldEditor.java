package me.ramazanenescik04.diken.game.editor;

import java.io.File;
import java.io.IOException;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.InputHandler;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.entity.Player;
import me.ramazanenescik04.diken.game.nodes.Part;
import me.ramazanenescik04.diken.game.nodes.Sky;
import me.ramazanenescik04.diken.game.nodes.Tool;
import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.Panel;
import me.ramazanenescik04.diken.gui.screen.Screen;
import me.ramazanenescik04.diken.gui.screen.StaticBackground;
import me.ramazanenescik04.diken.resource.Bitmap;

public class WorldEditor extends Screen {
	
	protected World theWorld;
	protected Player testPlayer;
	private Node selectedNode = new Part(0, 0, 16, 16);
	protected boolean isPlayTestMode = false;
	
	protected Panel topPanel;

	public WorldEditor() {
		this.testPlayer = new Player(32, 64);

		this.theWorld = new World("", 100, 100) {
			@Override
			public void mouseClicked(int relMouseX, int relMouseY, int button, boolean isTouch2) {
				super.mouseClicked(relMouseX, relMouseY, button, isTouch2);
				
				if (button == 0) {
					theWorld.addNode(selectedNode.copy());
				}
			}

			@Override
			public Bitmap render() {
				var bitmap = super.render();
				bitmap.blendDraw(selectedNode.render(), InputHandler.getMousePosition().x - this.x, InputHandler.getMousePosition().y - this.y, 0xaaffffff);
				return bitmap;
			}
		};
		this.theWorld.addNode(new Sky(0xffcefbf9 + 0xff7d7d7d));
		Tool tool = new Tool();
		Part handle = new Part();
		handle.name = "handle";
		handle.color = 0xffff0000;
		tool.addChild(handle);
		this.theWorld.addNode(tool);
	}
	
	public void startPlayTest() {
		this.theWorld.addNode(testPlayer);
		isPlayTestMode = true;
	}
	
	public void stopPlayTest() {
		this.theWorld.removeNode(testPlayer);
		isPlayTestMode = false;
	}
	
	public void openScreen() {		
		topPanel = new Panel(0, 0, engine.getWidth(), 52);
		topPanel.setBackground(new StaticBackground(Bitmap.createClearedBitmap(64, 64, 0xffffffff)));
		
		topPanel.add(new Button("Play", 2, 2, 20, 20).setRunnable(() -> {
			startPlayTest();
		}));
		
		topPanel.add(new Button("Pause", 24, 2, 20, 20).setRunnable(() -> {
			stopPlayTest();
		}));
		
		topPanel.add(new Button("Export", 46, 2, 40, 20).setRunnable(() -> {
			if (!this.isPlayTestMode) {
				try {
					World.saveWorld(theWorld, new File("./world.dew"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}));
		
		this.theWorld.setBounds(0, 52, engine.getWidth(), engine.getHeight() - 52);
		this.getContentPane().add(theWorld);
		this.getContentPane().add(topPanel);
	}
	
	public void render(Bitmap bitmap) {
		super.render(bitmap);
		
		bitmap.drawLine(0, 52, engine.getWidth(), 52, 0xffffffff, 1);
	}

	@Override
	public void tick() {
		super.tick();
		this.testPlayer.centerCamera(theWorld, engine);
		
		selectedNode.x = (int) (InputHandler.getMousePosition().x + theWorld.camera.x()) - this.theWorld.x;
		selectedNode.y = (int) (InputHandler.getMousePosition().y + theWorld.camera.y()) - this.theWorld.y;
	}

	@Override
	public void resized() {
		this.topPanel.setBounds(0, 0, engine.getWidth(), 52);
		this.theWorld.setBounds(0, 52, engine.getWidth(), engine.getHeight() - 52);
	}

	public static void main(String[] args) {
		DikenEngine engine = new DikenEngine(null, 320 * 2, 240 * 2, 2);
		engine.setCurrentScreen(new WorldEditor());
		engine.setTitle("DikenEngine - World Editor");
		engine.start();
	}

}
