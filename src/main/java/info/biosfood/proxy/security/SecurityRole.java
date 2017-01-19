package info.biosfood.proxy.security;

public enum SecurityRole {
    DEFAULT(0), EXPERIENCED(200), ROOT(300);

    private int level = 0;

    SecurityRole(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

}
