package me.ramazanenescik04.diken.game;

import java.util.*;
import java.util.stream.Collectors;

import me.ramazanenescik04.diken.game.nodes.*;
import me.ramazanenescik04.diken.game.nodes.values.*;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.gui.component.*;
import me.ramazanenescik04.diken.gui.component.color.*;
import me.ramazanenescik04.diken.scripting.Script;
import me.ramazanenescik04.diken.game.entity.*;
import me.ramazanenescik04.diken.game.event.BindableEvent;

public final class InstanceList {
	private static final ArrayList<Node> NODE_LIST;
	private static final Map<CategoryKey, List<Node>> NODE_TYPES;
	
	private InstanceList() {}
	
	public synchronized static List<Node> getNodeList() {
		return new ArrayList<>(NODE_LIST);
	}
	
	public synchronized static List<Class<?>> getNodeClassList() {
		List<Class<?>> list = new ArrayList<>();
		for (Node node : NODE_LIST) {
			list.add(node.getClass());
		}
		return list;
	}
	
	public synchronized static Map<CategoryKey, List<Node>> getTypedNodes() {
		return NODE_TYPES;
	}
	
	public synchronized static int registeredNodeCount() {
		return NODE_LIST.size();
	}
	
	public synchronized static Node getRegisteredNode(int index) {
		return NODE_LIST.get(index).copy();
	}
	
	public synchronized static Node getRegisteredNode(Class<? extends Node> nodeClass) {
		for (Node node : NODE_LIST) {
			if (node.getClass().isAssignableFrom(nodeClass)) {
				return node.copy();
			}
		}
		return null;
	}
	
	public synchronized static boolean isRegistered(Node node) {
	    if (node == null) return false;
	    for (Node node2 : NODE_LIST) {
	        if (node2.getClass().isAssignableFrom(node.getClass())) { // node'un sınıfını kontrol etmelisin
	            return true;
	        }
	    }
	    return false;
	}
	
	private static CategoryKey getCategoryName(Object obj) {
	    String nPackage = obj.getClass().getPackageName();
	    
	    if (nPackage.contains(".game.nodes.values")) {
	        return new CategoryKey(0, 2, "Değerler");
	    } else if (nPackage.contains(".game.nodes") || nPackage.contains(".game.entity")) {
	        return new CategoryKey(9, 1, "Temel Nesneler");
	    } else if (nPackage.contains(".gui.component") || nPackage.contains(".gui.component.color")) {
	        return new CategoryKey(15, 1, "Grafik Arayüz");
	    } else if (nPackage.contains(".scripting")) {
	        return new CategoryKey(12, 1, "Scriptler");
	    } else if (nPackage.contains(".game.event")) {
	        return new CategoryKey(8, 2, "Eventler");
	    }
	    return new CategoryKey(12, 3, "Diğer");
	}
	
	public static record CategoryKey(int iconX, int iconY, String displayName) {
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            CategoryKey that = (CategoryKey) obj;
            return iconX == (that.iconX) && iconY == that.iconY && displayName.equals(that.displayName);
		}

		@Override
		public int hashCode() {
			return Objects.hash(iconX, iconY, displayName);
		}

		@Override
		public String toString() {
			return "CategoryKey=[" + iconX + "x" + iconY + ", " + displayName + "]";
		}
	}
	
	static {
		NODE_LIST = new ArrayList<>();
		
		// me.ramazanenescik04.diken.game.event
		NODE_LIST.add(new BindableEvent());
		
		// me.ramazanenescik04.diken.game.nodes
		NODE_LIST.add(new SpriteSheet());
		NODE_LIST.add(new Decal());
		NODE_LIST.add(new Folder());
		NODE_LIST.add(new Model());
		NODE_LIST.add(new Part());
		NODE_LIST.add(new Sky());
		NODE_LIST.add(new SpawnLocation());
		NODE_LIST.add(new Texture());
		NODE_LIST.add(new Tool());
		NODE_LIST.add(new Audio());
		NODE_LIST.add(new Light());
		
		// e.ramazanenescik04.diken.game.nodes.values
		NODE_LIST.add(new StringValue());
		NODE_LIST.add(new IntegerValue());
		NODE_LIST.add(new FloatValue());
		NODE_LIST.add(new BooleanValue());
		NODE_LIST.add(new ObjectValue());
		
		// me.ramazanenescik04.diken.gui.component
		NODE_LIST.add(new ScreenGui());
		NODE_LIST.add(new Button("Edit Me!", UDim2.defaultV, UDim2.defaultV));
		NODE_LIST.add(new CheckBox("Edit Me!", UDim2.defaultV, UDim2.defaultV));
		NODE_LIST.add(new ImageButton("empty", UDim2.defaultV, UDim2.defaultV));
		NODE_LIST.add(new Panel(UDim2.defaultV, UDim2.defaultV));
		NODE_LIST.add(new PasswordField("Edit Me!", UDim2.defaultV, UDim2.defaultV));
		NODE_LIST.add(new ProgressBar(UDim2.defaultV, UDim2.defaultV));
		NODE_LIST.add(new ScrollBar(UDim2.defaultV, UDim2.defaultV));
		NODE_LIST.add(new RenderImage("empty", UDim2.defaultV, UDim2.defaultV));
		NODE_LIST.add(new Text("Edit Me!", UDim2.defaultV));
		NODE_LIST.add(new TextField("Edit Me!", UDim2.defaultV, UDim2.defaultV));
		NODE_LIST.add(new TextLine(UDim2.defaultV, UDim2.defaultV));
		
		// me.ramazanenescik04.diken.gui.component.color
		NODE_LIST.add(new AlphaPickBar(UDim2.defaultV, UDim2.defaultV));
		NODE_LIST.add(new ColorPickBar(UDim2.defaultV, UDim2.defaultV));
		NODE_LIST.add(new ColorPickBox(UDim2.defaultV, UDim2.defaultV));
		
		// me.ramazanenescik04.diken.game.entity
		NODE_LIST.add(new Humanoid());
		
		// me.ramazanenescik04.diken.scripting
		NODE_LIST.add(new Script());
		
		NODE_TYPES = NODE_LIST.stream()
		        .collect(Collectors.groupingBy(
		        		obj -> getCategoryName(obj)
		        ));
	}
}
