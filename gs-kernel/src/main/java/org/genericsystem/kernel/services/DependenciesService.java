package org.genericsystem.kernel.services;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.genericsystem.kernel.Dependencies.DependenciesEntry;
import org.genericsystem.kernel.Snapshot;
import org.genericsystem.kernel.Statics;

public interface DependenciesService<T extends DependenciesService<T>> extends AncestorsService<T>, ExceptionAdviserService<T> {

	Snapshot<T> getInstances();

	Snapshot<T> getInheritings();

	Snapshot<DependenciesEntry<T>> getMetaComposites();

	Snapshot<DependenciesEntry<T>> getSuperComposites();

	default boolean isAncestorOf(final T dependency) {
		return equiv(dependency) || (!dependency.equals(dependency.getMeta()) && isAncestorOf(dependency.getMeta())) || dependency.getSupersStream().anyMatch(component -> this.isAncestorOf(component))
				|| dependency.getComponentsStream().filter(component -> !dependency.equals(component)).anyMatch(component -> this.isAncestorOf(component))
				|| inheritsFrom(dependency.getMeta(), dependency.getValue(), dependency.getComponents(), getMeta(), getValue(), getComponents());
	}

	default LinkedHashSet<T> computeAllDependencies() {
		class DirectDependencies extends LinkedHashSet<T> {
			private static final long serialVersionUID = -5970021419012502402L;
			private final Set<T> alreadyVisited = new HashSet<>();

			public DirectDependencies() {
				visit(getMeta());
			}

			public void visit(T node) {
				if (!alreadyVisited.contains(node))
					if (!isAncestorOf(node)) {
						alreadyVisited.add(node);
						node.getComposites().forEach(this::visit);
						node.getInheritings().forEach(this::visit);
						node.getInstances().forEach(this::visit);
					} else
						addDependency(node);
			}

			public void addDependency(T node) {
				if (!alreadyVisited.contains(node)) {
					alreadyVisited.add(node);
					node.getComposites().forEach(this::addDependency);
					node.getInheritings().forEach(this::addDependency);
					node.getInstances().forEach(this::addDependency);
					super.add(node);
				}
			}
		}
		return new DirectDependencies();
	}

	default LinkedHashSet<T> computeAllDependencies(List<T> overrides, Serializable value, List<T> components) {
		class DirectDependencies extends LinkedHashSet<T> {
			private static final long serialVersionUID = -5970021419012502402L;
			private final Set<T> alreadyVisited = new HashSet<>();

			public DirectDependencies() {
				visit((T) this);
			}

			public void visit(T node) {
				if (!alreadyVisited.contains(node))
					if (!isAncestorOf(node)) {
						alreadyVisited.add(node);
						node.getComposites().forEach(this::visit);
						node.getInheritings().forEach(this::visit);
						node.getInstances().forEach(this::visit);
					} else
						addDependency(node);
			}

			public void addDependency(T node) {
				if (!alreadyVisited.contains(node)) {
					alreadyVisited.add(node);
					node.getComposites().forEach(this::addDependency);
					node.getInheritings().forEach(this::addDependency);
					node.getInstances().forEach(this::addDependency);
					super.add(node);
				}
			}
		}
		return new DirectDependencies();
	}

	default LinkedHashSet<T> computeDescendingDependencies() {
		class DirectDependencies extends LinkedHashSet<T> {
			private static final long serialVersionUID = -5970021419012502402L;
			private final Set<T> alreadyVisited = new HashSet<>();

			public DirectDependencies() {
				visit(getMeta());
			}

			public void visit(T node) {
				if (!alreadyVisited.contains(node))
					if (!isAncestorOf(node)) {
						alreadyVisited.add(node);
						node.getComposites().forEach(this::visit);
						node.getInheritings().forEach(this::visit);
						node.getInstances().forEach(this::visit);
					} else
						addDependency(node);
			}

			public void addDependency(T node) {
				if (!alreadyVisited.contains(node)) {
					alreadyVisited.add(node);
					node.getComposites().forEach(this::addDependency);
					node.getInheritings().forEach(this::addDependency);
					node.getInstances().forEach(this::addDependency);
					super.add(node);
				}
			}
		}
		return new DirectDependencies();
	}

	default Snapshot<T> getComposites() {
		return () -> Statics.concat(getMetaComposites().stream(), entry -> entry.getValue().stream()).iterator();
	}

	@SuppressWarnings("unchecked")
	default boolean isSuperOf(T subMeta, List<T> overrides, Serializable subValue, List<T> subComponents) {
		return overrides.stream().anyMatch(override -> override.inheritsFrom((T) this)) || inheritsFrom(subMeta, subValue, subComponents, getMeta(), getValue(), getComponents());
	}

	@SuppressWarnings("unchecked")
	default boolean isMetaOf(T subMeta, List<T> overrides, List<T> subComponents) {
		if (!subMeta.isSpecializationOf(getMeta()))
			return false;
		if (!subMeta.componentsDepends(subComponents, getComponents()))
			return false;
		return true;
	}

	default boolean inheritsFrom(T subMeta, Serializable subValue, List<T> subComponents, T superMeta, Serializable superValue, List<T> superComponents) {
		if (!subMeta.inheritsFrom(superMeta))
			return false;
		if (!subMeta.componentsDepends(subComponents, superComponents))
			return false;
		return subMeta.getValuesBiPredicate().test(subValue, superValue);
	}

	static interface SingularsLazyCache {
		boolean get(int i);
	}

	default boolean componentsDepends(List<T> subComponents, List<T> superComponents) {
		class SingularsLazyCacheImpl implements SingularsLazyCache {
			private final Boolean[] singulars = new Boolean[subComponents.size()];

			@Override
			public boolean get(int i) {
				return singulars[i] != null ? singulars[i] : (singulars[i] = DependenciesService.this.isSingularConstraintEnabled(i));
			}
		}
		return componentsDepends(new SingularsLazyCacheImpl(), subComponents, superComponents);
	}

	boolean isSingularConstraintEnabled(int pos);

	default boolean componentsDepends(SingularsLazyCache singulars, List<T> subComponents, List<T> superComponents) {
		int subIndex = 0;
		loop: for (T superComponent : superComponents) {
			for (; subIndex < subComponents.size(); subIndex++) {
				T subComponent = subComponents.get(subIndex);
				if (subComponent.isSpecializationOf(superComponent)) {
					if (singulars.get(subIndex))
						return true;
					subIndex++;
					continue loop;
				}
			}
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	default Stream<T> select() {
		return Stream.of((T) this);
	}

	default Stream<T> getAllInheritings() {
		return Stream.concat(select(), Statics.concat(getInheritings().stream(), inheriting -> inheriting.getAllInheritings()).distinct());
	}

	default Stream<T> getAllInstances() {
		return getAllInheritings().map(inheriting -> ((DependenciesService<T>) inheriting).getInstances().stream()).flatMap(x -> x);// .reduce(Stream.empty(), Stream::concat);
	}

	default Stream<T> selectInstances(Predicate<T> valuePredicate) {
		return getAllInstances().filter(valuePredicate);
	}

	default Stream<T> selectInstances(Serializable value) {
		return selectInstances(instance -> Objects.equals(value, instance.getValue()));
	}

	default Stream<T> selectInstances(Serializable value, T[] components) {
		return selectInstances(value, instance -> componentsDepends(Arrays.asList(components), instance.getComponents()));
	}

	default Stream<T> selectInstances(Serializable value, Predicate<T> componentsPredicate) {
		return selectInstances(value).filter(componentsPredicate);
	}

	@SuppressWarnings("unchecked")
	default Stream<T> selectInstances(Predicate<T> valuePredicate, T... components) {
		return selectInstances(valuePredicate, instance -> componentsDepends(Arrays.asList(components), instance.getComponents()));
	}

	default Stream<T> selectInstances(Predicate<T> valuePredicate, Predicate<T> componentsPredicate) {
		return selectInstances(valuePredicate).filter(componentsPredicate);
	}

	@SuppressWarnings("unchecked")
	default Stream<T> selectInstances(Predicate<T> supersPredicate, Serializable value, T... components) {
		return selectInstances(value, components).filter(supersPredicate);
	}

	default Stream<T> selectInstances(Predicate<T> supersPredicate, Serializable value, Predicate<T> componentsPredicate) {
		return selectInstances(value, componentsPredicate).filter(supersPredicate);
	}

	@SuppressWarnings("unchecked")
	default Stream<T> selectInstances(Predicate<T> supersPredicate, Predicate<T> valuePredicate, T... components) {
		return selectInstances(valuePredicate, components).filter(supersPredicate);
	}

	default Stream<T> selectInstances(Predicate<T> supersPredicate, Predicate<T> valuePredicate, Predicate<T> componentsPredicate) {
		return selectInstances(valuePredicate, componentsPredicate).filter(supersPredicate);
	}

	default Stream<T> selectInstances(Stream<T> supers, Serializable value, Predicate<T> componentsPredicate) {
		return selectInstances(instance -> supers.allMatch(superT -> instance.inheritsFrom(superT)), value, componentsPredicate);
	}

	@SuppressWarnings("unchecked")
	default Stream<T> selectInstances(Stream<T> supers, Predicate<T> valuePredicate, T... components) {
		return selectInstances((Predicate<T>) (instance -> supers.allMatch(superT -> instance.inheritsFrom(superT))), valuePredicate, components);
	}

	default Stream<T> selectInstances(Stream<T> supers, Predicate<T> valuePredicate, Predicate<T> componentsPredicate) {
		return selectInstances((Predicate<T>) (instance -> supers.allMatch(superT -> instance.inheritsFrom(superT))), valuePredicate, componentsPredicate);
	}

}
