package me.ramazanenescik04.diken.studio;

import java.awt.event.KeyEvent;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.Text;
import me.ramazanenescik04.diken.resource.Bitmap;

public class StudioExplorerNodeButton extends Button {
	private static final long serialVersionUID = 1L;

	private final Node node;
	private final Consumer<Node> onSelect;
	private final RightClickHandler onRightClick;
	private final Supplier<Node> selectedNodeSupplier;
	private final Supplier<Node> draggedNodeSupplier;
	private final Supplier<Node> dropTargetSupplier;
	private final IntSupplier dropModeSupplier;
	private final int dropIntoValue;
	private final int dropBeforeValue;
	private final int dropAfterValue;

	public StudioExplorerNodeButton(Node node, int x, int y, int width, int height,
			Consumer<Node> onSelect, RightClickHandler onRightClick,
			Supplier<Node> selectedNodeSupplier, Supplier<Node> draggedNodeSupplier,
			Supplier<Node> dropTargetSupplier, IntSupplier dropModeSupplier,
			int dropIntoValue, int dropBeforeValue, int dropAfterValue) {
		super(node.getName(), x, y, width, height);
		this.node = node;
		this.onSelect = onSelect;
		this.onRightClick = onRightClick;
		this.selectedNodeSupplier = selectedNodeSupplier;
		this.draggedNodeSupplier = draggedNodeSupplier;
		this.dropTargetSupplier = dropTargetSupplier;
		this.dropModeSupplier = dropModeSupplier;
		this.dropIntoValue = dropIntoValue;
		this.dropBeforeValue = dropBeforeValue;
		this.dropAfterValue = dropAfterValue;

		var categories = node.getNodeSettings();
		if (!categories.isEmpty()) {
			setButtonIcon(categories.get(categories.size() - 1).getKey().getImage());
		}
		setRunnable(() -> this.onSelect.accept(this.node));
	}

	public Node getNode() {
		return node;
	}

	@Override
	public void mouseClicked(int x, int y, int button, boolean isTouch) {
		if (button == 2 && isTouch) {
			onRightClick.handle(node, getGlobalX() + x, getGlobalY() + y);
			return;
		}
		super.mouseClicked(x, y, button, isTouch);
	}

	private Runnable delPressed, renamePressed;
	@Override
	public void keyPressed(char var1, int var2) {
		super.keyPressed(var1, var2);
		
		if (var2 == KeyEvent.VK_DELETE) {
			delPressed.run();
		} else if (var2 == KeyEvent.VK_F2) {
			renamePressed.run();
		}
	}
	
	public StudioExplorerNodeButton isPressed(Runnable remove, Runnable rename) {
		delPressed = remove;
		renamePressed = rename;
		return this;
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = new Bitmap(width, height);
		Node selectedNode = selectedNodeSupplier.get();
		Node draggedNode = draggedNodeSupplier.get();
		Node dropTargetNode = dropTargetSupplier.get();
		int dropMode = dropModeSupplier.getAsInt();

		boolean selected = selectedNode == node;
		boolean dragged = draggedNode == node;
		bitmap.fill(0, 0, width, height, selected ? 0xff3d5578 : 0xff232934);
		if (dragged) {
			bitmap.blendFill(0, 0, width, height, 0x7f6b7788);
		}
		if (selected) {
			bitmap.box(0, 0, width - 1, height - 1, 0xff7ca7dc);
		}
		if (dropTargetNode == node) {
			if (dropMode == dropIntoValue) {
				bitmap.box(0, 0, width - 1, height - 1, 0xff6fd38b);
			} else if (dropMode == dropBeforeValue) {
				bitmap.fill(0, 0, width, 2, 0xff6fd38b);
			} else if (dropMode == dropAfterValue) {
				bitmap.fill(0, height - 2, width, height, 0xff6fd38b);
			}
		}

		if (getButtonIcon() != null) {
			bitmap.draw(getButtonIcon(), 2, 1);
		}

		Text.render(text, bitmap, 20, 5, selected ? 0xffffffff : 0xffd7dce5);
		return bitmap;
	}

	@FunctionalInterface
	public interface RightClickHandler {
		void handle(Node node, int screenX, int screenY);
	}
}
