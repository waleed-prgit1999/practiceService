package com.example.travel.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings for accounts an administrator creates via POST /api/v1/users.
 *
 * @param defaultPassword the password every admin-created account starts with. Deliberately a
 *     configuration value rather than a compiled-in constant so it can be overridden per
 *     environment (DEFAULT_NEW_USER_PASSWORD) without a code change. Phase 1 has no
 *     password-reset flow, so this password stays valid until the user is given one another way.
 */
@ConfigurationProperties(prefix = "app.security")
public record UserProvisioningProperties(String defaultNewUserPassword) {
}
