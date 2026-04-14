package me.ramazanenescik04.diken.studio;

import java.util.function.BiConsumer;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.GuiComponent;
import me.ramazanenescik04.diken.gui.compoment.Panel;
import me.ramazanenescik04.diken.gui.compoment.TextField;
import me.ramazanenescik04.diken.resource.Bitmap;

public class StudioRenamePanel extends Panel {
	private static final long serialVersionUID = 1L;

	private final Node targetNode;
	private final TextField nameField;
	private final BiConsumer<Node, String> onApply;
	private final Runnable onClose;

	public StudioRenamePanel(Node targetNode, int x, int y, int width, int height,
			BiConsumer<Node, String> onApply, Runnable onClose) {
		super(x, y, width, height);
		this.targetNode = targetNode;
		this.onApply = onApply;
		this.onClose = onClose;
		this.nameField = new TextField(targetNode.getName(), 4, 4, width - 8, 18);
	}

	@Override
	public void init(DikenEngine engine) {
		super.init(engine);
		this.nameField.setFocused(true);
		add(nameField);
		add(new Button("OK", 4, 24, 58, 16).setRunnable(() -> {
			String value = nameField.getText().trim();
			if (!value.isEmpty()) {
				onApply.accept(targetNode, value);
			}
			onClose.run();
		}));
		add(new Button("Cancel", 68, 24, 58, 16).setRunnable(onClose));
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
