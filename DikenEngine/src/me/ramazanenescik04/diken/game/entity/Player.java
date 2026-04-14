package me.ramazanenescik04.diken.game.entity;

import java.util.List;

import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.Setting.EnumSettingType;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.SettingCategory.SettingCategoryHelper;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class Player extends Humanoid {
	private static final long serialVersionUID = -5395842731409825680L;
	public transient Bitmap body, hand, defaultFace;
	protected int viewType;
	
	public Player() {
		this(0, 0);
	}

	public Player(int x, int y) {
		super(x, y, 57, 64);
		this.setName("Player");
		this.aabb.setBounds(12, 0, 32, 64);
		this.canMove = false;

		ArrayBitmap default_avatar = (ArrayBitmap) ResourceLocator
				.getResource(new ResourceLocator.ResourceKey("capsule", "default_avatar"));
		this.body = default_avatar.getBitmap(0, 0);
		this.hand = default_avatar.getBitmap(0, 1);
		this.defaultFace = default_avatar.getBitmap(0, 2);
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = new Bitmap(57, 64);
		bitmap.draw(this.body, 12, 0);
		playHandAnimation(bitmap);
		bitmap.draw(viewType == 0 ? this.defaultFace.opposite(false) : this.defaultFace, 12, 6);
		return bitmap;
	}

	protected void playHandAnimation(Bitmap bitmap) {
		bitmap.draw(hand, 0, 34);
		bitmap.draw(hand, 49, 34);
	}

	@Override
	protected void reloadNode() {
		ArrayBitmap default_avatar = (ArrayBitmap) ResourceLocator
				.getResource(new ResourceLocator.ResourceKey("capsule", "default_avatar"));

		this.body = default_avatar.getBitmap(0, 0);
		this.hand = default_avatar.getBitmap(0, 1);
		this.defaultFace = default_avatar.getBitmap(0, 2);

		this.body = this.body.replaceColor(0xffffffff, this.color);
	}

	public int getViewType() {
		return viewType;
	}

	public void setViewType(int viewType) {
		this.viewType = viewType;
	}
	
	public void setViewType(boolean viewType) {
		this.viewType = viewType ? 0 : 1;
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("basePlayer", "Base Player",
				((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(2, 1));

		var settingCategory = SettingCategoryHelper.getOrCreateCategory(key,
				() -> SettingCategory.createSettingCategory(key))
				.addSetting(new Setting<Boolean>("Make Face Opposite", viewType == 0 ? true : false, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setViewType));

		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

}
