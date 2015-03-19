package eu.unifiedviews.plugins.loader.filestockan;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class FilesToCkanConfig_V1 {

    private Boolean replaceExisting = Boolean.TRUE;

    public FilesToCkanConfig_V1() {
    }

    public Boolean getReplaceExisting() {
        return this.replaceExisting;
    }

    public void setReplaceExisting(Boolean replaceExisting) {
        this.replaceExisting = replaceExisting;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }
}
