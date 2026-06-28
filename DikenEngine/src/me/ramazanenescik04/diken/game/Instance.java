package me.ramazanenescik04.diken.game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.ramazanenescik04.diken.game.event.Event;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public abstract class Instance extends Node {
    public final Event OnCollision = new Event();
    public final Event OnPreRender = new Event();
    public final Event OnPostRender = new Event();

    public int x;
    public int y;
    
    public float scaleX = 1.0f;
    public float scaleY = 1.0f;
    private float rotation = 0.0f;
    
    protected Hitbox aabb = null;
    public int color = 0xFFFFFFFF; // Varsayılan Beyaz
    
    protected boolean solid = true;
    protected boolean anchored = false;
    public boolean visible = true;

    public Instance() {
        this("Instance");
    }

    public Instance(String name) {
        super(name);
    }

    public Instance(String name, int x, int y) {
        super(name);
        this.x = x;
        this.y = y;
    }

    public Instance(DataInputStream in) throws IOException {
        super(in);
    }

    public abstract Bitmap render();
    
    public void draw(Bitmap btp) {
        draw(btp, null);
    }

    @Override
    public void draw(Bitmap btp, Hitbox viewport) {
    	OnPreRender.FireEvent();
    	
    	if (aabb != null && aabb.getRotation() != this.rotation) {
    		this.aabb.setRotation(rotation);
    	}
    	
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
                
                btp.draw(myTexture.rotate(rotation), screenX, screenY);
                
                if (debug && this instanceof Instance instance && instance.hasAABB()) {
                    Hitbox globalBox = instance.getGlobalAABB();
                    int debugX = globalBox.getX();
                    int debugY = globalBox.getY();
                    
                    if (viewport != null && !isCameraIndependent()) {
                    	debugX -= viewport.getX();
                    	debugY -= viewport.getY();
                    }
                    
                    btp.box(debugX, debugY, debugX + globalBox.getWidth(), debugY + globalBox.getHeight(), 0xffff0000);
                    btp.box(screenX, screenY, screenX + myTexture.w, screenY + myTexture.h, 0xff00ff00);
                }
            }
        }

        super.draw(btp, viewport);
    }

    @Override
	public int getGlobalX() {
        if (parent instanceof Instance parentInstance) {
            return parentInstance.getGlobalX() + this.x;
        }
        return this.x;
    }

    @Override
    public int getGlobalY() {
        if (parent instanceof Instance parentInstance) {
            return parentInstance.getGlobalY() + this.y;
        }
        return this.y;
    }

    @Override
    public int getRenderX() {
        return getGlobalX();
    }

    @Override
    public int getRenderY() {
        return getGlobalY();
    }

    public void setAABB(int width, int height) {
        this.aabb = new Hitbox(0, 0, width, height).setRotation(rotation);
    }

    public boolean hasAABB() {
        return this.aabb != null;
    }

    public int getAABBWidth() {
        return this.aabb != null ? this.aabb.getWidth() : 0;
    }

    public int getAABBHeight() {
        return this.aabb != null ? this.aabb.getHeight() : 0;
    }

    public void setAABBSize(int width, int height) {
        if (this.aabb == null) {
            return;
        }
        this.aabb.setWidth(Math.max(1, width));
        this.aabb.setHeight(Math.max(1, height));
    }
    
    public boolean isSolid() {
		return solid;
	}

	public void setSolid(boolean isSolid) {
		this.solid = isSolid;
	}

	public boolean isAnchored() {
		return this.anchored;
	}

	public void setAnchored(boolean isStatic) {
		this.anchored = isStatic;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
    
    public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	public List<Instance> findInArea(Hitbox area) {
		List<Instance> result = new ArrayList<>();
		
		for (Node child : getChildren()) {
			if (child instanceof Instance instance && instance.getGlobalAABB() != null && instance.getGlobalAABB().intersects(area)) {
				result.add(instance);
			}
	    }
	    
	    // Eğer çocuklarda yoksa, derinlemesine (recursive) ara
	    for (Node child : getChildren()) {
	    	if (child instanceof Instance instance) {
	    		List<Instance> childResult = instance.findInArea(area);
		        result.addAll(childResult);
	    	}
	    }
	    
	    return result;
	}

    public Hitbox getGlobalAABB() {
        if (aabb == null) return null;
        int globalX = getGlobalX() + aabb.getX();
        int globalY = getGlobalY() + aabb.getY();
        return new Hitbox(globalX, globalY, aabb.getWidth(), aabb.getHeight()).setRotation(rotation);
    }

    @Override
    public List<SettingCategory> getNodeSettings() {
        var list = super.getNodeSettings();

        var key = new SettingCategory.SettingKey("instance", "Instance", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(0, 1));
        var settingCategory = SettingCategory
                .createSettingCategory(key)
                .addSetting(new Setting<Integer>("X", x, Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(val -> this.x = val))
                .addSetting(new Setting<Integer>("Y", y, Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(val -> this.y = val))
                .addSetting(new Setting<Float>("Rotation", rotation, Float.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setRotation))
                .addSetting(new Setting<Boolean>("Visible", visible, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setVisible))
				.addSetting(new Setting<Boolean>("Solid", solid, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setSolid))
				.addSetting(new Setting<Boolean>("Anchored", anchored, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setAnchored))
				.addSetting(new Setting<Integer>("Color", color, Integer.class, EnumSettingType.COLOR_PICKER).addChangeListener(val -> this.color = val));
        
        if (this.aabb != null) {
			settingCategory.addSetting(new Setting<Integer>("Width", aabb.getWidth(), Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(val -> {
				if (aabb != null) aabb.setWidth(val);
			}))
			.addSetting(new Setting<Integer>("Height", aabb.getHeight(), Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(val -> {
				if (aabb != null) aabb.setHeight(val);
			}))
			.addSetting(new Setting<Integer>("Offset X", aabb.getX(), Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(val -> {
				if (aabb != null) aabb.setX(val);
			}))
			.addSetting(new Setting<Integer>("Offset Y", aabb.getY(), Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(val -> {
				if (aabb != null) aabb.setY(val);
			}));
		}

        list.add(settingCategory);
        return list;
    }

	@Override
	public Node copy() {
		Instance copy = (Instance) super.copy();
		
		copy.x = this.x;
		copy.y = this.y;
		copy.scaleX = this.scaleX;
		copy.scaleY = this.scaleY;
		copy.rotation = this.rotation;
		copy.color = this.color;
		copy.solid = this.solid;
		copy.anchored = this.anchored;
		copy.visible = this.visible;
		
		if (this.aabb != null) {
    		copy.aabb = (Hitbox) this.aabb.getBounds();
    	}
		
		return copy;
	}

	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);

		out.writeInt(x);
		out.writeInt(y);
		out.writeFloat(scaleX);
		out.writeFloat(scaleY);
		out.writeFloat(rotation);
		out.writeBoolean(aabb != null);
		if (aabb != null) {
			out.writeInt(aabb.getX());
			out.writeInt(aabb.getY());
			out.writeInt(aabb.getWidth());
			out.writeInt(aabb.getHeight());
		}
		out.writeInt(color);
		out.writeBoolean(solid);
		out.writeBoolean(anchored);
		out.writeBoolean(visible);
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);

		this.x = in.readInt();
		this.y = in.readInt();
		this.scaleX = in.readFloat();
		this.scaleY = in.readFloat();
		this.rotation = in.readFloat();
		if (in.readBoolean()) {
			this.aabb = new Hitbox(in.readInt(), in.readInt(), in.readInt(), in.readInt()).setRotation(rotation);
		} else {
			this.aabb = null;
		}
		this.color = in.readInt();
		this.solid = in.readBoolean();
		this.anchored = in.readBoolean();
		this.visible = in.readBoolean();
	}

}
