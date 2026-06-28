package me.ramazanenescik04.diken.studio;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.ToolTipManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.event.CDockableStateListener;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.DefaultCDockable;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.gui.dock.common.theme.ThemeMap;
import me.ramazanenescik04.diken.CrashDialog;
import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Config;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.nodes.Camera.CameraType;
import me.ramazanenescik04.diken.game.nodes.Sky;
import me.ramazanenescik04.diken.game.services.Lighting;
import me.ramazanenescik04.diken.renderer.RendererPanel;
import me.ramazanenescik04.diken.scripting.Script;
import me.ramazanenescik04.diken.studio.dockables.AIAssistantPanel;
import me.ramazanenescik04.diken.studio.dockables.BasicObjectsPanel;
import me.ramazanenescik04.diken.studio.dockables.ConsolePanel;
import me.ramazanenescik04.diken.studio.dockables.ExplorerPanel;
import me.ramazanenescik04.diken.studio.dockables.PropertiesPanel;
import me.ramazanenescik04.diken.studio.dockables.ResourcesPanel;
import me.ramazanenescik04.diken.studio.dockables.ScriptTabPanel;
import me.ramazanenescik04.diken.tools.Utils;

public class StudioPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private JFrame engineWindow;
	private RendererPanel gamePanel;
	private DikenEngine engine;
	
	private CControl control;
	private File layoutFile = new File(Config.defaultConfigFile.getParentFile(), "layout.dat");
	private File selectedWorldFile;
	
	private World editWorld;
	
	private ScriptTabPanel scriptTabPanel;
	private ExplorerPanel explorerPanel;
	private PropertiesPanel propertiesPanel;
	private ConsolePanel consolePanel;
	private BasicObjectsPanel objectsPanel;
	private ResourcesPanel resourcesPanel;
	private AIAssistantPanel assistantPanel;

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
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		
		editWorld = new World("Untitled World");
		
		Lighting lighting = editWorld.getService("Lighting");
		Sky sky = new Sky(0xffffffff);
		sky.setTexture("sky");
		
		lighting.addChild(sky);
		lighting.setSky(sky);
		
		engine.setWorld(editWorld);
		
		control = new CControl();
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);

		explorerPanel = new ExplorerPanel(editWorld);
		propertiesPanel = new PropertiesPanel(explorerPanel, engineWindow);
		consolePanel = new ConsolePanel();
		objectsPanel = new BasicObjectsPanel(node -> explorerPanel.addNodeToSelected(node));
		resourcesPanel = new ResourcesPanel(editWorld, engineWindow);
		assistantPanel = new AIAssistantPanel();
		scriptTabPanel = new ScriptTabPanel(gamePanel, editWorld);
		explorerPanel.addNodeOpenListener(node -> {
			if (node instanceof Script script) {
				scriptTabPanel.openScript(script);
			}
		});
			
		this.add(control.getContentArea(), BorderLayout.CENTER);
		
		setPreferredSize(new Dimension(1280, 706));
		
		CGrid grid = new CGrid(control);

        grid.add(1, 0, 3, 3, scriptTabPanel.getDockable());
        grid.add(1, 3, 3, 1, consolePanel.getDockable());
        grid.add(4, 0, 1, 2, explorerPanel.getDockable(), assistantPanel.getDockable());
        grid.add(4, 2, 1, 2, propertiesPanel.getDockable());
        grid.add(0, 0, 1, 4, objectsPanel.getDockable());
        grid.add(0, 4, 1, 2, resourcesPanel.getDockable());

        // Tasarımı ekrana uygula
        control.getContentArea().deploy(grid);
        
        try {
			control.read(layoutFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		var toolBar = generateToolbar();
		toolBar.setBackground(new Color(82, 82, 82));
		panel.add(toolBar, BorderLayout.SOUTH);

		JMenuBar menuBar = generateMenuBar(explorerPanel, scriptTabPanel);
		panel.add(menuBar, BorderLayout.NORTH);	
	}
	
	public void loadWorld(World world) {
		this.editWorld = world;
		this.engine.setWorld(world);
		
		this.explorerPanel.reloadWorld(world);
		this.resourcesPanel.reloadWorld(world);
		this.scriptTabPanel.reloadWorld(world);
	}
	
	public void startPlayTest() {
		var world = this.editWorld.copy();
		world.getRunService().run();
		this.engine.setWorld(world);
		
		this.explorerPanel.reloadWorld(world);
		this.resourcesPanel.reloadWorld(world);
		this.scriptTabPanel.reloadWorld(world);
	}
	
	public void stopPlayTest() {
		this.engine.getWorld().getRunService().stop();
		var world = this.editWorld;
		world.getCameraNode().setCameraType(CameraType.NONE);
		this.engine.setWorld(world);
		
		this.explorerPanel.reloadWorld(world);
		this.resourcesPanel.reloadWorld(world);
		this.scriptTabPanel.reloadWorld(world);
	}

	public void stop() {
		String worldName = this.editWorld.gameName;
		if (selectedWorldFile == null) {
			worldName = "*" + worldName;
		}
		
		int saveWorld = JOptionPane.showConfirmDialog(engineWindow, worldName + "'u Kaydetmek İster Misin?");
		
		if (saveWorld == JOptionPane.YES_OPTION) {
			saveWorld();
		} else if (saveWorld == JOptionPane.CANCEL_OPTION) {
			return;
		}
		
		try {
			control.write(layoutFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		engine.stop();
	}
	
	public void tick() {
		var camera = editWorld.getCameraNode();
		
		if (camera.getCameraType() == CameraType.NONE && engine.getWorld() == editWorld) {
			if (engine.input.isKeyDown(KeyEvent.VK_W)) {
				editWorld.camera.y -= 2;
			}
			
			if (engine.input.isKeyDown(KeyEvent.VK_A)) {
				editWorld.camera.x -= 2;
			}
			
			if (engine.input.isKeyDown(KeyEvent.VK_S)) {
				editWorld.camera.y += 2;
			}
			
			if (engine.input.isKeyDown(KeyEvent.VK_D)) {
				editWorld.camera.x += 2;
			}
		}
	}
	
	private void newWorld() {
		var newWorld = new World("Untitled World");
		
		Lighting lighting = newWorld.getService("Lighting");
		Sky sky = new Sky(0xffffffff);
		sky.setTexture("sky");
		
		lighting.addChild(sky);
		lighting.setSky(sky);
		
		loadWorld(newWorld);
	}
	
	private void loadWorld() {
		JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Yükleyeceğin Dünyayı Seç");
		fileChooser.setFileFilter(new FileNameExtensionFilter("DikenEngine World File", "dew"));

        int result = fileChooser.showOpenDialog(engineWindow);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = fileChooser.getSelectedFile();
        
        try {
			loadWorld(World.loadWorld(selectedFile));
			
			this.selectedWorldFile = selectedFile;
		} catch (IOException | ReflectiveOperationException e) {
			CrashDialog.crash(engineWindow, e, "Dünya Yükleme Başarısızlıkla Sonuçlandı!");
		}
	}
	
	private void saveWorld() {
		File selectedFile = this.selectedWorldFile;
		
		if (this.selectedWorldFile == null) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Dünyayı Kaydedeceğin Konumu Seç.");
			fileChooser.setFileFilter(new FileNameExtensionFilter("DikenEngine World File", "dew"));

        	int result = fileChooser.showSaveDialog(engineWindow);
        	if (result != JFileChooser.APPROVE_OPTION) return;

        	selectedFile = fileChooser.getSelectedFile();
        	
        	if (!selectedFile.getName().endsWith(".dew")) {
        		selectedFile = new File(selectedFile.getAbsolutePath() + ".dew");
        	}
		}
        
        try {
			World.saveWorld(editWorld, selectedFile);
			
			this.selectedWorldFile = selectedFile;
		} catch (IOException e) {
			CrashDialog.crash(engineWindow, e, "Dünya Kaydetme Başarısızlıkla Sonuçlandı!");
		}
	}
	
	private JToolBar generateToolbar() {
		var toolbarBuilder = new Toolbar.Builder();
		
		// File Manager
		var basicFileToolbar = toolbarBuilder.newToolbar("BasicFileTools");
		
		toolbarBuilder.addButton(basicFileToolbar, "newWorld", 10, 0, "Yeni Dünya", this::newWorld);
		toolbarBuilder.addButton(basicFileToolbar, "loadWorld", 9, 0, "Dünya Yükle", this::loadWorld);
		toolbarBuilder.addButton(basicFileToolbar, "saveWorld", 8, 0, "Dünyayı Kaydet", this::saveWorld);
		
		var playTestButtons = toolbarBuilder.newToolbar("PlayTestTools");
		
		toolbarBuilder.addButton(playTestButtons, "startPlayTest", 3, 0, "Oyunu Başlat", this::startPlayTest);
		toolbarBuilder.addButton(playTestButtons, "stopPlayTest", 13, 0, "Oyunu Durdur", this::stopPlayTest);
		
		return toolbarBuilder.getJToolBar();
	}
	
	private JMenuBar generateMenuBar(ExplorerPanel explorerPanel, ScriptTabPanel scriptTabPanel) {
		var menubarBuilder = new Menubar.Builder();
		
		var fileMenu = menubarBuilder.newMenu("fileMenu", "Dosya");
		menubarBuilder.addMenuItem(fileMenu, "Yeni Dünya", 10, 0, this::newWorld);
		menubarBuilder.addMenuItem(fileMenu, "Dünya Yükle", 9, 0, this::loadWorld);
		menubarBuilder.addMenuItem(fileMenu, "Dünyayı Kaydet", 8, 0, this::saveWorld);
		menubarBuilder.addMenuSeparator(fileMenu);
		menubarBuilder.addMenuItem(fileMenu, "Dışarıya Çıkar", 11, 0, this::exportProject);
		menubarBuilder.addMenuSeparator(fileMenu);
		menubarBuilder.addMenuItem(fileMenu, "Uygulamayı Kapat", 0, 0, this::stop);
		
		var editMenu = menubarBuilder.newMenu("editMenu", "Düzenle");
		menubarBuilder.addMenuItem(editMenu, "Oyun Ayarları", 1, 3, () -> StudioUtils.openGameSettingsDialog(editWorld, engineWindow));
		menubarBuilder.addMenuItem(editMenu, "Seçenekler", 1, 3, () -> StudioUtils.openSettingsDialog(control, engine, engineWindow));
		
		var windowsMenu = menubarBuilder.newMenu("windowsMenu", "Pencereler");
		for (int i = 0; i < control.getCDockableCount(); i++) {
			var cdockable = control.getCDockable(i);
			
			if (!(cdockable instanceof DefaultCDockable c)) continue;
			
			var id = menubarBuilder.addMenuItemCheckBox(windowsMenu, c.getTitleText(), -1, -1, () -> {
				var bool = !c.isVisible();
				
				c.setVisible(bool);
				menubarBuilder.setButtonChecked(windowsMenu, c.getTitleText() + "_ID", bool);
			});
			
			c.addCDockableStateListener(new CDockableStateListener() {
				@Override
				public void extendedModeChanged(CDockable c, ExtendedMode e) {}

				@Override
				public void visibilityChanged(CDockable arg0) {
					menubarBuilder.setButtonChecked(windowsMenu, id, c.isVisible());
				}
			});
			
			menubarBuilder.setButtonChecked(windowsMenu, id, c.isVisible());
		}
		
		var toolsMenu = menubarBuilder.newMenu("toolsMenu", "Araçlar");
		menubarBuilder.addMenuItem(toolsMenu, "Script Çalıştır", 12, 1, () -> {
			explorerPanel.startPickMode(node -> {
				if (node == null) return;
				
				if (node instanceof Script script) {
					Script executeScript = (Script) script.copy();
					script.getParent().addChild(executeScript);
					executeScript.setName("[Executing] " + script.getName());
					executeScript.initialize(editWorld);
					
					explorerPanel.rebuildExplorer();
				}
			});
		});
		
		var helpMenu = menubarBuilder.newMenu("helpMenu", "Yardım");
		menubarBuilder.addMenuItem(helpMenu, "GitHub Sayfası", -1, -1, () -> 
			Utils.openPage(URI.create("https://github.com/OfficialEmirE/DikenEngine"))
		);
		menubarBuilder.addMenuItem(helpMenu, "Javadoc", -1, -1, () -> 
		Utils.openPage(URI.create("https://github.com/OfficialEmirE/DikenEngine/Javadoc"))
		);
		menubarBuilder.addMenuItem(helpMenu, "Dokümantasyon", -1, -1, () -> 
			Utils.openPage(URI.create("https://github.com/OfficialEmirE/DikenEngine"))
		);
		menubarBuilder.addMenuSeparator(helpMenu);
		menubarBuilder.addMenuItem(helpMenu, "Hakkında", -1, -1, () -> 
			new AboutWindow(engineWindow).setVisible(true)
		);
		
		return menubarBuilder.getJMenuBar();
	}
	
	private void exportProject() {
		//var exportProject = new ExportProject(engineWindow);
	}
}
