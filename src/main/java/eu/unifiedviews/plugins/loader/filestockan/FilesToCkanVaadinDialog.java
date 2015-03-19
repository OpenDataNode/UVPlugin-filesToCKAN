package eu.unifiedviews.plugins.loader.filestockan;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 */
public class FilesToCkanVaadinDialog extends AbstractDialog<FilesToCkanConfig_V1> {

    private static final long serialVersionUID = -56684360836909428L;

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

        CheckBox box = new CheckBox(this.ctx.tr("FilesToCkanVaadinDialog.replaceExisting"), replaceExisting);
        mainLayout.addComponent(box);

        setCompositionRoot(mainLayout);
    }

    @Override
    public void setConfiguration(FilesToCkanConfig_V1 conf) throws DPUConfigException {
        replaceExisting.setValue(conf.getReplaceExisting());
    }

    @Override
    public FilesToCkanConfig_V1 getConfiguration() throws DPUConfigException {
        FilesToCkanConfig_V1 conf = new FilesToCkanConfig_V1();
        conf.setReplaceExisting(replaceExisting.getValue());
        return conf;
    }

}
