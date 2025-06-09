package it.petrinet.view.components;

public enum NetCategory {
    myNets("Owned"),
    mySubs("Subscribed"),
    discover("Discover");

    private final String displayName;

    NetCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static NetCategory fromString(String name) {
        for (NetCategory category : NetCategory.values()) {
            if (category.name().equalsIgnoreCase(name)) {
                return category;
            }
        }
        throw new IllegalArgumentException("No enum constant " + NetCategory.class.getCanonicalName() + "." + name);
    }
}
