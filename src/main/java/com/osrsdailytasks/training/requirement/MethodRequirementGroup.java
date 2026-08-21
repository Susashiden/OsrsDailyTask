package com.osrsdailytasks.training.requirement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class MethodRequirementGroup implements MethodRequirementNode
{
	public enum MatchMode
	{
		ALL_OF,
		ANY_OF
	}

	private final MatchMode matchMode;
	private final List<MethodRequirementNode> children;

	public MethodRequirementGroup(MatchMode matchMode, List<MethodRequirementNode> children)
	{
		this.matchMode = Objects.requireNonNull(matchMode, "matchMode");
		Objects.requireNonNull(children, "children");
		if (children.isEmpty())
		{
			throw new IllegalArgumentException("requirement group must not be empty");
		}
		this.children = Collections.unmodifiableList(new ArrayList<>(children));
		if (this.children.contains(null))
		{
			throw new NullPointerException("requirement group child");
		}
	}

	public MatchMode getMatchMode()
	{
		return matchMode;
	}

	public List<MethodRequirementNode> getChildren()
	{
		return children;
	}
}
