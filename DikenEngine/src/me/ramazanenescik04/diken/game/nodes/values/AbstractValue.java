package me.ramazanenescik04.diken.game.nodes.values;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public abstract class AbstractValue<T> extends Node {
	private T value;
	private final Class<T> typeClass;
	private final EnumSettingType enumSettingType;
	
	public AbstractValue(T value, Class<T> typeClass, EnumSettingType enumSettingType) {
		this("Value", value, typeClass, enumSettingType);
	}
	
	public AbstractValue(String name, T value, Class<T> typeClass, EnumSettingType enumSettingType) {
		super(name);
		
		this.typeClass = typeClass;
		this.value = value;
		if (enumSettingType.isAllowedClasses(typeClass)) {
			this.enumSettingType = enumSettingType;
        } else {
        	this.enumSettingType = EnumSettingType.UNKNOWN;
        }
	}
	
	@SuppressWarnings("unchecked")
	public AbstractValue(DataInputStream in) throws IOException {
		super(in);
		super.loadNodeData(in);
		try {
			this.typeClass = (Class<T>) Class.forName(in.readUTF());
		} catch (Exception e) {
			throw new IOException("Type Class load error", e);
		}
		this.enumSettingType = EnumSettingType.valueOf(in.readUTF());
		this.loadValueData(in);
	}

	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	public Class<T> getTypeClass() {
		return typeClass;
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("value", "Value", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(0, 2));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<T>("Value", value, typeClass, enumSettingType).addChangeListener(this::setValue));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
	
	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		out.writeUTF(typeClass.getName());
		out.writeUTF(enumSettingType.name());
		saveValueData(out);
	}

	private void saveValueData(DataOutputStream out) throws IOException {
		switch (enumSettingType) {
			case CHECK_BOX -> out.writeBoolean((Boolean) value);
			case SLIDER -> out.writeFloat(((Number) value).floatValue());
			case COLOR_PICKER -> out.writeInt((Integer) value);
			case KEY_BIND -> out.writeInt((Integer) value);
			case RESOURCE_SELECT -> out.writeUTF((String) value);
			case TEXT_FIELD -> {
			    if (value instanceof Long v)        { out.writeByte(0); out.writeLong(v); }
			    else if (value instanceof Integer v) { out.writeByte(1); out.writeInt(v); }
			    else if (value instanceof Short v)   { out.writeByte(2); out.writeShort(v); }
			    else if (value instanceof Byte v)    { out.writeByte(3); out.writeByte(v); }
			    else if (value instanceof Float v)   { out.writeByte(4); out.writeFloat(v); }
			    else if (value instanceof Double v)  { out.writeByte(5); out.writeDouble(v); }
			    else if (value instanceof String v)  { out.writeByte(6); out.writeUTF(v); }
			}
			case LIST_SELECT -> {
				if (value instanceof Enum<?> e)
					out.writeUTF(e.name());
				else
					out.writeUTF(value.toString());
			}
			case OBJECT_SELECT -> {
				Node node = (Node) value;
				out.writeUTF(node != null ? node.getNetId().toString() : "");
			}
			case UNKNOWN -> {
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void loadValueData(DataInputStream in) throws IOException {
	    switch (enumSettingType) {
	        case CHECK_BOX -> this.value = (T) Boolean.valueOf(in.readBoolean());
	        case SLIDER -> this.value = (T) Float.valueOf(in.readFloat());
	        case COLOR_PICKER -> this.value = (T) Integer.valueOf(in.readInt());
	        case KEY_BIND -> this.value = (T) Integer.valueOf(in.readInt());
	        case TEXT_FIELD -> {
	            byte typeId = in.readByte();
	            this.value = (T) switch (typeId) {
	                case 0 -> in.readLong();
	                case 1 -> in.readInt();
	                case 2 -> in.readShort();
	                case 3 -> in.readByte();
	                case 4 -> in.readFloat();
	                case 5 -> in.readDouble();
	                case 6 -> in.readUTF();
	                default -> throw new IOException("Bilinmeyen TEXT_FIELD tipi: " + typeId);
	            };
	        }
	        case RESOURCE_SELECT -> this.value = (T) in.readUTF();
	        case LIST_SELECT -> this.value = (T) in.readUTF();
	        case OBJECT_SELECT -> {
	            String uuidStr = in.readUTF();
	            if (!uuidStr.isEmpty()) {
	                OnReload.Connect(_ -> {
	                    UUID target = UUID.fromString(uuidStr);
	                    List<Node> results = getRootNode().findByNetId(target);
	                    this.value = (T) (results.isEmpty() ? null : results.get(0));
	                });
	            }
	        }
	        case UNKNOWN -> {}
	    }
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		throw new IOException("AbstractValue final alanları constructor içinde yüklenmelidir.");
	}
}
