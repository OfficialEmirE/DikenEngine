package me.ramazanenescik04.diken.studio.dockables;
import javax.swing.BoxLayout;

public class AssistantPanel extends DockablePanel {

	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 */
	public AssistantPanel() {
		super("assistant", "Assistant");
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

}
