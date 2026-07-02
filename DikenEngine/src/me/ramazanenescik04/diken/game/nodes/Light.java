package me.ramazanenescik04.diken.game.nodes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Sahnede bir ışık kaynağını temsil eder. Görsel bir doku çizmez;
 * Lighting servisi tarafından light map oluşturulurken pozisyonu,
 * rengi ve yarıçapı okunur.
 */
public class Light extends Instance {
    public enum LightType {
        POINT,
        SPOT,
        DIRECTIONAL
    }

    private boolean shadows = true;
    public int lightColor = 0xFFFFFFFF;
    public int radius = 150;
    public float intensity = 1.0f;

    public LightType type = LightType.POINT;
    public float coneAngle = 45.0f;

    public Light() {
        this("Light");
    }

    public Light(String name) {
        super(name);
        this.solid = false;
        this.setVisible(true);
    }

    public Light(String name, int x, int y) {
        this(name);
        this.setLocation(x, y);
    }

    public Light(DataInputStream in) throws IOException {
        super(in);
        loadNodeData(in);
    }
    
	@Override
    public Bitmap render() {
    	if (this.isDebugRenderer()) {
    		return ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(10, 1);
    	}
        return null;
    }

    public int getLightColor() {
        return lightColor;
    }

    public void setLightColor(int lightColor) {
        this.lightColor = lightColor;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = Math.max(1, radius);
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = Math.max(0f, intensity);
    }

    public LightType getType() {
        return type;
    }

    public void setType(LightType type) {
        this.type = type == null ? LightType.POINT : type;
    }

    public float getDirection() {
        return this.getRotation();
    }

    public void setDirection(float direction) {
        this.setRotation(direction);
    }

    public float getConeAngle() {
        return coneAngle;
    }

    public void setConeAngle(float coneAngle) {
        this.coneAngle = Math.max(1f, Math.min(360f, coneAngle));
    }
    
    public boolean isShadows() {
		return shadows;
	}

	public void setShadows(boolean shadows) {
		this.shadows = shadows;
	}

	@Override
    public List<SettingCategory> getNodeSettings() {
        var list = super.getNodeSettings();

        var key = new SettingCategory.SettingKey("light", "Light", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(10, 1));

        String[] typeOptions = new String[LightType.values().length];
        for (int i = 0; i < typeOptions.length; i++) {
            typeOptions[i] = LightType.values()[i].name();
        }

        var settingCategory = SettingCategory
                .createSettingCategory(key)
                .addSetting(new Setting<String>("Type", type.name(), typeOptions, String.class, EnumSettingType.LIST_SELECT)
                        .addChangeListener(value -> this.setType(LightType.valueOf(value))))
                .addSetting(new Setting<Integer>("Light Color", lightColor, Integer.class, EnumSettingType.COLOR_PICKER).addChangeListener(this::setLightColor))
                .addSetting(new Setting<Integer>("Radius", radius, Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setRadius))
                .addSetting(new Setting<Float>("Intensity", intensity, Float.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setIntensity))
                .addSetting(new Setting<Float>("Cone Angle", coneAngle, Float.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setConeAngle))
                .addSetting(new Setting<Boolean>("Shadows", shadows, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setShadows));

        list.add(settingCategory);
        return list;
    }

    @Override
    public Light copy() {
        Light copy = (Light) super.copy();
        copy.lightColor = this.lightColor;
        copy.radius = this.radius;
        copy.intensity = this.intensity;
        copy.type = this.type;
        copy.coneAngle = this.coneAngle;
        return copy;
    }

    @Override
    public void saveNodeData(DataOutputStream out) throws IOException {
        super.saveNodeData(out);
        out.writeBoolean(shadows);
        out.writeInt(lightColor);
        out.writeInt(radius);
        out.writeFloat(intensity);
        out.writeUTF(type.name());
        out.writeFloat(coneAngle);
    }

    @Override
    public void loadNodeData(DataInputStream in) throws IOException {
        super.loadNodeData(in);
        this.shadows = in.readBoolean();
        this.lightColor = in.readInt();
        this.radius = in.readInt();
        this.intensity = in.readFloat();
        this.type = LightType.valueOf(in.readUTF());
        this.coneAngle = in.readFloat();
    }
}
