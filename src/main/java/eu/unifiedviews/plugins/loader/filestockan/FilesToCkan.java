package eu.unifiedviews.plugins.loader.filestockan;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.fileshelper.FilesHelper;
import eu.unifiedviews.helpers.dataunit.resourcehelper.Resource;
import eu.unifiedviews.helpers.dataunit.resourcehelper.ResourceConverter;
import eu.unifiedviews.helpers.dataunit.resourcehelper.ResourceHelpers;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelpers;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import eu.unifiedviews.helpers.dpu.config.ConfigurableBase;
import eu.unifiedviews.helpers.dpu.localization.Messages;

@DPU.AsLoader
public class FilesToCkan extends ConfigurableBase<FilesToCkanConfig_V1> implements ConfigDialogProvider<FilesToCkanConfig_V1> {
    public static final String PROXY_API_ACTION = "action";

    public static final String PROXY_API_PIPELINE_ID = "pipeline_id";

    public static final String PROXY_API_USER_ID = "user_id";

    public static final String PROXY_API_TOKEN = "token";

    public static final String PROXY_API_TYPE = "type";

    public static final String PROXY_API_TYPE_FILE = "FILE";

    public static final String PROXY_API_TYPE_RDF = "RDF";

    public static final String PROXY_API_STORAGE_ID = "storage_id";

    public static final String PROXY_API_DATA = "data";

    public static final String PROXY_API_ATTACHMENT_NAME = "upload";

    public static final String CKAN_API_PACKAGE_SHOW = "package_show";

    public static final String CKAN_API_RESOURCE_UPDATE = "resource_update";

    public static final String CKAN_API_RESOURCE_CREATE = "resource_create";

    public static final String CONFIGURATION_SECRET_TOKEN = "dpu.l-filesToCkan.secret.token";

    public static final String CONFIGURATION_CATALOG_API_LOCATION = "dpu.l-filesToCkan.catalog.api.url";

    private static final Logger LOG = LoggerFactory.getLogger(FilesToCkan.class);

    @DataUnit.AsInput(name = "filesInput")
    public FilesDataUnit filesInput;

    public FilesToCkan() {
        super(FilesToCkanConfig_V1.class);
    }

    @Override
    public void execute(DPUContext dpuContext) throws DPUException, InterruptedException {
        Messages messages = new Messages(dpuContext.getLocale(), getClass().getClassLoader());
        
        String shortMessage = messages.getString("FilesToCkan.execute.start", this.getClass().getSimpleName()); 
        String longMessage = String.valueOf(config);
        dpuContext.sendMessage(DPUContext.MessageType.INFO, shortMessage, longMessage);
        Map<String, String> environment = dpuContext.getEnvironment();
        String secretToken = config.getSecretToken();
        if (secretToken == null || secretToken.isEmpty()) {
            secretToken = environment.get(CONFIGURATION_SECRET_TOKEN);
        }
        if (secretToken == null || secretToken.isEmpty()) {
            secretToken = null;
        }
        String catalogApiLocation = config.getCatalogApiLocation();
        if (catalogApiLocation == null || catalogApiLocation.isEmpty()) {
            catalogApiLocation = environment.get(CONFIGURATION_CATALOG_API_LOCATION);
        }
        if (catalogApiLocation == null || catalogApiLocation.isEmpty()) {
            throw new DPUException( messages.getString("FilesToCkan.execute.exception.missingCatalogApiLocation"));
        }
        String userId = dpuContext.getPipelineOwner();
        String pipelineId = String.valueOf(dpuContext.getPipelineId());

        if (filesInput == null) {
            throw new DPUException(messages.getString("FilesToCkan.execute.exception.missingInput"));
        }

        CloseableHttpResponse response = null;
        Map<String, String> existingResources = new HashMap<>();
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            URIBuilder uriBuilder;
            uriBuilder = new URIBuilder(catalogApiLocation);

            uriBuilder.setPath(uriBuilder.getPath());
            HttpPost httpPost = new HttpPost(uriBuilder.build().normalize());
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                    .addTextBody(PROXY_API_DATA, "{}", ContentType.APPLICATION_JSON.withCharset("UTF-8"))
                    .addTextBody(PROXY_API_ACTION, CKAN_API_PACKAGE_SHOW, ContentType.TEXT_PLAIN.withCharset("UTF-8"));

            if (pipelineId != null) {
                entityBuilder.addTextBody(PROXY_API_PIPELINE_ID, pipelineId, ContentType.TEXT_PLAIN.withCharset("UTF-8"));
            }
            if (userId != null) {
                entityBuilder.addTextBody(PROXY_API_USER_ID, userId, ContentType.TEXT_PLAIN.withCharset("UTF-8"));
            }
            if (secretToken != null) {
                entityBuilder.addTextBody(PROXY_API_TOKEN, secretToken, ContentType.TEXT_PLAIN.withCharset("UTF-8"));
            }
            HttpEntity entity = entityBuilder.build();
            httpPost.setEntity(entity);
            response = client.execute(httpPost);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new DPUException(messages.getString("FilesToCkan.execute.exception.noDataset"));
            }

            JsonReaderFactory readerFactory = Json.createReaderFactory(Collections.<String, Object> emptyMap());
            JsonReader reader = readerFactory.createReader(response.getEntity().getContent());
            JsonObject dataset = reader.readObject();
            JsonArray resources = dataset.getJsonArray("resources");
            if (resources != null) {
                for (JsonObject resource : resources.getValuesAs(JsonObject.class)) {
                    existingResources.put(resource.getString("name"), resource.getString("id"));
                }
            }
        } catch (URISyntaxException | IllegalStateException | IOException ex) {
            throw new DPUException(messages.getString("FilesToCkan.execute.exception.noDataset"), ex);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ex) {
                    LOG.warn("Error in close", ex);
                }
            }
        }

        JsonBuilderFactory factory = Json.createBuilderFactory(Collections.<String, Object> emptyMap());
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            Set<FilesDataUnit.Entry> files;
            try {
                files = FilesHelper.getFiles(filesInput);
            } catch (DataUnitException ex1) {
                throw new DPUException(messages.getString("FilesToCkan.execute.exception.dataunit"), ex1);
            }
            for (FilesDataUnit.Entry file : files) {
                CloseableHttpResponse responseUpdate = null;
                try {
                    String storageId = VirtualPathHelpers.getVirtualPath(filesInput, file.getSymbolicName());
                    if (storageId == null || storageId.isEmpty()) {
                        storageId = file.getSymbolicName();
                    }
                    Resource resource = ResourceHelpers.getResource(filesInput, file.getSymbolicName());
                    resource.setName(storageId);
                    JsonObjectBuilder resourceBuilder = buildResource(factory, resource);
                    if (existingResources.containsKey(storageId)) {
                        if (config.getReplaceExisting()) {
                            resourceBuilder.add("id", existingResources.get(storageId));
                        } else {
                            throw new DPUException(messages.getString("FilesToCkan.execute.exception.replaceExisting", storageId));
                        }
                    }

                    URIBuilder uriBuilder = new URIBuilder(catalogApiLocation);
                    uriBuilder.setPath(uriBuilder.getPath());
                    HttpPost httpPost = new HttpPost(uriBuilder.build().normalize());
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                            .addTextBody(PROXY_API_TYPE, PROXY_API_TYPE_FILE, ContentType.TEXT_PLAIN.withCharset("UTF-8"))
                            .addTextBody(PROXY_API_STORAGE_ID, storageId, ContentType.TEXT_PLAIN.withCharset("UTF-8"))
                            .addTextBody(PROXY_API_DATA, resourceBuilder.build().toString(), ContentType.APPLICATION_JSON.withCharset("UTF-8"));

                    if (pipelineId != null) {
                        builder.addTextBody(PROXY_API_PIPELINE_ID, pipelineId, ContentType.TEXT_PLAIN.withCharset("UTF-8"));
                    }
                    if (userId != null) {
                        builder.addTextBody(PROXY_API_USER_ID, userId, ContentType.TEXT_PLAIN.withCharset("UTF-8"));
                    }
                    if (secretToken != null) {
                        builder.addTextBody(PROXY_API_TOKEN, secretToken, ContentType.TEXT_PLAIN.withCharset("UTF-8"));
                    }
                    if (existingResources.containsKey(storageId)) {
                        builder.addTextBody(PROXY_API_ACTION, CKAN_API_RESOURCE_UPDATE, ContentType.TEXT_PLAIN.withCharset("UTF-8"));
                    } else {
                        builder.addTextBody(PROXY_API_ACTION, CKAN_API_RESOURCE_CREATE, ContentType.TEXT_PLAIN.withCharset("UTF-8"));
                    }
                    builder.addBinaryBody(PROXY_API_ATTACHMENT_NAME, new File(URI.create(file.getFileURIString())), ContentType.DEFAULT_BINARY, storageId);
                    HttpEntity entity = builder.build();
                    httpPost.setEntity(entity);

                    responseUpdate = client.execute(httpPost);
                    if (responseUpdate.getStatusLine().getStatusCode() == 200) {
                        JsonReaderFactory readerFactory = Json.createReaderFactory(Collections.<String, Object> emptyMap());
                        JsonReader reader = readerFactory.createReader(responseUpdate.getEntity().getContent());
                        JsonObject resourceResponse = reader.readObject();
                        if (resourceResponse.getBoolean("success")) {
                            LOG.info("Response:" + EntityUtils.toString(responseUpdate.getEntity()));
                        } else {
                            LOG.warn("Response:" + EntityUtils.toString(responseUpdate.getEntity()));
                            throw new DPUException(messages.getString("FilesToCkan.execute.exception.fail"));
                        }
                    } else {
                        LOG.warn("Response:" + EntityUtils.toString(responseUpdate.getEntity()));
                        throw new DPUException(messages.getString("FilesToCkan.execute.exception.fail"));
                    }
                } catch (DPUException ex) {
                    throw ex;
                } catch (UnsupportedRDFormatException | DataUnitException | IOException | URISyntaxException ex) {
                    throw new DPUException(messages.getString("FilesToCkan.execute.exception.fail"), ex);
                } finally {
                    if (responseUpdate != null) {
                        try {
                            responseUpdate.close();
                        } catch (IOException ex) {
                            LOG.warn("Error in close", ex);
                        }
                    }
                }
            }
        } finally {
            try {
                client.close();
            } catch (IOException ex) {
                LOG.warn("Error in close", ex);
            }
        }
    }

    @Override
    public AbstractConfigDialog<FilesToCkanConfig_V1> getConfigurationDialog() {
        return new FilesToCkanVaadinDialog();
    }

    private JsonObjectBuilder buildResource(JsonBuilderFactory factory, Resource resource) {
        JsonObjectBuilder resourceExtrasBuilder = factory.createObjectBuilder();
        for (Map.Entry<String, String> mapEntry : ResourceConverter.extrasToMap(resource.getExtras()).entrySet()) {
            resourceExtrasBuilder.add(mapEntry.getKey(), mapEntry.getValue());
        }

        JsonObjectBuilder resourceBuilder = factory.createObjectBuilder();
        for (Map.Entry<String, String> mapEntry : ResourceConverter.resourceToMap(resource).entrySet()) {
            resourceBuilder.add(mapEntry.getKey(), mapEntry.getValue());
        }
        resourceBuilder.add("extras", resourceExtrasBuilder);

        return resourceBuilder;
    }
}
