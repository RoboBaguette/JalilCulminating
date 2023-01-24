package jalil.sayeed;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
/**
 * Fllename: DesktopLauncher.java
 * Author: Jalil, S
 * Date Created: December 19th
 * Description: "Driver" class for the game
 */
public class DesktopLauncher {
	public static void main (String[] arg) {
		// Initial setup for the game
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("Pixel Warrior");
		config.setWindowedMode(1080, 720);
		config.setForegroundFPS(60);
		config.setIdleFPS(60);
		config.useVsync(true);
		new Lwjgl3Application(new Main(), config);
	}
}
