package org.torquebox.common.pool;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import org.torquebox.common.spi.ManageablePool;
import org.torquebox.common.spi.Pool;
import org.torquebox.common.spi.PoolListener;

public class SimplePool<T> implements ManageablePool<T> {

	private final Set<T> instances = new HashSet<T>();
	private final Set<T> borrowedInstances = new HashSet<T>();
	private final Set<T> availableInstances = new HashSet<T>();

	private final PoolListeners<T> listeners = new PoolListeners<T>();

	public void addListener(PoolListener<T> listener) {
		this.listeners.getListeners().add(listener);
	}

	public boolean removeListener(PoolListener<T> listener) {
		return this.listeners.getListeners().remove(listener);
	}

	@Override
	public T borrowInstance() throws Exception {
		return borrowInstance(0);
	}

	public T borrowInstance(long timeout) throws InterruptedException {
		this.listeners.instanceRequested(instances.size(), availableInstances.size());
		long start = System.currentTimeMillis();
		synchronized (this.instances) {
			while (availableInstances.isEmpty()) {
				long remainingTime = ((timeout == 0) ? 0 : (timeout - (System.currentTimeMillis() - start)));
				if ((timeout != 0) && (remainingTime <= 0)) {
					return null;
				}
				this.instances.wait(remainingTime);
			}

			Iterator<T> iter = this.availableInstances.iterator();
			T instance = iter.next();
			iter.remove();

			this.borrowedInstances.add(instance);

			this.listeners.instanceBorrowed(instance, instances.size(), availableInstances.size());
			return instance;
		}
	}

	@Override
	public void releaseInstance(T instance) {
		synchronized (this.instances) {
			this.borrowedInstances.remove(instance);
			this.availableInstances.add(instance);
			this.listeners.instanceReleased(instance, instances.size(), availableInstances.size());
			this.instances.notifyAll();
		}

	}

	public void fillInstance(T instance) {
		synchronized (this.instances) {
			this.instances.add(instance);
			this.availableInstances.add(instance);
			this.instances.notifyAll();
		}
	}

	public T drainInstance() throws Exception {
		return drainInstance(0);
	}

	public T drainInstance(long timeout) throws Exception {
		synchronized (this.instances) {
			T instance = borrowInstance(timeout);
			this.borrowedInstances.remove(instance);
			this.instances.remove(instance);
			return instance;
		}
	}

	int size() {
		synchronized (instances) {
			return this.instances.size();
		}
	}

	int borrowedSize() {
		synchronized (instances) {
			return this.borrowedInstances.size();
		}
	}

	int availableSize() {
		synchronized (instances) {
			return this.availableInstances.size();
		}
	}

}
