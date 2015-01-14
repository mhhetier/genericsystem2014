package org.genericsystem.kernel.systemproperty.constraints;

import java.io.Serializable;
import org.genericsystem.api.core.IVertex.SystemProperty;
import org.genericsystem.api.defaults.DefaultVertex;
import org.genericsystem.api.exception.ConstraintViolationException;

public interface Constraint<T extends DefaultVertex<T>> extends SystemProperty {

	void check(T modified, T constraintBase, Serializable value, int axe, boolean isOnAdd, boolean isFlushTime, boolean isRevert) throws ConstraintViolationException;

	public static interface AxedCheckedConstraint<T extends DefaultVertex<T>> extends Constraint<T> {

		void check(T modified, T constraintBase, Serializable value, int axe, boolean isRevert) throws ConstraintViolationException;

		@Override
		default void check(T modified, T constraintBase, Serializable value, int axe, boolean isOnAdd, boolean isFlushTime, boolean isRevert) throws ConstraintViolationException {
			check(modified, constraintBase, value, axe, isRevert);
		}
	}

	public static interface CheckedConstraint<T extends DefaultVertex<T>> extends AxedCheckedConstraint<T> {

		void check(T modified, T constraintBase, Serializable value) throws ConstraintViolationException;

		@Override
		default void check(T modified, T constraintBase, Serializable value, int axe, boolean isRevert) throws ConstraintViolationException {
			check(modified, constraintBase, value);
		}
	}

	public static interface AxedCheckableConstraint<T extends DefaultVertex<T>> extends Constraint<T> {

		void check(T modified, T constraintBase, Serializable value, int axe, boolean isRevert) throws ConstraintViolationException;

		boolean isCheckable(T modified, boolean isOnAdd, boolean isFlushTime, boolean isRevert);

		@Override
		default void check(T modified, T constraintBase, Serializable value, int axe, boolean isOnAdd, boolean isFlushTime, boolean isRevert) throws ConstraintViolationException {
			if (isCheckable(modified, isOnAdd, isFlushTime, isRevert))
				check(modified, constraintBase, value, axe, isRevert);
		}

	}

	public static interface CheckableConstraint<T extends DefaultVertex<T>> extends AxedCheckableConstraint<T> {

		void check(T modified, T constraintBase, Serializable value) throws ConstraintViolationException;

		@Override
		default void check(T modified, T constraintBase, Serializable value, int axe, boolean isRevert) throws ConstraintViolationException {
			check(modified, constraintBase, value);
		}

	}
}