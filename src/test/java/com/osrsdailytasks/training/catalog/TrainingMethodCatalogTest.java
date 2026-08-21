package com.osrsdailytasks.training.catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.osrsdailytasks.training.account.EhpProfile;
import com.osrsdailytasks.training.model.TrainingMethod;
import com.osrsdailytasks.training.model.TrainingMethodDefinition;
import com.osrsdailytasks.training.model.TrainingMethodSourceType;
import com.osrsdailytasks.training.requirement.MethodRequirement;
import com.osrsdailytasks.training.requirement.MethodRequirementClassification;
import com.osrsdailytasks.training.requirement.MethodRequirementGroup;
import com.osrsdailytasks.training.requirement.MethodRequirementNode;
import com.osrsdailytasks.training.requirement.MethodRequirementType;
import net.runelite.api.Skill;
import org.junit.Test;

public class TrainingMethodCatalogTest
{
	private final TrainingMethodCatalog catalog = new TrainingMethodCatalog();

	@Test
	public void containsExpectedNormalizedMethodCountAndUniqueIds()
	{
		assertEquals(415, catalog.getMethods().size());
		Set<String> ids = new HashSet<>();
		for (TrainingMethod method : catalog.getMethods())
		{
			assertTrue(ids.add(method.getMethodId()));
			assertEquals(method, catalog.findByMethodId(method.getMethodId()).orElse(null));
			assertTrue(method.getEndXp() >= method.getStartXp());
			assertNotNull(method.getRequirements());
		}
	}

	@Test
	public void cachesTheCompleteReviewedWomSnapshot()
	{
		int[] expectedRows = {146, 120, 129};
		int[] expectedSkills = {21, 22, 22};
		for (int profileIndex = 0; profileIndex < EhpProfile.values().length; profileIndex++)
		{
			EhpProfile profile = EhpProfile.values()[profileIndex];
			int definitions = 0;
			Set<String> skills = new HashSet<>();
			for (TrainingMethodDefinition definition : WomEhpSnapshot.definitions())
			{
				if (definition.getProfile() == profile)
				{
					definitions++;
					skills.add(definition.getSkillId());
				}
			}
			assertEquals(expectedRows[profileIndex], definitions);
			assertEquals(expectedSkills[profileIndex], skills.size());
		}
	}

	@Test
	public void everyProfileHasAPositiveMethodForEveryRealSkill()
	{
		for (EhpProfile profile : EhpProfile.values())
		{
			for (Skill skill : Skill.values())
			{
				if (!"OVERALL".equals(skill.name()))
				{
					TrainingMethod terminal = catalog.findTerminal(profile, skill.name()).orElse(null);
					assertNotNull(profile + " " + skill.name(), terminal);
					assertTrue(terminal.getEhpRate() > 0);
				}
			}
		}
	}

	@Test
	public void valeTotemsReplaceWomFletchingAcrossEveryProfile()
	{
		double[] rates = {22_500, 55_000, 87_500, 140_000, 265_000, 325_000};
		long[] startXp = {4_470, 22_406, 101_333, 449_428, 1_986_068, 5_346_332};
		for (EhpProfile profile : EhpProfile.values())
		{
			List<TrainingMethod> methods = catalog.findMethods(profile, "FLETCHING");
			assertEquals(6, methods.size());
			for (int index = 0; index < methods.size(); index++)
			{
				TrainingMethod method = methods.get(index);
				assertEquals(startXp[index], method.getStartXp());
				assertEquals(rates[index], method.getEhpRate(), 0.0);
				assertEquals(TrainingMethodSourceType.WIKI, method.getSource().getType());
				assertTrue(method.getTrainingMethod().contains("Vale Totems"));
				assertFalse(method.getTrainingMethod().toLowerCase().contains("javelin"));
			}
		}
	}

	@Test
	public void providesReviewedMagicAndHitpointsFallbacks()
	{
		double[] hitpointsRates = {395_675, 335_160, 258_020};
		for (int index = 0; index < EhpProfile.values().length; index++)
		{
			EhpProfile profile = EhpProfile.values()[index];
			TrainingMethod magic = catalog.findTerminal(profile, "MAGIC").orElse(null);
			TrainingMethod hitpoints = catalog.findTerminal(profile, "HITPOINTS").orElse(null);
			assertNotNull(magic);
			assertNotNull(hitpoints);
			assertEquals(414_000, magic.getEhpRate(), 0.0);
			assertEquals(TrainingMethodSourceType.WIKI, magic.getSource().getType());
			assertEquals(hitpointsRates[index], hitpoints.getEhpRate(), 0.0);
			assertEquals(TrainingMethodSourceType.DERIVED, hitpoints.getSource().getType());
			assertEquals("actual Ranged XP/h * (1.33 / 4)",
				hitpoints.getSource().getDerivation());
		}
	}

	@Test
	public void fallbackRequirementsRemainStructuredAndClassified()
	{
		TrainingMethod magic = catalog.findTerminal(EhpProfile.MAIN, "MAGIC").orElse(null);
		TrainingMethod fletching = catalog.findTerminal(EhpProfile.MAIN, "FLETCHING").orElse(null);
		TrainingMethod ironHitpoints = catalog.findTerminal(EhpProfile.IRONMAN, "HITPOINTS").orElse(null);
		assertNotNull(magic);
		assertNotNull(fletching);
		assertNotNull(ironHitpoints);

		assertTrue(hasRequirement(magic, MethodRequirementType.SKILL_LEVEL,
			"MAGIC", 71, MethodRequirementClassification.REQUIRED));
		assertTrue(hasRequirement(magic, MethodRequirementType.QUEST_COMPLETION,
			"LUNAR_DIPLOMACY", 1, MethodRequirementClassification.REQUIRED));
		assertTrue(hasRequirement(magic, MethodRequirementType.CUSTOM_UNLOCK,
			"LUNAR_SPELLBOOK", 1, MethodRequirementClassification.REQUIRED));
		assertTrue(hasRequirement(fletching, MethodRequirementType.MINIGAME_UNLOCK,
			"VALE_TOTEMS", 1, MethodRequirementClassification.REQUIRED));
		assertTrue(hasRequirement(fletching, MethodRequirementType.SKILL_LEVEL,
			"FLETCHING", 90, MethodRequirementClassification.REQUIRED));
		assertTrue(hasRequirement(ironHitpoints, MethodRequirementType.QUEST_STAGE,
			"MONKEY_MADNESS_II_KRUKS_DUNGEON", 1, MethodRequirementClassification.REQUIRED));
		assertTrue(hasRequirement(ironHitpoints, MethodRequirementType.SKILL_LEVEL,
			"HUNTER", 73, MethodRequirementClassification.RATE_REQUIRED));
		assertTrue(hasRequirement(ironHitpoints, MethodRequirementType.ITEM_UNLOCK,
			"CHINCHOMPAS", 1, MethodRequirementClassification.SUPPLY));
	}

	@Test
	public void preservesWomEffectiveAndActualRates()
	{
		TrainingMethod method = catalog.findTerminal(EhpProfile.MAIN, "FIREMAKING").orElse(null);
		assertNotNull(method);
		assertEquals(789_944, method.getEhpRate(), 0.0);
		assertEquals(302_000, method.getActualSkillRate(), 0.0);
		assertEquals(TrainingMethodSourceType.WISE_OLD_MAN, method.getSource().getType());
		assertEquals(WomEhpSnapshot.SNAPSHOT_VERSION, method.getSource().getVersion());
	}

	@Test
	public void selectsBandsByXpAndDerivesTheirEndXp()
	{
		List<TrainingMethod> methods = catalog.findMethods(EhpProfile.MAIN, "FLETCHING");
		assertFalse(catalog.findForXp(EhpProfile.MAIN, "FLETCHING", 4_469).isPresent());
		assertEquals(methods.get(0).getMethodId(),
			catalog.findForXp(EhpProfile.MAIN, "FLETCHING", 4_470).get().getMethodId());
		assertEquals(22_405, methods.get(0).getEndXp());
		assertEquals(methods.get(5).getMethodId(),
			catalog.findForXp(EhpProfile.MAIN, "FLETCHING", 200_000_000).get().getMethodId());
	}

	@Test
	public void returnedCollectionsAreImmutable()
	{
		try
		{
			catalog.getMethods().clear();
			fail("Expected immutable catalog");
		}
		catch (UnsupportedOperationException expected)
		{
			// Expected.
		}
		try
		{
			catalog.findMethods(EhpProfile.MAIN, "MAGIC").clear();
			fail("Expected immutable skill methods");
		}
		catch (UnsupportedOperationException expected)
		{
			// Expected.
		}
	}

	private static boolean hasRequirement(
		TrainingMethod method,
		MethodRequirementType type,
		String subjectId,
		int requiredValue,
		MethodRequirementClassification classification)
	{
		return hasRequirement(method.getRequirements(), type, subjectId, requiredValue, classification);
	}

	private static boolean hasRequirement(
		MethodRequirementNode node,
		MethodRequirementType type,
		String subjectId,
		int requiredValue,
		MethodRequirementClassification classification)
	{
		if (node instanceof MethodRequirement)
		{
			MethodRequirement requirement = (MethodRequirement) node;
			return requirement.getType() == type
				&& requirement.getSubjectId().equals(subjectId)
				&& requirement.getRequiredValue() == requiredValue
				&& requirement.getClassification() == classification;
		}
		for (MethodRequirementNode child : ((MethodRequirementGroup) node).getChildren())
		{
			if (hasRequirement(child, type, subjectId, requiredValue, classification))
			{
				return true;
			}
		}
		return false;
	}
}
