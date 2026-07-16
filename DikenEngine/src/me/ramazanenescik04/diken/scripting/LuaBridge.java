package me.ramazanenescik04.diken.scripting;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.InstanceList;
import me.ramazanenescik04.diken.game.Node;

public class LuaBridge {
	private Script script;
	
	public LuaBridge(Script script) {
		this.script = script;
	}
	
	public Object create(String className) {
		for (Node node : InstanceList.getNodeList()) {
			if (node.getClass().getSimpleName().equalsIgnoreCase(className)) {
				return node.copy();
			}
		}
		DikenEngine.errorLog("Instance.new Error: '" + className + "' adında bir Node bulunamadı!");
		return null;
	}

	public Object clone(Object object) {
		if (object == null) {
			DikenEngine.errorLog("Instance.clone Error: Object Null Olamaz!");
			return null;
		}

		if (object instanceof Node node) {
			var copyNode = node.copy();
			if (copyNode != null) {
				return copyNode;
			}

			DikenEngine.errorLog(
					"Instance.clone Error: '" + object.getClass().getSimpleName() + ", Archiveable true değil.");
			return null;
		} else if (object instanceof Cloneable c) {
			return c;
		}

		DikenEngine.errorLog(
				"Instance.clone Error: '" + object.getClass().getSimpleName() + ", Klonlamayı desteklemiyor.");
		return null;
	}

	public Object getCurrentScript() {
		return this.script;
	}

	public void log(String message) {
	    DikenEngine.log(message);
	}
}
