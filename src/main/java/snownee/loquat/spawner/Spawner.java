package snownee.loquat.spawner;

import snownee.lychee.core.contextual.ContextualHolder;
import snownee.lychee.core.post.PostAction;

public class Spawner {
	public Wave[] waves;

	public static class Wave {
		private static final ContextualHolder EMPTY_CONDITIONS = new ContextualHolder();

		public float wait;
		public int timeout;
		public ContextualHolder contextual = EMPTY_CONDITIONS;
		public PostAction[] post;
	}

}
