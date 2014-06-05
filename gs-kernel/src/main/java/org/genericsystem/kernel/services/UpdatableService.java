package org.genericsystem.kernel.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.genericsystem.kernel.UpdateRestructurator;

public interface UpdatableService<T extends UpdatableService<T>> extends BindingService<T> {

	@SuppressWarnings("unchecked")
	default T setValue(Serializable value) {
		T meta = getMeta();
		return ((UpdateRestructurator<T>) (() -> buildInstance().init(meta.getLevel() + 1, meta, getSupers(), value, getComponents()).plug())).rebuildAll((T) this, false);
	}

	@SuppressWarnings("unchecked")
	default T addSuper(T superToAdd) {
		return ((UpdateRestructurator<T>) (() -> getMeta().buildInstance(new OrderedSupers<T>(getSupersStream(), superToAdd), getValue(), getComponents()).plug())).rebuildAll((T) this, true);
	}

	public static class OrderedSupers<T extends UpdatableService<T>> extends ArrayList<T> {
		private static final long serialVersionUID = 6163099887384346235L;

		public OrderedSupers(Stream<T> adds) {
			for (T add : adds.collect(Collectors.toList()))
				add(add);
		}

		public OrderedSupers(Stream<T> adds, T lastAdd) {
			this(adds);
			add(lastAdd);
		}

		@Override
		public boolean add(T candidate) {
			for (T element : this)
				if (element.inheritsFrom(candidate))
					return false;
			Iterator<T> it = iterator();
			while (it.hasNext())
				if (candidate.inheritsFrom(it.next()))
					it.remove();
			return super.add(candidate);
		}

		public List<T> toList() {
			return this.stream().collect(Collectors.toList());
		}
	}

}