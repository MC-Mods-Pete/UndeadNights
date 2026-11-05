package net.petemc.undeadnights.util;

import java.util.Random;

public class RandomExtention extends Random {
    public int nextBetween(int min, int max) {
        return this.nextInt(max - min + 1) + min;
    }

    public int nextIntBetweenInclusive (int min, int max) {
        return this.nextInt(max - min + 1) + min;
    }
}
