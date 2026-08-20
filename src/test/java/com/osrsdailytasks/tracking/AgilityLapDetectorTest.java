package com.osrsdailytasks.tracking;

import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AgilityLapDetectorTest
{
	private final AgilityLapDetector detector = new AgilityLapDetector();

	@Test
	public void detectsEveryRegisteredCourseEndpoint()
	{
		assertEquals(11, AgilityLapCourse.values().length);
		for (AgilityLapCourse expectedCourse : AgilityLapCourse.values())
		{
			for (WorldPoint endpoint : expectedCourse.getEndpoints())
			{
				assertEquals(
					expectedCourse,
					detector.findCompletion(Skill.AGILITY, endpoint).orElse(null));
			}
		}
	}

	@Test
	public void includesRooftopsApeAtollAndPrifddinas()
	{
		assertEquals(11, AgilityLapCourse.values().length);
		assertEquals(AgilityLapCourse.APE_ATOLL, detector.findCompletion(
			Skill.AGILITY,
			AgilityLapCourse.APE_ATOLL.getEndpoints()[0]).orElse(null));
		assertEquals(AgilityLapCourse.PRIFDDINAS, detector.findCompletion(
			Skill.AGILITY,
			AgilityLapCourse.PRIFDDINAS.getEndpoints()[0]).orElse(null));
	}

	@Test
	public void ignoresOtherSkillsLocationsAndNullPlayers()
	{
		WorldPoint draynorEndpoint = AgilityLapCourse.DRAYNOR.getEndpoints()[0];
		assertFalse(detector.findCompletion(Skill.MAGIC, draynorEndpoint).isPresent());
		assertFalse(detector.findCompletion(Skill.AGILITY, new WorldPoint(3102, 3261, 0)).isPresent());
		assertFalse(detector.findCompletion(Skill.AGILITY, null).isPresent());
	}

	@Test
	public void supportsEveryFaladorEndpoint()
	{
		assertEquals(4, AgilityLapCourse.FALADOR.getEndpoints().length);
	}
}
