package com.osrsdailytasks.training.requirement;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;

public class DefaultMethodEligibilityEvaluatorTest
{
	private final DefaultMethodEligibilityEvaluator evaluator = new DefaultMethodEligibilityEvaluator();

	@Test
	public void allOfLocksOnAnyLockedRequirementAndPreservesUnknown()
	{
		MethodRequirement available = requirement("AVAILABLE", MethodRequirementClassification.REQUIRED);
		MethodRequirement unknown = requirement("UNKNOWN", MethodRequirementClassification.REQUIRED);
		MethodRequirement locked = requirement("LOCKED", MethodRequirementClassification.REQUIRED);

		assertEquals(MethodEligibility.UNKNOWN, evaluator.evaluate(
			group(MethodRequirementGroup.MatchMode.ALL_OF, available, unknown),
			this::status));
		assertEquals(MethodEligibility.LOCKED, evaluator.evaluate(
			group(MethodRequirementGroup.MatchMode.ALL_OF, available, unknown, locked),
			this::status));
	}

	@Test
	public void anyOfAcceptsAnyAvailableRequirementAndPreservesUnknown()
	{
		MethodRequirement available = requirement("AVAILABLE", MethodRequirementClassification.REQUIRED);
		MethodRequirement unknown = requirement("UNKNOWN", MethodRequirementClassification.REQUIRED);
		MethodRequirement locked = requirement("LOCKED", MethodRequirementClassification.REQUIRED);

		assertEquals(MethodEligibility.AVAILABLE, evaluator.evaluate(
			group(MethodRequirementGroup.MatchMode.ANY_OF, locked, available),
			this::status));
		assertEquals(MethodEligibility.UNKNOWN, evaluator.evaluate(
			group(MethodRequirementGroup.MatchMode.ANY_OF, locked, unknown),
			this::status));
	}

	@Test
	public void recommendationsAndSuppliesDoNotLockMethod()
	{
		assertEquals(MethodEligibility.AVAILABLE, evaluator.evaluate(
			requirement("LOCKED", MethodRequirementClassification.RECOMMENDED),
			this::status));
		assertEquals(MethodEligibility.AVAILABLE, evaluator.evaluate(
			requirement("LOCKED", MethodRequirementClassification.SUPPLY),
			this::status));
	}

	@Test
	public void evaluatesQuestDiaryAndRateSpecificRequirementsThroughTheProvider()
	{
		MethodRequirement quest = new MethodRequirement(
			MethodRequirementType.QUEST_COMPLETION,
			"QUEST",
			MethodRequirementComparison.EQUALS,
			1,
			MethodRequirementClassification.REQUIRED);
		MethodRequirement diary = new MethodRequirement(
			MethodRequirementType.DIARY_TIER,
			"DIARY",
			MethodRequirementComparison.AT_LEAST,
			3,
			MethodRequirementClassification.RATE_REQUIRED);

		assertEquals(MethodEligibility.LOCKED, evaluator.evaluate(
			group(MethodRequirementGroup.MatchMode.ALL_OF, quest, diary),
			requirement -> "QUEST".equals(requirement.getSubjectId())
				? MethodEligibility.AVAILABLE
				: MethodEligibility.LOCKED));
	}

	private MethodEligibility status(MethodRequirement requirement)
	{
		return MethodEligibility.valueOf(requirement.getSubjectId());
	}

	private static MethodRequirement requirement(
		String subjectId,
		MethodRequirementClassification classification)
	{
		return new MethodRequirement(
			MethodRequirementType.CUSTOM_UNLOCK,
			subjectId,
			MethodRequirementComparison.EQUALS,
			1,
			classification);
	}

	private static MethodRequirementGroup group(
		MethodRequirementGroup.MatchMode mode,
		MethodRequirementNode... requirements)
	{
		return new MethodRequirementGroup(mode, Arrays.asList(requirements));
	}
}
