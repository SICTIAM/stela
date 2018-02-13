package fr.sictiam.stela.acteservice.service.util;

import fr.sictiam.stela.acteservice.model.Right;

import java.util.List;
import java.util.Set;

public class RightUtils {

    public static Boolean hasRight(Set<Right> rights, List<Right> authorizedRights) {
        return rights.stream().anyMatch(right -> authorizedRights.contains(right));
    }
}
