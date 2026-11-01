package fr.tiogars.domaintemplate.domains.auth.role.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.tiogars.domaintemplate.common.models.DeleteSelectedResponse;
import fr.tiogars.domaintemplate.common.services.DeleteSelectedSupport;

/** Deletes a selection of roles in a single transaction; system roles are skipped. */
@Service
public class RoleDeleteSelectedService {

    private final RoleService roleService;

    public RoleDeleteSelectedService(RoleService roleService) {
        this.roleService = roleService;
    }

    @Transactional
    public DeleteSelectedResponse deleteSelected(List<Long> ids) {
        return DeleteSelectedSupport.deleteEach(ids, roleService::delete);
    }
}
