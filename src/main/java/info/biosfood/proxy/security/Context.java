package info.biosfood.proxy.security;

/**
 * Absolutely dummy user-specific context, only for holding user roles. For each user context is different.
 * Supposed to be stored in a session. Purpose - demonstration only.
 * */
public class Context {

    private static final Context context = new Context();

    public static Context getInstance() {
        return context;
    }

    SecurityRole securityRole = SecurityRole.DEFAULT;

    String userName = "ANONYMOUS";

    private Context() {

    }

    public SecurityRole getSecurityRole() {
        return securityRole;
    }

    public String getUserName() {
        return userName;
    }

}
