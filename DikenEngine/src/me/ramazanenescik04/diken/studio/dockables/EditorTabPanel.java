package me.ramazanenescik04.diken.studio.dockables;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.border.LineBorder;

import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.studio.editors.BaseEditor;

public class EditorTabPanel extends DockablePanel {
	private static final long serialVersionUID = -8433100060634996548L;
	
	private JTabbedPane tabbedPane;
	private List<BaseEditor> openEditors = new ArrayList<>();

	public EditorTabPanel() {
		super("editor_tab_id", "studio.windows.editorTab");

		setBorder(new LineBorder(new Color(0, 0, 0)));
		
		dock.setTitleShown(false);
		
		setLayout(new BorderLayout(0, 0));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, BorderLayout.CENTER);
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
		
		int index = tabbedPane.indexOfComponent(baseEditor);
        if (index >= 0) tabbedPane.removeTabAt(index);
	}

	public void updateEditor(BaseEditor editor) {
		int index = tabbedPane.indexOfComponent(editor);
        tabbedPane.setTabComponentAt(index, editor.getTabHeader());
        tabbedPane.setSelectedComponent(editor);
	}
}
