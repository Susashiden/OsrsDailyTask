package com.osrsdailytasks.tracking;

import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.osrsdailytasks.service.HiscoreBossCatalog;
import net.runelite.api.gameval.NpcID;

@Singleton
public class BossTaskMatcher
{
	private final HiscoreBossCatalog bossCatalog;

	@Inject
	public BossTaskMatcher(HiscoreBossCatalog bossCatalog)
	{
		this.bossCatalog = bossCatalog;
	}

	public BossTaskMatcher()
	{
		this(new HiscoreBossCatalog());
	}

	public Optional<String> subjectForNpc(int npcId)
	{
		if (npcId == NpcID.MOLE_GIANT)
		{
			return Optional.of("GIANT_MOLE");
		}
		return Optional.empty();
	}

	public Optional<String> subjectForNpcName(String npcName)
	{
		return bossCatalog.findByDisplayName(npcName).map(boss -> boss.getSubjectId());
	}
}
