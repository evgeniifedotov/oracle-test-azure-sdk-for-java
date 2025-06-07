
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.IntelliJCredential;
import com.azure.identity.IntelliJCredentialBuilder;
import com.azure.resourcemanager.oracledatabase.OracleDatabaseManager;
import com.azure.resourcemanager.oracledatabase.models.AutonomousDatabase;
import com.azure.resourcemanager.resources.fluent.ResourceManagementClient;
import com.azure.resourcemanager.resources.implementation.ResourceManagementClientBuilder;
import com.oracle.pic.orp.Adbs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.processing.SupportedAnnotationTypes;

public class AdbsTest {
    private static OracleDatabaseManager dbManager;
    private static AutonomousDatabase testDb;
    private static ResourceManagementClient rmClient;
    private String ADBS_ID = "subscriptions/4aa7be2d-ffd6-4657-828b-31ca25e39985/resourceGroups/CRDRacctestRG-250604021035673240/providers/Oracle.Database/autonomousDatabases/OFakeO250604021035673240";

 //   var client = new Microsoft.Azure.Management.Resources.ResourceManagementClient(credentials);
   // var result = c.ResourceGroups.CreateOrUpdateAsync("MyResourceGroup", new Microsoft.Azure.Management.Resources.Models.ResourceGroup("West US"), new System.Threading.CancellationToken()).Result;
    @BeforeAll
    static void setUp() {
        AzureProfile profile = new AzureProfile(TENANT_ID,SUBSCRIPTION_ID,AzureCloud.AZURE_PUBLIC_CLOUD);
        IntelliJCredential cr = new IntelliJCredentialBuilder()
                .tenantId("9195a8c5-6bc8-41cb-80ef-2e772dbc1f73")
                .build();
        dbManager = OracleDatabaseManager
                .authenticate(cr, profile);
rmClient = new ResourceManagementClientBuilder().subscriptionId().buildClient();
        testDb = manager.autonomousDatabases().define("SDKTEST").withRegion("useast").withExistingResourceGroup()
    }
    @BeforeEach
    public void setup()
    {

    }
    @Test
    public void getAdbsByName()
    {
        Response<AutonomousDatabase> response = Adbs.GetById(manager, ADBS_ID);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotNull(response.getHeaders().getValue("id"));
    }
}
