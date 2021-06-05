package io.github.mjtb49.strongholdtrainer.render;

public class Color {

	public static final Color WHITE = new Color(255, 255, 255);
	public static final Color GRAY = new Color(50,50,50);
	public static final Color ORANGE = new Color(255,126,0);
	public static final Color RED = new Color(255,0,0);
	public static final Color GREEN = new Color(0,255,0);
	public static final Color BLUE = new Color(0,0,255);
	public static final Color YELLOW = new Color(255,255,51);
	public static final Color PURPLE = new Color(153,50,204);
	public static final Color AQUA = new Color(0,255,255);
	public static final Color PINK = new Color(0xFF, 0x69, 0xB4);

	private final int red;
	private final int green;
	private final int blue;

	public Color(int red, int green, int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public int getRed() {
		return this.red;
	}

	public int getGreen() {
		return this.green;
	}

	public int getBlue() {
		return this.blue;
	}

	public float getFRed() {
		return this.getRed() / 255.0F;
	}

	public float getFGreen() {
		return this.getGreen() / 255.0F;
	}

	public float getFBlue() {
		return this.getBlue() / 255.0F;
	}

}
