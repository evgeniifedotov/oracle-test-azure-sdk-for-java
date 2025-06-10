
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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdbsTest {
    private static OracleDatabaseManager dbManager;
    private static AutonomousDatabase testDb;
    private static AutonomousDatabase testDbCRDR;
    private static AzureResourceManager.Authenticated azRm;
    private final static String TENANT_ID = "91";
    private final static String SUBSCRIPTION_ID = "4a";
    private final static String RG_NAME = "java-sdk-test-rg";
    private final static String ADBS_NAME = "javasdktestadbs";
    private final static String ADBS_CRDR_NAME = "javasdktestadbsCRDR";
    private final static String VNET_NAME = "java-sdk-test-vnet";
    private final static String VNET_FRA_NAME = "java-sdk-test-vnet-fra";
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
       network = CreateVnet(azRm, VNET_NAME, REGION);
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
    public void getAdbsById()
    {
        Response<AutonomousDatabase> response = Adbs.GetById(dbManager, "/subscriptions/4aa7be2d-ffd6-4657-828b-31ca25e39985/resourceGroups/java-sdk-test-rg/providers/Oracle.Database/autonomousDatabases/javasdktestadbs"); //testDb.id());
        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("eastus", response.getValue().location());
        Assertions.assertNotNull(response.getValue().properties().ocid());
    }

    @Test
    @Order(3)
    public void createAdbsCRDR()
    {
        Network network = CreateVnet(azRm, VNET_FRA_NAME, Region.GERMANY_WEST_CENTRAL);
        testDbCRDR = Adbs.CreateCRDR(dbManager, ADBS_CRDR_NAME, Region.GERMANY_WEST_CENTRAL, rg, network,
                "subscriptions/4aa7be2d-ffd6-4657-828b-31ca25e39985/resourceGroups/java-sdk-test-rg/providers/Oracle.Database/autonomousDatabases/javasdktestadbs");
        Assertions.assertNotNull(testDbCRDR);
        Assertions.assertNotNull(testDbCRDR.id());
    }

    private static Network CreateVnet(AzureResourceManager.Authenticated azRm, String name, Region region){
     return  azRm.withTenantId(TENANT_ID)
               .withSubscription(SUBSCRIPTION_ID).networks().define(name)
               .withRegion(region).withExistingResourceGroup(RG_NAME)
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
