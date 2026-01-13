package me.ramazanenescik04.diken.game.entity;

import java.util.ArrayList;
import java.util.List;

import me.ramazanenescik04.diken.game.nodes.Tool;

public class InvertoryPlayer {
	@SuppressWarnings("unused")
	private Player player;
	protected List<Tool> items = new ArrayList<>();
	
	private Tool selectedTool;

	public InvertoryPlayer(Player player) {
		this.player = player;
	}
	
	public void addTool(Tool tool) {
		items.add(tool);
	}
	
	public Tool setSelectedTool(int id) {
		return selectedTool = items.get(id);
	}
	
	public Tool[] getInvertoryShortcutTools() {
		Tool[] tools = new Tool[10];
		
		for (int i = 0; i < tools.length; i++)
			tools[i] = items.get(i);
		
		return tools;
	}
	
	public List<Tool> getAllInvertory() {
		return items;
	}
	
	public List<Tool> getInvertory() {
		List<Tool> items = new ArrayList<>();
		
		for (int i = 10; i < this.items.size(); i++)
			items.add(this.items.get(i));
		
		return items;
	}
	
	public Tool getSelectedTool() {
		return this.selectedTool;
	}
}
