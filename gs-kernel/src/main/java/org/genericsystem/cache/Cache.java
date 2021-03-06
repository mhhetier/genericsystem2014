package org.genericsystem.cache;

import org.genericsystem.api.core.Snapshot;
import org.genericsystem.api.core.exceptions.CacheNoStartedException;
import org.genericsystem.api.core.exceptions.ConcurrencyControlException;
import org.genericsystem.api.core.exceptions.OptimisticLockConstraintViolationException;
import org.genericsystem.api.core.exceptions.RollbackException;
import org.genericsystem.kernel.Context;
import org.genericsystem.kernel.Generic;
import org.genericsystem.kernel.Statics;

public class Cache extends Context {

	protected Transaction transaction;
	protected Differential cacheElement;
	private final ContextEventListener<Generic> listener;

	protected Cache(Engine engine) {
		this(new Transaction(engine));
	}

	protected Cache(Transaction subContext) {
		this(subContext, new ContextEventListener<Generic>() {
		});
	}

	protected Cache(Transaction subContext, ContextEventListener<Generic> listener) {
		super(subContext.getRoot());
		this.listener = listener;
		this.transaction = subContext;
		initialize();
	}

	@Override
	public long getTs() {
		return transaction.getTs();
	}

	@Override
	public Snapshot<Generic> getDependencies(Generic vertex) {
		return cacheElement.getDependencies(vertex);
	}

	protected void initialize() {
		cacheElement = new Differential(cacheElement == null ? new TransactionDifferential() : cacheElement.getSubCache());
	}

	public void shiftTs() throws RollbackException {
		transaction = new Transaction(getRoot(), getRoot().pickNewTs());
		listener.triggersRefreshEvent();
	}

	public void tryFlush() throws ConcurrencyControlException {
		if (!equals(getRoot().getCurrentCache()))
			discardWithException(new CacheNoStartedException("The Cache isn't started"));
		try {
			checkConstraints();
			doSynchronizedApplyInSubContext();
			initialize();
			listener.triggersFlushEvent();
		} catch (OptimisticLockConstraintViolationException exception) {
			discardWithException(exception);
		}
	}

	public void flush() {
		Throwable cause = null;
		for (int attempt = 0; attempt < Statics.ATTEMPTS; attempt++) {
			try {
				// TODO reactivate this
				// if (getEngine().pickNewTs() - getTs() >= timeOut)
				// throw new ConcurrencyControlException("The timestamp cache (" + getTs() + ") is bigger than the life time out : " + Statics.LIFE_TIMEOUT);
				tryFlush();
				return;
			} catch (ConcurrencyControlException e) {
				cause = e;
				try {
					Thread.sleep(Statics.ATTEMPT_SLEEP);
					shiftTs();
				} catch (InterruptedException ex) {
					discardWithException(ex);
				}
			}
		}
		discardWithException(cause);
	}

	protected void doSynchronizedApplyInSubContext() throws ConcurrencyControlException, OptimisticLockConstraintViolationException {
		Differential originalCacheElement = this.cacheElement;
		if (this.cacheElement.getSubCache() instanceof Differential)
			this.cacheElement = (Differential) this.cacheElement.getSubCache();
		try {
			synchronizedApply(originalCacheElement);
		} finally {
			this.cacheElement = originalCacheElement;
		}
	}

	private void synchronizedApply(Differential cacheElement) throws ConcurrencyControlException, OptimisticLockConstraintViolationException {
		synchronized (getRoot()) {
			cacheElement.apply();
		}
	}

	public void clear() {
		initialize();
		listener.triggersClearEvent();
		listener.triggersRefreshEvent();
	}

	public void mount() {
		cacheElement = new Differential(cacheElement);
	}

	public void unmount() {
		IDifferential subCache = cacheElement.getSubCache();
		cacheElement = subCache instanceof Differential ? (Differential) subCache : new Differential(subCache);
		listener.triggersClearEvent();
		listener.triggersRefreshEvent();
	}

	@Override
	public Engine getRoot() {
		return (Engine) super.getRoot();
	}

	public Cache start() {
		return getRoot().start(this);
	}

	public void stop() {
		getRoot().stop(this);
	}

	@Override
	protected void triggersMutation(Generic oldDependency, Generic newDependency) {
		if (listener != null)
			listener.triggersMutationEvent(oldDependency, newDependency);
	}

	@Override
	protected Generic plug(Generic generic) {
		cacheElement.plug(generic);
		getChecker().checkAfterBuild(true, false, generic);
		return generic;
	}

	@Override
	protected void unplug(Generic generic) {
		getChecker().checkAfterBuild(false, false, generic);
		cacheElement.unplug(generic);
	}

	protected void checkConstraints() throws RollbackException {
		cacheElement.checkConstraints(getChecker());
	}

	@Override
	public void discardWithException(Throwable exception) throws RollbackException {
		clear();
		throw new RollbackException(exception);
	}

	public int getCacheLevel() {
		return cacheElement.getCacheLevel();
	}

	private class TransactionDifferential implements IDifferential {

		@Override
		public void apply(Iterable<Generic> removes, Iterable<Generic> adds) throws ConcurrencyControlException, OptimisticLockConstraintViolationException {
			transaction.apply(removes, adds);
		}

		@Override
		public boolean isAlive(Generic generic) {
			return transaction.isAlive(generic);
		}

		@Override
		public Snapshot<Generic> getDependencies(Generic vertex) {
			return transaction.getDependencies(vertex);
		}
	}

	public static interface ContextEventListener<X> {

		default void triggersMutationEvent(X oldDependency, X newDependency) {
		}

		default void triggersRefreshEvent() {
		}

		default void triggersClearEvent() {
		}

		default void triggersFlushEvent() {
		}
	}

}
