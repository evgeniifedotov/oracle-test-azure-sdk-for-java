package com.oracle.pic.orp;

import com.azure.core.http.rest.Response;
import com.azure.resourcemanager.oracledatabase.OracleDatabaseManager;
import com.azure.resourcemanager.oracledatabase.models.AutonomousDatabase;

public class Adbs {
    public static Response<AutonomousDatabase> GetById(OracleDatabaseManager manager, String Id) {
        return manager.autonomousDatabases()
                .getByIdWithResponse(Id, com.azure.core.util.Context.NONE);
    }
}