package me.ramazanenescik04.diken.studio;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
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

import me.ramazanenescik04.diken.Config;
import me.ramazanenescik04.diken.CrashDialog;
import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.nodes.Decal;
import me.ramazanenescik04.diken.game.nodes.Folder;
import me.ramazanenescik04.diken.game.nodes.Part;
import me.ramazanenescik04.diken.game.nodes.Sky;
import me.ramazanenescik04.diken.game.services.AbstractService;
import me.ramazanenescik04.diken.game.services.Lighting;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.input.IInputListener;
import me.ramazanenescik04.diken.input.InputHandler;
import me.ramazanenescik04.diken.renderer.RendererPanel;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.scripting.Script;
import me.ramazanenescik04.diken.studio.builders.Menubar;
import me.ramazanenescik04.diken.studio.builders.Toolbar;
import me.ramazanenescik04.diken.studio.dockables.*;
import me.ramazanenescik04.diken.tools.Utils;

public final class StudioPanel extends JPanel implements IInputListener {
	private static final long serialVersionUID = 1L;
	private static int SELECTION_COLOR = 0xff33aaff;
	private static int HANDLE_COLOR = 0xffffffff;
	private static int HANDLE_SIZE = 6;
	private static int GRID_COLOR = 0xff2f4752;
	
	private JFrame engineWindow;
	private RendererPanel gamePanel;
	private DikenEngine engine;
	
	private CControl control;
	private File layoutFile = new File(Config.defaultConfigFile.getParentFile(), "layout.dat");
	private File selectedWorldFile;
	
	private World editWorld;
	
	private EditorTabPanel scriptTabPanel;
	private ExplorerPanel explorerPanel;
	private PropertiesPanel propertiesPanel;
	private ConsolePanel consolePanel;
	private BasicObjectsPanel objectsPanel;
	private ResourcesPanel resourcesPanel;
	private AIAssistantPanel assistantPanel;
	
	private EditorTool activeTool = EditorTool.SELECT;
	private int gridSize = 16;
	private boolean inputRegistered;
	private boolean dragging;
	private Point dragStartWorld = new Point();
	private volatile List<Node> selectedEditorNodes = new ArrayList<>();
	private Map<Instance, Point> dragStartLocations = new HashMap<>();
	private Map<Instance, Dimension> dragStartSizes = new HashMap<>();
	private boolean isPlayTestMode;
	private boolean drawGrid;
	
	private enum EditorTool {
		SELECT,
		MOVE,
		SCALE
	}

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
		
		editWorld = new World("");
		
		control = new CControl();
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		this.add(control.getContentArea(), BorderLayout.CENTER);
		setPreferredSize(new Dimension(1280, 706));

		explorerPanel = new ExplorerPanel(editWorld);
		propertiesPanel = new PropertiesPanel(explorerPanel, engineWindow);
		consolePanel = new ConsolePanel();
		objectsPanel = new BasicObjectsPanel(explorerPanel::addNodeToSelected);
		resourcesPanel = new ResourcesPanel(editWorld, engineWindow);
		assistantPanel = new AIAssistantPanel();
		scriptTabPanel = new EditorTabPanel(gamePanel, editWorld);
		explorerPanel.addSelectedNodeListener(new ExplorerPanel.SelectedNodeListener() {
			@Override
			public void onSelectedNode(Node node) {}

			@Override
			public void onSelectedNodes(List<Node> nodes) {
				selectedEditorNodes = new ArrayList<>(nodes);
			}
		});
		explorerPanel.addNodeOpenListener(node -> {
			if (node instanceof Script script) {
				scriptTabPanel.openScript(script);
			}
		});
		
		this.newWorld();
		System.gc(); // Eski Dünyayı yok et
		
		CGrid grid = new CGrid(control);
        grid.add(1, 0, 3, 3, scriptTabPanel.getDockable());
        grid.add(1, 3, 3, 1, consolePanel.getDockable());
        grid.add(4, 0, 1, 2, explorerPanel.getDockable(), assistantPanel.getDockable());
        grid.add(4, 2, 1, 2, propertiesPanel.getDockable());
        grid.add(0, 0, 1, 4, objectsPanel.getDockable());
        grid.add(0, 4, 1, 2, resourcesPanel.getDockable());

        control.getContentArea().deploy(grid);
        
        try {
        	if (!layoutFile.exists()) {
        		layoutFile.getParentFile().mkdir();
        	}
        	
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
		this.selectedEditorNodes = new ArrayList<>();
		
		this.explorerPanel.reloadWorld(world);
		this.resourcesPanel.reloadWorld(world);
		this.scriptTabPanel.reloadWorld(world, false);
		
		System.gc();
	}
	
	public void startPlayTest() {
		var world = this.editWorld.copy();
		this.engine.setWorld(world);
		world.getRunService().run();
		this.selectedEditorNodes = new ArrayList<>();
		
		this.explorerPanel.reloadWorld(world);
		this.resourcesPanel.reloadWorld(world);
		this.scriptTabPanel.reloadWorld(world, true);
		
		this.isPlayTestMode = true;
	}
	
	public void stopPlayTest() {
		this.engine.getWorld().getRunService().stop();
		var world = this.editWorld;
		this.engine.setWorld(world);
		this.selectedEditorNodes = new ArrayList<>();
		
		this.explorerPanel.reloadWorld(world);
		this.resourcesPanel.reloadWorld(world);
		this.scriptTabPanel.reloadWorld(world, false);
		
		this.isPlayTestMode = false;
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
		registerInputListenerIfNeeded();
		
		if (!isPlayTestMode) {
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
	
	private void registerInputListenerIfNeeded() {
		if (!inputRegistered && engine.input != null) {
			engine.input.addListener(this);
			inputRegistered = true;
		}
	}
	
	@Override
	public void keyHandled(int inputMode, int key, char character) {}
	
	@Override
	public void mouseHandled(int inputMode, int x, int y, int clicked) {
		if (editWorld.getRunService().isRunning()) return;
		
		if (inputMode == InputHandler.INPUT_PRESSED && clicked == 0) {
			handleEditorMousePressed(x, y);
		} else if (inputMode == InputHandler.INPUT_REPEATED && clicked == 0) {
			handleEditorMouseDragged(x, y);
		} else if (inputMode == InputHandler.INPUT_RELEASED) {
			handleEditorMouseReleased();
		}
	}
	
	private void handleEditorMousePressed(int screenX, int screenY) {
		Instance handleInstance = findScaleHandleAtScreen(screenX, screenY);
		Instance clickedInstance = handleInstance != null ? handleInstance : findInstanceAtScreen(screenX, screenY);
		
		if (clickedInstance != null && !selectedEditorNodes.contains(clickedInstance)) {
			explorerPanel.selectNode(clickedInstance);
		}
		
		if (clickedInstance == null || activeTool == EditorTool.SELECT) {
			dragging = false;
			return;
		}
		
		if (activeTool == EditorTool.SCALE && handleInstance == null) {
			dragging = false;
			return;
		}
		
		dragging = true;
		dragStartWorld = screenToWorldPoint(screenX, screenY);
		dragStartLocations.clear();
		dragStartSizes.clear();
		
		for (Node selectedNode : explorerPanel.getSelectedNodes()) {
			if (selectedNode instanceof Instance instance) {
				dragStartLocations.put(instance, new Point(instance.getX(), instance.getY()));
				
				if (instance.hasAABB()) {
					dragStartSizes.put(instance, new Dimension(instance.getAABBWidth(), instance.getAABBHeight()));
				}
			}
		}
	}
	
	private void handleEditorMouseDragged(int screenX, int screenY) {
		if (!dragging) return;
		
		Point currentWorld = screenToWorldPoint(screenX, screenY);
		int deltaX = currentWorld.x - dragStartWorld.x;
		int deltaY = currentWorld.y - dragStartWorld.y;
		
		if (activeTool == EditorTool.MOVE) {
			for (var entry : dragStartLocations.entrySet()) {
				Instance instance = entry.getKey();
				Point start = entry.getValue();
				instance.setLocation(snapValue(start.x + deltaX), snapValue(start.y + deltaY));
			}
		} else if (activeTool == EditorTool.SCALE) {
			for (var entry : dragStartSizes.entrySet()) {
				Instance instance = entry.getKey();
				Dimension start = entry.getValue();
				instance.setAABBSize(snapSize(start.width + deltaX), snapSize(start.height + deltaY));
			}
		}
	}
	
	private void handleEditorMouseReleased() {
		if (!dragging) return;
		
		dragging = false;
		dragStartLocations.clear();
		dragStartSizes.clear();
		explorerPanel.refreshSelection();
	}
	
	private Instance findInstanceAtScreen(int screenX, int screenY) {
		Point worldPoint = screenToWorldPoint(screenX, screenY);
		Instance selected = null;
		
		List<Node> allNodes = editWorld.getAllNodes();
		for (Node node : allNodes) {
			if (!(node instanceof Instance instance)) continue;
			
			Hitbox globalBox = instance.getGlobalAABB();
			if (globalBox == null || !globalBox.contains(worldPoint.x, worldPoint.y)) continue;
			
			if (selected == null || instance.getZIndex() >= selected.getZIndex()) {
				selected = instance;
			}
		}
		
		return selected;
	}
	
	private Instance findScaleHandleAtScreen(int screenX, int screenY) {
		if (activeTool != EditorTool.SCALE) return null;
		
		for (Node node : selectedEditorNodes) {
			if (node instanceof Instance instance && isInScaleHandle(instance, screenX, screenY)) {
				return instance;
			}
		}
		
		return null;
	}
	
	private boolean isInScaleHandle(Instance instance, int screenX, int screenY) {
		Hitbox globalBox = instance.getGlobalAABB();
		if (globalBox == null) return false;
		
		var bottomRight = editWorld.worldToScreen(globalBox.getX() + globalBox.getWidth() - 1, globalBox.getY() + globalBox.getHeight() - 1);
		int x = Math.round(bottomRight.x);
		int y = Math.round(bottomRight.y);
		int halfHandle = HANDLE_SIZE / 2;
		
		return screenX >= x - halfHandle && screenX <= x + halfHandle
				&& screenY >= y - halfHandle && screenY <= y + halfHandle;
	}
	
	private Point screenToWorldPoint(int screenX, int screenY) {
		var worldPoint = editWorld.screenToWorld(screenX, screenY);
		return new Point(Math.round(worldPoint.x), Math.round(worldPoint.y));
	}
	
	private int snapValue(int value) {
		if (gridSize <= 0) return value;
		return Math.round((float) value / gridSize) * gridSize;
	}
	
	private int snapSize(int value) {
		if (gridSize <= 0) return Math.max(1, value);
		return Math.max(gridSize, snapValue(value));
	}
	
	public void renderOverlay(Bitmap bitmap) {
		if (isPlayTestMode || explorerPanel == null) return;
		
		if (drawGrid) {
			drawGridOverlay(bitmap);
		}
		
		for (Node node : selectedEditorNodes) {
			if (node instanceof Instance instance) {
				drawSelectionOverlay(bitmap, instance);
			}
		}
	}

	private void drawSelectionOverlay(Bitmap bitmap, Instance instance) {
		Hitbox globalBox = instance.getGlobalAABB();
		if (globalBox == null) return;
		
		var topLeft = editWorld.worldToScreen(globalBox.getX(), globalBox.getY());
		var bottomRight = editWorld.worldToScreen(globalBox.getX() + globalBox.getWidth() - 1, globalBox.getY() + globalBox.getHeight() - 1);
		int x0 = Math.round(topLeft.x);
		int y0 = Math.round(topLeft.y);
		int x1 = Math.round(bottomRight.x);
		int y1 = Math.round(bottomRight.y);
		
		bitmap.box(x0, y0, x1, y1, SELECTION_COLOR);
		
		if (activeTool == EditorTool.SCALE) {
			int halfHandle = HANDLE_SIZE / 2;
			bitmap.fill(x1 - halfHandle, y1 - halfHandle, x1 + halfHandle, y1 + halfHandle, HANDLE_COLOR);
			bitmap.box(x1 - halfHandle, y1 - halfHandle, x1 + halfHandle, y1 + halfHandle, SELECTION_COLOR);
		}
	}
	
	private void drawGridOverlay(Bitmap bitmap) {
		if (gridSize <= 0) return;
		
		var topLeft = editWorld.screenToWorld(0, 0);
		var bottomRight = editWorld.screenToWorld(bitmap.w, bitmap.h);
		int startX = (int) Math.floor(topLeft.x / gridSize) * gridSize;
		int startY = (int) Math.floor(topLeft.y / gridSize) * gridSize;
		
		for (int worldX = startX; worldX <= bottomRight.x; worldX += gridSize) {
			var screenPoint = editWorld.worldToScreen(worldX, topLeft.y);
			int x = Math.round(screenPoint.x);
			bitmap.drawLine(x, 0, x, bitmap.h - 1, GRID_COLOR, 1);
		}
		
		for (int worldY = startY; worldY <= bottomRight.y; worldY += gridSize) {
			var screenPoint = editWorld.worldToScreen(topLeft.x, worldY);
			int y = Math.round(screenPoint.y);
			bitmap.drawLine(0, y, bitmap.w - 1, y, GRID_COLOR, 1);
		}
	}
	
	private void newWorld() {
		var newWorld = new World("Untitled World");
		
		Lighting lighting = newWorld.getService("Lighting");
		Sky sky = new Sky(0xffffffff);
		sky.setTexture("sky");
		
		lighting.addChild(sky);
		lighting.setSky(sky);
		
		this.selectedWorldFile = null;
		
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
		
		var instanceMovementTools = toolbarBuilder.newToolbar("InstanceMovementTools");
		addMovementTools(instanceMovementTools);
		
		toolbarBuilder.addButton(instanceMovementTools, "deletePart", 0, 0, "Seçtiğin herhangi bir objeyi siler", () -> {
			explorerPanel.getSelectedNodes().forEach(e -> {
				if (!(e instanceof AbstractService) && e != null) {
					e.removeNode();
				}
			});
			
			explorerPanel.rebuildExplorer();
		});
		
		var gridTools = toolbarBuilder.newToolbar("GridTools");
		addGridButtons(gridTools);
		
		var basicObjectTools = toolbarBuilder.newToolbar("BasicObjectTools");
		
		toolbarBuilder.addButton(basicObjectTools, "createPart", 0, 1, "Part oluştur",
				() -> addNodeToSelected(new Part()));
		toolbarBuilder.addButton(basicObjectTools, "createDecal", 1, 1, "Decal oluştur",
				() -> addNodeToSelected(new Decal()));
		toolbarBuilder.addButton(basicObjectTools, "createFolder", 6, 1, "Folder oluştur",
				() -> addNodeToSelected(new Folder()));
		
		return toolbarBuilder.getJToolBar();
	}
	
	private void addMovementTools(Toolbar toolbar) {
		ButtonGroup gridGroup = new ButtonGroup();
		
		var selectPart = createMovementButton(4, 0, "Instance'i seç", EditorTool.SELECT);
		var movePart = createMovementButton(5, 0, "Instance'i hareket ettir", EditorTool.MOVE);
		var scalePart = createMovementButton(6, 0, "Instance'in boyutunu genişlet", EditorTool.SCALE);
		
		selectPart.setSelected(true);
		
		gridGroup.add(selectPart);
		gridGroup.add(movePart);
		gridGroup.add(scalePart);
		
		toolbar.addButton("selectPart", selectPart);
		toolbar.addButton("movePart", movePart);
		toolbar.addButton("scalePart", scalePart);
	}
	
	private JToggleButton createMovementButton(int x, int y, String tooltip, EditorTool tool) {
		ImageIcon imageIcon;
		try {
			var image = ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(x, y);
			imageIcon = new ImageIcon(image.toImage());
		} catch (Exception ignore) {
			imageIcon = new ImageIcon(IOResource.missingTexture.toImage());
		} 
		
		JToggleButton button = new JToggleButton();
		button.setToolTipText(tooltip);
		button.setIcon(imageIcon);
		button.addActionListener(_ -> activeTool = tool);
		return button;
	}

	private void addGridButtons(Toolbar toolbar) {
		ButtonGroup gridGroup = new ButtonGroup();
		
		JToggleButton noGridButton = createGridButton("Yok", "Grid Yok", 0);
		JToggleButton grid8Button = createGridButton("8", "Grid 8px", 8);
		JToggleButton grid16Button = createGridButton("16", "Grid 16px", 16);
		
		grid16Button.setSelected(true);
		
		gridGroup.add(noGridButton);
		gridGroup.add(grid8Button);
		gridGroup.add(grid16Button);
		
		toolbar.addButton("gridNone", noGridButton);
		toolbar.addButton("grid8", grid8Button);
		toolbar.addButton("grid16", grid16Button);
	}
	
	private JToggleButton createGridButton(String text, String tooltip, int size) {
		JToggleButton button = new JToggleButton(text);
		button.setToolTipText(tooltip);
		button.addActionListener(_ -> gridSize = size);
		return button;
	}
	
	private JMenuBar generateMenuBar(ExplorerPanel explorerPanel, EditorTabPanel scriptTabPanel) {
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
		menubarBuilder.addMenuItem(editMenu, "Oyun Ayarları", 1, 3,
				() -> StudioUtils.openGameSettingsDialog(editWorld, engineWindow).setVisible(true));
		menubarBuilder.addMenuItem(editMenu, "Seçenekler", 1, 3,
				() -> openSettings());
		
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
		menubarBuilder.addMenuSeparator(toolsMenu);
		menubarBuilder.addMenuItem(toolsMenu, "Garbage Collector", 5, 3, System::gc);
		
		var helpMenu = menubarBuilder.newMenu("helpMenu", "Yardım");
		menubarBuilder.addMenuItem(helpMenu, "Hakkında", -1, -1, () -> 
			new AboutWindow(engineWindow).setVisible(true)
		);
		menubarBuilder.addMenuSeparator(helpMenu);
		menubarBuilder.addMenuItem(helpMenu, "GitHub Sayfası", -1, -1, () -> 
			Utils.openPage(URI.create("https://github.com/OfficialEmirE/DikenEngine"))
		);
		menubarBuilder.addMenuItem(helpMenu, "Dokümantasyon", -1, -1, () -> 
			Utils.openPage(URI.create("https://github.com/OfficialEmirE/DikenEngine"))
		);
		menubarBuilder.addMenuItem(helpMenu, "Obje Tarayıcısı", -1, -1, () -> {
			scriptTabPanel.openObjectReference();
		});
		
		return menubarBuilder.getJMenuBar();
	}
	
	private void openSettings() {
		var settings = StudioUtils.openSettingsDialog(control, engine, engineWindow);
		settings.addSection("Editör Ayarları");
		settings.addSetting(
				new Setting<>("Grid çiz", this.drawGrid, Boolean.class, EnumSettingType.CHECK_BOX)
						.addChangeListener(e -> this.drawGrid = e));
		settings.addSetting(
				new Setting<>("SELECTION_COLOR", SELECTION_COLOR, Integer.class, EnumSettingType.COLOR_PICKER)
						.addChangeListener(e -> SELECTION_COLOR = e));
		settings.addSetting(
				new Setting<>("HANDLE_COLOR", HANDLE_COLOR, Integer.class, EnumSettingType.COLOR_PICKER)
						.addChangeListener(e -> HANDLE_COLOR = e));
		settings.addSetting(
				new Setting<>("HANDLE_SIZE", HANDLE_SIZE, Integer.class, EnumSettingType.COLOR_PICKER)
						.addChangeListener(e -> HANDLE_SIZE = e));
		settings.addSetting(
				new Setting<>("GRID_COLOR", GRID_COLOR, Integer.class, EnumSettingType.COLOR_PICKER)
						.addChangeListener(e -> GRID_COLOR = e));
		settings.setVisible(true);
	}

	private void addNodeToSelected(Node n) {
		var selectedNode = explorerPanel.getSelectedNode();
		
		if (selectedNode != null) {
			selectedNode.addChild(n);
			explorerPanel.rebuildExplorer();
		}
	}
	
	private void exportProject() {
		//var exportProject = new ExportProject(engineWindow);
	}
}
