package com.kumoe.season_shop;

public enum CoinType {
    GOLD("Gold"),
    COPPER("Copper"),
    SILVER("Silver");
    private final String name;
    CoinType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getSerializedName() {
        return "CoinType: " + this;
    }
}
