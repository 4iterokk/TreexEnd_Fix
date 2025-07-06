package me.jetby.treexend.tools.storage;

import java.util.List;

public record EggPrices(List<Integer> slots,
                        List<Integer> prices) {

    public void increasePrices(int amount, int maximum) {
        for (int i = 0; i < prices.size(); i++) {
            Integer price = prices.get(i);
            int current = price+amount;
            if (price>=maximum) continue;
            prices.set(i, Math.min(current, maximum));
        }
    }
}
