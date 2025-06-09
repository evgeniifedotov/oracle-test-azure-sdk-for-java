
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.processing.SupportedAnnotationTypes;
import java.util.ArrayList;
import java.util.Map;

public class AdbsTest {
    private static OracleDatabaseManager dbManager;
    private static AutonomousDatabase testDb;
    private static AzureResourceManager.Authenticated azRm;
    private String ADBS_ID = "/s";
    private final static String TENANT_ID = "9";
    private final static String SUBSCRIPTION_ID = "4a";
    private final static String RG_NAME = "java-sdk-test-rg";
    private final static String ADBS_NAME = "javasdktestadbs";
    private final static String VNET_NAME = "java-sdk-test-vnet";
    private final static Region REGION = Region.US_EAST;
   @BeforeAll
    static void setUp() {
        AzureProfile profile = new AzureProfile(TENANT_ID,SUBSCRIPTION_ID,AzureCloud.AZURE_PUBLIC_CLOUD);
        IntelliJCredential cr = new IntelliJCredentialBuilder()
                .tenantId(TENANT_ID)
                .build();
        dbManager = OracleDatabaseManager
                .authenticate(cr, profile);
        azRm = AzureResourceManager.authenticate(cr, profile);
        ResourceGroup rg = createResourceGroup(azRm);
        testDb = createAdbs(rg);
    }
    @BeforeEach
    public void setup()
    {

    }
    @Test
    public void getAdbsByName()
    {
        Assertions.assertNull(Adbs.Create(null));
/*
        Response<AutonomousDatabase> response = Adbs.GetById(dbManager, ADBS_ID);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotNull(response.getHeaders().getValue("id"));
*/
    }
    private static AutonomousDatabaseBaseProperties createAdbsProperties()
    {
        Network nw = CreateVnet(azRm);
        ArrayList<CustomerContact> customers =  new ArrayList<>();
        customers.add(new CustomerContact().withEmail("test@test.com"));
        AutonomousDatabaseBaseProperties properties = new AutonomousDatabaseBaseProperties()
                .withDisplayName(ADBS_NAME)
               .withComputeModel(ComputeModel.ECPU)
               .withComputeCount(2.0)
                .withLicenseModel(LicenseModel.LICENSE_INCLUDED)
               .withBackupRetentionPeriodInDays(12)
               .withIsAutoScalingEnabled(false)
                .withIsAutoScalingForStorageEnabled(false)
               .withIsMtlsConnectionRequired(false)
                .withDataStorageSizeInTbs(1)
               .withDbWorkload(WorkloadType.DW)
                .withAdminPassword("TestPass#2024#")
                .withDbVersion("19c")
                .withSubnetId(nw.subnets().get("delegated").id())
                .withPermissionLevel(PermissionLevelType.RESTRICTED)
                .withAutonomousMaintenanceScheduleType(AutonomousMaintenanceScheduleType.REGULAR)
              .withVnetId(nw.id())
                .withDatabaseEdition(DatabaseEditionType.STANDARD_EDITION)
                .withCustomerContacts(customers);
        return properties;
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
    private static AutonomousDatabase createAdbs(ResourceGroup rg) {
       try {
           return dbManager.autonomousDatabases().define(ADBS_NAME)
                   .withRegion(REGION)
                   .withExistingResourceGroup(rg.name())
                   .withProperties(createAdbsProperties())
                   .create();
       }catch (Exception e){
           String f = "g";

       }
       return null;
    }
}
