
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.IntelliJCredential;
import com.azure.identity.IntelliJCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.network.fluent.models.SubnetInner;
import com.azure.resourcemanager.network.models.Delegation;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.oracledatabase.OracleDatabaseManager;
import com.azure.resourcemanager.oracledatabase.models.*;
import com.azure.resourcemanager.resources.fluent.ResourceManagementClient;
import com.azure.resourcemanager.resources.implementation.ResourceManagementClientBuilder;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.oracle.pic.orp.Adbs;
import org.junit.jupiter.api.*;

import javax.annotation.processing.SupportedAnnotationTypes;
import java.util.ArrayList;
import java.util.Map;

public class AdbsTest {
    private static OracleDatabaseManager dbManager;
    private static AutonomousDatabase testDb;
    private static AutonomousDatabase testDbCRDR;
    private static AzureResourceManager.Authenticated azRm;
    private final static String TENANT_ID = "91";
    private final static String SUBSCRIPTION_ID = "4";
    private final static String RG_NAME = "java-sdk-test-rg";
    private final static String ADBS_NAME = "javasdktestadbs";
    private final static String VNET_NAME = "java-sdk-test-vnet";
    private final static Region REGION = Region.US_EAST;
    private static ResourceGroup rg;
    private static Network network;
   @BeforeAll
    static void setUp() {
        AzureProfile profile = new AzureProfile(TENANT_ID,SUBSCRIPTION_ID,AzureCloud.AZURE_PUBLIC_CLOUD);
        IntelliJCredential cr = new IntelliJCredentialBuilder()
                .tenantId(TENANT_ID)
                .build();
        dbManager = OracleDatabaseManager
                .authenticate(cr, profile);
        azRm = AzureResourceManager.authenticate(cr, profile);
        rg = createResourceGroup(azRm);
       network = CreateVnet(azRm);
    }

    @Test
    @Order(1)
    public void createAdbs()
    {
        testDb = Adbs.Create(dbManager, ADBS_NAME, REGION, rg, network);
        Assertions.assertNotNull(testDb);
        Assertions.assertNotNull(testDb.id());
    }

    @Test
    @Order(2)
    public void getAdbsByName()
    {
        Response<AutonomousDatabase> response = Adbs.GetById(dbManager, testDb.id());
        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotNull(response.getHeaders().getValue("id"));
    }

    private static Network CreateVnet(AzureResourceManager.Authenticated azRm){
     return  azRm.withTenantId(TENANT_ID)
               .withSubscription(SUBSCRIPTION_ID).networks().define(VNET_NAME)
               .withRegion(REGION).withExistingResourceGroup(RG_NAME)
               .withAddressSpace("10.0.0.0/16")
             .withSubnet("default", "10.0.0.0/24")
             .defineSubnet("delegated")
             .withAddressPrefix("10.0.1.0/24")
             .withDelegation("Oracle.Database/networkAttachments")
             .attach()
             .create();
    }

    private static ResourceGroup createResourceGroup(AzureResourceManager.Authenticated azRm) {
      return azRm.withTenantId(TENANT_ID)
                .withSubscription(SUBSCRIPTION_ID)
                .resourceGroups().define(RG_NAME)
                .withRegion(REGION)
                .create();
    }
}
