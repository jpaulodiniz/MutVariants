package br.ufmg.labsoft.mutvariants.listeners;

/**
 * Issue #5
 */
public class ListenerUtil {

	public static ISimpleListener listener = new ISimpleListener() {
		@Override
		public void listen(String id) {
			// may be overridden by a 'client'
		}
	};

	public static ILoopListener loopListener = new ILoopListener() {
		@Override
		public void listen(String loopId, long count) {
			// may be overridden by a 'client'
		}
	};

	public static IMutantListener mutListener = new IMutantListener() {
		@Override
		public boolean listen(int id) {
			// may be overridden by a 'client'
			return false;
		}
	};			
}