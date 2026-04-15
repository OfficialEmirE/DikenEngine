package me.ramazanenescik04.diken.studio;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.Setting.EnumSettingType;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.nodes.Sky;
import me.ramazanenescik04.diken.game.world.World;
import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.CheckBox;
import me.ramazanenescik04.diken.gui.compoment.GuiComponent;
import me.ramazanenescik04.diken.gui.compoment.Panel;
import me.ramazanenescik04.diken.gui.compoment.ScrollPanel;
import me.ramazanenescik04.diken.gui.compoment.Text;
import me.ramazanenescik04.diken.gui.compoment.TextField;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.gui.screen.Screen;
import me.ramazanenescik04.diken.input.InputHandler;
import me.ramazanenescik04.diken.gui.window.ColorPickWindow;
import me.ramazanenescik04.diken.gui.window.ColorPickWindow.ColorPickFuture;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.studio.AddInstanceWindow.AddInstanceFuture;
import me.ramazanenescik04.diken.tools.ByteTransferable;

public class StudioScreen extends Screen {
	private static final int PADDING = 8;
	private static final int HEADER_HEIGHT = 18;
	private static final int TOOLBAR_HEIGHT = 28;
	private static final int SIDE_PANEL_WIDTH = 170;
	private static final int EXPLORER_SPLIT_HEIGHT = 190;
	private static final int ROW_HEIGHT = 18;
	private static final int CONTENT_GAP = 4;
	private static final int CONTEXT_MENU_WIDTH = 104;
	private static final int DROP_NONE = 0;
	private static final int DROP_BEFORE = 1;
	private static final int DROP_INTO = 2;
	private static final int DROP_AFTER = 3;
	static final int TOOL_SELECT = 0;
	static final int TOOL_MOVE = 1;
	static final int TOOL_RESIZE = 2;
	private static final int AXIS_NONE = 0;
	private static final int AXIS_X = 1;
	private static final int AXIS_Y = 2;
	private static final float ZOOM_MIN = 0.25f;
	private static final float ZOOM_MAX = 4.0f;
	private static final float ZOOM_STEP = 0.5f;
	private static final int CAMERA_PAN_SPEED = 4;

	final Screen parent;
	World world;
	Node selectedNode;

	private StudioRootPanel rootPanel;
	private StudioToolbarPanel toolbarPanel;
	private StudioExplorerPanel explorerPanel;
	private StudioPanePanel gamePanel;
	private StudioPropertiesPanel propertiesPanel;
	private ScrollPanel explorerScrollPanel;
	private ScrollPanel propertiesScrollPanel;
	private Panel explorerContent;
	private Panel propertiesContent;
	private StudioContextMenuPanel contextMenuPanel;
	private StudioRenamePanel renamePanel;
	private boolean ignoreRenameCloseClick;
	private Node pressedExplorerNode;
	private Node draggedNode;
	private Node dropTargetNode;
	private int dropMode = DROP_NONE;
	private int pressMouseX;
	private int pressMouseY;
	@SuppressWarnings("unused")
	private int dragMouseX;
	@SuppressWarnings("unused")
	private int dragMouseY;
	private boolean suppressNextClick;
	int activeTool = TOOL_SELECT;
	Button selectToolButton;
	Button moveToolButton;
	Button resizeToolButton;
	private Node pressedGameNode;
	private Node activeGameNode;
	private boolean draggingGameNode;
	private boolean draggingXAxis;
	private boolean draggingYAxis;
	private boolean draggingResize;
	private int gamePressWorldX;
	private int gamePressWorldY;
	private int gameNodeStartX;
	private int gameNodeStartY;
	private int gameStartAABBWidth;
	private int gameStartAABBHeight;
	private float viewZoom = 1.0f;
	private boolean rightMousePanning;
	private int panStartScreenX;
	private int panStartScreenY;
	private int panStartCameraX;
	private int panStartCameraY;

	public StudioScreen(Screen parent) {
		this(parent, null);
	}

	public StudioScreen(Screen parent, World world) {
		this.parent = parent;
		this.world = world != null ? world.copy() : null;
	}

	@Override
	public void openScreen() {
		if (world == null) {
			world = createDefaultWorld();
		}
		viewZoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, world.getZoom()));
		world.setZoom(viewZoom);
		selectedNode = world.root;

		rootPanel = new StudioRootPanel();
		toolbarPanel = new StudioToolbarPanel(this);
		explorerPanel = new StudioExplorerPanel();
		gamePanel = new StudioPanePanel("Game");
		propertiesPanel = new StudioPropertiesPanel();

		explorerScrollPanel = new ScrollPanel(4, HEADER_HEIGHT + 4, 50, 50);
		propertiesScrollPanel = new ScrollPanel(4, HEADER_HEIGHT + 4, 50, 50);

		explorerContent = new StudioContentPanel();
		propertiesContent = new StudioContentPanel();

		explorerPanel.add(explorerScrollPanel);
		propertiesPanel.add(propertiesScrollPanel);
		rootPanel.add(toolbarPanel);
		rootPanel.add(explorerPanel);
		rootPanel.add(gamePanel);
		rootPanel.add(propertiesPanel);
		
		updateToolbarToolButtons();

		setContentPane(rootPanel);
		gamePanel.add(world);

		layoutPanels();
		rebuildExplorer();
		rebuildProperties();
		
		explorerScrollPanel.setScrollComponent(explorerContent);
		propertiesScrollPanel.setScrollComponent(propertiesContent);
	}
	
	@Override
	public void tick() {
		handleKeyboardCameraPan();
		super.tick();
	}

	@Override
	public void keyDown(char eventCharacter, int eventKey) {
		super.keyDown(eventCharacter, eventKey);
		if (eventKey == KeyEvent.VK_ESCAPE) {
			hideRenamePanel();
			hideContextMenu();
			resetExplorerDrag();
			resetGameManipulation();
		}
		if (eventKey == KeyEvent.VK_ESCAPE && parent != null) {
			engine.setCurrentScreen(parent);
		}
		
		boolean pressingCtrl = engine.input.isKeyDown(KeyEvent.VK_CONTROL);
		if (eventKey == KeyEvent.VK_D && pressingCtrl && selectedNode != world.root) {
			Node parentNode = selectedNode != null ? selectedNode.getParent() : world.root;
			
			Node copySelected = selectedNode.copy();
			
			parentNode.addChild(copySelected);
			setSelectedNode(copySelected);
			
			rebuildExplorer();
		}
		
		if (eventKey == KeyEvent.VK_C && pressingCtrl && selectedNode != world.root) {
			copyNode(false);
		}
		
		if (eventKey == KeyEvent.VK_X && pressingCtrl && selectedNode != world.root) {
			copyNode(true);
		}
		
		if (eventKey == KeyEvent.VK_V && pressingCtrl) {
			Node parentNode = selectedNode != null ? selectedNode : world.root;
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		    try {
		        if (cb.isDataFlavorAvailable(ByteTransferable.BYTE_ARRAY_FLAVOR)) {
		            byte[] nodeBytes = (byte[]) cb.getData(ByteTransferable.BYTE_ARRAY_FLAVOR);
		            
		            Node copyNode = byteToNode(nodeBytes);
		            parentNode.addChild(copyNode);
		            setSelectedNode(copyNode);
		            
		            rebuildExplorer();
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
	}
	
	private static byte[] copyToByte(Node object) {
        try {
            // 1. Obje verisini bayt dizisine yazma (Serialize)
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.flush();
            
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
	
	private static Node byteToNode(byte[] object) {
        try {
            // 1. Obje verisini bayt dizisine yazma (Serialize)
            ByteArrayInputStream bos = new ByteArrayInputStream(object);
            ObjectInputStream oos = new ObjectInputStream(bos);
            
            return (Node) oos.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
	
	private void copyNode(boolean cut) {
		Node copyNode = selectedNode.copy();
		
		if (cut) {
			deleteNode(selectedNode);
			rebuildExplorer();
		}
		
		ByteTransferable byteT = new ByteTransferable(copyToByte(copyNode));
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(byteT, null);
	}

	@Override
	public void mouseEvent(int inputMode, int x, int y, int clicked) {
		if (this.engine == null) {
			return;
		}
		
		boolean screenActionMode = engine.wManager.screenActionMode(new java.awt.Point(x, y));
		boolean mouseOnScreen = engine.input.isMouseOnScreen();
		this.mouseGetInfo(x, y, screenActionMode, mouseOnScreen);
		
		if (inputMode == InputHandler.INPUT_WHEEL) {
			handleMouseWheelZoom(x, y, clicked);
		}
		
		if (inputMode == InputHandler.INPUT_PRESSED && clicked == 2) {
			beginRightMousePan(x, y);
		}
		
		if (inputMode == InputHandler.INPUT_PRESSED && clicked == 0) {
			StudioExplorerNodeButton button = findExplorerNodeButtonAt(x, y);
			pressedExplorerNode = button != null ? button.getNode() : null;
			pressMouseX = x;
			pressMouseY = y;
			dragMouseX = x;
			dragMouseY = y;
			beginGameManipulation(x, y);
		} else if (inputMode == InputHandler.INPUT_REPEATED && engine.input.isMouseDown(0)) {
			handleExplorerDrag(x, y);
			handleGameManipulation(x, y);
		} else if (inputMode == InputHandler.INPUT_RELEASED) {
			if (draggedNode != null) {
				finishExplorerDrag();
			} else {
				pressedExplorerNode = null;
			}
			finishGameManipulation(x, y);
		}
		
		if (inputMode == InputHandler.INPUT_REPEATED && engine.input.isMouseDown(2)) {
			updateRightMousePan(x, y);
		}
		if (inputMode == InputHandler.INPUT_RELEASED && !engine.input.isMouseDown(2)) {
			endRightMousePan();
		}
		
		if (inputMode == InputHandler.INPUT_PRESSED || inputMode == InputHandler.INPUT_REPEATED) {
			if (rightMousePanning) {
				return;
			}
			
			if (suppressNextClick) {
				suppressNextClick = false;
				return;
			}
			
			handleGameClick(x, y, clicked);
			this.mouseClick(x, y, clicked, mouseOnScreen, screenActionMode);
		}
	}
	
	@Override
	public void mouseClick(int mouseX, int mouseY, int eventButton, boolean isScreenActionMode, boolean isMouseOnScreen) {
		super.mouseClick(mouseX, mouseY, eventButton, isScreenActionMode, isMouseOnScreen);
		
		if (contextMenuPanel != null && !containsPoint(contextMenuPanel, mouseX, mouseY)) {
			hideContextMenu();
		}
		
		if (renamePanel != null && ignoreRenameCloseClick) {
			ignoreRenameCloseClick = false;
		} else if (renamePanel != null && !containsPoint(renamePanel, mouseX, mouseY)) {
			hideRenamePanel();
		}
	}

	@Override
	public void resized() {
		layoutPanels();
		rebuildExplorer();
		rebuildProperties();
		hideContextMenu();
		hideRenamePanel();
	}
	
	@Override
	public void render(Bitmap bitmap) {
		super.render(bitmap);
		renderGameToolOverlay(bitmap);
	}
	
	private void renderGameToolOverlay(Bitmap bitmap) {
		if (selectedNode == null || world == null) {
			return;
		}
		
		Hitbox worldBox = selectedNode.getGlobalAABB();
		Hitbox screenBox = worldToScreenHitbox(worldBox);
		if (screenBox == null) {
			return;
		}
		
		bitmap.box(screenBox.x, screenBox.y, screenBox.x + screenBox.width, screenBox.y + screenBox.height, 0xffffe66f);
		
		if (activeTool == TOOL_MOVE) {
			renderMoveHandles(bitmap, screenBox);
		} else if (activeTool == TOOL_RESIZE) {
			renderResizeHandle(bitmap, screenBox);
		}
	}
	
	private void renderMoveHandles(Bitmap bitmap, Hitbox screenBox) {
		int centerX = screenBox.x + screenBox.width / 2;
		int centerY = screenBox.y + screenBox.height / 2;
		Hitbox xHandle = worldToScreenHitbox(getMoveXHandleWorld());
		Hitbox yHandle = worldToScreenHitbox(getMoveYHandleWorld());
		
		if (xHandle != null) {
			bitmap.drawLine(centerX, centerY, xHandle.x, xHandle.y + xHandle.height / 2, 0xffff6f6f, 2);
			bitmap.fillPolygon(
				new int[] { xHandle.x + xHandle.width, xHandle.x, xHandle.x },
				new int[] { xHandle.y + xHandle.height / 2, xHandle.y, xHandle.y + xHandle.height },
				draggingXAxis ? 0xffff3f3f : 0xffff6f6f
			);
		}
		
		if (yHandle != null) {
			bitmap.drawLine(centerX, centerY, yHandle.x + yHandle.width / 2, yHandle.y + yHandle.height, 0xff6fff87, 2);
			bitmap.fillPolygon(
				new int[] { yHandle.x, yHandle.x + yHandle.width, yHandle.x + yHandle.width / 2 },
				new int[] { yHandle.y + yHandle.height, yHandle.y + yHandle.height, yHandle.y },
				draggingYAxis ? 0xff3fff63 : 0xff6fff87
			);
		}
	}
	
	private void renderResizeHandle(Bitmap bitmap, Hitbox screenBox) {
		Hitbox resizeHandle = worldToScreenHitbox(getResizeHandleWorld());
		if (resizeHandle == null) {
			return;
		}
		
		bitmap.fill(resizeHandle.x, resizeHandle.y, resizeHandle.x + resizeHandle.width, resizeHandle.y + resizeHandle.height,
			draggingResize ? 0xff89c4ff : 0xff63a7f0);
		bitmap.box(resizeHandle.x, resizeHandle.y, resizeHandle.x + resizeHandle.width, resizeHandle.y + resizeHandle.height, 0xffffffff);
	}

	private void layoutPanels() {
		if (engine == null || rootPanel == null) {
			return;
		}

		int scaledWidth = engine.getScaledWidth();
		int scaledHeight = engine.getScaledHeight();
		int bodyTop = PADDING + TOOLBAR_HEIGHT + 4;
		int bodyHeight = Math.max(1, scaledHeight - bodyTop - PADDING);
		int centerWidth = Math.max(1, scaledWidth - (PADDING * 3) - SIDE_PANEL_WIDTH);
		int explorerHeight = Math.min(Math.max(90, EXPLORER_SPLIT_HEIGHT), Math.max(90, bodyHeight - 70));
		int propertiesHeight = Math.max(1, bodyHeight - explorerHeight - PADDING);

		rootPanel.setSize(scaledWidth, scaledHeight);
		toolbarPanel.setBounds(PADDING, PADDING, scaledWidth - (PADDING * 2), TOOLBAR_HEIGHT);
		explorerPanel.setBounds(PADDING, bodyTop, SIDE_PANEL_WIDTH, explorerHeight);
		propertiesPanel.setBounds(PADDING, bodyTop + explorerHeight + PADDING, SIDE_PANEL_WIDTH, propertiesHeight);
		gamePanel.setBounds((PADDING * 2) + SIDE_PANEL_WIDTH, bodyTop, centerWidth, bodyHeight);

		int scrollWidth = Math.max(1, SIDE_PANEL_WIDTH - 8);
		int explorerScrollHeight = Math.max(1, explorerPanel.getHeight() - HEADER_HEIGHT - 8);
		int propertiesScrollHeight = Math.max(1, propertiesPanel.getHeight() - HEADER_HEIGHT - 8);
		explorerScrollPanel.setBounds(4, HEADER_HEIGHT + 4, scrollWidth, explorerScrollHeight);
		propertiesScrollPanel.setBounds(4, HEADER_HEIGHT + 4, scrollWidth, propertiesScrollHeight);

		world.setBounds(PADDING, HEADER_HEIGHT + 4, Math.max(1, gamePanel.getWidth() - (PADDING * 2)),
				Math.max(1, gamePanel.getHeight() - HEADER_HEIGHT - PADDING - 4));
	}

	private void setSelectedNode(Node node) {
		if (node == null || node == selectedNode) {
			return;
		}
		hideContextMenu();
		hideRenamePanel();
		resetGameManipulation();
		selectedNode = node;
		rebuildExplorer();
		rebuildProperties();
	}
	
	private boolean containsPoint(GuiComponent component, int x, int y) {
		int globalX = component.getGlobalX();
		int globalY = component.getGlobalY();
		return x >= globalX && x <= globalX + component.getWidth()
			&& y >= globalY && y <= globalY + component.getHeight();
	}
	
	private void showContextMenu(Node node, int x, int y) {
		hideContextMenu();
		hideRenamePanel();
		selectedNode = node;
		rebuildExplorer();
		rebuildProperties();
		
		int menuX = Math.max(0, Math.min(x, rootPanel.getWidth() - CONTEXT_MENU_WIDTH - 2));
		int menuY = Math.max(0, Math.min(y, rootPanel.getHeight() - 86));
		contextMenuPanel = new StudioContextMenuPanel(
			node,
			menuX,
			menuY,
			CONTEXT_MENU_WIDTH,
			86,
			target -> {
				showRenamePanel(target, this.gamePanel.x + 5, this.gamePanel.y + 5);
				hideContextMenu();
			},
			target -> {
				deleteNode(target);
				hideContextMenu();
			},
			target -> {
				addInstanceToNode(target);
				hideContextMenu();
			},
			node != world.root && node.getParent() != null
		);
		rootPanel.add(contextMenuPanel);
	}
	
	private void hideContextMenu() {
		if (contextMenuPanel != null && rootPanel != null && rootPanel.isVaild(contextMenuPanel)) {
			rootPanel.remove(contextMenuPanel);
		}
		contextMenuPanel = null;
	}
	
	private void showRenamePanel(Node node, int x, int y) {
		hideRenamePanel();
		int panelWidth = 132;
		int panelHeight = 44;
		int panelX = Math.max(0, Math.min(x, rootPanel.getWidth() - panelWidth - 2));
		int panelY = Math.max(0, Math.min(y, rootPanel.getHeight() - panelHeight - 2));
		renamePanel = new StudioRenamePanel(node, panelX, panelY, panelWidth, panelHeight, (targetNode, value) -> {
			targetNode.setName(value);
			rebuildExplorer();
			rebuildProperties();
		}, this::hideRenamePanel);
		rootPanel.add(renamePanel);
		ignoreRenameCloseClick = true;
	}
	
	private void hideRenamePanel() {
		if (renamePanel != null && rootPanel != null && rootPanel.isVaild(renamePanel)) {
			rootPanel.remove(renamePanel);
		}
		renamePanel = null;
		ignoreRenameCloseClick = false;
	}
	
	private void deleteNode(Node node) {
		if (node == null || node == world.root || node.getParent() == null) {
			return;
		}
		Node parentNode = node.getParent();
		parentNode.removeChild(node);
		setSelectedNode(parentNode);
	}
	
	void addInstanceToNode(Node node) {
		Node parentNode = node != null ? node : world.root;
		
		var addInstanceWindow = new AddInstanceWindow(0, 0, new AddInstanceFuture() {
			@Override
			public void cancelled() {
			}

			@Override
			public void success(Node node) {
				parentNode.addChild(node);
				setSelectedNode(node);
			}
		});
		engine.wManager.addWindow(addInstanceWindow, true);
		 
	}
	
	private StudioExplorerNodeButton findExplorerNodeButtonAt(int x, int y) {
		if (explorerContent == null) {
			return null;
		}
		
		for (GuiComponent component : explorerContent.getCompoments()) {
			if (component instanceof StudioExplorerNodeButton button && containsPoint(button, x, y)) {
				return button;
			}
		}
		return null;
	}
	
	private void handleExplorerDrag(int x, int y) {
		dragMouseX = x;
		dragMouseY = y;
		
		if (pressedExplorerNode == null && draggedNode == null) {
			return;
		}
		
		if (draggedNode == null) {
			if (Math.abs(x - pressMouseX) < 4 && Math.abs(y - pressMouseY) < 4) {
				return;
			}
			draggedNode = pressedExplorerNode;
			hideContextMenu();
			hideRenamePanel();
		}
		
		StudioExplorerNodeButton hoverButton = findExplorerNodeButtonAt(x, y);
		if (hoverButton == null) {
			dropTargetNode = null;
			dropMode = DROP_NONE;
			return;
		}
		
		Node targetNode = hoverButton.getNode();
		if (!isValidDropTarget(draggedNode, targetNode)) {
			dropTargetNode = null;
			dropMode = DROP_NONE;
			return;
		}
		
		dropTargetNode = targetNode;
		int localY = y - hoverButton.getGlobalY();
		if (localY < hoverButton.getHeight() / 3) {
			dropMode = DROP_BEFORE;
		} else if (localY > (hoverButton.getHeight() * 2) / 3) {
			dropMode = DROP_AFTER;
		} else {
			dropMode = DROP_INTO;
		}
	}
	
	private boolean isValidDropTarget(Node dragNode, Node targetNode) {
		if (dragNode == null || targetNode == null || dragNode == targetNode) {
			return false;
		}
		if (targetNode.isDescendantOf(dragNode)) {
			return false;
		}
		return true;
	}
	
	private void finishExplorerDrag() {
		if (draggedNode != null && dropTargetNode != null && dropMode != DROP_NONE) {
			applyNodeDrop(draggedNode, dropTargetNode, dropMode);
			suppressNextClick = true;
		}
		resetExplorerDrag();
		rebuildExplorer();
		rebuildProperties();
	}
	
	private void applyNodeDrop(Node dragNode, Node targetNode, int mode) {
		if (!isValidDropTarget(dragNode, targetNode)) {
			return;
		}
		
		if (mode == DROP_INTO) {
			targetNode.insertChild(targetNode.getChildren().size(), dragNode);
			setSelectedNode(dragNode);
			return;
		}
		
		Node parentNode = targetNode.getParent();
		if (parentNode == null || parentNode == dragNode || parentNode.isDescendantOf(dragNode)) {
			return;
		}
		
		int targetIndex = parentNode.getChildIndex(targetNode);
		if (targetIndex < 0) {
			return;
		}
		
		if (mode == DROP_AFTER) {
			parentNode.insertChild(targetIndex + 1, dragNode);
		} else if (mode == DROP_BEFORE) {
			parentNode.insertChild(targetIndex, dragNode);
		}
		
		setSelectedNode(dragNode);
	}
	
	private void resetExplorerDrag() {
		pressedExplorerNode = null;
		draggedNode = null;
		dropTargetNode = null;
		dropMode = DROP_NONE;
	}
	
	private void handleKeyboardCameraPan() {
		java.awt.Point point = engine.input.getMousePosition();
		if (engine == null || world == null || (!isInsideWorldViewport(point.x, point.y) || !engine.wManager.screenActionMode(point)) || engine.input.isKeyDown(KeyEvent.VK_CONTROL)) {
			return;
		}
		
		int speed = Math.max(1, Math.round(CAMERA_PAN_SPEED / Math.max(0.001f, viewZoom)));
		if (engine.input.isKeyDown(KeyEvent.VK_SHIFT)) {
			speed *= 2;
		}
		
		if (engine.input.isKeyDown(KeyEvent.VK_W)) {
			world.camera.y -= speed;
		}
		if (engine.input.isKeyDown(KeyEvent.VK_S)) {
			world.camera.y += speed;
		}
		if (engine.input.isKeyDown(KeyEvent.VK_A)) {
			world.camera.x -= speed;
		}
		if (engine.input.isKeyDown(KeyEvent.VK_D)) {
			world.camera.x += speed;
		}
	}
	
	private void handleMouseWheelZoom(int screenX, int screenY, int wheelDelta) {
		if (wheelDelta == 0 || world == null || (!isInsideWorldViewport(screenX, screenY) || !engine.wManager.screenActionMode(new java.awt.Point(screenX, screenY)))) {
			return;
		}
		
		java.awt.Point focusPoint = toWorldPointWithZoom(screenX, screenY, viewZoom);
		float targetZoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, viewZoom + (-wheelDelta * ZOOM_STEP)));
		if (Math.abs(targetZoom - viewZoom) < 0.0001f) {
			return;
		}
		
		viewZoom = targetZoom;
		world.setZoom(viewZoom);
		
		int localX = screenX - world.getGlobalX();
		int localY = screenY - world.getGlobalY();
		world.camera.x = Math.round(focusPoint.x - (localX / viewZoom));
		world.camera.y = Math.round(focusPoint.y - (localY / viewZoom));
	}
	
	private void beginRightMousePan(int screenX, int screenY) {
		if ((!isInsideWorldViewport(screenX, screenY) && engine.wManager.screenActionMode(new java.awt.Point(screenX, screenY))) || world == null) {
			return;
		}
		rightMousePanning = true;
		panStartScreenX = screenX;
		panStartScreenY = screenY;
		panStartCameraX = world.camera.x;
		panStartCameraY = world.camera.y;
	}
	
	private void updateRightMousePan(int screenX, int screenY) {
		if (!rightMousePanning || world == null) {
			return;
		}
		float invZoom = 1.0f / Math.max(0.001f, viewZoom);
		world.camera.x = panStartCameraX - Math.round((screenX - panStartScreenX) * invZoom);
		world.camera.y = panStartCameraY - Math.round((screenY - panStartScreenY) * invZoom);
	}
	
	private void endRightMousePan() {
		rightMousePanning = false;
	}
	
	void updateToolbarToolButtons() {
		if (selectToolButton == null || moveToolButton == null || resizeToolButton == null) {
			return;
		}
		
		selectToolButton.setButtonColor(activeTool == TOOL_SELECT ? 0xff5f89c5 : 0xffffffff);
		moveToolButton.setButtonColor(activeTool == TOOL_MOVE ? 0xff5f89c5 : 0xffffffff);
		resizeToolButton.setButtonColor(activeTool == TOOL_RESIZE ? 0xff5f89c5 : 0xffffffff);
	}
	
	private boolean isInsideWorldViewport(int screenX, int screenY) {
		int gx = world.getGlobalX();
		int gy = world.getGlobalY();
		return screenX >= gx && screenX <= gx + world.getWidth()
			&& screenY >= gy && screenY <= gy + world.getHeight();
	}
	
	private java.awt.Point toWorldPointWithZoom(int screenX, int screenY, float zoom) {
		int localX = screenX - world.getGlobalX();
		int localY = screenY - world.getGlobalY();
		float invZoom = 1.0f / Math.max(0.001f, zoom);
		return new java.awt.Point(Math.round(localX * invZoom) + world.camera.x, Math.round(localY * invZoom) + world.camera.y);
	}
	
	private java.awt.Point toWorldPoint(int screenX, int screenY) {
		return toWorldPointWithZoom(screenX, screenY, viewZoom);
	}
	
	private int worldToScreenX(int worldX) {
		return world.getGlobalX() + Math.round((worldX - world.camera.x) * viewZoom);
	}
	
	private int worldToScreenY(int worldY) {
		return world.getGlobalY() + Math.round((worldY - world.camera.y) * viewZoom);
	}
	
	private Hitbox worldToScreenHitbox(Hitbox worldBox) {
		if (worldBox == null) {
			return null;
		}
		int scaledWidth = Math.max(1, Math.round(worldBox.width * viewZoom));
		int scaledHeight = Math.max(1, Math.round(worldBox.height * viewZoom));
		return new Hitbox(worldToScreenX(worldBox.x), worldToScreenY(worldBox.y), scaledWidth, scaledHeight);
	}
	
	private Node findNodeAtScreenPoint(int screenX, int screenY) {
		if (!isInsideWorldViewport(screenX, screenY)) {
			return null;
		}
		java.awt.Point worldPos = toWorldPoint(screenX, screenY);
		return findNodeAtWorldPoint(worldPos.x, worldPos.y);
	}
	
	private Node findNodeAtWorldPoint(int worldX, int worldY) {
		List<Node> nodes = world.getAllNodes();
		for (int i = nodes.size() - 1; i >= 0; i--) {
			Node node = nodes.get(i);
			if (node == null || node == world.root) {
				continue;
			}
			Hitbox box = node.getGlobalAABB();
			if (box != null && box.contains(worldX, worldY)) {
				return node;
			}
		}
		return null;
	}
	
	private Hitbox getMoveXHandleWorld() {
		if (selectedNode == null) {
			return null;
		}
		Hitbox box = selectedNode.getGlobalAABB();
		if (box == null) {
			return null;
		}
		int centerY = box.y + box.height / 2;
		return new Hitbox(box.x + box.width + 8, centerY - 4, 12, 8);
	}
	
	private Hitbox getMoveYHandleWorld() {
		if (selectedNode == null) {
			return null;
		}
		Hitbox box = selectedNode.getGlobalAABB();
		if (box == null) {
			return null;
		}
		int centerX = box.x + box.width / 2;
		return new Hitbox(centerX - 4, box.y - 16, 8, 12);
	}
	
	private Hitbox getResizeHandleWorld() {
		if (selectedNode == null) {
			return null;
		}
		Hitbox box = selectedNode.getGlobalAABB();
		if (box == null) {
			return null;
		}
		return new Hitbox(box.x + box.width - 4, box.y + box.height - 4, 8, 8);
	}
	
	private void beginGameManipulation(int screenX, int screenY) {
		pressedGameNode = findNodeAtScreenPoint(screenX, screenY);
		if (!isInsideWorldViewport(screenX, screenY)) {
			return;
		}
		
		java.awt.Point worldPoint = toWorldPoint(screenX, screenY);
		gamePressWorldX = worldPoint.x;
		gamePressWorldY = worldPoint.y;
		
		if (activeTool == TOOL_MOVE && selectedNode != null && selectedNode.hasAABB()) {
			int axis = detectMoveGizmoAxis(worldPoint.x, worldPoint.y);
			if (axis == AXIS_X) {
				draggingXAxis = true;
				activeGameNode = selectedNode;
				gameNodeStartX = selectedNode.x;
				return;
			}
			if (axis == AXIS_Y) {
				draggingYAxis = true;
				activeGameNode = selectedNode;
				gameNodeStartY = selectedNode.y;
				return;
			}
		}
		
		if (activeTool == TOOL_RESIZE && selectedNode != null && selectedNode.hasAABB()) {
			Hitbox resizeHandle = getResizeHandleWorld();
			if (resizeHandle != null && resizeHandle.contains(worldPoint.x, worldPoint.y)) {
				draggingResize = true;
				activeGameNode = selectedNode;
				gameStartAABBWidth = selectedNode.getAABBWidth();
				gameStartAABBHeight = selectedNode.getAABBHeight();
				return;
			}
		}
		
		if (activeTool == TOOL_MOVE && pressedGameNode != null) {
			setSelectedNode(pressedGameNode);
			activeGameNode = selectedNode;
			if (activeGameNode != null) {
				draggingGameNode = true;
				gameNodeStartX = activeGameNode.x;
				gameNodeStartY = activeGameNode.y;
			}
		}
	}
	
	private void handleGameManipulation(int screenX, int screenY) {
		if (!(draggingGameNode || draggingXAxis || draggingYAxis || draggingResize) || world == null) {
			return;
		}
		
		java.awt.Point worldPoint = toWorldPoint(screenX, screenY);
		int deltaX = worldPoint.x - gamePressWorldX;
		int deltaY = worldPoint.y - gamePressWorldY;
		
		if (activeGameNode != null) {
			if (draggingGameNode) {
				activeGameNode.x = gameNodeStartX + deltaX;
				activeGameNode.y = gameNodeStartY + deltaY;
			}
			if (draggingXAxis) {
				activeGameNode.x = gameNodeStartX + deltaX;
			}
			if (draggingYAxis) {
				activeGameNode.y = gameNodeStartY + deltaY;
			}
			if (draggingResize && activeGameNode.hasAABB()) {
				activeGameNode.setAABBSize(gameStartAABBWidth + deltaX, gameStartAABBHeight + deltaY);
			}
		}
		
		rebuildProperties();
	}
	
	private void finishGameManipulation(int screenX, int screenY) {
		if (draggingGameNode || draggingXAxis || draggingYAxis || draggingResize) {
			suppressNextClick = true;
			rebuildExplorer();
			rebuildProperties();
		}
		resetGameManipulation();
	}
	
	void resetGameManipulation() {
		pressedGameNode = null;
		activeGameNode = null;
		draggingGameNode = false;
		draggingXAxis = false;
		draggingYAxis = false;
		draggingResize = false;
	}
	
	private void handleGameClick(int screenX, int screenY, int button) {
		if (button != 0 || !isInsideWorldViewport(screenX, screenY)) {
			return;
		}
		
		java.awt.Point worldPoint = toWorldPoint(screenX, screenY);
		if (activeTool == TOOL_MOVE && detectMoveGizmoAxis(worldPoint.x, worldPoint.y) != AXIS_NONE) {
			return;
		}
		if (activeTool == TOOL_RESIZE) {
			Hitbox resizeHandle = getResizeHandleWorld();
			if (resizeHandle != null && resizeHandle.contains(worldPoint.x, worldPoint.y)) {
				return;
			}
		}
		
		Node hitNode = findNodeAtScreenPoint(screenX, screenY);
		if (hitNode != null) {
			setSelectedNode(hitNode);
		}
	}
	
	private int detectMoveGizmoAxis(int worldX, int worldY) {
		if (selectedNode == null || !selectedNode.hasAABB()) {
			return AXIS_NONE;
		}
		
		Hitbox box = selectedNode.getGlobalAABB();
		Hitbox xHandle = getMoveXHandleWorld();
		Hitbox yHandle = getMoveYHandleWorld();
		if (box == null || xHandle == null || yHandle == null) {
			return AXIS_NONE;
		}
		
		if (xHandle.contains(worldX, worldY)) {
			return AXIS_X;
		}
		if (yHandle.contains(worldX, worldY)) {
			return AXIS_Y;
		}
		
		int centerX = box.x + box.width / 2;
		int centerY = box.y + box.height / 2;
		Hitbox xLine = new Hitbox(box.x + box.width, centerY - 3, Math.max(1, (xHandle.x + xHandle.width) - (box.x + box.width)), 6);
		Hitbox yLine = new Hitbox(centerX - 3, yHandle.y, 6, Math.max(1, (box.y - yHandle.y)));
		
		boolean onX = xLine.contains(worldX, worldY);
		boolean onY = yLine.contains(worldX, worldY);
		if (onX && onY) {
			int dx = Math.abs(worldX - centerX);
			int dy = Math.abs(worldY - centerY);
			return dx >= dy ? AXIS_X : AXIS_Y;
		}
		if (onX) {
			return AXIS_X;
		}
		if (onY) {
			return AXIS_Y;
		}
		
		return AXIS_NONE;
	}

	private void rebuildExplorer() {
		if (explorerContent == null || explorerScrollPanel == null || world == null) {
			return;
		}

		explorerContent.clear();
		int[] y = new int[] { 0 };
		addExplorerNode(world.root, 0, y);
		explorerContent.setSize(Math.max(1, explorerScrollPanel.getWidth() - 18), Math.max(y[0], 1));
		explorerScrollPanel.updateBars();
	}

	private void addExplorerNode(Node node, int depth, int[] y) {
		int indent = 6 + (depth * 12);
		StudioExplorerNodeButton button = new StudioExplorerNodeButton(
			node,
			indent,
			y[0],
			Math.max(32, explorerScrollPanel.getWidth() - 30 - indent),
			ROW_HEIGHT,
			this::setSelectedNode,
			this::showContextMenu,
			() -> selectedNode,
			() -> draggedNode,
			() -> dropTargetNode,
			() -> dropMode,
			DROP_INTO,
			DROP_BEFORE,
			DROP_AFTER
		).isPressed(() -> {
			if (selectedNode == node) {
				deleteNode(node);
				rebuildExplorer();
			}
		}, () -> {
			if (selectedNode == node) {
				showRenamePanel(node, this.gamePanel.x + 5, this.gamePanel.y + 5);
				rebuildExplorer();
			}
		});
		explorerContent.add(button);
		y[0] += ROW_HEIGHT;

		for (Node child : node.getChildren()) {
			addExplorerNode(child, depth + 1, y);
		}
	}
	
	void rebuildAnything(World oldWorld) {
		this.gamePanel.remove(oldWorld);
		
		rebuildExplorer();
		rebuildProperties();
		
		resetGameManipulation();
		
		this.gamePanel.add(world);
	}

	private void rebuildProperties() {
		if (propertiesContent == null || propertiesScrollPanel == null) {
			return;
		}

		propertiesContent.clear();

		if (selectedNode == null) {
			propertiesContent.add(new Text("No selection", 6, 6, 0xff9aa4b2));
			propertiesContent.setSize(Math.max(1, propertiesScrollPanel.getWidth() - 18), 28);
			propertiesScrollPanel.updateBars();
			return;
		}

		int contentWidth = Math.max(80, propertiesScrollPanel.getWidth() - 26);
		int y = 6;
		propertiesContent.add(new Text(selectedNode.getName(), 6, y, 0xffffffff));
		y += 14;
		propertiesContent.add(new Text(selectedNode.getClass().getSimpleName(), 6, y, 0xff9aa4b2));
		y += 16;

		List<SettingCategory> categories = selectedNode.getNodeSettings();
		for (SettingCategory category : categories) {
			StudioCategoryHeader header = new StudioCategoryHeader(category.getKey(), 4, y, contentWidth, 18);
			propertiesContent.add(header);
			y += 22;

			for (Setting<?> setting : category.getSettings()) {
				propertiesContent.add(new Text(setting.getName(), 8, y + 4, 0xffd7dce5));
				y += 14;
				
				GuiComponent input = createSettingComponent(setting, 8, y, contentWidth - 8);
				if (input != null) {
					propertiesContent.add(input);
					y += input.getHeight() + CONTENT_GAP;
				} else {
					propertiesContent.add(new Text(String.valueOf(setting.getValue()), 8, y + 2, 0xffb7c1cf));
					y += 14;
				}
			}

			y += 8;
		}

		propertiesContent.setSize(Math.max(1, contentWidth), Math.max(y, 1));
		propertiesScrollPanel.updateBars();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private GuiComponent createSettingComponent(Setting<?> setting, int x, int y, int width) {
		if (setting.getType() == EnumSettingType.CHECK_BOX && setting.getValue() instanceof Boolean boolValue) {
			CheckBox checkBox = new CheckBox("", x, y).setChecked(boolValue).setConsumer(cb -> {
				((Setting) setting).setValue(cb.isChecked());
				rebuildExplorer();
				rebuildProperties();
			});
			return checkBox;
		}

		if (setting.getType() == EnumSettingType.TEXT_FIELD) {
			TextField field = new TextField(String.valueOf(setting.getValue()), x, y, Math.max(40, width), 18);
			if (Number.class.isAssignableFrom(setting.getTypeClass())) {
				field.setNumberic();
			}
			field.setTextChanged(value -> {
				applySettingValue(setting, value);
				
				rebuildExplorer();
			});
			return field;
		}
		
		if (setting.getType() == EnumSettingType.COLOR_PICKER) {
			Button colorSelectButton = new Button("", x, y, 20, 20).setButtonColor(
					(int) setting.getValue()
			).setRunnable(e -> {
				ColorPickWindow window = new ColorPickWindow(0, 0).setSelectedColor((int) setting.getValue()).setColorPickFuture(new ColorPickFuture() {
					@Override
					public void cancelled() {
					}

					@Override
					public void succesed(int color) {
						applySettingValue(setting, String.valueOf(color));
						e.setButtonColor(color);
					}

					@Override
					public void closed() {				
					}
				});
				engine.wManager.addWindow(window, true);
			});
			
			return colorSelectButton;
		}
		
		if (setting.getType() == EnumSettingType.RESOURCE_SELECT) {
			int panelWidth = Math.max(40, width);
			Panel container = new Panel(x, y, panelWidth, 18);
			TextField field = new TextField(String.valueOf(setting.getValue()), 0, 0, Math.max(22, panelWidth - 22), 18);
			if (Number.class.isAssignableFrom(setting.getTypeClass())) {
				field.setNumberic();
			}
			field.setTextChanged(value -> {
				applySettingValue(setting, value);
				
				rebuildExplorer();
			});
			
			Button pickButton = new Button("...", panelWidth - 20, 0, 20, 18).setRunnable(() -> {
				engine.wManager.addWindow(new StudioResourceSelectWindow(world, key -> {
					field.setText(key);
				}), true);
			});
			
			container.add(field);
			container.add(pickButton);
			return container;
		}

		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void applySettingValue(Setting<?> setting, String value) {
		try {
			Class<?> clazz = setting.getTypeClass();
			if (clazz == String.class) {
				((Setting) setting).setValue(value);
			} else if (clazz == Character.class) {
				if (!value.isBlank()) {
					((Setting) setting).setValue(value.charAt(0));
				}
			} else if (clazz == Integer.class) {
				if (!value.isBlank()) {
					((Setting) setting).setValue(Integer.parseInt(value));
				}
			} else if (clazz == Byte.class) {
				if (!value.isBlank()) {
					((Setting) setting).setValue(Byte.parseByte(value));
				}
			} else if (clazz == Short.class) {
				if (!value.isBlank()) {
					((Setting) setting).setValue(Short.parseShort(value));
				}
			} else if (clazz == Long.class) {
				if (!value.isBlank()) {
					((Setting) setting).setValue(Long.parseLong(value));
				}
			} else if (clazz == Float.class) {
				if (!value.isBlank()) {
					((Setting) setting).setValue(Float.parseFloat(value));
				}
			} else if (clazz == Double.class) {
				if (!value.isBlank()) {
					((Setting) setting).setValue(Double.parseDouble(value));
				}
			}
		} catch (NumberFormatException ignored) {
		}
	}

	World createDefaultWorld() {
		ArrayBitmap icon = (ArrayBitmap) ResourceLocator.getResource("bgd-tiles");
		World studioWorld = new World("MyNewGame", 1, 1);
		
		// Add Default Elements
		studioWorld.addResource("missingTexture", IOResource.missingTexture);
		studioWorld.addResource("templateSign", IOResource.loadResource(StudioScreen.class.getResourceAsStream("/templateWorld/templateSign.png"), EnumResource.IMAGE));
		studioWorld.addResource("sky", IOResource.loadResource(StudioScreen.class.getResourceAsStream("/sky.png"), EnumResource.IMAGE));
		studioWorld.addResource("grassTexture", icon.getBitmap(0, 0));

		Sky sky = new Sky(0xfffffff);
		sky.setTexture("sky");
		studioWorld.addNode(sky);

		return studioWorld;
	}

}
