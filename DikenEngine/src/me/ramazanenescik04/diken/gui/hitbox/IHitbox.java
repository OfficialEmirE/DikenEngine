package me.ramazanenescik04.diken.gui.hitbox;

public interface IHitbox {

	boolean intersects(Hitbox r);

	boolean contains(int px, int py);

	boolean contains(Hitbox r);

	IHitbox setActive(boolean active);

	boolean isActive();

	void setLocation(int x, int y);

	int getX();

	void setX(int x);

	int getY();

	void setY(int y);

	int getWidth();

	int getHeight();

	void setWidth(int width);

	void setHeight(int height);

	void setBounds(int x, int y, int width, int height);

	void setSize(int width, int height);
	
	float getRotation();
	
	IHitbox setRotation(float rotation);
	
	IHitbox rotate(float deltaDegress);

	IHitbox getBounds();
}