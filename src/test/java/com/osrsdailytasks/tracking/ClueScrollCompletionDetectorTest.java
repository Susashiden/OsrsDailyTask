package com.osrsdailytasks.tracking;

import com.osrsdailytasks.model.ClueTier;
import net.runelite.api.ChatMessageType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ClueScrollCompletionDetectorTest
{
	private final ClueScrollCompletionDetector detector = new ClueScrollCompletionDetector();

	@Test
	public void detectsEverySupportedTier()
	{
		for (ClueTier tier : ClueTier.values())
		{
			assertEquals(
				tier,
				detector.findCompletedTier(
					ChatMessageType.GAMEMESSAGE,
					"You have completed 3 " + tier.getMessageName() + " Treasure Trails.")
					.orElse(null));
		}
	}

	@Test
	public void handlesSingularSpamAndFormatting()
	{
		assertEquals(
			ClueTier.MASTER,
			detector.findCompletedTier(
				ChatMessageType.SPAM,
				"<col=ff0000>You have completed 1 master Treasure Trail.</col>")
				.orElse(null));
	}

	@Test
	public void ignoresUnknownTiersOtherMessagesAndMessageTypes()
	{
		assertFalse(detector.findCompletedTier(
			ChatMessageType.GAMEMESSAGE,
			"You have completed 3 legendary Treasure Trails.").isPresent());
		assertFalse(detector.findCompletedTier(
			ChatMessageType.PUBLICCHAT,
			"You have completed 3 hard Treasure Trails.").isPresent());
		assertFalse(detector.findCompletedTier(
			ChatMessageType.GAMEMESSAGE,
			"You have a funny feeling like you would have been followed.").isPresent());
	}
}
