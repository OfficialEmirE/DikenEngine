package me.ramazanenescik04.diken.gui.component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `Panel` type within the DikenEngine `gui.compoment` package.
 */

public class Panel extends GuiComponent {
	public enum BorderStyle {
		Classic,
		Fill
	}
	
	private BorderStyle panelBorder = BorderStyle.Fill;
	private int backgroundColor = 0xffaaaaaa, borderColor = 0xff000000;
	private boolean clipsDescendants;
	private int borderSize = 1;

	public Panel(UDim2 position, UDim2 size) {
		super("Panel", position, size);
	}

	public Panel(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
	}

	public Bitmap render() {
		Bitmap bitmap = super.render();
		if (panelBorder == BorderStyle.Fill) {
			bitmap.clear(this.backgroundColor);
			
			int x = 0, y = 0, w = this.getWidth() - 1, h = this.getHeight() - 1;
			for (int i = 0; i < borderSize; i++) {
				bitmap.box(x, y, w, h, borderColor);
				
				x += 1; y += 1;
				w -= 1; h -= 1;
			}
		}
		
		if (isDebugRenderer()) {
			bitmap.box(0, 0, bitmap.w - 1, bitmap.h - 1, 0xffffffff);
			bitmap.drawLine(0, 0, bitmap.w, bitmap.h, 0xffffffff, 1);
			bitmap.drawLine(bitmap.w, 0, 0, bitmap.h, 0xffffffff ,1);
		}

		return bitmap;
	}

	@Override
	public void drawComponent(Bitmap sceneBitmap, Hitbox viewport) {
		OnPreRender.FireEvent();
        if (!visible) return;

        if (super.shouldRenderSelf(viewport)) {
            Bitmap myTexture = render();

            if (myTexture != null) {
                int renderX = getRenderX();
                int renderY = getRenderY();
                int screenX = renderX;
                int screenY = renderY;
                
                if (viewport != null && !isCameraIndependent()) {
                	screenX -= viewport.getX();
                	screenY -= viewport.getY();
                }
                
                sceneBitmap.blend(myTexture, screenX, screenY);
            }
        }
        
        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            if (child instanceof GuiComponent childInstance && shouldRenderSelf(childInstance, viewport)) {
				childInstance.drawComponent(sceneBitmap, viewport);
			}
        }
        
        OnPostRender.FireEvent();
	}

	protected boolean shouldRenderSelf(GuiComponent childComponent, Hitbox viewport) {
		if (childComponent == null) return false;
		if (!clipsDescendants) return true;
		
		Hitbox globalBox = this.getLocalAbsoluteBounds();
		return (globalBox.intersects(childComponent.getAbsoluteBounds()));
	}

	public boolean isClipsDescendants() {
		return clipsDescendants;
	}

	public void setClipsDescendants(boolean clipsDescendants) {
		this.clipsDescendants = clipsDescendants;
	}

	public BorderStyle getBorderStyle() {
		return panelBorder;
	}

	public void setBorderStyle(BorderStyle panelBorder) {
		this.panelBorder = panelBorder;
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public int getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(int borderColor) {
		this.borderColor = borderColor;
	}

	public int getBorderSize() {
		return borderSize;
	}

	public void setBorderSize(int borderSize) {
		this.borderSize = borderSize;
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("panel", "Panel", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(4, 2));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<Boolean>("Clips Descendants", this.clipsDescendants, Boolean.class,
						EnumSettingType.CHECK_BOX).addChangeListener(this::setClipsDescendants))
				.addSetting(new Setting<BorderStyle>("Border Style", this.panelBorder, BorderStyle.values(), BorderStyle.class,
						EnumSettingType.LIST_SELECT).addChangeListener(this::setBorderStyle))
				.addSetting(new Setting<Integer>("Background Color", this.backgroundColor, Integer.class,
						EnumSettingType.COLOR_PICKER).addChangeListener(this::setBackgroundColor))
				.addSetting(new Setting<Integer>("Border Color", this.borderColor, Integer.class,
						EnumSettingType.COLOR_PICKER).addChangeListener(this::setBorderColor))
				.addSetting(new Setting<Integer>("Border Size", this.borderSize, Integer.class,
						EnumSettingType.TEXT_FIELD).addChangeListener(this::setBorderSize));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		out.writeUTF(panelBorder.name());
		out.writeInt(backgroundColor);
		out.writeInt(borderColor);
		out.writeBoolean(clipsDescendants);
		out.writeInt(borderSize);
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		this.panelBorder = BorderStyle.valueOf(in.readUTF());
		this.backgroundColor = in.readInt();
		this.borderColor = in.readInt();
		this.clipsDescendants = in.readBoolean();
		this.borderSize = in.readInt();
	}
}
