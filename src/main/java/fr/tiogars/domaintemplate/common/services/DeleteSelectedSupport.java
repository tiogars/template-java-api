package fr.tiogars.domaintemplate.common.services;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;

import fr.tiogars.domaintemplate.common.models.DeleteSelectedResponse;

/**
 * Applies a single-record deletion to each selected identifier.
 *
 * @since 0.1.0
 */
public final class DeleteSelectedSupport {

    private DeleteSelectedSupport() {
    }

    /**
     * Deletes each distinct identifier, preserving the input order.
     *
     * @param ids    selected identifiers
     * @param delete deletion returning {@code true} when the record was deleted
     * @return deleted and skipped identifiers
     */
    public static DeleteSelectedResponse deleteEach(List<Long> ids, Predicate<Long> delete) {
        List<Long> deleted = new ArrayList<>();
        List<Long> skipped = new ArrayList<>();
        for (Long id : new LinkedHashSet<>(ids)) {
            if (delete.test(id)) {
                deleted.add(id);
            } else {
                skipped.add(id);
            }
        }
        return new DeleteSelectedResponse(List.copyOf(deleted), List.copyOf(skipped));
    }
}
