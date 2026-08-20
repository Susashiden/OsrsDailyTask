package com.osrsdailytasks.tracking;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Singleton;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;

@Singleton
public class AgilityLapDetector
{
	public static final String SUBJECT_ID = "AGILITY_COURSE_LAPS";

	private static final Map<Integer, AgilityLapCourse> COURSES_BY_REGION = coursesByRegion();

	public Optional<AgilityLapCourse> findCompletion(Skill changedSkill, WorldPoint playerLocation)
	{
		if (changedSkill != Skill.AGILITY || playerLocation == null)
		{
			return Optional.empty();
		}

		AgilityLapCourse course = COURSES_BY_REGION.get(playerLocation.getRegionID());
		if (course == null || !course.endsAt(playerLocation))
		{
			return Optional.empty();
		}

		return Optional.of(course);
	}

	private static Map<Integer, AgilityLapCourse> coursesByRegion()
	{
		Map<Integer, AgilityLapCourse> courses = new HashMap<>();
		for (AgilityLapCourse course : AgilityLapCourse.values())
		{
			if (courses.put(course.getRegionId(), course) != null)
			{
				throw new IllegalStateException("Duplicate Agility course region: " + course.getRegionId());
			}
		}
		return Collections.unmodifiableMap(courses);
	}
}
