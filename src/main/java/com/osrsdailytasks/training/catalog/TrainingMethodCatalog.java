package com.osrsdailytasks.training.catalog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.inject.Singleton;
import com.osrsdailytasks.training.account.EhpProfile;
import com.osrsdailytasks.training.model.TrainingMethod;
import com.osrsdailytasks.training.model.TrainingMethodDefinition;
import com.osrsdailytasks.training.model.TrainingMethodSource;
import com.osrsdailytasks.training.model.TrainingMethodSourceType;
import com.osrsdailytasks.training.requirement.MethodRequirement;
import com.osrsdailytasks.training.requirement.MethodRequirementClassification;
import com.osrsdailytasks.training.requirement.MethodRequirementComparison;
import com.osrsdailytasks.training.requirement.MethodRequirementGroup;
import com.osrsdailytasks.training.requirement.MethodRequirementNode;
import com.osrsdailytasks.training.requirement.MethodRequirementType;
import net.runelite.api.Experience;
import net.runelite.api.Skill;

@Singleton
public class TrainingMethodCatalog
{
	private static final long MAX_SKILL_XP = 200_000_000L;
	private static final String WIKI_VERSION = "2026-08-21";

	private static final TrainingMethodSource MAGIC_SOURCE = new TrainingMethodSource(
		TrainingMethodSourceType.WIKI,
		"https://oldschool.runescape.wiki/w/Pay-to-play_Magic_training",
		WIKI_VERSION,
		null);
	private static final TrainingMethodSource FLETCHING_SOURCE = new TrainingMethodSource(
		TrainingMethodSourceType.WIKI,
		"https://oldschool.runescape.wiki/w/Vale_Totems/Strategies",
		WIKI_VERSION,
		null);
	private static final TrainingMethodSource HITPOINTS_SOURCE = new TrainingMethodSource(
		TrainingMethodSourceType.DERIVED,
		"https://oldschool.runescape.wiki/w/Pay-to-play_Hitpoints_training"
			+ " + Wise Old Man Ranged terminal rates",
		WIKI_VERSION,
		"actual Ranged XP/h * (1.33 / 4)");

	private final List<TrainingMethod> methods;
	private final Map<EhpProfile, Map<String, List<TrainingMethod>>> methodsByProfileAndSkill;
	private final Map<String, TrainingMethod> methodsById;

	public TrainingMethodCatalog()
	{
		this(createDefinitions());
	}

	TrainingMethodCatalog(List<TrainingMethodDefinition> definitions)
	{
		Objects.requireNonNull(definitions, "definitions");
		this.methods = Collections.unmodifiableList(normalize(definitions));
		this.methodsByProfileAndSkill = index(methods);
		this.methodsById = indexById(methods);
		validateCompleteCoverage();
	}

	public List<TrainingMethod> getMethods()
	{
		return methods;
	}

	public List<TrainingMethod> findMethods(EhpProfile profile, String skillId)
	{
		Objects.requireNonNull(profile, "profile");
		String normalizedSkill = normalizeSkillId(skillId);
		Map<String, List<TrainingMethod>> bySkill = methodsByProfileAndSkill.get(profile);
		if (bySkill == null)
		{
			return Collections.emptyList();
		}
		return bySkill.getOrDefault(normalizedSkill, Collections.emptyList());
	}

	public Optional<TrainingMethod> findByMethodId(String methodId)
	{
		Objects.requireNonNull(methodId, "methodId");
		return Optional.ofNullable(methodsById.get(methodId));
	}

	public Optional<TrainingMethod> findForXp(EhpProfile profile, String skillId, long currentXp)
	{
		if (currentXp < 0)
		{
			throw new IllegalArgumentException("currentXp must not be negative");
		}
		TrainingMethod selected = null;
		for (TrainingMethod method : findMethods(profile, skillId))
		{
			if (method.getStartXp() <= currentXp && method.getEhpRate() > 0)
			{
				selected = method;
			}
		}
		return Optional.ofNullable(selected);
	}

	public Optional<TrainingMethod> findTerminal(EhpProfile profile, String skillId)
	{
		List<TrainingMethod> candidates = findMethods(profile, skillId);
		for (int index = candidates.size() - 1; index >= 0; index--)
		{
			TrainingMethod method = candidates.get(index);
			if (method.getEhpRate() > 0)
			{
				return Optional.of(method);
			}
		}
		return Optional.empty();
	}

	private static List<TrainingMethodDefinition> createDefinitions()
	{
		List<TrainingMethodDefinition> definitions = new ArrayList<>();
		for (TrainingMethodDefinition definition : WomEhpSnapshot.definitions())
		{
			if (!"FLETCHING".equals(definition.getSkillId()))
			{
				definitions.add(definition);
			}
		}
		for (EhpProfile profile : EhpProfile.values())
		{
			definitions.add(magicDefinition(profile));
			definitions.add(hitpointsDefinition(profile));
			addValeTotemDefinitions(definitions, profile);
		}
		return definitions;
	}

	private static TrainingMethodDefinition magicDefinition(EhpProfile profile)
	{
		return wikiDefinition(
			"wiki-" + profileId(profile) + "-magic-cure-me-araxytes",
			profile,
			"MAGIC",
			814_445L,
			"Cure Me on Araxytes",
			414_000,
			MAGIC_SOURCE,
			allOf(
				requirement(MethodRequirementType.QUEST_COMPLETION, "LUNAR_DIPLOMACY", 1,
					MethodRequirementClassification.REQUIRED),
				requirement(MethodRequirementType.QUEST_COMPLETION, "PRIEST_IN_PERIL", 1,
					MethodRequirementClassification.REQUIRED),
				requirement(MethodRequirementType.CUSTOM_UNLOCK, "LUNAR_SPELLBOOK", 1,
					MethodRequirementClassification.REQUIRED),
				requirement(MethodRequirementType.SKILL_LEVEL, "PRAYER", 43,
					MethodRequirementClassification.RECOMMENDED),
				requirement(MethodRequirementType.ITEM_UNLOCK, "ASTRAL_LAW_COSMIC_RUNES", 1,
					MethodRequirementClassification.SUPPLY)));
	}

	private static TrainingMethodDefinition hitpointsDefinition(EhpProfile profile)
	{
		double rate;
		String description;
		int hunterLevel;
		switch (profile)
		{
			case MAIN:
				rate = 395_675;
				description = "Black chinchompas at maniacal monkeys";
				hunterLevel = 0;
				break;
			case IRONMAN:
				rate = 335_160;
				description = "Black chinchompas at maniacal monkeys";
				hunterLevel = 73;
				break;
			case ULTIMATE:
				rate = 258_020;
				description = "Chinchompas at maniacal monkeys";
				hunterLevel = 0;
				break;
			default:
				throw new IllegalArgumentException("Unsupported EHP profile: " + profile);
		}

		List<MethodRequirementNode> requirements = new ArrayList<>();
		requirements.add(requirement(MethodRequirementType.QUEST_STAGE,
			"MONKEY_MADNESS_II_KRUKS_DUNGEON", 1, MethodRequirementClassification.REQUIRED));
		requirements.add(requirement(MethodRequirementType.SKILL_LEVEL,
			"RANGED", 65, MethodRequirementClassification.REQUIRED));
		requirements.add(requirement(MethodRequirementType.SKILL_LEVEL,
			"PRAYER", 43, MethodRequirementClassification.REQUIRED));
		requirements.add(requirement(MethodRequirementType.ITEM_UNLOCK,
			"CHINCHOMPAS", 1, MethodRequirementClassification.SUPPLY));
		requirements.add(requirement(MethodRequirementType.ITEM_UNLOCK,
			"LIGHT_SOURCE", 1, MethodRequirementClassification.SUPPLY));
		if (hunterLevel > 0)
		{
			requirements.add(requirement(MethodRequirementType.SKILL_LEVEL,
				"HUNTER", hunterLevel, MethodRequirementClassification.RATE_REQUIRED));
		}

		return wikiDefinition(
			"derived-" + profileId(profile) + "-hitpoints-chinning-maniacal-monkeys",
			profile,
			"HITPOINTS",
			0,
			description,
			rate,
			HITPOINTS_SOURCE,
			new MethodRequirementGroup(MethodRequirementGroup.MatchMode.ALL_OF, requirements));
	}

	private static void addValeTotemDefinitions(
		List<TrainingMethodDefinition> definitions,
		EhpProfile profile)
	{
		addValeTotemDefinition(definitions, profile, "oak", 4_470L, 22_500);
		addValeTotemDefinition(definitions, profile, "willow", 22_406L, 55_000);
		addValeTotemDefinition(definitions, profile, "maple", 101_333L, 87_500);
		addValeTotemDefinition(definitions, profile, "yew", 449_428L, 140_000);
		addValeTotemDefinition(definitions, profile, "magic", 1_986_068L, 265_000);
		addValeTotemDefinition(definitions, profile, "redwood", 5_346_332L, 325_000);
	}

	private static void addValeTotemDefinition(
		List<TrainingMethodDefinition> definitions,
		EhpProfile profile,
		String logType,
		long startXp,
		double rate)
	{
		String upperLogType = logType.toUpperCase(Locale.ENGLISH);
		definitions.add(wikiDefinition(
			"wiki-" + profileId(profile) + "-fletching-vale-totems-" + logType,
			profile,
			"FLETCHING",
			startXp,
			upperLogType.charAt(0) + logType.substring(1) + " Vale Totems",
			rate,
			FLETCHING_SOURCE,
			allOf(
				requirement(MethodRequirementType.MINIGAME_UNLOCK, "VALE_TOTEMS", 1,
					MethodRequirementClassification.REQUIRED),
				requirement(MethodRequirementType.ITEM_UNLOCK, "KNIFE", 1,
					MethodRequirementClassification.SUPPLY),
				requirement(MethodRequirementType.ITEM_UNLOCK, upperLogType + "_LOGS_OR_AXE", 1,
					MethodRequirementClassification.SUPPLY))));
	}

	private static TrainingMethodDefinition wikiDefinition(
		String methodId,
		EhpProfile profile,
		String skillId,
		long startXp,
		String description,
		double rate,
		TrainingMethodSource source,
		MethodRequirementNode requirements)
	{
		return new TrainingMethodDefinition(
			methodId,
			profile,
			skillId,
			startXp,
			description,
			rate,
			rate,
			source,
			requirements);
	}

	private static List<TrainingMethod> normalize(List<TrainingMethodDefinition> definitions)
	{
		Map<String, List<TrainingMethodDefinition>> grouped = new LinkedHashMap<>();
		Set<String> methodIds = new HashSet<>();
		Set<String> bandKeys = new HashSet<>();
		for (TrainingMethodDefinition definition : definitions)
		{
			String skillId = normalizeSkillId(definition.getSkillId());
			if (!isSupportedSkill(skillId))
			{
				continue;
			}
			String methodId = definition.getMethodId();
			if (!methodIds.add(methodId))
			{
				throw new IllegalArgumentException("Duplicate training method ID: " + methodId);
			}
			String bandKey = definition.getProfile() + ":" + skillId + ":" + definition.getStartXp();
			if (!bandKeys.add(bandKey))
			{
				throw new IllegalArgumentException("Duplicate training method band: " + bandKey);
			}
			grouped.computeIfAbsent(definition.getProfile() + ":" + skillId, ignored -> new ArrayList<>())
				.add(definition);
		}

		List<TrainingMethod> normalized = new ArrayList<>();
		for (List<TrainingMethodDefinition> bandDefinitions : grouped.values())
		{
			bandDefinitions.sort(Comparator.comparingLong(TrainingMethodDefinition::getStartXp));
			for (int index = 0; index < bandDefinitions.size(); index++)
			{
				TrainingMethodDefinition definition = bandDefinitions.get(index);
				long endXp = index + 1 < bandDefinitions.size()
					? bandDefinitions.get(index + 1).getStartXp() - 1
					: MAX_SKILL_XP;
				normalized.add(new TrainingMethod(
					definition.getMethodId(),
					definition.getProfile(),
					normalizeSkillId(definition.getSkillId()),
					definition.getStartXp(),
					endXp,
					definition.getDescription(),
					definition.getEhpRate(),
					definition.getActualSkillRate(),
					definition.getSource(),
					withSkillLevelRequirement(definition)));
			}
		}
		return normalized;
	}

	private static MethodRequirementNode withSkillLevelRequirement(TrainingMethodDefinition definition)
	{
		int minimumLevel = Experience.getLevelForXp(
			(int) Math.min(definition.getStartXp(), Integer.MAX_VALUE));
		MethodRequirement levelRequirement = requirement(
			MethodRequirementType.SKILL_LEVEL,
			normalizeSkillId(definition.getSkillId()),
			minimumLevel,
			MethodRequirementClassification.REQUIRED);
		if (definition.getExtraRequirements() == null)
		{
			return allOf(levelRequirement);
		}
		return allOf(levelRequirement, definition.getExtraRequirements());
	}

	private static Map<EhpProfile, Map<String, List<TrainingMethod>>> index(
		List<TrainingMethod> sourceMethods)
	{
		Map<EhpProfile, Map<String, List<TrainingMethod>>> index = new EnumMap<>(EhpProfile.class);
		for (TrainingMethod method : sourceMethods)
		{
			index.computeIfAbsent(method.getEhpProfile(), ignored -> new HashMap<>())
				.computeIfAbsent(method.getSkillId(), ignored -> new ArrayList<>())
				.add(method);
		}
		for (Map<String, List<TrainingMethod>> bySkill : index.values())
		{
			for (Map.Entry<String, List<TrainingMethod>> entry : bySkill.entrySet())
			{
				entry.setValue(Collections.unmodifiableList(entry.getValue()));
			}
		}
		return index;
	}

	private static Map<String, TrainingMethod> indexById(List<TrainingMethod> sourceMethods)
	{
		Map<String, TrainingMethod> index = new HashMap<>();
		for (TrainingMethod method : sourceMethods)
		{
			index.put(method.getMethodId(), method);
		}
		return Collections.unmodifiableMap(index);
	}

	private void validateCompleteCoverage()
	{
		for (EhpProfile profile : EhpProfile.values())
		{
			for (Skill skill : Skill.values())
			{
				if ("OVERALL".equals(skill.name()))
				{
					continue;
				}
				if (!findTerminal(profile, skill.name()).isPresent())
				{
					throw new IllegalStateException(
						"No positive training method for " + profile + " " + skill.name());
				}
			}
		}
	}

	private static MethodRequirement requirement(
		MethodRequirementType type,
		String subjectId,
		int requiredValue,
		MethodRequirementClassification classification)
	{
		return new MethodRequirement(
			type,
			subjectId,
			MethodRequirementComparison.AT_LEAST,
			requiredValue,
			classification);
	}

	private static MethodRequirementGroup allOf(MethodRequirementNode... requirements)
	{
		return new MethodRequirementGroup(
			MethodRequirementGroup.MatchMode.ALL_OF,
			Arrays.asList(requirements));
	}

	private static String normalizeSkillId(String skillId)
	{
		Objects.requireNonNull(skillId, "skillId");
		String normalized = skillId.trim().toUpperCase(Locale.ENGLISH);
		if (normalized.isEmpty())
		{
			throw new IllegalArgumentException("skillId must not be blank");
		}
		if ("RUNECRAFTING".equals(normalized))
		{
			return "RUNECRAFT";
		}
		return normalized;
	}

	private static String profileId(EhpProfile profile)
	{
		return profile.name().toLowerCase(Locale.ENGLISH);
	}

	private static boolean isSupportedSkill(String skillId)
	{
		if ("OVERALL".equals(skillId))
		{
			return false;
		}
		try
		{
			Skill.valueOf(skillId);
			return true;
		}
		catch (IllegalArgumentException exception)
		{
			return false;
		}
	}
}
