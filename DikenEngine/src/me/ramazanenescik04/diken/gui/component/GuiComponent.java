package me.ramazanenescik04.diken.gui.component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.event.Event;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.gui.hitbox.IHitbox;
import me.ramazanenescik04.diken.renderer.FrameBitmapPool;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `GuiComponent` type within the DikenEngine `gui.component` package.
 */
public abstract class GuiComponent extends Node {
	private static final long serialVersionUID = 1L;
	
	public final Event OnPreRender = new Event();
    public final Event OnPostRender = new Event();

	private UDim2 position;
	private UDim2 size;

	protected List<IGuiListener> listeners = new ArrayList<>();
	private Hitbox prevBounds;
	
	protected boolean visible = true;
	protected boolean active = true;

	public GuiComponent(double scaleX, int offsetX, double scaleY, int offsetY,
	                     double scaleWidth, int offsetWidth, double scaleHeight, int offsetHeight) {
		var position = new UDim2(scaleX, offsetX, scaleY, offsetY);
		var size = new UDim2(scaleWidth, offsetWidth, scaleHeight, offsetHeight);

		this ("GuiComponent", position, size);
	}
	
	public GuiComponent(String name, UDim2 position, UDim2 size) {
		super(name);

		this.position = position;
		this.size = size;

		this.prevBounds = getAbsoluteBounds();
	}

	public GuiComponent(UDim2 position, UDim2 size) {
		this("GuiComponent", position, size);
	}

	public Bitmap render() {
		Hitbox bounds = getAbsoluteBounds();
		return FrameBitmapPool.newBitmap(bounds.getWidth(), bounds.getHeight());
	}

	@Override
	public void update(World world, DikenEngine engine) {
		super.update(world, engine);

		this.tick(engine);
		this.checkListener();
	}

	public void tick(DikenEngine engine) {
	}

	protected void keyPressed(char var1, int var2) {
	}

	protected void mouseClicked(int x, int y, int button, boolean isTouch) {
	}

	protected void mouseGetInfo(int x, int y, boolean isTouch) {
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible;
	}

	public UDim2 getPosition() {
		return position;
	}

	public void setPosition(UDim2 position) {
		this.position = position;
	}
	
	public void setPosition(int x, int y) {
		this.position = new UDim2(0, x, 0, y);
	}

	public UDim2 getSize() {
		return size;
	}

	public void setSize(UDim2 size) {
		this.size = size;
	}
	
	public void setSize(int w, int h) {
	    this.size = new UDim2(0, w, 0, h);
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean isActive() {
		return active;
	}

	@Override
    public int getGlobalX() {
        return this.getAbsoluteBounds().getX();
    }

	@Override
    public int getGlobalY() {
        return this.getAbsoluteBounds().getY();
    }
	
	public int getLocalX() {
		return getLocalAbsoluteBounds().getX();
	}
	
	public int getLocalY() {
		return getLocalAbsoluteBounds().getY();
	}
	
	public int getWidth() {
	    return getAbsoluteBounds().getWidth();
	}

	public int getHeight() {
	    return getAbsoluteBounds().getHeight();
	}

	/**
	 * Parent'ın absolute bounds'unu baz alarak bu komponentin
	 * absolute (pixel cinsinden) Hitbox'unu hesaplar.
	 */
	public Hitbox getAbsoluteBounds() {
		Hitbox parentBounds = getParentAbsoluteBounds();

		IHitbox sizeBox = size.getGlobalPosition(parentBounds.getWidth(), parentBounds.getHeight());
		int absWidth = sizeBox.getX();
		int absHeight = sizeBox.getY();

		IHitbox posBox = position.getGlobalPosition(parentBounds.getWidth(), parentBounds.getHeight());
		int absX = parentBounds.getX() + posBox.getX();
		int absY = parentBounds.getY() + posBox.getY();

		return new Hitbox(absX, absY, absWidth, absHeight);
	}

	private Hitbox getParentAbsoluteBounds() {
		Node parent = getParent();
		if (parent instanceof GuiComponent guiParent) {
			return guiParent.getAbsoluteBounds();
		}

		var engine = DikenEngine.getEngine();
		if (engine == null) { 
			return new Hitbox(0, 0, 100, 100);
		}
		return new Hitbox(0, 0, engine.getScaledWidth(), engine.getScaledHeight());
	}
	
	protected Hitbox getLocalAbsoluteBounds() {
		Hitbox parentBounds = getParentAbsoluteBounds();

		IHitbox sizeBox = size.getGlobalPosition(parentBounds.getWidth(), parentBounds.getHeight());
		int absWidth = sizeBox.getX();
		int absHeight = sizeBox.getY();

		IHitbox posBox = position.getGlobalPosition(parentBounds.getWidth(), parentBounds.getHeight());
		int absX = posBox.getX();
		int absY = posBox.getY();

		return new Hitbox(absX, absY, absWidth, absHeight);
	}

	public void addGuiListener(IGuiListener listener) {
		listeners.add(listener);
	}

	public void removeGuiListener(IGuiListener listener) {
		listeners.remove(listener);
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}

	public void checkListener() {
		Hitbox current = getAbsoluteBounds();

		if (!current.equals(prevBounds)) {
			Hitbox old = prevBounds;
			prevBounds = current;

			listeners.forEach(l -> {
				l.changedBounds(current);
				if (old.getWidth() != current.getWidth() || old.getHeight() != current.getHeight()) {
					l.changedSize(current, current.getWidth(), current.getHeight());
				}
				if (old.getX() != current.getX() || old.getY() != current.getY()) {
					l.changedLocation(current, current.getX(), current.getY());
				}
			});
		}
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("guiComponent", "GuiComponent", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(15, 1));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<String>("Position", position.toString(), String.class, EnumSettingType.TEXT_FIELD).addChangeListener(e -> {
					this.setPosition(toUDim2(e));
				}))
				.addSetting(new Setting<String>("Size", size.toString(), String.class, EnumSettingType.TEXT_FIELD).addChangeListener(e -> {
					this.setSize(toUDim2(e));
				}))
				.addSetting(new Setting<Boolean>("Visible", this.visible, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setVisible))
				.addSetting(new Setting<Boolean>("Active", this.active, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setActive));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
	
	protected static UDim2 toUDim2(String udim2Str) {
        List<Double> numberList = new ArrayList<>();

        Pattern desen = Pattern.compile("-?\\d+(\\.\\d+)?");
        Matcher eslesme = desen.matcher(udim2Str);

        while (eslesme.find()) {
        	numberList.add(Double.parseDouble(eslesme.group()));
        }
        
        if (numberList.size() < 4) return UDim2.defaultV;

        return new UDim2(
        		numberList.get(0),
        		numberList.get(1).intValue(), 
        		numberList.get(2), 
        		numberList.get(3).intValue()
        );
    }

	public void drawComponent(Bitmap sceneBitmap, Hitbox viewport) {
		OnPreRender.FireEvent();
        if (!visible) return;

        if (shouldRenderSelf(viewport)) {
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
            if (child instanceof GuiComponent childInstance) {
				childInstance.drawComponent(sceneBitmap, viewport);
			}
        }
        
        OnPostRender.FireEvent();
	}
}