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

    public static AutonomousDatabase CreateCRDR (OracleDatabaseManager dbManager, String name, Region region, ResourceGroup rg, Network nw, String originalDbId)
    {
        Response<AutonomousDatabase> originalDb = Adbs.GetById(dbManager, originalDbId);
       return dbManager.autonomousDatabases().define(name)
                .withRegion(region)
                .withExistingResourceGroup(rg.name())
                .withProperties(createAdbsCRDRProperties(name, nw, originalDb.getValue()))
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
                .withAdminPassword("TestPass#2024#")
                .withDbVersion("19c")
                .withSubnetId(nw.subnets().get("delegated").id())
                .withPermissionLevel(PermissionLevelType.RESTRICTED)
                .withAutonomousMaintenanceScheduleType(AutonomousMaintenanceScheduleType.REGULAR)
                .withVnetId(nw.id())
                .withCustomerContacts(customers);
    }

    private static AutonomousDatabaseBaseProperties createAdbsCRDRProperties(String name, Network nw, AutonomousDatabase originalDb)
    {
        return new AutonomousDatabaseCrossRegionDisasterRecoveryProperties()
                .withDisplayName(name)
                .withComputeModel(originalDb.properties().computeModel())
                .withComputeCount(originalDb.properties().computeCount())
                .withLicenseModel(originalDb.properties().licenseModel())
                .withBackupRetentionPeriodInDays(originalDb.properties().backupRetentionPeriodInDays())
                .withIsAutoScalingEnabled(originalDb.properties().isAutoScalingEnabled())
                .withIsAutoScalingForStorageEnabled(originalDb.properties().isAutoScalingForStorageEnabled())
                .withIsMtlsConnectionRequired(originalDb.properties().isMtlsConnectionRequired())
                .withDataStorageSizeInTbs(originalDb.properties().dataStorageSizeInTbs())
                .withDbWorkload(originalDb.properties().dbWorkload())
                .withAdminPassword(originalDb.properties().adminPassword())
                .withDbVersion(originalDb.properties().dbVersion())
                .withSubnetId(nw.subnets().get("delegated").id())
                .withPermissionLevel(originalDb.properties().permissionLevel())
                .withAutonomousMaintenanceScheduleType(originalDb.properties().autonomousMaintenanceScheduleType())
                .withVnetId(nw.id())
                .withSourceId(originalDb.id())
                .withSourceOcid(originalDb.properties().ocid())
                .withRemoteDisasterRecoveryType(DisasterRecoveryType.ADG)
                .withCustomerContacts(originalDb.properties().customerContacts());
    }

}