import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestCommon {
    public final static String TENANT_ID = "91";
    public final static String SUBSCRIPTION_ID = "4a";
    public final static String RG_NAME = "java-sdk-test-rg";
    public final static String ADBS_NAME = "javasdktestadbs";
    public final static String ADBS_CRDR_NAME = "javasdktestadbsCRDR";
    public final static String VNET_NAME = "java-sdk-test-vnet";
    public final static String VNET_FRA_NAME = "java-sdk-test-vnet-fra";
    public final static Region REGION = Region.US_EAST;
    public final static List<String> Zones = Arrays.asList("2");
    public final static String EXA_INFRA_NAME = "OFakejavasdktestExaInfra";
    public final static String EXA_SV_NAME = "OFakejavasdktestExaStorageVault";

    public static ResourceGroup createResourceGroup(AzureResourceManager.Authenticated azRm) {
        return azRm.withTenantId(TestCommon.TENANT_ID)
                .withSubscription(TestCommon.SUBSCRIPTION_ID)
                .resourceGroups().define(TestCommon.RG_NAME)
                .withRegion(TestCommon.REGION)
                .create();
    }
}
