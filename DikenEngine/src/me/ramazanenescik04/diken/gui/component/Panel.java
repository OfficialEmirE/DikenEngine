package me.ramazanenescik04.diken.gui.component;

import java.util.List;

import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `Panel` type within the DikenEngine `gui.compoment` package.
 */

// TODO: en baştan kodlanacak. inş gold gibi bir sınıf olacak
public class Panel extends GuiComponent {
	private static final long serialVersionUID = 1L;
	
	public enum BorderStyle {
		
	}
	
	private boolean clipsDescendants;
	private BorderStyle panelBorder;

	public Panel(UDim2 position, UDim2 size) {
		super("Panel", position, size);
	}

	public Bitmap render() {
		Bitmap bitmap = super.render();
		
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
                
                sceneBitmap.draw(myTexture, screenX, screenY);
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

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("panel", "Panel", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(4, 2));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<Boolean>("Clips Descendants", this.clipsDescendants, Boolean.class,
						EnumSettingType.CHECK_BOX).addChangeListener(this::setClipsDescendants))
				.addSetting(new Setting<Boolean>("Border Style", this.clipsDescendants, Boolean.class,
						EnumSettingType.CHECK_BOX).addChangeListener(this::setClipsDescendants))
				.addSetting(new Setting<Boolean>("Background Color", this.clipsDescendants, Boolean.class,
						EnumSettingType.CHECK_BOX).addChangeListener(this::setClipsDescendants))
				.addSetting(new Setting<Boolean>("Border Color", this.clipsDescendants, Boolean.class,
						EnumSettingType.CHECK_BOX).addChangeListener(this::setClipsDescendants))
				.addSetting(new Setting<Boolean>("Border Size", this.clipsDescendants, Boolean.class,
						EnumSettingType.CHECK_BOX).addChangeListener(this::setClipsDescendants));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}
