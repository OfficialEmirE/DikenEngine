package me.ramazanenescik04.diken.studio.builders;

import java.awt.FlowLayout;
import java.util.Objects;

import javax.swing.*;

import me.ramazanenescik04.diken.language.Lang;

public class Buttonbar extends AbstractBuilder<AbstractButton> {
	public Buttonbar(String id) {
		super(id);
	}
	
	public static class Builder extends AbstractBuilder.Builder<Buttonbar, JPanel> {
		@Override
		protected Buttonbar createT(String key) {
			return new Buttonbar(key);
		}
		
		public void addButton(Buttonbar bar, String id, String text, Runnable r, Object...objects) {
			Runnable runnable = Objects.requireNonNullElse(r, () -> {});
			Objects.requireNonNull(bar);
			Objects.requireNonNull(id);
			
			JButton button = new JButton(Lang.get(text, objects));
			button.addActionListener(_ -> {
				runnable.run();
			});
			button.setName(id);
			
			bar.add(text, button);
		}

		@Override
		public JPanel convert() {
			JPanel panel = new JPanel();
			FlowLayout flowLayout = new FlowLayout();
			panel.setLayout(flowLayout);
			flowLayout.setAlignment(FlowLayout.RIGHT);
			
			for (var entry : this.abstractBuilders.values()) {
				entry.getButtons().forEach(button -> panel.add(button));
			}
			
			return panel;
		}
	}
}
