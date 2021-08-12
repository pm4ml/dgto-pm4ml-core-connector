package com.modus.client;

import com.datasonnet.Mapper;
import com.datasonnet.document.Document;
import com.datasonnet.document.DefaultDocument;
import com.datasonnet.document.MediaType;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DatasonnetMappingTests2 {

    @Test
    public void testPartiesMappingCustom() throws IOException, JSONException {
        // This libsonnet is not needed for this mapping it's just to show how to import it
        Map<String, String> namedImports = new HashMap<String, String>();
//        namedImports.put("mappings/commonRequest.libsonnet", "{\n" +
//                "  getCommonRequest()::\n" +
//                "  {\n" +
//                "      company: \"MB\",\n" +
//                "      password: \"Test\",\n" +
//                "      userName: \"Test\"\n" +
//                "  }\n" +
//                "}");

        String mappingFilePath = "mappings/getPartiesResponse.ds";
        InputStream mappingStream = this.getClass().getClassLoader().getResourceAsStream(mappingFilePath);
        String mapping = IOUtils.toString(mappingStream);

        String idTypeHeaderJson = "{\"idType\":\"ACCOUNT_ID\"}";
        String idValueHeaderJson = "{\"idValue\":\"123\"}";
        DefaultDocument idTypeHeaderDocument = new DefaultDocument(idTypeHeaderJson, MediaType.valueOf("application/json"));
        DefaultDocument idValueHeaderDocument = new DefaultDocument(idValueHeaderJson, MediaType.valueOf("application/json"));

        Map<String, Document<?>> jsonnetVars = new HashMap<String, Document<?>>();
        jsonnetVars.put("headers", idValueHeaderDocument);
        jsonnetVars.put("header", idValueHeaderDocument);

        jsonnetVars.put("headers", idTypeHeaderDocument);
        jsonnetVars.put("header", idTypeHeaderDocument);

        String inputData = "[{\"Status\":{\"transactionId\":null,\"messageId\":null,\"successIndicator\":\"Success\",\"application\":null},\"ACCTVIEWType\":{\"enquiryInputCollection\":null,\"gACCTVIEWDetailType\":{\"mACCTVIEWDetailType\":{\"ACCOUNTTITLE1\":\"Test Account Name\"}}}}]";
        DefaultDocument payload = new DefaultDocument(inputData, MediaType.valueOf("application/json"));

        Mapper mapper = new Mapper(mapping, jsonnetVars.keySet(), namedImports, true);
        DefaultDocument mappedDoc = (DefaultDocument) mapper.transform(payload, jsonnetVars, MediaType.valueOf("application/json"));
        Object mappedBody = mappedDoc.getContent();

        String expectedOutput = "{\"type\":\"CONSUMER\",\"idType\":\"ACCOUNT_ID\",\"idValue\":\"123\"}";
        JSONAssert.assertEquals(expectedOutput, mappedBody.toString(), true);
    }

}
