package com.oracle.pic.orp;

import com.azure.core.http.rest.Response;
import com.azure.core.management.Region;
import com.azure.core.util.Context;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.oracledatabase.OracleDatabaseManager;
import com.azure.resourcemanager.oracledatabase.models.*;
import com.azure.resourcemanager.resources.models.ResourceGroup;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public static AutonomousDatabase Update(OracleDatabaseManager dbManager, String originalDbId) {
        AutonomousDatabase originalDb = Adbs.GetById(dbManager, originalDbId).getValue();
        return originalDb.update()
                .withProperties(createAdbsUpdateProperties())
                .apply();
    }

    public static AutonomousDatabase UpdateBackupRetention(OracleDatabaseManager dbManager, String originalDbId) {
        AutonomousDatabase originalDb = Adbs.GetById(dbManager, originalDbId).getValue();
        return originalDb.update()
                .withProperties(new AutonomousDatabaseUpdateProperties()
                        .withBackupRetentionPeriodInDays(20))
                .apply();
    }

    //Seems that Updating CRDR is not working properly
    // Call goes to Azure, CRDR DB shows "Updating" state, but return value here is null
    public static AutonomousDatabase UpdateCRDR(OracleDatabaseManager dbManager, String rgName, String crdrDbName) {
       return dbManager.autonomousDatabases().changeDisasterRecoveryConfiguration(rgName, crdrDbName ,CreateDisasterRecoveryConfigurationForUpdate());
    }

    public static DisasterRecoveryConfigurationDetails CreateDisasterRecoveryConfigurationForUpdate() {
        return new DisasterRecoveryConfigurationDetails()
                .withDisasterRecoveryType(DisasterRecoveryType.BACKUP_BASED)
                .withIsReplicateAutomaticBackups(true)
               .withIsSnapshotStandby(false)
               .withTimeSnapshotStandbyEnabledTill(OffsetDateTime.now().plusDays(20));
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

    public static List<AutonomousDatabase> ListByResourceGroup (OracleDatabaseManager dbManager, String rgName) {
       return dbManager.autonomousDatabases().listByResourceGroup(rgName).stream().toList();
    }

    // deleteXXXX can be one method, but why not to test another overload whe you have a chance
    public static void deleteCrdrAdbs (OracleDatabaseManager dbManager,String rgName, String adbsName) {
        dbManager.autonomousDatabases().delete(rgName, adbsName, Context.NONE);
    }

    public static void deleteAdbs (OracleDatabaseManager dbManager,String adbsId) {
        dbManager.autonomousDatabases().deleteById(adbsId);
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
    // Warning
    // Cannot simultaneously update the Autonomous Database backup retention period and other attributes
    // Seems that invoking update with same values results in a bad request. So you can run test once and then it fails. You need to change values every run
    private static AutonomousDatabaseUpdateProperties createAdbsUpdateProperties()
    {
        return new AutonomousDatabaseUpdateProperties()
                .withComputeCount(3.0)
                .withIsAutoScalingEnabled(true)
                .withIsAutoScalingForStorageEnabled(true);
    }

    private static AutonomousDatabaseCrossRegionDisasterRecoveryProperties createAdbsCRDRProperties(String name, Network nw, AutonomousDatabase originalDb)
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