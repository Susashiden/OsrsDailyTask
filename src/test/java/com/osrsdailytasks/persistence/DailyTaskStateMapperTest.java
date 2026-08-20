package com.osrsdailytasks.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import com.osrsdailytasks.model.ActiveTask;
import com.osrsdailytasks.model.TaskType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DailyTaskStateMapperTest
{
	private final DailyTaskStateMapper mapper = new DailyTaskStateMapper();

	@Test
	public void roundTripsCompleteState()
	{
		ActiveTask expected = new ActiveTask(
			LocalDate.of(2026, 8, 18),
			"xp-agility",
			TaskType.XP,
			"AGILITY",
			"Gain Agility XP",
			10_000,
			10_000,
			2_000_000,
			Instant.parse("2026-08-18T10:15:30Z"),
			1);

		Optional<ActiveTask> restored = mapper.fromMap(mapper.toMap(expected));

		assertTrue(restored.isPresent());
		assertEquals(expected, restored.get());
	}

	@Test
	public void roundTripsOptionalValuesWhenAbsent()
	{
		ActiveTask expected = new ActiveTask(
			LocalDate.of(2026, 8, 18),
			"boss-giant-mole",
			TaskType.BOSS,
			"GIANT_MOLE",
			"Defeat the Giant Mole",
			2,
			0,
			null,
			null,
			0);

		Optional<ActiveTask> restored = mapper.fromMap(mapper.toMap(expected));

		assertTrue(restored.isPresent());
		assertEquals(expected, restored.get());
	}

	@Test
	public void rejectsMalformedOrIncompleteState()
	{
		Map<String, String> state = new LinkedHashMap<>();
		state.put(
			DailyTaskStateMapper.KEY_SCHEMA_VERSION,
			Integer.toString(DailyTaskStateMapper.CURRENT_SCHEMA_VERSION));
		state.put(DailyTaskStateMapper.KEY_GENERATION_DATE, "not-a-date");

		assertFalse(mapper.fromMap(state).isPresent());
	}

	@Test
	public void rejectsUnsupportedOrInProgressSchemas()
	{
		Map<String, String> state = mapper.toMap(new ActiveTask(
			LocalDate.of(2026, 8, 18),
			"activity-agility-laps",
			TaskType.ACTIVITY,
			"AGILITY_LAPS",
			"Complete agility laps",
			10,
			0,
			null,
			null,
			0));

		state.put(DailyTaskStateMapper.KEY_SCHEMA_VERSION, "0");
		assertFalse(mapper.fromMap(state).isPresent());

		state.put(DailyTaskStateMapper.KEY_SCHEMA_VERSION, "1");
		assertFalse(mapper.fromMap(state).isPresent());

		state.put(DailyTaskStateMapper.KEY_SCHEMA_VERSION, "99");
		assertFalse(mapper.fromMap(state).isPresent());
	}
}
