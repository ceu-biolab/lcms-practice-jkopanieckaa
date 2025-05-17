package lipid;

import adduct.AdductList;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

/**
 * Class to represent the annotation over a lipid
 */
public class Annotation {

    private final Lipid lipid;
    private final double mz;
    private final double intensity;
    private final double rtMin;
    private final IoniationMode ionizationMode;
    private String adduct;
    private final Set<Peak> groupedSignals;
    private int score;
    private int totalScoresApplied;

    private static final double TOLERANCE = 0.01;

    /**
     * Constructs an Annotation without grouped signals
     */
    public Annotation(Lipid lipid,
                      double mz,
                      double intensity,
                      double retentionTime,
                      IoniationMode ionizationMode) {
        this(lipid, mz, intensity, retentionTime, ionizationMode, Collections.emptySet());
    }

    /**
     * Constructs an Annotation with grouped signals and performs adduct detection
     */
    public Annotation(Lipid lipid,
                      double mz,
                      double intensity,
                      double retentionTime,
                      IoniationMode ionizationMode,
                      Set<Peak> groupedSignals) {
        this.lipid = lipid;
        this.mz = mz;
        this.rtMin = retentionTime;
        this.intensity = intensity;
        this.ionizationMode = ionizationMode;
        // use a HashSet since Peak isn't Comparable
        this.groupedSignals = groupedSignals.isEmpty()
                ? Collections.emptySet()
                : new HashSet<>(groupedSignals);
        this.score = 0;
        this.totalScoresApplied = 0;
        // perform adduct detection
        detectAdduct();
    }

    public Lipid getLipid() { return lipid; }
    public double getMz() { return mz; }
    public double getRtMin() { return rtMin; }
    public String getAdduct() { return adduct; }
    public void setAdduct(String adduct) { this.adduct = adduct; }
    public double getIntensity() { return intensity; }
    public IoniationMode getIonizationMode() { return ionizationMode; }
    public Set<Peak> getGroupedSignals() { return Collections.unmodifiableSet(groupedSignals); }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public void addScore(int delta) { this.score += delta; this.totalScoresApplied++; }
    public double getNormalizedScore() { return totalScoresApplied > 0 ? (double) score / totalScoresApplied : 0.0; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Annotation)) return false;
        Annotation that = (Annotation) o;
        return Double.compare(that.mz, mz) == 0
                && Double.compare(that.rtMin, rtMin) == 0
                && Objects.equals(lipid, that.lipid);
    }
    @Override
    public int hashCode() {
        return Objects.hash(lipid, mz, rtMin);
    }
    @Override
    public String toString() {
        return String.format("Annotation(%s, mz=%.4f, RT=%.2f, adduct=%s, intensity=%.1f, score=%d)",
                lipid.getName(), mz, rtMin, adduct, intensity, score);
    }

    /**
     * Detects an adduct by iterating over positive then negative adduct lists.
     * Implements three nested loops for peaks, neutral mass, and expected mass.
     */
    private void detectAdduct() {
        adduct = "unknown";
        if (groupedSignals.isEmpty()) {
            return;
        }
        // Prepare adduct maps in correct order
        Map<String, Double> firstMap = ionizationMode == IoniationMode.POSITIVE
                ? AdductList.MAPMZPOSITIVEADDUCTS
                : AdductList.MAPMZNEGATIVEADDUCTS;
        Map<String, Double> secondMap = ionizationMode == IoniationMode.POSITIVE
                ? AdductList.MAPMZNEGATIVEADDUCTS
                : AdductList.MAPMZPOSITIVEADDUCTS;

        // Try primary list
        if (scanMaps(firstMap)) return;
        // Fallback
        scanMaps(secondMap);
    }

    /**
     * Scans through each peak, neutral mass, and expected mass in the given adduct map.
     */
    private boolean scanMaps(Map<String, Double> adductMap) {
        for (Peak peak : groupedSignals) {
            double observedMz = peak.getMz();
            // Loop over all adduct entries to calculate neutral mass
            for (Map.Entry<String, Double> entry : adductMap.entrySet()) {
                String candidateName = entry.getKey();
                double delta = entry.getValue();
                double neutralMass = observedMz - delta;
                // Compare expected mass across all entries
                for (Map.Entry<String, Double> inner : adductMap.entrySet()) {
                    double expected = neutralMass + inner.getValue();
                    if (Math.abs(expected - observedMz) <= TOLERANCE) {
                        adduct = candidateName;
                        return true;
                    }
                }
            }
        }
        return false;
    }
}