package eu.unifiedviews.plugins.loader.filestockan;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class FilesToCkanConfig_V1 {
    private String catalogApiLocation;

    private String secretToken;

    private Boolean replaceExisting = Boolean.TRUE;

    public FilesToCkanConfig_V1() {
    }

    public Boolean getReplaceExisting() {
        return replaceExisting;
    }

    public void setReplaceExisting(Boolean replaceExisting) {
        this.replaceExisting = replaceExisting;
    }

    public String getCatalogApiLocation() {
        return catalogApiLocation;
    }

    public void setCatalogApiLocation(String catalogApiLocation) {
        this.catalogApiLocation = catalogApiLocation;
    }

    public String getSecretToken() {
        return secretToken;
    }

    public void setSecretToken(String secretToken) {
        this.secretToken = secretToken;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }
}
