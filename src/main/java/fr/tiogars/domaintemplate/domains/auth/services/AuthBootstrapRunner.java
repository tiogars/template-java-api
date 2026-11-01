package fr.tiogars.domaintemplate.domains.auth.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fr.tiogars.domaintemplate.config.AdminAccountProperties;
import fr.tiogars.domaintemplate.domains.auth.role.services.RoleService;
import fr.tiogars.domaintemplate.domains.auth.user.services.UserService;

/** Seeds mandatory roles and the forced administrator account. */
@Component
class AuthBootstrapRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthBootstrapRunner.class);

    private final AdminAccountProperties adminAccountProperties;
    private final RoleService roleService;
    private final UserService userService;

    AuthBootstrapRunner(AdminAccountProperties adminAccountProperties, RoleService roleService, UserService userService) {
        this.adminAccountProperties = adminAccountProperties;
        this.roleService = roleService;
        this.userService = userService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        roleService.seedDefaultRoles();
        userService.forceAdminUser(adminAccountProperties.username(), adminAccountProperties.password());
        LOGGER.info("Forced administrator account '{}' is ready", adminAccountProperties.username());
    }
}