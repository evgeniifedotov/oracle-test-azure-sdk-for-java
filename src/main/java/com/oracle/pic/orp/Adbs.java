package com.oracle.pic.orp;

import com.azure.core.http.rest.Response;
import com.azure.core.management.Region;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.oracledatabase.OracleDatabaseManager;
import com.azure.resourcemanager.oracledatabase.implementation.AutonomousDatabasesImpl;
import com.azure.resourcemanager.oracledatabase.models.*;
import com.azure.resourcemanager.resources.models.ResourceGroup;

import java.util.ArrayList;

public class Adbs {
    public static Response<AutonomousDatabase> GetById(OracleDatabaseManager manager, String Id) {
        return manager.autonomousDatabases()
                .getByIdWithResponse(Id, com.azure.core.util.Context.NONE);
    }
    public static AutonomousDatabase Create(OracleDatabaseManager dbManager, String name, Region region, ResourceGroup rg, Network nw) {
        return dbManager.autonomousDatabases().define(name)
                .withRegion(region)
                .withExistingResourceGroup(rg.name())
                .withProperties(createAdbsProperties(name, nw))
                .create();
    }

    private static AutonomousDatabaseBaseProperties createAdbsProperties(String name, Network nw)
    {
        ArrayList<CustomerContact> customers =  new ArrayList<>();
        customers.add(new CustomerContact().withEmail("test@test.com"));
        return new AutonomousDatabaseProperties()
                .withDisplayName(name)
                .withComputeModel(ComputeModel.ECPU)
                .withComputeCount(2.0)
                .withLicenseModel(LicenseModel.LICENSE_INCLUDED)
                .withBackupRetentionPeriodInDays(12)
                .withIsAutoScalingEnabled(false)
                .withIsAutoScalingForStorageEnabled(false)
                .withIsMtlsConnectionRequired(false)
                .withDataStorageSizeInTbs(1)
                .withDbWorkload(WorkloadType.DW)
                .withAdminPassword("T")
                .withDbVersion("19c")
                .withSubnetId(nw.subnets().get("delegated").id())
                .withPermissionLevel(PermissionLevelType.RESTRICTED)
                .withAutonomousMaintenanceScheduleType(AutonomousMaintenanceScheduleType.REGULAR)
                .withVnetId(nw.id())
                .withCustomerContacts(customers);
    }

}