package com.osrsdailytasks.tracking;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Singleton;
import com.osrsdailytasks.model.ClueTier;
import net.runelite.api.ChatMessageType;
import net.runelite.client.util.Text;

@Singleton
public class ClueScrollCompletionDetector
{
	private static final Pattern COMPLETION_PATTERN = Pattern.compile(
		"^You have completed [0-9]+ (beginner|easy|medium|hard|elite|master) Treasure Trails?\\.$");

	public Optional<ClueTier> findCompletedTier(ChatMessageType type, String message)
	{
		if (type != ChatMessageType.GAMEMESSAGE && type != ChatMessageType.SPAM)
		{
			return Optional.empty();
		}

		Matcher matcher = COMPLETION_PATTERN.matcher(Text.removeTags(message));
		return matcher.matches()
			? ClueTier.fromMessageName(matcher.group(1))
			: Optional.empty();
	}
}
