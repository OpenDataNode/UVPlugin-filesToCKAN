package eu.unifiedviews.plugins.loader.filestockan;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dpu.exec.UserExecContext;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlProblemException;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

public class DistributionMetadataToResourceConverter {
    private static final Logger logger = LoggerFactory.getLogger(DistributionMetadataToResourceConverter.class);

    private MetadataDataUnit metadata;

    private UserExecContext ctx;

    public DistributionMetadataToResourceConverter(MetadataDataUnit metadata, UserExecContext userExecContext) {
        super();
        this.metadata = metadata;
        this.ctx = userExecContext;
    }

    public List<Map<String, String>> convert() throws SparqlProblemException, MalformedQueryException, DataUnitException, DPUException {
        List<Map<String, String>> resources = new ArrayList<>();
        String datasetURI = executeSimpleSelectQuery("SELECT ?d WHERE {?d a <" + CKANLoaderVocabulary.DCAT_DATASET_CLASS + ">}", "d");

        LinkedList<String> distributions = new LinkedList<String>();
        for (Map<String, Value> map : executeSelectQuery("SELECT ?distribution WHERE {<" + datasetURI + "> <" + CKANLoaderVocabulary.DCAT_DISTRIBUTION + "> ?distribution }")) {
            distributions.add(map.get("distribution").stringValue());
        }

        //Distributions
        for (String distribution : distributions) {
            Map<String, String> distro = new LinkedHashMap<>();

            String dtitle = executeSimpleSelectQuery("SELECT ?title WHERE {<" + distribution + "> <" + DCTERMS.TITLE + "> ?title FILTER(LANGMATCHES(LANG(?title), \"cs\"))}", "title");
            String ddescription = executeSimpleSelectQuery("SELECT ?description WHERE {<" + distribution + "> <" + DCTERMS.DESCRIPTION + "> ?description FILTER(LANGMATCHES(LANG(?description), \"cs\"))}", "description");
            String dtemporalStart = executeSimpleSelectQuery("SELECT ?temporalStart WHERE {<" + distribution + "> <" + DCTERMS.TEMPORAL + ">/<" + CKANLoaderVocabulary.SCHEMA_STARTDATE + "> ?temporalStart }", "temporalStart");
            String dtemporalEnd = executeSimpleSelectQuery("SELECT ?temporalEnd WHERE {<" + distribution + "> <" + DCTERMS.TEMPORAL + ">/<" + CKANLoaderVocabulary.SCHEMA_ENDDATE + "> ?temporalEnd }", "temporalEnd");
//              String dspatial = executeSimpleSelectQuery("SELECT ?spatial WHERE {<" + distribution + "> <"+ DCTERMS.SPATIAL + "> ?spatial }", "spatial");
            String dschemaURL = executeSimpleSelectQuery("SELECT ?schema WHERE {<" + distribution + "> <" + CKANLoaderVocabulary.WDRS_DESCRIBEDBY + "> ?schema }", "schema");
            String dschemaType = executeSimpleSelectQuery("SELECT ?schema WHERE {<" + distribution + "> <" + CKANLoaderVocabulary.POD_DISTRIBUTION_DESCRIBREBYTYPE + "> ?schema }", "schema");
            String dissued = executeSimpleSelectQuery("SELECT ?issued WHERE {<" + distribution + "> <" + DCTERMS.ISSUED + "> ?issued }", "issued");
            String dmodified = executeSimpleSelectQuery("SELECT ?modified WHERE {<" + distribution + "> <" + DCTERMS.MODIFIED + "> ?modified }", "modified");
            String dlicense = executeSimpleSelectQuery("SELECT ?license WHERE {<" + distribution + "> <" + DCTERMS.LICENSE + "> ?license }", "license");
            String dformat = executeSimpleSelectQuery("SELECT ?format WHERE {<" + distribution + "> <" + DCTERMS.FORMAT + "> ?format }", "format");
            String dwnld = executeSimpleSelectQuery("SELECT ?dwnld WHERE {<" + distribution + "> <" + CKANLoaderVocabulary.DCAT_DOWNLOADURL + "> ?dwnld }", "dwnld");

            if (!dtitle.isEmpty())
                distro.put("name", dtitle);
            if (!ddescription.isEmpty())
                distro.put("description", ddescription);
            if (!dlicense.isEmpty())
                distro.put("license_link", dlicense);
            if (!dtemporalStart.isEmpty())
                distro.put("temporal_start", dtemporalStart);
            if (!dtemporalEnd.isEmpty())
                distro.put("temporal_end", dtemporalEnd);
            if (!dschemaURL.isEmpty())
                distro.put("describedBy", dschemaURL);
            if (!dschemaType.isEmpty())
                distro.put("describedByType", dschemaType);
            if (!dformat.isEmpty())
                distro.put("format", dformat);
            if (!dformat.isEmpty())
                distro.put("mimetype", dformat);
            if (!dwnld.isEmpty())
                distro.put("url", dwnld);
            if (!distribution.isEmpty())
                distro.put("distro_url", distribution);

            distro.put("resource_type", "file");

            if (!dissued.isEmpty())
                distro.put("created", dissued);
            if (!dmodified.isEmpty())
                distro.put("last_modified", dmodified);

//              if (!dspatial.isEmpty()) {
//                  distro.put("ruian_type", "ST");
//                  distro.put("ruian_code", 1);
//                  distro.put("spatial_uri", dspatial);
//              }

            resources.add(distro);
        }
        return resources;
    }

    private String executeSimpleSelectQuery(final String queryAsString, String bindingName) throws SparqlProblemException, DataUnitException, MalformedQueryException, DPUException {
        // Prepare SPARQL update query.
        final SparqlUtils.SparqlSelectObject query = SparqlUtils.createSelect(queryAsString,
                DataUnitUtils.getEntries(metadata, RDFDataUnit.Entry.class));
        final SparqlUtils.QueryResultCollector result = new SparqlUtils.QueryResultCollector();
        result.prepare();
        RepositoryConnection connection = null;
        try {
            connection = metadata.getConnection();
            SparqlUtils.execute(connection, ctx, query, result);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                }
            }
        }
        if (result.getResults().size() == 1) {
            return result.getResults().get(0).get(bindingName).stringValue();
        } else if (result.getResults().isEmpty()) {
            return "";
        } else {
            throw new DataUnitException("Unexpected number of results: " + result.getResults().size());
        }
    }

    private List<Map<String, Value>> executeSelectQuery(final String queryAsString) throws SparqlProblemException, DataUnitException, MalformedQueryException, DPUException {
        // Prepare SPARQL update query.
        SparqlUtils.SparqlSelectObject query;
        query = SparqlUtils.createSelect(queryAsString,
                DataUnitUtils.getEntries(metadata, RDFDataUnit.Entry.class));
        final SparqlUtils.QueryResultCollector result = new SparqlUtils.QueryResultCollector();
        result.prepare();
        RepositoryConnection connection = null;
        try {
            connection = metadata.getConnection();
            SparqlUtils.execute(connection, ctx, query, result);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                }
            }
        }
        return result.getResults();
    }
}
