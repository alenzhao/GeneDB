package org.genedb.querying.tmpquery;

public class GeneSummary implements Comparable<GeneSummary> {
    private String systematicId;

    private String taxonDisplayName;

    private String product;

    private String topLevelFeatureName;

    private int left;

    public GeneSummary() {
        // No-args constructor
    }

    public GeneSummary(String systematicId, String taxonDisplayName,
            String product, String topLevelFeatureName, int left) {
        setSystematicId(systematicId);
        this.taxonDisplayName = taxonDisplayName;
        this.product = product;
        this.topLevelFeatureName = topLevelFeatureName;
        this.left = left;
    }

    public String getSystematicId() {
        return systematicId;
    }

    public void setSystematicId(String systematicId) {
        String munged = removeSuffix(systematicId, ":pep");
        munged = removeSuffix(munged, ":mRNA");
        this.systematicId = munged;
    }

    private String removeSuffix(String original, String suffix) {
        if (original.endsWith(suffix)) {
            return original.substring(0, original.length()-suffix.length());
        }
        return original;
    }

    public String getTaxonDisplayName() {
        return taxonDisplayName;
    }

    public void setTaxonDisplayName(String taxonDisplayName) {
        this.taxonDisplayName = taxonDisplayName;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getTopLevelFeatureName() {
        return topLevelFeatureName;
    }

    public void setTopLevelFeatureName(String topLevelFeatureName) {
        this.topLevelFeatureName = topLevelFeatureName;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    @Override
    public int compareTo(GeneSummary other) {
        int compare = this.taxonDisplayName.compareTo(other.getTaxonDisplayName());
        if (compare != 0) {
            return compare;
        }

        compare = this.topLevelFeatureName.compareTo(other.getTopLevelFeatureName());
        if (compare != 0) {
            return compare;
        }

        if (this.left < other.getLeft()) {
            return -1;
        }
        if (this.left > other.getLeft()) {
            return 1;
        }
        return this.systematicId.compareTo(other.getSystematicId());
    }

}