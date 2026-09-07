package com.osrsdailytasks.boss.target;

import java.math.BigDecimal;
import java.util.Objects;
import javax.inject.Singleton;
import com.osrsdailytasks.TaskDifficulty;
import com.osrsdailytasks.boss.model.BossEfficiencyDefinition;

@Singleton
public class BossTargetCalculator
{
	private static final BigDecimal MINIMUM_NORMAL_FACTOR = new BigDecimal("0.1875");
	private static final BigDecimal MAXIMUM_NORMAL_FACTOR = new BigDecimal("0.3125");

	public BossTargetRange calculate(
		BossEfficiencyDefinition definition,
		TaskDifficulty difficulty)
	{
		Objects.requireNonNull(definition, "definition");
		return calculateForRate(definition.getKillsPerHour(), difficulty);
	}

	public BossTargetRange calculateForRate(
		BigDecimal killsPerHour,
		TaskDifficulty difficulty)
	{
		Objects.requireNonNull(killsPerHour, "killsPerHour");
		Objects.requireNonNull(difficulty, "difficulty");
		if (killsPerHour.compareTo(BigDecimal.ZERO) <= 0)
		{
			throw new IllegalArgumentException("killsPerHour must be positive");
		}

		int minimum = difficulty.scaleTarget(
			killsPerHour.multiply(MINIMUM_NORMAL_FACTOR));
		int maximum = difficulty.scaleTarget(
			killsPerHour.multiply(MAXIMUM_NORMAL_FACTOR));
		return new BossTargetRange(minimum, Math.max(minimum, maximum));
	}
}
