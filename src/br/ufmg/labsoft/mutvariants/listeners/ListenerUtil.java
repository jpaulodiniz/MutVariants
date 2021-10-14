package br.ufmg.labsoft.mutvariants.listeners;

/**
 * Issue #5
 */
public class ListenerUtil {

	/**
	 * a call to listen will be included in all places defined in Issue #5
	 */
	public static IMutatedCodeListener listener = new IMutatedCodeListener() {
		@Override
		public void listen(String operation) {
			// may be overridden by a 'client'
		}

		@Override
		public void listen(String loopVar, long count) {
			// may be overridden by a 'client'
		}
	};
}