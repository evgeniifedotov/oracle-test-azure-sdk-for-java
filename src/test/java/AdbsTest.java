
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
import com.oracle.pic.orp.Adbs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.processing.SupportedAnnotationTypes;

public class AdbsTest {
    com.azure.resourcemanager.oracledatabase.OracleDatabaseManager manager;
    String ADBS_ID = "subscriptions/4aa7be2d-ffd6-4657-828b-31ca25e39985/resourceGroups/CRDRacctestRG-250604021035673240/providers/Oracle.Database/autonomousDatabases/OFakeO250604021035673240";
    @BeforeEach
    public void setup()
    {
        AzureProfile profile = new AzureProfile("9195a8c5-6bc8-41cb-80ef-2e772dbc1f73","4aa7be2d-ffd6-4657-828b-31ca25e39985",AzureCloud.AZURE_PUBLIC_CLOUD);
        IntelliJCredential cr = new IntelliJCredentialBuilder()
                .tenantId("9195a8c5-6bc8-41cb-80ef-2e772dbc1f73")
                .build();
        manager = OracleDatabaseManager
                .authenticate(cr, profile);
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
