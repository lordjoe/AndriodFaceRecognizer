package com.lordjoe.identifier;

/**
 * com.lordjoe.identifier.IdentificationResult
 * User: Steve
 * Date: 3/25/2017
 */
public class IdentificationResult implements Comparable<IdentificationResult> {
    public final int label;
    public final double confidence;
    public final String source;

    public IdentificationResult(String source, int label, double confidence) {
        this.label = label;
        this.confidence = confidence;
        this.source = source;
    }

    @Override
    public int compareTo(IdentificationResult o) {
        int ret =  Double.compare(confidence, o.confidence);    // lower is better
        if (ret != 0)
            return ret;
        ret = Integer.compare(label, o.label);
        if (ret != 0)
            return ret;
        ret = source.compareTo(o.source);
        return ret;
    }

    @Override
    public String toString() {
        return "IdentificationResult{" +
                "label=" + label +
                ", confidence=" + confidence +
                ", source='" + source + '\'' +
                '}';
    }
}
