package org.example.model.constants;

public class DbConstants {

    public static class DbSourceData {

        public static final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
        public static final String URL = "jdbc:mysql://localhost:3306/pos";
        public static final String USERNAME = "root";
        public static final String PASSWORD = "DiaryMilk123$";

    }

    public static class EntityPackage {

        public static final String PACKAGE_TO_SCAN = "org.example.pojo";

    }

    public static class HibernateProperties {

        public static final String DIALECT_PROPERTY_1 = "hibernate.dialect";
        public static final String DIALECT_PROPERTY_2 = "org.hibernate.dialect.MySQL8Dialect";
        public static final String HIBERNATE_PROPERTY_1 = "hibernate.hbm2ddl.auto";
        public static final String HIBERNATE_PROPERTY_2 = "update";
        public static final String SQL_PROPERTY_1 = "hibernate.show_sql";
        public static final String SQL_PROPERTY_2 = "true";

    }
}
