package org.genericsystem.defaults.constraints;

import java.io.Serializable;

import org.genericsystem.api.exception.ConstraintViolationException;
import org.genericsystem.defaults.DefaultVertex;
import org.genericsystem.defaults.constraints.Constraint.AxedCheckedConstraint;
import org.genericsystem.defaults.exceptions.SingularConstraintViolationException;

/**
 * Represents the constraint to allow only one value for a relation.
 * 
 * @author Nicolas Feybesse
 *
 * @param <T>
 *            the implementation of DefaultVertex.
 */
public class SingularConstraint<T extends DefaultVertex<T>> implements AxedCheckedConstraint<T> {
	@Override
	public void check(T modified, T attribute, Serializable value, int axe, boolean isRevert) throws ConstraintViolationException {
		T base = isRevert ? modified : modified.getComponents().get(axe);
		if (base.getHolders(attribute).size() > 1)
			throw new SingularConstraintViolationException(base + " has more than one link : " + base.getHolders(attribute).info() + " for attribute : " + attribute);
	}
}
