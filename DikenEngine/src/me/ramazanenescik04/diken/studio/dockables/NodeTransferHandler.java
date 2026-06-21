package me.ramazanenescik04.diken.studio.dockables;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import me.ramazanenescik04.diken.game.Node;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.List;

class NodeTransferHandler extends TransferHandler {
    private static final long serialVersionUID = 5430224853184022373L;

    private List<DefaultMutableTreeNode> draggedNodes = new ArrayList<>();
	private ExplorerPanel explorer;
	
	NodeTransferHandler (ExplorerPanel e) {
		this.explorer = e;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
	    JTree tree = (JTree) c;
	    TreePath[] paths = tree.getSelectionPaths();
	    if (paths == null || paths.length == 0) return null;

	    draggedNodes = new ArrayList<>();
	    for (TreePath path : paths) {
	        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
	        if (node.isRoot()) continue;
	        draggedNodes.add(node);
	    }

	    // Ancestor-descendant çakışmasını temizle
	    // (hem parent hem child seçiliyse child'ı listeden çıkar, parent zaten taşıyacak)
	    draggedNodes.removeIf(node ->
	        draggedNodes.stream().anyMatch(other -> other != node && node.isNodeDescendant(other))
	    );

	    if (draggedNodes.isEmpty()) return null;
	    return new NodesTransferable();
	}

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE; 
    }

    @Override
    public boolean importData(TransferSupport support) {
    	if (!support.isDrop() || draggedNodes.isEmpty()) return false;
        if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) return false;

        JTree tree = (JTree) support.getComponent();
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        TreePath destPath = dl.getPath();

        DefaultMutableTreeNode newParentTreeNode;
        if (destPath == null) {
            newParentTreeNode = (DefaultMutableTreeNode)
                ((DefaultTreeModel) tree.getModel()).getRoot();
        } else {
            newParentTreeNode = (DefaultMutableTreeNode) destPath.getLastPathComponent();
        }

        try {
            if (!(newParentTreeNode.getUserObject() instanceof me.ramazanenescik04.diken.game.Node yeniParentOyunNode)) {
                return false;
            }

            for (DefaultMutableTreeNode draggedNode : draggedNodes) {
                if (draggedNode.getUserObject() instanceof me.ramazanenescik04.diken.game.Node oyunNode) {
                    explorer.moveNode(oyunNode, yeniParentOyunNode);
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            draggedNodes.clear();
        }
        return false;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if (!support.isDrop() || draggedNodes.isEmpty()) return false;
        if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) return false;

        support.setShowDropLocation(true);
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        TreePath destPath = dl.getPath();

        DefaultMutableTreeNode targetNode;
        if (destPath == null) {
            return false; // Root'a sürüklemeyi engelle
        } else {
            targetNode = (DefaultMutableTreeNode) destPath.getLastPathComponent();
        }
        
        // Root node'una sürüklemeyi engelle
        if (targetNode.isRoot()) return false;
        
        // Servis olan node'lara direkt sürüklemeyi engelle
        // (servisin çocuğuna değil, servisin kendisine drop)
        if (targetNode.getUserObject() instanceof Node gameNode) {
            if (explorer.isService(gameNode) && dl.getChildIndex() == -1) {
                // Servisin içine bırakılıyorsa izin ver, 
                // ama servisler arasına (root seviyesine) bırakılıyorsa engelle
            }
        }

        for (DefaultMutableTreeNode dragged : draggedNodes) {
            if (dragged == targetNode || targetNode.isNodeAncestor(dragged)) {
                return false;
            }
            // Servisleri taşımayı engelle
            if (dragged.getUserObject() instanceof Node gameNode) {
                if (explorer.isService(gameNode)) return false;
            }
        }

        return true;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        // Boş bırak
    }

    private static class NodesTransferable implements Transferable {
        private static final DataFlavor[] FLAVORS = { DataFlavor.stringFlavor };

        @Override
        public DataFlavor[] getTransferDataFlavors() { return FLAVORS; }
        
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) { 
            return DataFlavor.stringFlavor.equals(flavor); 
        }
        
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
            return "diken-node-transfer"; // dummy string, önemli değil
        }
    }
}