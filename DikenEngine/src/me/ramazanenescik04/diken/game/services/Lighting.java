package me.ramazanenescik04.diken.game.services;

import java.util.ArrayList;
import java.util.List;

import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.nodes.Light;
import me.ramazanenescik04.diken.game.nodes.Sky;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.renderer.FrameBitmapPool;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class Lighting extends Service {
	private static final long serialVersionUID = 1L;

	private Sky sky;
	public int ambientColor = 0xFF3C3C3C;
	public boolean lightingEnabled = true;

	public Lighting() {
		this("Lighting");
	}

	public Lighting(String name) {
		super(name);
	}

	public void draw(Bitmap sceneBitmap, Hitbox viewport) {
		drawSky(sceneBitmap, viewport);
	}

	public void drawSky(Bitmap sceneBitmap, Hitbox viewport) {
		OnPreRender.FireEvent();

		if (sky != null) {
			sky.draw(sceneBitmap, viewport);
		}

		OnPostRender.FireEvent();
	}

	public void applyLightOverlay(Bitmap sceneBitmap, Hitbox viewport, Workspace workspace) {
		if (!lightingEnabled) {
			return;
		}

		List<Light> lights = workspace.findByClass(Light.class);

		Bitmap lightMap = FrameBitmapPool.newBitmap(sceneBitmap.w, sceneBitmap.h);
		
		lightMap.xOffs = 0;
		lightMap.yOffs = 0;
		lightMap.xFlip = false;
		lightMap.clear(ambientColor);

		int viewX = viewport != null ? viewport.getX() : 0;
		int viewY = viewport != null ? viewport.getY() : 0;

		for (Light light : lights) {
			if (!light.isVisible()) continue;

			Light.LightType type = light.getType();

			if (type == Light.LightType.DIRECTIONAL) {
				drawDirectionalLight(lightMap, light.getLightColor(), light.getIntensity());
				continue;
			}

			int radius = light.getRadius();
			if (radius <= 0) continue;

			int lightWorldX = light.getGlobalX();
			int lightWorldY = light.getGlobalY();
			int lightScreenX = lightWorldX - viewX;
			int lightScreenY = lightWorldY - viewY;

			if (lightScreenX + radius < 0 || lightScreenX - radius >= lightMap.w
					|| lightScreenY + radius < 0 || lightScreenY - radius >= lightMap.h) {
				continue;
			}

			List<Instance> occluders = collectOccluders(workspace, lightWorldX, lightWorldY, radius);

			if (type == Light.LightType.SPOT) {
				drawSpotLight(lightMap, lightScreenX, lightScreenY, radius, light.getLightColor(), light.getIntensity(),
						light.getDirection(), light.getConeAngle(), occluders, viewX, viewY, light.isShadows());
			} else {
				drawPointLight(lightMap, lightScreenX, lightScreenY, radius, light.getLightColor(),
						light.getIntensity(), occluders, viewX, viewY, light.isShadows());
			}
		}

		sceneBitmap.multiplyBlend(lightMap, 0, 0);
	}

	private void drawPointLight(Bitmap lightMap, int cx, int cy, int radius, int color, float intensity,
			List<Instance> occluders, int viewX, int viewY, boolean shadows) {
		drawRadialArea(lightMap, cx, cy, radius, color, intensity, -1f, -1f, occluders, viewX, viewY, shadows); // açı filtresi yok
	}

	private void drawSpotLight(Bitmap lightMap, int cx, int cy, int radius, int color, float intensity, float direction,
			float coneAngle, List<Instance> occluders, int viewX, int viewY, boolean shadows) {
		drawRadialArea(lightMap, cx, cy, radius, color, intensity, direction, coneAngle, occluders, viewX, viewY, shadows);
	}

	private void drawDirectionalLight(Bitmap lightMap, int color, float intensity) {
		int srcR = (color >> 16) & 0xff;
		int srcG = (color >> 8) & 0xff;
		int srcB = color & 0xff;

		float strength = Math.max(0f, Math.min(1f, intensity));
		if (strength <= 0f) return;

		for (int y = 0; y < lightMap.h; y++) {
			for (int x = 0; x < lightMap.w; x++) {
				int existing = lightMap.getPixel(x, y);
				int exR = (existing >> 16) & 0xff;
				int exG = (existing >> 8) & 0xff;
				int exB = existing & 0xff;

				int newR = (int) (exR + (srcR - exR) * strength);
				int newG = (int) (exG + (srcG - exG) * strength);
				int newB = (int) (exB + (srcB - exB) * strength);

				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));

				lightMap.setPixel(x, y, 0xff000000 | (newR << 16) | (newG << 8) | newB);
			}
		}
	}

	private void drawRadialArea(Bitmap lightMap, int cx, int cy, int radius, int color, float intensity,
			float direction, float coneAngle, List<Instance> occluders, int viewX, int viewY, boolean shadows) {
		if (radius <= 0) return;

		int x0 = Math.max(0, cx - radius);
		int x1 = Math.min(lightMap.w - 1, cx + radius);
		int y0 = Math.max(0, cy - radius);
		int y1 = Math.min(lightMap.h - 1, cy + radius);

		if (x0 > x1 || y0 > y1) return;

		int srcR = (color >> 16) & 0xff;
		int srcG = (color >> 8) & 0xff;
		int srcB = color & 0xff;

		float radiusSq = radius * radius;
		boolean useCone = coneAngle >= 0f;
		float halfConeRad = (float) Math.toRadians(coneAngle / 2.0);
		float dirRad = (float) Math.toRadians(direction);

		float lightWorldX = cx + viewX;
		float lightWorldY = cy + viewY;

		for (int y = y0; y <= y1; y++) {
			for (int x = x0; x <= x1; x++) {
				float dx = x - cx;
				float dy = y - cy;
				float distSq = dx * dx + dy * dy;

				if (distSq > radiusSq) continue;

				if (useCone && distSq > 0.0001f) {
					float pixelAngle = (float) Math.atan2(dy, dx);
					float angleDiff = angleDifference(pixelAngle, dirRad);
					if (Math.abs(angleDiff) > halfConeRad) continue;
				}

				if (!occluders.isEmpty() && isOccluded(occluders, lightWorldX, lightWorldY, x + viewX, y + viewY) && shadows) {
					continue;
				}

				float normalizedDistSq = distSq / radiusSq;
				float falloff = 1.0f - normalizedDistSq;
				float strength = falloff * intensity;
				if (strength <= 0f) continue;
				if (strength > 1f) strength = 1f;

				int existing = lightMap.getPixel(x, y);
				int exR = (existing >> 16) & 0xff;
				int exG = (existing >> 8) & 0xff;
				int exB = existing & 0xff;

				int newR = (int) (exR + (srcR - exR) * strength);
				int newG = (int) (exG + (srcG - exG) * strength);
				int newB = (int) (exB + (srcB - exB) * strength);

				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));

				lightMap.setPixel(x, y, 0xff000000 | (newR << 16) | (newG << 8) | newB);
			}
		}
	}

	private static float angleDifference(float a, float b) {
		float diff = a - b;
		while (diff > Math.PI) diff -= 2 * Math.PI;
		while (diff < -Math.PI) diff += 2 * Math.PI;
		return diff;
	}

	private List<Instance> collectOccluders(Workspace workspace, int lightWorldX, int lightWorldY, int radius) {
		Hitbox lightArea = new Hitbox(lightWorldX - radius, lightWorldY - radius, radius * 2, radius * 2);
		List<Instance> found = workspace.findInArea(lightArea);

		List<Instance> occluders = new ArrayList<>();
		for (Instance inst : found) {
			if (inst.isSolid() && inst.getGlobalAABB() != null) {
				occluders.add(inst);
			}
		}
		return occluders;
	}

	private boolean isOccluded(List<Instance> occluders, float lightX, float lightY, float pixelX, float pixelY) {
		for (Instance inst : occluders) {
			if (!inst.isVisible())
				return false;
			
			Hitbox box = inst.getGlobalAABB();
			if (box == null) continue;

			if (segmentIntersectsAABB(lightX, lightY, pixelX, pixelY, box.getX(), box.getY(), box.getWidth(), box.getHeight())) {
				return true;
			}
		}
		return false;
	}

	private boolean segmentIntersectsAABB(float x0, float y0, float x1, float y1, int bx, int by, int bw, int bh) {
		float dx = x1 - x0;
		float dy = y1 - y0;

		float tMin = 0f;
		float tMax = 1f;

		float minX = bx;
		float maxX = bx + bw;
		float minY = by;
		float maxY = by + bh;

		if (dx == 0f) {
			if (x0 < minX || x0 > maxX) return false;
		} else {
			float tx1 = (minX - x0) / dx;
			float tx2 = (maxX - x0) / dx;
			if (tx1 > tx2) { float tmp = tx1; tx1 = tx2; tx2 = tmp; }
			tMin = Math.max(tMin, tx1);
			tMax = Math.min(tMax, tx2);
			if (tMin > tMax) return false;
		}

		if (dy == 0f) {
			if (y0 < minY || y0 > maxY) return false;
		} else {
			float ty1 = (minY - y0) / dy;
			float ty2 = (maxY - y0) / dy;
			if (ty1 > ty2) { float tmp = ty1; ty1 = ty2; ty2 = tmp; }
			tMin = Math.max(tMin, ty1);
			tMax = Math.min(tMax, ty2);
			if (tMin > tMax) return false;
		}

		return true;
	}

	public void setSky(Sky sky) {
		this.sky = sky;
	}

	public Sky getSky() {
		return sky;
	}

	public int getAmbientColor() {
		return ambientColor;
	}

	public void setAmbientColor(int ambientColor) {
		this.ambientColor = ambientColor;
	}

	public boolean isLightingEnabled() {
		return lightingEnabled;
	}

	public void setLightingEnabled(boolean lightingEnabled) {
		this.lightingEnabled = lightingEnabled;
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("lighting", "Lighting", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(10, 1));

		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<Sky>("Sky", sky, Sky.class, EnumSettingType.OBJECT_SELECT).addChangeListener(this::setSky))
				.addSetting(new Setting<Boolean>("Lighting Enabled", lightingEnabled, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setLightingEnabled))
				.addSetting(new Setting<Integer>("Ambient Color", ambientColor, Integer.class, EnumSettingType.COLOR_PICKER).addChangeListener(this::setAmbientColor));

		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

}