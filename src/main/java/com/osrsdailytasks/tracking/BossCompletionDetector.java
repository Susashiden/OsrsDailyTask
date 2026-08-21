package com.osrsdailytasks.tracking;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.osrsdailytasks.boss.catalog.HiscoreBossCatalog;
import net.runelite.api.ChatMessageType;
import net.runelite.client.util.Text;

@Singleton
public class BossCompletionDetector
{
	private static final Pattern KILL_COUNT_PATTERN = Pattern.compile(
		"Your (?<pre>completion count for |subdued |completed )?"
			+ "(?<boss>.+?) "
			+ "(?<post>(?:(?:kill|harvest|lap|completion|success|Total Ticket) )?(?:count )?)"
			+ "is: ?(?<kc>[0-9,]+)");

	private final HiscoreBossCatalog bossCatalog;

	@Inject
	public BossCompletionDetector(HiscoreBossCatalog bossCatalog)
	{
		this.bossCatalog = bossCatalog;
	}

	public Optional<BossCompletion> findCompletion(ChatMessageType type, String message)
	{
		if (type != ChatMessageType.GAMEMESSAGE && type != ChatMessageType.SPAM || message == null)
		{
			return Optional.empty();
		}

		Matcher matcher = KILL_COUNT_PATTERN.matcher(Text.removeTags(message));
		if (!matcher.find())
		{
			return Optional.empty();
		}

		return bossCatalog.findByDisplayName(matcher.group("boss"))
			.map(boss -> new BossCompletion(
				boss.getSubjectId(),
				Integer.parseInt(matcher.group("kc").replace(",", ""))));
	}
}
