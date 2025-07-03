package com.aegis.portal;

public class App {
    public static void main(String[] args) {
        System.out.println("=== Aegis Portal Sync Application ===");

        if (args.length < 2) {
            System.out.println("Usage: java -jar aegis-portal.jar <PortalEnv> <OktaEnv>");
            System.exit(1);
        }

        String portalEnv = args[0];
        String oktaEnv = args[1];

        System.out.println("Starting user sync with:");
        System.out.println(" - Portal Environment: " + portalEnv);
        System.out.println(" - Okta Environment: " + oktaEnv);

        com.aegis.portal.migration.UserSync.main(args);
        

        System.out.println("User sync process completed.");
    }
}
