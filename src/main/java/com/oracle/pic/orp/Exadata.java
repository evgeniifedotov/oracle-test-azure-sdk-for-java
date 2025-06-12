package com.oracle.pic.orp;

import com.azure.core.http.rest.Response;
import com.azure.core.management.Region;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.oracledatabase.OracleDatabaseManager;
import com.azure.resourcemanager.oracledatabase.models.*;

import java.util.ArrayList;
import java.util.List;

public class Exadata {

    public static CloudExadataInfrastructure CreateExadataInfra(OracleDatabaseManager manager, String infraName, Region region, String rgName, List<String> zones) {
        return manager.cloudExadataInfrastructures().define(infraName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withZones(zones)
                .withProperties(createExaInfraProperties(infraName))
                .create();
    }

    public static CloudExadataInfrastructure GetExadataInfra(OracleDatabaseManager manager, String infraName, String rgName) {
        return manager.cloudExadataInfrastructures().getByResourceGroup(rgName, infraName);
    }

    public static List<CloudExadataInfrastructure> ListExadataInfraByResourceGroup(OracleDatabaseManager dbManager, String rgName) {
        return dbManager.cloudExadataInfrastructures().listByResourceGroup(rgName).stream().toList();
    }

    public static Response<CloudExadataInfrastructure> GetExadataInfraById(OracleDatabaseManager manager, String Id) {
        return manager.cloudExadataInfrastructures()
                .getByIdWithResponse(Id, com.azure.core.util.Context.NONE);
    }

    public static void DeleteExadataInfra(OracleDatabaseManager dbManager, String exaInfraId) {
        dbManager.cloudExadataInfrastructures().deleteById(exaInfraId);
    }

    public static ExascaleDbStorageVault CreateExaStorageVault(OracleDatabaseManager manager, String vaultName, Region region, String rgName, List<String> zones, int size) {
        return manager.exascaleDbStorageVaults().define(vaultName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withZones(zones)
                .withProperties(createExaStorageVaultProperties(vaultName, size))
                .create();
    }
    public static Response<ExascaleDbStorageVault> GetExaStorageVaultById(OracleDatabaseManager manager, String Id) {
        return manager.exascaleDbStorageVaults()
                .getByIdWithResponse(Id, com.azure.core.util.Context.NONE);
    }

    public static List<ExascaleDbStorageVault> ListExaStorageVaultByResourceGroup(OracleDatabaseManager dbManager, String rgName) {
        return dbManager.exascaleDbStorageVaults().listByResourceGroup(rgName).stream().toList();
    }

    public static ExascaleDbStorageVault GetExaStorageVault(OracleDatabaseManager manager, String svName, String rgName) {
        return manager.exascaleDbStorageVaults().getByResourceGroup(rgName, svName);
    }

    public static void DeleteExaStorageVault(OracleDatabaseManager dbManager, String svId) {
        dbManager.exascaleDbStorageVaults().deleteById(svId);
    }


    private static CloudExadataInfrastructureProperties createExaInfraProperties(String name) {
        return new CloudExadataInfrastructureProperties()
                .withDisplayName(name)
                .withComputeCount(2)
                .withShape("Exadata.X9M")
                .withStorageCount(3);
    }

    private static ExascaleDbStorageVaultProperties createExaStorageVaultProperties(String name, int size) {
        return new ExascaleDbStorageVaultProperties()
                .withDisplayName(name)
                .withAdditionalFlashCacheInPercent(100)
                .withTimeZone("UTC")
                .withHighCapacityDatabaseStorageInput(new ExascaleDbStorageInputDetails().withTotalSizeInGbs(size));

    }
}
