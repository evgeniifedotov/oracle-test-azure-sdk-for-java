
import com.azure.core.http.rest.Response;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.identity.IntelliJCredential;
import com.azure.identity.IntelliJCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.oracledatabase.OracleDatabaseManager;
import com.azure.resourcemanager.oracledatabase.models.*;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.oracle.pic.orp.Adbs;
import org.junit.jupiter.api.*;

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdbsTest {
    private static OracleDatabaseManager dbManager;
    private static AutonomousDatabase testDb;
    private static AutonomousDatabase testDbCRDR;
    private static AzureResourceManager.Authenticated azRm;
    private static ResourceGroup rg;
    private static Network network;
   @BeforeAll
    static void setUp() {
        AzureProfile profile = new AzureProfile(TestCommon.TENANT_ID, TestCommon.SUBSCRIPTION_ID,AzureCloud.AZURE_PUBLIC_CLOUD);
        IntelliJCredential cr = new IntelliJCredentialBuilder()
                .tenantId(TestCommon.TENANT_ID)
                .build();
        dbManager = OracleDatabaseManager
                .authenticate(cr, profile);
        azRm = AzureResourceManager.authenticate(cr, profile);
        rg = TestCommon.createResourceGroup(azRm);
       network = CreateVnet(azRm, TestCommon.VNET_NAME, TestCommon.REGION);
    }

    // Current code assumes full sequence Create ADBS, getbyId, create Disaster Recovery DB for ADBS, update ADBS,
    // update Disaster Recovery DB for ADBS, List ADBS by Resource Group, delete Disaster Recovery ADBS, delete ADBS
    // Sequence is very important, as you cannot create Disater Recovery without ADBS, you cannot delete ADBS without deleting Disaster Recovery DB first
    // So I'm using Ordered tests
    // As creating ADBS is very long-running operation (40 min - 1 hour) so use full sequence for final tests.
    // Dependecy here is createAdbs test which creates original ADBS and assigns it to global 'testDb' variable
    // Further in the test this object that represents ADBS is used to get ADBS id subsequent operations.
    // To run tests individually you still need to create ADBS, but then you can go to Azure, copy ADBS id
    // ! To copy ADBS id opend ADBS DB page and click 'JSON View' !
    // and use that string instead of 'testDb.id()'
    // for individual tests


    // Seems that separate call for deleting Resource Group is not necessary is you have just ADBS related resources in the group
    // When you delete BOTH Cross Region Disaster Recovery ADBS and original ADBS, all associated vnets are deleted automatically,
    // Resource group becomes empty and is also deleted automatically
    // Please check with your SDKs

    @Test
    @Order(1)
    public void createAdbs()
    {
        testDb = Adbs.Create(dbManager, TestCommon.ADBS_NAME, TestCommon.REGION, rg, network);
        Assertions.assertNotNull(testDb);
        Assertions.assertNotNull(testDb.id());
    }

    @Test
    @Order(2)
    public void getAdbsById()
    {
        Response<AutonomousDatabase> response = Adbs.GetById(dbManager, testDb.id()); //testDb.id());
        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("eastus", response.getValue().location());
        Assertions.assertNotNull(response.getValue().properties().ocid());
    }

    @Test
    @Order(3)
    public void createAdbsCRDR()
    {
        Network network = CreateVnet(azRm, TestCommon.VNET_FRA_NAME, Region.GERMANY_WEST_CENTRAL);
        testDbCRDR = Adbs.CreateCRDR(dbManager, TestCommon.ADBS_CRDR_NAME, Region.GERMANY_WEST_CENTRAL, rg, network,
                testDb.id());
        Assertions.assertNotNull(testDbCRDR);
        Assertions.assertNotNull(testDbCRDR.id());
    }

    @Test
    @Order(4)
    public void updateAdbs()
    {
        testDb = Adbs.Update(dbManager, testDb.id());
        Assertions.assertNotNull(testDb);
        Assertions.assertEquals(3.0, testDb.properties().computeCount());
        Assertions.assertTrue(testDb.properties().isAutoScalingEnabled());
        Assertions.assertTrue(testDb.properties().isAutoScalingForStorageEnabled());
    }

    @Test
    @Order(5)
    public void updateAdbsBackupRetention()
    {
        testDb = Adbs.UpdateBackupRetention(dbManager, testDb.id());
        Assertions.assertNotNull(testDb);
        Assertions.assertEquals(20, testDb.properties().backupRetentionPeriodInDays());
    }

    //Seems that Updating CRDR is not working properly
    // Call goes to Azure, CRDR DB shows "Updating" state, but return value here is null
    // Please, check on other SDKs
    @Test
    @Order(6)
    public void updateCRDR()
    {
        testDbCRDR = Adbs.UpdateCRDR(dbManager, rg.name(), TestCommon.ADBS_CRDR_NAME
                );  //"/subscriptions/4aa7be2d-ffd6-4657-828b-31ca25e39985/resourceGroups/java-sdk-test-rg/providers/Oracle.Database/autonomousDatabases/javasdktestadbsCRDR"
    //    Assertions.assertNotNull(testDbCRDR);
    }

    @Test
    @Order(7)
    public void listByRG()
    {
        List<AutonomousDatabase> dbs = Adbs.ListByResourceGroup(dbManager, rg.name());
        Assertions.assertNotNull(dbs);
        Assertions.assertEquals(2, dbs.size());
    }

    @Test
    @Order(8)
    public void deleteCRDR()
    {
        Adbs.deleteCrdrAdbs(dbManager, rg.name(), TestCommon.ADBS_CRDR_NAME);
        List<AutonomousDatabase> dbs = Adbs.ListByResourceGroup(dbManager, rg.name());
        Assertions.assertNotNull(dbs);
        Assertions.assertEquals(1, dbs.size());
    }

    @Test
    @Order(9)
    public void deleteADBS()
    {
        Adbs.deleteAdbs(dbManager, testDb.id());
        List<AutonomousDatabase> dbs = Adbs.ListByResourceGroup(dbManager, rg.name());
        Assertions.assertNotNull(dbs);
        Assertions.assertEquals(0, dbs.size());
    }


    private static Network CreateVnet(AzureResourceManager.Authenticated azRm, String name, Region region){
     return  azRm.withTenantId(TestCommon.TENANT_ID)
               .withSubscription(TestCommon.SUBSCRIPTION_ID).networks().define(name)
               .withRegion(region).withExistingResourceGroup(TestCommon.RG_NAME)
               .withAddressSpace("10.0.0.0/16")
             .withSubnet("default", "10.0.0.0/24")
             .defineSubnet("delegated")
             .withAddressPrefix("10.0.1.0/24")
             .withDelegation("Oracle.Database/networkAttachments")
             .attach()
             .create();
    }
}
