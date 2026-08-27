package com.osrsdailytasks.boss.catalog;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Singleton;
import com.osrsdailytasks.boss.model.BossEfficiencyDefinition;
import com.osrsdailytasks.training.account.EhpProfile;

@Singleton
public class BossEfficiencyCatalog
{
	private final List<BossEfficiencyDefinition> definitions;
	private final Map<EhpProfile, Map<String, BossEfficiencyDefinition>> byProfileAndSubject;

	public BossEfficiencyCatalog()
	{
		this(WomEhbSnapshot.definitions());
	}

	BossEfficiencyCatalog(List<BossEfficiencyDefinition> definitions)
	{
		Objects.requireNonNull(definitions, "definitions");
		this.definitions = Collections.unmodifiableList(
			List.copyOf(definitions));
		this.byProfileAndSubject = index(this.definitions);
	}

	public List<BossEfficiencyDefinition> getDefinitions()
	{
		return definitions;
	}

	public Optional<BossEfficiencyDefinition> find(
		EhpProfile profile,
		String subjectId)
	{
		Objects.requireNonNull(profile, "profile");
		String normalizedSubject = normalizeSubjectId(subjectId);
		Map<String, BossEfficiencyDefinition> bySubject = byProfileAndSubject.get(profile);
		return bySubject == null
			? Optional.empty()
			: Optional.ofNullable(bySubject.get(normalizedSubject));
	}

	private static Map<EhpProfile, Map<String, BossEfficiencyDefinition>> index(
		List<BossEfficiencyDefinition> definitions)
	{
		Map<EhpProfile, Map<String, BossEfficiencyDefinition>> mutable =
			new EnumMap<>(EhpProfile.class);
		for (BossEfficiencyDefinition definition : definitions)
		{
			Objects.requireNonNull(definition, "definition");
			Map<String, BossEfficiencyDefinition> bySubject = mutable.computeIfAbsent(
				definition.getProfile(), ignored -> new HashMap<>());
			BossEfficiencyDefinition previous = bySubject.put(
				definition.getSubjectId(), definition);
			if (previous != null)
			{
				throw new IllegalArgumentException(
					"Duplicate boss efficiency definition: "
						+ definition.getProfile() + "/" + definition.getSubjectId());
			}
		}

		Map<EhpProfile, Map<String, BossEfficiencyDefinition>> immutable =
			new EnumMap<>(EhpProfile.class);
		for (Map.Entry<EhpProfile, Map<String, BossEfficiencyDefinition>> entry
			: mutable.entrySet())
		{
			immutable.put(entry.getKey(), Collections.unmodifiableMap(entry.getValue()));
		}
		return Collections.unmodifiableMap(immutable);
	}

	private static String normalizeSubjectId(String subjectId)
	{
		Objects.requireNonNull(subjectId, "subjectId");
		String normalized = subjectId.trim().toUpperCase(Locale.ENGLISH);
		if (normalized.isEmpty())
		{
			throw new IllegalArgumentException("subjectId must not be blank");
		}
		return normalized;
	}
}
