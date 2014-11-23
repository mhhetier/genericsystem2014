package org.genericsystem.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class Archiver<T extends AbstractVertex<T>> extends org.genericsystem.kernel.Archiver<T> {

	public Archiver(T root, String directoryPath) {
		this(root, new ZipFileManager(), directoryPath);
	}

	public Archiver(T root, FileManager fileManager, String directoryPath) {
		super(new WriterLoaderManager<T>(root, fileManager), directoryPath);
	}

	public static class WriterLoaderManager<T extends AbstractVertex<T>> extends org.genericsystem.kernel.Archiver.WriterLoaderManager<T> {

		public WriterLoaderManager(T root, FileManager fileManager) {
			super(root, fileManager);
		}

		@Override
		protected List<T> getOrderedVertex() {
			return new ArrayList<T>(new DependenciesOrder<T>().visit(root));
		}

		public static class DependenciesOrder<T extends AbstractVertex<T>> extends TreeSet<T> {
			private static final long serialVersionUID = -5970021419012502402L;

			public DependenciesOrder<T> visit(T node) {
				if (!contains(node)) {
					node.getCompositesDependencies().forEach(this::visit);
					node.getInheritingsDependencies().forEach(this::visit);
					node.getInstancesDependencies().forEach(this::visit);
					if (!node.isRoot())
						add(node);
				}
				return this;
			}
		}

		@Override
		protected long pickNewTs() {
			return ((Root) root).pickNewTs();
		}

		@Override
		protected long getTs(T dependency) {
			return dependency.getLifeManager().getDesignTs();
		}

	}
}