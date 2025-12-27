package me.ramazanenescik04.diken.gui.compoment;

public class PasswordField extends TextField {
	private static final long serialVersionUID = 1L;

	public PasswordField(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	public PasswordField(String text, int x, int y, int width, int height) {
		super(text, x, y, width, height);
	}

	@Override
	protected String getRenderedText() {
		StringBuilder masked = new StringBuilder();
		for (int i = 0; i < getText().length(); i++) {
			masked.append('*');
		}
		return masked.toString();
	}
}
