package org.example.model.constants;

public class CorsConstants {

    public static class Mapping {

        public static final String MAPPING = "/api/**";

    }

    public static class Origin {

        public static final String DEFAULT = "http://localhost:4200";

    }

    public static class Methods {

        public static final String GET = "GET";
        public static final String PUT = "PUT";
        public static final String POST = "POST";
        public static final String DELETE = "DELETE";
        public static final String OPTIONS = "OPTIONS";

    }

    public static class Headers {

        public static final String ALL = "*";

    }
}
