package me.ramazanenescik04.diken.game.nodes;

import java.awt.event.KeyEvent;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.services.AbstractService;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class Camera extends AbstractService {
	private static final long serialVersionUID = 1L;
	
	public enum CameraType {
		NONE,
		FOLLOW,
		FREECAM,
		SCRIPTABLE
	}
	
	private CameraType cameraType = CameraType.NONE;
	private Instance followingInstance;
	private java.awt.Point cameraPos;
	
	public Camera() {
		this("Camera");
	}
	
	public Camera(String name) {
		super(name);
	}
	
	@Override
	public void update(World world, DikenEngine engine) {
		super.update(world, engine);
		
		if (cameraType == CameraType.FOLLOW && followingInstance != null) {
			world.camera.x = (followingInstance.x + followingInstance.getAABBWidth() / 2)
					- (engine.getScaledWidth() / 2);
			world.camera.y = (followingInstance.y + followingInstance.getAABBHeight() / 2)
					- (engine.getScaledHeight() / 2);
		} else if (cameraType == CameraType.FREECAM) {		
			if (engine.input.isKeyDown(KeyEvent.VK_W)) {
				world.camera.y -= 2;
			}
			
			if (engine.input.isKeyDown(KeyEvent.VK_A)) {
				world.camera.x -= 2;
			}
			
			if (engine.input.isKeyDown(KeyEvent.VK_S)) {
				world.camera.y += 2;
			}
			
			if (engine.input.isKeyDown(KeyEvent.VK_D)) {
				world.camera.x += 2;
			}
		} else if (cameraType == CameraType.SCRIPTABLE && cameraPos != null) { // SCRIPTABLE
			world.camera.x = cameraPos.x;
			world.camera.y = cameraPos.y;
		}
	}
	
	// API's
	
	public CameraType getCameraType() {
		return cameraType;
	}

	public void setCameraType(CameraType cameraType) {
		this.cameraType = cameraType;
	}

	public Instance getFollowingInstance() {
		return followingInstance;
	}

	public void setFollowingInstance(Instance followingInstance) {
		this.followingInstance = followingInstance;
	}

	public java.awt.Point getCameraPos() {
		return cameraPos;
	}

	public void setCameraPos(java.awt.Point cameraPos) {
		this.cameraPos = cameraPos;
	}

	@Override
    public List<SettingCategory> getNodeSettings() {
        var list = super.getNodeSettings();

        var key = new SettingCategory.SettingKey("camera", "Camera", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(13, 3));

        var settingCategory = SettingCategory
                .createSettingCategory(key)
                .addSetting(new Setting<CameraType>("Camera Type", cameraType, CameraType.values(), CameraType.class, EnumSettingType.LIST_SELECT)
                        .addChangeListener(this::setCameraType))
				.addSetting(new Setting<Instance>("Following Instance", followingInstance, Instance.class,
						EnumSettingType.OBJECT_SELECT).addChangeListener(this::setFollowingInstance));

        list.add(settingCategory);
        return list;
    }
}
