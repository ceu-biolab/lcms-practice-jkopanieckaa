package lipid;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for lipid ordering, annotation, etc.
 */
public final class LipidUtils {

    // Define an explicit rank for each lipid type (PG lowest, PC highest)
    private static final Map<String, Integer> TYPE_RANK;
    static {
        TYPE_RANK = new HashMap<>();
        TYPE_RANK.put("PG", 1);
        TYPE_RANK.put("PE", 2);
        TYPE_RANK.put("PI", 3);
        TYPE_RANK.put("PA", 4);
        TYPE_RANK.put("PS", 5);
        TYPE_RANK.put("PC", 6);
    }

    private LipidUtils() {
        // no-instantiation
    }

    /**
     * Returns the ordering rank for a given lipid type string.
     * PG=1, PE=2, PI=3, PA=4, PS=5, PC=6; unknown types return 0.
     * @param type lipid type code (e.g. "PE")
     * @return integer rank, higher means elutes later
     */
    public static int getTypeRank(String type) {
        return TYPE_RANK.getOrDefault(type, 0);
    }
}
