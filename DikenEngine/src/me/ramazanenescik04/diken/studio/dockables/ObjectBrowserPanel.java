package me.ramazanenescik04.diken.studio.dockables;

import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.reflect.*;
import java.util.*;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;

import me.ramazanenescik04.diken.game.InstanceList;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.event.Event;
import me.ramazanenescik04.diken.game.nodes.Camera;
import me.ramazanenescik04.diken.game.services.*;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.scripting.LuaDoc;
import me.ramazanenescik04.diken.scripting.LuaInit;

public class ObjectBrowserPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final JList<Class<?>> classList;
    private final JList<Object> memberList; // Method veya Field
    private final JTextPane docPane;
    private boolean showUnlisted = true;

    public ObjectBrowserPanel() {
        setLayout(new BorderLayout());

        // Sol: Class listesi
        classList = new JList<>();
        classList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        classList.setCellRenderer(new DefaultListCellRenderer() {
            @SuppressWarnings("unchecked")
			@Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean hasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                Class<?> clazz = (Class<?>) value;
                setText(clazz.getSimpleName());
                
                if (clazz.isEnum()) {
                    setIcon(getEditorIcon(0, 2));
                } else if (Node.class.isAssignableFrom(clazz)) {
                	Node node = null;
                    try {
						node = (Node) clazz.getDeclaredConstructor().newInstance();
					} catch (Exception e) {
						node = InstanceList.getRegisteredNode((Class<? extends Node>) clazz);
					} finally {
						if (node == null) {
							setIcon(getEditorIcon(12, 3));
						} else {
							setIcon(new ImageIcon(node.getNodeSettings().getLast().getKey().getImage().toImage()));
						}
					}
                } else {
                	setIcon(getEditorIcon(8, 1));
                }
                return this;
            }
        });

        // Sağ üst: Member listesi
        memberList = new JList<>();
        memberList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean hasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                
                if (value instanceof Method m) {
                    setText(m.getName());
                    setIcon(getEditorIcon(9, 3));
                } else if (value instanceof Field f) {
                    if (f.getType() == Event.class) {
                        setText(f.getName());
                        setIcon(getEditorIcon(8, 2));
                    } else {
                        setText(f.getName());
                        setIcon(getEditorIcon(8, 3));
                    }
                } else if (value instanceof Enum<?> e) {
                	setText(e.name());
                	setIcon(getEditorIcon(0, 2));
                }
                
                return this;
            }
        });

        // Sağ alt: Dokümantasyon
        docPane = new JTextPane();
        docPane.setEditable(false);
        docPane.setContentType("text/html");

        // Sağ taraf: üst member listesi, alt doc
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(memberList),
                new JScrollPane(docPane));
        rightSplit.setResizeWeight(0.4);

        // Ana split: sol class listesi, sağ panel
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(classList),
                rightSplit);
        mainSplit.setResizeWeight(0.25);

        add(mainSplit, BorderLayout.CENTER);

        // Event'ler
        classList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onClassSelected();
        });
        memberList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onMemberSelected();
        });

        // Class listesini doldur
        loadClasses();
    }

    public boolean isShowUnlisted() {
		return showUnlisted;
	}

	public void setShowUnlisted(boolean showUnlisted) {
		this.showUnlisted = showUnlisted;
		onClassSelected();
	}

	private void loadClasses() {
        List<Class<?>> classes = findNodeSubclasses();
        classList.setListData(classes.toArray(new Class[0]));
    }

    private List<Class<?>> findNodeSubclasses() {
        List<Class<?>> result = new ArrayList<>();
        result.addAll(InstanceList.getNodeClassList());
        result.add(Camera.class);
        result.add(Game.class);
        result.add(Workspace.class);
        result.add(PlayerService.class);
        result.add(Lighting.class);
        result.add(UIService.class);
        result.add(InputService.class);
        result.add(RunService.class);
        result.add(CoreUIService.class);
        
        // Enumlar ve Yardımcı Sınıflar
        result.addAll(LuaInit.initClasses().values());
        result.addAll(LuaInit.initEnums().values());
        
        return result;
    }

    private void onClassSelected() {
        Class<?> clazz = classList.getSelectedValue();
        if (clazz == null) return;

        List<Object> members = new ArrayList<>();

        if (clazz.isEnum()) {
            for (Object constant : clazz.getEnumConstants()) {
                members.add(constant);
            }
        } else {
            for (Field f : clazz.getFields()) {
                if (f.isAnnotationPresent(LuaDoc.class) || f.getType() == Event.class || showUnlisted) {
                    members.add(f);
                }
            }
            for (Method m : clazz.getMethods()) {
                if (m.isAnnotationPresent(LuaDoc.class) || showUnlisted) {
                    members.add(m);
                }
            }
        }

        memberList.setListData(members.toArray());
        docPane.setText("");
    }

    private void onMemberSelected() {
        Object selected = memberList.getSelectedValue();
        if (selected == null) return;

        StringBuilder html = new StringBuilder("<html><body style='font-family:monospace; padding:8px'>");

        if (selected instanceof Method m) {
            LuaDoc doc = m.getAnnotation(LuaDoc.class);

            // İmza
            html.append("<b>").append(m.getReturnType().getSimpleName())
                .append(" ").append(m.getName()).append("(");
            Parameter[] params = m.getParameters();
            for (int i = 0; i < params.length; i++) {
                if (i > 0) html.append(", ");
                html.append(params[i].getType().getSimpleName())
                    .append(" ").append(params[i].getName());
            }
            
            html.append(") : [")
            .append(m.getReturnType().getSimpleName())
            .append("] (")
            .append(m.getDeclaringClass().getSimpleName())
            .append(") ");

            if (doc != null) {
                if (!doc.description().isEmpty())
                    html.append(doc.description()).append("<br><br>");
                if (doc.params().length > 0) {
                    html.append("<b>Parametreler:</b><br>");
                    for (String p : doc.params())
                        html.append("&nbsp;&nbsp;").append(p).append("<br>");
                    html.append("<br>");
                }
                if (!doc.returns().isEmpty())
                    html.append("<b>Döner:</b> ").append(doc.returns()).append("<br><br>");
                if (!doc.example().isEmpty())
                    html.append("<b>Örnek:</b><br><pre>").append(doc.example()).append("</pre>");
            }

        } else if (selected instanceof Field f) {
            LuaDoc doc = f.getAnnotation(LuaDoc.class);
            String kind = f.getType() == Event.class ? "Event" : "Field";

            html.append("<b>[").append(kind).append("] ")
                .append(f.getType().getSimpleName())
                .append(" ").append(f.getName())
                .append(" [")
                .append(f.getDeclaringClass().getSimpleName())
                .append("]")
                .append("</b><br><br>");

            if (doc != null && !doc.description().isEmpty()) {
                html.append(doc.description());
            }
        } else if (selected instanceof Enum<?> e) {
            LuaDoc doc = null;
            try {
                doc = e.getClass().getField(e.name()).getAnnotation(LuaDoc.class);
            } catch (NoSuchFieldException ex) { }

            html.append("<b>").append(e.name()).append("</b><br><br>");
            if (doc != null && !doc.description().isEmpty()) {
                html.append(doc.description());
            }
        }

        html.append("</body></html>");
        docPane.setText(html.toString());
    }
    
    private static ImageIcon getEditorIcon(int x, int y) {
		if (x >= 0 && y >= 0) {
			// load icon
			ImageIcon imageIcon;
			try {
				var image = ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(x, y);
				imageIcon = new ImageIcon(image.toImage());
			} catch (Exception ignore) {
				imageIcon = new ImageIcon(IOResource.missingTexture.toImage());
			} 
			
			return imageIcon;
		}
		
		return null;
	}
}