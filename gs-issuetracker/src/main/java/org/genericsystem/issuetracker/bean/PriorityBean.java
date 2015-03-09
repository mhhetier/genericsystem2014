package org.genericsystem.issuetracker.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.genericsystem.issuetracker.model.Priority;
import org.genericsystem.issuetracker.qualifier.Provide;

@Named
@SessionScoped
public class PriorityBean implements Serializable {

	private static final long serialVersionUID = 3628359912273571503L;

	@Inject
	@Provide
	private transient Priority priority;

	public List<String> getPriorities() {
		return priority.getAllInstances().get().map(generic -> Objects.toString(generic.getValue())).collect(Collectors.toList());
	}

}