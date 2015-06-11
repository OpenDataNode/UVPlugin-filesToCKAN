package eu.unifiedviews.plugins.loader.filestockan;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class FilesToCkanConfig_V1 {

    private Boolean replaceExisting = Boolean.TRUE;

    private String resourceName = null;

    private boolean useFileNameAsResourceName = true;

    public FilesToCkanConfig_V1() {
    }

    public Boolean getReplaceExisting() {
        return this.replaceExisting;
    }

    public void setReplaceExisting(Boolean replaceExisting) {
        this.replaceExisting = replaceExisting;
    }

    public String getResourceName() {
        return this.resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public boolean isUseFileNameAsResourceName() {
        return this.useFileNameAsResourceName;
    }

    public void setUseFileNameAsResourceName(boolean useFileNameAsResourceName) {
        this.useFileNameAsResourceName = useFileNameAsResourceName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }
}
