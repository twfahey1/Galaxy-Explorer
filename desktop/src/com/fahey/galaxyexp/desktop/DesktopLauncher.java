package com.fahey.galaxyexp.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.fahey.galaxyexp.GalaxyExplorerMain;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title="Galaxy Explorer BETA";
		config.width = 1080;
		config.height = 720;
		new LwjglApplication(new GalaxyExplorerMain(), config);
	}
}
