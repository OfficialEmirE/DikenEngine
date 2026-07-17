package me.ramazanenescik04.diken.game.nodes;

import java.awt.Point;
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
		FREECAM
	}
	
	private CameraType cameraType = CameraType.NONE;
	
	private Instance followingInstance;
	private String nodeObjectID = "";
	
	private Point cameraPos = new Point();
	private float zoom = 1.0f;
	
	public Camera(World world) {
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
			cameraPos.x = (followingInstance.getX() + followingInstance.getAABBWidth() / 2)
					- (engine.getScaledWidth() / 2);
			cameraPos.y = (followingInstance.getY() + followingInstance.getAABBHeight() / 2)
					- (engine.getScaledHeight() / 2);
	} else if (cameraType == CameraType.FREECAM) {		
			if (engine.input.isKeyDown(KeyEvent.VK_W)) {
				cameraPos.y -= 8;
			}
			
			if (engine.input.isKeyDown(KeyEvent.VK_A)) {
				cameraPos.x -= 8;
			}
			
			if (engine.input.isKeyDown(KeyEvent.VK_S)) {
				cameraPos.y += 8;
			}
			
			if (engine.input.isKeyDown(KeyEvent.VK_D)) {
				cameraPos.x += 8;
			}
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
		
		if (followingInstance != null) {
			this.nodeObjectID = followingInstance != null ? followingInstance.getNetId().toString() : "";
		}
	}

	public Point getPosition() {
		return cameraPos;
	}

	public void setPosition(Point cameraPos) {
		this.cameraPos = cameraPos;
	}
	
	public void setPosition(int x, int y) {
		this.cameraPos = new java.awt.Point(x, y);
	}
	
	public int getX() {
		return cameraPos.x;
	}
	
	public void setX(int x) {
		this.cameraPos.x = x;
	}

	public int getY() {
		return cameraPos.y;
	}
	
	public void setY(int y) {
		this.cameraPos.y = y;
	}
	
	public void addY(int i) {
		this.cameraPos.y += i;
	}

	public void addX(int i) {
		this.cameraPos.x += i;		
	}

	public float getZoom() {
		return zoom;
	}
	
	public void setZoom(float zoom) {
		this.zoom = Math.max(0.1f, zoom);
	}

	public void reset() {
		this.cameraPos = new Point();
		this.zoom = 1.0f;
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
	
	protected void reloadNode() {
		if (nodeObjectID.isEmpty())
			return;
		
		UUID target = UUID.fromString(nodeObjectID);
        List<Node> results = getRootNode().findByNetId(target);
        this.followingInstance = (Instance) (results.isEmpty() ? null : results.get(0));
	}

	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		
		out.writeUTF(cameraType.name());
		out.writeUTF(nodeObjectID);
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
		this.nodeObjectID = in.readUTF();
		if (in.readBoolean()) {
			int x = in.readInt();
			int y = in.readInt();
			this.cameraPos = new java.awt.Point(x, y);
		} else {
			this.cameraPos = null;
		}
	}

	@Override
	public boolean showStudio() {
		return true;
	}
}
