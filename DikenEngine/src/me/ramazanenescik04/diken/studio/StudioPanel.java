package me.ramazanenescik04.diken.studio;

import java.util.Objects;

import javax.swing.JFrame;
import javax.swing.JPanel;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.nodes.Sky;
import me.ramazanenescik04.diken.game.services.Lighting;
import me.ramazanenescik04.diken.renderer.RendererPanel;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IOResource;

import javax.swing.JToolBar;

import bibliothek.gui.dock.common.*;
import bibliothek.gui.dock.common.menu.SingleCDockableListMenuPiece;
import bibliothek.gui.dock.common.theme.ThemeMap;
import bibliothek.gui.dock.facile.menu.RootMenuPiece;

import java.awt.BorderLayout;

import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.Color;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class StudioPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private JFrame engineWindow;
	private RendererPanel gamePanel;
	private DikenEngine engine;
	private CControl control;
	
	private World theWorld;

	public StudioPanel(JFrame window, RendererPanel gamePanel, DikenEngine engine) {
		super(new BorderLayout());
		
		Objects.requireNonNull(window);
		Objects.requireNonNull(gamePanel);
		
		this.engineWindow = window;
		this.gamePanel = gamePanel;
		this.engine = Objects.requireNonNullElse(engine, DikenEngine.getEngine());
		
		init();
	}
	
	public void init() {
		theWorld = new World("Untitled World");
		theWorld.addResource("sky", IOResource.loadResource(DikenEngine.class.getResourceAsStream("/sky.png"), EnumResource.IMAGE));
		
		Lighting lighting = theWorld.getService("Lighting");
		Sky sky = new Sky(0xffffffff);
		sky.setTexture("sky");
		
		lighting.addChild(sky);
		lighting.setSky(sky);
		
		engine.setWorld(theWorld);
		
		control = new CControl();
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);

		var scriptTabPanel = new ScriptTabPanel(gamePanel);
		var explorerPanel = new ExplorerPanel(theWorld);
		var propertiesPanel = new PropertiesPanel(explorerPanel, engineWindow);
		var consolePanel = new ConsolePanel();
		var objectsPanel = new BasicObjectsPanel(node -> explorerPanel.addNodeToSelected(node));
		var resourcesPanel = new ResourcesPanel(theWorld, engineWindow);
		explorerPanel.addScriptOpenListener(script -> scriptTabPanel.openScript(script));
			
		this.add(control.getContentArea(), BorderLayout.CENTER);
		
		setPreferredSize(new Dimension(1280, 706));
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		JToolBar toolBar = new JToolBar();
		toolBar.setBackground(new Color(82, 82, 82));
		panel.add(toolBar, BorderLayout.SOUTH);
		
		addTools(toolBar);
		
		JMenuBar menuBar = new JMenuBar();
		panel.add(menuBar, BorderLayout.NORTH);
		
		addMenus(menuBar);
		
		CGrid grid = new CGrid(control);

        grid.add(1, 0, 3, 3, scriptTabPanel.getDockable());
        grid.add(1, 3, 3, 1, consolePanel.getDockable());
        grid.add(4, 0, 1, 2, explorerPanel.getDockable());
        grid.add(4, 2, 1, 2, propertiesPanel.getDockable());
        grid.add(0, 0, 1, 4, objectsPanel.getDockable());
        grid.add(0, 4, 1, 2, resourcesPanel.getDockable());

        // Tasarımı ekrana uygula
        control.getContentArea().deploy(grid);
	}

	public void stop() {
		engine.stop();
	}
	
	private void addTools(JToolBar toolBar) {
		JButton exitButton = new JButton("E");
		exitButton.setToolTipText("fdfd");
		toolBar.add(exitButton);
		
		JButton newaWorldButton = new JButton("NW");
		newaWorldButton.setToolTipText("fdfd");
		toolBar.add(newaWorldButton);
	}
	
	private void addMenus(JMenuBar menuBar) {
		JMenu fileMenu = new JMenu("Dosya");
		fileMenu.setMnemonic('f');
		menuBar.add(fileMenu);
		
		JMenuItem exitMenuItem = new JMenuItem("Uygulamadan Çık");
		fileMenu.add(exitMenuItem);
		
		RootMenuPiece settings = new RootMenuPiece("Pencereler", false);
        settings.add(new SingleCDockableListMenuPiece(control));
        
        menuBar.add(settings.getMenu());
        
        JMenu toolsMenu = new JMenu("Araçlar");
		menuBar.add(toolsMenu);
		
		JMenuItem executeScriptMenuItem = new JMenuItem("New menu item");
		toolsMenu.add(executeScriptMenuItem);
	}
}
