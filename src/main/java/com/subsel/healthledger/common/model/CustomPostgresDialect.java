package com.subsel.healthledger.common.model;

import org.hibernate.dialect.PostgreSQL10Dialect;

import java.sql.Types;

public class CustomPostgresDialect extends PostgreSQL10Dialect {

    public CustomPostgresDialect() {
        this.registerColumnType(Types.JAVA_OBJECT, "jsonb");
    }
}
