package fr.sictiam.stela.acteservice.service.util;

import java.util.List;
import java.util.Set;

import fr.sictiam.stela.acteservice.model.Right;

public class RightUtils {

    public static Boolean hasRight(Set<Right> rights, List<Right> authorizedRights) {
        return rights.stream().anyMatch(right -> authorizedRights.contains(right));
    }
}
