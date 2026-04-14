package me.ramazanenescik04.diken.studio;

import java.util.function.Consumer;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.GuiComponent;
import me.ramazanenescik04.diken.gui.compoment.Panel;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class StudioContextMenuPanel extends Panel {
	private static final long serialVersionUID = 1L;

	private final Node targetNode;

	public StudioContextMenuPanel(Node targetNode, int x, int y, int width, int height,
			Consumer<Node> onRename, Consumer<Node> onDelete,
			Consumer<Node> onAddInstance, boolean canDelete) {
		super(x, y, width, height);
		this.targetNode = targetNode;
		build(onRename, onDelete, onAddInstance, canDelete);
	}

	private void build(Consumer<Node> onRename, Consumer<Node> onDelete,
			Consumer<Node> onAddInstance, boolean canDelete) {
		ArrayBitmap icons = (ArrayBitmap) ResourceLocator.getResource("editor_icons");
		
		add(new Button("Rename", 3, 3, width - 6, 18).setRunnable(() -> onRename.accept(targetNode)).setButtonIcon(icons.getBitmap(1, 0)));

		Button deleteButton = new Button("Delete", 3, 23, width - 6, 18).setRunnable(() -> onDelete.accept(targetNode)).setButtonIcon(icons.getBitmap(0, 0));
		deleteButton.setActive(canDelete);
		add(deleteButton);

		add(new Button("Add Instance", 3, 43, width - 6, 18).setRunnable(() -> onAddInstance.accept(targetNode)).setButtonIcon(icons.getBitmap(2, 0)));
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = new Bitmap(width, height);
		bitmap.fill(0, 0, width, height, 0xff303844);
		bitmap.box(0, 0, width - 1, height - 1, 0xff93a2b7);
		for (GuiComponent component : getCompoments()) {
			if (component != null && component.isVisible()) {
				bitmap.draw(component.render(), component.x, component.y);
			}
		}
		return bitmap;
	}
}
