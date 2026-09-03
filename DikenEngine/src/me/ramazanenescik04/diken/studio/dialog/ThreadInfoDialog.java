package me.ramazanenescik04.diken.studio.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class ThreadInfoDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JTree tree;
	private Timer timer;
	private JTextArea infoArea;

	// Thread durum takibi ve renklendirme için haritalar
	private final Map<Long, ThreadStatus> previousThreads = new HashMap<>();
	private final Map<Long, HighlightInfo> highlightMap = new HashMap<>();

	// Renklerin ekranda kalma süresi (ms)
	private static final long HIGHLIGHT_DURATION_MS = 2000;

	private enum StatusType { NEW, DEAD }

	private record HighlightInfo(StatusType type, long timestamp) {}
	private record ThreadStatus(long id, String name, ThreadGroup group) {}

	public ThreadInfoDialog(Frame owner) {
		super(owner);
		setTitle("Thread List");
		setBounds(100, 100, 420, 520);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				timer.stop();
			}
		});
		
		setLayout(new BorderLayout());
		
		tree = new JTree();
		tree.setCellRenderer(new CellRenderer(highlightMap));
		tree.setRootVisible(false);
		
		JScrollPane treeScrollPane = new JScrollPane(tree);

		JPanel infoPanel = new JPanel(new BorderLayout());
		infoPanel.setBorder(BorderFactory.createTitledBorder("Thread Info"));
		
		infoArea = new JTextArea();
		infoArea.setEditable(false);
		infoArea.setLineWrap(true);
		infoArea.setWrapStyleWord(true);
		
		JScrollPane infoScrollPane = new JScrollPane(infoArea);
		infoPanel.add(infoScrollPane, BorderLayout.CENTER);
		infoPanel.setPreferredSize(new Dimension(350, 140));

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treeScrollPane, infoPanel);
		splitPane.setResizeWeight(0.7);
		add(splitPane, BorderLayout.CENTER);

		tree.addTreeSelectionListener(_ -> updateInfoPanel());

		updateThreads();

		timer = new Timer(500, this);
		timer.setActionCommand("update");
		timer.start();
	}
	
	public void updateThreads() {
	    long selectedThreadId = -1;
	    String selectedGroupName = null;
	    
	    TreePath currentSelection = tree.getSelectionPath();
	    if (currentSelection != null) {
	        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) currentSelection.getLastPathComponent();
	        Object userObj = selectedNode.getUserObject();
	        if (userObj instanceof Thread thread) {
	            selectedThreadId = thread.threadId();
	        } else if (userObj instanceof ThreadGroup group) {
	            selectedGroupName = group.getName();
	        }
	    }
	    
	    Set<Thread> currentThreads = Thread.getAllStackTraces().keySet();
	    long now = System.currentTimeMillis();

	    // 1. Süresi dolan renklendirmeleri temizle
	    highlightMap.entrySet().removeIf(entry -> now - entry.getValue().timestamp() > HIGHLIGHT_DURATION_MS);

	    Map<Long, ThreadStatus> currentMap = new HashMap<>();
	    for (Thread t : currentThreads) {
	        // Name boş ise fallback isim üret
	        String tName = t.getName();
	        if (tName == null || tName.isBlank()) {
	            tName = (t.isVirtual() ? "VirtualThread-" : "Thread-") + t.threadId();
	        }
	        currentMap.put(t.threadId(), new ThreadStatus(t.threadId(), tName, t.getThreadGroup()));
	    }

	    // 2. Yeni ve Ölen Thread'leri tespit et
	    if (!previousThreads.isEmpty()) {
	        for (Long id : currentMap.keySet()) {
	            if (!previousThreads.containsKey(id)) {
	                highlightMap.put(id, new HighlightInfo(StatusType.NEW, now));
	            }
	        }

	        for (Map.Entry<Long, ThreadStatus> entry : previousThreads.entrySet()) {
	            if (!currentMap.containsKey(entry.getKey())) {
	                highlightMap.put(entry.getKey(), new HighlightInfo(StatusType.DEAD, now));
	            }
	        }
	    }

	    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Root");
	    Map<ThreadGroup, DefaultMutableTreeNode> groupNodeMap = new HashMap<>();
	    DefaultMutableTreeNode nodeToSelect = null;

	    // 3. Aktif Thread'leri Ağaca Ekle
	    for (Thread thread : currentThreads) {
	        ThreadGroup group = thread.getThreadGroup();
	        DefaultMutableTreeNode groupNode = getOrCreateGroupNode(rootNode, group, groupNodeMap);
	        
	        DefaultMutableTreeNode threadNode = new DefaultMutableTreeNode(thread);
	        groupNode.add(threadNode);
	        
	        if (selectedThreadId != -1 && thread.threadId() == selectedThreadId) {
	            nodeToSelect = threadNode;
	        } else if (selectedGroupName != null && group != null && selectedGroupName.equals(group.getName())) {
	            if (nodeToSelect == null) {
	                nodeToSelect = groupNode;
	            }
	        }
	    }

	    // 4. Ölen Thread'leri İsim Bilgisiyle Ekle
	    for (Map.Entry<Long, HighlightInfo> entry : highlightMap.entrySet()) {
	        if (entry.getValue().type() == StatusType.DEAD) {
	            long deadId = entry.getKey();
	            ThreadStatus oldStatus = previousThreads.get(deadId);
	            String deadName = (oldStatus != null && oldStatus.name() != null && !oldStatus.name().isBlank()) 
	                              ? oldStatus.name() : "Thread-" + deadId;

	            DefaultMutableTreeNode deadNode = new DefaultMutableTreeNode("DeadThread:" + deadId + ":" + deadName);
	            
	            ThreadGroup oldGroup = (oldStatus != null) ? oldStatus.group() : null;
	            DefaultMutableTreeNode targetGroupNode = getOrCreateGroupNode(rootNode, oldGroup, groupNodeMap);
	            targetGroupNode.add(deadNode);
	        }
	    }

	    // DÜZELTME: previousThreads haritasını en son güncelliyoruz
	    previousThreads.clear();
	    previousThreads.putAll(currentMap);
	    
	    tree.setModel(new DefaultTreeModel(rootNode));
	    
	    for (int i = 0; i < tree.getRowCount(); i++) {
	        tree.expandRow(i);
	    }
	    
	    if (nodeToSelect != null) {
	        tree.setSelectionPath(new TreePath(nodeToSelect.getPath()));
	    }
	    
	    updateInfoPanel();
	}

	private DefaultMutableTreeNode getOrCreateGroupNode(DefaultMutableTreeNode rootNode, ThreadGroup group, Map<ThreadGroup, DefaultMutableTreeNode> groupNodeMap) {
		if (group == null) {
			ThreadGroup nullGroup = new ThreadGroup("Other / Unassigned");
			return groupNodeMap.computeIfAbsent(nullGroup, g -> {
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(g);
				rootNode.add(node);
				return node;
			});
		}

		if (groupNodeMap.containsKey(group)) {
			return groupNodeMap.get(group);
		}

		DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group);
		groupNodeMap.put(group, groupNode);

		ThreadGroup parentGroup = group.getParent();
		if (parentGroup != null) {
			DefaultMutableTreeNode parentNode = getOrCreateGroupNode(rootNode, parentGroup, groupNodeMap);
			parentNode.add(groupNode);
		} else {
			rootNode.add(groupNode);
		}

		return groupNode;
	}

	@SuppressWarnings("removal")
	private void updateInfoPanel() {
		TreePath selection = tree.getSelectionPath();
		if (selection != null) {
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selection.getLastPathComponent();
			Object userObj = selectedNode.getUserObject();
			
			if (userObj instanceof Thread thread) {
				StringBuilder sb = new StringBuilder();
				sb.append("TYPE: Thread\n");
				sb.append("Name: ").append(thread.getName()).append("\n");
				sb.append("ID: ").append(thread.threadId()).append("\n");
				sb.append("State: ").append(thread.getState()).append("\n");
				sb.append("Priority: ").append(thread.getPriority()).append("\n");
				sb.append("Thread Group: ").append(thread.getThreadGroup() != null ? thread.getThreadGroup().getName() : "Yok").append("\n");
				sb.append("is Daemon: ").append(thread.isDaemon() ? "Evet" : "Hayır").append("\n");
				sb.append("is Alive?: ").append(thread.isAlive() ? "Evet" : "Hayır");
				
				infoArea.setText(sb.toString());
				infoArea.setCaretPosition(0);
				return;
			} else if (userObj instanceof ThreadGroup group) {
				StringBuilder sb = new StringBuilder();
				sb.append("TYPE: ThreadGroup\n");
				sb.append("Group Name: ").append(group.getName()).append("\n");
				sb.append("Max Priority: ").append(group.getMaxPriority()).append("\n");
				sb.append("Active Thread Count: ").append(group.activeCount()).append("\n");
				sb.append("Active Group Count: ").append(group.activeGroupCount()).append("\n");
				sb.append("is Daemon: ").append(group.isDaemon() ? "Evet" : "Hayır");
				
				infoArea.setText(sb.toString());
				infoArea.setCaretPosition(0);
				return;
			}
		}
		infoArea.setText("Select a Thread or ThreadGroup to view its details.");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("update".equals(e.getActionCommand())) {
			updateThreads();
		}
	}
	
	private static class CellRenderer extends DefaultTreeCellRenderer {
	    private static final long serialVersionUID = 1L;
	    private final Map<Long, HighlightInfo> highlightMap;
	    private Bitmap icon;

	    public CellRenderer(Map<Long, HighlightInfo> highlightMap) {
	        this.highlightMap = highlightMap;
	    }
	    
	    @Override
	    public Component getTreeCellRendererComponent(JTree tree, Object value,
	                                                  boolean sel, boolean expanded,
	                                                  boolean leaf, int row, boolean hasFocus) {
	        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

	        // Arka plan renginin görünebilmesi için mat (opaque) yapıyoruz
	        setOpaque(true);

	        if (value instanceof DefaultMutableTreeNode node) {
	            Object userObject = node.getUserObject();
	            
	            if (userObject instanceof Thread data) {
	                String text = data.getName();
	                if (data == Thread.currentThread()) {
	                    text += " [Main Thread]";
	                }
	                setText(text);
	                setIcon(scaleIcon(14));

	                // Renklendirme kontrolü (Arka plan rengi)
	                HighlightInfo info = highlightMap.get(data.threadId());
	                if (info != null && info.type() == StatusType.NEW) {
	                    setBackground(new Color(200, 255, 200)); // Hafif Açık Yeşil
	                    if (!sel) setForeground(Color.BLACK);
	                } else if (!sel) {
	                    setBackground(tree.getBackground());
	                   //setForeground(Color.BLACK);
	                }
	            } 
	            else if (userObject instanceof String str && str.startsWith("DeadThread:")) {
	                long id = Long.parseLong(str.split(":")[1]);
	                setText("Thread-" + id + " [Terminated]");
	                setBackground(new Color(255, 200, 200)); // Hafif Açık Kırmızı
	                if (!sel) setForeground(Color.BLACK);
	                setIcon(scaleIcon(14));
	            }
	            else if (userObject instanceof ThreadGroup group) {
	                setText("[" + group.getName() + "]");
	                if (!sel) {
	                    setBackground(tree.getBackground());
	                    //setForeground(Color.BLACK);
	                }
	            }
	        }
	        
	        // Eğer satır seçiliyse Swing varsayılan seçim rengini korusun
	        if (sel) {
	        	setOpaque(false);
	            setBackground(getBackgroundSelectionColor());
	            setForeground(getTextSelectionColor());
	        }

	        return this;
	    }
	    
	    private ImageIcon scaleIcon(int size) {
	        icon = ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(6, 3);
	        	 
	        Image scaled = icon.toImage()
	                .getScaledInstance(size, size, Image.SCALE_SMOOTH);

	        return new ImageIcon(scaled);
	    }
	}
}