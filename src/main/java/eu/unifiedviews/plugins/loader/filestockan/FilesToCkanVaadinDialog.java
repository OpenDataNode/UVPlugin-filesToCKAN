package eu.unifiedviews.plugins.loader.filestockan;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 */
public class FilesToCkanVaadinDialog extends AbstractDialog<FilesToCkanConfig_V1> {

    private static final long serialVersionUID = -56684360836909428L;

    private ObjectProperty<String> catalogApiLocation = new ObjectProperty<String>("");

    private ObjectProperty<Boolean> replaceExisting = new ObjectProperty<Boolean>(Boolean.TRUE);

    public FilesToCkanVaadinDialog() {
        super(FilesToCkan.class);
    }

    @Override
    protected void buildDialogLayout() {
        FormLayout mainLayout = new FormLayout();

        // top-level component properties
        setWidth("100%");
        setHeight("100%");

        TextField txtCatalogApiLocation = new TextField(this.ctx.tr("FilesToCkanVaadinDialog.catalogApiLocation"), catalogApiLocation);
        txtCatalogApiLocation.setWidth("100%");
        mainLayout.addComponent(txtCatalogApiLocation);

        CheckBox box = new CheckBox(this.ctx.tr("FilesToCkanVaadinDialog.replaceExisting"), replaceExisting);
        mainLayout.addComponent(box);

        setCompositionRoot(mainLayout);
    }

    @Override
    public void setConfiguration(FilesToCkanConfig_V1 conf) throws DPUConfigException {
        catalogApiLocation.setValue(conf.getCatalogApiLocation());
        replaceExisting.setValue(conf.getReplaceExisting());
    }

    @Override
    public FilesToCkanConfig_V1 getConfiguration() throws DPUConfigException {
        FilesToCkanConfig_V1 conf = new FilesToCkanConfig_V1();
        conf.setCatalogApiLocation(catalogApiLocation.getValue());
        conf.setReplaceExisting(replaceExisting.getValue());
        return conf;
    }

    @Override
    public String getDescription() {
        return catalogApiLocation.getValue();
    }

}
