package me.ramazanenescik04.diken.studio.dockables;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.border.LineBorder;

import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.studio.editors.BaseEditor;

public class EditorTabPanel extends DockablePanel {
	private static final long serialVersionUID = -8433100060634996548L;

	private JTabbedPane tabbedPane;
	private JPanel trailing; // tab satırının sağındaki alan
	private List<BaseEditor> openEditors = new ArrayList<>();
	private Map<BaseEditor, JToolBar> toolbars = new HashMap<>();

	public EditorTabPanel() {
		super("editor_tab_id", "studio.windows.editorTab");

		setBorder(new LineBorder(Color.black));
		dock.setTitleShown(false);
		setLayout(new BorderLayout());

		trailing = new JPanel(new BorderLayout());
		trailing.setOpaque(false);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.addChangeListener(_ -> refreshToolbar());

		// null layout: önce eklenen component üstte çizilir ve fareyi önce o alır
		JPanel content = new JPanel(null) {
			private static final long serialVersionUID = 1L;

			@Override
			public void doLayout() {
				int w = getWidth(), h = getHeight();
				tabbedPane.setBounds(0, 0, w, h);

				Dimension d = trailing.getPreferredSize();
				int tabH = tabbedPane.getTabCount() > 0
						? tabbedPane.getBoundsAt(0).height
						: d.height;
				trailing.setBounds(w - d.width, 0, d.width, tabH);
			}

			@Override
			public Dimension getPreferredSize() {
				return tabbedPane.getPreferredSize();
			}
		};
		content.add(trailing);   // önce toolbar (üstte)
		content.add(tabbedPane);
		add(content, BorderLayout.CENTER);
	}

	private void refreshToolbar() {
		trailing.removeAll();

		if (tabbedPane.getSelectedComponent() instanceof BaseEditor editor) {
			JToolBar bar = toolbars.computeIfAbsent(editor, e -> {
				var builder = e.getToolbar();
				if (builder == null) return null;
				JToolBar tb = builder.convert();
				tb.setFloatable(false);
				tb.setOpaque(false);
				tb.setBorder(null);
				return tb;
			});
			if (bar != null) trailing.add(bar, BorderLayout.CENTER);
		}

		trailing.revalidate();
		trailing.getParent().revalidate(); // content paneli yeniden yerleştirsin
		trailing.repaint();
	}

	public void reloadWorld(World world, boolean playtest) {
		setBorder(new LineBorder(playtest ? Color.green : Color.black));
		openEditors.forEach(e -> e.refreshWorld(world, playtest));
	}

	public void openEditor(BaseEditor editor) {
		editor.init(this);
		openEditors.add(editor);

		tabbedPane.addTab(null, editor);
		int index = tabbedPane.indexOfComponent(editor);
		tabbedPane.setTabComponentAt(index, editor.getTabHeader());
		tabbedPane.setSelectedComponent(editor);
	}

	public void removeEditor(BaseEditor baseEditor) {
		baseEditor.closing();
		openEditors.remove(baseEditor);
		toolbars.remove(baseEditor);

		int index = tabbedPane.indexOfComponent(baseEditor);
		if (index >= 0) tabbedPane.removeTabAt(index);
		refreshToolbar();
	}

	public void updateEditor(BaseEditor editor) {
		int index = tabbedPane.indexOfComponent(editor);
		tabbedPane.setTabComponentAt(index, editor.getTabHeader());
		tabbedPane.setSelectedComponent(editor);
	}
}