package jalil.sayeed;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.physics.box2d.Box2D;
import jalil.sayeed.Driver;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("Pixel Warrior");
		config.setWindowedMode(1080, 720);
		config.setForegroundFPS(60);
		config.setIdleFPS(60);
		config.useVsync(true);
		new Lwjgl3Application(new Driver(), config);
	}
}
