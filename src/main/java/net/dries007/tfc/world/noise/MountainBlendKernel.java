package net.dries007.tfc.world.noise;

import su.terrafirmagreg.core.world.WorldgenData;

public class MountainBlendKernel {
	public static volatile Kernel KERNEL = rebuild(WorldgenData.MOUNTAIN_SCALING.heightBlend());

	public static Kernel rebuild(int radius)
	{
		final double radiusSq = (double) radius * radius;
		final int size = radius * 2 + 1;
		final double[] raw = new double[size * size];
		double sum = 0;
		for (int x = 0; x < size; x++)
		{
			for (int z = 0; z < size; z++)
			{
				final int dx = x - radius;
				final int dz = z - radius;
				final double v = Math.max(0, 1.0 - (dx * dx + dz * dz) / radiusSq);
				raw[x + z * size] = v;
				sum += v;
			}
		}

		final double invSum = 1.0 / sum;
		final int capturedRadius = radius;
		return Kernel.create((x, z) -> {
			final int ix = x + capturedRadius;
			final int iz = z + capturedRadius;
			return raw[ix + iz * size] * invSum;
		}, capturedRadius);
	}
}
