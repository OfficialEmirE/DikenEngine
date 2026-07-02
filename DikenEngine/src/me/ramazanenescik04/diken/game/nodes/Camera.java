package me.ramazanenescik04.diken.game.nodes;

import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.services.AbstractService;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class Camera extends AbstractService {
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

	public Camera(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
	}
	
	@Override
	public void update(World world, DikenEngine engine) {
		super.update(world, engine);
		
		if (!world.getRunService().isRunning()) {
			return;
		}
		
		if (cameraType == CameraType.FOLLOW && followingInstance != null) {
			world.camera.x = (followingInstance.getX() + followingInstance.getAABBWidth() / 2)
					- (engine.getScaledWidth() / 2);
			world.camera.y = (followingInstance.getY() + followingInstance.getAABBHeight() / 2)
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
	
	public void setCameraPos(int x, int y) {
		this.cameraPos = new java.awt.Point(x, y);
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

	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		
		out.writeUTF(cameraType.name());
		out.writeUTF(followingInstance != null ? followingInstance.getNetId().toString() : "");
		out.writeBoolean(cameraPos != null);
		if (cameraPos != null) {
			out.writeInt(cameraPos.x);
			out.writeInt(cameraPos.y);
		}
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		
		this.cameraType = CameraType.valueOf(in.readUTF());
		String uuidStr = in.readUTF();
		if (in.readBoolean()) {
			int x = in.readInt();
			int y = in.readInt();
			this.cameraPos = new java.awt.Point(x, y);
		} else {
			this.cameraPos = null;
		}
		
        if (!uuidStr.isEmpty()) {
            OnReload.Connect(_ -> {
                UUID target = UUID.fromString(uuidStr);
                List<Node> results = getRootNode().findByNetId(target);
                this.followingInstance = (Instance) (results.isEmpty() ? null : results.get(0));
            });
        }
	}
}
