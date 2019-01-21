package fr.sictiam.stela.convocationservice.model.util;

import fr.sictiam.stela.convocationservice.model.Right;

import java.util.List;
import java.util.Set;

public class RightUtils {

    public static Boolean hasRight(Set<Right> rights, List<Right> authorizedRights) {
        return rights.stream().anyMatch(right -> authorizedRights.contains(right));
    }
}
