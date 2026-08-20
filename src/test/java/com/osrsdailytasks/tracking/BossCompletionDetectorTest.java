package com.osrsdailytasks.tracking;

import com.osrsdailytasks.service.BossDefinition;
import com.osrsdailytasks.service.HiscoreBossCatalog;
import net.runelite.api.ChatMessageType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BossCompletionDetectorTest
{
	private final HiscoreBossCatalog catalog = new HiscoreBossCatalog();
	private final BossCompletionDetector detector = new BossCompletionDetector(catalog);

	@Test
	public void recognizesEveryCatalogDisplayName()
	{
		for (BossDefinition boss : catalog.getDefinitions())
		{
			String message = "Your " + boss.getDisplayName()
				+ " kill count is: <col=ff0000>1</col>";
			BossCompletion completion = detector.findCompletion(
				ChatMessageType.GAMEMESSAGE,
				message).get();
			assertEquals(boss.getSubjectId(), completion.getSubjectId());
			assertEquals(1, completion.getKillCount());
		}
	}

	@Test
	public void recognizesCompletionWordingAliasesAndFormattedCounts()
	{
		BossCompletion barrows = detector.findCompletion(
			ChatMessageType.SPAM,
			"Your completion count for Barrows chest is: <col=ff0000>1,234</col>").get();
		assertEquals("BARROWS_CHESTS", barrows.getSubjectId());
		assertEquals(1_234, barrows.getKillCount());
	}

	@Test
	public void ignoresUnrelatedMessagesAndTypes()
	{
		assertFalse(detector.findCompletion(ChatMessageType.GAMEMESSAGE, "Nothing interesting happens.").isPresent());
		assertFalse(detector.findCompletion(
			ChatMessageType.PUBLICCHAT,
			"Your Vorkath kill count is: 1").isPresent());
		assertFalse(detector.findCompletion(
			ChatMessageType.GAMEMESSAGE,
			"Your Imaginary Boss kill count is: 1").isPresent());
		assertTrue(detector.findCompletion(
			ChatMessageType.GAMEMESSAGE,
			"Your Vorkath kill count is: 1").isPresent());
	}
}
