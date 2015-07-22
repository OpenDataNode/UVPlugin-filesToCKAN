package org.opendatanode.plugins.loader.filestockan;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import eu.unifiedviews.plugins.loader.filestockan.FilesToCkanConfig_V1;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 */
public class FilesToCkanVaadinDialog extends AbstractDialog<FilesToCkanConfig_V1> {

    private static final long serialVersionUID = -56684360836909428L;

    private ObjectProperty<Boolean> replaceExisting = new ObjectProperty<Boolean>(Boolean.TRUE);

    private ObjectProperty<Boolean> useFileNameAsResourceName = new ObjectProperty<Boolean>(Boolean.FALSE);

    private TextField txtResourceName;

    private VerticalLayout mainLayout;

    public FilesToCkanVaadinDialog() {
        super(FilesToCkan.class);
    }

    @Override
    protected void buildDialogLayout() {
        // top-level component properties
        setWidth("100%");
        setHeight("100%");

        this.mainLayout = new VerticalLayout();
        this.mainLayout.setWidth("100%");
        this.mainLayout.setHeight("-1px");
        this.mainLayout.setSpacing(true);
        this.mainLayout.setMargin(true);

        this.txtResourceName = new TextField();
        this.txtResourceName.setNullRepresentation("");
        this.txtResourceName.setRequired(false);
        this.txtResourceName.setCaption(this.ctx.tr("FilesToCkanVaadinDialog.ckan.resource.name"));
        this.txtResourceName.setWidth("100%");
        this.txtResourceName.setDescription(this.ctx.tr("FilesToCkanVaadinDialog.resource.name.help"));
        this.mainLayout.addComponent(this.txtResourceName);

        CheckBox useFileNameAsResourceNameCheckBox = new CheckBox(this.ctx.tr("FilesToCkanVaadinDialog.useFileName"), this.useFileNameAsResourceName);
        useFileNameAsResourceNameCheckBox.setDescription(this.ctx.tr("FilesToCkanVaadinDialog.useFileName.help"));
        this.mainLayout.addComponent(useFileNameAsResourceNameCheckBox);

        CheckBox replaceCheckBox = new CheckBox(this.ctx.tr("FilesToCkanVaadinDialog.replaceExisting"), this.replaceExisting);
        this.mainLayout.addComponent(replaceCheckBox);

        setCompositionRoot(this.mainLayout);
    }

    @Override
    public void setConfiguration(FilesToCkanConfig_V1 conf) throws DPUConfigException {
        this.replaceExisting.setValue(conf.getReplaceExisting());
        this.txtResourceName.setValue(conf.getResourceName());
        this.useFileNameAsResourceName.setValue(conf.isUseFileNameAsResourceName());
    }

    @Override
    public FilesToCkanConfig_V1 getConfiguration() throws DPUConfigException {
        boolean isValid = (this.txtResourceName.getValue() != null && !this.txtResourceName.getValue().equals(""))
                || this.useFileNameAsResourceName.getValue();
        if (!isValid) {
            throw new DPUConfigException(this.ctx.tr("FilesToCkanVaadinDialog.errors.configuration"));
        }

        FilesToCkanConfig_V1 conf = new FilesToCkanConfig_V1();
        conf.setReplaceExisting(this.replaceExisting.getValue());
        conf.setResourceName(this.txtResourceName.getValue());
        conf.setUseFileNameAsResourceName(this.useFileNameAsResourceName.getValue());
        return conf;
    }

}
