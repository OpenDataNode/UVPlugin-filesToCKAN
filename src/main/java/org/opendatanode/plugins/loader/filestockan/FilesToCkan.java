package org.opendatanode.plugins.loader.filestockan;

import java.io.File;
import java.io.IOException;
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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
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
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
import eu.unifiedviews.helpers.dataunit.resource.Resource;
import eu.unifiedviews.helpers.dataunit.resource.ResourceConverter;
import eu.unifiedviews.helpers.dataunit.resource.ResourceHelpers;
import eu.unifiedviews.helpers.dataunit.resource.ResourceMerger;
import eu.unifiedviews.helpers.dataunit.virtualpath.VirtualPathHelpers;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.plugins.loader.filestockan.FilesToCkanConfig_V1;

@DPU.AsLoader
public class FilesToCkan extends AbstractDpu<FilesToCkanConfig_V1> {
    public static final String distributionSymbolicName = "distributionMetadata";

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

    public static final String CKAN_API_ACTOR_ID = "actor_id";

    public static final String CONFIGURATION_SECRET_TOKEN = "org.opendatanode.CKAN.secret.token";

    public static final String CONFIGURATION_CATALOG_API_LOCATION = "org.opendatanode.CKAN.api.url";

    public static final String CONFIGURATION_HTTP_HEADER = "org.opendatanode.CKAN.http.header.";

    private static final Logger LOG = LoggerFactory.getLogger(FilesToCkan.class);

    @DataUnit.AsInput(name = "filesInput")
    public FilesDataUnit filesInput;

    @DataUnit.AsInput(name = "distributionInput", optional = true)
    public RDFDataUnit distributionInput;

    private DPUContext dpuContext;

    public FilesToCkan() {
        super(FilesToCkanVaadinDialog.class, ConfigHistory.noHistory(FilesToCkanConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        this.dpuContext = this.ctx.getExecMasterContext().getDpuContext();
        String shortMessage = this.ctx.tr("FilesToCkan.execute.start", this.getClass().getSimpleName());
        String longMessage = String.valueOf(config);
        dpuContext.sendMessage(DPUContext.MessageType.INFO, shortMessage, longMessage);
        Map<String, String> environment = dpuContext.getEnvironment();

        String secretToken = environment.get(CONFIGURATION_SECRET_TOKEN);
        if (isEmpty(secretToken)) {
            throw ContextUtils.dpuException(this.ctx, "FilesToCkan.execute.exception.missingSecretToken");
        }

        String catalogApiLocation = environment.get(CONFIGURATION_CATALOG_API_LOCATION);
        if (isEmpty(catalogApiLocation)) {
            throw ContextUtils.dpuException(this.ctx, "FilesToCkan.execute.exception.missingCatalogApiLocation");
        }

        Map<String, String> additionalHttpHeaders = new HashMap<>();
        for (Map.Entry<String, String> configEntry : environment.entrySet()) {
            if (configEntry.getKey().startsWith(CONFIGURATION_HTTP_HEADER)) {
                String headerName = configEntry.getKey().replace(CONFIGURATION_HTTP_HEADER, "");
                String headerValue = configEntry.getValue();
                additionalHttpHeaders.put(headerName, headerValue);
            }
        }

        String userId = (this.dpuContext.getPipelineExecutionOwnerExternalId() != null) ? this.dpuContext
                .getPipelineExecutionOwnerExternalId()
                : this.dpuContext.getPipelineExecutionOwner();
        String pipelineId = String.valueOf(dpuContext.getPipelineId());

        if (filesInput == null) {
            throw ContextUtils.dpuException(this.ctx, "FilesToCkan.execute.exception.missingInput");
        }

        Set<FilesDataUnit.Entry> files;
        try {
            files = FilesHelper.getFiles(this.filesInput);
        } catch (DataUnitException ex1) {
            throw ContextUtils.dpuException(this.ctx, ex1, "FilesToCkan.execute.exception.dataunit");
        }

        if (files.size() != 1 && !this.config.isUseFileNameAsResourceName()) {
            ContextUtils.sendError(this.ctx, "FilesToCkan.execute.exception.filesCount.short",
                    "FilesToCkan.execute.exception.filesCount.long");
            return;
        }

        Resource distributionFromRdfInput = null;
        if (distributionInput != null) {
            if (files.size() != 1) {
                throw ContextUtils.dpuException(this.ctx, "FilesToCkan.execute.exception.tooManyFilesForOneDistribution");
            }
            try {
                distributionFromRdfInput = ResourceHelpers.getResource(distributionInput, distributionSymbolicName);
            } catch (DataUnitException ex) {
                throw ContextUtils.dpuException(this.ctx, "FilesToCkan.execute.exception.dataunit");
            }
        }

        CloseableHttpResponse response = null;
        Map<String, String> existingResources = new HashMap<>();
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            URIBuilder uriBuilder;
            uriBuilder = new URIBuilder(catalogApiLocation);

            uriBuilder.setPath(uriBuilder.getPath());
            HttpPost httpPost = new HttpPost(uriBuilder.build().normalize());
            for (Map.Entry<String, String> additionalHeader : additionalHttpHeaders.entrySet()) {
                httpPost.addHeader(additionalHeader.getKey(), additionalHeader.getValue());
            }
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
                    .addTextBody(PROXY_API_DATA, "{}", ContentType.APPLICATION_JSON.withCharset("UTF-8"))
                    .addTextBody(PROXY_API_PIPELINE_ID, pipelineId, ContentType.TEXT_PLAIN.withCharset("UTF-8"))
                    .addTextBody(PROXY_API_USER_ID, userId, ContentType.TEXT_PLAIN.withCharset("UTF-8"))
                    .addTextBody(PROXY_API_TOKEN, secretToken, ContentType.TEXT_PLAIN.withCharset("UTF-8"))
                    .addTextBody(PROXY_API_ACTION, CKAN_API_PACKAGE_SHOW, ContentType.TEXT_PLAIN.withCharset("UTF-8"));

            HttpEntity entity = entityBuilder.build();
            httpPost.setEntity(entity);
            response = client.execute(httpPost);

            if (response.getStatusLine().getStatusCode() != 200) {
                LOG.error("Response:" + EntityUtils.toString(response.getEntity()));
                throw ContextUtils.dpuException(this.ctx, "FilesToCkan.execute.exception.noDataset");
            }

            // Checking success parameter of CKAN response 
            JsonReaderFactory readerFactory = Json.createReaderFactory(Collections.<String, Object> emptyMap());
            JsonReader reader = readerFactory.createReader(response.getEntity().getContent());
            JsonObject resourceResponse = reader.readObject();
            if (response.getStatusLine().getStatusCode() == 200) {
                if (resourceResponse.getBoolean("success")) {
                    LOG.info("Response:" + EntityUtils.toString(response.getEntity()));
                } else {
                    LOG.warn("Response:" + EntityUtils.toString(response.getEntity()));
                    throw ContextUtils.dpuException(this.ctx, "FilesToCkan.execute.exception.noDataset");
                }
            }

            JsonArray resources = resourceResponse.getJsonObject("result").getJsonArray("resources");
            if (resources != null) {
                for (JsonObject resource : resources.getValuesAs(JsonObject.class)) {
                    String resourceName = resource.getString("name");
                    String resourceId = resource.getString("id");
                    LOG.debug("Found resource with name {} and id {}", resourceName, resourceId);
                    existingResources.put(resourceName, resourceId);
                }
            }
        } catch (URISyntaxException | IllegalStateException | IOException ex) {
            throw ContextUtils.dpuException(this.ctx, ex, "FilesToCkan.execute.exception.noDataset");
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
            for (FilesDataUnit.Entry file : files) {
                CloseableHttpResponse responseUpdate = null;
                boolean bResourceExists = false;
                try {
                    String resourceName = null;
                    if (this.config.getResourceName() != null && !this.config.isUseFileNameAsResourceName()) {
                        resourceName = this.config.getResourceName();
                    }
                    Resource resource = ResourceHelpers.getResource(filesInput, file.getSymbolicName());
                    if (distributionFromRdfInput != null) {
                        Resource mergedDistribution = ResourceMerger.merge(distributionFromRdfInput, resource);
                        resource = mergedDistribution;
                        if (StringUtils.isEmpty(resourceName)) {
                            resourceName = resource.getName();
                        }
                    }
                    if (StringUtils.isEmpty(resourceName) && this.config.isUseFileNameAsResourceName()) {
                        resourceName = VirtualPathHelpers.getVirtualPath(filesInput, file.getSymbolicName());
                    }
                    if (StringUtils.isEmpty(resourceName)) {
                        resourceName = file.getSymbolicName();
                    }
                    if (existingResources.containsKey(resourceName)) {
                        bResourceExists = true;
                        resource.setCreated(null);
                    }
                    resource.setName(resourceName);
                    String fileExtension = getFileExtension(file);
                    if (fileExtension != null && fileExtension.length() > 0) {
                        resource.setFormat(fileExtension);
                    }

                    JsonObjectBuilder resourceBuilder = buildResource(factory, resource);
                    if (bResourceExists) {
                        if (config.getReplaceExisting()) {
                            resourceBuilder.add("id", existingResources.get(resourceName));
                        } else {
                            throw ContextUtils.dpuException(this.ctx, "FilesToCkan.execute.exception.replaceExisting", resourceName);
                        }
                    }

                    URIBuilder uriBuilder = new URIBuilder(catalogApiLocation);
                    uriBuilder.setPath(uriBuilder.getPath());
                    HttpPost httpPost = new HttpPost(uriBuilder.build().normalize());
                    for (Map.Entry<String, String> additionalHeader : additionalHttpHeaders.entrySet()) {
                        httpPost.addHeader(additionalHeader.getKey(), additionalHeader.getValue());
                    }
                    MultipartEntityBuilder builder = MultipartEntityBuilder
                            .create()
                            .addTextBody(PROXY_API_TYPE, PROXY_API_TYPE_FILE, ContentType.TEXT_PLAIN.withCharset("UTF-8"))
                            .addTextBody(PROXY_API_STORAGE_ID, resourceName, ContentType.TEXT_PLAIN.withCharset("UTF-8"))
                            .addTextBody(PROXY_API_PIPELINE_ID, pipelineId, ContentType.TEXT_PLAIN.withCharset("UTF-8"))
                            .addTextBody(PROXY_API_USER_ID, userId, ContentType.TEXT_PLAIN.withCharset("UTF-8"))
                            .addTextBody(PROXY_API_TOKEN, secretToken, ContentType.TEXT_PLAIN.withCharset("UTF-8"))
                            .addTextBody(PROXY_API_DATA, resourceBuilder.build().toString(),
                                    ContentType.APPLICATION_JSON.withCharset("UTF-8"));

                    if (bResourceExists) {
                        builder.addTextBody(PROXY_API_ACTION, CKAN_API_RESOURCE_UPDATE, ContentType.TEXT_PLAIN.withCharset("UTF-8"));
                    } else {
                        builder.addTextBody(PROXY_API_ACTION, CKAN_API_RESOURCE_CREATE, ContentType.TEXT_PLAIN.withCharset("UTF-8"));
                    }
                    String fileName = VirtualPathHelpers.getVirtualPath(filesInput, file.getSymbolicName());
                    if (fileName == null) {
                        fileName = file.getSymbolicName();
                    }
                    builder.addBinaryBody(PROXY_API_ATTACHMENT_NAME, new File(java.net.URI.create(file.getFileURIString())),
                            ContentType.DEFAULT_BINARY, fileName);

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
                            throw ContextUtils.dpuException(this.ctx, "FilesToCkan.execute.exception.fail");
                        }
                    } else {
                        LOG.error("Response:" + EntityUtils.toString(responseUpdate.getEntity()));
                        throw ContextUtils.dpuException(this.ctx, "FilesToCkan.execute.exception.fail");
                    }
                } catch (DPUException ex) {
                    throw ex;
                } catch (UnsupportedRDFormatException | DataUnitException | IOException | URISyntaxException ex) {
                    throw ContextUtils.dpuException(this.ctx, ex, "FilesToCkan.execute.exception.fail");
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

    private static boolean isEmpty(String value) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return false;
    }

    private JsonObjectBuilder buildResource(JsonBuilderFactory factory, Resource resource) {

        JsonObjectBuilder resourceBuilder = factory.createObjectBuilder();
        for (Map.Entry<String, String> mapEntry : ResourceConverter.resourceToMap(resource).entrySet()) {
            resourceBuilder.add(mapEntry.getKey(), mapEntry.getValue());
        }

        for (Map.Entry<String, String> mapEntry : ResourceConverter.extrasToMap(resource.getExtras()).entrySet()) {
            resourceBuilder.add(mapEntry.getKey(), mapEntry.getValue());
        }

        if (this.dpuContext.getPipelineExecutionActorExternalId() != null) {
            resourceBuilder.add(CKAN_API_ACTOR_ID, this.dpuContext.getPipelineExecutionActorExternalId());
        }

        return resourceBuilder;
    }

    private String getFileExtension(FilesDataUnit.Entry file) throws DataUnitException {
        String fileName = VirtualPathHelpers.getVirtualPath(this.filesInput, file.getSymbolicName());
        if (fileName == null) {
            fileName = file.getSymbolicName();
        }
        return FilenameUtils.getExtension(fileName);
    }
}
