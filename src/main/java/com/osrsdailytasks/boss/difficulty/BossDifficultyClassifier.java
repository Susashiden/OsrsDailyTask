package com.osrsdailytasks.boss.difficulty;

import java.util.Optional;
import javax.inject.Singleton;
import com.osrsdailytasks.PvmDifficulty;
import net.runelite.client.hiscore.HiscoreSkill;

@Singleton
public class BossDifficultyClassifier
{
	public Optional<PvmDifficulty> classify(HiscoreSkill boss)
	{
		switch (boss)
		{
			case BARROWS_CHESTS:
			case BRYOPHYTA:
			case CHAOS_ELEMENTAL:
			case CHAOS_FANATIC:
			case CRAZY_ARCHAEOLOGIST:
			case DERANGED_ARCHAEOLOGIST:
			case GIANT_MOLE:
			case HESPORI:
			case KING_BLACK_DRAGON:
			case MIMIC:
			case OBOR:
			case SCURRIUS:
			case SKOTIZO:
			case TEMPOROSS:
			case WINTERTODT:
			case ZALCANO:
				return Optional.of(PvmDifficulty.LOW);
			case ABYSSAL_SIRE:
			case AMOXLIATL:
			case ARAXXOR:
			case ARTIO:
			case BRUTUS:
			case CALLISTO:
			case CALVARION:
			case CERBERUS:
			case COMMANDER_ZILYANA:
			case CORPOREAL_BEAST:
			case DAGANNOTH_PRIME:
			case DAGANNOTH_REX:
			case DAGANNOTH_SUPREME:
			case GENERAL_GRAARDOR:
			case GROTESQUE_GUARDIANS:
			case KALPHITE_QUEEN:
			case KRAKEN:
			case KREEARRA:
			case KRIL_TSUTSAROTH:
			case LUNAR_CHESTS:
			case MAD_ANGEL:
			case MAGGOT_KING:
			case PHANTOM_MUSPAH:
			case SARACHNIS:
			case SCORPIA:
			case SHELLBANE_GRYPHON:
			case SPINDEL:
			case THE_GAUNTLET:
			case THE_HUEYCOATL:
			case THE_ROYAL_TITANS:
			case THERMONUCLEAR_SMOKE_DEVIL:
			case VENENATIS:
			case VETION:
			case VORKATH:
			case ZULRAH:
				return Optional.of(PvmDifficulty.MEDIUM);
			case ALCHEMICAL_HYDRA:
			case CHAMBERS_OF_XERIC:
			case DUKE_SUCELLUS:
			case NEX:
			case NIGHTMARE:
			case THE_CORRUPTED_GAUNTLET:
			case THE_LEVIATHAN:
			case THE_WHISPERER:
			case TOMBS_OF_AMASCUT:
			case TZTOK_JAD:
			case VARDORVIS:
			case YAMA:
				return Optional.of(PvmDifficulty.HIGH);
			case CHAMBERS_OF_XERIC_CHALLENGE_MODE:
			case DOOM_OF_MOKHAIOTL:
			case SOL_HEREDIT:
			case THEATRE_OF_BLOOD:
			case THEATRE_OF_BLOOD_HARD_MODE:
			case TOMBS_OF_AMASCUT_EXPERT:
			case PHOSANIS_NIGHTMARE:
			case TZKAL_ZUK:
				return Optional.of(PvmDifficulty.ENDGAME);
			default:
				return Optional.empty();
		}
	}
}
