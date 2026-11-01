package fr.tiogars.domaintemplate.domains.auth.user.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.tiogars.domaintemplate.common.models.DeleteSelectedResponse;
import fr.tiogars.domaintemplate.common.services.DeleteSelectedSupport;

/** Deletes a selection of users in a single transaction. */
@Service
public class UserDeleteSelectedService {

    private final UserService userService;

    public UserDeleteSelectedService(UserService userService) {
        this.userService = userService;
    }

    @Transactional
    public DeleteSelectedResponse deleteSelected(List<Long> ids) {
        return DeleteSelectedSupport.deleteEach(ids, userService::delete);
    }
}
