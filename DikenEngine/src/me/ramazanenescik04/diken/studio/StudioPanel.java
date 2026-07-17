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
import java.util.function.Consumer;

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
import javax.swing.KeyStroke;
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
import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.nodes.Decal;
import me.ramazanenescik04.diken.game.nodes.Folder;
import me.ramazanenescik04.diken.game.nodes.Part;
import me.ramazanenescik04.diken.game.nodes.Sky;
import me.ramazanenescik04.diken.game.services.AbstractService;
import me.ramazanenescik04.diken.game.services.Lighting;
import me.ramazanenescik04.diken.game.services.UIService;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.gui.component.GuiComponent;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.input.IInputListener;
import me.ramazanenescik04.diken.input.InputHandler;
import me.ramazanenescik04.diken.language.Lang;
import me.ramazanenescik04.diken.renderer.RendererPanel;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.scripting.Script;
import me.ramazanenescik04.diken.studio.builders.Menubar;
import me.ramazanenescik04.diken.studio.builders.Toolbar;
import me.ramazanenescik04.diken.studio.dialog.SettingsDialog;
import me.ramazanenescik04.diken.studio.dockables.*;
import me.ramazanenescik04.diken.studio.editors.GamePreview;
import me.ramazanenescik04.diken.studio.editors.ObjectBrowserPanel;
import me.ramazanenescik04.diken.studio.editors.ScriptEditor;
import me.ramazanenescik04.diken.tools.Utils;

public final class StudioPanel extends JPanel implements IInputListener {
	private static final long serialVersionUID = 1L;
	

	private JFrame engineWindow;
	private RendererPanel gamePanel;
	private DikenEngine engine;
	
	CControl control;
	private File layoutFile = new File(Config.defaultConfigFile.getParentFile(), "layout.dat");
	private File selectedWorldFile;
	
	private World editWorld;
	
	private EditorTabPanel scriptTabPanel;
	private ExplorerPanel explorerPanel;
	private PropertiesPanel propertiesPanel;
	private ConsolePanel consolePanel;
	private BasicObjectsPanel objectsPanel;
	private ResourcesPanel resourcesPanel;
	private AssistantPanel assistantPanel;
	private CodeEditorPanel codeEditorPanel;
	
	public int selectionColor = 0xff33aaff;
	public int handleColor = 0xffffff00;
	public int scaleHandleColor = 0xffffffff;
	public int scaleHandleBorderColor = 0xff000000;
	public int handleSize = 16;
	public int gridColor = 0xff2f4752;
	
	private Consumer<World> newWorldListener;
	
	private EditorTool activeTool = EditorTool.SELECT;
	private int gridSize = 16;
	
	private boolean inputRegistered;
	private boolean dragging;
	private boolean middleDragging;
	
	// Drag state – screen-space start point, world-space start positions/sizes
	private Point dragStartScreen = new Point();
	private Point mouseLastScreen = new Point();
	
	private volatile List<Node> selectedEditorNodes = new ArrayList<>();
	private Map<Instance, Point> dragStartPositions = new HashMap<>();
	private Map<Instance, Dimension> dragStartSizes = new HashMap<>();
	
	// UI elemanı sürükleme
	private boolean uiDragging;
	private Map<Node, UDim2> uiDragStartPositions = new HashMap<>();
	private Map<Node, Dimension> uiDragStartSizes = new HashMap<>();
	private Point uiDragStartScreen = new Point();
	
	private boolean isPlayTestMode;
	boolean drawGrid;
	
	private enum EditorTool {
		SELECT,
		MOVE,
		SCALE
	}
	
	private enum HandleType {
		NONE,
		CENTER,
		TOP, BOTTOM, LEFT, RIGHT,
		TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
	}
	
	private HandleType activeHandle = HandleType.NONE;
	private Instance handleTargetInstance;

	public StudioPanel(JFrame window, RendererPanel gamePanel, DikenEngine engine) {
		super(new BorderLayout());
		
		Objects.requireNonNull(window);
		Objects.requireNonNull(gamePanel);
		
		this.engineWindow = window;
		this.gamePanel = gamePanel;
		this.engine = Objects.requireNonNullElse(engine, DikenEngine.getEngine());
		
		init();
	}
	
	public void setNewWorldListener(Consumer<World> c) {
		this.newWorldListener = c;
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
		explorerPanel.postRebuildCallback = () -> codeEditorPanel.reloadScripts(editWorld);
		explorerPanel.scriptRenameCallback = () -> codeEditorPanel.refreshScriptTitles();
		propertiesPanel = new PropertiesPanel(explorerPanel, engineWindow);
		consolePanel = new ConsolePanel();
		objectsPanel = new BasicObjectsPanel(explorerPanel::addNodeToSelected);
		assistantPanel = new AssistantPanel();
		codeEditorPanel = new CodeEditorPanel();
		scriptTabPanel = new EditorTabPanel();
		scriptTabPanel.openEditor(new GamePreview(gamePanel));
		resourcesPanel = new ResourcesPanel(editWorld, scriptTabPanel, engineWindow);
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
				codeEditorPanel.openScript(script);
			}
		});
		
		System.gc();
		
		CGrid grid = new CGrid(control);
        grid.add(1, 0, 2, 3, scriptTabPanel.getDockable());
        grid.add(3, 0, 1, 3, codeEditorPanel.getDockable());
        grid.add(1, 3, 2, 1, consolePanel.getDockable());
        grid.add(3, 3, 1, 1, assistantPanel.getDockable());
        grid.add(4, 0, 1, 2, explorerPanel.getDockable());
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
		
		StudioUtils.init(this, engine, engineWindow);
		SettingsManager.load();
	}
	
	public void loadWorld(World world) {
		this.editWorld = world;
		this.engine.setWorld(world);
		this.selectedEditorNodes = new ArrayList<>();
		
		this.explorerPanel.reloadWorld(world);
		this.resourcesPanel.reloadWorld(world);
		this.scriptTabPanel.reloadWorld(world, false);
		codeEditorPanel.reloadScripts(world);
		StudioUtils.reloadGameSettings(world);
		
		System.gc();
	}
	
	public void startPlayTest() {
		var world = this.editWorld.copy();
		this.engine.setWorld(world);
		world.getRunService().run();
		world.getCamera().setZoom(1.0f);
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
		String worldName = this.editWorld.getRoot().getName();
		if (selectedWorldFile == null) {
			worldName = "*" + worldName;
		}
		
		int saveWorld = JOptionPane.showConfirmDialog(engineWindow, Lang.get("studio.exitWarning", worldName));
		
		if (saveWorld == JOptionPane.YES_OPTION) {
			saveWorld(false);
		} else if (saveWorld == JOptionPane.CANCEL_OPTION) {
			return;
		}
		
		try {
			control.write(layoutFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		engine.stop();
		SettingsManager.save();
	}
	
	public void tick() {
		registerInputListenerIfNeeded();
		
		boolean ctrl = engine.input.isKeyDown(KeyEvent.VK_CONTROL);
		
		if (!isPlayTestMode && !ctrl && engine.input.isMouseOnScreen()) {
			if (engine.input.isKeyDown(StudioUtils.keyMapList.get("goForward"))) {
				editWorld.camera.addY(-8);
			}
			
			if (engine.input.isKeyDown(StudioUtils.keyMapList.get("goLeft"))) {
				editWorld.camera.addX(-8);
			}
			
			if (engine.input.isKeyDown(StudioUtils.keyMapList.get("goBack"))) {
				editWorld.camera.addY(+8);
			}
			
			if (engine.input.isKeyDown(StudioUtils.keyMapList.get("goRight"))) {
				editWorld.camera.addX(8);
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
	public void keyHandled(int inputMode, int key, char character) {
		if (inputMode == InputHandler.INPUT_PRESSED) {
			if (key == StudioUtils.keyMapList.get("goInstance")) {
				int x = 0, y = 0, i = 0;
				for (Node node : selectedEditorNodes) {
					if (!(node instanceof Instance instance)) continue;
					
					x += instance.getGlobalX();
					y += instance.getGlobalY();
					i++;
				}
				
				if (i == 0) return;
				x = Math.max(x, 1);
				y = Math.max(y, 1);
				
				this.editWorld.camera.setX((x / i) - (this.engine.getScaledWidth() / 2));
				this.editWorld.camera.setY((y / i) - (this.engine.getScaledHeight() / 2));
			}
			
			explorerPanel.keyPressed(engine.input.isKeyDown(KeyEvent.VK_CONTROL), key, character);
		}
	}
	
	@Override
	public void mouseHandled(int inputMode, int x, int y, int clicked) {
		if (editWorld.getRunService().isRunning()) return;
		
		if (inputMode == InputHandler.INPUT_RELEASED) {
			handleEditorMouseReleased();
			return;
		}
		
		if (inputMode == InputHandler.INPUT_WHEEL) {
			handleMouseWheelZoom(x, y, clicked);
			return;
		}
		
		if (clicked == 1) {
			if (inputMode == InputHandler.INPUT_PRESSED) {
				middleDragging = true;
				mouseLastScreen.setLocation(x, y);
			} else if (inputMode == InputHandler.INPUT_REPEATED && middleDragging) {
				int dx = x - mouseLastScreen.x;
				int dy = y - mouseLastScreen.y;
				float zoom = editWorld.getCamera().getZoom();
				editWorld.camera.addX(Math.round(-dx / zoom));
				editWorld.camera.addY(Math.round(-dy / zoom));
				mouseLastScreen.setLocation(x, y);
			}
			return;
		}
		
		if (inputMode == InputHandler.INPUT_PRESSED && clicked == 0) {
			handleEditorMousePressed(x, y);
		} else if (inputMode == InputHandler.INPUT_REPEATED && clicked == 0) {
			handleEditorMouseDragged(x, y);
		}
	}
	
	private void handleMouseWheelZoom(int x, int y, int clicked) {
		if (clicked == 0) return;
		
		float zoom = editWorld.getCamera().getZoom();
		var focusPoint = screenToWorldPoint(x, y);
		float targetZoom = Math.max(0.25f, Math.min(4.0f, zoom + (-clicked * 0.5f)));
		if (Math.abs(targetZoom - zoom) < 0.0001f) {
			return;
		}
		
		zoom = targetZoom;
		editWorld.getCamera().setZoom(zoom);
		
		editWorld.camera.setX(Math.round(focusPoint.x - (x / zoom)));
		editWorld.camera.setY(Math.round(focusPoint.y - (y / zoom)));
	}

	// =========================================================================
	//  MOUSE PRESS – find what was clicked, set up drag
	// =========================================================================
	private void handleEditorMousePressed(int screenX, int screenY) {
		activeHandle = HandleType.NONE;
		handleTargetInstance = null;
		dragging = false;
		uiDragging = false;

		// ----- First: try to click a gizmo handle of an already-selected node -----
		Node anySelected = selectedEditorNodes.isEmpty() ? null : selectedEditorNodes.get(selectedEditorNodes.size() - 1);
		if (anySelected != null && activeTool != EditorTool.SELECT) {
			Hitbox selBounds = getNodeScreenBounds(anySelected);
			if (selBounds != null) {
				HandleType h = detectHandleAtBounds(selBounds, screenX, screenY);
				if (h != HandleType.NONE) {
					activeHandle = h;
					startGuiGizmoDrag(screenX, screenY);
					if (anySelected instanceof Instance inst) {
						handleTargetInstance = inst;
					}
					return;
				}
			}
		}

		// ----- Otherwise: hit-test UI components (text, etc.) -----
		Node uiNode = findUIComponentAtScreen(screenX, screenY);
		if (uiNode != null) {
			selectNode(uiNode);
			// If a tool is active, check gizmo on the newly selected node immediately
			if (activeTool != EditorTool.SELECT) {
				Hitbox selBounds = getNodeScreenBounds(uiNode);
				if (selBounds != null) {
					HandleType h = detectHandleAtBounds(selBounds, screenX, screenY);
					if (h != HandleType.NONE) {
						activeHandle = h;
						startGuiGizmoDrag(screenX, screenY);
						return;
					}
				}
			}
			// Fallback: free move (uiDragging)
			uiDragging = true;
			uiDragStartScreen.setLocation(screenX, screenY);
			uiDragStartPositions.clear();
			for (Node node : selectedEditorNodes) {
				if (node instanceof GuiComponent comp) {
					uiDragStartPositions.put(node, comp.getPosition());
				}
			}
			return;
		}

		// ----- Hit-test world instances -----
		int handleMargin = 24;
		Instance hitInstance = findInstanceIncludingHandlesAtScreen(screenX, screenY, handleMargin);

		if (hitInstance != null) {
			selectNode(hitInstance);
			handleTargetInstance = hitInstance;

			if (activeTool == EditorTool.SELECT) {
				activeHandle = HandleType.CENTER;
			} else {
				activeHandle = detectHandle(hitInstance, screenX, screenY);
				if (activeHandle == HandleType.NONE) {
					activeHandle = HandleType.CENTER;
				}
			}

			startDragging(screenX, screenY);
		}
	}

	/**
	 * Returns the screen-space Hitbox of a selected Node (Instance or GuiComponent).
	 */
	private Hitbox getNodeScreenBounds(Node node) {
		if (node instanceof Instance instance && instance.hasAABB()) {
			Hitbox g = instance.getGlobalAABB();
			if (g == null) return null;
			var tl = editWorld.worldToScreen(g.getX(), g.getY());
			var br = editWorld.worldToScreen(g.getX() + g.getWidth(), g.getY() + g.getHeight());
			return new Hitbox(Math.round(tl.x), Math.round(tl.y),
					Math.round(br.x) - Math.round(tl.x),
					Math.round(br.y) - Math.round(tl.y));
		}
		if (node instanceof GuiComponent comp) {
			return comp.getAbsoluteBounds();
		}
		return null;
	}

	/**
	 * Gizmo hit-test against a screen-space Hitbox (used for both Instance and GuiComponent).
	 */
	private HandleType detectHandleAtBounds(Hitbox bounds, int screenX, int screenY) {
		int x0 = bounds.getX();
		int y0 = bounds.getY();
		int x1 = x0 + bounds.getWidth();
		int y1 = y0 + bounds.getHeight();
		int cx = (x0 + x1) / 2;
		int cy = (y0 + y1) / 2;
		int tr = 12;
		int gap = 16;

		if (activeTool == EditorTool.MOVE) {
			int topTip    = y0 - gap - 10;
			int bottomTip = y1 + gap + 10;
			int leftTip   = x0 - gap - 10;
			int rightTip  = x1 + gap + 10;

			if (hitVLine(screenX, screenY, cx, y0 - gap, topTip,    tr, tr)) return HandleType.TOP;
			if (hitVLine(screenX, screenY, cx, y1 + gap, bottomTip, tr, tr)) return HandleType.BOTTOM;
			if (hitHLine(screenX, screenY, leftTip,  x0 - gap, cy, tr, tr))   return HandleType.LEFT;
			if (hitHLine(screenX, screenY, x1 + gap, rightTip, cy, tr, tr))   return HandleType.RIGHT;
			if (isPointInHandle(screenX, screenY, cx, cy, 8)) return HandleType.CENTER;
			return HandleType.NONE;
		}

		if (activeTool == EditorTool.SCALE) {
			if (isPointInHandle(screenX, screenY, x0, y0, tr)) return HandleType.TOP_LEFT;
			if (isPointInHandle(screenX, screenY, x1, y0, tr)) return HandleType.TOP_RIGHT;
			if (isPointInHandle(screenX, screenY, x1, y1, tr)) return HandleType.BOTTOM_RIGHT;
			if (isPointInHandle(screenX, screenY, x0, y1, tr)) return HandleType.BOTTOM_LEFT;
			if (isPointInHandle(screenX, screenY, cx, y0, tr)) return HandleType.TOP;
			if (isPointInHandle(screenX, screenY, x1, cy, tr)) return HandleType.RIGHT;
			if (isPointInHandle(screenX, screenY, cx, y1, tr)) return HandleType.BOTTOM;
			if (isPointInHandle(screenX, screenY, x0, cy, tr)) return HandleType.LEFT;
			if (isPointInHandle(screenX, screenY, cx, cy, 8))  return HandleType.CENTER;
		}

		return HandleType.NONE;
	}

	/**
	 * Start a gizmo-based drag for GuiComponents.
	 */
	private void startGuiGizmoDrag(int screenX, int screenY) {
		dragging = true;
		dragStartScreen.setLocation(screenX, screenY);
		uiDragStartPositions.clear();
		uiDragStartSizes.clear();
		dragStartPositions.clear();
		dragStartSizes.clear();

		for (Node node : selectedEditorNodes) {
			if (node instanceof GuiComponent comp) {
				// Store position AND size for both move and scale
				uiDragStartPositions.put(node, comp.getPosition());
				uiDragStartSizes.put(node, new Dimension(comp.getWidth(), comp.getHeight()));
			}
			if (node instanceof Instance instance) {
				dragStartPositions.put(instance, new Point(instance.getX(), instance.getY()));
				if (instance.hasAABB()) {
					dragStartSizes.put(instance, new Dimension(instance.getAABBWidth(), instance.getAABBHeight()));
				}
			}
		}
	}

	// =========================================================================
	//  DRAG START – snap screen point, record world positions + sizes
	// =========================================================================
	private void startDragging(int screenX, int screenY) {
		dragging = true;
		dragStartScreen.setLocation(screenX, screenY);
		dragStartPositions.clear();
		dragStartSizes.clear();

		for (Node selectedNode : selectedEditorNodes) {
			if (selectedNode instanceof Instance instance) {
				dragStartPositions.put(instance, new Point(instance.getX(), instance.getY()));
				if (instance.hasAABB()) {
					dragStartSizes.put(instance, new Dimension(instance.getAABBWidth(), instance.getAABBHeight()));
				}
			}
		}
	}

	// Scale sensitivity — makes ratio-based scale feel natural at any size
	private static final float SCALE_SENSITIVITY = 0.6f;

	// =========================================================================
	//  DRAG – screen delta → apply per tool/handle (GuiComponent + Instance)
	// =========================================================================
	private void handleEditorMouseDragged(int screenX, int screenY) {
		if (!dragging && !uiDragging) return;

		// ---------- 1. UI dragging (SELECT tool only) ----------
		if (uiDragging) {
			int uiDX = screenX - uiDragStartScreen.x;
			int uiDY = screenY - uiDragStartScreen.y;
			for (var entry : uiDragStartPositions.entrySet()) {
				Node node = entry.getKey();
				if (!(node instanceof GuiComponent comp)) continue;
				if (comp.isRemoved()) continue;
				UDim2 startPos = entry.getValue();
				comp.setPosition(new UDim2(startPos.x.scale, startPos.x.offset + uiDX,
						startPos.y.scale, startPos.y.offset + uiDY));
			}
			return;
		}

		// ---------- 2. Gizmo operation (not uiDragging) ----------
		if (!dragging) return;

		int screenDX = screenX - dragStartScreen.x;
		int screenDY = screenY - dragStartScreen.y;
		float zoom = Math.max(0.25f, editWorld.getCamera().getZoom());
		int worldDX = Math.round(screenDX / zoom);
		int worldDY = Math.round(screenDY / zoom);

		// --- SELECT / CENTER → free move ---
		if (activeTool == EditorTool.SELECT || activeHandle == HandleType.CENTER) {
			// Move GuiComponents (screen space)
			for (var entry : uiDragStartPositions.entrySet()) {
				Node node = entry.getKey();
				if (!(node instanceof GuiComponent comp)) continue;
				if (comp.isRemoved()) continue;
				UDim2 startPos = entry.getValue();
				comp.setPosition(new UDim2(startPos.x.scale, startPos.x.offset + screenDX,
						startPos.y.scale, startPos.y.offset + screenDY));
			}
			// Move Instances (world space)
			for (var entry : dragStartPositions.entrySet()) {
				Instance instance = entry.getKey();
				Point start = entry.getValue();
				instance.setLocation(start.x + worldDX, start.y + worldDY);
			}
			return;
		}

		// --- MOVE tool → axis-constrained move ---
		if (activeTool == EditorTool.MOVE) {
			int sx = 0, sy = 0, wx = 0, wy = 0;
			switch (activeHandle) {
				case TOP: case BOTTOM: sy = screenDY; wy = worldDY; break;
				case LEFT: case RIGHT: sx = screenDX; wx = worldDX; break;
				default: break;
			}
			for (var entry : uiDragStartPositions.entrySet()) {
				Node node = entry.getKey();
				if (!(node instanceof GuiComponent comp)) continue;
				if (comp.isRemoved()) continue;
				UDim2 startPos = entry.getValue();
				comp.setPosition(new UDim2(startPos.x.scale, startPos.x.offset + sx,
						startPos.y.scale, startPos.y.offset + sy));
			}
			for (var entry : dragStartPositions.entrySet()) {
				Instance instance = entry.getKey();
				Point start = entry.getValue();
				instance.setLocation(start.x + wx, start.y + wy);
			}
			return;
		}

		// --- SCALE tool → ratio-based scale + anchor ---
		if (activeTool == EditorTool.SCALE && activeHandle != HandleType.NONE) {
			float edgeLenX = 0, edgeLenY = 0;
			HandleType h = activeHandle;
			edgeLenX = (h == HandleType.LEFT || h == HandleType.RIGHT) ? 0 : 1;
			edgeLenY = (h == HandleType.TOP || h == HandleType.BOTTOM) ? 0 : 1;
			switch (h) {
				case TOP_LEFT: case BOTTOM_LEFT: case LEFT: edgeLenX = -1; break;
				case TOP_RIGHT: case BOTTOM_RIGHT: case RIGHT: edgeLenX = 1; break;
				default: break;
			}
			switch (h) {
				case TOP_LEFT: case TOP_RIGHT: case TOP: edgeLenY = -1; break;
				case BOTTOM_LEFT: case BOTTOM_RIGHT: case BOTTOM: edgeLenY = 1; break;
				default: break;
			}
			boolean scaleX = h == HandleType.TOP_LEFT || h == HandleType.TOP_RIGHT
					|| h == HandleType.BOTTOM_LEFT || h == HandleType.BOTTOM_RIGHT
					|| h == HandleType.LEFT || h == HandleType.RIGHT;
			boolean scaleY = h == HandleType.TOP_LEFT || h == HandleType.TOP_RIGHT
					|| h == HandleType.BOTTOM_LEFT || h == HandleType.BOTTOM_RIGHT
					|| h == HandleType.TOP || h == HandleType.BOTTOM;

			// GuiComponents: ratio-based scale using stored start sizes
			for (var entry : uiDragStartPositions.entrySet()) {
				Node node = entry.getKey();
				if (!(node instanceof GuiComponent comp)) continue;
				if (comp.isRemoved()) continue;
				Dimension startSize = uiDragStartSizes.get(node);
				if (startSize == null) continue;
				UDim2 startPos = entry.getValue();

				int sX = startPos.x.offset;
				int sY = startPos.y.offset;
				int sW = startSize.width;
				int sH = startSize.height;
				int minSize = 1;

				float ratioX = (sW > 0) ? (float)screenDX / sW * SCALE_SENSITIVITY : 0;
				float ratioY = (sH > 0) ? (float)screenDY / sH * SCALE_SENSITIVITY : 0;

				int newW = sW, newH = sH, newX = sX, newY = sY;

				if (scaleX) {
					int deltaW = Math.round(edgeLenX * ratioX * sW);
					newW = Math.max(minSize, sW + deltaW);
					if (edgeLenX < 0) newX = sX + sW - newW;
				}
				if (scaleY) {
					int deltaH = Math.round(edgeLenY * ratioY * sH);
					newH = Math.max(minSize, sH + deltaH);
					if (edgeLenY < 0) newY = sY + sH - newH;
				}

				comp.setPosition(new UDim2(startPos.x.scale, newX, startPos.y.scale, newY));
				comp.setSize(new UDim2(0, newW, 0, newH));
			}

			// Instances — ratio-based scale per-instance
			for (var entry : dragStartSizes.entrySet()) {
				Instance instance = entry.getKey();
				Dimension startSize = entry.getValue();
				Point startPos = dragStartPositions.get(instance);
				if (startPos == null) startPos = new Point(instance.getX(), instance.getY());

				int startW = startSize.width;
				int startH = startSize.height;
				int startX = startPos.x;
				int startY = startPos.y;
				int minSize = 1;

				float ratioX = (startW > 0) ? (float)worldDX / startW * SCALE_SENSITIVITY : 0;
				float ratioY = (startH > 0) ? (float)worldDY / startH * SCALE_SENSITIVITY : 0;

				int newW = startW, newH = startH, newX = startX, newY = startY;

				if (scaleX) {
					int deltaW = Math.round(edgeLenX * ratioX * startW);
					newW = Math.max(minSize, startW + deltaW);
					if (edgeLenX < 0) newX = startX + startW - newW;
				}
				if (scaleY) {
					int deltaH = Math.round(edgeLenY * ratioY * startH);
					newH = Math.max(minSize, startH + deltaH);
					if (edgeLenY < 0) newY = startY + startH - newH;
				}

				instance.setLocation(newX, newY);
				instance.setAABBSize(newW, newH);
			}
		}
	}
	
	/**
	 * Axis-constrained scale like Godot 2D viewport.
	 * Drag a corner → scale X and Y, keep opposite corner anchored.
	 * Drag an edge midpoint → scale only that axis, keep opposite edge anchored.
	 */
	private void applyAxisScale(Instance instance, Point startPos, Dimension startSize, int worldDX, int worldDY) {
		int startX = startPos.x;
		int startY = startPos.y;
		int startW = startSize.width;
		int startH = startSize.height;
		
		int newW = startW, newH = startH;
		int newX = startX, newY = startY;
		int minSize = 1;
		
		switch (activeHandle) {
			// --- Corners: scale both axes, anchor opposite corner ---
			case TOP_LEFT:
				newW = Math.max(minSize, startW - worldDX);
				newH = Math.max(minSize, startH - worldDY);
				newX = startX + startW - newW;
				newY = startY + startH - newH;
				break;
			case TOP_RIGHT:
				newW = Math.max(minSize, startW + worldDX);
				newH = Math.max(minSize, startH - worldDY);
				newX = startX;
				newY = startY + startH - newH;
				break;
			case BOTTOM_LEFT:
				newW = Math.max(minSize, startW - worldDX);
				newH = Math.max(minSize, startH + worldDY);
				newX = startX + startW - newW;
				newY = startY;
				break;
			case BOTTOM_RIGHT:
				newW = Math.max(minSize, startW + worldDX);
				newH = Math.max(minSize, startH + worldDY);
				newX = startX;
				newY = startY;
				break;
			
			// --- Midpoints: scale one axis, anchor opposite edge ---
			case TOP:
				newH = Math.max(minSize, startH - worldDY);
				newY = startY + startH - newH;
				break;
			case BOTTOM:
				newH = Math.max(minSize, startH + worldDY);
				break;
			case LEFT:
				newW = Math.max(minSize, startW - worldDX);
				newX = startX + startW - newW;
				break;
			case RIGHT:
				newW = Math.max(minSize, startW + worldDX);
				break;
			
			// CENTER/NONE handled elsewhere
			default:
				break;
		}
		
		instance.setLocation(newX, newY);
		instance.setAABBSize(newW, newH);
	}
	
	// =========================================================================
	//  MOUSE RELEASE
	// =========================================================================
	private void handleEditorMouseReleased() {
		boolean wasDragging = dragging || uiDragging;
		middleDragging = false;
		
		dragging = false;
		uiDragging = false;
		uiDragStartPositions.clear();
		dragStartPositions.clear();
		dragStartSizes.clear();
		
		if (wasDragging) {
			updateProperties();
			if (dragging || uiDragging) {
				// only refresh if it was instance drag
				try { explorerPanel.refreshSelection(); } catch (Exception ignore) {}
			}
		}
		
		// Reset drag completely
		dragging = false;
		uiDragging = false;
	}
	
	// =========================================================================
	//  HIT-TEST HELPER – find instance under screen point (with handle margin)
	// =========================================================================
	private Instance findInstanceIncludingHandlesAtScreen(int screenX, int screenY, int marginPx) {
		List<Node> allNodes = editWorld.getAllNodes();
		Instance bestMatch = null;
		int bestZ = Integer.MIN_VALUE;
		
		for (Node node : allNodes) {
			if (!(node instanceof Instance instance)) continue;
			if (!instance.hasAABB()) continue;
			
			Hitbox globalBox = instance.getGlobalAABB();
			if (globalBox == null) continue;
			
			var tl = editWorld.worldToScreen(globalBox.getX(), globalBox.getY());
			var br = editWorld.worldToScreen(globalBox.getX() + globalBox.getWidth(), globalBox.getY() + globalBox.getHeight());
			int sx0 = Math.round(tl.x) - marginPx;
			int sy0 = Math.round(tl.y) - marginPx;
			int sx1 = Math.round(br.x) + marginPx;
			int sy1 = Math.round(br.y) + marginPx;
			
			if (screenX >= sx0 && screenX <= sx1 && screenY >= sy0 && screenY <= sy1) {
				if (instance.getZIndex() >= bestZ) {
					bestZ = instance.getZIndex();
					bestMatch = instance;
				}
			}
		}
		
		return bestMatch;
	}
	
	private void selectNode(Node node) {
		if (!selectedEditorNodes.contains(node)) {
			selectedEditorNodes = new ArrayList<>();
			selectedEditorNodes.add(node);
			explorerPanel.selectNode(node);
		}
	}
	
	// =========================================================================
	//  HANDLE DETECTION – match screen point to gizmo handle
	// =========================================================================
	private HandleType detectHandle(Instance instance, int screenX, int screenY) {
		Hitbox globalBox = instance.getGlobalAABB();
		if (globalBox == null) return HandleType.NONE;

		var topLeft = editWorld.worldToScreen(globalBox.getX(), globalBox.getY());
		var bottomRight = editWorld.worldToScreen(globalBox.getX() + globalBox.getWidth(), globalBox.getY() + globalBox.getHeight());
		int x0 = Math.round(topLeft.x);
		int y0 = Math.round(topLeft.y);
		int x1 = Math.round(bottomRight.x);
		int y1 = Math.round(bottomRight.y);
		int cx = (x0 + x1) / 2;
		int cy = (y0 + y1) / 2;

		// Touch radius for handles — needs to be generous for small instances
		int tr = 12;
		// Gap from box edge to arrow (must match drawSelectionOverlay)
		int gap = 16;

		if (activeTool == EditorTool.MOVE) {
			// Hit-test arrows extending outward from the box EDGES
			// (matching drawSelectionOverlay positions)

			// Top arrow: shaft from (cx, y0-gap) to (cx, topTip = y0-gap-10)
			int topTip = y0 - gap - 10;
			if (hitVLine(screenX, screenY, cx, y0 - gap, topTip, tr, tr)) return HandleType.TOP;

			// Bottom arrow
			int bottomTip = y1 + gap + 10;
			if (hitVLine(screenX, screenY, cx, y1 + gap, bottomTip, tr, tr)) return HandleType.BOTTOM;

			// Left arrow
			int leftTip = x0 - gap - 10;
			if (hitHLine(screenX, screenY, leftTip, x0 - gap, cy, tr, tr)) return HandleType.LEFT;

			// Right arrow
			int rightTip = x1 + gap + 10;
			if (hitHLine(screenX, screenY, x1 + gap, rightTip, cy, tr, tr)) return HandleType.RIGHT;

			// Center square (6x6 drawn, detect with radius)
			if (isPointInHandle(screenX, screenY, cx, cy, 8)) return HandleType.CENTER;

			return HandleType.NONE;
		}

		if (activeTool == EditorTool.SCALE) {
			// 8 squares on AABB boundary + center
			if (isPointInHandle(screenX, screenY, x0, y0, tr)) return HandleType.TOP_LEFT;
			if (isPointInHandle(screenX, screenY, x1, y0, tr)) return HandleType.TOP_RIGHT;
			if (isPointInHandle(screenX, screenY, x1, y1, tr)) return HandleType.BOTTOM_RIGHT;
			if (isPointInHandle(screenX, screenY, x0, y1, tr)) return HandleType.BOTTOM_LEFT;
			if (isPointInHandle(screenX, screenY, cx, y0, tr)) return HandleType.TOP;
			if (isPointInHandle(screenX, screenY, x1, cy, tr)) return HandleType.RIGHT;
			if (isPointInHandle(screenX, screenY, cx, y1, tr)) return HandleType.BOTTOM;
			if (isPointInHandle(screenX, screenY, x0, cy, tr)) return HandleType.LEFT;
			// Center
			if (isPointInHandle(screenX, screenY, cx, cy, 8)) return HandleType.CENTER;
		}

		return HandleType.NONE;
	}

	/** Hit-test a vertical line segment from (x, yA) to (x, yB) with horizontal tolerance. */
	private boolean hitVLine(int px, int py, int x, int yA, int yB, int horizR, int vertR) {
		int yTop = Math.min(yA, yB) - vertR;
		int yBot = Math.max(yA, yB) + vertR;
		return px >= x - horizR && px <= x + horizR && py >= yTop && py <= yBot;
	}

	/** Hit-test a horizontal line segment from (xA, y) to (xB, y) with vertical tolerance. */
	private boolean hitHLine(int px, int py, int xA, int xB, int y, int horizR, int vertR) {
		int xLeft  = Math.min(xA, xB) - horizR;
		int xRight = Math.max(xA, xB) + horizR;
		return py >= y - vertR && py <= y + vertR && px >= xLeft && px <= xRight;
	}

	private boolean isPointInHandle(int px, int py, int hx, int hy, int halfHandle) {
		return px >= hx - halfHandle && px <= hx + halfHandle
				&& py >= hy - halfHandle && py <= hy + halfHandle;
	}
	
	// =========================================================================
	//  COORDINATE HELPERS
	// =========================================================================
	private Point screenToWorldPoint(int screenX, int screenY) {
		var worldPoint = editWorld.screenToWorld(screenX, screenY);
		return new Point(Math.round(worldPoint.x), Math.round(worldPoint.y));
	}
	
	private void updateProperties() {
		Node selected = explorerPanel.getSelectedNode();
		if (selected != null) {
			final Node sel = selected;
			javax.swing.SwingUtilities.invokeLater(() -> propertiesPanel.inspect(sel));
		}
	}
	
	private Node findUIComponentAtScreen(int screenX, int screenY) {
		var uiService = editWorld.getService(UIService.class);
		if (uiService == null) return null;
		
		Node bestMatch = null;
		int bestZ = Integer.MIN_VALUE;
		
		for (Node child : uiService.getDescendants()) {
			if (!(child instanceof GuiComponent comp)) continue;
			if (!comp.isVisible()) continue;
			
			var bounds = comp.getAbsoluteBounds();
			if (bounds.contains(screenX, screenY)) {
				if (comp.getZIndex() >= bestZ) {
					bestZ = comp.getZIndex();
					bestMatch = comp;
				}
			}
		}
		
		return bestMatch;
	}
	
	// =========================================================================
	//  RENDER – gizmo overlay
	// =========================================================================
	public void renderOverlay(Bitmap bitmap) {
		if (isPlayTestMode || explorerPanel == null) return;
		
		if (drawGrid) {
			drawGridOverlay(bitmap);
		}
		
		for (Node node : selectedEditorNodes) {
			if (node instanceof Instance instance && instance.hasAABB()) {
				drawSelectionOverlay(bitmap, instance);
			} else if (node instanceof GuiComponent comp) {
				drawGuiSelectionOverlay(bitmap, comp);
			}
		}
	}

	private void drawGuiSelectionOverlay(Bitmap bitmap, GuiComponent comp) {
		Hitbox bounds = comp.getAbsoluteBounds();
		if (bounds == null) return;
		
		int x0 = bounds.getX(), y0 = bounds.getY();
		int x1 = x0 + bounds.getWidth() - 1, y1 = y0 + bounds.getHeight() - 1;
		
		bitmap.box(x0, y0, x1, y1, selectionColor);

		// Gizmo handles for UI components (same as Instance)
		int cx = (x0 + x1) / 2;
		int cy = (y0 + y1) / 2;
		int hs = handleSize / 2;   // half-size for square handles = 8
		int gap = 16;

		switch (activeTool) {
			case SELECT:
				break;

			case MOVE: {
				int red   = 0xffff3333;
				int green = 0xff33ff33;

				int leftTipX  = x0 - gap - 10;
				int rightTipX = x1 + gap + 10;
				drawArrow(bitmap, x0 - gap, cy, leftTipX,  cy, red);
				drawArrow(bitmap, x1 + gap, cy, rightTipX, cy, red);

				int topTipY    = y0 - gap - 10;
				int bottomTipY = y1 + gap + 10;
				drawArrow(bitmap, cx, y0 - gap, cx, topTipY,    green);
				drawArrow(bitmap, cx, y1 + gap, cx, bottomTipY, green);

				bitmap.fill(cx - 4, cy - 4, cx + 4, cy + 4, handleColor);
				bitmap.box (cx - 4, cy - 4, cx + 4, cy + 4, 0xff000000);
				break;
			}

			case SCALE: {
				int[][] pts = {{x0, y0}, {x1, y0}, {x1, y1}, {x0, y1},
				               {cx, y0}, {x1, cy}, {cx, y1}, {x0, cy}};
				for (int[] p : pts) {
					bitmap.fill(p[0] - hs, p[1] - hs, p[0] + hs, p[1] + hs, scaleHandleColor);
					bitmap.box (p[0] - hs, p[1] - hs, p[0] + hs, p[1] + hs, scaleHandleBorderColor);
				}

				bitmap.fill(cx - 3, cy - 3, cx + 3, cy + 3, scaleHandleColor);
				bitmap.box (cx - 3, cy - 3, cx + 3, cy + 3, scaleHandleBorderColor);
				break;
			}
		}
	}
	
	private void drawSelectionOverlay(Bitmap bitmap, Instance instance) {
		Hitbox globalBox = instance.getGlobalAABB();
		if (globalBox == null) return;

		var topLeft = editWorld.worldToScreen(globalBox.getX(), globalBox.getY());
		var bottomRight = editWorld.worldToScreen(globalBox.getX() + globalBox.getWidth(), globalBox.getY() + globalBox.getHeight());
		int x0 = Math.round(topLeft.x);
		int y0 = Math.round(topLeft.y);
		int x1 = Math.round(bottomRight.x);
		int y1 = Math.round(bottomRight.y);

		// Always draw selection border
		bitmap.box(x0, y0, x1, y1, selectionColor);

		int cx = (x0 + x1) / 2;
		int cy = (y0 + y1) / 2;
		int hs = handleSize / 2;   // half-size for square handles = 8

		// Arrow shaft goes from box edge to tip (gap outside box, must match detectHandle)
		int gap = 16;

		switch (activeTool) {
			case SELECT:
				// Only the blue border, nothing else
				break;

			case MOVE: {
				// Four colored arrows extending outward from the box center
				int red   = 0xffff3333;
				int green = 0xff33ff33;

				// --- X-axis (red) ---
				int leftTipX  = x0 - gap - 10;
				int rightTipX = x1 + gap + 10;
				drawArrow(bitmap, x0 - gap, cy, leftTipX,  cy, red);
				drawArrow(bitmap, x1 + gap, cy, rightTipX, cy, red);

				// --- Y-axis (green) ---
				int topTipY    = y0 - gap - 10;
				int bottomTipY = y1 + gap + 10;
				drawArrow(bitmap, cx, y0 - gap, cx, topTipY,    green);
				drawArrow(bitmap, cx, y1 + gap, cx, bottomTipY, green);

				// Center square (yellow)
				bitmap.fill(cx - 4, cy - 4, cx + 4, cy + 4, handleColor);
				bitmap.box (cx - 4, cy - 4, cx + 4, cy + 4, 0xff000000);
				break;
			}

			case SCALE: {
				// 8 white squares with black border on corners + edge midpoints
				int[][] pts = {{x0, y0}, {x1, y0}, {x1, y1}, {x0, y1},
				               {cx, y0}, {x1, cy}, {cx, y1}, {x0, cy}};
				for (int[] p : pts) {
					bitmap.fill(p[0] - hs, p[1] - hs, p[0] + hs, p[1] + hs, scaleHandleColor);
					bitmap.box (p[0] - hs, p[1] - hs, p[0] + hs, p[1] + hs, scaleHandleBorderColor);
				}

				// Center dot
				bitmap.fill(cx - 3, cy - 3, cx + 3, cy + 3, scaleHandleColor);
				bitmap.box (cx - 3, cy - 3, cx + 3, cy + 3, scaleHandleBorderColor);
				break;
			}
		}
	}

	/**
	 * Draws a single arrow: shaft from (x1,y1) → (x2,y2), arrowhead at (x2,y2).
	 * Arrowhead orientation is computed from the shaft direction.
	 */
	private void drawArrow(Bitmap bitmap, int x1, int y1, int x2, int y2, int color) {
		// Shaft
		bitmap.drawLine(x1, y1, x2, y2, color, 3);

		// Arrowhead direction
		int dx = x2 - x1;
		int dy = y2 - y1;
		int sign = (Math.abs(dx) >= Math.abs(dy)) ? ((dx > 0) ? 1 : -1) : ((dy > 0) ? 1 : -1);
		int s = 7; // arrowhead size
		int tipX = x2, tipY = y2;

		int[] xp, yp;
		if (Math.abs(dx) >= Math.abs(dy)) {
			// Horizontal arrowhead ← →
			xp = new int[]{tipX, tipX - sign * s, tipX - sign * s};
			yp = new int[]{tipY, tipY - 5, tipY + 5};
		} else {
			// Vertical arrowhead ↑ ↓
			xp = new int[]{tipX, tipX - 5, tipX + 5};
			yp = new int[]{tipY, tipY - sign * s, tipY - sign * s};
		}
		bitmap.fillPolygon(xp, yp, 3, color);
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
			bitmap.drawLine(x, 0, x, bitmap.h - 1, gridColor, 1);
		}
		
		for (int worldY = startY; worldY <= bottomRight.y; worldY += gridSize) {
			var screenPoint = editWorld.worldToScreen(topLeft.x, worldY);
			int y = Math.round(screenPoint.y);
			bitmap.drawLine(0, y, bitmap.w - 1, y, gridColor, 1);
		}
	}
	
	public void newWorld() {
		var newWorld = new World("Untitled World");
		
		Lighting lighting = newWorld.getService("Lighting");
		Sky sky = new Sky(0xffffffff);
		sky.setTexture("sky");
		
		lighting.addChild(sky);
		lighting.setSky(sky);
		
		this.selectedWorldFile = null;
		
		try {
			if (this.newWorldListener != null)
				newWorldListener.accept(newWorld);
		} finally {
			loadWorld(newWorld);
		}
	}
	
	public void loadWorld() {
		JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(Lang.get("studio.loadWorldDialog"));
		fileChooser.setFileFilter(new FileNameExtensionFilter("DikenEngine World File", "dwf"));

        int result = fileChooser.showOpenDialog(engineWindow);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = fileChooser.getSelectedFile();
        
        try {
			loadWorld(World.loadWorld(selectedFile));
			
			this.selectedWorldFile = selectedFile;
		} catch (IOException | ReflectiveOperationException e) {
			CrashDialog.crash(engineWindow, e, "studio.error.loadWorldDialog");
		}
	}
	
	public void saveWorld(boolean openFileChooser) {
		File selectedFile = this.selectedWorldFile;
		
		if (this.selectedWorldFile == null || openFileChooser) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle(Lang.get("studio.saveWorldDialog"));
			fileChooser.setFileFilter(new FileNameExtensionFilter("DikenEngine World File", "dwf"));

        	int result = fileChooser.showSaveDialog(engineWindow);
        	if (result != JFileChooser.APPROVE_OPTION) return;

        	selectedFile = fileChooser.getSelectedFile();
        	
        	if (!selectedFile.getName().endsWith(".dwf")) {
        		selectedFile = new File(selectedFile.getAbsolutePath() + ".dwf");
        	}
		}
        
        try {
			World.saveWorld(editWorld, selectedFile);
			
			this.selectedWorldFile = selectedFile;
		} catch (IOException e) {
			CrashDialog.crash(engineWindow, e, "studio.error.saveWorldDialog");
		}
	}
	
	private JToolBar generateToolbar() {
		var toolbarBuilder = new Toolbar.Builder();
		
		var basicFileToolbar = toolbarBuilder.newToolbar("BasicFileTools");
		
		toolbarBuilder.addButton(basicFileToolbar, "newWorld", 10, 0, "studio.toolbar.newWorld", this::newWorld);
		toolbarBuilder.addButton(basicFileToolbar, "loadWorld", 9, 0, "studio.toolbar.loadWorld", this::loadWorld);
		toolbarBuilder.addButton(basicFileToolbar, "saveWorld", 8, 0, "studio.toolbar.saveWorld", () -> this.saveWorld(false));
		
		var playTestButtons = toolbarBuilder.newToolbar("PlayTestTools");
		
		toolbarBuilder.addButton(playTestButtons, "startPlayTest", 3, 0, "studio.toolbar.startPlayTest", this::startPlayTest);
		toolbarBuilder.addButton(playTestButtons, "stopPlayTest", 13, 0, "studio.toolbar.stopPlayTest", this::stopPlayTest);
		
		var instanceMovementTools = toolbarBuilder.newToolbar("InstanceMovementTools");
		addMovementTools(instanceMovementTools);
		
		toolbarBuilder.addButton(instanceMovementTools, "deletePart", 0, 0, "studio.toolbar.deletePart", () -> {
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
		
		toolbarBuilder.addButton(basicObjectTools, "createPart", 0, 1, "studio.toolbar.createPart",
				() -> addNodeToSelected(new Part()));
		toolbarBuilder.addButton(basicObjectTools, "createDecal", 1, 1, "studio.toolbar.createDecal",
				() -> addNodeToSelected(new Decal()));
		toolbarBuilder.addButton(basicObjectTools, "createFolder", 6, 1, "studio.toolbar.createFolder",
				() -> addNodeToSelected(new Folder()));
		
		var scriptTools = toolbarBuilder.newToolbar("ScriptTools");
		toolbarBuilder.addButton(scriptTools, "newScript", 12, 1, "studio.toolbar.newScript",
				() -> codeEditorPanel.newScript());
		
		return toolbarBuilder.getJToolBar();
	}
	
	private void addMovementTools(Toolbar toolbar) {
		ButtonGroup gridGroup = new ButtonGroup();
		
		var selectPart = createMovementButton(4, 0, "studio.toolbar.selectPart", EditorTool.SELECT);
		var movePart = createMovementButton(5, 0, "studio.toolbar.movePart", EditorTool.MOVE);
		var scalePart = createMovementButton(6, 0, "studio.toolbar.scalePart", EditorTool.SCALE);
		
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
		button.addActionListener(e -> activeTool = tool);
		return button;
	}

	private void addGridButtons(Toolbar toolbar) {
		ButtonGroup gridGroup = new ButtonGroup();
		
		JToggleButton noGridButton = createGridButton(Lang.get("studio.none"), "studio.toolbar.noGrid", 0);
		JToggleButton grid8Button = createGridButton("8", "studio.toolbar.halfGrid", 8);
		JToggleButton grid16Button = createGridButton("16", "studio.toolbar.defaultGrid", 16);
		
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
		button.setToolTipText(Lang.get(tooltip));
		button.addActionListener(_ -> gridSize = size);
		return button;
	}
	
	private JMenuBar generateMenuBar(ExplorerPanel explorerPanel, EditorTabPanel scriptTabPanel) {
		var menubarBuilder = new Menubar.Builder();
		
		var fileMenu = menubarBuilder.newMenu("fileMenu", "studio.menubar.fileMenu");
		menubarBuilder.addMenuItem(fileMenu, "studio.menubar.new", 10, 0, this::newWorld,
				KeyStroke.getKeyStroke('N', KeyEvent.CTRL_DOWN_MASK));
		menubarBuilder.addMenuItem(fileMenu, "studio.menubar.load", 9, 0, this::loadWorld);
		menubarBuilder.addMenuItem(fileMenu, "studio.menubar.save", 8, 0, () -> this.saveWorld(false),
				KeyStroke.getKeyStroke('S', KeyEvent.CTRL_DOWN_MASK));
		menubarBuilder.addMenuItem(fileMenu, "studio.menubar.saveAs", -1, -1, () -> this.saveWorld(true));
		menubarBuilder.addMenuSeparator(fileMenu);
		menubarBuilder.addMenuItem(fileMenu, "studio.menubar.export", 11, 0, this::exportProject);
		menubarBuilder.addMenuSeparator(fileMenu);
		menubarBuilder.addMenuItem(fileMenu, "studio.menubar.closeApp", 0, 0, this::stop,
				KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK));
		
		var editMenu = menubarBuilder.newMenu("editMenu", "studio.menubar.editMenu");
		menubarBuilder.addMenuItem(editMenu, "studio.menubar.preferences", 1, 3,
				() -> new SettingsDialog(engineWindow, this).setVisible(true));
		
		var windowsMenu = menubarBuilder.newMenu("windowsMenu", "studio.menubar.windowsMenu");
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

		var toolsMenu = menubarBuilder.newMenu("toolsMenu", "studio.menubar.toolsMenu");
		menubarBuilder.addMenuItem(toolsMenu, "studio.menubar.runScript", 12, 1, () -> {
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
		menubarBuilder.addMenuItem(toolsMenu, "studio.menubar.resetCamera", 13, 3, () -> editWorld.camera.reset());
		menubarBuilder.addMenuSeparator(toolsMenu);
		menubarBuilder.addMenuItem(toolsMenu, "studio.menubar.gc", 5, 3, System::gc);
		
		var helpMenu = menubarBuilder.newMenu("helpMenu", "studio.menubar.helpMenu");
		menubarBuilder.addMenuItem(helpMenu, "studio.about", 12, 3, () -> 
			new AboutWindow(engineWindow).setVisible(true), "DikenEngine"
		);
		menubarBuilder.addMenuSeparator(helpMenu);
		menubarBuilder.addMenuItem(helpMenu, "studio.menubar.github", -1, -1, () -> 
			Utils.openPage(URI.create("https://github.com/OfficialEmirE/DikenEngine"))
		);
		menubarBuilder.addMenuItem(helpMenu, "studio.menubar.docs", -1, -1, () -> 
			Utils.openPage(URI.create("https://github.com/OfficialEmirE/DikenEngine"))
		);
		menubarBuilder.addMenuItem(helpMenu, "studio.windows.objectBrowser", 9, 3, () -> {
			scriptTabPanel.openEditor(new ObjectBrowserPanel());
		});
		
		return menubarBuilder.getJMenuBar();
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
