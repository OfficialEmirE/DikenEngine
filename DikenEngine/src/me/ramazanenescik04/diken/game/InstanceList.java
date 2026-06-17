package me.ramazanenescik04.diken.game;

import java.util.*;
import me.ramazanenescik04.diken.game.nodes.*;
import me.ramazanenescik04.diken.game.nodes.values.*;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.gui.component.*;
import me.ramazanenescik04.diken.gui.component.color.*;
import me.ramazanenescik04.diken.scripting.Script;
import me.ramazanenescik04.diken.game.entity.*;

public final class InstanceList {
	private static final ArrayList<Node> NODE_LIST;
	
	private InstanceList() {}
	
	public synchronized static List<Node> getNodeList() {
		return new ArrayList<>(NODE_LIST);
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
	
	static {
		NODE_LIST = new ArrayList<>();
		
		// me.ramazanenescik04.diken.game.nodes
		NODE_LIST.add(new Decal());
		NODE_LIST.add(new Folder());
		NODE_LIST.add(new Model());
		NODE_LIST.add(new Part());
		NODE_LIST.add(new Sky());
		NODE_LIST.add(new SpawnLocation());
		NODE_LIST.add(new Texture());
		NODE_LIST.add(new Tool());
		NODE_LIST.add(new Audio());
		
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
	}
}
