import com.azure.core.http.rest.Response;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.identity.IntelliJCredential;
import com.azure.identity.IntelliJCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.oracledatabase.OracleDatabaseManager;
import com.azure.resourcemanager.oracledatabase.models.AutonomousDatabase;
import com.azure.resourcemanager.oracledatabase.models.CloudExadataInfrastructure;
import com.azure.resourcemanager.oracledatabase.models.ExascaleDbStorageVault;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.xml.implementation.aalto.impl.IoStreamException;
import com.oracle.pic.orp.Adbs;
import com.oracle.pic.orp.Exadata;
import org.junit.jupiter.api.*;

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExadataTest {
    private static OracleDatabaseManager dbManager;
    private static ResourceGroup rg;
    private static AzureResourceManager.Authenticated azRm;
    private static ExascaleDbStorageVault exaStorageVault;
private static CloudExadataInfrastructure exaInfra;
    @BeforeAll
    static void setUp() {
        AzureProfile profile = new AzureProfile(TestCommon.TENANT_ID, TestCommon.SUBSCRIPTION_ID, AzureCloud.AZURE_PUBLIC_CLOUD);
        IntelliJCredential cr = new IntelliJCredentialBuilder()
                .tenantId(TestCommon.TENANT_ID)
                .build();
        dbManager = OracleDatabaseManager
                .authenticate(cr, profile);
        azRm = AzureResourceManager.authenticate(cr, profile);
        rg = TestCommon.createResourceGroup(azRm);
    }

    @Test
    @Order(1)
    public void createExaInfra()
    {
        exaInfra = Exadata.CreateExadataInfra (dbManager, TestCommon.EXA_INFRA_NAME, TestCommon.REGION, rg.name(), TestCommon.Zones);
Assertions.assertNotNull(exaInfra);
Assertions.assertNotNull(exaInfra.id());
        Assertions.assertNotNull(exaInfra.name());
        Assertions.assertNotNull(exaInfra.type());
        String expectedId = String.format("/subscriptions/%s/resourceGroups/%s/providers/Oracle.Database/cloudExadataInfrastructures/%s", TestCommon.SUBSCRIPTION_ID, TestCommon.RG_NAME, TestCommon.EXA_INFRA_NAME);
        Assertions.assertEquals(expectedId, exaInfra.id());
        Assertions.assertEquals(TestCommon.EXA_INFRA_NAME, exaInfra.name());
        Assertions.assertEquals("oracle.database/cloudexadatainfrastructures", exaInfra.type());
    }
    @Test
    @Order(2)
    public void getExaInfra() {
        exaInfra = Exadata.GetExadataInfra(dbManager, TestCommon.EXA_INFRA_NAME, rg.name());
        Assertions.assertNotNull(exaInfra);
        Assertions.assertNotNull(exaInfra.id());
        Assertions.assertNotNull(exaInfra.name());
        Assertions.assertNotNull(exaInfra.type());
        Assertions.assertEquals("oracle.database/cloudexadatainfrastructures", exaInfra.type());
        String expectedId = String.format("/subscriptions/%s/resourceGroups/%s/providers/Oracle.Database/cloudExadataInfrastructures/%s", TestCommon.SUBSCRIPTION_ID, TestCommon.RG_NAME, TestCommon.EXA_INFRA_NAME);
        Assertions.assertEquals(expectedId, exaInfra.id());
    }

    @Test
    @Order(3)
    public void listExaInfra() {
        List<CloudExadataInfrastructure> infrastructureList = Exadata.ListExadataInfraByResourceGroup(dbManager, rg.name());
        Assertions.assertNotNull(infrastructureList);
        Assertions.assertEquals(1, infrastructureList.size());
        String expectedId = String.format("/subscriptions/%s/resourceGroups/%s/providers/Oracle.Database/cloudExadataInfrastructures/%s", TestCommon.SUBSCRIPTION_ID, TestCommon.RG_NAME, TestCommon.EXA_INFRA_NAME);
        Assertions.assertEquals(expectedId, infrastructureList.get(0).id());
    }

    @Test
    @Order(4)
    public void getExaInfraById()
    {
        Response<CloudExadataInfrastructure> response = Exadata.GetExadataInfraById(dbManager, exaInfra.id());
        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("eastus", response.getValue().location());
        Assertions.assertNotNull(response.getValue().properties().ocid());
        Assertions.assertTrue (response.getValue().id().contains("/providers/Oracle.Database/cloudExadataInfrastructures/"));
        Assertions.assertEquals("oracle.database/cloudexadatainfrastructures", response.getValue().type());
    }

    @Test
    @Order(5)
    public void createExaDbStorageVault()
    {
        exaStorageVault = Exadata.CreateExaStorageVault (dbManager, TestCommon.EXA_SV_NAME, TestCommon.REGION, rg.name(), TestCommon.Zones,300);
        Assertions.assertNotNull(exaStorageVault);
        Assertions.assertNotNull(exaStorageVault.id());
        Assertions.assertNotNull(exaStorageVault.name());
        Assertions.assertNotNull(exaStorageVault.type());
        String expectedId = String.format("/subscriptions/%s/resourceGroups/%s/providers/Oracle.Database/exascaleDbStorageVaults/%s", TestCommon.SUBSCRIPTION_ID, TestCommon.RG_NAME, TestCommon.EXA_SV_NAME);
        Assertions.assertEquals(expectedId, exaStorageVault.id());
        Assertions.assertEquals(TestCommon.EXA_SV_NAME, exaStorageVault.name());
        Assertions.assertEquals("oracle.database/exascaledbstoragevaults", exaStorageVault.type());
    }

    @Test
    @Order(6)
    public void createExaDbStorageVaultFailed()
    {
        Assertions.assertThrows(ManagementException.class, ()->Exadata.CreateExaStorageVault (dbManager, TestCommon.EXA_SV_NAME+"Fail", TestCommon.REGION, rg.name(), TestCommon.Zones, 20));
    }

    @Test
    @Order(7)
    public void getExaStorageVaultById()
    {
        Response<ExascaleDbStorageVault> response = Exadata.GetExaStorageVaultById(dbManager, exaStorageVault.id());
        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("eastus", response.getValue().location());
        Assertions.assertNotNull(response.getValue().properties().ocid());
        String expectedId = String.format("/subscriptions/%s/resourceGroups/%s/providers/Oracle.Database/exascaleDbStorageVaults/%s", TestCommon.SUBSCRIPTION_ID, TestCommon.RG_NAME, TestCommon.EXA_SV_NAME);
        Assertions.assertEquals(expectedId, exaStorageVault.id());
        Assertions.assertEquals("oracle.database/exascaledbstoragevaults", response.getValue().type());
        Assertions.assertEquals(TestCommon.EXA_SV_NAME, response.getValue().name());
    }

    @Test
    @Order(8)
    public void getExaStorageVault() {
        exaStorageVault = Exadata.GetExaStorageVault(dbManager, TestCommon.EXA_SV_NAME, rg.name());
        Assertions.assertNotNull(exaStorageVault);
        Assertions.assertNotNull(exaStorageVault.id());
        Assertions.assertNotNull(exaStorageVault.name());
        Assertions.assertNotNull(exaStorageVault.type());
        String expectedId = String.format("/subscriptions/%s/resourceGroups/%s/providers/Oracle.Database/exascaleDbStorageVaults/%s", TestCommon.SUBSCRIPTION_ID, TestCommon.RG_NAME, TestCommon.EXA_SV_NAME);
        Assertions.assertEquals(expectedId, exaStorageVault.id());
        Assertions.assertEquals("oracle.database/exascaledbstoragevaults", exaStorageVault.type());
        Assertions.assertEquals(TestCommon.EXA_SV_NAME, exaStorageVault.name());
    }

    @Test
    @Order(9)
    public void listExaExaStorageVault() {
        List<ExascaleDbStorageVault> infrastructureList = Exadata.ListExaStorageVaultByResourceGroup(dbManager, rg.name());
        Assertions.assertNotNull(infrastructureList);
        Assertions.assertEquals(1, infrastructureList.size());
        String expectedId = String.format("/subscriptions/%s/resourceGroups/%s/providers/Oracle.Database/exascaleDbStorageVaults/%s", TestCommon.SUBSCRIPTION_ID, TestCommon.RG_NAME, TestCommon.EXA_SV_NAME);
        Assertions.assertEquals(expectedId, infrastructureList.get(0).id());
    }

    @Test
    @Order(10)
    public void deleteExaStorageVault()
    {
        Exadata.DeleteExaStorageVault(dbManager, exaStorageVault.id());
        List<ExascaleDbStorageVault> infrastructureList = Exadata.ListExaStorageVaultByResourceGroup(dbManager, rg.name());
        Assertions.assertNotNull(infrastructureList);
        Assertions.assertEquals(0, infrastructureList.size());
    }

    @Test
    @Order(11)
    public void deleteExaInfra() {
        Exadata.DeleteExadataInfra(dbManager, exaInfra.id());
        List<CloudExadataInfrastructure> infrastructureList = Exadata.ListExadataInfraByResourceGroup(dbManager, rg.name());
        Assertions.assertNotNull(infrastructureList);
        Assertions.assertEquals(0, infrastructureList.size());
    }

}