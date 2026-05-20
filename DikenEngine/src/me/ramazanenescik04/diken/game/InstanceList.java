package me.ramazanenescik04.diken.game;

import java.util.*;
import me.ramazanenescik04.diken.game.nodes.*;
import me.ramazanenescik04.diken.scripting.Script;
import me.ramazanenescik04.diken.game.entity.*;

public final class InstanceList {
	private static final List<Node> NODE_LIST;
	
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
		
		// me.ramazanenescik04.diken.game.entity
		NODE_LIST.add(new Humanoid());
		NODE_LIST.add(new Player());
		
		// me.ramazanenescik04.diken.scripting
		NODE_LIST.add(new Script());
	}
}
