package com.osrsdailytasks.training.target;

import static org.junit.Assert.assertEquals;

import com.osrsdailytasks.TaskDifficulty;
import com.osrsdailytasks.training.account.EhpProfile;
import com.osrsdailytasks.training.catalog.TrainingMethodCatalog;
import org.junit.Test;

public class XpTargetCalculatorTest
{
	private final TrainingMethodCatalog catalog = new TrainingMethodCatalog();

	@Test
	public void calculatesMagicRangesForEveryDifficulty()
	{
		XpTargetCalculator calculator = new XpTargetCalculator(catalog, EhpProfile.MAIN);
		assertRange(calculator.calculateCurrent("MAGIC", TaskDifficulty.EASY), 77_625, 129_375);
		assertRange(calculator.calculateCurrent("MAGIC", TaskDifficulty.NORMAL), 155_250, 258_750);
		assertRange(calculator.calculateCurrent("MAGIC", TaskDifficulty.HARD), 310_500, 517_500);
	}

	@Test
	public void calculatesTerminalValeTotemsRangesAcrossProfiles()
	{
		for (EhpProfile profile : EhpProfile.values())
		{
			XpTargetCalculator calculator = new XpTargetCalculator(catalog, profile);
			assertRange(calculator.calculateCurrent("FLETCHING", TaskDifficulty.EASY),
				60_937, 101_562);
			assertRange(calculator.calculateCurrent("FLETCHING", TaskDifficulty.NORMAL),
				121_875, 203_125);
			assertRange(calculator.calculateCurrent("FLETCHING", TaskDifficulty.HARD),
				243_750, 406_250);
		}
	}

	@Test
	public void calculatesEveryValeTotemsBandWithFloorRounding()
	{
		XpTargetCalculator calculator = new XpTargetCalculator(catalog, EhpProfile.MAIN);
		double[] rates = {22_500, 55_000, 87_500, 140_000, 265_000, 325_000};
		int[][] easy = {
			{4_218, 7_031}, {10_312, 17_187}, {16_406, 27_343},
			{26_250, 43_750}, {49_687, 82_812}, {60_937, 101_562}
		};
		int[][] normal = {
			{8_437, 14_062}, {20_625, 34_375}, {32_812, 54_687},
			{52_500, 87_500}, {99_375, 165_625}, {121_875, 203_125}
		};
		int[][] hard = {
			{16_875, 28_125}, {41_250, 68_750}, {65_625, 109_375},
			{105_000, 175_000}, {198_750, 331_250}, {243_750, 406_250}
		};
		for (int index = 0; index < rates.length; index++)
		{
			assertRange(calculator.calculateForRate(rates[index], TaskDifficulty.EASY),
				easy[index][0], easy[index][1]);
			assertRange(calculator.calculateForRate(rates[index], TaskDifficulty.NORMAL),
				normal[index][0], normal[index][1]);
			assertRange(calculator.calculateForRate(rates[index], TaskDifficulty.HARD),
				hard[index][0], hard[index][1]);
		}
	}

	@Test
	public void calculatesProfileSpecificHitpointsRangesWithFloorRounding()
	{
		int[][] easyRanges = {
			{74_189, 123_648},
			{62_842, 104_737},
			{48_378, 80_631}
		};
		int[][] normalRanges = {
			{148_378, 247_296},
			{125_685, 209_475},
			{96_757, 161_262}
		};
		int[][] hardRanges = {
			{296_756, 494_593},
			{251_370, 418_950},
			{193_515, 322_525}
		};
		for (int index = 0; index < EhpProfile.values().length; index++)
		{
			XpTargetCalculator calculator = new XpTargetCalculator(catalog, EhpProfile.values()[index]);
			assertRange(calculator.calculateCurrent("HITPOINTS", TaskDifficulty.EASY),
				easyRanges[index][0], easyRanges[index][1]);
			assertRange(calculator.calculateCurrent("HITPOINTS", TaskDifficulty.NORMAL),
				normalRanges[index][0], normalRanges[index][1]);
			assertRange(calculator.calculateCurrent("HITPOINTS", TaskDifficulty.HARD),
				hardRanges[index][0], hardRanges[index][1]);
		}
	}

	@Test
	public void clampsVeryLargeCalculatedRangesToIntegerMaximum()
	{
		XpTargetCalculator calculator = new XpTargetCalculator(catalog, EhpProfile.MAIN);
		assertRange(
			calculator.calculateForRate(Double.MAX_VALUE, TaskDifficulty.HARD),
			Integer.MAX_VALUE,
			Integer.MAX_VALUE);
	}

	@Test
	public void clampsVerySmallCalculatedRangesToOne()
	{
		XpTargetCalculator calculator = new XpTargetCalculator(catalog, EhpProfile.MAIN);
		assertRange(
			calculator.calculateForRate(0.1, TaskDifficulty.EASY),
			1,
			1);
	}

	private static void assertRange(XpTargetRange range, int minimum, int maximum)
	{
		assertEquals(minimum, range.getMinimum());
		assertEquals(maximum, range.getMaximum());
	}
}
