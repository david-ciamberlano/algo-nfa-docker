package it.davidlab.algonfa.domain;

public class AssetModel {

    private String txId;
    private String creatorAddress;
    private boolean defaultFrozen;
    private String unitName;
    private String  assetName;
    private long assetTotal;
    private int assetDecimals;
    private String url;
    private Metadata metadata;

    public AssetModel() {
    }

    public AssetModel(boolean defaultFrozen, String unitName, String assetName, long assetTotal, int assetDecimals, String url, Metadata metadata) {
        this.defaultFrozen = defaultFrozen;
        this.unitName = unitName;
        this.assetName = assetName;
        this.assetTotal = assetTotal;
        this.assetDecimals = assetDecimals;
        this.url = url;
        this.metadata = metadata;
    }

    public String getCreatorAddress() {
        return creatorAddress;
    }

    public boolean isDefaultFrozen() {
        return defaultFrozen;
    }

    public String getUnitName() {
        return unitName;
    }

    public String getAssetName() {
        return assetName;
    }

    public long getAssetTotal() {
        return assetTotal;
    }

    public int getAssetDecimals() {
        return assetDecimals;
    }

    public String getUrl() {
        return url;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }
}
