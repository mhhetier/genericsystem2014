package org.genericsystem.kernel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.genericsystem.api.core.Snapshot;
import org.genericsystem.api.defaults.DefaultContext;
import org.genericsystem.api.defaults.DefaultRoot;
import org.genericsystem.api.defaults.DefaultVertex;
import org.genericsystem.api.exception.ExistsException;
import org.genericsystem.api.exception.ReferentialIntegrityConstraintViolationException;
import org.genericsystem.api.exception.UnreachableOverridesException;

public abstract class Context<T extends DefaultVertex<T>> implements DefaultContext<T> {

	private final DefaultRoot<T> root;

	private final Checker<T> checker;

	protected Builder<T> builder;

	protected Context(DefaultRoot<T> root) {
		this.root = root;
		this.checker = buildChecker();
		this.builder = buildBuilder();
	}

	public abstract long getTs();

	protected Checker<T> buildChecker() {
		return new Checker<>(this);
	}

	protected abstract Builder<T> buildBuilder();

	@Override
	public Checker<T> getChecker() {
		return checker;
	}

	@Override
	public Builder<T> getBuilder() {
		return builder;
	}

	// @Override
	@Override
	public DefaultRoot<T> getRoot() {
		return root;
	}

	@Override
	public boolean isAlive(T vertex) {
		return vertex != null && vertex.equals(getAlive(vertex));
	}

	abstract protected T plug(T generic);

	abstract protected void unplug(T generic);

	@Override
	public void forceRemove(T generic) {
		computeDependencies(generic, true).forEach(this::unplug);
	}

	@Override
	public void remove(T generic) {
		computeDependencies(generic, false).forEach(this::unplug);
	}

	@Deprecated
	public// TODO to remove
	Set<T> computeDependencies(T node) {
		return computeDependencies(node, true);
	}

	Set<T> computeDependencies(T node, boolean force) {
		return new OrderedDependencies(force).visit(node);
	}

	class OrderedDependencies extends LinkedHashSet<T> {
		private static final long serialVersionUID = -5970021419012502402L;

		private final boolean force;

		public OrderedDependencies(boolean force) {
			this.force = force;
		}

		OrderedDependencies visit(T node) {
			if (!contains(node)) {
				if (!force && !node.getInheritings().isEmpty())
					discardWithException(new ReferentialIntegrityConstraintViolationException("Ancestor : " + node + " has a inheriting dependencies : " + node.getInheritings()));
				getInheritings(node).forEach(this::visit);

				if (!force && !node.getInstances().isEmpty())
					discardWithException(new ReferentialIntegrityConstraintViolationException("Ancestor : " + node + " has a instance dependencies : " + node.getInstances()));
				getInstances(node).forEach(this::visit);

				for (T composite : node.getComposites()) {
					if (!force)
						for (int componentPos = 0; componentPos < composite.getComponents().size(); componentPos++)
							if (composite.getComponents().get(componentPos).equals(node) && !contains(composite) && composite.getMeta().isReferentialIntegrityEnabled(componentPos))
								discardWithException(new ReferentialIntegrityConstraintViolationException(composite + " is Referential Integrity for ancestor " + node + " by composite position : " + componentPos));
					visit(composite);
				}
				add(node);
				for (int axe = 0; axe < node.getComponents().size(); axe++)
					if (node.isCascadeRemove(axe))
						visit(node.getComponents().get(axe));
			}
			return this;
		}
	}

	private T getAlive(T vertex) {
		if (vertex.isRoot())
			return vertex;
		if (vertex.isMeta()) {
			T aliveSuper = getAlive(vertex.getSupers().get(0));
			return aliveSuper != null ? getInheritings(aliveSuper).get(vertex) : null;
		}
		T aliveMeta = getAlive(vertex.getMeta());
		return aliveMeta != null ? getInstances(aliveMeta).get(vertex) : null;
	}

	@SuppressWarnings("unchecked")
	protected T getMeta(int dim) {
		T adjustedMeta = getBuilder().adjustMeta((T) getRoot(), dim);
		return adjustedMeta != null && adjustedMeta.getComponents().size() == dim ? adjustedMeta : null;
	}

	@Override
	public abstract Snapshot<T> getInstances(T vertex);

	@Override
	public abstract Snapshot<T> getInheritings(T vertex);

	@Override
	public abstract Snapshot<T> getComposites(T vertex);

	protected void triggersMutation(T oldDependency, T newDependency) {
	}

	public Set<T> computePotentialDependencies(T meta, List<T> supers, Serializable value, List<T> components) {
		return new PotentialDependenciesComputer() {
			private static final long serialVersionUID = -3611136800445783634L;

			@Override
			boolean isSelected(T node) {
				return node.isDependencyOf(meta, supers, value, components);
			}
		}.visit(meta);
	}

	abstract class PotentialDependenciesComputer extends LinkedHashSet<T> {
		private static final long serialVersionUID = -5970021419012502402L;
		private final Set<T> alreadyVisited = new HashSet<>();

		abstract boolean isSelected(T node);

		PotentialDependenciesComputer visit(T node) {
			if (!alreadyVisited.contains(node))
				if (isSelected(node))
					addDependency(node);
				else {
					alreadyVisited.add(node);
					node.getComposites().forEach(this::visit);
					node.getInheritings().forEach(this::visit);
					node.getInstances().forEach(this::visit);
				}
			return this;
		}

		private void addDependency(T node) {
			if (!alreadyVisited.contains(node)) {
				alreadyVisited.add(node);
				node.getComposites().forEach(this::addDependency);
				node.getInheritings().forEach(this::addDependency);
				node.getInstances().forEach(this::addDependency);
				super.add(node);
			}
		}
	}

	protected static class AbstractVertexBuilder<T extends AbstractVertex<T>> extends Builder<T> {
		protected AbstractVertexBuilder(Context<T> context) {
			super(context);
		}

		@Override
		public T setInstance(Class<?> clazz, T meta, List<T> overrides, Serializable value, List<T> components) {
			GenericBuilder<T> genericBuilder = new GenericBuilder<T>(this, clazz, meta, overrides, value, components);
			genericBuilder.check();
			genericBuilder.adjustMeta();
			genericBuilder.reComputeSupers();
			T generic = genericBuilder.get();
			if (generic != null)
				return generic;
			generic = genericBuilder.getEquiv();
			return generic == null ? genericBuilder.add() : genericBuilder.update(generic);

			// getContext().getChecker().checkBeforeBuild(clazz, meta, overrides, value, components);
			// T adjustedMeta = meta.isMeta() ? setMeta(components.size()) : meta.adjustMeta(value, components);
			// List<T> supers = computeAndCheckOverridesAreReached(adjustedMeta, overrides, value, components);
			// if (supers.size() == 1 && supers.get(0).equalsRegardlessSupers(adjustedMeta, value, components)) {
			// if (Statics.areOverridesReached(supers.get(0).getSupers(), overrides))
			// return supers.get(0);
			// }
			//
			// T equivInstance = adjustedMeta.getDirectEquivInstance(value, components);
			// if (equivInstance == null)
			// return internalAddInstance(clazz, adjustedMeta, supers, value, components);
			// return rebuildAll(equivInstance, () -> build(clazz, adjustedMeta, supers, value, components), getContext().computeDependencies(equivInstance, true));
		}

		@Override
		public T update(T update, List<T> overrides, Serializable newValue, List<T> newComponents) {

			GenericBuilder<T> genericBuilder = new GenericBuilder<T>(this, null, update.getMeta(), overrides, newValue, newComponents);
			genericBuilder.check();
			genericBuilder.adjustMeta();
			genericBuilder.reComputeSupers();
			return genericBuilder.update2(update);

			// getContext().getChecker().checkBeforeBuild(update.getClass(), update.getMeta(), overrides, newValue, newComponents);
			// T adjustedMeta = update.getMeta().isMeta() ? setMeta(newComponents.size()) : update.getMeta().adjustMeta(newValue, newComponents);
			// List<T> supers = computeAndCheckOverridesAreReached(adjustedMeta, overrides, newValue, newComponents);
			//
			// if (supers.size() == 1 && supers.get(0).equalsRegardlessSupers(adjustedMeta, newValue, newComponents))
			// if (Statics.areOverridesReached(supers.get(0).getSupers(), overrides))
			// supers = supers.get(0).getSupers();
			// else
			// assert false;
			// //
			// List<T> filterSupers = supers;
			//
			// return rebuildAll(update, () -> getOrBuild(update.getClass(), adjustedMeta, filterSupers.stream().filter(x -> !x.equals(update)).collect(Collectors.toList()), newValue, newComponents), getContext().computeDependencies(update, true));
		}

		@Override
		public T addInstance(Class<?> clazz, T meta, List<T> overrides, Serializable value, List<T> components) {
			GenericBuilder<T> genericBuilder = new GenericBuilder<T>(this, clazz, meta, overrides, value, components);
			genericBuilder.check();
			genericBuilder.adjustMeta();
			genericBuilder.reComputeSupers();
			T generic = genericBuilder.get();
			if (generic != null)
				getContext().discardWithException(new ExistsException("An equivalent instance already exists : " + generic.info()));
			return genericBuilder.add();
			// getContext().getChecker().checkBeforeBuild(clazz, meta, overrides, value, components);
			// T adjustedMeta = meta.isMeta() ? setMeta(components.size()) : meta.adjustMeta(value, components);
			// List<T> supers = computeAndCheckOverridesAreReached(adjustedMeta, overrides, value, components);
			// if (supers.size() == 1 && supers.get(0).equalsRegardlessSupers(adjustedMeta, value, components))
			// getContext().discardWithException(new ExistsException("An equivalent instance already exists : " + supers.get(0).info()));
			// return internalAddInstance(clazz, adjustedMeta, supers, value, components);
		}

		private T internalAddInstance(Class<?> clazz, T adjustedMeta, List<T> supers, Serializable value, List<T> components) {
			return rebuildAll(null, () -> build(clazz, adjustedMeta, supers, value, components), getContext().computePotentialDependencies(adjustedMeta, supers, value, components));
		}

		private class ConvertMap extends HashMap<T, T> {
			private static final long serialVersionUID = 5003546962293036021L;

			private T convert(T oldDependency) {
				if (oldDependency.isAlive())
					return oldDependency;
				T newDependency = get(oldDependency);
				if (newDependency == null) {
					if (oldDependency.isMeta()) {
						assert oldDependency.getSupers().size() == 1;
						newDependency = setMeta(oldDependency.getComponents().size());
					} else {
						List<T> overrides = oldDependency.getSupers().stream().map(x -> convert(x)).collect(Collectors.toList());
						List<T> components = oldDependency.getComponents().stream().map(x -> x != null ? convert(x) : null).collect(Collectors.toList());
						T adjustedMeta = convert(oldDependency.getMeta()).adjustMeta(oldDependency.getValue(), components);
						List<T> supers = computeAndCheckOverridesAreReached(adjustedMeta, overrides, oldDependency.getValue(), components);
						// TODO KK designTs
						newDependency = getOrBuild(oldDependency.getClass(), adjustedMeta, supers, oldDependency.getValue(), components);
					}
					put(oldDependency, newDependency);// triggers mutation
				}
				return newDependency;
			}

			@Override
			public T put(T oldDependency, T newDependency) {
				T result = super.put(oldDependency, newDependency);
				getContext().triggersMutation(oldDependency, newDependency);
				return result;
			}
		}

		@Override
		T rebuildAll(T toRebuild, Supplier<T> rebuilder, Set<T> dependenciesToRebuild) {
			dependenciesToRebuild.forEach(getContext()::unplug);
			T build = rebuilder.get();
			dependenciesToRebuild.remove(toRebuild);
			ConvertMap convertMap = new ConvertMap();
			if (toRebuild != null) {
				convertMap.put(toRebuild, build);
				getContext().triggersMutation(toRebuild, build);
			}
			Statics.reverseCollections(dependenciesToRebuild).forEach(x -> convertMap.convert(x));
			return build;
		}

		@Override
		List<T> computeAndCheckOverridesAreReached(T adjustedMeta, List<T> overrides, Serializable value, List<T> components) {
			List<T> supers = new ArrayList<>(new SupersComputer<>(adjustedMeta, overrides, value, components));
			if (!Statics.areOverridesReached(supers, overrides))
				getContext().discardWithException(new UnreachableOverridesException("Unable to reach overrides : " + overrides + " with computed supers : " + supers));
			return supers;
		}

		@Override
		protected T newT(Class<?> clazz, T meta, List<T> supers, Serializable value, List<T> components) {
			return newT(clazz, meta).init(meta, supers, value, components);
		}

		@Override
		protected T getOrBuild(Class<?> clazz, T meta, List<T> supers, Serializable value, List<T> components) {
			T instance = meta == null ? getContext().getMeta(components.size()) : meta.getDirectInstance(value, components);
			return instance == null ? build(clazz, meta, supers, value, components) : instance;
		}
	}

}
