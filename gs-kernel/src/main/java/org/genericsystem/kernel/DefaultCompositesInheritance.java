package org.genericsystem.kernel;

import java.io.Serializable;
import org.genericsystem.api.core.IVertex;
import org.genericsystem.api.core.Snapshot;

public interface DefaultCompositesInheritance<T extends AbstractVertex<T>> extends IVertex<T> {

	@SuppressWarnings("unchecked")
	@Override
	default Snapshot<T> getAttributes(T attribute) {
		return ((T) this).getInheritings(attribute, Statics.STRUCTURAL);
	}

	@Override
	default Snapshot<T> getAttributes() {
		return getAttributes(getRoot().getMetaAttribute());
	}

	@SuppressWarnings("unchecked")
	@Override
	default Snapshot<T> getAttributes(int pos) {
		return () -> getAttributes().get().filter(attribute -> pos >= 0 && pos < attribute.getComponents().size() && ((T) this).isSpecializationOf(attribute.getComponents().get(pos)));
	}

	@SuppressWarnings("unchecked")
	@Override
	default Snapshot<T> getHolders(T attribute) {
		return ((T) this).getInheritings(attribute, Statics.CONCRETE);
	}

	@SuppressWarnings("unchecked")
	@Override
	default Snapshot<T> getHolders(T attribute, int pos) {
		return () -> getHolders(attribute).get().filter(holder -> pos >= 0 && pos < holder.getComponents().size() && ((T) this).isSpecializationOf(holder.getComponents().get(pos)));

	}

	@Override
	default Snapshot<Serializable> getValues(T attribute) {
		return () -> getHolders(attribute).get().map(T::getValue);
	}

	@Override
	default Snapshot<Serializable> getValues(T attribute, int pos) {
		return () -> getHolders(attribute, pos).get().map(T::getValue);
	}
}