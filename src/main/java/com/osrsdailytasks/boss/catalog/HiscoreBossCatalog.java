package com.osrsdailytasks.boss.catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.osrsdailytasks.PvmDifficulty;
import com.osrsdailytasks.boss.difficulty.BossDifficultyClassifier;
import com.osrsdailytasks.boss.model.BossDefinition;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.hiscore.HiscoreSkillType;

@Singleton
public class HiscoreBossCatalog
{
	private final List<BossDefinition> definitions;

	@Inject
	public HiscoreBossCatalog(BossDifficultyClassifier classifier)
	{
		List<BossDefinition> bosses = new ArrayList<>();
		for (HiscoreSkill skill : HiscoreSkill.values())
		{
			if (skill.getType() != HiscoreSkillType.BOSS)
			{
				continue;
			}
			Optional<PvmDifficulty> difficulty = classifier.classify(skill);
			bosses.add(new BossDefinition(
				skill,
				difficulty.orElse(null),
				titleFor(skill),
				difficulty.isPresent() && skill != HiscoreSkill.DOOM_OF_MOKHAIOTL));
		}
		definitions = Collections.unmodifiableList(bosses);
	}

	public HiscoreBossCatalog()
	{
		this(new BossDifficultyClassifier());
	}

	public List<BossDefinition> getDefinitions()
	{
		return definitions;
	}

	public Optional<BossDefinition> findBySubjectId(String subjectId)
	{
		if (subjectId == null)
		{
			return Optional.empty();
		}
		for (BossDefinition definition : definitions)
		{
			if (definition.getSubjectId().equals(subjectId))
			{
				return Optional.of(definition);
			}
		}
		return Optional.empty();
	}

	public Optional<BossDefinition> findByDisplayName(String displayName)
	{
		String normalized = normalizeName(displayName);
		if ("barrows chest".equals(normalized))
		{
			normalized = "barrows chests";
		}
		for (BossDefinition definition : definitions)
		{
			if (normalizeName(definition.getDisplayName()).equals(normalized))
			{
				return Optional.of(definition);
			}
		}
		return Optional.empty();
	}

	private static String titleFor(HiscoreSkill skill)
	{
		switch (skill)
		{
			case BARROWS_CHESTS:
			case CHAMBERS_OF_XERIC:
			case CHAMBERS_OF_XERIC_CHALLENGE_MODE:
			case LUNAR_CHESTS:
			case TEMPOROSS:
			case THE_GAUNTLET:
			case THE_CORRUPTED_GAUNTLET:
			case THEATRE_OF_BLOOD:
			case THEATRE_OF_BLOOD_HARD_MODE:
			case TOMBS_OF_AMASCUT:
			case TOMBS_OF_AMASCUT_EXPERT:
			case WINTERTODT:
			case ZALCANO:
				return "Complete " + skill.getName();
			default:
				return "Defeat " + skill.getName();
		}
	}

	static String normalizeName(String value)
	{
		return value == null
			? ""
			: value.toLowerCase(Locale.ENGLISH)
				.replace("’", "'")
				.replace(":", "")
				.trim();
	}
}
