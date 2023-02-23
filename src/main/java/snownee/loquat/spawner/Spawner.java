package snownee.loquat.spawner;

import snownee.lychee.core.post.PostAction;

import java.util.Map;

public class Spawner {
	public Wave[] waves;

	public static class Wave {
		public int wait;
		public int timeOut;
		public PostAction[] actions;
	}

}
